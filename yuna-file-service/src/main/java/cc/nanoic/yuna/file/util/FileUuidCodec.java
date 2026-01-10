package cc.nanoic.yuna.file.util;

import cc.nanoic.yuna.common.security.config.JwtProperties;
import cc.nanoic.yuna.file.config.FileStorageProperties;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FileUuidCodec {

    public static final int TABLE_USER_FILE_0 = 0;
    public static final int TABLE_USER_FILE_1 = 1;
    public static final int TABLE_USER_FILE_2 = 2;
    public static final int TABLE_USER_FILE_3 = 3;
    public static final int TABLE_FILE_LOG = 4;

    private static final int TABLE_BITS = 6;
    private static final int TABLE_MASK = (1 << TABLE_BITS) - 1;

    private static final int TIME_BITS = 40;
    private static final int WORKER_BITS = 8;
    private static final int SEQ_BITS = 9;

    private static final long MAX_WORKER = (1L << WORKER_BITS) - 1;
    private static final long MAX_SEQ = (1L << SEQ_BITS) - 1;

    private static final long EPOCH_MS = 1735689600000L;

    private final FileStorageProperties props;
    private final JwtProperties jwtProperties;

    private final SecureRandom random = new SecureRandom();
    private final Object lock = new Object();
    private long lastTs = -1L;
    private long seq = 0L;

    public String nextUserFileUuid(long userId) {
        int shard = shardOfUserId(userId);
        return nextUuidForTable(shard);
    }

    public String nextLogUuid() {
        return nextUuidForTable(TABLE_FILE_LOG);
    }

    public Locate decode(String uuid36) {
        int tableCode = decodeTableCode(uuid36);
        return switch (tableCode) {
            case TABLE_USER_FILE_0 -> new Locate("yuna_file_0", 0);
            case TABLE_USER_FILE_1 -> new Locate("yuna_file_1", 1);
            case TABLE_USER_FILE_2 -> new Locate("yuna_file_2", 2);
            case TABLE_USER_FILE_3 -> new Locate("yuna_file_3", 3);
            case TABLE_FILE_LOG -> new Locate("yuna_file_log", null);
            default -> throw new IllegalArgumentException("Unknown tableCode: " + tableCode);
        };
    }

    public int decodeTableCode(String uuid36) {
        UUID u = UUID.fromString(uuid36);
        byte[] b = uuidToBytes(u);
        int enc = b[8] & TABLE_MASK;
        int seed = secretSeed();
        return feistel6(enc, seed, true);
    }

    public int shardOfUserId(long userId) {
        long a = Math.floorMod(userId, 10);
        return (int) Math.floorMod(a, 4);
    }

    private String nextUuidForTable(int tableCodePlain) {
        if (tableCodePlain < 0 || tableCodePlain > TABLE_MASK) {
            throw new IllegalArgumentException("tableCode out of range: " + tableCodePlain);
        }

        long snow;
        synchronized (lock) {
            long now = nowMs();
            if (now < lastTs) {
                now = waitNextMillis(lastTs);
            }
            if (now == lastTs) {
                seq = (seq + 1) & MAX_SEQ;
                if (seq == 0) {
                    now = waitNextMillis(lastTs);
                }
            } else {
                seq = 0;
            }
            lastTs = now;

            long t = now - EPOCH_MS;
            if (t < 0 || (t >>> TIME_BITS) != 0) {
                throw new IllegalStateException("timestamp out of range");
            }

            long worker = workerId() & MAX_WORKER;
            snow = (t << (WORKER_BITS + SEQ_BITS)) | (worker << SEQ_BITS) | seq;
        }

        int seed = secretSeed();
        int encTable = feistel6(tableCodePlain, seed, false);

        byte[] out = new byte[16];
        ByteBuffer.wrap(out, 0, 8).putLong(snow);
        out[8] = (byte) (encTable & TABLE_MASK);

        byte[] tail = new byte[7];
        random.nextBytes(tail);
        System.arraycopy(tail, 0, out, 9, 7);

        UUID uuid = bytesToUuid(out);
        return uuid.toString();
    }

    private long nowMs() {
        return Instant.now().toEpochMilli();
    }

    private long waitNextMillis(long last) {
        long ts = nowMs();
        while (ts <= last) {
            ts = nowMs();
        }
        return ts;
    }

    private int secretSeed() {
        String s = props.getUuidSecret();
        if (StrUtil.isBlank(s)) {
            s = jwtProperties.getSecret();
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return ByteBuffer.wrap(dig, 0, 4).getInt();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private long workerId() {
        Integer w = props.getWorkerId();
        if (w != null) {
            return w & 0xFFL;
        }
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dig = md.digest(host.getBytes(StandardCharsets.UTF_8));
            return (dig[0] & 0xFFL);
        } catch (Exception e) {
            return 1L;
        }
    }

    private int feistel6(int x, int seed, boolean decrypt) {
        x &= TABLE_MASK;
        int rounds = 3;

        if (!decrypt) {
            for (int r = 0; r < rounds; r++) {
                x = feistelRound6(x, seed, r);
            }
            return x & TABLE_MASK;
        }

        for (int r = rounds - 1; r >= 0; r--) {
            x = feistelRound6Inv(x, seed, r);
        }
        return x & TABLE_MASK;
    }

    private int feistelRound6(int x, int seed, int round) {
        int l = (x >>> 3) & 0x7;
        int r = x & 0x7;
        int f = feistelF3(r, seed, round);
        int nl = r;
        int nr = l ^ f;
        return ((nl & 0x7) << 3) | (nr & 0x7);
    }

    private int feistelRound6Inv(int x, int seed, int round) {
        int nl = (x >>> 3) & 0x7;
        int nr = x & 0x7;
        int r = nl;
        int f = feistelF3(r, seed, round);
        int l = nr ^ f;
        return ((l & 0x7) << 3) | (r & 0x7);
    }

    private int feistelF3(int v, int seed, int round) {
        int k = seed ^ (round * 0x9E3779B9);
        int out = (v * 31) ^ (k >>> (round * 3));
        return out & 0x7;
    }

    private byte[] uuidToBytes(UUID uuid) {
        byte[] b = new byte[16];
        ByteBuffer.wrap(b, 0, 8).putLong(uuid.getMostSignificantBits());
        ByteBuffer.wrap(b, 8, 8).putLong(uuid.getLeastSignificantBits());
        return b;
    }

    private UUID bytesToUuid(byte[] b) {
        long msb = ByteBuffer.wrap(b, 0, 8).getLong();
        long lsb = ByteBuffer.wrap(b, 8, 8).getLong();
        return new UUID(msb, lsb);
    }

    public record Locate(String tableName, Integer shard) {
    }
}
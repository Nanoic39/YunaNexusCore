package cc.nanoic.yuna.file.service.impl;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.file.config.FileStorageProperties;
import cc.nanoic.yuna.file.entity.YunaFile;
import cc.nanoic.yuna.file.entity.YunaFileShare;
import cc.nanoic.yuna.file.mapper.UserFileShardMapper;
import cc.nanoic.yuna.file.mapper.YunaFileShareMapper;
import cc.nanoic.yuna.file.model.vo.FileMetaVO;
import cc.nanoic.yuna.file.model.vo.FileUploadVO;
import cc.nanoic.yuna.file.service.FileService;
import cc.nanoic.yuna.file.util.FileUuidCodec;
import cc.nanoic.yuna.file.util.UserFileTableUtil;
import cc.nanoic.yuna.common.security.utils.JwtUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final UserFileShardMapper shardMapper;
    private final YunaFileShareMapper shareMapper;
    private final FileUuidCodec uuidCodec;
    private final FileStorageProperties props;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadVO upload(Long userId, MultipartFile file, Integer folderId, Integer category) {
        if (userId == null) {
            throw new BusinessException("未登录或Token失效");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.FAILURE, "文件为空");
        }
        if (file.getSize() > props.getMaxSizeBytes()) {
            throw new BusinessException(ResultCode.FAILURE, "文件过大");
        }

        int shard = uuidCodec.shardOfUserId(userId);
        String table = UserFileTableUtil.userFileTable(shard);

        String uuid = uuidCodec.nextUserFileUuid(userId);
        String originName = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        originName = sanitizeName(originName);
        String fileName = originName;

        String mimeType = file.getContentType();
        String fileType = ext(originName);

        YunaFile e = new YunaFile();
        e.setUuid(uuid);
        e.setUserId(userId);
        e.setFolderId(folderId == null ? 0L : folderId.longValue());
        e.setOriginName(originName);
        e.setFileName(fileName);
        e.setFileSize(file.getSize());
        e.setFileType(fileType);
        e.setMimeType(mimeType);
        e.setCategory(category == null ? 0 : category);
        e.setIsFolder(0);
        e.setFileCount(0);
        e.setSubSize(0L);
        e.setCreateBy(userId);
        e.setStatus(0);

        String identifier;
        boolean canBase64 = isImage(mimeType) && file.getSize() <= props.getBase64MaxBytes();

        if (canBase64) {
            byte[] bytes = readAllBytes(file);
            identifier = sha256Hex(bytes);
            e.setIdentifier(identifier);
            e.setStorageType(1);
            e.setFileContent(Base64.getEncoder().encodeToString(bytes));
            e.setFilePath(null);
            shardMapper.insertOne(table, e);
        } else {
            e.setStorageType(0);
            String relPath = buildRelPath(uuid, originName);
            Path abs = resolveAbsPath(relPath);
            try {
                Files.createDirectories(abs.getParent());
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                try (InputStream in = file.getInputStream(); DigestInputStream din = new DigestInputStream(in, md)) {
                    Files.copy(din, abs);
                }
                identifier = HexFormat.of().formatHex(md.digest());
            } catch (Exception ex) {
                try {
                    Files.deleteIfExists(abs);
                } catch (Exception ignore) {
                }
                throw new BusinessException(ResultCode.FAILURE, "文件保存失败");
            }

            e.setIdentifier(identifier);
            e.setFilePath(relPath);
            e.setFileContent(null);
            shardMapper.insertOne(table, e);
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setUuid(e.getUuid());
        vo.setOriginName(e.getOriginName());
        vo.setFileName(e.getFileName());
        vo.setFileSize(e.getFileSize());
        vo.setMimeType(e.getMimeType());
        vo.setStorageType(e.getStorageType());
        vo.setCategory(e.getCategory());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadVO uploadLocal(Long userId, java.io.File file, String fileName, Integer folderId) {
        if (userId == null) {
            throw new BusinessException("未登录或Token失效");
        }
        if (file == null || !file.exists()) {
            throw new BusinessException(ResultCode.FAILURE, "文件不存在");
        }

        int shard = uuidCodec.shardOfUserId(userId);
        String table = UserFileTableUtil.userFileTable(shard);

        String uuid = uuidCodec.nextUserFileUuid(userId);
        fileName = sanitizeName(fileName);

        String mimeType = cn.hutool.core.io.FileUtil.getMimeType(fileName);
        String fileType = ext(fileName);

        YunaFile e = new YunaFile();
        e.setUuid(uuid);
        e.setUserId(userId);
        e.setFolderId(folderId == null ? 0L : folderId.longValue());
        e.setOriginName(fileName);
        e.setFileName(fileName);
        e.setFileSize(file.length());
        e.setFileType(fileType);
        e.setMimeType(mimeType);
        e.setCategory(0); // TODO: detect category
        e.setIsFolder(0);
        e.setFileCount(0);
        e.setSubSize(0L);
        e.setCreateBy(userId);
        e.setStatus(0);

        boolean canBase64 = isImage(mimeType) && file.length() <= props.getBase64MaxBytes();
        String identifier;

        if (canBase64) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                identifier = sha256Hex(bytes);

                checkDuplicate(table, identifier, fileName);

                e.setIdentifier(identifier);
                e.setStorageType(1);
                e.setFileContent(Base64.getEncoder().encodeToString(bytes));
                e.setFilePath(null);
            } catch (BusinessException be) {
                throw be;
            } catch (Exception ex) {
                throw new BusinessException(ResultCode.FAILURE, "处理图片失败: " + ex.getMessage());
            }
        } else {
            e.setStorageType(0);
            String relPath = buildRelPath(uuid, fileName);
            Path abs = resolveAbsPath(relPath);
            try {
                // Calculate hash first for deduplication check
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                try (InputStream in = Files.newInputStream(file.toPath())) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = in.read(buffer)) != -1) {
                        md.update(buffer, 0, len);
                    }
                }
                identifier = HexFormat.of().formatHex(md.digest());

                checkDuplicate(table, identifier, fileName);

                Files.createDirectories(abs.getParent());
                // 移动文件 (rename) 效率最高
                try {
                    Files.move(file.toPath(), abs, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception ex) {
                    Files.copy(file.toPath(), abs, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    Files.delete(file.toPath());
                }
            } catch (BusinessException be) {
                try {
                    Files.deleteIfExists(file.toPath());
                } catch (Exception ignore) {
                }
                throw be;
            } catch (Exception ex) {
                try {
                    Files.deleteIfExists(abs);
                } catch (Exception ignore) {
                }
                throw new BusinessException(ResultCode.FAILURE, "文件保存失败");
            }

            e.setIdentifier(identifier);
            e.setFilePath(relPath);
            e.setFileContent(null);
        }

        shardMapper.insertOne(table, e);

        FileUploadVO vo = new FileUploadVO();
        vo.setUuid(e.getUuid());
        vo.setOriginName(e.getOriginName());
        vo.setFileName(e.getFileName());
        vo.setFileSize(e.getFileSize());
        vo.setMimeType(e.getMimeType());
        vo.setStorageType(e.getStorageType());
        vo.setCategory(e.getCategory());
        return vo;
    }

    @Override
    public FileMetaVO meta(String fileUuid, Long userId) {
        YunaFile e = getAndCheckAccess(fileUuid, userId, true);
        return toMetaVO(e);
    }

    @Override
    public Resource loadResource(String fileUuid, Long userId) {
        YunaFile e = getAndCheckAccess(fileUuid, userId, true);
        if (e.getStorageType() != null && e.getStorageType() == 1) {
            // Base64 storage
            if (StrUtil.isBlank(e.getFileContent())) {
                throw new BusinessException(ResultCode.FAILURE, "文件内容为空");
            }
            try {
                // 使用 MimeDecoder 以兼容可能存在的换行符
                byte[] bytes = Base64.getMimeDecoder().decode(e.getFileContent());
                return new ByteArrayResource(Objects.requireNonNull(bytes));
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ResultCode.FAILURE, "文件内容损坏");
            }
        }
        Path abs = resolveAbsPath(e.getFilePath());
        if (!Files.exists(abs)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        try {
            return new UrlResource(Objects.requireNonNull(abs.toUri()));
        } catch (MalformedURLException ex) {
            throw new BusinessException(ResultCode.FAILURE, "文件路径异常");
        }
    }

    @Override
    public String originalName(String fileUuid, Long userId) {
        return getAndCheckAccess(fileUuid, userId, true).getOriginName();
    }

    @Override
    public String mimeType(String fileUuid, Long userId) {
        return getAndCheckAccess(fileUuid, userId, true).getMimeType();
    }

    @Override
    public void delete(String fileUuid, Long userId) {
        YunaFile e = getAndCheckAccess(fileUuid, userId, false);
        // The doc says "status 0" is normal. Set status = 1 (Recycle Bin).
        e.setStatus(1);
        String table = UserFileTableUtil.userFileTable(uuidCodec.shardOfUserId(userId));
        shardMapper.updateStatus(table, fileUuid, userId, 1, userId);
    }

    @Override
    public void rename(String fileUuid, String newName, Long userId) {
        if (StrUtil.isBlank(newName)) {
            throw new BusinessException(ResultCode.FAILURE, "文件名不能为空");
        }
        YunaFile e = getAndCheckAccess(fileUuid, userId, false);
        String table = UserFileTableUtil.userFileTable(uuidCodec.shardOfUserId(userId));

        // Update originName
        e.setOriginName(newName);
        // Also update fileName if needed, but originName is the display name
        shardMapper.updateName(table, fileUuid, newName);
    }

    @Override
    public void createFolder(String name, Long parentId, Long userId) {
        if (StrUtil.isBlank(name)) {
            throw new BusinessException(ResultCode.FAILURE, "文件夹名称不能为空");
        }
        YunaFile e = new YunaFile();
        e.setUuid(uuidCodec.nextUserFileUuid(userId));
        e.setUserId(userId);
        e.setFolderId(parentId == null ? 0L : parentId);
        e.setOriginName(name);
        e.setFileName(name);
        e.setIsFolder(1);
        e.setStatus(0);
        e.setCreateBy(userId);
        e.setCreateTime(LocalDateTime.now());
        e.setUpdateTime(LocalDateTime.now());
        e.setFileSize(0L);
        e.setStorageType(0);

        String table = UserFileTableUtil.userFileTable(uuidCodec.shardOfUserId(userId));
        shardMapper.insertOne(table, e);
    }

    @Override
    public void move(String uuid, Long targetFolderId, Long userId) {
        getAndCheckAccess(uuid, userId, false);
        String table = UserFileTableUtil.userFileTable(uuidCodec.shardOfUserId(userId));
        shardMapper.updateFolderId(table, uuid, userId, targetFolderId == null ? 0L : targetFolderId);
    }

    @Override
    public List<FileMetaVO> list(Long folderId, Long userId) {
        String table = UserFileTableUtil.userFileTable(uuidCodec.shardOfUserId(userId));
        List<YunaFile> list = shardMapper.selectByFolder(table, userId, folderId);
        List<FileMetaVO> voList = list.stream().map(this::toMetaVO).toList();

        if (!voList.isEmpty()) {
            List<String> fileUuids = voList.stream()
                    .filter(v -> v.getIsFolder() == 0)
                    .map(FileMetaVO::getUuid)
                    .toList();
            if (!fileUuids.isEmpty()) {
                List<String> sharedUuids = shareMapper.selectObjs(new LambdaQueryWrapper<YunaFileShare>()
                        .select(YunaFileShare::getFileUuid)
                        .eq(YunaFileShare::getUserId, userId)
                        .eq(YunaFileShare::getStatus, 0)
                        .in(YunaFileShare::getFileUuid, fileUuids))
                        .stream().map(Object::toString).toList();

                for (FileMetaVO vo : voList) {
                    vo.setIsShared(sharedUuids.contains(vo.getUuid()));
                }
            }
        }
        return voList;
    }

    @Override
    public String generateDownloadToken(String uuid, Long userId) {
        getAndCheckAccess(uuid, userId, false);
        Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", userId);
        claims.put("fileUuid", uuid);
        claims.put("type", "download");
        // 5分钟有效期 (300秒)
        return jwtUtil.generateToken(claims, 300);
    }

    @Override
    public Long validateDownloadToken(String uuid, String token) {
        if (StrUtil.isBlank(token))
            return null;
        try {
            // 校验是否有效且类型为 download
            if (!jwtUtil.validateToken(token, null, "download")) {
                return null;
            }
            // 校验 uuid 是否匹配
            String tokenUuid = (String) jwtUtil.getClaimFromToken(token, "fileUuid");
            if (tokenUuid == null || !tokenUuid.equals(uuid)) {
                return null;
            }
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<FileMetaVO> my(Long userId, int limit) {
        if (userId == null) {
            throw new BusinessException("未登录或Token失效");
        }
        int safe = Math.max(1, Math.min(limit, 200));
        int shard = uuidCodec.shardOfUserId(userId);
        String table = UserFileTableUtil.userFileTable(shard);
        return shardMapper.listByUser(table, userId, safe).stream().map(this::toMetaVO).toList();
    }

    @Override
    public List<FileMetaVO> recycleBin(Long userId, int limit) {
        if (userId == null) {
            throw new BusinessException("未登录或Token失效");
        }
        int safe = Math.max(1, Math.min(limit, 200));
        int shard = uuidCodec.shardOfUserId(userId);
        String table = UserFileTableUtil.userFileTable(shard);
        return shardMapper.selectRecycleBin(table, userId, safe).stream().map(this::toMetaVO).toList();
    }

    @Override
    public void recover(String uuid, Long userId) {
        YunaFile e = getInRecycleBin(uuid, userId);
        String table = UserFileTableUtil.userFileTable(uuidCodec.shardOfUserId(userId));
        shardMapper.updateStatus(table, uuid, userId, 0, userId);
    }

    @Override
    public void clean(String uuid, Long userId) {
        YunaFile e = getInRecycleBin(uuid, userId);
        String table = UserFileTableUtil.userFileTable(uuidCodec.shardOfUserId(userId));
        // User manual delete -> Status 2 (Hidden/Admin only) per policy requirement to
        // keep for 180 days
        shardMapper.updateStatus(table, uuid, userId, 2, userId);
    }

    private YunaFile getInRecycleBin(String fileUuid, Long userId) {
        if (StrUtil.isBlank(fileUuid)) {
            throw new BusinessException(ResultCode.FAILURE, "缺少文件UUID");
        }
        FileUuidCodec.Locate loc = uuidCodec.decode(fileUuid);
        if (loc.shard() == null) {
            throw new BusinessException(ResultCode.FAILURE, "不支持该类型资源");
        }
        String table = UserFileTableUtil.userFileTable(loc.shard());
        YunaFile e = shardMapper.selectByUuid(table, fileUuid);
        if (e == null || e.getStatus() != 1) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在或不在回收站");
        }
        if (userId == null) {
            throw new BusinessException("未登录或Token失效");
        }
        if (!Objects.equals(e.getUserId(), userId)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权访问该文件");
        }
        return e;
    }

    private YunaFile getAndCheckAccess(String fileUuid, Long userId, boolean allowPublicCategory) {
        if (StrUtil.isBlank(fileUuid)) {
            throw new BusinessException(ResultCode.FAILURE, "缺少文件UUID");
        }
        FileUuidCodec.Locate loc = uuidCodec.decode(fileUuid);
        if (loc.shard() == null) {
            throw new BusinessException(ResultCode.FAILURE, "不支持该类型资源");
        }

        String table = UserFileTableUtil.userFileTable(loc.shard());
        YunaFile e = shardMapper.selectByUuid(table, fileUuid);
        if (e == null || (e.getStatus() != null && e.getStatus() != 0)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        boolean isPublic = allowPublicCategory && e.getCategory() != null && e.getCategory() == 1;
        if (isPublic) {
            return e;
        }

        if (userId == null) {
            throw new BusinessException("未登录或Token失效");
        }
        if (!Objects.equals(e.getUserId(), userId)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权访问该文件");
        }
        return e;
    }

    private Path resolveAbsPath(String relPath) {
        if (StrUtil.isBlank(relPath)) {
            throw new BusinessException(ResultCode.FAILURE, "文件路径为空");
        }
        Path root = Paths.get(props.getRootPath()).normalize();
        Path abs = root.resolve(relPath).normalize();
        if (!abs.startsWith(root)) {
            throw new BusinessException(ResultCode.FAILURE, "非法文件路径");
        }
        return abs;
    }

    private void checkDuplicate(String table, String identifier, String fileName) {
        // Check if file with same identifier exists in this shard
        YunaFile exists = shardMapper.selectByIdentifier(table, identifier);

        if (exists != null) {
            throw new BusinessException(ResultCode.FAILURE, "文件已存在: " + exists.getOriginName());
        }
    }

    private String buildRelPath(String uuid, String originName) {
        LocalDate d = LocalDate.now();
        String dateDir = "%04d/%02d/%02d".formatted(d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        String safe = sanitizeName(originName);
        return dateDir + "/" + uuid + "_" + safe;
    }

    private String sanitizeName(String name) {
        String s = name.replace('\\', '_').replace('/', '_').replace('\0', '_');
        s = s.replaceAll("[\\s]+", " ").trim();
        if (s.length() > 255) {
            s = s.substring(s.length() - 255);
        }
        if (s.isEmpty()) {
            return "file";
        }
        return s;
    }

    private boolean isImage(String mime) {
        return mime != null && mime.toLowerCase().startsWith("image/");
    }

    private String ext(String name) {
        if (name == null)
            return null;
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1)
            return null;
        String e = name.substring(i + 1);
        if (e.length() > 50) {
            e = e.substring(0, 50);
        }
        return e;
    }

    private byte[] readAllBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAILURE, "读取文件失败");
        }
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes);
            return HexFormat.of().formatHex(md.digest());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.FAILURE, "计算指纹失败");
        }
    }

    private FileMetaVO toMetaVO(YunaFile e) {
        FileMetaVO vo = new FileMetaVO();
        vo.setId(e.getId());
        vo.setUuid(e.getUuid());
        vo.setUserId(e.getUserId());
        vo.setFolderId(e.getFolderId());
        vo.setOriginName(e.getOriginName());
        vo.setFileName(e.getFileName());
        vo.setFilePath(e.getFilePath());
        vo.setStorageType(e.getStorageType());
        vo.setFileSize(e.getFileSize());
        vo.setFileType(e.getFileType());
        vo.setMimeType(e.getMimeType());
        vo.setIdentifier(e.getIdentifier());
        vo.setCategory(e.getCategory());
        vo.setIsFolder(e.getIsFolder());
        vo.setFileCount(e.getFileCount());
        vo.setSubSize(e.getSubSize());
        vo.setCreateTime(e.getCreateTime());
        vo.setUpdateTime(e.getUpdateTime());
        vo.setStatus(e.getStatus());
        return vo;
    }
}
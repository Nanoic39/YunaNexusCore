package cc.nanoic.yuna.file.service.impl;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.file.entity.YunaFile;
import cc.nanoic.yuna.file.entity.YunaFileShare;
import cc.nanoic.yuna.file.mapper.UserFileShardMapper;
import cc.nanoic.yuna.file.mapper.YunaFileShareMapper;
import cc.nanoic.yuna.file.model.dto.ShareCreateDTO;
import cc.nanoic.yuna.file.model.vo.FileMetaVO;
import cc.nanoic.yuna.file.model.vo.ShareVO;
import cc.nanoic.yuna.file.service.FileService;
import cc.nanoic.yuna.file.service.ShareService;
import cc.nanoic.yuna.file.util.FileUuidCodec;
import cc.nanoic.yuna.file.util.UserFileTableUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final YunaFileShareMapper shareMapper;
    private final UserFileShardMapper shardMapper;
    private final FileUuidCodec uuidCodec;
    private final FileService fileService;

    private final SecureRandom random = new SecureRandom();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareVO create(Long userId, ShareCreateDTO dto) {
        if (userId == null) {
            throw new BusinessException("未登录或Token失效");
        }
        if (dto == null || StrUtil.isBlank(dto.getFileUuid())) {
            throw new BusinessException(ResultCode.FAILURE, "缺少文件UUID");
        }

        FileUuidCodec.Locate loc = uuidCodec.decode(dto.getFileUuid());
        if (loc.shard() == null) {
            throw new BusinessException(ResultCode.FAILURE, "不支持分享该类型资源");
        }
        String table = UserFileTableUtil.userFileTable(loc.shard());
        YunaFile f = shardMapper.selectByUuid(table, dto.getFileUuid());
        if (f == null || (f.getStatus() != null && f.getStatus() != 0)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        if (f.getIsFolder() != null && f.getIsFolder() == 1) {
            throw new BusinessException(ResultCode.FAILURE, "不支持分享文件夹");
        }
        if (!Objects.equals(f.getUserId(), userId)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权分享该文件");
        }

        String token = randomToken64();

        YunaFileShare s = new YunaFileShare();
        s.setFileUuid(dto.getFileUuid());
        s.setUserId(userId);
        s.setShareToken(token);
        String pwd = dto.getSharePwd();
        s.setSharePwd(StrUtil.isBlank(pwd) ? null : pwd);
        s.setPermissionType(dto.getPermissionType() == null ? 1 : dto.getPermissionType());
        s.setDownloadLimit(dto.getDownloadLimit() == null ? 0 : dto.getDownloadLimit());
        s.setDownloadCount(0);
        
        if (dto.getExpireSeconds() != null && dto.getExpireSeconds() > 0) {
            s.setExpireTime(LocalDateTime.now().plusSeconds(dto.getExpireSeconds()));
        } else {
            s.setExpireTime(null);
        }

        s.setStatus(0); // 0: Active
        s.setCreateBy(userId);

        shareMapper.insert(s);

        ShareVO vo = new ShareVO();
        vo.setShareToken(s.getShareToken());
        vo.setFileUuid(s.getFileUuid());
        vo.setPermissionType(s.getPermissionType());
        vo.setDownloadLimit(s.getDownloadLimit());
        vo.setDownloadCount(s.getDownloadCount());
        vo.setExpireTime(s.getExpireTime());
        vo.setNeedPwd(StrUtil.isNotBlank(s.getSharePwd()));
        return vo;
    }

    @Override
    public ShareVO get(String token) {
        YunaFileShare s = getValidShare(token);
        ShareVO vo = new ShareVO();
        vo.setShareToken(s.getShareToken());
        vo.setFileUuid(s.getFileUuid());
        vo.setPermissionType(s.getPermissionType());
        vo.setDownloadLimit(s.getDownloadLimit());
        vo.setDownloadCount(s.getDownloadCount());
        vo.setExpireTime(s.getExpireTime());
        vo.setNeedPwd(StrUtil.isNotBlank(s.getSharePwd()));
        return vo;
    }

    @Override
    public FileMetaVO fileMeta(String token, String pwd) {
        YunaFileShare s = getValidShare(token);
        checkPwd(s, pwd);

        FileUuidCodec.Locate loc = uuidCodec.decode(s.getFileUuid());
        if (loc.shard() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        String table = UserFileTableUtil.userFileTable(loc.shard());
        YunaFile f = shardMapper.selectByUuid(table, s.getFileUuid());
        if (f == null || (f.getStatus() != null && f.getStatus() != 0)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }

        FileMetaVO vo = new FileMetaVO();
        vo.setUuid(f.getUuid());
        vo.setUserId(f.getUserId());
        vo.setFolderId(f.getFolderId());
        vo.setOriginName(f.getOriginName());
        vo.setFileName(f.getFileName());
        vo.setFilePath(f.getFilePath());
        vo.setStorageType(f.getStorageType());
        vo.setFileSize(f.getFileSize());
        vo.setFileType(f.getFileType());
        vo.setMimeType(f.getMimeType());
        vo.setIdentifier(f.getIdentifier());
        vo.setCategory(f.getCategory());
        vo.setIsFolder(f.getIsFolder());
        vo.setFileCount(f.getFileCount());
        vo.setSubSize(f.getSubSize());
        vo.setCreateTime(f.getCreateTime());
        vo.setUpdateTime(f.getUpdateTime());
        vo.setStatus(f.getStatus());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Resource download(String token, String pwd) {
        YunaFileShare s = getValidShare(token);
        checkPwd(s, pwd);

        int perm = s.getPermissionType() == null ? 1 : s.getPermissionType();
        // Permission check: bit 0 (value 1) = download allowed
        boolean canDownload = (perm & 1) == 1;
        if (!canDownload) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无下载权限");
        }

        if (s.getDownloadLimit() != null && s.getDownloadLimit() > 0) {
            int cnt = s.getDownloadCount() == null ? 0 : s.getDownloadCount();
            if (cnt >= s.getDownloadLimit()) {
                throw new BusinessException(ResultCode.FAILURE, "已达到下载次数限制");
            }
        }

        s.setDownloadCount((s.getDownloadCount() == null ? 0 : s.getDownloadCount()) + 1);
        shareMapper.updateById(s);

        YunaFile f = getFileByUuid(s.getFileUuid());
        if (f == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在或已被删除");
        }

        return fileService.loadResource(s.getFileUuid(), s.getUserId());
    }

    @Override
    public String downloadOriginalName(String token) {
        YunaFileShare s = getValidShare(token);
        YunaFile f = getFileByUuid(s.getFileUuid());
        return f != null ? f.getOriginName() : "unknown";
    }

    @Override
    public String downloadMimeType(String token) {
        YunaFileShare s = getValidShare(token);
        YunaFile f = getFileByUuid(s.getFileUuid());
        return f != null ? f.getMimeType() : "application/octet-stream";
    }

    private YunaFile getFileByUuid(String uuid) {
        try {
            FileUuidCodec.Locate loc = uuidCodec.decode(uuid);
            if (loc.shard() == null)
                return null;
            String table = UserFileTableUtil.userFileTable(loc.shard());
            return shardMapper.selectByUuid(table, uuid);
        } catch (Exception e) {
            return null;
        }
    }

    private YunaFileShare getValidShare(String token) {
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ResultCode.FAILURE, "缺少分享Token");
        }
        YunaFileShare s = shareMapper.selectOne(new LambdaQueryWrapper<YunaFileShare>()
                .eq(YunaFileShare::getShareToken, token)
                .last("limit 1"));
        if (s == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分享不存在");
        }
        if (s.getStatus() != null && s.getStatus() != 0) {
            throw new BusinessException(ResultCode.FAILURE, "分享已失效");
        }
        if (s.getExpireTime() != null && s.getExpireTime().isBefore(LocalDateTime.now())) {
            s.setStatus(1);
            shareMapper.updateById(s);
            throw new BusinessException(ResultCode.FAILURE, "分享已过期");
        }
        return s;
    }

    private void checkPwd(YunaFileShare s, String pwd) {
        if (StrUtil.isBlank(s.getSharePwd())) {
            return;
        }
        if (!Objects.equals(s.getSharePwd(), pwd)) {
            throw new BusinessException(ResultCode.FAILURE, "提取码错误");
        }
    }

    private String randomToken64() {
        byte[] b = new byte[48];
        random.nextBytes(b);
        String t = Base64.getUrlEncoder().withoutPadding().encodeToString(b);
        if (t.length() > 64) {
            return t.substring(0, 64);
        }
        return t;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, String fileUuid) {
        YunaFileShare s = shareMapper.selectOne(new LambdaQueryWrapper<YunaFileShare>()
                .eq(YunaFileShare::getUserId, userId)
                .eq(YunaFileShare::getFileUuid, fileUuid)
                .eq(YunaFileShare::getStatus, 0) // Only cancel active shares
                .last("limit 1"));

        if (s == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "未找到有效分享");
        }

        s.setStatus(2); // 2: Invalid/Cancelled
        shareMapper.updateById(s);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, String token) {
        YunaFileShare s = shareMapper.selectOne(new LambdaQueryWrapper<YunaFileShare>()
                .eq(YunaFileShare::getShareToken, token));

        if (s == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分享不存在");
        }

        if (!Objects.equals(s.getUserId(), userId)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权操作");
        }

        shareMapper.deleteById(s.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long userId, String token, Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new BusinessException(ResultCode.FAILURE, "无效的状态");
        }
        YunaFileShare s = shareMapper.selectOne(new LambdaQueryWrapper<YunaFileShare>()
                .eq(YunaFileShare::getShareToken, token));

        if (s == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分享不存在");
        }

        if (!Objects.equals(s.getUserId(), userId)) {
            throw new BusinessException(ResultCode.UN_AUTHORIZED, "无权操作");
        }

        s.setStatus(status);
        shareMapper.updateById(s);
    }

    @Override
    public java.util.List<ShareVO> listMyShares(Long userId) {
        java.util.List<YunaFileShare> list = shareMapper.selectList(new LambdaQueryWrapper<YunaFileShare>()
                .eq(YunaFileShare::getUserId, userId)
                .orderByDesc(YunaFileShare::getCreateTime));

        return list.stream().map(s -> {
            ShareVO vo = new ShareVO();
            vo.setShareToken(s.getShareToken());
            vo.setFileUuid(s.getFileUuid());
            vo.setPermissionType(s.getPermissionType());
            vo.setDownloadLimit(s.getDownloadLimit());
            vo.setDownloadCount(s.getDownloadCount());
            vo.setExpireTime(s.getExpireTime());
            vo.setNeedPwd(StrUtil.isNotBlank(s.getSharePwd()));
            vo.setStatus(s.getStatus());

            YunaFile f = getFileByUuid(s.getFileUuid());
            if (f != null) {
                vo.setFileName(f.getOriginName());
            } else {
                vo.setFileName("未知文件");
            }
            return vo;
        }).collect(java.util.stream.Collectors.toList());
    }
}
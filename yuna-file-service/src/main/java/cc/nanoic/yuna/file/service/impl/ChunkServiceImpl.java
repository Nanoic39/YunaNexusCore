package cc.nanoic.yuna.file.service.impl;

import cc.nanoic.yuna.common.core.exception.BusinessException;
import cc.nanoic.yuna.common.core.result.ResultCode;
import cc.nanoic.yuna.file.config.FileStorageProperties;
import cc.nanoic.yuna.file.model.vo.FileUploadVO;
import cc.nanoic.yuna.file.service.ChunkService;
import cc.nanoic.yuna.file.service.FileService;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChunkServiceImpl implements ChunkService {

    private final FileStorageProperties props;
    private final FileService fileService;

    @Override
    public String initChunkUpload(Long userId, String identifier, String filename, Integer totalChunks, Long totalSize) {
        // 生成上传ID
        String uploadId = IdUtil.simpleUUID();
        // 创建临时目录
        String tempPath = getTempPath(uploadId);
        File tempDir = new File(tempPath);
        if (!tempDir.exists() && !tempDir.mkdirs()) {
             log.error("Failed to create temp directory: {}", tempPath);
             throw new BusinessException(ResultCode.FAILURE, "初始化上传失败: 无法创建临时目录");
        }
        log.info("Initialized chunk upload: uploadId={}, path={}", uploadId, tempDir.getAbsolutePath());
        return uploadId;
    }

    @Override
    public void uploadChunk(Long userId, String uploadId, Integer chunkNumber, MultipartFile file) {
        String tempPath = getTempPath(uploadId);
        File tempDir = new File(tempPath);
        if (!tempDir.exists()) {
            log.error("Upload task not found: uploadId={}, path={}", uploadId, tempPath);
            throw new BusinessException(ResultCode.FAILURE, "上传任务不存在或已过期 (ID: " + uploadId + ", Path: " + tempDir.getAbsolutePath() + ")");
        }
        
        // 保存分片
        File chunkFile = new File(tempDir, String.valueOf(chunkNumber));
        try (InputStream in = file.getInputStream();
             FileOutputStream out = new FileOutputStream(chunkFile)) {
            IoUtil.copy(in, out);
        } catch (IOException e) {
            log.error("Failed to upload chunk: {} -> {}", file.getOriginalFilename(), chunkFile.getAbsolutePath(), e);
            throw new BusinessException(ResultCode.FAILURE, "分片上传失败: " + e.getMessage());
        }
    }

    @Override
    public FileUploadVO mergeChunks(Long userId, String uploadId, String filename, Long folderId) {
        String tempPath = getTempPath(uploadId);
        File tempDir = new File(tempPath);
        if (!tempDir.exists()) {
            throw new BusinessException(ResultCode.FAILURE, "上传任务不存在");
        }

        File[] chunks = tempDir.listFiles();
        if (chunks == null || chunks.length == 0) {
            log.error("No chunks found in: {}", tempDir.getAbsolutePath());
            throw new BusinessException(ResultCode.FAILURE, "分片数据丢失: " + tempDir.getAbsolutePath());
        }

        // 排序分片
        Arrays.sort(chunks, Comparator.comparingInt(o -> Integer.parseInt(o.getName())));

        // 合并文件
        File mergedFile = new File(tempDir, "merged_" + filename);
        try (FileOutputStream out = new FileOutputStream(mergedFile, true)) {
            for (File chunk : chunks) {
                if (chunk.getName().startsWith("merged_")) continue;
                Files.copy(chunk.toPath(), out);
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.FAILURE, "合并文件失败");
        }

        // 调用 FileService 保存
        try {
            FileUploadVO vo = fileService.uploadLocal(userId, mergedFile, filename, folderId == null ? 0 : folderId.intValue());
            
            // 清理临时文件
            FileUtil.del(tempDir);
            
            return vo;
        } catch (Exception e) {
            // 合并后的文件如果保存失败，保留以便排查？或者也删除
            // 这里选择不删除临时目录，以便重试
            throw new BusinessException(ResultCode.FAILURE, "保存文件失败: " + e.getMessage());
        }
    }

    private String getTempPath(String uploadId) {
        return props.getRootPath() + File.separator + "temp" + File.separator + uploadId;
    }
}

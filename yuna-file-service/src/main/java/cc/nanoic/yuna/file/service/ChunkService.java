package cc.nanoic.yuna.file.service;

import cc.nanoic.yuna.file.model.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface ChunkService {
    /**
     * 初始化分片上传
     * @param userId 用户ID
     * @param identifier 文件MD5/Identifier
     * @param filename 文件名
     * @param totalChunks 总分片数
     * @param totalSize 总大小
     * @return uploadId
     */
    String initChunkUpload(Long userId, String identifier, String filename, Integer totalChunks, Long totalSize);

    /**
     * 上传分片
     * @param userId 用户ID
     * @param uploadId 上传ID
     * @param chunkNumber 分片号
     * @param file 分片文件
     */
    void uploadChunk(Long userId, String uploadId, Integer chunkNumber, MultipartFile file);

    /**
     * 合并分片
     * @param userId 用户ID
     * @param uploadId 上传ID
     * @param filename 文件名
     * @param folderId 文件夹ID
     * @return 文件信息
     */
    FileUploadVO mergeChunks(Long userId, String uploadId, String filename, Long folderId);
}

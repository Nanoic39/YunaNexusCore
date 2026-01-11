package cc.nanoic.yuna.file.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.file.model.vo.FileUploadVO;
import cc.nanoic.yuna.file.service.ChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file/chunk")
@RequiredArgsConstructor
public class ChunkController {

    private final ChunkService chunkService;

    /**
     * 初始化分块上传
     * 
     * @param userId      用户ID
     * @param identifier  标识符（可选）
     * @param filename    文件名
     * @param totalChunks 总分块数
     * @param totalSize   总大小（字节）
     * @return 上传ID
     */
    @PostMapping("/init")
    public R<String> init(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestParam(value = "identifier", required = false) String identifier,
            @RequestParam("filename") String filename,
            @RequestParam("totalChunks") Integer totalChunks,
            @RequestParam("totalSize") Long totalSize) {
        return R.success(chunkService.initChunkUpload(userId, identifier, filename, totalChunks, totalSize));
    }

    /**
     * 上传分块
     * 
     * @param userId      用户ID
     * @param uploadId    上传ID
     * @param chunkNumber 分块编号
     * @param file        文件分块
     * @return 无
     */
    @PostMapping("/upload")
    public R<Void> upload(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkNumber") Integer chunkNumber,
            @RequestParam("file") MultipartFile file) {
        chunkService.uploadChunk(userId, uploadId, chunkNumber, file);
        return R.success(null);
    }

    /**
     * 合并分块上传
     * 
     * @param userId   用户ID
     * @param uploadId 上传ID
     * @param filename 文件名
     * @param folderId 文件夹ID（可选）
     * @return 文件上传VO
     */
    @PostMapping("/merge")
    public R<FileUploadVO> merge(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("filename") String filename,
            @RequestParam(value = "folderId", required = false) Long folderId) {
        return R.success(chunkService.mergeChunks(userId, uploadId, filename, folderId));
    }

    /**
     * 获取分块上传配置
     * 
     * @param userId 用户ID
     * @return 分块上传配置（并发数、分块大小）
     */
    @GetMapping("/config")
    public R<Map<String, Object>> config(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        Map<String, Object> config = new HashMap<>();
        // TODO: 真正的权限判断。目前权限系统未完善，假设 ID 为 1 的用户是超级管理员
        boolean isAdmin = userId != null && userId == 1L;
        config.put("concurrency", isAdmin ? 200 : 50);
        config.put("chunkSize", 25 * 1024 * 1024); // 25MB
        return R.success(config);
    }
}

package cc.nanoic.yuna.file.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.file.model.vo.FileMetaVO;
import cc.nanoic.yuna.file.model.vo.FileUploadVO;
import cc.nanoic.yuna.file.service.FileService;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 文件上传
     * 
     * @param userId   用户ID
     * @param file     文件
     * @param folderId 文件夹ID
     * @param category 文件分类
     * @return 文件上传VO
     */
    @PostMapping("/upload")
    public R<FileUploadVO> upload(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folderId", required = false) Integer folderId,
            @RequestParam(value = "category", required = false, defaultValue = "0") Integer category) {
        return R.success(fileService.upload(userId, file, folderId, category));
    }

    /**
     * 文件元数据查询
     * 
     * @param uuid   文件UUID
     * @param userId 用户ID
     * @return 文件元数据VO
     */
    @GetMapping("/meta/{uuid}")
    public R<FileMetaVO> meta(@PathVariable("uuid") String uuid,
            @RequestHeader(value = SecurityConstants.DETAILS_USER_ID, required = false) Long userId) {
        return R.success(fileService.meta(uuid, userId));
    }

    /**
     * 文件下载
     * 
     * @param uuid   文件UUID
     * @param userId 用户ID
     * @param token  下载令牌
     * @param inline 是否内联
     * @return 文件资源
     */
    @GetMapping("/download/{uuid}")
    public ResponseEntity<Resource> download(@PathVariable("uuid") String uuid,
            @RequestHeader(value = SecurityConstants.DETAILS_USER_ID, required = false) Long userId,
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "inline", required = false, defaultValue = "false") boolean inline) {

        if (userId == null && StrUtil.isNotBlank(token)) {
            userId = fileService.validateDownloadToken(uuid, token);
        }

        Resource resource = fileService.loadResource(uuid, userId);

        String originalName = fileService.originalName(uuid, userId);
        String encoded = URLEncoder.encode(originalName, StandardCharsets.UTF_8);
        String disposition = (inline ? "inline" : "attachment") + "; filename*=UTF-8''" + encoded;

        String mt = fileService.mimeType(uuid, userId);
        MediaType mediaType;
        try {
            if (mt != null) {
                mediaType = MediaType.parseMediaType(mt);
            } else {
                // 尝试从文件名推断
                String suffix = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
                switch (suffix) {
                    case "png":
                        mediaType = MediaType.IMAGE_PNG;
                        break;
                    case "jpg":
                    case "jpeg":
                        mediaType = MediaType.IMAGE_JPEG;
                        break;
                    case "gif":
                        mediaType = MediaType.IMAGE_GIF;
                        break;
                    case "webp":
                        mediaType = MediaType.parseMediaType("image/webp");
                        break;
                    case "bmp":
                        mediaType = MediaType.parseMediaType("image/bmp");
                        break;
                    case "mp4":
                        mediaType = MediaType.parseMediaType("video/mp4");
                        break;
                    case "mp3":
                        mediaType = MediaType.parseMediaType("audio/mpeg");
                        break;
                    default:
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                }
            }
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }

    /**
     * 文件夹创建
     * 
     * @param name     文件夹名称
     * @param parentId 父文件夹ID
     * @param userId   用户ID
     * @return 无
     */
    @PostMapping("/folder")
    public R<Void> createFolder(@RequestParam("name") String name,
            @RequestParam(value = "parentId", required = false) Long parentId,
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        fileService.createFolder(name, parentId, userId);
        return R.success(null, "创建成功");
    }

    /**
     * 文件移动
     * 
     * @param uuid           文件UUID
     * @param targetFolderId 目标文件夹ID
     * @param userId         用户ID
     * @return 无
     */
    @PostMapping("/move/{uuid}")
    public R<Void> move(@PathVariable("uuid") String uuid,
            @RequestParam(value = "targetFolderId", required = false) Long targetFolderId,
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        fileService.move(uuid, targetFolderId, userId);
        return R.success(null, "移动成功");
    }

    /**
     * 回收站列表
     */
    @GetMapping("/recycle/list")
    public R<List<FileMetaVO>> recycleList(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        return R.success(fileService.recycleBin(userId, 100));
    }

    /**
     * 恢复文件
     */
    @PostMapping("/recycle/recover/{uuid}")
    public R<Void> recover(@PathVariable("uuid") String uuid,
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        fileService.recover(uuid, userId);
        return R.success(null, "恢复成功");
    }

    /**
     * 彻底删除文件
     */
    @DeleteMapping("/recycle/clean/{uuid}")
    public R<Void> clean(@PathVariable("uuid") String uuid,
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        fileService.clean(uuid, userId);
        return R.success(null, "删除成功");
    }

    /**
     * 文件列表查询
     * 
     * @param userId   用户ID
     * @param folderId 文件夹ID
     * @return 文件元数据VO列表
     */
    @GetMapping("/list")
    public R<List<FileMetaVO>> list(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestParam(value = "folderId", required = false) Long folderId) {
        return R.success(fileService.list(folderId, userId));
    }

    /**
     * 文件下载令牌生成
     * 
     * @param uuid   文件UUID
     * @param userId 用户ID
     * @return 下载令牌
     */
    @GetMapping("/download-token/{uuid}")
    public R<String> getDownloadToken(@PathVariable("uuid") String uuid,
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        return R.success(fileService.generateDownloadToken(uuid, userId));
    }

    /**
     * 文件删除
     * 
     * @param uuid   文件UUID
     * @param userId 用户ID
     * @return 无
     */
    @DeleteMapping("/{uuid}")
    public R<Void> delete(@PathVariable("uuid") String uuid,
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        fileService.delete(uuid, userId);
        return R.success(null, "删除成功");
    }

    /**
     * 文件重命名
     * 
     * @param uuid   文件UUID
     * @param name   新文件名
     * @param userId 用户ID
     * @return 无
     */
    @PostMapping("/rename/{uuid}")
    public R<Void> rename(@PathVariable("uuid") String uuid,
            @RequestParam("name") String name,
            @RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        fileService.rename(uuid, name, userId);
        return R.success(null, "重命名成功");
    }

    /**
     * 用户文件列表查询
     * 
     * @param userId 用户ID
     * @param limit  查询数量限制
     * @return 文件元数据VO列表
     */
    @GetMapping("/my")
    public R<List<FileMetaVO>> my(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
            @RequestParam(value = "limit", required = false, defaultValue = "50") int limit) {
        return R.success(fileService.my(userId, limit));
    }
}
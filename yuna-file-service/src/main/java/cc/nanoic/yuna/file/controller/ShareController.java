package cc.nanoic.yuna.file.controller;

import cc.nanoic.yuna.common.core.constant.SecurityConstants;
import cc.nanoic.yuna.common.core.result.R;
import cc.nanoic.yuna.file.model.dto.ShareCreateDTO;
import cc.nanoic.yuna.file.model.vo.FileMetaVO;
import cc.nanoic.yuna.file.model.vo.ShareVO;
import cc.nanoic.yuna.file.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/share")
@RequiredArgsConstructor
public class ShareController {

    private final ShareService shareService;

    @PostMapping("/create")
    public R<ShareVO> create(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
                             @RequestBody ShareCreateDTO dto) {
        return R.success(shareService.create(userId, dto));
    }

    @GetMapping("/my")
    public R<java.util.List<ShareVO>> listMy(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId) {
        return R.success(shareService.listMyShares(userId));
    }

    @GetMapping("/{token:.+}")
    public R<ShareVO> get(@PathVariable("token") String token) {
        return R.success(shareService.get(token));
    }

    @GetMapping("/{token}/meta")
    public R<FileMetaVO> meta(@PathVariable("token") String token,
                              @RequestParam(value = "pwd", required = false) String pwd) {
        return R.success(shareService.fileMeta(token, pwd));
    }

    @GetMapping("/{token}/download")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable("token") String token,
                                                                                                  @RequestParam(value = "pwd", required = false) String pwd,
                                                                                                  @RequestParam(value = "inline", required = false, defaultValue = "false") boolean inline) {
        org.springframework.core.io.Resource resource = shareService.download(token, pwd);
        String filename = shareService.downloadOriginalName(token);
        String mimeType = "application/octet-stream";
        try {
             mimeType = shareService.downloadMimeType(token);
        } catch (Exception e) {
             // ignore
        }

        try {
            filename = java.net.URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        } catch (java.io.UnsupportedEncodingException e) {
            // ignore
        }

        String dispositionType = inline ? "inline" : "attachment";

        return org.springframework.http.ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(mimeType))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, dispositionType + "; filename*=UTF-8''" + filename)
                .body(resource);
    }

    @PostMapping("/cancel/{fileUuid}")
    public R<Void> cancel(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
                          @PathVariable("fileUuid") String fileUuid) {
        shareService.cancel(userId, fileUuid);
        return R.success(null, "取消分享成功");
    }

    @PostMapping("/delete/{token}")
    public R<Void> delete(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
                          @PathVariable("token") String token) {
        shareService.delete(userId, token);
        return R.success(null, "删除分享成功");
    }

    @PostMapping("/update-status")
    public R<Void> updateStatus(@RequestHeader(SecurityConstants.DETAILS_USER_ID) Long userId,
                                @RequestParam("token") String token,
                                @RequestParam("status") Integer status) {
        shareService.updateStatus(userId, token, status);
        return R.success(null, "状态更新成功");
    }
}
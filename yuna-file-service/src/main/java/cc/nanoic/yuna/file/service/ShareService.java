package cc.nanoic.yuna.file.service;

import cc.nanoic.yuna.file.model.dto.ShareCreateDTO;
import cc.nanoic.yuna.file.model.vo.FileMetaVO;
import cc.nanoic.yuna.file.model.vo.ShareVO;
import org.springframework.core.io.Resource;

public interface ShareService {
    ShareVO create(Long userId, ShareCreateDTO dto);

    ShareVO get(String token);

    FileMetaVO fileMeta(String token, String pwd);

    Resource download(String token, String pwd);

    String downloadOriginalName(String token);

    String downloadMimeType(String token);

    void cancel(Long userId, String fileUuid);

    void delete(Long userId, String token);

    void updateStatus(Long userId, String token, Integer status);

    java.util.List<ShareVO> listMyShares(Long userId);
}
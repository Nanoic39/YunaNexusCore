package cc.nanoic.yuna.file.service;

import cc.nanoic.yuna.file.model.vo.FileMetaVO;
import cc.nanoic.yuna.file.model.vo.FileUploadVO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileService {
    FileUploadVO upload(Long userId, MultipartFile file, Integer folderId, Integer category);

    FileMetaVO meta(String fileUuid, Long userId);

    Resource loadResource(String fileUuid, Long userId);

    String originalName(String fileUuid, Long userId);

    String mimeType(String fileUuid, Long userId);

    void delete(String fileUuid, Long userId);

    void rename(String fileUuid, String newName, Long userId);

    FileUploadVO uploadLocal(Long userId, java.io.File file, String fileName, Integer folderId);

    void createFolder(String name, Long parentId, Long userId);

    void move(String uuid, Long targetFolderId, Long userId);

    List<FileMetaVO> list(Long folderId, Long userId);

    String generateDownloadToken(String uuid, Long userId);

    Long validateDownloadToken(String uuid, String token);

    List<FileMetaVO> my(Long userId, int limit);

    List<FileMetaVO> recycleBin(Long userId, int limit);

    void recover(String uuid, Long userId);

    void clean(String uuid, Long userId);
}
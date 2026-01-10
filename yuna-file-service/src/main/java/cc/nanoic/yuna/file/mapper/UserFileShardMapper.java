package cc.nanoic.yuna.file.mapper;

import cc.nanoic.yuna.file.entity.YunaFile;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserFileShardMapper {

    @Insert("""
            INSERT INTO ${table} (
              uuid, user_id, folder_id, origin_name, file_name,
              file_path, file_content, storage_type,
              file_size, file_type, mime_type, identifier,
              category, is_folder, file_count, sub_size,
              create_by, status
            ) VALUES (
              #{e.uuid}, #{e.userId}, #{e.folderId}, #{e.originName}, #{e.fileName},
              #{e.filePath}, #{e.fileContent}, #{e.storageType},
              #{e.fileSize}, #{e.fileType}, #{e.mimeType}, #{e.identifier},
              #{e.category}, #{e.isFolder}, #{e.fileCount}, #{e.subSize},
              #{e.createBy}, #{e.status}
            )
            """)
    int insertOne(@Param("table") String table, @Param("e") YunaFile e);

    @Select("SELECT * FROM ${table} WHERE uuid = #{uuid} LIMIT 1")
    YunaFile selectByUuid(@Param("table") String table, @Param("uuid") String uuid);

    @Select("""
            SELECT * FROM ${table}
            WHERE user_id = #{userId} AND status = 0
            ORDER BY create_time DESC
            LIMIT #{limit}
            """)
    List<YunaFile> listByUser(@Param("table") String table, @Param("userId") Long userId, @Param("limit") int limit);

    @Update("""
            UPDATE ${table}
            SET status = #{status}, delete_by = #{deleteBy}, delete_time = NOW()
            WHERE uuid = #{uuid} AND user_id = #{userId}
            """)
    int updateStatus(@Param("table") String table,
                     @Param("uuid") String uuid,
                     @Param("userId") Long userId,
                     @Param("status") Integer status,
                     @Param("deleteBy") Long deleteBy);

    @Update("""
            UPDATE ${table}
            SET origin_name = #{name}, update_time = NOW()
            WHERE uuid = #{uuid}
            """)
    int updateName(@Param("table") String table, @Param("uuid") String uuid, @Param("name") String name);

    @Select("""
            <script>
            SELECT * FROM ${table}
            WHERE user_id = #{userId} AND status = 0
            <if test="folderId != null">
              AND folder_id = #{folderId}
            </if>
            <if test="folderId == null">
              AND (folder_id IS NULL OR folder_id = 0)
            </if>
            ORDER BY is_folder DESC, create_time DESC
            </script>
            """)
    List<YunaFile> selectByFolder(@Param("table") String table, @Param("userId") Long userId, @Param("folderId") Long folderId);

    @Update("""
            UPDATE ${table}
            SET folder_id = #{folderId}, update_time = NOW()
            WHERE uuid = #{uuid} AND user_id = #{userId}
            """)
    int updateFolderId(@Param("table") String table, @Param("uuid") String uuid, @Param("userId") Long userId, @Param("folderId") Long folderId);

    @Select("SELECT * FROM ${table} WHERE identifier = #{identifier} LIMIT 1")
    YunaFile selectByIdentifier(@Param("table") String table, @Param("identifier") String identifier);
}
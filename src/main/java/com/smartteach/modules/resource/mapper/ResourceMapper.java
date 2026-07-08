package com.smartteach.modules.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteach.modules.resource.entity.Resource;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE biz_resource SET download_count = download_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementDownloadCount(@Param("id") Long id);

    @Update("UPDATE biz_resource SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);

    /**
     * Physical delete: bypasses @TableLogic and removes rows from the table.
     */
    @Delete("<script>DELETE FROM biz_resource WHERE id IN"
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>"
            + "#{id}"
            + "</foreach></script>")
    int physicalDeleteByIds(@Param("ids") List<Long> ids);
}
package com.smartteach.modules.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smartteach.modules.resource.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    @Update("UPDATE biz_resource SET download_count = download_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementDownloadCount(@Param("id") Long id);

    @Update("UPDATE biz_resource SET view_count = view_count + 1 WHERE id = #{id} AND deleted = 0")
    int incrementViewCount(@Param("id") Long id);
}

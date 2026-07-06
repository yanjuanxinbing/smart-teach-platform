package com.smartteach.modules.portal.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.portal.dto.PortalArticleQueryDTO;
import com.smartteach.modules.portal.entity.PortalArticle;

import java.util.List;

public interface PortalArticleService extends IService<PortalArticle> {

    PageResult<PortalArticle> page(PortalArticleQueryDTO query);

    /** 门户前台按栏目类型查询已发布内容（无需登录鉴权） */
    List<PortalArticle> listPublished(Integer type, Integer limit);

    void publish(Long id);

    void offline(Long id);
}

package com.smartteach.modules.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.portal.dto.PortalArticleQueryDTO;
import com.smartteach.modules.portal.entity.PortalArticle;
import com.smartteach.modules.portal.mapper.PortalArticleMapper;
import com.smartteach.modules.portal.service.PortalArticleService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PortalArticleServiceImpl extends ServiceImpl<PortalArticleMapper, PortalArticle> implements PortalArticleService {

    @Override
    public PageResult<PortalArticle> page(PortalArticleQueryDTO query) {
        LambdaQueryWrapper<PortalArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getType() != null, PortalArticle::getType, query.getType())
                .eq(query.getStatus() != null, PortalArticle::getStatus, query.getStatus())
                .like(StringUtils.isNotBlank(query.getKeyword()), PortalArticle::getTitle, query.getKeyword())
                .orderByDesc(PortalArticle::getTop)
                .orderByDesc(PortalArticle::getSort)
                .orderByDesc(PortalArticle::getCreateTime);
        IPage<PortalArticle> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public List<PortalArticle> listPublished(Integer type, Integer limit) {
        return this.lambdaQuery()
                .eq(PortalArticle::getType, type)
                .eq(PortalArticle::getStatus, 1)
                .orderByDesc(PortalArticle::getTop)
                .orderByDesc(PortalArticle::getSort)
                .orderByDesc(PortalArticle::getPublishTime)
                .last(limit != null, "limit " + (limit == null ? 10 : limit))
                .list();
    }

    @Override
    public void publish(Long id) {
        PortalArticle article = new PortalArticle();
        article.setId(id);
        article.setStatus(1);
        article.setPublishTime(LocalDateTime.now());
        this.updateById(article);
    }

    @Override
    public void offline(Long id) {
        PortalArticle article = new PortalArticle();
        article.setId(id);
        article.setStatus(2);
        this.updateById(article);
    }
}

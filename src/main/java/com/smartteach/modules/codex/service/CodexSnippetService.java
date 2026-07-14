package com.smartteach.modules.codex.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.modules.codex.entity.CodexSnippet;
import com.smartteach.modules.codex.mapper.CodexSnippetMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 笔记分享优秀作品展示 service
 */
@Service
public class CodexSnippetService extends ServiceImpl<CodexSnippetMapper, CodexSnippet> {

    /** 前台公开分页 */
    public PageResult<CodexSnippet> sitePage(String lang, String q, long current, long size) {
        LambdaQueryWrapper<CodexSnippet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CodexSnippet::getIsPublic, 1);
        if (StringUtils.isNotBlank(lang)) {
            wrapper.eq(CodexSnippet::getLanguage, lang);
        }
        if (StringUtils.isNotBlank(q)) {
            String kw = q.trim();
            wrapper.and(w -> w.like(CodexSnippet::getTitle, kw)
                    .or().like(CodexSnippet::getTags, kw)
                    .or().like(CodexSnippet::getAuthor, kw));
        }
        wrapper.orderByDesc(CodexSnippet::getCreateTime);
        IPage<CodexSnippet> page = this.page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    /** 公开详情 + 浏览量 +1 */
    @Transactional(rollbackFor = Exception.class)
    public CodexSnippet siteDetail(Long id) {
        CodexSnippet s = this.getById(id);
        if (s == null || s.getIsPublic() == null || s.getIsPublic() != 1) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        // 浏览量 +1（显式 SQL 避免覆盖逻辑删除字段）
        this.lambdaUpdate()
                .eq(CodexSnippet::getId, id)
                .setSql("views = IFNULL(views, 0) + 1")
                .update();
        s.setViews((s.getViews() == null ? 0 : s.getViews()) + 1);
        return s;
    }
}

package com.smartteach.modules.codex.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.codex.entity.CodexSnippet;
import com.smartteach.modules.codex.service.CodexSnippetService;
import com.smartteach.modules.portal.dto.PortalCodexQueryDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台门户 - 笔记分享优秀作品展示（公开）。
 * <p>
 * 仅返回 isPublic=1 的优秀作品；详情接口会顺带 +1 浏览量。
 */
@Api(tags = "门户-笔记分享优秀作品展示")
@RestController
@RequestMapping("/portal/codex")
@RequiredArgsConstructor
public class PortalCodexController {

    private final CodexSnippetService codexService;

    @ApiOperation("笔记分享优秀作品分页（公开）")
    @GetMapping("/page")
    public Result<PageResult<CodexSnippet>> page(PortalCodexQueryDTO query) {
        return Result.success(codexService.sitePage(
                query.getLang(), query.getQ(), query.getCurrent(), query.getSize()));
    }

    @ApiOperation("笔记分享优秀作品详情（公开，访问一次浏览量+1）")
    @GetMapping("/{id}")
    public Result<CodexSnippet> detail(@PathVariable Long id) {
        return Result.success(codexService.siteDetail(id));
    }
}

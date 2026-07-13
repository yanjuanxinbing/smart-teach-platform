package com.smartteach.modules.portal.controller;

import com.smartteach.common.base.PageResult;
import com.smartteach.common.result.Result;
import com.smartteach.modules.portal.dto.PortalArticleQueryDTO;
import com.smartteach.modules.portal.entity.PortalArticle;
import com.smartteach.modules.portal.service.PortalArticleService;
import com.smartteach.modules.portal.service.PortalStatsService;
import com.smartteach.modules.portal.vo.PortalStatsVO;
import com.smartteach.modules.systemmonitor.annotation.OperationLog;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 网站门户模块：
 * - /portal/manage/** 后台管理接口，需要登录鉴权
 * - /portal/site/** 前台门户展示接口，公开访问（已在 SecurityConfig 放行）
 */
@Api(tags = "网站门户")
@RestController
@RequiredArgsConstructor
public class PortalArticleController {

    private final PortalArticleService articleService;

    // 1) 顶部新增依赖注入
    private final PortalStatsService statsService;

    // 2) 在“前台门户展示（公开）”区块新增
    @ApiOperation("门户首页-统计图表数据（公开）")
    @GetMapping("/portal/site/stats")
    public Result<PortalStatsVO> stats() {
        return Result.success(statsService.getStats());
    }

    // ------------------- 后台管理 -------------------

    @ApiOperation("分页查询门户内容（后台管理）")
    @GetMapping("/portal/manage/page")
    @PreAuthorize("hasAuthority('portal:article:list')")
    public Result<PageResult<PortalArticle>> page(PortalArticleQueryDTO query) {
        return Result.success(articleService.page(query));
    }

    @ApiOperation("新增门户内容")
    @PostMapping("/portal/manage")
    @PreAuthorize("hasAuthority('portal:article:add')")
    @OperationLog(module = "门户内容", action = "新增门户内容")
    public Result<Void> add(@Valid @RequestBody PortalArticle article) {
        articleService.save(article);
        return Result.success();
    }

    @ApiOperation("编辑门户内容")
    @PutMapping("/portal/manage")
    @PreAuthorize("hasAuthority('portal:article:edit')")
    @OperationLog(module = "门户内容", action = "编辑门户内容")
    public Result<Void> edit(@Valid @RequestBody PortalArticle article) {
        articleService.updateById(article);
        return Result.success();
    }

    @ApiOperation("批量删除门户内容")
    @DeleteMapping("/portal/manage")
    @PreAuthorize("hasAuthority('portal:article:remove')")
    @OperationLog(module = "门户内容", action = "批量删除门户内容", saveParams = false)
    public Result<Void> remove(@RequestBody List<Long> ids) {
        articleService.removeByIds(ids);
        return Result.success();
    }

    @ApiOperation("发布")
    @PutMapping("/portal/manage/{id}/publish")
    @PreAuthorize("hasAuthority('portal:article:edit')")
    @OperationLog(module = "门户内容", action = "发布门户内容", saveParams = false)
    public Result<Void> publish(@PathVariable Long id) {
        articleService.publish(id);
        return Result.success();
    }

    @ApiOperation("下线")
    @PutMapping("/portal/manage/{id}/offline")
    @PreAuthorize("hasAuthority('portal:article:edit')")
    @OperationLog(module = "门户内容", action = "下线门户内容", saveParams = false)
    public Result<Void> offline(@PathVariable Long id) {
        articleService.offline(id);
        return Result.success();
    }

    // ------------------- 前台门户展示（公开） -------------------

    @ApiOperation("门户首页-按栏目获取已发布内容列表")
    @GetMapping("/portal/site/list")
    public Result<List<PortalArticle>> siteList(@RequestParam Integer type,
            @RequestParam(required = false) Integer limit) {
        return Result.success(articleService.listPublished(type, limit));
    }

    @ApiOperation("门户首页-获取内容详情")
    @GetMapping("/portal/site/{id}")
    public Result<PortalArticle> siteDetail(@PathVariable Long id) {
        return Result.success(articleService.getById(id));
    }
}

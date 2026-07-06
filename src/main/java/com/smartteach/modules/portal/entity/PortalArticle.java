package com.smartteach.modules.portal.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.smartteach.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 网站门户内容：轮播图 / 通知公告 / 新闻资讯，通过 type 区分，
 * 前台门户首页根据 type + status 拉取对应栏目内容渲染。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("portal_article")
public class PortalArticle extends BaseEntity {

    /** 类型 1轮播图 2通知公告 3新闻资讯 */
    private Integer type;

    /** 标题 */
    private String title;

    /** 封面图（轮播图/新闻列表使用） */
    private String coverImage;

    /** 跳转链接（轮播图可配置点击跳转地址，留空则跳转详情页） */
    private String linkUrl;

    /** 正文内容（富文本HTML） */
    private String content;

    /** 排序，数字越小越靠前 */
    private Integer sort;

    /** 是否置顶 0否 1是 */
    private Integer top;

    /** 发布时间 */
    private LocalDateTime publishTime;

    /** 状态 0草稿 1已发布 2已下线 */
    private Integer status;

    /** 浏览次数 */
    private Long viewCount;
}

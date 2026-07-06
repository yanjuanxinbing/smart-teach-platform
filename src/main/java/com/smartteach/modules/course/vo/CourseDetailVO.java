package com.smartteach.modules.course.vo;

import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.entity.CourseChapter;
import com.smartteach.modules.course.entity.CourseContent;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 课程详情：基础信息 + 章节树 + 章节下的内容列表
 */
@Data
public class CourseDetailVO {
    private Course course;
    /** 章节树（含子章节） */
    private List<ChapterNode> chapters = new ArrayList<>();

    @Data
    public static class ChapterNode {
        private CourseChapter chapter;
        private List<CourseContent> contents = new ArrayList<>();
        private List<ChapterNode> children = new ArrayList<>();
    }
}

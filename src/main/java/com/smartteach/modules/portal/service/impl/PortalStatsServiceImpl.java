package com.smartteach.modules.portal.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartteach.modules.course.entity.Course;
import com.smartteach.modules.course.mapper.CourseMapper;
import com.smartteach.modules.portal.entity.PortalArticle;
import com.smartteach.modules.portal.mapper.PortalArticleMapper;
import com.smartteach.modules.portal.service.PortalStatsService;
import com.smartteach.modules.portal.vo.NameValueVO;
import com.smartteach.modules.portal.vo.PortalStatsVO;
import com.smartteach.modules.resource.entity.Resource;
import com.smartteach.modules.resource.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalStatsServiceImpl implements PortalStatsService {

    private final CourseMapper courseMapper;
    private final ResourceMapper resourceMapper;
    private final PortalArticleMapper articleMapper;

    private static final String[] COURSE_TYPE_LABEL = {"", "必修", "选修"};
    private static final String[] RESOURCE_TYPE_LABEL = {"", "文档", "图片", "视频", "音频", "压缩包", "其他"};
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public PortalStatsVO getStats() {
        PortalStatsVO vo = new PortalStatsVO();
        vo.setCourseTypeDistribution(courseTypeDistribution());
        vo.setResourceTypeCount(resourceTypeCount());
        vo.setMonthlyArticleTrend(monthlyArticleTrend());
        vo.setCourseHoursHistogram(courseHoursHistogram());
        return vo;
    }

    private List<NameValueVO> courseTypeDistribution() {
        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<>());
        Map<Integer, Long> grouped = courses.stream()
                .filter(c -> c.getCourseType() != null)
                .collect(Collectors.groupingBy(Course::getCourseType, Collectors.counting()));
        List<NameValueVO> result = new ArrayList<>();
        grouped.forEach((type, count) -> {
            String label = (type >= 0 && type < COURSE_TYPE_LABEL.length) ? COURSE_TYPE_LABEL[type] : "其他";
            result.add(new NameValueVO(label, count));
        });
        return result;
    }

    private List<NameValueVO> resourceTypeCount() {
        List<Resource> resources = resourceMapper.selectList(new LambdaQueryWrapper<>());
        Map<Integer, Long> grouped = resources.stream()
                .filter(r -> r.getResourceType() != null)
                .collect(Collectors.groupingBy(Resource::getResourceType, Collectors.counting()));
        List<NameValueVO> result = new ArrayList<>();
        for (int type = 1; type < RESOURCE_TYPE_LABEL.length; type++) {
            result.add(new NameValueVO(RESOURCE_TYPE_LABEL[type], grouped.getOrDefault(type, 0L)));
        }
        return result;
    }

    private List<NameValueVO> monthlyArticleTrend() {
        List<PortalArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<PortalArticle>()
                .eq(PortalArticle::getStatus, 1));

        LocalDateTime now = LocalDateTime.now();
        // 最近6个月先占位为0，保证没有数据的月份也能在折线图上显示出来
        LinkedHashMap<String, Long> monthMap = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            monthMap.put(now.minusMonths(i).format(MONTH_FMT), 0L);
        }
        for (PortalArticle a : articles) {
            if (a.getPublishTime() == null) continue;
            String key = a.getPublishTime().format(MONTH_FMT);
            if (monthMap.containsKey(key)) {
                monthMap.merge(key, 1L, Long::sum);
            }
        }
        List<NameValueVO> result = new ArrayList<>();
        monthMap.forEach((month, count) -> result.add(new NameValueVO(month, count)));
        return result;
    }

    private List<NameValueVO> courseHoursHistogram() {
        List<Course> courses = courseMapper.selectList(new LambdaQueryWrapper<>());
        String[] labels = {"0-20", "21-40", "41-60", "61-80", "81+"};
        long[] buckets = new long[labels.length];
        for (Course c : courses) {
            Integer hours = c.getTotalHours();
            if (hours == null) continue;
            int idx;
            if (hours <= 20) idx = 0;
            else if (hours <= 40) idx = 1;
            else if (hours <= 60) idx = 2;
            else if (hours <= 80) idx = 3;
            else idx = 4;
            buckets[idx]++;
        }
        List<NameValueVO> result = new ArrayList<>();
        for (int i = 0; i < labels.length; i++) {
            result.add(new NameValueVO(labels[i], buckets[i]));
        }
        return result;
    }
}
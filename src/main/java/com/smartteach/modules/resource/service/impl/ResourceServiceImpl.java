package com.smartteach.modules.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.smartteach.common.base.PageResult;
import com.smartteach.common.exception.BusinessException;
import com.smartteach.common.result.ResultCode;
import com.smartteach.common.utils.UserContext;
import com.smartteach.modules.course.entity.CourseContent;
import com.smartteach.modules.course.mapper.CourseContentMapper;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.dto.ResourceSaveDTO;
import com.smartteach.modules.resource.entity.Resource;
import com.smartteach.modules.resource.entity.ResourceCategory;
import com.smartteach.modules.resource.mapper.ResourceCategoryMapper;
import com.smartteach.modules.resource.mapper.ResourceMapper;
import com.smartteach.modules.resource.service.ResourceService;
import com.smartteach.modules.resource.storage.FileStorage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ResourceServiceImpl extends ServiceImpl<ResourceMapper, Resource> implements ResourceService {

    private final FileStorage fileStorage;
    private final CourseContentMapper courseContentMapper;
    private final ResourceCategoryMapper categoryMapper;

    @Value("${file.access-prefix:/api/files/}")
    private String localAccessPrefix;

    public ResourceServiceImpl(FileStorage fileStorage, CourseContentMapper courseContentMapper, ResourceCategoryMapper categoryMapper) {
        this.fileStorage = fileStorage;
        this.courseContentMapper = courseContentMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public PageResult<Resource> page(ResourceQueryDTO query) {
        LambdaQueryWrapper<Resource> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(query.getResourceName()), Resource::getResourceName, query.getResourceName())
                .eq(query.getResourceType() != null, Resource::getResourceType, query.getResourceType())
                .eq(query.getCategoryId() != null, Resource::getCategoryId, query.getCategoryId())
                .eq(query.getCourseId() != null, Resource::getCourseId, query.getCourseId())
                .like(StringUtils.isNotBlank(query.getTags()), Resource::getTags, query.getTags())
                .eq(query.getStatus() != null, Resource::getStatus, query.getStatus())
                .orderByDesc(Resource::getCreateTime);
        IPage<Resource> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        PageResult<Resource> result = PageResult.of(page);
        fillCategoryAndCourseName(result.getList());
        return result;
    }

    /**
     * For each row, look up category_name (and course_name if missing) via the FK ids,
     * since biz_resource stores redundant name columns that may be empty.
     */
    private void fillCategoryAndCourseName(java.util.List<Resource> list) {
        if (list == null || list.isEmpty()) return;
        java.util.Set<Long> catIds = new java.util.HashSet<>();
        java.util.Set<Long> courseIds = new java.util.HashSet<>();
        for (Resource r : list) {
            if (r.getCategoryId() != null && (r.getCategoryName() == null || r.getCategoryName().isEmpty())) {
                catIds.add(r.getCategoryId());
            }
            if (r.getCourseId() != null && (r.getCourseName() == null || r.getCourseName().isEmpty())) {
                courseIds.add(r.getCourseId());
            }
        }
        java.util.Map<Long, String> catMap = new java.util.HashMap<>();
        if (!catIds.isEmpty()) {
            categoryMapper.selectList(new LambdaQueryWrapper<ResourceCategory>().in(ResourceCategory::getId, catIds))
                    .forEach(c -> catMap.put(c.getId(), c.getCategoryName()));
        }
        for (Resource r : list) {
            if (r.getCategoryId() != null && (r.getCategoryName() == null || r.getCategoryName().isEmpty())) {
                r.setCategoryName(catMap.get(r.getCategoryId()));
            }
        }
    }

    @Override
    public void save(ResourceSaveDTO dto) {
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        resource.setUploadBy(UserContext.getUserId());
        resource.setUploadName(UserContext.getUsername());
        if (resource.getResourceType() == null) resource.setResourceType(1);
        if (resource.getStatus() == null) resource.setStatus(1);
        if (resource.getDownloadCount() == null) resource.setDownloadCount(0);
        if (resource.getViewCount() == null) resource.setViewCount(0);
        this.save(resource);
    }

    @Override
    public void update(ResourceSaveDTO dto) {
        Resource exists = this.getById(dto.getId());
        if (exists == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST);
        }
        Resource resource = new Resource();
        BeanUtils.copyProperties(dto, resource);
        this.updateById(resource);
    }

    /**
     * Resource batch delete:
     *   1) refuse if any row is still referenced by biz_course_content (avoids orphan/dangling resourceId);
     *   2) physically DELETE row (bypassing @TableLogic);
     *   3) delete the underlying file via FileStorage.
     * File IO failures do not roll back the DB (deleteFile is best-effort).
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        // 1) Reference check: refuse if any of these resources is still used as course content
        for (Long id : ids) {
            Long used = courseContentMapper.selectCount(new LambdaQueryWrapper<CourseContent>()
                    .eq(CourseContent::getResourceId, id));
            if (used != null && used > 0) {
                throw new BusinessException("Resource id=" + id + " is still referenced by "
                        + used + " course content(s); detach or delete those first");
            }
        }
        // 2) Snapshot before physical delete (need filePath/fileUrl for file cleanup)
        List<Resource> toDelete = this.listByIds(ids);
        // 3) Physical DB delete (bypassing @TableLogic)
        this.baseMapper.physicalDeleteByIds(ids);
        // 4) Best-effort file cleanup
        if (toDelete != null) {
            for (Resource r : toDelete) {
                deleteFile(r);
            }
        }
    }

    /**
     * Resolve a stored object key from the row, then ask FileStorage to delete it.
     * For backwards compatibility, when filePath is empty we fall back to deriving
     * a relative path from the local-access-prefix'd fileUrl (works for the local
     * backend; for OSS/MinIO the bucket won't match this pattern and we skip).
     */
    private void deleteFile(Resource r) {
        if (r == null) return;
        String key = r.getFilePath();
        if (StringUtils.isBlank(key) && StringUtils.isNotBlank(r.getFileUrl())) {
            // Legacy rows: derive relative key from /api/files/yyyy/MM/dd/xxx.ext
            String prefix = localAccessPrefix.endsWith("/") ? localAccessPrefix : localAccessPrefix + "/";
            if (r.getFileUrl().startsWith(prefix)) {
                key = r.getFileUrl().substring(prefix.length());
            }
        }
        if (StringUtils.isBlank(key)) {
            return;
        }
        try {
            fileStorage.delete(key);
        } catch (Exception e) {
            // File backend is best-effort; never fail the transaction because of it.
            org.slf4j.LoggerFactory.getLogger(ResourceServiceImpl.class)
                    .warn("deleteFile failed: id={}, key={}, err={}", r.getId(), key, e.getMessage());
        }
    }

    @Override
    public void changeStatus(Long id, Integer status) {
        Resource resource = new Resource();
        resource.setId(id);
        resource.setStatus(status);
        this.updateById(resource);
    }

    @Override
    public void incrementViewCount(Long id) {
        this.baseMapper.incrementViewCount(id);
    }

    @Override
    public void incrementDownloadCount(Long id) {
        this.baseMapper.incrementDownloadCount(id);
    }
}
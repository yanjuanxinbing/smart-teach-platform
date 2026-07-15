package com.smartteach.modules.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartteach.modules.resource.dto.ResourceQueryDTO;
import com.smartteach.modules.resource.vo.PortalResourceVO;

/**
 * 门户侧资源服务 —— 学生可用,只暴露上架(status=1)资源
 *
 * <p>对接前端契约见 web-portal/src/api/resource.js</p>
 */
public interface PortalResourceService {

    /**
     * 资源分页(强制 status=1 上架过滤)
     *
     * @param query 分页 + 过滤条件(resourceName/resourceType/categoryId/courseId/tags)
     */
    IPage<PortalResourceVO> pageForPortal(ResourceQueryDTO query);
}

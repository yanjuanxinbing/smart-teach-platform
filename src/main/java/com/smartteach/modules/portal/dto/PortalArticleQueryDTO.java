package com.smartteach.modules.portal.dto;

import com.smartteach.common.base.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PortalArticleQueryDTO extends PageQuery {
    private Integer type;
    private Integer status;
}

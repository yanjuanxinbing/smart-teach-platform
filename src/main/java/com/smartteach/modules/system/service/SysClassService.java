package com.smartteach.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.smartteach.common.base.PageResult;
import com.smartteach.modules.system.dto.SysClassBatchAddDTO;
import com.smartteach.modules.system.dto.SysClassMemberDTO;
import com.smartteach.modules.system.dto.SysClassQueryDTO;
import com.smartteach.modules.system.dto.SysClassSaveDTO;
import com.smartteach.modules.system.entity.SysClass;
import com.smartteach.modules.system.vo.SysClassBatchAddResultVO;
import com.smartteach.modules.system.vo.SysClassVO;
import com.smartteach.modules.user.vo.UserVO;

import java.util.List;

public interface SysClassService extends IService<SysClass> {

    PageResult<SysClassVO> page(SysClassQueryDTO query);

    SysClassVO detail(Long id);

    void save(SysClassSaveDTO dto);

    void update(SysClassSaveDTO dto);

    void remove(List<Long> ids);

    /** 按部门筛选（用于院系级联下拉） */
    List<SysClassVO> listByDept(Long deptId);

    /** 全量有效班级（status=1） */
    List<SysClassVO> listAllEnabled();

    /** 当前登录用户所在班级 */
    List<SysClassVO> listByCurrentUser();

    /** 班级成员列表（按中文角色名过滤："教师"/"学生"/""=全部） */
    List<UserVO> listMembers(Long classId, String roleName);

    /** 整批替换班级成员 */
    void assignMembers(SysClassMemberDTO dto);

    /** 批量绑定班级成员（append-only，已存在则跳过），返回逐条结果 */
    SysClassBatchAddResultVO batchAddMembers(SysClassBatchAddDTO dto);
}
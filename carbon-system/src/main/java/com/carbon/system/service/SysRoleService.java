package com.carbon.system.service;

import com.carbon.system.entity.SysRole;
import com.carbon.common.service.BaseService;
import com.carbon.system.entity.SysRoleMenu;
import com.carbon.system.param.SysRoleMenuAddParam;
import com.carbon.system.param.SysRoleQueryParam;
import com.carbon.system.vo.SysAccountRoleVo;
import com.carbon.system.vo.SysRoleQueryVo;
import com.carbon.common.api.Paging;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 角色  服务类
 * </p>
 *
 * @author Li Jun
 * @since 2021-06-18
 */
public interface SysRoleService extends BaseService<SysRole> {

    /**
     * 根据ID获取查询对象
     * @param id 主键id
     * @return SysRoleQueryVo
     */
    SysRoleQueryVo getSysRoleById(Serializable id);

    /**
     * 获取分页对象
     * @param param SysRoleQueryParam
     * @return Paging<SysRoleQueryVo>
     */
    Paging<SysRoleQueryVo> getSysRolePageList(SysRoleQueryParam param);

    /**
     * 添加角色
     * @param sysRole SysRole
     */
    void addSysRole(SysRole sysRole);

    /**
     * 查询账户角色
     * @param accountId 账户ID
     * @return SysAccountRoleVo
     */
    SysAccountRoleVo getAccountRole(Long accountId);

    /**
     * 保存/更新 账户角色
     * @param accountId 账户ID
     * @param roleIds 角色ID
     */
    void saveAccountRoles(Long accountId,List<Long> roleIds);

    /**
     * 添加角色菜单
     * @param param 参数
     */
    void addRoleMenu(SysRoleMenuAddParam param);


    List<SysRoleMenu> selectRoleMenuByid(Long id);
}

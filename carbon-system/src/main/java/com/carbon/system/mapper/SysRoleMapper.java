package com.carbon.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carbon.system.entity.SysRole;
import com.carbon.system.param.SysRoleQueryParam;
import com.carbon.system.vo.SysAccountRoleVo;
import com.carbon.system.vo.SysRoleQueryVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * <p>
 * 角色  Mapper 接口
 * </p>
 *
 * @author Li Jun
 * @since 2021-06-18
 */
@Repository
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据ID获取查询对象
     * @param id 主键id
     * @return SysRoleQueryVo
     */
    SysRoleQueryVo getSysRoleById(Serializable id);

    /**
     * 获取分页对象
     * @param page 分页参数
     * @param param 查询参数
     * @return IPage<SysRoleQueryVo>
     */
    IPage<SysRoleQueryVo> getSysRolePageList(@Param("page") Page<?> page, @Param("param") SysRoleQueryParam param);

}

package com.carbon.assets.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carbon.assets.entity.CarbonProjectDoc;
import com.carbon.assets.param.CarbonProjectDocQueryParam;
import com.carbon.assets.vo.CarbonProjectDocQueryVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * <p>
 * 碳减排项目文档 Mapper 接口
 * </p>
 *
 * @author Li Jun
 * @since 2022-04-24
 */
@Repository
public interface CarbonProjectDocMapper extends BaseMapper<CarbonProjectDoc> {

    /**
     * 根据ID获取查询对象
     * @param id 主键id
     * @return CarbonProjectDocQueryVo
     */
    CarbonProjectDocQueryVo getCarbonProjectDocById(Serializable id);

    /**
     * 获取分页对象
     * @param page 分页参数
     * @param param 查询参数
     * @return IPage<CarbonProjectDocQueryVo>
     */
    IPage<CarbonProjectDocQueryVo> getCarbonProjectDocPageList(@Param("page") Page<?> page, @Param("param") CarbonProjectDocQueryParam param);

}

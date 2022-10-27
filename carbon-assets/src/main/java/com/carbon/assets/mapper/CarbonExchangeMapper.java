package com.carbon.assets.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.carbon.assets.entity.CarbonExchange;
import com.carbon.assets.param.CarbonExchangeQueryParam;
import com.carbon.domain.assets.vo.CarbonExchangeQueryVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

/**
 * <p>
 * 碳交易所  Mapper 接口
 * </p>
 *
 * @author Li Jun
 * @since 2021-08-16
 */
@Repository
public interface CarbonExchangeMapper extends BaseMapper<CarbonExchange> {

    /**
     * 根据ID获取查询对象
     * @param id 主键id
     * @return CarbonExchangeQueryVo
     */
    CarbonExchangeQueryVo getCarbonExchangeById(Serializable id);

    /**
     * 获取分页对象
     * @param page 分页参数
     * @param param 查询参数
     * @return IPage<CarbonExchangeQueryVo>
     */
    IPage<CarbonExchangeQueryVo> getCarbonExchangePageList(@Param("page") Page<?> page, @Param("param") CarbonExchangeQueryParam param);

}

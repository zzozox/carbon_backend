package com.carbon.domain.assets.api;

import com.carbon.domain.assets.api.hystrix.AssetsServiceApiFallback;
import com.carbon.domain.assets.vo.CarbonExchangeQueryVo;
import com.carbon.domain.common.ApiResult;


import com.carbon.domain.mq.entity.CarbonProjectAddParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 系统服务Api
 * @author Li Jun
 */
@FeignClient(value = "carbon-assets", fallbackFactory = AssetsServiceApiFallback.class)
@RequestMapping("assets")
public interface AssetsServiceApi {


	/**
	 * 根据交易所id获取 交易所详情
	 */
	@GetMapping("/carbonExchange/info/{id}")
	ApiResult<CarbonExchangeQueryVo> getExchangeInfoById(@PathVariable(value = "id") Long id);

	/**
	 * 根据交易所字典获取 交易所详情
	 */
	@GetMapping("/carbonExchange/info/dict/{code}")
	ApiResult<CarbonExchangeQueryVo> getExchangeInfoByDict(@PathVariable(value = "code") String code);

	@PostMapping("/carbonProject/addFeishuProject")
	ApiResult addFeishuProject(@RequestBody(required = false) CarbonProjectAddParam param);
}

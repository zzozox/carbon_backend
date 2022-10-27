package com.carbon.assets.controller;

import com.carbon.domain.assets.vo.MethodologyUploadParam;
import com.carbon.assets.repository.MethodologyRepository;
import com.carbon.assets.service.CarbonMethodologyService;
import com.carbon.assets.param.CarbonMethodologyQueryParam;
import com.carbon.assets.vo.CarbonMethodologyQueryVo;
import com.carbon.assets.entity.CarbonMethodology;
import com.carbon.assets.common.BaseController;
import com.carbon.assets.vo.CarbonMethodologySelectVo;
import com.carbon.domain.common.ApiResult;
import com.carbon.common.api.Paging;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * <p>
 * 碳减排方法学 前端控制器
 * </p>
 *
 * @author Li Jun
 * @since 2021-07-31
 */
@Slf4j
@RestController
@RequestMapping("/carbonMethodology")
@Api(value = "碳减排方法学模块", tags = {"碳减排方法学模块"})
public class CarbonMethodologyController extends BaseController {

    @Autowired
    private CarbonMethodologyService carbonMethodologyService;

    @Autowired
    private MethodologyRepository methodologyRepository;



    /**
     * 添加碳减排方法学
     */
//    @PostMapping("/add")
//    @ApiOperation(value = "添加碳减排方法学",notes = "添加碳减排方法学")
//    public ApiResult<Boolean> addCarbonMethodology(@Valid @RequestBody CarbonMethodology carbonMethodology) {
//        carbonMethodologyService.addCarbonMethodology(carbonMethodology);
//        return ApiResult.ok();
//    }

    /**
     * 添加碳减排方法学
     */
    @PostMapping("/upload")
    @ApiOperation(value = "添加碳减排方法学",notes = "添加碳减排方法学")
    public ApiResult<Boolean> addCarbonMethodology(@Valid @RequestBody MethodologyUploadParam param) {

        carbonMethodologyService.addCarbonMethodology(param);

        return ApiResult.ok();
    }

    /**
     * 修改碳减排方法学
     */
    @PutMapping("/update")
    @ApiOperation(value = "修改碳减排方法学",notes = "修改碳减排方法学")
    public ApiResult<Boolean> updateCarbonMethodology(@Valid @RequestBody CarbonMethodology carbonMethodology) {
        System.out.println(carbonMethodology);
        boolean flag = carbonMethodologyService.updateCarbonMethodology(carbonMethodology);
        return ApiResult.result(flag);
    }

    /**
     * 获取碳减排方法学
     */
    @GetMapping("/info/{id}")
    @ApiOperation(value = "查看碳减排方法学",notes = "查看碳减排方法学")
    public ApiResult<CarbonMethodologyQueryVo> getCarbonMethodology(@PathVariable String id) {
        CarbonMethodologyQueryVo carbonMethodologyQueryVo = carbonMethodologyService.getCarbonMethodologyById(id);
        return ApiResult.ok(carbonMethodologyQueryVo);
    }

    /**
     * 碳减排方法学分页列表
     */
    @PostMapping("/getPageList")
    @ApiOperation(value = "碳减排方法学分页列表",notes = "碳减排方法学分页列表")
    public ApiResult<Paging<CarbonMethodologyQueryVo>> getCarbonMethodologyPageList(@Valid @RequestBody(required = false) CarbonMethodologyQueryParam carbonMethodologyQueryParam) {
        Paging<CarbonMethodologyQueryVo> paging = carbonMethodologyService.getCarbonMethodologyPageList(carbonMethodologyQueryParam);
        return ApiResult.ok(paging);
    }

    /**
     * 获取碳减排方法学列表
     */
    @GetMapping("/list")
    @ApiOperation(value = "方法学下拉列表",notes = "方法学下拉列表")
    public ApiResult<List<CarbonMethodologySelectVo>> getCarbonMethodologyList() {
        return ApiResult.ok(carbonMethodologyService.getCarbonMethodologyList());
    }

    @GetMapping("/gbn/{name}")
    public CarbonMethodology getCarbonMethodologyByName(@PathVariable String name) {
        System.out.println(name);
        try {
            return carbonMethodologyService.getCarbonMethodologyByName(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}


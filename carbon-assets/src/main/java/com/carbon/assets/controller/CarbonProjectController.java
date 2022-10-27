package com.carbon.assets.controller;

import com.carbon.assets.param.CarbonDataSubmissionQueryParam;
import com.carbon.assets.param.CarbonProjectAddParam;
import com.carbon.assets.param.CarbonProjectOwnerDataParam;
import com.carbon.assets.service.CarbonProjectService;
import com.carbon.assets.param.CarbonProjectQueryParam;
import com.carbon.assets.vo.CarbonDetectionDataVo;
import com.carbon.assets.vo.CarbonProjectListVo;
import com.carbon.assets.vo.CarbonProjectQueryVo;
import com.carbon.assets.common.BaseController;
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
 * 碳减排项目 前端控制器
 * </p>
 *
 * @author Li Jun
 * @since 2021-07-31
 */
@Slf4j
@RestController
@RequestMapping("/carbonProject")
@Api(value = "碳减排项目模块", tags = {"碳减排项目模块"})
public class CarbonProjectController extends BaseController {

    @Autowired
    private CarbonProjectService carbonProjectService;
    /**
    * 添加碳减排项目
    */
    @PostMapping("/add")
    @ApiOperation(value = "添加碳减排项目",notes = "添加碳减排项目")
    public ApiResult<CarbonProjectQueryVo> addCarbonProject(@Valid @RequestBody CarbonProjectAddParam param) {
        return ApiResult.ok(carbonProjectService.addCarbonProject(param));
    }

    /**
    * 修改碳减排项目
    */
    @PutMapping("/update")
    @ApiOperation(value = "修改碳减排项目",notes = "修改碳减排项目")
    public ApiResult<Boolean> updateCarbonProject(@Valid @RequestBody CarbonProjectAddParam param) {
        boolean flag = carbonProjectService.updateCarbonProject(param);
        return ApiResult.result(flag);
    }

    /**
     * 修改碳减排项目
     */
    @PostMapping("/uploadOwnerData")
    @ApiOperation(value = "上传业主资料",notes = "上传业主资料")
    public ApiResult<Boolean> uploadOwnerData(@Valid @RequestBody CarbonProjectOwnerDataParam param) {
        carbonProjectService.uploadOwnerData(param);
        return ApiResult.ok();
    }

    /**
    * 删除碳减排项目
    */
    @DeleteMapping("/delete/{id}")
    @ApiOperation(value = "删除碳减排项目",notes = "删除碳减排项目")
    public ApiResult<Boolean> deleteCarbonProject(@PathVariable String id) {
        boolean flag = carbonProjectService.removeById(id);
        return ApiResult.result(flag);
    }

    /**
    * 获取碳减排项目
    */
    @GetMapping("/info/{id}")
    @ApiOperation(value = "查看碳减排项目",notes = "查看碳减排项目")
    public ApiResult<CarbonProjectQueryVo> getCarbonProject(@PathVariable String id) {
        CarbonProjectQueryVo carbonProjectQueryVo = carbonProjectService.getCarbonProjectById(id);
        return ApiResult.ok(carbonProjectQueryVo);
    }

    /**
     * 项目监测数据报送列表
     */
    @PostMapping("/dataSubmissionPageList")
    @ApiOperation(value = "项目-监测数据报送",notes = "监测数据报送")
    public ApiResult<Paging<CarbonProjectListVo>> getDataSubmissionPageList(@Valid @RequestBody(required = false) CarbonProjectQueryParam carbonProjectQueryParam) {
        Paging<CarbonProjectListVo> paging = carbonProjectService.getDataSubmissionPage(carbonProjectQueryParam);
        return ApiResult.ok(paging);
    }



    @GetMapping("/dataSubmissionPage/{id}")
    @ApiOperation(value = "项目-数据报送-填写数据", notes = "填写数据")
    public ApiResult<List<CarbonDetectionDataVo>>dataSubmissionPage(@PathVariable("id")String id) {

        List<CarbonDetectionDataVo> paging = carbonProjectService.getDataSubmissionPageById(id);
        return ApiResult.ok(paging);
    }

    @PostMapping("/dataSubmission")
    @ApiOperation(value = "项目-数据报送-填写数据-报送", notes = "报送")
    public ApiResult<Boolean>dataSubmission(CarbonDataSubmissionQueryParam carbonDataSubmissionQueryParam) {

        boolean flag = carbonProjectService.insertSubmissionToDB(carbonDataSubmissionQueryParam);
        return ApiResult.result(flag);
    }


    @PostMapping("/updateDataSubmissionPage/{id}")
    @ApiOperation(value = "项目-数据报送-修改", notes = "修改数据")
    public ApiResult<List<String>>updateDataSubmissionPage(@PathVariable("id")String id) {

        List<String> carbonDetectionDataVos = carbonProjectService.updateDataSubmissionPage(id);


        return ApiResult.ok(carbonDetectionDataVos);
    }

    @PostMapping("/delDataSubmissionPage/{id}")
    @ApiOperation(value = "项目-数据报送-删除", notes = "删除")
    public ApiResult<Boolean> delDataSubmissionPage(@PathVariable("id")String id) {

        carbonProjectService.updateDataSubmissionPage(id);

        return ApiResult.result(false);
    }



    /**
     * 项目立项分页列表
     */
    @PostMapping("/getPageList")
    @ApiOperation(value = "项目-立项分页列表",notes = "立项分页列表")
    public ApiResult<Paging<CarbonProjectListVo>> getCarbonProjectPageList(@Valid @RequestBody(required = false) CarbonProjectQueryParam carbonProjectQueryParam) {
        Paging<CarbonProjectListVo> paging = carbonProjectService.getCarbonProjectPageList(carbonProjectQueryParam);
        return ApiResult.ok(paging);
    }

    /**
     * 项目开发实施分页列表
     */
    @PostMapping("/getDevelopPageList")
    @ApiOperation(value = "项目-开发实施分页列表",notes = "开发实施分页列表")
    public ApiResult<Paging<CarbonProjectListVo>> getDevelopPageList(@Valid @RequestBody(required = false) CarbonProjectQueryParam carbonProjectQueryParam) {
        Paging<CarbonProjectListVo> paging = carbonProjectService.getCarbonProjectPageList(carbonProjectQueryParam);
        return ApiResult.ok(paging);
    }

    @PostMapping("/addFeishuProject")
    public ApiResult addFeishuProject(@Valid @RequestBody(required = false) CarbonProjectAddParam param) {
        System.out.println("---param:"+param);
        carbonProjectService.addFeishuProject(param);
        return ApiResult.ok();
    }
}


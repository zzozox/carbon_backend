package com.carbon.common.enums;

/**
 * 飞书事件审批定义枚举
 * @author Li Jun
 * @since 2021-06-11
 */
public enum ApprovalCodeEnum {

    ASSETS_APPROVAL("BE8AEA35-16C4-4347-9E07-F2C24815B9E4", "资产上传审批"),
    TRADE_ACCOUNT_APPROVAL("78F37335-2BB3-466A-A058-C90904C8D868", "添加交易账户审批"),
    PROJECT_INITIATION_APPROVAL("6EF513F7-AAE8-42A3-A79E-C38D31E57F75", "项目立项审批"),
    QUOTA_APPROVAL("DE26864E-3A5A-4FCC-BF2C-D99261233EE2","碳配额审批");
    ;

    private String code;
    private String title;

    private ApprovalCodeEnum(String code, String title) {
        this.code = code;
        this.title = title;
    }

    ApprovalCodeEnum() {
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return title;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.title = message;
    }
}

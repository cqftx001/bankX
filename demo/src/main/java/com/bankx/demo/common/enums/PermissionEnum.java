package com.bankx.demo.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionEnum {

    ACCOUNT_READ_OWN(ResourceEnum.ACCOUNT, ActionEnum.READ_OWN, "查看自己账户"),
    ACCOUNT_READ_ALL(ResourceEnum.ACCOUNT, ActionEnum.READ_ALL, "查看所有账户"),
    ACCOUNT_CREATE(ResourceEnum.ACCOUNT, ActionEnum.CREATE, "创建账户"),
    ACCOUNT_UPDATE(ResourceEnum.ACCOUNT, ActionEnum.UPDATE, "修改账户信息"),
    ACCOUNT_FREEZE(ResourceEnum.ACCOUNT, ActionEnum.FREEZE, "冻结账户"),
    ACCOUNT_UNFREEZE(ResourceEnum.ACCOUNT, ActionEnum.UNFREEZE, "解冻账户"),
    ACCOUNT_CLOSE(ResourceEnum.ACCOUNT, ActionEnum.CLOSE, "关闭账户"),
    TRANSACTION_CREATE(ResourceEnum.TRANSACTION, ActionEnum.CREATE, "发起交易"),
    TRANSACTION_READ_OWN(ResourceEnum.TRANSACTION, ActionEnum.READ_OWN, "查看自己交易记录"),
    TRANSACTION_READ_ALL(ResourceEnum.TRANSACTION, ActionEnum.READ_ALL, "查看所有交易"),
    TRANSACTION_REVERSE(ResourceEnum.TRANSACTION, ActionEnum.REVERSE, "冲正交易"),
    AUDIT_LOG_READ(ResourceEnum.AUDIT_LOG, ActionEnum.READ, "查看审计日志"),
    AUDIT_LOG_EXPORT(ResourceEnum.AUDIT_LOG, ActionEnum.EXPORT, "导出审计日志"),
    USER_CREATE(ResourceEnum.USER, ActionEnum.CREATE, "创建用户"),
    USER_READ(ResourceEnum.USER, ActionEnum.READ, "查看用户"),
    USER_UPDATE(ResourceEnum.USER, ActionEnum.UPDATE, "修改用户"),
    USER_DELETE(ResourceEnum.USER, ActionEnum.DELETE, "删除用户"),
    USER_ASSIGN_ROLE(ResourceEnum.USER, ActionEnum.ASSIGN_ROLE, "分配角色"),
    USER_FREEZE(ResourceEnum.USER, ActionEnum.FREEZE, "冻结用户"),
    USER_PROFILE_READ(ResourceEnum.USER_PROFILE, ActionEnum.READ, "查看用户资料"),
    USER_PROFILE_UPDATE(ResourceEnum.USER_PROFILE, ActionEnum.UPDATE, "修改用户资料");

    private final ResourceEnum resource;
    private final ActionEnum action;
    private final String description;

    public String code() {
        return resource.name() + ":" + action.name();
    }
}
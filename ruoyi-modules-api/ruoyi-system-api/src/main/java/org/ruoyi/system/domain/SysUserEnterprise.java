package org.ruoyi.system.domain;

import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 用户企业微信对象 sys_user_enterprise
 */
@Data
public class SysUserEnterprise {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 企业ID
     */
    private Long enterpriseId;
    /**
     * 企业微信用户ID
     */
    private String enterpriseUserId;
    /**
     * 企业微信企业ID
     */
    private String enterpriseCorpId;
    /**
     * 企业微信企业名称
     */
    private String enterpriseName;

    /**
     * 企业微信用户昵称
     */
    private String enterpriseNickname;

    /**
     * 企业微信部门ID
     */
    private Long enterpriseDeptId;

    /**
     * 企业微信部门名称
     */
    private String enterpriseDeptName;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("enterpriseId", getEnterpriseId())
                .append("enterpriseUserId", getEnterpriseUserId())
                .append("enterpriseNickname", getEnterpriseNickname())
                .append("enterpriseDeptId", getEnterpriseDeptId())
                .append("enterpriseDeptName", getEnterpriseDeptName())
                .toString();
    }
}
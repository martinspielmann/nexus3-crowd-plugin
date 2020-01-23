package com.epomeroy.jira.crowd.nexus3.plugin.internal;

import java.util.List;

/**
 * Description:
 * Created: 21 Jan 2020
 *
 * @author epomeroy
 */
public class CrowdConfigurationFile {
    private String serverURL;
    private String appUser;
    private String appPass;
    private boolean authCache;
    private Integer connectTimeout;
    private Integer socketTimeout;
    private Integer requestTimeout;
    private String filterGroup;
    private List<RoleMapping> roleMapping;

    public String getFilterGroup() {
        return filterGroup;
    }

    public void setFilterGroup(String filterGroup) {
        this.filterGroup = filterGroup;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getAppUser() {
        return appUser;
    }

    public void setAppUser(String appUser) {
        this.appUser = appUser;
    }

    public String getAppPass() {
        return appPass;
    }

    public void setAppPass(String appPass) {
        this.appPass = appPass;
    }

    public boolean isAuthCache() {
        return authCache;
    }

    public void setAuthCache(boolean authCache) {
        this.authCache = authCache;
    }

    public List<RoleMapping> getRoleMapping() {
        return roleMapping;
    }

    public void setRoleMapping(List<RoleMapping> roleMapping) {
        this.roleMapping = roleMapping;
    }
}

class RoleMapping {
    private String nexusRole;
    private String jiraRole;

    public String getNexusRole() {
        return nexusRole;
    }

    public void setNexusRole(String nexusRole) {
        this.nexusRole = nexusRole;
    }

    public String getJiraRole() {
        return jiraRole;
    }

    public void setJiraRole(String jiraRole) {
        this.jiraRole = jiraRole;
    }
}

package com.epomeroy.jira.crowd.nexus3.plugin.internal.entity;

public class CrowdUserResult {

    private String name;
    private String emailAddress;
    private String displayName;
    private boolean active;
    private String timeZone;
    private CrowdGroupsResult groups;

    public CrowdGroupsResult getGroups() {
        return groups;
    }

    public void setGroups(CrowdGroupsResult groups) {
        this.groups = groups;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return emailAddress;
    }

    public void setEmail(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getFirstName() {
        return getDisplayName().split(" ")[0];
    }

    public String getLastName() {
        return getDisplayName().split(" ")[1];
    }
}

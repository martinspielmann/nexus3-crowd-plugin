package com.epomeroy.jira.crowd.nexus3.plugin.internal.entity;

import java.util.ArrayList;
import java.util.List;

public class CrowdUsersResult {

    private List<CrowdUserResult> values = new ArrayList<>();

    public List<CrowdUserResult> getValues() {
        return values;
    }

    public void setValues(List<CrowdUserResult> values) {
        this.values = values;
    }
}

package com.epomeroy.jira.crowd.nexus3.plugin.internal.entity;

import java.util.ArrayList;
import java.util.List;

public class CrowdGroupsResult {
    private int size;
    private List<CrowdGroupResult> items = new ArrayList<>();

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<CrowdGroupResult> getItems() {
        return items;
    }

    public void setItems(List<CrowdGroupResult> items) {
        this.items = items;
    }
}

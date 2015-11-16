package com.manywho.services.manywho.database.entities;

import com.manywho.sdk.enums.ContentType;

import java.util.Map;

public class Schema {
    private String id;
    private String name;
    private Map<String, ContentType> fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, ContentType> getFields() {
        return fields;
    }

    public void setFields(Map<String, ContentType> fields) {
        this.fields = fields;
    }
}

package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import lombok.Getter;
import lombok.Setter;

public class Properties {
    @Getter
    @Setter
    private int property_id;
    @Getter @Setter
    private String project;
    @Getter @Setter
    private String property;
    @Getter @Setter
    private String module;
    @Getter @Setter
    private String value;
    @Getter @Setter
    private String instance;
}

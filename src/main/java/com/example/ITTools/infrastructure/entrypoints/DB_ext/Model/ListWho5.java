package com.example.ITTools.infrastructure.entrypoints.DB_ext.Model;

import lombok.Getter;
import lombok.Setter;

public class ListWho5 {
    @Getter @Setter
    private String databaseName;
    @Getter @Setter
    private String timeSec;
    @Getter @Setter
    private String username;
    @Getter @Setter
    private String hostname;
    @Getter @Setter
    private String spid;
    @Getter @Setter
    private String status;
    @Getter @Setter
    private String command;
    @Getter @Setter
    private String proceso;
    @Getter @Setter
    private String sqlBatchText;
    @Getter @Setter
    private String bloqueando;
    @Getter @Setter
    private String physicalIO;
    @Getter @Setter
    private String cpu;
    @Getter @Setter
    private String writes;
    @Getter @Setter
    private String reads;
    @Getter @Setter
    private String logicalReads;
    @Getter @Setter
    private String schedulerId;
    @Getter @Setter
    private String lastWaitType;
    @Getter @Setter
    private String loginTime;
}

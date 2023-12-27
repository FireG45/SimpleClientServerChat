package org.example.util;

public enum LogType {
    INFO("INFO"),
    WARN("WARN"),
    ERROR("ERROR");

    public final String name;

    LogType(String name) {
        this.name = name;
    }
}

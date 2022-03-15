package com.smartbear.tcpbench;

public class Env {
    public static String getEnv(String key) {
        String value = System.getenv(key);
        if (value == null) {
            throw new RuntimeException(String.format("%s must be defined", key));
        }
        return value;
    }
}

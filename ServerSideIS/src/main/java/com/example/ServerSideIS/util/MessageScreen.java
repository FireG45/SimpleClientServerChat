package com.example.ServerSideIS.util;

public class MessageScreen {
    public static String screen(String string) {
        byte[] bytes = string.getBytes();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(b).append(" ");
        return sb.toString();
    }

    public static String unscreen(String string) {
        String[] split = string.split(" ");
        byte[] bytes = new byte[split.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = Byte.parseByte(split[i]);
        }
        return new String(bytes);
    }
}

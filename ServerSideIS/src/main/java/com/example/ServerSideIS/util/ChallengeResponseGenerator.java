package com.example.ServerSideIS.util;

import java.util.Random;

public class ChallengeResponseGenerator {
    public static String generate() {
        Random random = new Random();
        return String.valueOf(random.nextInt());
    }
}

package com.birthdates.quests;

public class TestUtil {

    public static void waitTick() {
        waitFor(50);
    }

    public static void waitFor(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String randomStr() {
        return "test" + Math.random();
    }

    public static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min)) + min;
    }
}

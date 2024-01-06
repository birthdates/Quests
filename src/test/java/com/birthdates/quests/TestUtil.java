package com.birthdates.quests;

public class TestUtil {

    public static void waitTick() {
        try {
            Thread.sleep(50);
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

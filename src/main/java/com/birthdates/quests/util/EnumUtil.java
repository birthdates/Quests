package com.birthdates.quests.util;

public class EnumUtil {
    public static boolean isInvalidEnum(Class<? extends Enum<?>> enumClass, String enumName) {
        for (Enum<?> enumValue : enumClass.getEnumConstants()) {
            if (enumValue.name().equalsIgnoreCase(enumName)) {
                return false;
            }
        }
        return true;
    }
}

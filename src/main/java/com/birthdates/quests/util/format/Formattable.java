package com.birthdates.quests.util.format;


import java.util.Collection;

public interface Formattable {

    Formattable setPlaceholder(String placeholder, String value);

    Formattable setPlaceholder(String placeholder, String[] values);

    default Formattable setPlaceholder(String placeholder, Collection<String> values) {
        return setPlaceholder(placeholder, values.toArray(new String[0]));
    }

    String[] getLines();
}

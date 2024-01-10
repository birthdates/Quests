package com.birthdates.quests.util.format;


import java.util.Collection;

/**
 * A formattable object (placeholders).
 * Ability to turn into {@link String[]}
 */
public interface Formattable {

    /**
     * Set a basic placeholder value
     *
     * @param placeholder Placeholder to replace
     * @param value       Value to replace with
     * @return Formattable object (for chaining)
     */
    Formattable setPlaceholder(String placeholder, String value);

    /**
     * Set a placeholder value with multiple values (will be inserted at placeholder).
     * i.e. if you have format list of ['a', '%test%', 'c'] and you set placeholder '%test%' to ['b', 'b'],
     * the resulting list will be ['a', 'b', 'b', 'c']
     *
     * @param placeholder Placeholder to replace
     * @param values      Values to replace with
     * @return Formattable object (for chaining)
     */
    Formattable setPlaceholder(String placeholder, String[] values);

    /**
     * Set a placeholder value with multiple values (will be inserted at placeholder).
     * See {@link #setPlaceholder(String, String[])}
     *
     * @param placeholder Placeholder to replace
     * @param values      Values to replace with
     * @return Formattable object (for chaining)
     */
    default Formattable setPlaceholder(String placeholder, Collection<String> values) {
        return setPlaceholder(placeholder, values.toArray(new String[0]));
    }

    /**
     * Get formatted lines
     *
     * @return {@link String[]} see {@link Formattable}
     */
    String[] getLines();
}

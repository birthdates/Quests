package com.birthdates.quests.util.format;

import java.util.ArrayList;
import java.util.List;

/**
 * A formattable string (placeholders).
 */
public class FormattableString implements Formattable {
    private final List<StringBuffer> buffers = new ArrayList<>();

    public FormattableString(String string) {
        addString(string, 0);
    }

    /**
     * Insert or add string into buffers
     *
     * @param string String to insert
     * @param index  Index to insert at (0 to add at end)
     */
    private void addString(String string, int index) {
        String[] lines = string.split("\\\\n");
        for (String line : lines) {
            StringBuffer buffer = new StringBuffer(line);
            if (index > 0) {
                buffers.set(index, buffer);
            } else {
                buffers.add(buffer);
            }
        }
    }

    @Override
    public Formattable setPlaceholder(String placeholder, String value) {
        for (StringBuffer buffer : buffers) {
            int index;
            while ((index = buffer.indexOf(placeholder)) >= 0) {
                buffer.replace(index, index + placeholder.length(), value);
            }
        }
        return this;
    }

    @Override
    public Formattable setPlaceholder(String placeholder, String[] values) {
        // Loop in reverse order to avoid CME
        for (int i = buffers.size() - 1; i >= 0; i--) {
            StringBuffer buffer = buffers.get(i);
            int index;
            while ((index = buffer.indexOf(placeholder)) >= 0) {
                for (String value : values) {
                    addString(value, index);
                    buffer.delete(index, index + placeholder.length());
                }
            }
        }
        return this;
    }

    @Override
    public String[] getLines() {
        return buffers.stream().map(StringBuffer::toString).toArray(String[]::new);
    }
}

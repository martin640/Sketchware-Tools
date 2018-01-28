package io.ready.tools;

public class IdHelper {

    public static int getId(String path) {
        StringBuilder builder = new StringBuilder();

        char[] charArray = path.toCharArray();

        for (char c : charArray) {

            if (isNumeric(c) && builder.length() < 3) {
                builder.append(c);
            } else {
                if (builder.length() != 3)
                    builder = new StringBuilder();
            }
        }

        if (builder.length() == 3) {
            return Integer.valueOf(builder.toString());
        } else {
            return -1;
        }
    }

    public static boolean isNumeric(char u) {
        String s = String.valueOf(u);
        return s.matches("[-+]?\\d*\\.?\\d+");
    }
}

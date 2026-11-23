package com.astahpromcp.tool.astah.pro.matrix;

import java.util.Comparator;

// Compares strings in natural order: a run of ASCII digits is compared as a number, so that REQ-2 comes before REQ-10.
// Strings that differ only in the leading zeros of a number, such as REQ-01 and REQ-1, are still told apart by plain string order.
final class NaturalOrderComparator implements Comparator<String> {

    static final NaturalOrderComparator INSTANCE = new NaturalOrderComparator();

    private NaturalOrderComparator() {
    }

    @Override
    public int compare(String a, String b) {
        int natural = compareNaturally(a, b);
        return natural != 0 ? natural : a.compareTo(b);
    }

    private static int compareNaturally(String a, String b) {
        int i = 0;
        int j = 0;
        while (i < a.length() && j < b.length()) {
            char charA = a.charAt(i);
            char charB = b.charAt(j);

            if (isDigit(charA) && isDigit(charB)) {
                int startA = i;
                int startB = j;
                while (i < a.length() && isDigit(a.charAt(i))) {
                    i++;
                }
                while (j < b.length() && isDigit(b.charAt(j))) {
                    j++;
                }

                // Without its leading zeros, the longer number is the larger, and numbers of one length compare as strings
                String numberA = withoutLeadingZeros(a.substring(startA, i));
                String numberB = withoutLeadingZeros(b.substring(startB, j));
                if (numberA.length() != numberB.length()) {
                    return Integer.compare(numberA.length(), numberB.length());
                }
                int compared = numberA.compareTo(numberB);
                if (compared != 0) {
                    return compared;
                }
                continue;
            }

            if (charA != charB) {
                return Character.compare(charA, charB);
            }
            i++;
            j++;
        }

        // The string with characters left over comes after the other
        return Integer.compare(a.length() - i, b.length() - j);
    }

    // Only ASCII digits, so that a number is never read from digits of another script
    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static String withoutLeadingZeros(String digits) {
        int start = 0;
        while (start < digits.length() - 1 && digits.charAt(start) == '0') {
            start++;
        }
        return digits.substring(start);
    }
}

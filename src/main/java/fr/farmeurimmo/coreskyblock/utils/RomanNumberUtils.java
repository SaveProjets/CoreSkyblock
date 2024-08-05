package fr.farmeurimmo.coreskyblock.utils;

import java.util.TreeMap;

public class RomanNumberUtils {

    private final static TreeMap<Integer, String> map = new TreeMap<Integer, String>();

    static {

        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");

    }

    public static String toRoman(int number) {
        int l = map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number - l);
    }

    public static int value(char r) {
        if (r == 'I')
            return 1;
        if (r == 'V')
            return 5;
        if (r == 'X')
            return 10;
        if (r == 'L')
            return 50;
        if (r == 'C')
            return 100;
        if (r == 'D')
            return 500;
        if (r == 'M')
            return 1000;
        return -1;
    }

    public static int fromRoman(String str) {
        int res = 0;
        for (int i = 0; i < str.length(); i++) {

            int s1 = value(str.charAt(i));
            if (i + 1 < str.length()) {

                int s2 = value(str.charAt(i + 1));
                if (s1 >= s2) {
                    res += s1;
                } else {
                    res += (s2 - s1);
                    i++;
                }
            } else {
                res += s1;
            }
        }
        return res;
    }

}

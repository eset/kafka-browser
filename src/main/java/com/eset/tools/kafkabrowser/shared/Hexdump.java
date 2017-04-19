/*
 * Java Hexdump Library
 *
 * Copyright (c) Matthew Johnson 2005
 * Copyright (c) 2018-2019 ESET
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Source:
 * https://github.com/cathive/dbus-java/blob/master/src/main/java/cx/ath/matthew/utils/Hexdump.java
 */

package com.eset.tools.kafkabrowser.shared;

public class Hexdump {
    public static final char[] hexchars = new char[]{'0', '1', '2', '3', '4',
            '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};


    public static String toHex(byte[] buf) {
        return toHex(buf, 0, buf.length);
    }

    public static String toHex(byte[] buf, int ofs, int len) {
        StringBuffer sb = new StringBuffer();
        int j = ofs + len;
        for (int i = ofs; i < j; i++) {
            if (i < buf.length) {
                sb.append(hexchars[(buf[i] & 0xF0) >> 4]);
                sb.append(hexchars[buf[i] & 0x0F]);
                sb.append(' ');
            } else {
                sb.append(' ');
                sb.append(' ');
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    public static String completeHex(String s) {
        String out = "";
        for (int i = s.length(); i < 9; i++) {
            out += 0;
        }
        return out + s;
    }
}

/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.shared;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;

public class Shared {


    public static final String ANONYMOUS = "anonymous";
    public static final String START = "start";
    public static final String END = "end";

    public static String[] splitPath(String path) {
        return path.replace(getBasePath(), "").split("/");

    }

    public static String getBasePath() {
        if (Config.BASE != null && !Config.BASE.equals("")) {
            return "/" + Config.BASE;
        } else {
            return "";
        }
    }

    public static String doubleToString(Double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#");
        return decimalFormat.format(value);
    }

    public static class ExceptionConnection extends Exception {
    }

    public static enum VIEWER {
        TEXT, JSON, XML, HEX
    }
    public String getDoubleString(double value){
        DecimalFormat decimalFormat = new DecimalFormat("#");
        return decimalFormat.format(value);
    }

    public static Integer[] limits() {
        Integer[] limits = {15, 30, 100, 500, 1000};
        return limits;
    }


    public static class ExceptionUnauthorized extends Exception {
    }



    public static String getHost(String ip) {
        String hostname = "";
        try {
            hostname = InetAddress.getByName(ip).getHostName();
            if (hostname.equals(ip)) {
                return ip;
            } else if (hostname.split("\\.").length > 0) {
                return hostname.split("\\.")[0];
            } else {
                return hostname;
            }


        } catch (UnknownHostException e) {
            e.printStackTrace();
            return ip;
        }

    }
}
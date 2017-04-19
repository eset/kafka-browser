/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.shared;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import scala.xml.Node;
import scala.xml.PrettyPrinter;
import scala.xml.TopScope$;
import scala.xml.XML;

public class Renderer {

    String sourceData;

    public Renderer(String data) {
        sourceData = data;
    }

    /*
    JSON
     */

    public String renderJSON() {
        String out = "";
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(sourceData);
            String prettyJsonString = gson.toJson(je);
            out = prettyJsonString;
        } catch (Exception e) {
            out = "Malformed JSON";
        }

        return out;
    }

    /*
    XML
     */
    public String renderXML() {
        String out = "";
        try {
            PrettyPrinter pp = new PrettyPrinter(150, 3);
            String formatted = pp.format((Node) XML.loadString(sourceData), TopScope$.MODULE$);
            out = formatted;
        } catch (Exception e) {
            out = "Malformed XML";
        }
        return out;
    }
    /*
    HEX
     */

    public String renderHex() {
        String out = "";

        String part = "";
        String partFirst = "";
        int position = 0;
        boolean wasend = false;
        boolean setOffset = true;
        int offset = 0;
        Hexdump hex = new Hexdump();
        int i = 0;
        for (i = 0; i < sourceData.length(); i++) {
            if (setOffset) {
                offset = i;
                setOffset = false;

            }

            wasend = false;
            position++;
            part += sourceData.charAt(i);

            if (position == 8) {
                out += Hexdump.completeHex(Integer.toHexString(offset)) + " | " + hex.toHex(part.getBytes()) + " | ";
                partFirst = part;
                part = "";

            } else if (position == 16) {
                out += hex.toHex(part.getBytes()) + " | " + partFirst + part + "\r\n";
                part = "";
                partFirst = "";
                position = 0;
                wasend = true;
                setOffset = true;

            }
        }

        if (!wasend) {
            out += hex.toHex(part.getBytes()) + " | " + partFirst + part + "\r\n";
        }
        return out;
    }
}

/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.shared;

import com.vaadin.server.VaadinService;

import org.apache.commons.configuration.*;
import scala.Int;

import java.util.*;

public class Config {

    public static String PORT = "9092" ;
    public static String SECUREPORT = "9093" ;
    public static Object GROUPNAME = "kafkabrowsergui" ;
    public static List<String> CLUSTERNAMES = new ArrayList();
    public static HashMap<String, List> CLUSETBROKERS = new HashMap<String, List>();
    public static String SERVERPATH = "";
    public static String BASE;
    public static int POLLTRIES=3;


    private static Config instance = null;
    XMLConfiguration config;


    public Config() {

        try {

            String conffile = "";

            conffile = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath() + "/appconf.xml";

            XMLConfiguration config = new XMLConfiguration(conffile);
            Config.CLUSTERNAMES = config.getList("clusters.cluster.name");
            for (int i = 0; i < Config.CLUSTERNAMES.size(); i++) {
                Config.CLUSETBROKERS.put(Config.CLUSTERNAMES.get(i).toString(), config.getList("clusters.cluster(" + i + ").brokers"));
            }
            Config.BASE = config.getString("base");
            Config.PORT = config.getString("port");
            Config.SECUREPORT = config.getString("secureport");
            Config.GROUPNAME = config.getString("groupname");
            Config.POLLTRIES = Integer.valueOf(config.getString("polltries"));
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Config  getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public Configuration getConfig() {
        return config;
    }


}





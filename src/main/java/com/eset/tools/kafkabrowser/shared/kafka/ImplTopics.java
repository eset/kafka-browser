/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.shared.kafka;

import com.eset.tools.kafkabrowser.Kafkabrowser;
import com.vaadin.ui.UI;

import com.eset.tools.kafkabrowser.shared.Shared;

import java.util.*;
public class ImplTopics {
    public static String[] getTopics() throws Exception {

        String[] topics = KafkaConnect.getInstance().getTopics();

        Arrays.sort(topics);
        return topics;
    }

    public static HashMap<Integer, InfoObject> getTopicInfo(String topic) throws Exception {


        return KafkaConnect.getInstance().getTopicInfo(topic);
    }

    public static InfoObject getTopicPartition(String topic, int partition) throws Exception {


        return KafkaConnect.getInstance().getTopicInfo(topic).get(partition);
    }


    public static Integer[] getTopicPartitions(String topic) throws Exception {
        Integer[] partitions = KafkaConnect.getInstance().getTopicsPartitions(topic);


        return partitions;
    }


    public static List<MessageObject> getMessages(String topic, int partition, String offset) throws Exception {

        Long offsetLong;
        if (offset.equals(Shared.END)) {
            InfoObject info = getTopicPartition(topic, partition);
            offsetLong = info.getHighwater() - ((Kafkabrowser) UI.getCurrent()).LIMIT;
            if (offsetLong < 0) {
                offsetLong = Long.valueOf(0);
            }
        } else if (offset.equals(Shared.START)) {
            InfoObject info = getTopicPartition(topic, partition);
            offsetLong = info.getStart();
        } else {
            offsetLong = Long.valueOf(offset);

        }


        return KafkaConnect.getInstance().getMessages(topic, Integer.valueOf(partition), offsetLong);

    }


}

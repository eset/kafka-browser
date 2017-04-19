/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.shared.kafka;

import com.eset.tools.kafkabrowser.shared.Shared;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class MessageObject {
    String topic;
    int partition;
    String value;
    String key;
    double offset;

    public MessageObject(String topic, int partition, ConsumerRecord<String, String> message) {
        this.topic = topic;
        this.partition = partition;
        this.value = message.value();
        this.key = message.key();
        this.offset = message.offset();
    }

    public MessageObject() {

    }

    public String getTopic() {
        return topic;
    }

    public int getPartition() {
        return partition;
    }

    public String getValue() {
        return value;
    }

    public String getOffset() {
        return Shared.doubleToString(offset);
    }

    public Double getDoubleOffset() {
        return offset;
    }

    public String getKey() {
        return key;
    }

}

/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.shared.kafka;

public class InfoObject {
    String topic;
    int partition;
    long start;
    long highwater;

    public InfoObject(String topic, int partition, Long actualStartOffset, Long actualEndOffset) {
        this.topic=topic;
        this.partition=partition;
        this.start=actualStartOffset;
        this.highwater=actualEndOffset;
    }

    public String getTopic() {
        return topic;
    }
    public long getStart() {
        return start;
    }
    public int getPartition() {
        return partition;
    }
    public long getHighwater() {
        return highwater;
    }

}

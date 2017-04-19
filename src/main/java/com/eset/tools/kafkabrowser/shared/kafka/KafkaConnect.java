/*
 * # Copyright (c) 2018-2019 ESET
 * # See LICENSE file for redistribution.
 */

package com.eset.tools.kafkabrowser.shared.kafka;

import com.eset.tools.kafkabrowser.Kafkabrowser;
import com.vaadin.ui.UI;
import com.eset.tools.kafkabrowser.shared.Config;
import com.eset.tools.kafkabrowser.shared.Shared;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.*;

public class KafkaConnect {


    public static HashMap<String, HashMap<String, InfoObject>> topicState;
    private HashMap<String, KafkaConsumer<String, String>> consumers = new HashMap<>();
    public List<String> topics = new ArrayList<>();
    private static KafkaConnect instance;

    public KafkaConnect() {

    }


    public static synchronized KafkaConnect getInstance() throws Shared.ExceptionUnauthorized {

        if (instance != null) {
            return instance;
        } else {
            throw new Shared.ExceptionUnauthorized();
        }

    }

    public static synchronized void initInstance(String username, String password) throws Shared.ExceptionUnauthorized {

        if (instance == null) {
            instance = new KafkaConnect();
        }

        if (!username.equals(Shared.ANONYMOUS)) {
            instance.consumers.put(username, getConsumer(username, password));
        }

    }

    /*
    Consumer
     */
    private static KafkaConsumer<String, String> getConsumer(String username, String password) throws Shared.ExceptionUnauthorized {
        Properties props = new Properties();
        String actualCluster = ((Kafkabrowser) UI.getCurrent()).CLUSTER;
        List brokers = Config.CLUSETBROKERS.get(actualCluster);


        if (username.equals(Shared.ANONYMOUS)) {
            String servers = "";
            for (Object broker : brokers) {
                if (!servers.equals("")) {
                    servers += ",";
                }
                servers += broker + ":"+Config.PORT;
            }
            props.put("bootstrap.servers", servers);
        } else {
            String servers = "";
            for (Object broker : brokers) {
                if (!servers.equals("")) {
                    servers += ",";
                }
                servers += broker + ":"+Config.SECUREPORT;
            }
            props.put("bootstrap.servers", servers);
            props.put("sasl.mechanism", "PLAIN");
            props.put("security.protocol", "SASL_PLAINTEXT");
            props.put("sasl.jaas.config",
                    "org.apache.kafka.common.security.plain.PlainLoginModule required\n" +
                            "username=\"" + username + "\"\n" +
                            "password=\"" + password + "\";");
        }
        props.put("auto.offset.reset", "earliest");
        props.put("connections.max.idle.ms", "5000");
        props.put("group.id", Config.GROUPNAME);
        props.put("enable.auto.commit", "false");
        props.put("exclude.internal.topics", true);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer=new KafkaConsumer<String, String>(props);


        if (checkConnection(consumer, username)) {

            ((Kafkabrowser) UI.getCurrent()).USERNAME = username;

            return consumer;
        } else {
            throw new Shared.ExceptionUnauthorized();
        }

    }


    /*
    List topics
     */
    public String[] getTopics() throws Shared.ExceptionUnauthorized {

        KafkaConsumer<String, String> consumer = getConsumer();
        Map<String, List<PartitionInfo>> topics;
        topics = consumer.listTopics();

        Set<String> topicList = topics.keySet();

        consumerClose(consumer);
        return topicList.toArray(new String[topicList.size()]);
    }

    /*
    List partitions
     */

    public Integer[] getTopicsPartitions(String topic) throws Shared.ExceptionUnauthorized {

        KafkaConsumer<String, String> consumer = getConsumer();
        List<PartitionInfo> list = consumer.partitionsFor(topic);

        List<Integer> partitionsList = new ArrayList<Integer>();
        for (PartitionInfo detail : list) {
            partitionsList.add(detail.partition());
        }


        Integer[] partitions = partitionsList.toArray(new Integer[0]);
        Arrays.sort(partitions);

        consumerClose(consumer);
        return partitions;
    }

    /*
    Get topic info
     */

    public HashMap<Integer, InfoObject> getTopicInfo(String topic) throws Shared.ExceptionUnauthorized {

        KafkaConsumer<String, String> consumer = getConsumer();
        HashMap<Integer, InfoObject> hashMap = new HashMap<Integer, InfoObject>();
        Integer[] partitionList = getTopicsPartitions(topic);

        Set<TopicPartition> partitions = new HashSet<TopicPartition>();
        for (Integer part : partitionList) {
            TopicPartition actualTopicPartition = new TopicPartition(topic, part);
            partitions.add(actualTopicPartition);
        }
        consumer.assign(partitions);

        for (TopicPartition tPart :
                partitions) {
            Long actualStartOffset = consumer.beginningOffsets(partitions).get(tPart);
            Long actualEndOffset = consumer.endOffsets(partitions).get(tPart);
            long actualPosition = consumer.position(tPart);
            InfoObject partInfo = new InfoObject(topic, tPart.partition(), actualStartOffset, actualEndOffset);
            hashMap.put(partInfo.getPartition(), partInfo);

        }
        consumerClose(consumer);

        return hashMap;

    }

    /*
    Get messages
     */
    public List<MessageObject> getMessages(String topic, int partition, long offset) throws Shared.ExceptionUnauthorized {


        List<MessageObject> messages = new ArrayList<>();
        Set<TopicPartition> partitions = new HashSet<TopicPartition>();
        TopicPartition actualTopicPartition = new TopicPartition(topic, partition);
        partitions.add(actualTopicPartition);
        KafkaConsumer<String, String> consumer = getConsumer();
        consumer.assign(partitions);
        consumer.seek(actualTopicPartition, offset);

        int tryPoll = 0;
        while (tryPoll<Config.POLLTRIES) {
            if (messages.size()==((Kafkabrowser) UI.getCurrent()).LIMIT){
                break;
            }
            ConsumerRecords<String, String> data = consumer.poll(Duration.ofSeconds(5));


            for(ConsumerRecord<String, String> message: data){
                MessageObject offsetMessage = new MessageObject(topic, partition, message);
                messages.add(offsetMessage);
                if (messages.size() > ((Kafkabrowser) UI.getCurrent()).LIMIT) {
                    break;
                }
            }
            tryPoll++;
        }
        consumerClose(consumer);
        return messages;

    }

    /*
    Close consumer -only anonymous
     */
    private void consumerClose(KafkaConsumer<String, String> consumer) {
        if (((Kafkabrowser) UI.getCurrent()).USERNAME.equals(Shared.ANONYMOUS)) {
            consumer.close();
        }
    }

    /*
    Get Consumer
     */

    private KafkaConsumer<String, String> getConsumer() throws Shared.ExceptionUnauthorized {


        String username = ((Kafkabrowser) UI.getCurrent()).USERNAME;
        if (consumers.containsKey(username)) {
            return consumers.get(username); //authorized
        } else {
            return getConsumer(Shared.ANONYMOUS, ""); //anonymous
        }
    }
 /*
    Check connection after authorization
     */

    public static boolean checkConnection(final KafkaConsumer<String, String> consumer, String username) {
        if (!username.equals(Shared.ANONYMOUS)) {
            try {
                Thread checker = new Thread(new Runnable() {
                    public void run() {
                        try {
                            consumer.listTopics();


                        } catch (Exception e) {

                        }
                    }
                });
                checker.start();
                int i = 0;
                while (checker.isAlive()) {
                    i++;
                    Thread.sleep(1000);
                    if (i > 5) {
                        checker.interrupt();
                        return false;
                    }
                }

            } catch (KafkaException e) {
                return false;


            } catch (InterruptedException e) {
                System.out.println("Something wrong");

                return false;
            }
        }

        return true;
    }

}


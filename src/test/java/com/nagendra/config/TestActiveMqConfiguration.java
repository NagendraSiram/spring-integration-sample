package com.nagendra.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import javax.jms.Destination;


/**
 * Created by nagendra on 26/04/2018.
 */
@TestConfiguration
public class TestActiveMqConfiguration {

    public static final String JMS_BROKER_URL = "vm://localhost";

    @Bean(name = "orderQueue")
    public Destination orderQueue() {
        return new ActiveMQQueue("test.queue");
    }

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        return new ActiveMQConnectionFactory(JMS_BROKER_URL);
    }

}

package com.nagendra;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;

/**
 * Created by nagendra on 17/04/2018.
 */
@Component
public class ActiveMqConfiguration {

    @Value("${activemq.broker-url}")
    private String brokerUrl;

    @Bean
    public ActiveMQConnectionFactory activeMQConnectionFactory() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(brokerUrl);

        return activeMQConnectionFactory;
    }


    @Bean
    public CachingConnectionFactory cachingConnectionFactory() {
        return new CachingConnectionFactory(activeMQConnectionFactory());
    }
}

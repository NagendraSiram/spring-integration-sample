package com.nagendra;

import com.nagendra.service.OrderService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.aggregator.TimeoutCountSequenceSizeReleaseStrategy;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.jms.dsl.Jms;

/**
 * Created by nagendra on 18/12/2018.
 */
public class AggregateBufferExampleFlow {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    @Value("${input.order.queue}")
    private String sourceQueue;

    @Autowired
    private OrderService orderService;

    @Bean
    public IntegrationFlow saveBatchFlow() {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(activeMQConnectionFactory)
                        .destination(sourceQueue))
                    .aggregate(aggregatorSpec -> aggregatorSpec.correlationStrategy(m ->1)
                        .expireGroupsUponCompletion(true)
                        .expireGroupsUponTimeout(true)
                        .groupTimeout(1000l)
                        .releaseStrategy(new TimeoutCountSequenceSizeReleaseStrategy(5, 1000l)))
                .handle(orderService, "save")
                .get();
    }

}

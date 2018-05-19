package com.nagendra;

import com.nagendra.service.OrderService;
import com.nagendra.transformer.OrderTransformer;
import com.nagendra.vo.OrderVO;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.advice.IdempotentReceiverInterceptor;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.integration.selector.MetadataStoreSelector;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.jms.Session;

/**
 * Created by nagendra on 24/04/2018.
 */
@EnableIntegration
public class OrderPersistFlow {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    @Autowired
    private OrderTransformer orderTransformer;

    @Autowired
    private OrderService orderService;

    @Value("${input.order.queue}")
    private String sourceQueue;

    @Bean
    public IntegrationFlow persistFlow() {
        return IntegrationFlows
                .from(Jms.messageDrivenChannelAdapter(activeMQConnectionFactory, SimpleMessageListenerContainer.class)
                        .id("orderInputChannel")
                        .destination(sourceQueue)
                        .jmsMessageConverter(new MarshallingMessageConverter(jaxbMarshaller()))
                        .configureListenerContainer(spec -> {
                            spec.sessionTransacted(false);
                            spec.sessionAcknowledgeMode(Session.DUPS_OK_ACKNOWLEDGE);
//                            spec.cacheLevel(CACHE_SESSION);
//                            spec.maxMessagesPerTask(50);
//                            spec.concurrentConsumers(1);
                        }))
                .channel("beforeTransform")
                .transform(orderTransformer, "transform", e -> e.advice(idempotentReceiverInterceptor()))
                .handle(orderService, "save")
                .get();
    }

    @Bean
    public IdempotentReceiverInterceptor idempotentReceiverInterceptor() {
        IdempotentReceiverInterceptor idempotentReceiverInterceptor = new IdempotentReceiverInterceptor(new MetadataStoreSelector(m ->
                (String) m.getHeaders().get("JMSMessageId")));
        idempotentReceiverInterceptor.setDiscardChannelName("ignoreDuplicates");
        idempotentReceiverInterceptor.setThrowExceptionOnRejection(false);
        return idempotentReceiverInterceptor;
    }

    @Bean
    public IntegrationFlow ignoreDuplicatesFlow(){
        return IntegrationFlows.from("ignoreDuplicates")
                .handle(message -> System.out.println("Duplicate:" + message))
                .get();
    }

    private Jaxb2Marshaller jaxbMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setMappedClass(OrderVO.class);
        jaxb2Marshaller.setPackagesToScan("com.nagendra.vo");
        return jaxb2Marshaller;
    }

}

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
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.jms.support.converter.MarshallingMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

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
                .from(Jms.inboundGateway(activeMQConnectionFactory)
                        .id("orderInputChannel")
                        .destination(sourceQueue)
                        .jmsMessageConverter(new MarshallingMessageConverter(jaxbMarshaller()))
                        .configureListenerContainer(spec -> spec.get().setSessionTransacted(true)))
                .channel("beforeTransform")
                .transform(orderTransformer)
                .handle(orderService, "save")
                .get();
    }

    private org.springframework.oxm.Marshaller jaxbMarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setMappedClass(OrderVO.class);
        jaxb2Marshaller.setPackagesToScan("com.nagendra.vo");
        return jaxb2Marshaller;
    }

}

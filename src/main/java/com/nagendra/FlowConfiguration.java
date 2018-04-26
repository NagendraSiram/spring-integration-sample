package com.nagendra;

import com.nagendra.vo.OrderVO;
import com.nagendra.vo.OrderStatusType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.advice.ErrorMessageSendingRecoverer;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.http.dsl.Http;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.messaging.MessageChannel;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by nagendra on 17/04/2018.
 */
@Component
@EnableIntegration
public class FlowConfiguration {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    @Value("${input.order.queue}")
    private String sourceQueue;

    @Bean
    public IntegrationFlow processFlow() {
        return IntegrationFlows.from(Jms.inboundGateway(activeMQConnectionFactory)
                .destination(sourceQueue)
//                .jmsMessageConverter()
                .configureListenerContainer(spec -> spec.get().setSessionTransacted(true)))
                .log(LoggingHandler.Level.DEBUG, "dev", m -> m.getPayload())
                .log(LoggingHandler.Level.DEBUG, "dev", m -> m.getHeaders())
                .<OrderVO, OrderStatusType>route(orderVO -> orderVO.getOrderStatusType(), mapping -> mapping
                        .channelMapping(OrderStatusType.CREATE, "createFlow")
                        .channelMapping(OrderStatusType.UPDATE, "createFlow")
                        .channelMapping(OrderStatusType.DELETE, "createFlow")
                        .defaultOutputChannel("nullChannel")
                        .resolutionRequired(false)
                ).get();
    }

    @Bean
    public IntegrationFlow performCreate() {
        return IntegrationFlows.from("createFlow")
                .handle(Http.outboundGateway("http://localhost:8080/create")
                                .httpMethod(HttpMethod.GET)
                                .expectedResponseType(String.class)
                                .requestFactory(simpleClientHttpRequestFactory()),
                        e -> e.advice(retryAdvice()))
                .get();
    }

    @Bean
    public RequestHandlerRetryAdvice retryAdvice() {
        RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();

        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(3));
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(5);
        backOffPolicy.setMaxInterval(6000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        requestHandlerRetryAdvice.setRetryTemplate(retryTemplate);

        requestHandlerRetryAdvice.setRecoveryCallback(errorMessageSendingRecoverer());
        return requestHandlerRetryAdvice;
    }

    @Bean
    public ErrorMessageSendingRecoverer errorMessageSendingRecoverer() {
        return new ErrorMessageSendingRecoverer(recoveryChannel());
    }

    @Bean
    public MessageChannel recoveryChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow handleRecovery() {
        return IntegrationFlows.from("recoveryChannel")
                .log(LoggingHandler.Level.ERROR, "error",
                        m -> m.getPayload())
                .handle((p, h) -> null)
                .get();
    }

    private SimpleClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setReadTimeout(5000);
        simpleClientHttpRequestFactory.setConnectTimeout(5000);
        return simpleClientHttpRequestFactory;
    }
}

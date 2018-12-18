package com.nagendra;

import com.nagendra.domain.OrderDetails;
import com.nagendra.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.Level.CATEGORY;
import static org.springframework.integration.handler.LoggingHandler.Level.INFO;
import static org.springframework.integration.http.dsl.Http.outboundGateway;

/**
 * Created by nagendra on 18/12/2018.
 */
public class ScatterGatterExampleFlow {

    @Autowired
    private OrderService orderService;

    @Bean
    public IntegrationFlow processMultipleRequestsFlow() {
        return IntegrationFlows
                .from("receiverChannel")
                .scatterGather(scatterer -> scatterer
                        .applySequence(true)
                        .recipientFlow(fetchRoles())
                        .recipientFlow(fetchGroups()))
                .log(INFO, CATEGORY, m -> "Aggregated List: " + m.getPayload())
                .<List<OrderDetails[]>>handle((p, h) -> p.stream().flatMap(m -> Arrays.asList(m).stream()).collect((Collectors.toList())))
                .handle(orderService, "save")
                .get();
    }

    @Bean
    public IntegrationFlow fetchRoles() {
        return IntegrationFlows.from("fetch.roles")
                .handle(outboundGateway("/{departmentId}/roles")
                        .uriVariable("departmentId", m -> m.getHeaders().get("departmentId"))
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(OrderDetails[].class))
                .get();
    }

    @Bean
    public IntegrationFlow fetchGroups() {
        return IntegrationFlows.from("fetch.groups")
                .handle(outboundGateway("/{departmentId}/groups")
                        .uriVariable("departmentId", m -> m.getHeaders().get("departmentId"))
                        .httpMethod(HttpMethod.GET)
                        .expectedResponseType(OrderDetails[].class))
                .get();
    }
}

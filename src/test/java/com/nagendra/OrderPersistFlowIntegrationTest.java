package com.nagendra;

import com.nagendra.config.TestActiveMqConfiguration;
import com.nagendra.domain.OrderDetails;
import com.nagendra.service.OrderService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by nagendra on 26/04/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {MainApplication.class, TestActiveMqConfiguration.class})
@Transactional
public class OrderPersistFlowIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private ActiveMQConnectionFactory activeMQConnectionFactory;

    @Value("${input.order.queue}")
    private String inputQueue;

    @Autowired
    private OrderService orderService;

    private JmsTemplate jmsTemplate;

    @PostConstruct
    public void init() {
        this.jmsTemplate = new JmsTemplate(activeMQConnectionFactory);
        this.jmsTemplate.setPubSubDomain(true);
    }

    @Before
    public void setUp() {
        jdbcTemplate.execute("insert into user (id, name) values (1, \'nagendra\'");
    }

    @Test
    public void persistFlowIntegrationTest() {
        String payload = "<order>\n" +
                "    <id>order-001</id>\n" +
                "    <name>testOrder</name>\n" +
                "    <description>test Order</description>\n" +
                "</order>";
        jmsTemplate.convertAndSend(inputQueue, message -> {
            message.setStringProperty("userId", "user001");
            return message;
        });

        OrderDetails orderDetails = orderService.get("order-001");
        //This is failing because the order persisted by integration flow is not visible to the test
        assertThat(orderDetails, is(notNullValue()));
    }
}
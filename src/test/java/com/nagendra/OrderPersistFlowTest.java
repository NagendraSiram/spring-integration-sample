package com.nagendra;

import com.nagendra.vo.OrderVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.test.context.MockIntegrationContext;
import org.springframework.integration.test.context.SpringIntegrationTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

/**
 * Created by nagendra on 26/04/2018.
 */
@RunWith(SpringRunner.class)
@SpringIntegrationTest(noAutoStartup = "orderInputChannel")
public class OrderPersistFlowTest {

    @Autowired
    private MockIntegrationContext mockIntegrationContext;

    @Test
    public void persistFlowTest(){
        OrderVO orderVO = new OrderVO();
        orderVO.setId("1234");
        orderVO.setName("TestOrder");
        orderVO.setDescription("order desc");

        MessageSource<OrderVO> messageSource = () -> new GenericMessage<>(orderVO);

        this.mockIntegrationContext.substituteMessageSourceFor("beforeTransform", messageSource);

        //seeing the following error
        //org.springframework.beans.factory.NoSuchBeanDefinitionException: No bean named 'beforeTransform' available

        Message<?> receive = messageSource.receive();
    }
}
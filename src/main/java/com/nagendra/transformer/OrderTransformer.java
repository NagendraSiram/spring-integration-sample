package com.nagendra.transformer;

import com.nagendra.domain.OrderDetails;
import com.nagendra.vo.OrderVO;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Created by nagendra on 26/04/2018.
 */
@Component
public class OrderTransformer {

    @Transformer
    public OrderDetails transform(OrderVO order, @Header String userId) {
        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setId(order.getId());
        orderDetails.setName(order.getName());
        orderDetails.setDescription(order.getDescription());
        orderDetails.setUserId(userId);
        return orderDetails;
    }
}

package com.nagendra.service;

import com.nagendra.domain.OrderDetails;
import org.springframework.stereotype.Component;

/**
 * Created by nagendra on 26/04/2018.
 */
@Component
public class OrderService {

    public void save(OrderDetails orderDetails) {

    }

    public OrderDetails get(String orderId) {
        return new OrderDetails();
    }
}

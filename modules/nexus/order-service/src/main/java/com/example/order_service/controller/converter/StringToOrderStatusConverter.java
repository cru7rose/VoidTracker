package com.example.order_service.controller.converter;

import com.example.danxils_commons.enums.OrderStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converter to handle String to OrderStatus enum conversion
 * for request parameters
 */
@Component
public class StringToOrderStatusConverter implements Converter<String, OrderStatus> {
    
    @Override
    public OrderStatus convert(String source) {
        try {
            return OrderStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid OrderStatus: " + source, e);
        }
    }
}

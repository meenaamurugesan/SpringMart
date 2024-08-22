package com.learning.order_service.dto;

import lombok.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    // Getter method for orderLineItemsDtoList
    private List<OrderLineItemsDto> orderLineItemsDtoList = new ArrayList<>();

    // Getter method for orderLineItemsDtoList
    public List<OrderLineItemsDto> getOrderLineItemsDtoList() {
        return orderLineItemsDtoList;
    }

}

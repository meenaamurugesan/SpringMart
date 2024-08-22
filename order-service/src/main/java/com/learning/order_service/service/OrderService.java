package com.learning.order_service.service;

import com.learning.order_service.dto.InventoryResponse;
import com.learning.order_service.dto.OrderLineItemsDto;
import com.learning.order_service.dto.OrderRequest;
import com.learning.order_service.model.Order;
import com.learning.order_service.model.OrderLineItems;
import com.learning.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){
        Order order= new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        System.out.println("OrderRequest: " + orderRequest);
        System.out.println("OrderLineItemsDtoList: " + orderRequest.getOrderLineItemsDtoList());
        if (orderRequest.getOrderLineItemsDtoList() == null || orderRequest.getOrderLineItemsDtoList().isEmpty()) {
            throw new IllegalArgumentException("OrderLineItemsDtoList cannot be null or empty");
        }

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        System.out.println("Mapped OrderLineItems: " + orderLineItems);

        order.setOrderLineItemsList(orderLineItems);
        System.out.println("OrderLineItemsList After Set: " + order.getOrderLineItemsList());

//        List<String> skuCodes= order.getOrderLineItemsList().stream()
//                .map(OrderLineItems::getSkuCode)
//                .toList();
        List<String> skuCodes = order.getOrderLineItemsList().stream()
                .map(item -> {
                    System.out.println("Extracted SKU Code: " + item.getSkuCode());
                    return item.getSkuCode();
                })
                .toList();
        System.out.println("SKU Codes: " + skuCodes);

        InventoryResponse[] inventoryResponsesArray= webClient.get()
                .uri("http://localhost:8084/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        System.out.println("Inventory Response: " + Arrays.toString(inventoryResponsesArray));

//        boolean allProductsInStock= Arrays.stream(inventoryResponsesArray)
//                .allMatch(InventoryResponse::isInStock);

        boolean allProductsInStock = Arrays.stream(inventoryResponsesArray)
                .allMatch(inventoryResponse -> {
                    // Find the requested quantity for the current SKU
                    int requestedQuantity = orderRequest.getOrderLineItemsDtoList()
                            .stream()
                            .filter(item -> item.getSkuCode().equals(inventoryResponse.getSkuCode()))
                            .findFirst()
                            .map(OrderLineItemsDto::getQuantity)
                            .orElse(0);

                    // Check if the available quantity is greater than or equal to the requested quantity
                    return inventoryResponse.isInStock() && inventoryResponse.getQuantity() >= requestedQuantity;
                });


        if (allProductsInStock){
        orderRepository.save(order);
        }
        else{
            throw new IllegalArgumentException("Product is not in Stock, Please try again later.");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }
}

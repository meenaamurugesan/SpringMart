package com.learning.inventory_service.service;

import com.learning.inventory_service.dto.InventoryResponse;
import com.learning.inventory_service.model.Inventory;
import com.learning.inventory_service.repository.InventoryRepository;
import com.learning.order_service.dto.OrderLineItemsDto;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly=true)
    public List<InventoryResponse> isInStock(List<String> skuCode){
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponse.builder()
                            .skuCode(inventory.getSkuCode())
                            .isInStock(inventory.getQuantity()>0)
                            .quantity(inventory.getQuantity())
                            .build()
                )
                .toList();
    }

}

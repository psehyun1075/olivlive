package com.olivelive.order.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olivelive.order.order.dto.CreateOrderRequest;
import com.olivelive.order.order.dto.OrderItemRequest;
import com.olivelive.order.order.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    void createOrder_buyerIdExceeds100Chars_returns400InvalidRequest() throws Exception {
        String longBuyerId = "x".repeat(101);
        var request = new CreateOrderRequest(
                longBuyerId,
                null,
                List.of(new OrderItemRequest("prod_01", "이어폰", 89000, 1))
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"));

        verify(orderService, never()).createOrder(any());
    }

    @Test
    void createOrder_buyerIdExactly100Chars_passesValidation() throws Exception {
        String exactBuyerId = "x".repeat(100);
        var request = new CreateOrderRequest(
                exactBuyerId,
                null,
                List.of(new OrderItemRequest("prod_01", "이어폰", 89000, 1))
        );

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(orderService).createOrder(any());
    }
}

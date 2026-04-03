package com.springmart.controller;

import com.springmart.dto.OrderRequest;
import com.springmart.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@WebMvcTest(
    controllers = {OrderController.class, GlobalExceptionHandler.class},
    excludeAutoConfiguration = {SecurityAutoConfiguration.class}
)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private com.springmart.security.JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("デッドロック発生時に409 Conflictと適切なメッセージが返されることを検証")
    @WithMockUser(username = "user1")
    void verifyDeadlockErrorHandling() throws Exception {
        // OrderService が呼ばれた際にデッドロック（ConcurrencyFailureException系）の例外を投げるようモック化
        OrderRequest request = new OrderRequest();
        com.springmart.dto.OrderItemRequest item = new com.springmart.dto.OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(1);
        request.setItems(Collections.singletonList(item));

        Mockito.when(orderService.createOrder(Mockito.any(OrderRequest.class)))
               .thenThrow(new PessimisticLockingFailureException("Deadlock detected"));

        ObjectMapper mapper = new ObjectMapper();

        // テストリクエストを送信して GlobalExceptionHandler の動きを確認
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
               .andExpect(status().isConflict()) // 409が返るはず
               .andExpect(jsonPath("$.error").value("同時アクセスエラー（デッドロックなど）"))
               .andExpect(jsonPath("$.message").value("システムが混み合っているため、処理を中断しました。しばらく経ってから再度お試しください。"));
    }
}

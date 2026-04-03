package com.springmart.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.springmart.controller.ProductController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class ProductControllerRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private com.springmart.service.ProductService productService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("ADMINロールで商品作成が成功することを検証")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanCreateProduct() throws Exception {
        // モックの戻り値は簡易的に空オブジェクトでOK
        when(productService.createProduct(org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.springmart.dto.ProductResponse());

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"テスト商品\",\"price\":1000}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("一般ユーザーが商品作成を試みたら403 Forbiddenになることを検証")
    @WithMockUser(username = "user", roles = {"USER"})
    void userCannotCreateProduct() throws Exception {
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"テスト商品\",\"price\":1000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMINロールで商品削除が成功することを検証")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void adminCanDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("一般ユーザーが商品削除を試みたら403になることを検証")
    @WithMockUser(username = "user", roles = {"USER"})
    void userCannotDeleteProduct() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }
}

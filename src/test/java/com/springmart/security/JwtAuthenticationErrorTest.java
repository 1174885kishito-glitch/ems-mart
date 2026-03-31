package com.springmart.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(controllers = {com.springmart.controller.OrderController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
class JwtAuthenticationErrorTest {

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.springmart.service.OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("不正あるいは適当なトークンでAPIにアクセスした場合に、401（Unauthorized）が返却されることを検証")
    void verifyInvalidTokenHandling() throws Exception {
        
        // でたらめなトークン（不正）
        String invalidToken = "Bearer eyJhbGciOiJIUzI1NiJ9.invalid.token";

        // 保護されているエンドポイントにアクセス
        // ※SecurityConfigで/api/などが保護対象になったため、認証情報なしor不正だと401になる
        mockMvc.perform(get("/api/products")
                .header("Authorization", invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized()) // 401 Unauthorized
               .andExpect(jsonPath("$.error").value("認証エラー"))
               .andExpect(jsonPath("$.message").value("無効または期限切れのトークンです。"));
    }

    @Test
    @DisplayName("トークンを一切指定せずにAPIへアクセスした場合にも401が返却されることを検証")
    void verifyNoTokenHandling() throws Exception {
        
        // Authorizationヘッダーなし
        mockMvc.perform(get("/api/products")
                .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized())
               .andExpect(jsonPath("$.error").value("認証エラー"))
               .andExpect(jsonPath("$.message").value("無効または期限切れのトークンです。"));
    }
}

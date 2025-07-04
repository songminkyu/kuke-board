package kuke.board.common.jwt.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kuke.board.common.jwt.payload.request.LoginRequest;
import kuke.board.common.jwt.payload.request.SignupRequest;

public class AuthControllerTest {

    private RestClient restClient;
    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    
    @BeforeEach
    void setUp() {
        // RestClient 초기화
        restClient = RestClient.create("http://localhost:8081");
        objectMapper = new ObjectMapper();
        
        // 로그인 요청 데이터 설정
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // 회원가입 요청 데이터 설정
        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser" + System.currentTimeMillis()); // 고유한 사용자 이름
        signupRequest.setEmail("newuser" + System.currentTimeMillis() + "@example.com"); // 고유한 이메일
        signupRequest.setPassword("password123");
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);
    }

    @Test
    void testRegisterUser() throws JsonProcessingException {
        // API 요청 실행
        String response = restClient.post()
                .uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(signupRequest)
                .retrieve()
                .body(String.class);
        
        // 응답 검증
        JsonNode jsonNode = objectMapper.readTree(response);
        assertEquals("User registered successfully!", jsonNode.get("message").asText());
    }
    
    @Test
    void testRegisterUserWithExistingUsername() throws JsonProcessingException {
        // 먼저 사용자 등록
        try {
            restClient.post()
                    .uri("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(signupRequest)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            // 이미 존재하는 사용자일 수 있으므로 무시
        }
        
        // 동일한 사용자 이름으로 다시 등록 시도
        try {
            restClient.post()
                    .uri("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(signupRequest)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            // 오류 응답 검증
            JsonNode jsonNode = objectMapper.readTree(e.getResponseBodyAsString());
            assertEquals("Error: Username is already taken!", jsonNode.get("message").asText());
        }
    }
    
    @Test
    void testAuthenticateUser() throws JsonProcessingException {
        // 테스트용 사용자 등록

        try {
            restClient.post()
                    .uri("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(signupRequest)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            // 이미 존재하는 사용자일 수 있으므로 무시
        }
        
        // 로그인 요청
        String response = restClient.post()
                .uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(signupRequest)
                .retrieve()
                .body(String.class);

        // 응답 검증
        JsonNode jsonNode = objectMapper.readTree(response);
        assertNotNull(jsonNode.get("accessToken").asText());
        assertEquals(signupRequest.getUsername(), jsonNode.get("username").asText());
        assertEquals(signupRequest.getEmail(), jsonNode.get("email").asText());
        assertTrue(jsonNode.get("roles").isArray());
    }
}
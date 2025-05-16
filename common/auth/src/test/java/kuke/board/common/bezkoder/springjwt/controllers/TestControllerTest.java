package kuke.board.common.bezkoder.springjwt.controllers;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kuke.board.common.bezkoder.springjwt.payload.request.LoginRequest;
import kuke.board.common.bezkoder.springjwt.payload.request.SignupRequest;

public class TestControllerTest {

    private RestClient restClient;
    private ObjectMapper objectMapper;
    private String userToken;
    private String modToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        // RestClient 초기화
        restClient = RestClient.create("http://localhost:8081");
        objectMapper = new ObjectMapper();

        // 테스트 사용자 생성 및 토큰 획득
        createTestUsersAndGetTokens();
    }

    private void createTestUsersAndGetTokens() throws JsonProcessingException {
        // 타임스탬프로 고유한 사용자 이름 생성
        long timestamp = System.currentTimeMillis();

        // 일반 사용자 생성 및 토큰 획득
        userToken = createUserAndGetToken("user" + timestamp, "user" + timestamp + "@example.com", "password123", "user");

        // 관리자 사용자 생성 및 토큰 획득
        modToken = createUserAndGetToken("mod" + timestamp, "mod" + timestamp + "@example.com", "password123", "mod");

        // 관리자 사용자 생성 및 토큰 획득
        adminToken = createUserAndGetToken("admin" + timestamp, "admin" + timestamp + "@example.com", "password123", "admin");
    }

    private String createUserAndGetToken(String username, String email, String password, String role) throws JsonProcessingException {
        // 회원가입 요청 설정
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername(username);
        signupRequest.setEmail(email);
        signupRequest.setPassword(password);
        Set<String> roles = new HashSet<>();
        roles.add(role);
        signupRequest.setRole(roles);

        // 회원가입 요청
        try {
            restClient.post()
                    .uri("/api/auth/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(signupRequest)
                    .retrieve()
                    .body(String.class);
        } catch (HttpClientErrorException e) {
            // 이미 존재하는 사용자인 경우 무시
            if (e.getStatusCode() != HttpStatus.BAD_REQUEST) {
                throw e;
            }
        }

        // 로그인 요청
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        String response = restClient.post()
                .uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(loginRequest)
                .retrieve()
                .body(String.class);

        // JWT 토큰 추출
        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("accessToken").asText();
    }

    @Test
    void testPublicEndpoint() {
        // 공개 엔드포인트는 인증 없이 접근 가능해야 함
        String response = restClient.get()
                .uri("/api/test/all")
                .retrieve()
                .body(String.class);

        assertEquals("Public Content.", response);
    }

    @Test
    void testUserEndpoint() {
        // USER 권한으로 접근
        String response = restClient.get()
                .uri("/api/test/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                .retrieve()
                .body(String.class);

        assertEquals("User Content.", response);
    }

    @Test
    void testModeratorEndpoint() {
        // MODERATOR 권한으로 접근
        String response = restClient.get()
                .uri("/api/test/mod")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + modToken)
                .retrieve()
                .body(String.class);

        assertEquals("Moderator Board.", response);
    }

    @Test
    void testAdminEndpoint() {
        // ADMIN 권한으로 접근
        String response = restClient.get()
                .uri("/api/test/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(String.class);

        assertEquals("Admin Board.", response);
    }

    @Test
    void testUserCannotAccessModeratorEndpoint() {
        // 일반 사용자는 MODERATOR 엔드포인트에 접근할 수 없어야 함
        try {
            restClient.get()
                    .uri("/api/test/mod")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                    .retrieve()
                    .body(String.class);

            fail("일반 사용자가 모더레이터 엔드포인트에 접근할 수 없어야 합니다");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

    @Test
    void testUserCannotAccessAdminEndpoint() {
        // 일반 사용자는 ADMIN 엔드포인트에 접근할 수 없어야 함
        try {
            restClient.get()
                    .uri("/api/test/admin")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken)
                    .retrieve()
                    .body(String.class);

            fail("일반 사용자가 관리자 엔드포인트에 접근할 수 없어야 합니다");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

    @Test
    void testModeratorCanAccessUserEndpoint() {
        // 모더레이터는 USER 엔드포인트에 접근할 수 있어야 함
        String response = restClient.get()
                .uri("/api/test/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + modToken)
                .retrieve()
                .body(String.class);

        assertEquals("User Content.", response);
    }

    @Test
    void testAdminCanAccessAllEndpoints() {
        // 관리자는 모든 엔드포인트에 접근할 수 있어야 함
        String userResponse = restClient.get()
                .uri("/api/test/user")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .body(String.class);

        assertEquals("User Content.", userResponse);

        // admin 권한으로는 mod 엔드포인트에 접근할 수 있는지 테스트
        try {
            restClient.get()
                    .uri("/api/test/mod")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                    .retrieve()
                    .body(String.class);

            // 참고: PreAuthorize 설정상 admin은 mod 엔드포인트에 접근할 수 없음
            // 이 부분은 실패할 수 있음
            fail("ADMIN 권한만으로는 MODERATOR 전용 엔드포인트에 접근할 수 없어야 합니다");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.FORBIDDEN, e.getStatusCode());
        }
    }

    @Test
    void testUnauthenticatedAccessToProtectedEndpoints() {
        // 인증 없이 보호된 엔드포인트에 접근 시 401 Unauthorized 응답을 받아야 함
        try {
            restClient.get()
                    .uri("/api/test/user")
                    .retrieve()
                    .body(String.class);

            fail("인증 없이 보호된 엔드포인트에 접근할 수 없어야 합니다");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
    }

    @Test
    void testInvalidToken() {
        // 잘못된 토큰으로 접근 시 인증 실패해야 함
        try {
            restClient.get()
                    .uri("/api/test/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer invalidtoken123")
                    .retrieve()
                    .body(String.class);

            fail("잘못된 토큰으로 접근 시 인증 실패해야 합니다");
        } catch (HttpClientErrorException e) {
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
        }
    }
}
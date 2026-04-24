package com.bankx.demo.admin;

import com.bankx.demo.common.constant.SuperConstant;
import com.bankx.demo.common.enums.RoleEnum;
import com.bankx.demo.security.Entity.Role;
import com.bankx.demo.security.dto.RegisterRequest;
import com.bankx.demo.security.repository.RoleRepository;
import com.bankx.demo.user.entity.User;
import com.bankx.demo.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("AdminController Integration Tests")
public class AdminControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private RoleRepository roleRepository;


    private String adminToken;
    private String customerToken;
    private UUID testUserId;
    private String adminEmail;
    private String customerEmail;
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() throws Exception {

        // Use unique emails per test to avoid Redis rate-limit keys and DB unique constraints leaking across tests
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        adminEmail = "admin_" + suffix + "@bankx.com";
        customerEmail = "customer_" + suffix + "@bankx.com";


        // 注册 + 登录拿 ADMIN token
        adminToken = registerAndLogin("admin_" + suffix, adminEmail, "Str0ngP@ss!");

        // 手动把 admin 升级为 ROLE_ADMIN
        User adminUser = userRepository.findByEmail(adminEmail).orElseThrow();

        upgradeToAdmin(adminUser);
        // 重新登录获取带 ADMIN 权限的 token
        adminToken = login(adminEmail, "Str0ngP@ss!");

        // 注册普通用户
        customerToken = registerAndLogin("customer_" + suffix, customerEmail, "Str0ngP@ss!");

        // 拿到普通用户的 ID
        testUserId = userRepository.findByEmail(customerEmail).orElseThrow().getId();
    }


    @AfterEach
    void tearDown() {
        if (adminEmail != null) {
            redisTemplate.delete(SuperConstant.REDIS_EMAIL_CODE_PREFIX + adminEmail);
        }
        if (customerEmail != null) {
            redisTemplate.delete(SuperConstant.REDIS_EMAIL_CODE_PREFIX + customerEmail);
        }
    }

    // ══════════════════════════════════════════════
    // GET /admin/users
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("GET /api/v1/admin/users")
    class GetAllUsers{

        @Test
        @DisplayName("✅ ADMIN 正常获取用户列表")
        void getAllUsers_asAdmin_success() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0000"))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("❌ 无 token 返回 401")
        void getAllUsers_noToken_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("❌ CUSTOMER token 返回 403")
        void getAllUsers_asCustomer_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                            .header("Authorization", "Bearer " + customerToken))
                    .andExpect(status().isForbidden());
        }
    }

    // ══════════════════════════════════════════════
    // GET /admin/users/{id}
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("GET /api/v1/admin/users/{id}")
    class GetUserById{

        @Test
        @DisplayName("✅ ADMIN 正常获取用户信息")
        void getUserById_asAdmin_success() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users/{id}", testUserId)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0000"))
                    .andExpect(jsonPath("$.data.id").value(testUserId.toString()));
        }

        @Test
        @DisplayName("❌ 无 token 获取用户信息返回 401")
        void getUserById_noToken_returns401() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users/{id}", testUserId))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("❌ 用户不存在返回 404")
        void getUserById_asCustomer_returns403() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users/{id}", UUID.randomUUID())
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }
    // ══════════════════════════════════════════════
    // PATCH /admin/users/{id}/freeze
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/freeze")
    class FreezeUser {

        @Test
        @DisplayName("✅ ADMIN 正常冻结用户")
        void freezeUser_asAdmin_success() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/users/{id}/freeze", testUserId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0000"))
                    .andExpect(jsonPath("$.data.userStatus").value("FROZEN"));
        }

        @Test
        @DisplayName("❌ 重复冻结返回 400")
        void freezeUser_alreadyFrozen_returns400() throws Exception {
            // 先冻结一次
            mockMvc.perform(patch("/api/v1/admin/users/{id}/freeze", testUserId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 再次冻结
            mockMvc.perform(patch("/api/v1/admin/users/{id}/freeze", testUserId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1000"));
        }

        @Test
        @DisplayName("❌ CUSTOMER token 返回 403")
        void freezeUser_asCustomer_returns403() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/users/{id}/freeze", testUserId)
                            .header("Authorization", "Bearer " + customerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("❌ 无 token 返回 401")
        void freezeUser_noToken_returns401() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/users/{id}/freeze", testUserId))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ══════════════════════════════════════════════
    // PATCH /admin/users/{id}/unfreeze
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/unfreeze")
    class UnfreezeUser {

        @Test
        @DisplayName("✅ ADMIN 正常解冻用户")
        void unfreezeUser_asAdmin_success() throws Exception {
            // 先冻结
            mockMvc.perform(patch("/api/v1/admin/users/{id}/freeze", testUserId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 再解冻
            mockMvc.perform(patch("/api/v1/admin/users/{id}/unfreeze", testUserId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.userStatus").value("ACTIVE"));
        }

        @Test
        @DisplayName("❌ 解冻 ACTIVE 用户返回 400")
        void unfreezeUser_notFrozen_returns400() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/users/{id}/unfreeze", testUserId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isBadRequest());
        }
    }

    // ══════════════════════════════════════════════
    // PATCH /admin/users/{id}/role
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("PATCH /api/v1/admin/users/{id}/role")
    class AssignRole {

        @Test
        @DisplayName("✅ ADMIN 正常分配角色")
        void assignRole_asAdmin_success() throws Exception {
            Map<String, String> body = Map.of("role", "ROLE_TELLER");

            mockMvc.perform(patch("/api/v1/admin/users/{id}/role", testUserId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0000"))
                    .andExpect(jsonPath("$.data.roles[0]")
                            .value("ROLE_TELLER"));
        }

        @Test
        @DisplayName("❌ 重复分配同一角色返回 409")
        void assignRole_duplicate_returns409() throws Exception {
            Map<String, String> body = Map.of("role", "ROLE_CUSTOMER");

            mockMvc.perform(patch("/api/v1/admin/users/{id}/role", testUserId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("1005"));
        }

        @Test
        @DisplayName("❌ role 字段为空返回 400")
        void assignRole_missingRole_returns400() throws Exception {
            Map<String, String> body = Map.of();

            mockMvc.perform(patch("/api/v1/admin/users/{id}/role", testUserId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("❌ CUSTOMER token 返回 403")
        void assignRole_asCustomer_returns403() throws Exception {
            Map<String, String> body = Map.of("role", "ROLE_TELLER");

            mockMvc.perform(patch("/api/v1/admin/users/{id}/role", testUserId)
                            .header("Authorization", "Bearer " + customerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(body)))
                    .andExpect(status().isForbidden());
        }
    }

    // ══════════════════════════════════════════════
    // DELETE /admin/users/{id}
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("DELETE /api/v1/admin/users/{id}")
    class DeleteUser {

        @Test
        @DisplayName("✅ ADMIN 正常软删除用户")
        void deleteUser_asAdmin_success() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/users/{id}/delete", testUserId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("0000"));

            // 验证数据库里 deleted = true
            User user = userRepository.findById(testUserId).orElseThrow();
            Assertions.assertTrue(user.isDeleted());
        }

        @Test
        @DisplayName("❌ 用户不存在返回 404")
        void deleteUser_notFound_returns404() throws Exception {
            mockMvc.perform(patch("/api/v1/admin/users/{id}/delete", UUID.randomUUID())
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("❌ CUSTOMER token 返回 403")
        void deleteUser_asCustomer_returns403() throws Exception {
            mockMvc.perform(delete("/api/v1/admin/users/{id}/delete", testUserId)
                            .header("Authorization", "Bearer " + customerToken))
                    .andExpect(status().isForbidden());
        }
    }


    // ══════════════════════════════════════════════
    // helpers
    // ══════════════════════════════════════════════

    /**
     * POST /api/v1/auth/login
     * Content-Type: application/json
     *
     * {
     *   "email": "...",
     *   "password": "..."
     * }
     * @param email
     * @param password
     * @return
     * @throws Exception
     */
    private String login(String email, String password) throws Exception{
        Map<String, String> loginBody = Map.of(
                "email", email,
                "password", password
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0000"))
                .andExpect(jsonPath("$.data.accessToken").isString())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response)
                .path("data").path("accessToken").asText();
    }

    /**
     * Perform register and login
     * @param username
     * @param email
     * @param password
     * @return
     * @throws Exception
     */
    private String registerAndLogin(String username, String email, String password) throws Exception{
        // Send email veri-code
        // Seed email verification code directly into Redis.
        // This test focuses on AdminController permissions, not the external email provider.
        String code = "123456";
        redisTemplate.opsForValue().set(
                SuperConstant.REDIS_EMAIL_CODE_PREFIX + email,
                code,
                5,
                TimeUnit.MINUTES
        );

        RegisterRequest payload = new RegisterRequest();
        payload.setUsername(username);
        payload.setEmail(email);
        payload.setCode(code);
        payload.setPassword(password);
        payload.setFirstName("Test_First");
        payload.setLastName("Test_Last");
        payload.setPhone("+11234567890");
        payload.setDateOfBirth(LocalDate.of(1990, 5, 15));
        payload.setAddressLine1("123 main St");
        payload.setCity("New York");
        payload.setZipCode("12345");
        payload.setCountry("US");
        payload.setState("NY");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        return login(email, password);
    }

    /**
     * Get email verification code from redis
     * @param email
     * @return
     */
    private String getCodeFromRedis(String email){

        String prefix = SuperConstant.REDIS_EMAIL_CODE_PREFIX + email;
        String code = redisTemplate.opsForValue().get(prefix);

        assertNotNull(code, "Email verification code should exist in Reids for key: " + prefix);

        return code;
    }

    /**
     * Upgrade user role to admin
     * @param user
     */
    private void upgradeToAdmin(User user){
        Role adminRole = roleRepository.findByName(RoleEnum.ROLE_ADMIN)
                        .orElseThrow();
        user.getUserRoles().forEach(ur -> ur.setRole(adminRole));

        userRepository.saveAndFlush(user);
    }
}

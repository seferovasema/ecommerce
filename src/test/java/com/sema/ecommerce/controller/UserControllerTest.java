package com.sema.ecommerce.controller;

import com.sema.ecommerce.dto.request.UserRequest;
import com.sema.ecommerce.dto.response.UserResponse;
import com.sema.ecommerce.exception.ResourceNotFoundException;
import com.sema.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = jakarta.servlet.Filter.class))
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final String VALID_BODY = """
            {
              "email": "sema@example.com",
              "password": "Password123",
              "firstName": "Sema",
              "lastName": "Aliyeva"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_shouldReturnCreated() throws Exception {
        when(userService.createUser(any(UserRequest.class)))
                .thenReturn(mock(UserResponse.class));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated());

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    void createUser_shouldReturnBadRequestWhenBodyEmpty() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_shouldReturnBadRequestWhenEmailInvalid() throws Exception {
        String body = """
                {
                  "email": "not-an-email",
                  "password": "Password123",
                  "firstName": "Sema",
                  "lastName": "Aliyeva"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any());
    }

    @Test
    void getUserById_shouldReturnOk() throws Exception {
        when(userService.getUserById(1L)).thenReturn(mock(UserResponse.class));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_shouldReturnNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new ResourceNotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldReturnOk() throws Exception {
        when(userService.updateUser(eq(1L), any(UserRequest.class)))
                .thenReturn(mock(UserResponse.class));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isOk());

        verify(userService).updateUser(eq(1L), any(UserRequest.class));
    }

    @Test
    void updateUser_shouldReturnBadRequestWhenBodyEmpty() throws Exception {
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateUser(any(), any());
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }
}
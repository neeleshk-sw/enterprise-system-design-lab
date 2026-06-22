package com.enterprise.basiccrud.controller;

import com.enterprise.basiccrud.dto.CustomerRequest;
import com.enterprise.basiccrud.dto.CustomerResponse;
import com.enterprise.basiccrud.exception.GlobalExceptionHandler;
import com.enterprise.basiccrud.exception.NotFoundException;
import com.enterprise.basiccrud.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService service;

    private CustomerResponse sample(Long id) {
        return new CustomerResponse(id, "Ada", "Lovelace", "ada@example.com", Instant.now(), Instant.now());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void createReturns201WithLocationAndPlainBody() throws Exception {
        when(service.create(any())).thenReturn(sample(1L));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Ada", "Lovelace", "ada@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/customers/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("ada@example.com"))
                // plain DTO — no response envelope
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.success").doesNotExist());
    }

    @Test
    void createWithInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("", "Lovelace", "not-an-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/customers"));
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(service.getById(1L)).thenReturn(sample(1L));

        mockMvc.perform(get("/api/v1/customers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        when(service.getById(99L)).thenThrow(new NotFoundException("Customer not found with id: 99"));

        mockMvc.perform(get("/api/v1/customers/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/api/v1/customers/99"));
    }

    @Test
    void listReturns200Array() throws Exception {
        when(service.list()).thenReturn(List.of(sample(1L)));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ada@example.com"));
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(sample(1L));

        mockMvc.perform(put("/api/v1/customers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Grace", "Hopper", "grace@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/{id}", 1))
                .andExpect(status().isNoContent());
    }
}

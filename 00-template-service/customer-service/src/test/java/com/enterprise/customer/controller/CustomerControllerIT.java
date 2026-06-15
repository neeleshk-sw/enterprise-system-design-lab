package com.enterprise.customer.controller;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.GlobalExceptionHandler;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.customer.common.CustomerErrorCodes;
import com.enterprise.customer.dto.CustomerRequest;
import com.enterprise.customer.dto.CustomerResponse;
import com.enterprise.customer.entity.CustomerStatus;
import com.enterprise.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    private CustomerResponse sampleResponse(Long id) {
        return new CustomerResponse(id, "Ada", "Lovelace", "ada@example.com",
                "+1-555-0100", CustomerStatus.ACTIVE, Instant.now(), Instant.now());
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void createReturns201AndSuccessWrapper() throws Exception {
        when(service.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Ada", "Lovelace", "ada@example.com", "+1-555-0100", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("ada@example.com"))
                .andExpect(jsonPath("$.message").value("Customer created successfully"));
    }

    @Test
    void createWithInvalidBodyReturns400ValidationError() throws Exception {
        // blank firstName + invalid email
        String invalid = json(new CustomerRequest("", "Lovelace", "not-an-email", null, null));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/api/v1/customers"));
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(service.getById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/api/v1/customers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        when(service.getById(99L)).thenThrow(
                new ResourceNotFoundException(CustomerErrorCodes.CUSTOMER_NOT_FOUND, "Customer not found with id: '99'"));

        mockMvc.perform(get("/api/v1/customers/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/v1/customers/99"));
    }

    @Test
    void listReturnsPagedWrapper() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        when(service.list(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sampleResponse(1L)), pageable, 1));

        mockMvc.perform(get("/api/v1/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].email").value("ada@example.com"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(put("/api/v1/customers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Grace", "Hopper", "ada@example.com", "+1-555-0100", CustomerStatus.INACTIVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer updated successfully"));
    }

    @Test
    void updateWithDuplicateEmailReturns409() throws Exception {
        when(service.update(eq(1L), any())).thenThrow(
                new BusinessException(CustomerErrorCodes.CUSTOMER_EMAIL_EXISTS, "Customer already exists with email: 'taken@example.com'"));

        mockMvc.perform(put("/api/v1/customers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Ada", "Lovelace", "taken@example.com", "+1-555-0100", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_EMAIL_EXISTS"));
    }

    @Test
    void deleteReturns200WithMessage() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer deleted successfully"));
    }
}

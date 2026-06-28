package com.enterprise.multimodule.app;

import com.enterprise.common.exception.BusinessException;
import com.enterprise.common.exception.GlobalExceptionHandler;
import com.enterprise.common.exception.ResourceNotFoundException;
import com.enterprise.multimodule.api.controller.CustomerController;
import com.enterprise.multimodule.api.dto.CustomerRequest;
import com.enterprise.multimodule.api.mapper.CustomerMapper;
import com.enterprise.multimodule.domain.common.CustomerErrorCodes;
import com.enterprise.multimodule.domain.entity.Customer;
import com.enterprise.multimodule.domain.entity.CustomerStatus;
import com.enterprise.multimodule.domain.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web-slice test. Lives in the app module (where the {@code @SpringBootApplication} is),
 * loads the controller from the api module, and mocks the domain service.
 */
@WebMvcTest(CustomerController.class)
@Import({GlobalExceptionHandler.class, CustomerMapper.class})
class CustomerControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService service;

    private Customer customer(Long id) {
        Customer c = new Customer();
        c.setId(id);
        c.setFirstName("Ada");
        c.setLastName("Lovelace");
        c.setEmail("ada@example.com");
        c.setStatus(CustomerStatus.ACTIVE);
        return c;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    @Test
    void createReturns201WithEnvelope() throws Exception {
        when(service.create(any(Customer.class))).thenReturn(customer(1L));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Ada", "Lovelace", "ada@example.com", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.email").value("ada@example.com"))
                .andExpect(jsonPath("$.message").value("Customer created successfully"));
    }

    @Test
    void createWithInvalidBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("", "Lovelace", "not-an-email", null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void getByIdReturns200() throws Exception {
        when(service.getById(1L)).thenReturn(customer(1L));

        mockMvc.perform(get("/api/v1/customers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void getByIdNotFoundReturns404() throws Exception {
        when(service.getById(99L)).thenThrow(
                new ResourceNotFoundException(CustomerErrorCodes.CUSTOMER_NOT_FOUND, "Customer not found with id: '99'"));

        mockMvc.perform(get("/api/v1/customers/{id}", 99))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_NOT_FOUND"));
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        when(service.create(any(Customer.class))).thenThrow(
                new BusinessException(CustomerErrorCodes.CUSTOMER_EMAIL_EXISTS, "Customer already exists with email: 'ada@example.com'"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Ada", "Lovelace", "ada@example.com", null))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("CUSTOMER_EMAIL_EXISTS"));
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(1L), any(Customer.class))).thenReturn(customer(1L));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/v1/customers/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CustomerRequest("Grace", "Hopper", "ada@example.com", CustomerStatus.INACTIVE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer updated successfully"));
    }

    @Test
    void deleteReturns200WithMessage() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Customer deleted successfully"));
    }
}

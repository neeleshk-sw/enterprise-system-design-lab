package com.enterprise.common.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void successWithDataAndMessage() {
        ApiResponse<String> response = ApiResponse.success("payload", "ok");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("payload");
        assertThat(response.message()).isEqualTo("ok");
    }

    @Test
    void successWithDataOnlyHasNullMessage() {
        ApiResponse<String> response = ApiResponse.success("payload");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isEqualTo("payload");
        assertThat(response.message()).isNull();
    }

    @Test
    void messageOnlyHasNullData() {
        ApiResponse<String> response = ApiResponse.message("deleted");

        assertThat(response.success()).isTrue();
        assertThat(response.data()).isNull();
        assertThat(response.message()).isEqualTo("deleted");
    }
}

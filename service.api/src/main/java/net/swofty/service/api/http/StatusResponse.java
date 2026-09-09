package net.swofty.service.api.http;

import lombok.Getter;

// The success/error status reported in an APIResponse.
public enum StatusResponse {
    SUCCESS("success"),
    ERROR("error");

    @Getter
    private final String status;

    StatusResponse(String status) {
        this.status = status;
    }
}
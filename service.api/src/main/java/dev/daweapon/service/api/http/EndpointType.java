package dev.daweapon.service.api.http;

// Distinguishes public-facing API endpoints from administrative ones.
public enum EndpointType {
    CONSUMER(),
    ADMINISTRATIVE()
}

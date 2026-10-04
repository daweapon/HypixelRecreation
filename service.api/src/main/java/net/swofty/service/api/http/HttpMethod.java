package net.swofty.service.api.http;

// HTTP verb an endpoint answers on. Endpoints that do not override it stay GET, as every
// pre-existing endpoint was.
public enum HttpMethod {
    GET,
    POST
}

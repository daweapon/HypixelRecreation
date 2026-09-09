package net.swofty.proxyapi.impl;

// Marker interface for events that can be serialized to a string and sent across the proxy boundary.
public interface ProxyUnderstandableEvent {
    String asProxyUnderstandable();
}

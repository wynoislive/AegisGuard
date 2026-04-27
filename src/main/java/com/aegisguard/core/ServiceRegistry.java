package com.aegisguard.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple service registry for dependency injection pattern.
 * All services are registered during startup and retrieved by type.
 */
public final class ServiceRegistry {

    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

    /**
     * Register a service instance.
     */
    public <T> void register(Class<T> type, T instance) {
        services.put(type, instance);
    }

    /**
     * Get a registered service by type.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {
        Object service = services.get(type);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + type.getSimpleName());
        }
        return (T) service;
    }

    /**
     * Check if a service is registered.
     */
    public boolean has(Class<?> type) {
        return services.containsKey(type);
    }

    /**
     * Unregister a service.
     */
    public void unregister(Class<?> type) {
        services.remove(type);
    }

    /**
     * Clear all registered services.
     */
    public void clear() {
        services.clear();
    }
}

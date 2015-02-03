package org.metaborg.sunshine.environment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class.getName());

    private static ServiceRegistry INSTANCE;

    private Injector injector;

    public static final ServiceRegistry INSTANCE() {
        if(INSTANCE == null) {
            INSTANCE = new ServiceRegistry();
        }
        return INSTANCE;
    }

    private ServiceRegistry() {

    }

    public <T> T getService(Class<T> clazz) {
        T service = injector.getInstance(clazz);
        logger.trace("Retrieved provider {} for service {}", clazz, service);
        return service;
    }

    public <T> T getService(TypeLiteral<T> type) {
        T service = injector.getInstance(Key.get(type));
        logger.trace("Retrieved provider {} for service {}", type, service);
        return service;
    }

    public void setInjector(Injector injector) {
        this.injector = injector;
    }
}

/**
 * 
 */
package org.metaborg.sunshine.environment;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Vlad Vergu <v.a.vergu add tudelft.nl>
 * 
 */
public class ServiceRegistry {

	private static final Logger logger = LogManager
			.getLogger(ServiceRegistry.class.getName());

	private final Map<Class<?>, Object> services = new HashMap<>();

	private static ServiceRegistry INSTANCE;

	public static final ServiceRegistry INSTANCE() {
		if (INSTANCE == null) {
			INSTANCE = new ServiceRegistry();
		}
		return INSTANCE;
	}

	private ServiceRegistry() {

	}

	public void reset() {
		logger.info("Resetting registered services");
		services.clear();
	}

	public void registerService(Class<?> clazz, Object service) {
		Object replaced = services.put(clazz, service);
		logger.info("Registered service {} to {} replacing {}", clazz,
				service, replaced);
	}

	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> clazz) {
		T service = (T) services.get(clazz);
		logger.trace("Retrieved provider {} for service {}", clazz, service);
		return service;
	}

}

package com.smartcampus.config;

import com.smartcampus.util.SeedData;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS application entry point.
 *
 * The base path /api/v1 is declared once in web.xml (url-pattern).
 * @ApplicationPath is intentionally omitted to avoid the prefix being
 * applied twice, which would produce /api/v1/api/v1/... when web.xml
 * is also present.
 */
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        packages("com.smartcampus");
        SeedData.load();
    }
}

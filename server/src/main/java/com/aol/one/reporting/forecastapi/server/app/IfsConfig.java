package com.aol.one.reporting.forecastapi.server.app;


import com.aol.one.reporting.forecastapi.server.util.WebPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class IfsConfig {
    private final static Logger LOG = LoggerFactory.getLogger(IfsConfig.class);

    private static IfsConfig config;
    private final Properties properties;
    private static String webInfDir;

    private static IfsCache cache = new IfsCache();

    public static IfsCache getCache() {
        return cache;
    }

    public static String getWebInfDir() {
        return webInfDir;
    }

    public static Properties config() throws IOException {
        if (config == null) {
            config = new IfsConfig();
        }
        return config.properties();
    }


    private IfsConfig() throws IOException {
        // load application properties for configuration
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        this.properties = new Properties();
        try (final InputStream in = classLoader.getResourceAsStream("forecast-api.properties")) {
            if (in == null) {
                throw new IOException(
                        "Configuration file forecast-api.properties not found in classpath");
            }
            this.properties.load(in);
        }

        // let system properties override what is set in the app's properties file.
        // It is a good
        // practice, plus it help with test scenarios.
        final Properties systemProperties = System.getProperties();
        for (final Object key : properties.keySet()) {
            if (systemProperties.containsKey(key))
                this.properties.setProperty(key.toString(), systemProperties.getProperty(key.toString()));
            System.out.println(key.toString() + " --> " + this.properties.getProperty(key.toString()).toString());
        }
        webInfDir = WebPath.getWebInfPath();
        LOG.debug("WebInfDir : " + webInfDir);

        cache = new IfsCache();
    }

    private Properties properties() {
        return this.properties;
    }


}

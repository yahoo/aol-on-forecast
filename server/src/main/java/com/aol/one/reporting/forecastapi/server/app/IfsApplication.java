/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.app;

import com.aol.one.reporting.forecastapi.server.metrics.MetricsContextListener;
import com.aol.one.reporting.forecastapi.server.resource.CannedSetResource;
import com.aol.one.reporting.forecastapi.server.resource.CollectionListResource;
import com.aol.one.reporting.forecastapi.server.resource.EasyForecastResource;
import com.aol.one.reporting.forecastapi.server.resource.HealthResource;
import com.aol.one.reporting.forecastapi.server.resource.ImpressionForecastResource;
import com.aol.one.reporting.forecastapi.server.resource.SelectionForecastResource;
import com.aol.one.reporting.forecastapi.server.resource.SimpleForecastResource;
import com.aol.one.reporting.forecastapi.server.resource.WelcomeResource;
import com.codahale.metrics.jersey2.InstrumentedResourceMethodApplicationListener;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.MutableParameter;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jersey.JerseyApiReader;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.reader.ClassReaders;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Properties;

/**
 * The Class ExampleApplication. The main entry point of the app that registers
 * JAX-RS resources and providers, and initializes other components.
 */
@ApplicationPath("/")
public class IfsApplication extends ResourceConfig {

    private final static Logger LOG = LoggerFactory.getLogger(IfsApplication.class);

    /**
     * Instantiates a new example resource config. This constructor is only for
     * use by unit tests, in real initialization Jersey invokes the nullary
     * constructor.
     *
     * @throws IOException Signals that an I/O exception has occurred.
     */

    @SuppressWarnings("unchecked")
    public IfsApplication() throws IOException {


        final Properties properties = IfsConfig.config();

        // to enable metrics annotations on resource methods
        register(new InstrumentedResourceMethodApplicationListener(MetricsContextListener.getMetrics()));

        // to enable returning domain level objects as JSON using Jackson
        register(new JacksonJaxbJsonProvider() {
            @Override
            public Object readFrom(Class<Object> type, Type genericType,
                                   Annotation[] annotations, MediaType mediaType,
                                   MultivaluedMap<String, String> httpHeaders,
                                   InputStream entityStream) throws IOException {

                try {
                    return super.readFrom(type, genericType, annotations,
                            mediaType, httpHeaders, entityStream);

                } catch (InvalidFormatException e) {
                    LOG.error("Error deserializng JSON entity", e);
                    throw new WebApplicationException(Response
                            .status(Response.Status.BAD_REQUEST)
                            .entity(Entity.entity(e.getMessage(),
                                    MediaType.TEXT_PLAIN)).build());
                }
            }
        });

        // register resources

        register(new EasyForecastResource());
        register(new SelectionForecastResource());
        register(new CannedSetResource());
        register(new CollectionListResource());
        register(new ImpressionForecastResource());
        register(new WelcomeResource());
        register(new HealthResource());
        register(new SimpleForecastResource());

        // register Swagger resources
        packages("com.wordnik.swagger.jersey.listing");
        // configure Swagger
        ApiInfo apiInfo = new ApiInfo("IFS REST API(TM)",
                "A set of API's to to forecast using Ingeniously developed Models ", "http://corp.aol.com/",
                "AOL Inc.", "Contact AOL For Licensing Terms",
                "http://corp.aol.com/");
        SwaggerConfig swaggerConfig = ConfigFactory.config();
        swaggerConfig.setApiVersion("1.0");

        ClassReaders.setReader(new JerseyApiReader() {
            @Override
            public Option<Parameter> processParamAnnotations(
                    MutableParameter mutable, Annotation[] paramAnnotations) {
                if (paramAnnotations == null) {
                    // tell Sonar to stop complaining about this
                    return super.processParamAnnotations(mutable,
                            paramAnnotations); // NOSONAR
                }

                return super.processParamAnnotations(mutable, paramAnnotations);
            }
        });

        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        swaggerConfig.setBasePath(properties
                .getProperty("swagger.api.basepath"));
        swaggerConfig.setApiInfo(apiInfo);
        // turn on bean validation errors being returned to client
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        // @ValidateOnExecution annotations on subclasses won't cause errors.
        property(
                ServerProperties.BV_DISABLE_VALIDATE_ON_EXECUTABLE_OVERRIDE_CHECK,
                true);

    }


}

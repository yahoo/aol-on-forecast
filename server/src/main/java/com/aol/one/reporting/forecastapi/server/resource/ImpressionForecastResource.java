/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/
package com.aol.one.reporting.forecastapi.server.resource;

import com.aol.one.reporting.forecastapi.server.model.request.ImpressionForecastRequest;
import com.aol.one.reporting.forecastapi.server.model.response.ForecastResponse;
import com.aol.one.reporting.forecastapi.server.service.ForecastService;
import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Class EasyForecastResource. A JAX-RS resource with API method to generate
 * forecast for a given time series with at least one value.
 */
@Path("/impression-forecast-service/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api(
        value = "Impression Forecast",
        description = "Produce an impression forecast given a historical impression time series and a list of canned set names",
        position = 5
)
public class ImpressionForecastResource {

    private static final Logger LOG = LoggerFactory.getLogger(ImpressionForecastResource.class);

    private static final String ACCEPT_HEADERS = "accept";
    @Context
    private HttpHeaders headers;
    @Context
    private HttpServletRequest httpServletRequest;


    /**
     * Generate easy forecast for give time series
     *
     * @return forecastResponse object
     */
    @POST
    @Path("/canned-set-competition-forecast")
    @Timed
    @ExceptionMetered
    @ApiOperation(value = "Impression Forecast",
            notes = "Produce an impression forecast given a historical impression time series and a list of canned set names",
            response = ForecastResponse.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Impression Forecast successful"),
            @ApiResponse(code = 404, message = "Failed to calculate forecast"),
            @ApiResponse(code = 500, message = "Internal server error due to encoding the data"),
            @ApiResponse(code = 400, message = "Bad request due to decoding the data"),
            @ApiResponse(code = 412, message = "Pre condition failed due to required data not found")})

    public Response generateForecast(
            @Valid @NotNull final ImpressionForecastRequest impressionForecastRequest) {
        long start = System.currentTimeMillis();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String request = null;
        try {
            request = ow.writeValueAsString(impressionForecastRequest);
        } catch (JsonProcessingException ex) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        ForecastResponse response = null;
        LOG.debug("Get a forecast for a given time series");
        String json = null;
        try {


            response = ForecastService.impressionForecast(impressionForecastRequest, start);
            json = ow.writeValueAsString(response);

            if (headers.getRequestHeaders().get(HttpHeaders.ACCEPT).contains(MediaType.APPLICATION_JSON)) {
                if (response != null) {
                    return Response.ok().entity(json).build();
                } else {
                    return Response.status(404).build();
                }
            }
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (IFSException ifsException) {
            Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            LOG.error("Failed to generate easy forecast Error : " + e.getMessage(), e);
            String message = e.getMessage();
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(message).type("text/plain").build();
        }
        return Response.ok().entity(json).build();

    }
}

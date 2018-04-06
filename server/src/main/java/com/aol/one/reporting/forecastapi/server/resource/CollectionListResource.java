/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.resource;


import com.aol.one.reporting.forecastapi.server.model.response.CollectionResponse;
import com.aol.one.reporting.forecastapi.server.service.ForecastService;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
        value = "Collection List",
        description = "List available canned set collections.",
        position = 4
)
public class CollectionListResource {

    /**
     * The Constant log.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CollectionListResource.class);

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
    @GET
    @Path("/canned-set-collection-list")
    @Timed
    @ExceptionMetered
    @ApiOperation(value = "Collection List",
            notes = "List available canned set collections.",
            responseContainer = "Array", response = CollectionResponse.class)
    @ApiResponses({
            @ApiResponse(code = 200, message = "Collection Name list successful"),
            @ApiResponse(code = 404, message = "Failed to List CannedSets"),
            @ApiResponse(code = 500, message = "Internal server error due to encoding the data"),
            @ApiResponse(code = 400, message = "Bad request due to decoding the data"),
            @ApiResponse(code = 412, message = "Pre condition failed due to required data not found")
    })

    public Response collectionList(@QueryParam("regex") String regex) {
        long start = System.currentTimeMillis();
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        CollectionResponse[] collectionResponse = null;


        LOG.debug("List of collection names and canned definitions");
        String json = null;
        try {

            collectionResponse = ForecastService.getCollectionCannedSets(regex);

            json = ow.writeValueAsString(collectionResponse);
            if (headers.getRequestHeaders().get(HttpHeaders.ACCEPT).contains(MediaType.APPLICATION_JSON)) {
                if (collectionResponse != null) {
                    return Response.ok().entity(json).build();
                } else {
                    return Response.status(404).build();
                }
            }
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            LOG.error("Failed to generate forecast selected canned sets Error : " + e.getMessage(), e);
            String message = e.getMessage();
            return Response.status(Response.Status.PRECONDITION_FAILED).entity(message).type("text/plain").build();
        }
        return Response.ok().entity(json).build();
    }

}

/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import java.util.Arrays;

@ApiModel(value = "Collection Set Response")
public class CollectionResponse implements Comparable<CollectionResponse> {

    @ApiModelProperty(value = "Name of the collection")
    private String collectionName;

    @ApiModelProperty(value = "Array of Canned Sets", dataType = "CannedSetResponse[]")
    private CannedSetResponse[] cannedSets;


    public CollectionResponse() {

    }

    public CollectionResponse(
            @JsonProperty("collectionName") String collectionName,
            @JsonProperty("cannedSets") CannedSetResponse[] cannedSets

    ) {
        this.collectionName = collectionName;
        this.cannedSets = cannedSets;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public CannedSetResponse[] getCannedSets() {
        return cannedSets;
    }

    public void setCannedSets(CannedSetResponse[] cannedSets) {
        this.cannedSets = cannedSets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass())
            return false;

        CollectionResponse that = (CollectionResponse) o;

        if (!Arrays.equals(cannedSets, that.cannedSets))
            return false;
        if (!collectionName.equals(that.collectionName))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionName.hashCode();
        result = 31 * result + Arrays.hashCode(cannedSets);
        return result;
    }

    @Override
    public int compareTo(CollectionResponse o) {
        return collectionName.compareTo(o.collectionName);
    }
}

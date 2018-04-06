/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Canned Set name and Description")
public class CannedSetResponse implements Comparable<CannedSetResponse> {

    @ApiModelProperty(value = "Name of canned set")
    private String cannedSetName;

    @ApiModelProperty(value = "Description of the canned set")
    private String description;


    public CannedSetResponse() {

    }


    public CannedSetResponse(
            @JsonProperty("cannedSetName") String cannedSetName,
            @JsonProperty("description") String description
    ) {
        this.cannedSetName = cannedSetName;
        this.description = description;
    }

    public String getCannedSetName() {
        return cannedSetName;
    }


    public void setCannedSetName(String cannedSetName) {
        this.cannedSetName = cannedSetName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CannedSetResponse [ cannedSetName : ").append(cannedSetName);
        sb.append(" description :").append(description);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CannedSetResponse that = (CannedSetResponse) o;

        if (cannedSetName != null ? !cannedSetName.equals(that.cannedSetName) : that.cannedSetName != null)
            return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cannedSetName != null ? cannedSetName.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(CannedSetResponse o) {
        return cannedSetName.compareTo(o.cannedSetName);
    }
}

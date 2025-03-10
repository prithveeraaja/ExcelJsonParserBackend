package com.parser.exceljson.model;

import com.fasterxml.jackson.databind.JsonNode;

public class ConversionResponse {
    private JsonNode json;
    private JsonNode schema;
    private String error;

    public ConversionResponse(JsonNode json, JsonNode schema, String error) {
        this.json = json;
        this.schema = schema;
        this.error = error;
    }

    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {
        this.json = json;
    }

    public JsonNode getSchema() {
        return schema;
    }

    public void setSchema(JsonNode schema) {
        this.schema = schema;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

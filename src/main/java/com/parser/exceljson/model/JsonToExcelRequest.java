package com.parser.exceljson.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.List;

public class JsonToExcelRequest {
    private JsonNode json;
    private Map<String, List<String>> format;

    public JsonNode getJson() {
        return json;
    }

    public void setJson(JsonNode json) {
        this.json = json;
    }

    public Map<String, List<String>> getFormat() {
        return format;
    }

    public void setFormat(Map<String, List<String>> format) {
        this.format = format;
    }
}

package com.nexon.cloud;

import java.util.HashMap;
import java.util.Map;

public class CacheEntity {

    private String content;

    private Map<String,String> headers;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}

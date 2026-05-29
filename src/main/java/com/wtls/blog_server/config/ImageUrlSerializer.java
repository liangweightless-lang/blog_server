package com.wtls.blog_server.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class ImageUrlSerializer extends JsonSerializer<String> {

    @Value("${app.file.base-url}")
    private String baseUrl;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && baseUrl != null && !baseUrl.isEmpty()) {
            if (value.startsWith("/uploads/")) {
                gen.writeString(baseUrl + value);
                return;
            }
            if (value.contains("/uploads/")) {
                // 替换富文本 HTML 或 JSON 数组中的相对路径
                String replaced = value.replaceAll("([\"'])(/uploads/)", "$1" + baseUrl + "/uploads/");
                gen.writeString(replaced);
                return;
            }
        }
        gen.writeString(value);
    }
    
    @Override
    public Class<String> handledType() {
        return String.class;
    }
}

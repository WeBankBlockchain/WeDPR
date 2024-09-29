package com.webank.wedpr.core.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public class Json2StringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext cxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        return node.toString(); // Convert the node to a string
    }
}

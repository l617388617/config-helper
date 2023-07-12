package com.github.config.helper.component.json;

import static com.fasterxml.jackson.databind.node.JsonNodeType.STRING;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.diagnostic.Logger;

/**
 * JacksonConverter
 *
 * @author lupeng
 * Created on 2022-04-16
 */
public class JacksonConverter {

    private static final Logger logger = Logger.getInstance(JacksonConverter.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        //json中可以有注释
        MAPPER.enable(JsonParser.Feature.ALLOW_COMMENTS);
        //重复检测
        MAPPER.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        //宽松的json序列化，java类中字段可以和json不完全匹配
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private Set<String> convertedJsonNodes = new HashSet<>();

    public JsonNode decode(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            String path = "$";
            recursiveParser(root, path, false);
            return root;
        } catch (JsonProcessingException e) {
            logger.error("decodeError", e);
            return null;
        }
    }

    public JsonNode encode(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            String path = "$";
            recursiveParser(root, path, true);
            return root;
        } catch (JsonProcessingException e) {
            logger.error("decodeError", e);
            return null;
        }
    }

    private void recursiveParser(JsonNode node, String path, boolean isEncode) {
        // 获取jackson的Node节点类型
        JsonNodeType nodeType = node.getNodeType();
        switch (nodeType) {
            case OBJECT:
                ObjectNode objNode = (ObjectNode) node;
                Iterator<Map.Entry<String, JsonNode>> iterator = objNode.fields();
                while (iterator.hasNext()) {
                    Map.Entry<String, JsonNode> entry = iterator.next();
                    String key = entry.getKey();
                    JsonNode value = entry.getValue();
                    String currPath = path + "." + key;
                    if (isEncode && convertedJsonNodes.contains(currPath)) {
                        recursiveParser(value, currPath, true);
                        ((ObjectNode) node).put(key, value.toString());
                        continue;
                    }
                    Pair<Boolean, JsonNode> result = tryDecodeJsonNode(value);
                    if (result.getLeft()) {
                        convertedJsonNodes.add(currPath);
                        JsonNode decodeJsonNode = result.getRight();
                        objNode.put(key, decodeJsonNode);
                        value = decodeJsonNode;
                    }
                    recursiveParser(value, currPath, isEncode);
                }
                break;
            case ARRAY:
                ArrayNode arrayNode = (ArrayNode) node;
                for (int i = 0; i < arrayNode.size(); i++) {
                    JsonNode value = arrayNode.get(i);
                    String currPath = path + "[" + i + "]";
                    if (isEncode && convertedJsonNodes.contains(currPath)) {
                        recursiveParser(value, currPath, true);
                        arrayNode.insert(i, value.toString());
                        continue;
                    }
                    Pair<Boolean, JsonNode> result = tryDecodeJsonNode(value);
                    if (result.getLeft()) {
                        convertedJsonNodes.add(currPath);
                        JsonNode decodeJsonNode = result.getRight();
                        ((ArrayNode) node).insert(i, decodeJsonNode);
                        value = decodeJsonNode;
                    }
                    recursiveParser(value, currPath, isEncode);
                }
                break;
        }
    }

    private Pair<Boolean, JsonNode> tryDecodeJsonNode(JsonNode jsonNode) {
        if (jsonNode.getNodeType() != STRING) {
            return Pair.of(false, null);
        }
        try {
            JsonNode node = MAPPER.readTree(jsonNode.asText());
            return Pair.of(true, node);
        } catch (JsonProcessingException e) {
            return Pair.of(false, null);
        }
    }

}

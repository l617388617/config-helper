package com.github.config.helper.component.json;

import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

/**
 * JacksonUtil
 *
 * @author lupeng
 * Created on 2022-04-11
 */
public class JacksonUtil {
    private JacksonUtil() {
        throw new UnsupportedOperationException();
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter();

    static {
        //json中可以有注释
        MAPPER.enable(JsonParser.Feature.ALLOW_COMMENTS);
        //重复检测
        MAPPER.enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        //宽松的json序列化，java类中字段可以和json不完全匹配
        MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        //格式化相关
        DefaultIndenter indenter = new DefaultIndenter("  ", DefaultIndenter.SYS_LF);
        PRETTY_PRINTER.indentArraysWith(indenter);
        PRETTY_PRINTER.indentArraysWith(indenter);
    }

    /**
     * 对象 转换成json string
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 格式化之后的json string
     */
    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writer(PRETTY_PRINTER).writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 反序列化json
     * @return 入参的类型泛型实例化对象
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return MAPPER.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 反序列化json
     * @return JavaType对象的类型实例
     */
    public static <T> T fromJson(String json, JavaType javaType) {
        try {
            return MAPPER.readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * 反序列化json，转换成集合，集合中的元素为泛型对象
     * @return JavaType对象的类型实例
     */
    public static <E, T extends Collection<E>> T ofJsonCollection(String json,
            Class<? extends Collection> CollectionType, Class<E> itemType) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructCollectionType(CollectionType, itemType));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反序列化json，转换成Map
     * @return JavaType对象的类型实例
     */
    public static <K, V, T extends Map<K, V>> T ofJsonMap(String json,
            Class<? extends Map> mapType, Class<K> keyType, Class<V> valueType) {
        try {
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructMapType(mapType, keyType, valueType));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, V, T extends Map<K, Set<V>>> T ofSetMap(String json,
            Class<? extends Map> mapType, Class<K> keyType, Class<V> subValueType) {
        try {
            JavaType javaKeyType = MAPPER.getTypeFactory().constructType(keyType);
            CollectionType valueType = MAPPER.getTypeFactory().constructCollectionType(Set.class, subValueType);
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructMapType(mapType, javaKeyType, valueType));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <K, SUB_KEY, SUB_VALUE, T extends Map<K, Map<SUB_KEY, SUB_VALUE>>> T ofMapMap(String json,
            Class<? extends Map> mapType, Class<K> keyType, Class<SUB_KEY> subKeyType, Class<SUB_VALUE> subValueType) {
        try {
            JavaType javaKeyType = MAPPER.getTypeFactory().constructType(keyType);
            MapType subMapType = MAPPER.getTypeFactory().constructMapType(mapType, subKeyType, subValueType);
            return MAPPER.readValue(json, MAPPER.getTypeFactory().constructMapType(mapType, javaKeyType, subMapType));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> fromJson(String json) {
        return ofJsonMap(json, Map.class, String.class, Object.class);
    }


}

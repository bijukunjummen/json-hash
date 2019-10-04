package org.bk.json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonHashUtilTest {

    @Test
    void testHashingOfAJsonNode() {
        final ObjectNode childNode = JsonNodeFactory.instance.objectNode()
                .put("child-key2", "child-value2")
                .put("child-key1", "child-value1")
                .put("child-key3", 123.23f);

        final ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode()
                .add("arr-value1")
                .add("arr-value2");

        final ObjectNode jsonNode = JsonNodeFactory
                .instance
                .objectNode()
                .put("key1", "value1");

        jsonNode.set("key3", arrayNode);
        jsonNode.set("key2", childNode);

        final String calculatedHash = sha256Hex(
                sha256Hex("key1") + sha256Hex("value1")
                        + sha256Hex("key2") + sha256Hex(
                        sha256Hex("child-key1") + sha256Hex("child-value1")
                                + sha256Hex("child-key2") + sha256Hex("child-value2")
                                + sha256Hex("child-key3") + sha256Hex("123.23")
                )
                        + sha256Hex("key3") + sha256Hex(
                        sha256Hex("arr-value1")
                                + sha256Hex("arr-value2")
                )
        );

        assertThat(JsonHashUtil.generateHash(jsonNode)).isEqualTo(calculatedHash);
    }

    @Test
    void testHashingOfAJsonNodeWithIgnoredPatterns() {
        final ObjectNode childNode = JsonNodeFactory.instance.objectNode()
                .put("child-key2", "child-value2")
                .put("child-key1", "child-value1")
                .put("child-key3", 123.23f);

        final ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode()
                .add("arr-value1")
                .add("arr-value2");

        final ObjectNode jsonNode = JsonNodeFactory
                .instance
                .objectNode()
                .put("key1", "value1");

        jsonNode.set("key3", arrayNode);
        jsonNode.set("key2", childNode);

        final String calculatedHash = sha256Hex(
                sha256Hex("key1") + sha256Hex("value1")
                        + sha256Hex("key2") + sha256Hex(
                        sha256Hex("child-key1") + sha256Hex("child-value1")
                                + sha256Hex("child-key2") + sha256Hex("child-value2")
                                + sha256Hex("child-key3") + sha256Hex("123.23")
                )
        );

        assertThat(JsonHashUtil.generateHash(jsonNode, Set.of("key[3]"))).isEqualTo(calculatedHash);
    }
}

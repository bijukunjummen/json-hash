package org.bk.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

public class JsonHashUtil {
    private JsonHashUtil() {

    }

    public static String generateHash(JsonNode jsonNode) {
        return generateHash(jsonNode, Set.of());
    }

    public static String generateHash(JsonNode jsonNode, Set<String> fieldNamePatternsToIgnore) {
        return traverseTreeAndGenerateHash(jsonNode,
                fieldNamePatternsToIgnore
                        .stream()
                        .collect(Collectors.joining("|")));
    }

    private static String traverseTreeAndGenerateHash(JsonNode jsonNode, String fieldNamePatternsToIgnore) {
        if (jsonNode.isObject()) {
            return traverseObjectAndGenerateHash((ObjectNode) jsonNode, fieldNamePatternsToIgnore);
        } else if (jsonNode.isArray()) {
            return traverseArrayAndGenerateHash((ArrayNode) jsonNode, fieldNamePatternsToIgnore);
        } else {
            return sha256Hex(jsonNode.asText());
        }
    }

    private static String traverseArrayAndGenerateHash(ArrayNode arrayNode, String fieldNamePatternsToIgnore) {
        final Iterable<JsonNode> iterable = () -> arrayNode.iterator();
        final String concatenatedHash = StreamSupport.stream(iterable.spliterator(), false)
                .map(node -> traverseTreeAndGenerateHash(node, fieldNamePatternsToIgnore))
                .collect(Collectors.joining());
        return sha256Hex(concatenatedHash);
    }

    private static String traverseObjectAndGenerateHash(ObjectNode objectNode, String fieldNamePatternsToIgnore) {
        final Iterable<String> fieldNamesIterable = () -> objectNode.fieldNames();
        final String concatenated = StreamSupport.stream(fieldNamesIterable.spliterator(), false)
                .sorted() //sorts the field names to make the traversal order deterministic
                .filter(fieldName -> !matchesIgnoredFieldNamesPattern(fieldName, fieldNamePatternsToIgnore))
                .map(fieldName -> sha256Hex(fieldName) +
                        traverseTreeAndGenerateHash(objectNode.get(fieldName), fieldNamePatternsToIgnore)
                )
                .collect(Collectors.joining());

        return sha256Hex(concatenated);
    }

    private static boolean matchesIgnoredFieldNamesPattern(String fieldName, String fieldNamePatternsToIgnore) {
        return fieldName.matches(fieldNamePatternsToIgnore);
    }
}

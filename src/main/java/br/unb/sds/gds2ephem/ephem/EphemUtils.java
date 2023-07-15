package br.unb.sds.gds2ephem.ephem;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class EphemUtils {
    private EphemUtils() {
    }

    public static List<CustomTuple<String, String>> converterPythonStringTupleEmList(final String input) {
        String listString = input.substring(3, input.length() - 3);
        String[] elements = listString.split("\\), \\(");
        List<CustomTuple<String, String>> tuples = new ArrayList<>();
        for (String element : elements) {
            String[] tupleElements = element.split("', '");
            String key = tupleElements[0];
            String value = tupleElements[1];
            tuples.add(new CustomTuple<>(key, value));
        }

        return tuples;
    }

    @Data
    public static class CustomTuple<K, V> {
        private K key;
        private V value;

        public CustomTuple(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}

package br.unb.sds.gds2ephem.ephem;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class EphemUtils {
    private EphemUtils() {
    }

    public static List<Selection<String, String>> converterPythonStringTupleEmList(final String input) {
        String listString = input.substring(3, input.length() - 3);
        String[] elements = listString.split("\\), \\(");
        List<Selection<String, String>> tuples = new ArrayList<>();
        for (String element : elements) {
            String[] tupleElements = element.split("', '");
            String key = tupleElements[0];
            String value = tupleElements[1];
            tuples.add(new Selection<>(key, value));
        }

        return tuples;
    }

    @Data
    public static class Selection<K, V> {
        private K key;
        private V value;

        public Selection(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }
}

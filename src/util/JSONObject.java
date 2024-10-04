package util;

import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JSONObject implements Serializable {
    private final Map<String, String> hashmap = new HashMap<String, String>();

    public JSONObject(String input) throws ParseException {
        try {
            if (input.contains("{")) {
                parseJSON(input);
            } else {
                parseList(input);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void parseJSON(String input) throws ParseException {
        try {
            Pattern pattern = Pattern.compile("\"(.*?)\"\\s*:\\s*(\".*?\"|[-+]?[0-9]*\\.?[0-9]+)");
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(2).replaceAll("\"", "");
                hashmap.put(key, value);
            }
        } catch (PatternSyntaxException e) {
            System.err.println("JSON syntax error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("JSON syntax error: " + e.getMessage());
            throw e;
        }
    }

    private void parseList(String input) throws ParseException {
        try {
            String[] keys = input.split("\n");
            for (String key : keys) {
                String[] values = key.split(":");
                hashmap.put(values[0].trim(), values[1].trim());
            }
        } catch (Exception e) {
            System.err.println("JSON syntax error: " + e.getMessage());
            throw e;
        }
    }

    public void put(String key, String value) {
        hashmap.put(key, value);
    }

    public String get(String key) {
        return hashmap.get(key);
    }

    public String JSONtoString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        for (Map.Entry<String, String> entry : hashmap.entrySet()) {
            sb.append("   ");
            sb.append('"').append(entry.getKey()).append('"').append(':');

            if (entry.getValue().matches("-?\\d+(\\.\\d+)?")) {
                sb.append(entry.getValue());
            } else {
                sb.append('"').append(entry.getValue()).append('"');
            }

            sb.append('\n');
        }

        sb.delete(sb.length() - 2, sb.length());
        sb.append("\n}");
        return sb.toString();
    }

    public String ListToString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : hashmap.entrySet()) {
            sb.append(entry.getKey()).append(':').append(entry.getValue()).append('\n');
        }
        return sb.toString().trim();
    }


}

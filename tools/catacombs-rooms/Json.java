import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A minimal JSON reader: objects become maps, arrays lists, numbers doubles. */
final class Json {
    private final String text;
    private int at;

    private Json(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        Json json = new Json(text);
        Object value = json.value();
        json.space();
        if (json.at != text.length()) throw json.error("trailing text");
        return value;
    }

    private Object value() {
        space();
        char c = text.charAt(at);
        if (c == '{') return object();
        if (c == '[') return array();
        if (c == '"') return string();
        if (text.startsWith("true", at)) {
            at += 4;
            return Boolean.TRUE;
        }
        if (text.startsWith("false", at)) {
            at += 5;
            return Boolean.FALSE;
        }
        if (text.startsWith("null", at)) {
            at += 4;
            return null;
        }
        int start = at;
        while (at < text.length() && "+-0123456789.eE".indexOf(text.charAt(at)) >= 0) at++;
        if (start == at) throw error("unexpected '" + c + "'");
        return Double.parseDouble(text.substring(start, at));
    }

    private Map<String, Object> object() {
        Map<String, Object> map = new LinkedHashMap<>();
        at++;
        space();
        if (text.charAt(at) == '}') {
            at++;
            return map;
        }
        while (true) {
            space();
            String key = string();
            space();
            expect(':');
            map.put(key, value());
            space();
            if (text.charAt(at) == ',') {
                at++;
                continue;
            }
            expect('}');
            return map;
        }
    }

    private List<Object> array() {
        List<Object> list = new ArrayList<>();
        at++;
        space();
        if (text.charAt(at) == ']') {
            at++;
            return list;
        }
        while (true) {
            list.add(value());
            space();
            if (text.charAt(at) == ',') {
                at++;
                continue;
            }
            expect(']');
            return list;
        }
    }

    private String string() {
        expect('"');
        StringBuilder out = new StringBuilder();
        while (true) {
            char c = text.charAt(at++);
            if (c == '"') return out.toString();
            if (c != '\\') {
                out.append(c);
                continue;
            }
            char escape = text.charAt(at++);
            switch (escape) {
                case 'n' -> out.append('\n');
                case 't' -> out.append('\t');
                case 'r' -> out.append('\r');
                case 'b' -> out.append('\b');
                case 'f' -> out.append('\f');
                case 'u' -> {
                    out.append((char) Integer.parseInt(text.substring(at, at + 4), 16));
                    at += 4;
                }
                default -> out.append(escape);
            }
        }
    }

    private void expect(char c) {
        if (text.charAt(at) != c) throw error("expected '" + c + "'");
        at++;
    }

    private void space() {
        while (at < text.length() && Character.isWhitespace(text.charAt(at))) at++;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at " + at);
    }
}

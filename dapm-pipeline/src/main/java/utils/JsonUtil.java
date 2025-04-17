package utils;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class JsonUtil {

    public static String encode(String str) {
        return URLEncoder.encode(str, StandardCharsets.UTF_8);
    }

    public static String decode(String str) {
        return URLDecoder.decode(str, StandardCharsets.UTF_8);
    }
}

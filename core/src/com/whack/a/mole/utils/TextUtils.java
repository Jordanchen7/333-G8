package com.whack.a.mole.utils;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.whack.a.mole.component.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    public static JsonValue text2json(String jsonString) {
        return new JsonReader().parse(jsonString);
    }

    public static String json2text(JsonValue jsonValue) {
        return jsonValue.toJson(JsonWriter.OutputType.json);
    }

    public static boolean isEmpty(String text) {
        if (text == null || text.isEmpty()) {
            return true;
        }

        return false;
    }

    public static boolean isValidIPAddress(String ipAddress) {
        if (TextUtils.isEmpty(ipAddress)) {
            return false;
        }

        // IP地址的正则表达式模式
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

        // 使用正则表达式匹配IP地址
        Pattern pattern = Pattern.compile(ipPattern);
        Matcher matcher = pattern.matcher(ipAddress);

        return matcher.matches();
    }
}

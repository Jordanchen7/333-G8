package com.whack.a.mole.data;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.whack.a.mole.bridge.SystemCapability;
import com.whack.a.mole.utils.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreManager {
    private static final String SAVE_KEY = "rank_list";
    private SystemCapability systemCapability;

    // format: {"cc": 100, "zz": 322}
    public static class RankItem {
        public String name;
        public int score;

        public RankItem(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }


    public ScoreManager(SystemCapability systemCapability) {
        this.systemCapability = systemCapability;
    }

    public List<RankItem> getTopList() {
        List<RankItem> items;
        String rankStr = systemCapability.readValue(SAVE_KEY);
        systemCapability.log("zc", "getTopList:" + rankStr);

        Map<String, Integer> scoreMap = jsonStringToMap(rankStr);
        items = mapToListAndSortByScore(scoreMap);
        return items;
    }

    public void saveScore(String name, int score) {
        String rankStr = systemCapability.readValue(SAVE_KEY);
        Map<String, Integer> scoreMap = jsonStringToMap(rankStr);
        scoreMap.put(name, score);

        // save to disk
        rankStr = mapToJsonString(scoreMap);
        systemCapability.writeValue(SAVE_KEY, rankStr);

        systemCapability.log("zc", "saveScore:" + rankStr);
    }

    public static Map<String, Integer> jsonStringToMap(String jsonString) {
        // 创建一个空的Map来存储结果，值为整数
        Map<String, Integer> map = new HashMap<>();

        try {
            // 使用LibGDX的JsonReader解析JSON字符串
            JsonValue jsonValue = new JsonReader().parse(jsonString);

            // 遍历JSON对象的属性并将其键值对添加到Map中，将值解析为整数
            for (JsonValue entry = jsonValue.child; entry != null; entry = entry.next) {
                String key = entry.name;
                int value = entry.asInt();
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public List<RankItem> mapToListAndSortByScore(Map<String, Integer> map) {
        List<RankItem> list = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String name = entry.getKey();
            int score = entry.getValue();
            RankItem rankItem = new RankItem(name, score);
            list.add(rankItem);
        }

        Collections.sort(list, new Comparator<RankItem>() {
            @Override
            public int compare(RankItem item1, RankItem item2) {
                return Integer.compare(item2.score, item1.score);
            }
        });

        return list;
    }

    public static String mapToJsonString(Map<String, Integer> map) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            json.addChild(key, new JsonValue(value));
        }

        // 创建Json实例
        return TextUtils.json2text(json);
    }

}

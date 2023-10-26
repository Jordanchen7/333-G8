package com.whack.a.mole.mode;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class BasicTypeExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        // 排除非基本数据类型字段
        Class<?> fieldType = f.getDeclaredClass();
        return !fieldType.isPrimitive() && !fieldType.equals(String.class);
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
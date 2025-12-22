package com.example.timeflow.utils;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Converters {
    // Boolean List 转换器
    @TypeConverter
    public static List<Boolean> fromStringToBooleanList(String value) {
        List<Boolean> list = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            String[] parts = value.split(",");
            for (String part : parts) {
                list.add(Boolean.parseBoolean(part));
            }
        }
        return list;
    }

    @TypeConverter
    public static String fromBooleanListToString(List<Boolean> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    // Integer List 转换器
    @TypeConverter
    public static List<Integer> fromStringToIntegerList(String value) {
        List<Integer> list = new ArrayList<>();
        if (value != null && !value.isEmpty()) {
            String[] parts = value.split(",");
            for (String part : parts) {
                try {
                    list.add(Integer.parseInt(part.trim()));
                } catch (NumberFormatException e) {
                    // 忽略无效数字
                }
            }
        }
        return list;
    }

    @TypeConverter
    public static String fromIntegerListToString(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i < list.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    // Date 转换器
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
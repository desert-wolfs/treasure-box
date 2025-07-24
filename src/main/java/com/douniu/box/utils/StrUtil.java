package com.douniu.box.utils;

public class StrUtil {

    /**
     * 将字符串转换为小驼峰格式
     * @param input 输入的字符串，支持下划线分隔的形式
     * @return 转换后的小驼峰格式字符串
     */
    public static String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '-' || c == '_') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    /**
     * 将字符串转换为大驼峰格式
     * @param input 输入的字符串，支持下划线分隔的形式
     * @return 转换后的大驼峰格式字符串
     */
    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String camelCase = toCamelCase(input);
        return Character.toUpperCase(camelCase.charAt(0)) + camelCase.substring(1);
    }
}
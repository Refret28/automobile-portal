package com.example.automobile_portal.models;

import java.util.HashMap;
import java.util.Map;

public class RoleTranslator {
    private static final Map<String, String> roleTranslations = new HashMap<>();

    static {
        roleTranslations.put("ADMIN", "Администратор");
        roleTranslations.put("MODERATOR", "Модератор");
        roleTranslations.put("USER", "Пользователь");
    }

    public static String translate(String role) {
        return roleTranslations.getOrDefault(role, role);
    }
}

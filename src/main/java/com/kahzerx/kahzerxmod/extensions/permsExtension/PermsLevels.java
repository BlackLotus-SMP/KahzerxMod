package com.kahzerx.kahzerxmod.extensions.permsExtension;

import java.util.Locale;

// TODO customize
public enum PermsLevels {
    TEST_MEMBER(1, "TEST_MEMBER"),
    MEMBER(2, "MEMBER"),
    HELPER(3, "HELPER"),
    MOD(4, "MOD"),
    ADMIN(5, "ADMIN");

    private final int id;
    private final String name;

    PermsLevels(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static PermsLevels getValue(int l) {
        return switch (l) {
            case 2 -> MEMBER;
            case 3 -> HELPER;
            case 4 -> MOD;
            case 5 -> ADMIN;
            default -> TEST_MEMBER;
        };
    }

    public static String[] permNames() {
        return new String[]{"TEST_MEMBER", "MEMBER", "HELPER", "MOD", "ADMIN"};
    }

    public static int getLevel(String name) {
        String upName = name.toUpperCase(Locale.ROOT);
        return switch (upName) {
            case "TEST_MEMBER" -> 1;
            case "MEMBER" -> 2;
            case "HELPER" -> 3;
            case "MOD" -> 4;
            case "ADMIN" -> 5;
            default -> -1;
        };
    }
}

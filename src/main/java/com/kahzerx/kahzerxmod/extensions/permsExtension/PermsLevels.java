package com.kahzerx.kahzerxmod.extensions.permsExtension;

import java.util.Locale;

public enum PermsLevels {
    SUB(1, "SUB"),
    HELPER(2, "HELPER"),
    MOD(3, "MOD"),
    EMSI(4, "EMSI");

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
            case 2 -> HELPER;
            case 3 -> MOD;
            case 4 -> EMSI;
            default -> SUB;
        };
    }

    public static String[] permNames() {
        return new String[]{"SUB", "HELPER", "MOD", "EMSI"};
    }

    public static int getLevel(String name) {
        String upName = name.toUpperCase(Locale.ROOT);
        return switch (upName) {
            case "SUB" -> 1;
            case "HELPER" -> 2;
            case "MOD" -> 3;
            case "EMSI" -> 4;
            default -> -1;
        };
    }
}

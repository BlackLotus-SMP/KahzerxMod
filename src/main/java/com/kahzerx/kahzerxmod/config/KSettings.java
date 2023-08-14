package com.kahzerx.kahzerxmod.config;

import java.util.List;

public record KSettings(List<Object> settings) {
    @Override
    public String toString() {
        return "KSettings{settings=" + settings + '}';
    }
}

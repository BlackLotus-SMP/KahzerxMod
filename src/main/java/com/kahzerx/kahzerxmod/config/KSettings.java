package com.kahzerx.kahzerxmod.config;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.List;

public record KSettings(List<ExtensionSettings> settings) {
    @Override
    public String toString() {
        return "KSettings{settings=" + settings + '}';
    }
}

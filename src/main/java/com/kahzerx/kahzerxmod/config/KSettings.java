package com.kahzerx.kahzerxmod.config;

import com.google.gson.internal.LinkedTreeMap;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.List;
import java.util.Objects;

public record KSettings(List<Object> settings) {
    @Override
    public String toString() {
        return "KSettings{settings=" + settings + '}';
    }
}

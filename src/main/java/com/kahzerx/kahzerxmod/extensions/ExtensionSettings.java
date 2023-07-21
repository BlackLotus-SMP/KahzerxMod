package com.kahzerx.kahzerxmod.extensions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;

public class ExtensionSettings {
    private final String name;
    private boolean enabled;
    private final String description;

    public ExtensionSettings(HashMap<String, String> fileSettings, String name, String description) {
        ExtensionSettings file = this.processFileSettings(fileSettings.getOrDefault(name, null), this.getClass());
        this.name = name;
        this.enabled = file != null && file.isEnabled();
        this.description = description;
    }

    protected ExtensionSettings processFileSettings(String settings, Class<? extends ExtensionSettings> c) {
        if (settings == null) {
            return null;
        }
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(settings, c);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "config{" +
                "name='" + name + '\'' +
                ", enabled=" + enabled +
                ", description='" + description + '\'' +
                '}';
    }
}

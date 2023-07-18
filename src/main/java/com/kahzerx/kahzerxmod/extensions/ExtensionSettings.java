package com.kahzerx.kahzerxmod.extensions;

import java.util.HashMap;

public class ExtensionSettings {
    private final String name;
    private boolean enabled;
    private final String description;

    public ExtensionSettings(HashMap<String, Boolean> config, String name, String description) {
        this.name = name;
        this.enabled = this.isEnabled(config, name);
        this.description = description;
    }

    private boolean isEnabled(HashMap<String, Boolean> found, String extension) {
        return found.get(extension) != null ? found.get(extension) : false;
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

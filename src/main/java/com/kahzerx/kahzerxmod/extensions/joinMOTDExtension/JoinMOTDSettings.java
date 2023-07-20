package com.kahzerx.kahzerxmod.extensions.joinMOTDExtension;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.HashMap;

public class JoinMOTDSettings extends ExtensionSettings {
    private String message;
    public JoinMOTDSettings(HashMap<String, String> fileSettings, String name, String description) {
        super(fileSettings, name, description);
        JoinMOTDSettings file = (JoinMOTDSettings) this.processFileSettings(fileSettings.getOrDefault(name, null), this.getClass());
        this.message = file != null && file.getMessage() != null ? file.getMessage() : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "config{" +
                "name='" + this.getName() + '\'' +
                ", enabled=" + this.isEnabled() +
                ", description='" + this.getDescription() + '\'' +
                ", message='" + this.getMessage() + '\'' +
                '}';
    }
}

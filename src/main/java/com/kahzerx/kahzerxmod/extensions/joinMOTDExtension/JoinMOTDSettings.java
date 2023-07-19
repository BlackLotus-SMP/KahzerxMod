package com.kahzerx.kahzerxmod.extensions.joinMOTDExtension;

import com.kahzerx.kahzerxmod.extensions.ExtensionSettings;

import java.util.HashMap;

public class JoinMOTDSettings extends ExtensionSettings {
    private String message;
    public JoinMOTDSettings(HashMap<String, String> fileSettings, String name, String description, String message) {
        super(fileSettings, name, description);
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

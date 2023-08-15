package com.kahzerx.kahzerxmod.config;

import java.util.List;

public record KSettings(List<Object> settings, int commandLevel) {
    @Override
    public String toString() {
        return "KSettings{commandLevel=" + this.commandLevel + ", settings=" + this.settings + "}";
    }
}

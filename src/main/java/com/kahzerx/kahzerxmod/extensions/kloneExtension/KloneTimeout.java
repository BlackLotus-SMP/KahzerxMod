package com.kahzerx.kahzerxmod.extensions.kloneExtension;

import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class KloneTimeout extends TimerTask {
    private final KloneExtension kloneExtension;
    public KloneTimeout(KloneExtension kloneExtension) {
        this.kloneExtension = kloneExtension;
    }

    @Override
    public void run() {
        for (KlonePlayerEntity kp : this.kloneExtension.getKlones()) {
            if (kp.isTimeout()) {
                kp.kill();
            }
        }
    }
}

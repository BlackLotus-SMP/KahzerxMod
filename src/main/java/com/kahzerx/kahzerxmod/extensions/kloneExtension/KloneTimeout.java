package com.kahzerx.kahzerxmod.extensions.kloneExtension;

import com.kahzerx.kahzerxmod.klone.KlonePlayerEntity;

import java.util.ArrayList;
import java.util.TimerTask;

public class KloneTimeout extends TimerTask {
    private final KloneExtension kloneExtension;
    public KloneTimeout(KloneExtension kloneExtension) {
        this.kloneExtension = kloneExtension;
    }

    @Override
    public void run() {
        // Copia de la lista (kill() la modifica) y el kill en el hilo del servidor: esto corre en un Timer.
        for (KlonePlayerEntity kp : new ArrayList<>(this.kloneExtension.getKlones())) {
            if (kp.isTimeout()) {
                kp.level().getServer().execute(kp::kill);
            }
        }
    }
}

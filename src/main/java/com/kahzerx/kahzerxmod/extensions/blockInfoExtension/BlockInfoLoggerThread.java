package com.kahzerx.kahzerxmod.extensions.blockInfoExtension;

import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.helpers.BlockActionLog;

import java.util.concurrent.TimeUnit;

public class BlockInfoLoggerThread extends Thread {
    // Set from the server thread, read here: volatile, or this thread may never see the change.
    private volatile boolean running = true;
    private final BlockInfoExtension extension;
    private volatile boolean shouldClear = false;

    public BlockInfoLoggerThread(String name, BlockInfoExtension extension) {
        super(name);
        this.extension = extension;
    }

    @Override
    public void run() {
        while (this.running) {
            if (shouldClear) {
                extension.db.getQuery().clearLogTable();
                shouldClear = false;
            }
            try {
                // Sleeps until an action arrives instead of spinning on isEmpty(), which kept a
                // whole core busy. The timeout bounds how long a stop or a clear waits.
                BlockActionLog action = BlockInfoExtension.queue.poll(1, TimeUnit.SECONDS);
                if (action != null && this.extension.getSettings().isEnabled()) {
                    extension.db.getQuery().logAction(action);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopLogger() {
        this.running = false;
    }

    public void setShouldClear(boolean shouldClear) {
        this.shouldClear = shouldClear;
    }
}

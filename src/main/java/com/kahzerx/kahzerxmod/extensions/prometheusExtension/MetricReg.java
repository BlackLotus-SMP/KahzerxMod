package com.kahzerx.kahzerxmod.extensions.prometheusExtension;

import com.kahzerx.kahzerxmod.extensions.prometheusExtension.metrics.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class MetricReg {
    private final PrometheusExtension extension;
    private Timer timer;
    private final List<AbstractMetric> metrics = new ArrayList<>();
    public MetricReg(PrometheusExtension prometheusExtension) {
        this.extension = prometheusExtension;
    }

    public void registerMetrics() {
        MSPTMetric msptMetric = new MSPTMetric("mspt", "Current MSPT on server.");
        msptMetric.getGauge().register();

        TPSMetric tpsMetric = new TPSMetric("tps", "Average TPS on server.");
        tpsMetric.getGauge().register();

        LoadedChunksMetrics loadedChunksMetrics = new LoadedChunksMetrics("loaded_chunks", "Amount of loaded chunks.");
        loadedChunksMetrics.getGauge().register();

        EntitiesMetrics entitiesMetrics = new EntitiesMetrics("entities", "Amount of entities.");
        entitiesMetrics.getGauge().register();

        BlockEntitiesMetrics blockEntitiesMetrics = new BlockEntitiesMetrics("block_entities", "Amount of block entities");
        blockEntitiesMetrics.getGauge().register();

        RamMetric ramMetric = new RamMetric("ram", "Usage of RAM.");
        ramMetric.getGauge().register();

        OnlinePlayersMetrics onlinePlayersMetrics = new OnlinePlayersMetrics("online_players", "Connected players");
        onlinePlayersMetrics.getGauge().register();

        metrics.add(msptMetric);
        metrics.add(tpsMetric);
        metrics.add(loadedChunksMetrics);
        metrics.add(entitiesMetrics);
        metrics.add(blockEntitiesMetrics);
        metrics.add(ramMetric);
        metrics.add(onlinePlayersMetrics);
    }

    public void runUpdater() {
        if (this.timer != null) {
            this.timer.cancel();
        }
        this.timer = new Timer();
        MetricUpdater metricUpdater = new MetricUpdater(this.extension);
        for (AbstractMetric m : metrics) {
            metricUpdater.addMetric(m);
        }
        this.timer.schedule(metricUpdater, 1000, 1000);
    }

    public Timer getTimer() {
        return timer;
    }
}
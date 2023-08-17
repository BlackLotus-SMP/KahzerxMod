package com.kahzerx.kahzerxmod;

import com.kahzerx.kahzerxmod.config.KSettings;
import com.kahzerx.kahzerxmod.extensions.backExtension.BackExtension;
import com.kahzerx.kahzerxmod.extensions.badgeExtension.BadgeExtension;
import com.kahzerx.kahzerxmod.extensions.bedTimeExtension.BedTimeExtension;
import com.kahzerx.kahzerxmod.extensions.blockInfoExtension.BlockInfoExtension;
import com.kahzerx.kahzerxmod.extensions.bocaExtension.BocaExtension;
import com.kahzerx.kahzerxmod.extensions.cameraExtension.CameraExtension;
import com.kahzerx.kahzerxmod.extensions.deathMsgExtension.DeathMsgExtension;
import com.kahzerx.kahzerxmod.extensions.deepslateInstaMineExtension.DeepslateInstaMineExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordAdminToolsExtension.DiscordAdminToolsExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordExtension.DiscordExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistExtension.DiscordWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.discordExtension.discordWhitelistSyncExtension.DiscordWhitelistSyncExtension;
import com.kahzerx.kahzerxmod.extensions.endermanNoGriefExtension.EndermanNoGriefExtension;
import com.kahzerx.kahzerxmod.extensions.farmlandMyceliumExtension.FarmlandMyceliumExtension;
import com.kahzerx.kahzerxmod.extensions.fbiExtension.FBIExtension;
import com.kahzerx.kahzerxmod.extensions.fckPrivacyExtension.FckPrivacyExtension;
import com.kahzerx.kahzerxmod.extensions.hatExtension.HatExtension;
import com.kahzerx.kahzerxmod.extensions.helperKickExtension.HelperKickExtension;
import com.kahzerx.kahzerxmod.extensions.hereExtension.HereExtension;
import com.kahzerx.kahzerxmod.extensions.homeExtension.HomeExtension;
import com.kahzerx.kahzerxmod.extensions.itemFormattedExtension.ItemFormattedExtension;
import com.kahzerx.kahzerxmod.extensions.joinMOTDExtension.JoinMOTDExtension;
import com.kahzerx.kahzerxmod.extensions.kloneExtension.KloneExtension;
import com.kahzerx.kahzerxmod.extensions.maintenanceExtension.MaintenanceExtension;
import com.kahzerx.kahzerxmod.extensions.memberExtension.MemberExtension;
import com.kahzerx.kahzerxmod.extensions.opOnWhitelistExtension.OpOnWhitelistExtension;
import com.kahzerx.kahzerxmod.extensions.permsExtension.PermsExtension;
import com.kahzerx.kahzerxmod.extensions.playerDropsSkullExtension.PlayerDropsSkullExtension;
import com.kahzerx.kahzerxmod.extensions.prankExtension.PrankExtension;
import com.kahzerx.kahzerxmod.extensions.randomTPExtension.RandomTPExtension;
import com.kahzerx.kahzerxmod.extensions.renewableElytraExtension.RenewableElytraExtension;
import com.kahzerx.kahzerxmod.extensions.scoreboardExtension.ScoreboardExtension;
import com.kahzerx.kahzerxmod.extensions.seedExtension.SeedExtension;
import com.kahzerx.kahzerxmod.extensions.sitExtension.SitExtension;
import com.kahzerx.kahzerxmod.extensions.skullExtension.SkullExtension;
import com.kahzerx.kahzerxmod.extensions.slabExtension.SlabExtension;
import com.kahzerx.kahzerxmod.extensions.solExtension.SolExtension;
import com.kahzerx.kahzerxmod.extensions.spawnExtension.SpawnExtension;
import com.kahzerx.kahzerxmod.extensions.spoofExtension.SpoofExtension;
import com.kahzerx.kahzerxmod.extensions.survivalExtension.SurvivalExtension;
import com.kahzerx.kahzerxmod.extensions.totopoExtension.TotopoExtension;
import com.kahzerx.kahzerxmod.extensions.villagersFollowEmeraldExtension.VillagersFollowEmeraldExtension;
import com.kahzerx.kahzerxmod.extensions.whereExtension.WhereExtension;
import com.kahzerx.kahzerxmod.utils.FileUtils;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class ExtensionManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final SortedMap<String, Extensions> extensions = new TreeMap<>();
    private final String SETTINGS_BASE_COMMAND;
    private int defaultSettingCommandLevel;

    public ExtensionManager(String settingsBaseCommand, int defaultSettingCommandLevel) {
        this.SETTINGS_BASE_COMMAND = settingsBaseCommand;
        this.defaultSettingCommandLevel = defaultSettingCommandLevel;
    }

    public int getDefaultSettingCommandLevel() {
        return defaultSettingCommandLevel;
    }

    public void setDefaultSettingCommandLevel(int defaultSettingCommandLevel) {
        this.defaultSettingCommandLevel = defaultSettingCommandLevel;
    }

    public SortedMap<String, Extensions> getExtensions() {
        return this.extensions;
    }

    // TODO force func call on setters?
    public void saveSettings() {
        List<Object> settingsArray = new ArrayList<>();
        for (Extensions ex : this.extensions.values()) {
            settingsArray.add(ex.extensionSettings());
        }
        KSettings settings = new KSettings(settingsArray, this.getDefaultSettingCommandLevel());
        FileUtils.createConfig(KahzerxServer.minecraftServer.getSavePath(WorldSavePath.ROOT).toString(), settings);
    }

    public void loadExtensions(String settings) {
        LOGGER.info("Loading settings from file world/KConfig.json");
        HashMap<String, String> fileSettings = new HashMap<>();
        JSONParser parser = new JSONParser();
        try {
            JSONObject setting = (JSONObject) parser.parse(settings);
            long settingCommandLevel = (long) setting.getOrDefault("commandLevel", -1);
            if (settingCommandLevel != -1) {
                this.setDefaultSettingCommandLevel((int) settingCommandLevel);
            }
            JSONArray settingsList = (JSONArray) setting.get("settings");
            for (Object obj : settingsList) {
                String ruleName = (String) ((JSONObject) obj).getOrDefault("name", null);
                if (ruleName == null) {
                    continue;
                }
                fileSettings.put(ruleName, ((JSONObject) obj).toJSONString());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // TODO way of making so the extensions register themselves so we dont need massive class of extensions.add(...)
        // TODO modTP for perms!

        this.registerExtension(new MemberExtension(fileSettings));
        this.registerExtension(new PermsExtension(fileSettings));
        this.registerExtension(new HomeExtension(fileSettings));
        this.registerExtension(new BackExtension(fileSettings));
        this.registerExtension(new CameraExtension(fileSettings));
        this.registerExtension(new HelperKickExtension(fileSettings));
        this.registerExtension(new SurvivalExtension(fileSettings));
        this.registerExtension(new HereExtension(fileSettings));
        this.registerExtension(new DeathMsgExtension(fileSettings));
        this.registerExtension(new RandomTPExtension(fileSettings));
        this.registerExtension(new BlockInfoExtension(fileSettings));
        this.registerExtension(new SeedExtension(fileSettings));
        this.registerExtension(new FckPrivacyExtension(fileSettings));
        this.registerExtension(new SpoofExtension(fileSettings));
        this.registerExtension(new ScoreboardExtension(fileSettings));
        this.registerExtension(new SpawnExtension(fileSettings));
        this.registerExtension(new WhereExtension(fileSettings));
        this.registerExtension(new BocaExtension(fileSettings));
        this.registerExtension(new TotopoExtension(fileSettings));
        this.registerExtension(new HatExtension(fileSettings));
        this.registerExtension(new EndermanNoGriefExtension(fileSettings));
        this.registerExtension(new DeepslateInstaMineExtension(fileSettings));
        this.registerExtension(new RenewableElytraExtension(fileSettings));
        this.registerExtension(new VillagersFollowEmeraldExtension(fileSettings));
        this.registerExtension(new SolExtension(fileSettings));
        this.registerExtension(new KloneExtension(fileSettings));
        this.registerExtension(new MaintenanceExtension(fileSettings));
        this.registerExtension(new PrankExtension(fileSettings));
        this.registerExtension(new SkullExtension(fileSettings));
        this.registerExtension(new PlayerDropsSkullExtension(fileSettings));
        this.registerExtension(new BadgeExtension(fileSettings));
        this.registerExtension(new ItemFormattedExtension(fileSettings));
        this.registerExtension(new SlabExtension(fileSettings));
        this.registerExtension(new SitExtension(fileSettings));
        this.registerExtension(new FarmlandMyceliumExtension(fileSettings));
        this.registerExtension(new FBIExtension(fileSettings));
        this.registerExtension(new OpOnWhitelistExtension(fileSettings));
        this.registerExtension(new BedTimeExtension(fileSettings));
        // TODO bring back profile extension!
        this.registerExtension(new JoinMOTDExtension(fileSettings));
        this.registerExtension(new DiscordExtension(fileSettings));
        this.registerExtension(new DiscordWhitelistExtension(fileSettings));
        this.registerExtension(new DiscordAdminToolsExtension(fileSettings));
        this.registerExtension(new DiscordWhitelistSyncExtension(fileSettings));
        // TODO bungee support
        this.printExtensions();
        LOGGER.info("Settings loaded!");
    }

    private void printExtensions() {
        for (Extensions extension : this.extensions.values()) {
            LOGGER.info(extension.extensionSettings());
        }
    }

    private void registerExtension(Extensions extension) {
        this.extensions.put(extension.extensionSettings().getName(), extension);
    }

    public String getSettingsBaseCommand() {
        return this.SETTINGS_BASE_COMMAND;
    }
}

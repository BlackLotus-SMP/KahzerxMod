package com.kahzerx.kahzerxmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
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
import com.kahzerx.kahzerxmod.extensions.profileExtension.ProfileExtension;
import com.kahzerx.kahzerxmod.extensions.randomTPExtension.RandomTPExtension;
import com.kahzerx.kahzerxmod.extensions.renewableElytraExtension.RenewableElytraExtension;
import com.kahzerx.kahzerxmod.extensions.scoreboardExtension.ScoreboardExtension;
import com.kahzerx.kahzerxmod.extensions.seedExtension.SeedExtension;
import com.kahzerx.kahzerxmod.extensions.shopExtension.ShopExtension;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExtensionManager {
    public static void saveSettings() {
        List<Object> settingsArray = new ArrayList<>();
        for (Extensions ex : KahzerxServer.extensions) {
            settingsArray.add(ex.extensionSettings());
        }
        KSettings settings = new KSettings(settingsArray);
        FileUtils.createConfig(KahzerxServer.minecraftServer.getSavePath(WorldSavePath.ROOT).toString(), settings);
    }

    public static void manageExtensions(String settings) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        KSettings ks = gson.fromJson(settings, KSettings.class);
        HashMap<String, String> fileSettings = new HashMap<>();

        if (ks != null) {
            for (Object es : ks.settings()) {
                if (es == null) {
                    continue;
                }
                Object name = ((LinkedTreeMap<?, ?>) es).get("name");
                if (name == null) {
                    continue;
                }
                fileSettings.put((String) name, gson.toJson(es));
            }
        }

        // TODO way of making so the extensions register themselves so we dont need massive class of extensions.add(...)

        MemberExtension memberExtension = new MemberExtension(fileSettings);
        // TODO make extensions have access to all the other extensions so they can consult this.extensions.get("member").getSettings()...
        PermsExtension permsExtension = new PermsExtension(fileSettings, memberExtension);
        ShopExtension shopExtension = new ShopExtension(fileSettings, permsExtension);

        KahzerxServer.extensions.add(memberExtension);
        KahzerxServer.extensions.add(permsExtension);
        KahzerxServer.extensions.add(shopExtension);
        KahzerxServer.extensions.add(new HomeExtension(fileSettings));
        KahzerxServer.extensions.add(new BackExtension(fileSettings, permsExtension));
        KahzerxServer.extensions.add(new CameraExtension(fileSettings, permsExtension));
        // TODO modTP for perms!
        KahzerxServer.extensions.add(new HelperKickExtension(fileSettings, permsExtension));
        KahzerxServer.extensions.add(new SurvivalExtension(fileSettings));
        KahzerxServer.extensions.add(new HereExtension(fileSettings));
        KahzerxServer.extensions.add(new DeathMsgExtension(fileSettings));
        KahzerxServer.extensions.add(new RandomTPExtension(fileSettings, permsExtension));
        KahzerxServer.extensions.add(new BlockInfoExtension(fileSettings));
        KahzerxServer.extensions.add(new SeedExtension(fileSettings));
        KahzerxServer.extensions.add(new FckPrivacyExtension(fileSettings));
        KahzerxServer.extensions.add(new SpoofExtension(fileSettings));
        KahzerxServer.extensions.add(new ScoreboardExtension(fileSettings));
        KahzerxServer.extensions.add(new SpawnExtension(fileSettings));
        KahzerxServer.extensions.add(new WhereExtension(fileSettings, permsExtension));
        KahzerxServer.extensions.add(new BocaExtension(fileSettings));
        KahzerxServer.extensions.add(new TotopoExtension(fileSettings));
        KahzerxServer.extensions.add(new HatExtension(fileSettings));
        KahzerxServer.extensions.add(new EndermanNoGriefExtension(fileSettings));
        KahzerxServer.extensions.add(new DeepslateInstaMineExtension(fileSettings));
        KahzerxServer.extensions.add(new RenewableElytraExtension(fileSettings));
        KahzerxServer.extensions.add(new VillagersFollowEmeraldExtension(fileSettings));
        KahzerxServer.extensions.add(new SolExtension(fileSettings));
        KahzerxServer.extensions.add(new KloneExtension(fileSettings, permsExtension));
        KahzerxServer.extensions.add(new MaintenanceExtension(fileSettings));
        KahzerxServer.extensions.add(new PrankExtension(fileSettings));
        // TODO validate if 2 extensions with the same name exist
        KahzerxServer.extensions.add(new SkullExtension(fileSettings));
        KahzerxServer.extensions.add(new PlayerDropsSkullExtension(fileSettings));
        KahzerxServer.extensions.add(new BadgeExtension(fileSettings, permsExtension));
        KahzerxServer.extensions.add(new ItemFormattedExtension(fileSettings));
        KahzerxServer.extensions.add(new SlabExtension(fileSettings));
        KahzerxServer.extensions.add(new SitExtension(fileSettings));
        KahzerxServer.extensions.add(new FarmlandMyceliumExtension(fileSettings));
        KahzerxServer.extensions.add(new FBIExtension(fileSettings));
        KahzerxServer.extensions.add(new OpOnWhitelistExtension(fileSettings));
        KahzerxServer.extensions.add(new BedTimeExtension(fileSettings));
        KahzerxServer.extensions.add(new ProfileExtension(fileSettings, shopExtension));
        KahzerxServer.extensions.add(new JoinMOTDExtension(fileSettings, permsExtension));
        DiscordExtension discordExtension = new DiscordExtension(fileSettings);
        KahzerxServer.extensions.add(discordExtension);
        DiscordWhitelistExtension discordWhitelistExtension = new DiscordWhitelistExtension(fileSettings, discordExtension);
        KahzerxServer.extensions.add(discordWhitelistExtension);
        DiscordAdminToolsExtension discordAdminToolsExtension = new DiscordAdminToolsExtension(fileSettings, discordExtension, discordWhitelistExtension);
        KahzerxServer.extensions.add(discordAdminToolsExtension);
        KahzerxServer.extensions.add(new DiscordWhitelistSyncExtension(fileSettings, discordExtension, discordWhitelistExtension));
    }
}

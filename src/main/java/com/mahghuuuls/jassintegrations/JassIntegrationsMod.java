package com.mahghuuuls.jassintegrations;

import com.mahghuuuls.jassintegrations.config.Settings;
import com.mahghuuuls.jassintegrations.diagnostic.DebugLog;
import com.mahghuuuls.jassintegrations.integration.IntegrationGate;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:jass@[1.0.0,);required-after:mixinbooter@[10.7,);"
                + "after:spartanweaponry;after:spartanshields;after:qualitytools;after:quark;after:jafs")
public class JassIntegrationsMod {

    @SidedProxy(clientSide = "com.mahghuuuls.jassintegrations.client.ClientProxy",
            serverSide = "com.mahghuuuls.jassintegrations.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        reloadSettings();
        MinecraftForge.EVENT_BUS.register(new ConfigListener());
        IntegrationGate.startAll();
        proxy.startClientParts();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        IntegrationGate.postInitAll();
    }

    private static void reloadSettings() {
        for (String warning : Settings.reload()) {
            DebugLog.warn(warning);
        }
    }

    /** Applies edits made in the in-game configuration screen. */
    public static final class ConfigListener {

        @SubscribeEvent
        public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (Tags.MOD_ID.equals(event.getModID())) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
                reloadSettings();
            }
        }
    }
}

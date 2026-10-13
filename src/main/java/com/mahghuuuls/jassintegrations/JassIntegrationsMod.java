package com.mahghuuuls.jassintegrations;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION,
        dependencies = "required-after:jass@[1.0.0,);required-after:mixinbooter@[10.7,);"
                + "after:spartanweaponry;after:spartanshields;after:qualitytools;after:quark")
public class JassIntegrationsMod {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }
}

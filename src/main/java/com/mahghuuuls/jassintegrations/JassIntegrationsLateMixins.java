package com.mahghuuuls.jassintegrations;

import net.minecraftforge.fml.common.Loader;
import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

/**
 * Mixins into other mods' classes, queued by MixinBooter once mods are known: the Spartan Shields
 * bash Mixins only when Spartan Shields is present. Lives outside every Mixin package, because
 * Mixin refuses to load ordinary classes from a package a Mixin configuration owns.
 */
public final class JassIntegrationsLateMixins implements ILateMixinLoader {

    static final String SPARTAN_SHIELDS_CONFIG = "mixins.jassintegrations.spartanshields.json";

    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList(SPARTAN_SHIELDS_CONFIG);
    }

    @Override
    public boolean shouldMixinConfigQueue(String config) {
        return !SPARTAN_SHIELDS_CONFIG.equals(config) || Loader.isModLoaded("spartanshields");
    }
}

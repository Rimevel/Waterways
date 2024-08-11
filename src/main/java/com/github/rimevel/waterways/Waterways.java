package com.github.rimevel.waterways;

import com.github.rimevel.waterways.common.Register;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Waterways.MODID)
public class Waterways {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "waterways";

    public Waterways() {
        // Register all new things to the Deferred Register
        Register.init();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.CONFIG);
    }
}

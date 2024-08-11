package com.github.rimevel.waterways;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

// Replace 'com.yourmod' with your actual package name
@Mod.EventBusSubscriber(modid = Waterways.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final String CATEGORY_GENERAL = "general";

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG;

    public static final ForgeConfigSpec.BooleanValue BUCKET_NERF;

    static {
        BUILDER.comment("General settings").push(CATEGORY_GENERAL);

        BUCKET_NERF = BUILDER.comment("Enable or disable bucket nerf")
                .define("bucketNerf", true);

        BUILDER.pop();
        CONFIG = BUILDER.build();
    }
}

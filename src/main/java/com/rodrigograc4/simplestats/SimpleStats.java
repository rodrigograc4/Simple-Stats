package com.rodrigograc4.simplestats;

import com.rodrigograc4.simplestats.config.SimpleStatsConfig;
import com.rodrigograc4.simplestats.renderer.SimpleStatsHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleStats implements ClientModInitializer {
    public static final String MOD_ID = "simplestats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        SimpleStatsConfig.init();
        
        HudRenderCallback.EVENT.register(new SimpleStatsHud());
        
        LOGGER.info("Simple Stats inicializado com sucesso!");
    }
}
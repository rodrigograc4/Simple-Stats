package com.rodrigograc4.windrose;

import com.rodrigograc4.windrose.config.WindRoseConfig;
import com.rodrigograc4.windrose.renderer.WindRoseHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindRose implements ClientModInitializer {
    public static final String MOD_ID = "windrose";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        WindRoseConfig.init();
        
        HudRenderCallback.EVENT.register(new WindRoseHud());
        
        LOGGER.info("WindRose inicializado com sucesso!");
    }
}
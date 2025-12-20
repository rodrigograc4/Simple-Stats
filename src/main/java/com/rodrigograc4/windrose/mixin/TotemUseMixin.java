package com.rodrigograc4.windrose.mixin;

import com.rodrigograc4.windrose.config.WindRoseConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class TotemUseMixin {

    @Inject(method = "onEntityStatus", at = @At("HEAD"))
    private void onEntityStatus(EntityStatusS2CPacket packet, CallbackInfo ci) {
        if (packet.getStatus() != EntityStatuses.USE_TOTEM_OF_UNDYING) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread() || client.world == null || client.player == null) return;

        Entity entity = packet.getEntity(client.world);
        if (entity != client.player) return;

        String worldKey;
        if (client.getCurrentServerEntry() != null) {
            worldKey = client.getCurrentServerEntry().name;
        } else {
            worldKey = client.getServer() != null
                    ? client.getServer().getSaveProperties().getLevelName()
                    : "UnknownWorld";
        }

        WindRoseConfig.INSTANCE.incrementTotems(worldKey);

        System.out.println("!!! TOTEM CONTADO COM SUCESSO PARA WORLD/SERVER: " 
                + worldKey 
                + " | Total: " + WindRoseConfig.INSTANCE.getTotemsForWorld(worldKey));
    }
}

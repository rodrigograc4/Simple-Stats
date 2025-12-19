package com.rodrigograc4.simplestats.renderer;

import com.rodrigograc4.simplestats.config.SimpleStatsConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;

public class SimpleStatsHud implements HudRenderCallback {

    /* ===================== UTIL ===================== */

    private static int opaque(int rgb) {
        return 0xFF000000 | rgb;
    }

    /* ===================== DIRECTION ===================== */

    private static String getCardinalFull(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        if (yaw >= 315 || yaw < 45) return "South";
        if (yaw < 135) return "West";
        if (yaw < 225) return "North";
        return "East";
    }

    private static String getCardinalShort(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        if (yaw >= 315 || yaw < 45) return "S";
        if (yaw < 135) return "W";
        if (yaw < 225) return "N";
        return "E";
    }

    private static String getAxisFull(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        if (yaw >= 315 || yaw < 45) return "Positive Z";
        if (yaw < 135) return "Negative X";
        if (yaw < 225) return "Negative Z";
        return "Positive X";
    }

    private static String getAxisShort(float yaw) {
        yaw = (yaw % 360 + 360) % 360;
        if (yaw >= 315 || yaw < 45) return "+Z";
        if (yaw < 135) return "-X";
        if (yaw < 225) return "-Z";
        return "+X";
    }

    /* ===================== RENDER ===================== */

    @Override
    public void onHudRender(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.world == null) return;

        SimpleStatsConfig config = SimpleStatsConfig.INSTANCE;
        if (config == null || !config.statsEnabled) return;

        TextRenderer tr = client.textRenderer;

        if (config.hudPosition == SimpleStatsConfig.HudPosition.HOTBAR_TOP) {
            renderHotbarHud(ctx, client, tr, config);
        } else {
            renderTopLeftHud(ctx, client, tr, config);
        }
    }

    /* ===================== TOP LEFT HUD ===================== */

    private void renderTopLeftHud(DrawContext ctx, MinecraftClient client, TextRenderer tr, SimpleStatsConfig c) {
        int x = (int) c.padding;
        int y = (int) c.padding;
        int spacing = tr.fontHeight + (int) c.linepadding;

        // DAY
        if (c.showDayCount) {
            long ticks = client.world.getTimeOfDay();
            long days = (ticks / 24000L) + c.dayCountOffset;

            ctx.drawTextWithShadow(tr, c.dayCountString, x, y, opaque(c.labelColor));
            int dx = x + tr.getWidth(c.dayCountString);
            ctx.drawTextWithShadow(tr, String.valueOf(days), dx, y, opaque(c.valueColor));

            y += spacing;
        }

        // COORDS
        if (c.showCoords) {
            BlockPos p = client.player.getBlockPos();
            String coords = p.getX() + ", " + p.getY() + ", " + p.getZ();

            ctx.drawTextWithShadow(tr, c.coordsString, x, y, opaque(c.labelColor));
            ctx.drawTextWithShadow(tr, coords, x + tr.getWidth(c.coordsString), y, opaque(c.valueColor));

            y += spacing;
        }

        // DIRECTION (FULL)
        if (c.showDirection) {
            float yaw = client.player.getYaw();
            String dir = switch (c.directionMode) {
                case CARDINAL -> getCardinalFull(yaw);
                case AXIS -> getAxisFull(yaw);
            };

            ctx.drawTextWithShadow(tr, c.directionLabel, x, y, opaque(c.labelColor));
            ctx.drawTextWithShadow(tr, dir, x + tr.getWidth(c.directionLabel), y, opaque(c.valueColor));

            y += spacing;
        }

        // TOTEMS
        if (c.showTotemsPopped) {
            String world = client.getCurrentServerEntry() != null
                    ? client.getCurrentServerEntry().name
                    : "Singleplayer";

            String value = String.valueOf(c.getTotemsForWorld(world));

            ctx.drawTextWithShadow(tr, c.totemsPoppedString, x, y, opaque(c.labelColor));
            ctx.drawTextWithShadow(tr, value,
                    x + tr.getWidth(c.totemsPoppedString),
                    y,
                    opaque(c.valueColor));
        }
    }

   /* ===================== HOTBAR HUD ===================== */

    private int getHotbarHudWidth(TextRenderer tr, MinecraftClient client, SimpleStatsConfig c) {
        String sep = " | ";
        int width = 0;

        if (c.showCoords) {
            BlockPos p = client.player.getBlockPos();
            String value = p.getX() + " " + p.getY() + " " + p.getZ();
            width += tr.getWidth(c.coordsString) + tr.getWidth(value) + tr.getWidth(sep);
        }

        if (c.showDirection) {
            float yaw = client.player.getYaw();
            String value = switch (c.directionMode) {
                case CARDINAL -> getCardinalShort(yaw);
                case AXIS -> getAxisShort(yaw);
            };
            width += tr.getWidth(c.directionLabel) + tr.getWidth(value) + tr.getWidth(sep);
        }

        if (c.showDayCount) {
            long ticks = client.world.getTimeOfDay();
            long days = (ticks / 24000L) + c.dayCountOffset;
            width += tr.getWidth(c.dayCountString) + tr.getWidth(String.valueOf(days)) + tr.getWidth(sep);
        }

        if (c.showTotemsPopped) {
            String world = client.getCurrentServerEntry() != null
                    ? client.getCurrentServerEntry().name
                    : "Singleplayer";
            String value = String.valueOf(c.getTotemsForWorld(world));
            width += tr.getWidth(c.totemsPoppedString) + tr.getWidth(value);
        }

        // remover o último separador
        if (width > 0) {
            width -= tr.getWidth(sep);
        }

        return width;
    }


    private void renderHotbarHud(DrawContext ctx, MinecraftClient client, TextRenderer tr, SimpleStatsConfig c) {
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int y = sh - 49 - tr.fontHeight - 4;

        int totalWidth = getHotbarHudWidth(tr, client, c);
        int drawX = (sw - totalWidth) / 2;

        String separator = " | ";

        /* ========= COORDS ========= */
        if (c.showCoords) {
            BlockPos p = client.player.getBlockPos();
            String coordsValue = p.getX() + ", " + p.getY() + ", " + p.getZ();

            drawX -= tr.getWidth(
                    c.coordsString + coordsValue + separator
            ) / 2;

            ctx.drawTextWithShadow(tr, c.coordsString, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(c.coordsString);

            ctx.drawTextWithShadow(tr, coordsValue, drawX, y, opaque(c.valueColor));
            drawX += tr.getWidth(coordsValue);

            ctx.drawTextWithShadow(tr, separator, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(separator);
        }

        /* ========= DIRECTION (SHORT) ========= */
        if (c.showDirection) {
            float yaw = client.player.getYaw();
            String dirValue = switch (c.directionMode) {
                case CARDINAL -> getCardinalShort(yaw);
                case AXIS -> getAxisShort(yaw);
            };

            ctx.drawTextWithShadow(tr, c.directionLabel, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(c.directionLabel);

            ctx.drawTextWithShadow(tr, dirValue, drawX, y, opaque(c.valueColor));
            drawX += tr.getWidth(dirValue);

            ctx.drawTextWithShadow(tr, separator, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(separator);
        }

        /* ========= DAY ========= */
        if (c.showDayCount) {
            long ticks = client.world.getTimeOfDay();
            long days = (ticks / 24000L) + c.dayCountOffset;

            ctx.drawTextWithShadow(tr, c.dayCountString, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(c.dayCountString);

            ctx.drawTextWithShadow(tr, String.valueOf(days), drawX, y, opaque(c.valueColor));
            drawX += tr.getWidth(String.valueOf(days));

            ctx.drawTextWithShadow(tr, separator, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(separator);
        }

        /* ========= TOTEMS ========= */
        if (c.showTotemsPopped) {
            String world = client.getCurrentServerEntry() != null
                    ? client.getCurrentServerEntry().name
                    : "Singleplayer";

            String value = String.valueOf(c.getTotemsForWorld(world));

            ctx.drawTextWithShadow(tr, c.totemsPoppedString, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(c.totemsPoppedString);

            ctx.drawTextWithShadow(tr, value, drawX, y, opaque(c.valueColor));
        }
    }
}
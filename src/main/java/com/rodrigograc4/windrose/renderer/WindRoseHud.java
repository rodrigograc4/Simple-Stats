package com.rodrigograc4.windrose.renderer;

import com.rodrigograc4.windrose.config.WindRoseConfig;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.BlockPos;

public class WindRoseHud implements HudRenderCallback {

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

        WindRoseConfig config = WindRoseConfig.INSTANCE;
        if (config == null || !config.statsEnabled) return;

        TextRenderer tr = client.textRenderer;

        if (config.hudPosition == WindRoseConfig.HudPosition.HOTBAR_TOP) {
            renderHotbarHud(ctx, client, tr, config);
        } else {
            renderTopLeftHud(ctx, client, tr, config);
        }
    }


    /* ===================== HELPERS ===================== */

    private boolean hasMoreHotbarStats(WindRoseConfig c, String current) {
        return switch (current) {
            case "COORDS" -> c.showDirection || c.showDayCount || c.showTotemsPopped;
            case "DIRECTION" -> c.showDayCount || c.showTotemsPopped;
            case "DAY" -> c.showTotemsPopped;
            default -> false;
        };
    }

    private String getWorldKey(MinecraftClient client) {
        if (client.getCurrentServerEntry() != null) {
            return client.getCurrentServerEntry().name;
        }

        if (client.getServer() != null) {
            return client.getServer().getSaveProperties().getLevelName();
        }

        return "UnknownWorld";
    }


    /* ===================== TOP LEFT HUD ===================== */

    private void renderTopLeftHud(DrawContext ctx, MinecraftClient client, TextRenderer tr, WindRoseConfig c) {
        int x = (int) c.padding;
        int y = (int) c.padding;
        int spacing = tr.fontHeight + (int) c.linepadding;

        // DAY
        if (c.showDayCount) {
            long totalTicks = client.world.getTimeOfDay();
            long days = (totalTicks / 24000L) + c.dayCountOffset;

            long dayTime = totalTicks % 24000L;
            int hours = (int) ((dayTime / 1000 + 6) % 24);
            int minutes = (int) ((dayTime % 1000) * 60 / 1000);

            String symbol = (dayTime < 12000) ? " ☀" : " 🌙";
            String timeValue = String.format("%02d:%02d", hours, minutes);

            int dx = x;

            ctx.drawTextWithShadow(tr, c.dayCountString, dx, y, opaque(c.labelColor));
            dx += tr.getWidth(c.dayCountString);

            ctx.drawTextWithShadow(tr, String.valueOf(days), dx, y, opaque(c.valueColor));
            dx += tr.getWidth(String.valueOf(days));

            if (c.showHours) {
                String label2 = " " + symbol + " ";
                ctx.drawTextWithShadow(tr, label2, dx, y, opaque(c.labelColor));
                dx += tr.getWidth(label2);

                ctx.drawTextWithShadow(tr, timeValue, dx, y, opaque(c.valueColor));
            }

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
            String worldKey = getWorldKey(client);
            String value = String.valueOf(c.getTotemsForWorld(worldKey));

            ctx.drawTextWithShadow(tr, c.totemsPoppedString, x, y, opaque(c.labelColor));
            ctx.drawTextWithShadow(
                    tr,
                    value,
                    x + tr.getWidth(c.totemsPoppedString),
                    y,
                    opaque(c.valueColor)
            );
        }
    }

   /* ===================== HOTBAR HUD ===================== */

    private int getHotbarHudWidth(TextRenderer tr, MinecraftClient client, WindRoseConfig c) {
        String sep = "  |  ";
        int width = 0;

        if (c.showCoords) {
            BlockPos p = client.player.getBlockPos();
            String value = p.getX() + ", " + p.getY() + ", " + p.getZ();
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

            width += tr.getWidth(c.dayCountString);
            width += tr.getWidth(String.valueOf(days));

            if (c.showHours) {
                width += tr.getWidth("  ☀ ");
                width += tr.getWidth("00:00");
            }

            width += tr.getWidth(sep);
        }

        if (c.showTotemsPopped) {
            String worldKey = getWorldKey(client);
            String value = String.valueOf(c.getTotemsForWorld(worldKey));
            width += tr.getWidth(c.totemsPoppedString) + tr.getWidth(value);
        }

        // remover o último separador
        if (width > 0) {
            width -= tr.getWidth(sep);
        }

        return width;
    }


    private void renderHotbarHud(DrawContext ctx, MinecraftClient client, TextRenderer tr, WindRoseConfig c) {
        int sw = client.getWindow().getScaledWidth();
        int sh = client.getWindow().getScaledHeight();
        int y = sh - 49 - tr.fontHeight - 4;

        int totalWidth = getHotbarHudWidth(tr, client, c);
        int drawX = (sw - totalWidth) / 2;

        String separator = "  |  ";

        /* ========= COORDS ========= */
        if (c.showCoords) {
            BlockPos p = client.player.getBlockPos();
            String coordsValue = p.getX() + ", " + p.getY() + ", " + p.getZ();

            ctx.drawTextWithShadow(tr, c.coordsString, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(c.coordsString);

            ctx.drawTextWithShadow(tr, coordsValue, drawX, y, opaque(c.valueColor));
            drawX += tr.getWidth(coordsValue);

            if (hasMoreHotbarStats(c, "COORDS")) {
                ctx.drawTextWithShadow(tr, separator, drawX, y, opaque(c.labelColor));
                drawX += tr.getWidth(separator);
            }
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

            if (hasMoreHotbarStats(c, "DIRECTION")) {
                ctx.drawTextWithShadow(tr, separator, drawX, y, opaque(c.labelColor));
                drawX += tr.getWidth(separator);
            }
        }

        /* ========= DAY ========= */
        if (c.showDayCount) {
            long totalTicks = client.world.getTimeOfDay();
            long days = (totalTicks / 24000L) + c.dayCountOffset;

            long dayTime = totalTicks % 24000L;
            int hours = (int) ((dayTime / 1000 + 6) % 24);
            int minutes = (int) ((dayTime % 1000) * 60 / 1000);

            String symbol = (dayTime < 12000) ? " ☀" : " 🌙";
            String timeValue = String.format("%02d:%02d", hours, minutes);

            ctx.drawTextWithShadow(tr, c.dayCountString, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(c.dayCountString);

            ctx.drawTextWithShadow(tr, String.valueOf(days), drawX, y, opaque(c.valueColor));
            drawX += tr.getWidth(String.valueOf(days));

            if (c.showHours) {
                String label2 = " " + symbol + " ";
                ctx.drawTextWithShadow(tr, label2, drawX, y, opaque(c.labelColor));
                drawX += tr.getWidth(label2);

                ctx.drawTextWithShadow(tr, timeValue, drawX, y, opaque(c.valueColor));
                drawX += tr.getWidth(timeValue);
            }

            if (hasMoreHotbarStats(c, "DAY")) {
                ctx.drawTextWithShadow(tr, separator, drawX, y, opaque(c.labelColor));
                drawX += tr.getWidth(separator);
            }
        }

        /* ========= TOTEMS ========= */
        if (c.showTotemsPopped) {
            String worldKey = getWorldKey(client);
            String value = String.valueOf(c.getTotemsForWorld(worldKey));

            ctx.drawTextWithShadow(tr, c.totemsPoppedString, drawX, y, opaque(c.labelColor));
            drawX += tr.getWidth(c.totemsPoppedString);

            ctx.drawTextWithShadow(tr, value, drawX, y, opaque(c.valueColor));
        }
    }
}
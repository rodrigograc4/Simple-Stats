package com.rodrigograc4.windrose.config;

import java.util.HashMap;
import java.util.Map;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;

@Config(name = "windrose")
public class WindRoseConfig implements ConfigData {

    @ConfigEntry.Gui.Excluded
    public static WindRoseConfig INSTANCE;

    @ConfigEntry.Gui.Tooltip
    public boolean statsEnabled = true;

    // ===== HUD POSITION =====

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public HudPosition hudPosition = HudPosition.TOP_LEFT;

    public enum HudPosition {
        TOP_LEFT,
        HOTBAR_TOP   
    }

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public float padding = 3.0F;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public float linepadding = 3.0F;

    // ===== COLORS =====

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker
    public int labelColor = 0xFF9D05; 

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.ColorPicker
    public int valueColor = 0xFFFFFF; 

    // ===== DAY =====

    @ConfigEntry.Gui.Tooltip
    public boolean showDayCount = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
    public int dayCountOffset = 1;

    @ConfigEntry.Gui.Tooltip
    public String dayCountString = "Day: ";

    @ConfigEntry.Gui.Tooltip
    public boolean showHours = true;

    // ===== COORDS =====

    @ConfigEntry.Gui.Tooltip
    public boolean showCoords = true;

    @ConfigEntry.Gui.Tooltip
    public boolean coordsUseBrackets = false;

    @ConfigEntry.Gui.Tooltip
    public String coordsString = "XYZ: ";

    // ===== DIRECTION =====

    @ConfigEntry.Gui.Tooltip
    public boolean showDirection = true;

    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public DirectionMode directionMode = DirectionMode.CARDINAL;

    public enum DirectionMode {
        CARDINAL,   // North / South / East / West
        AXIS        // +X / -X / +Z / -Z
    }

    @ConfigEntry.Gui.Tooltip
    public String directionLabel = "↑ ";

    // ===== TOTEMS =====

    @ConfigEntry.Gui.Tooltip
    public boolean showTotemsPopped = true;

    @ConfigEntry.Gui.Tooltip
    public String totemsPoppedString = "Totems Popped: ";
    
    @ConfigEntry.Gui.Excluded
    public Map<String, Integer> totemsPerWorld = new HashMap<>();

    public int getTotemsForWorld(String worldName) {
        return totemsPerWorld.getOrDefault(worldName, 0);
    }
    
    public void incrementTotems(String worldName) {
        totemsPerWorld.merge(worldName, 1, Integer::sum);
        save();
    }
    
    public static void save() {
        AutoConfig.getConfigHolder(WindRoseConfig.class).save();
    }

    public static void init() {
        AutoConfig.register(WindRoseConfig.class, JanksonConfigSerializer::new);
        INSTANCE = AutoConfig.getConfigHolder(WindRoseConfig.class).getConfig();
    }
}

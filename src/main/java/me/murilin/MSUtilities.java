package me.murilin;

import org.bukkit.plugin.java.JavaPlugin;

public class MSUtilities extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getPluginManager().getPlugin("MSEssentials").onDisable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}

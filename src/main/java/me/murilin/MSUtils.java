package me.murilin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class MSUtils extends JavaPlugin {

    enum STATE {READING, LOADBEFORE, SOFTDEPEND, DEPEND, COMMANDS}


    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "[MSUtils] Applying modules configurations"));
        Plugin plugin = getServer().getPluginManager().getPlugin("MSEssentials");
        if(plugin != null) {
            plugin.getPluginLoader().disablePlugin(plugin);
            plugin.onDisable();
        }

        writeModulesConfig();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public void writeModulesConfig() {
        try {
            String plugins = getServer().getPluginManager().getPlugin("MSUtils").getDataFolder().getCanonicalPath();
            plugins = plugins.substring(0, plugins.indexOf("\\MSUtils"));
            String path = plugins + '\\' + "MSEssentials" + '\\' + "modules";
            File file = new File(path);
            File pluginsFolder = new File(plugins);
            JarFile MSEssentials = null;
            boolean pathCreated = file.exists();
            if (!pathCreated)
                pathCreated = file.mkdirs();
            if (pathCreated) {
                String[] files = file.list();
                String MSEssentialsName = null;
                String[] contentsPluginFolder = pluginsFolder.list();
                if (contentsPluginFolder != null && contentsPluginFolder.length != 0) {
                    for (String plugin : contentsPluginFolder)
                        if (plugin.endsWith(".jar") && plugin.contains("MSEssentials")) {
                            MSEssentialsName = plugin;
                            MSEssentials = new JarFile(plugins + '\\' + plugin);
                            break;
                        }
                    InputStream inStream = null;
                    OutputStream ouStream = null;
                    if (MSEssentials != null) {
                        for (Enumeration<JarEntry> entries = MSEssentials.entries(); entries.hasMoreElements(); ) {
                            JarEntry currentEntry = entries.nextElement();
                            if (currentEntry.getName().equals("plugin.yml")) {
                                inStream = new ZipFile(plugins + '\\' + MSEssentialsName).getInputStream(currentEntry);
                                ouStream = Files.newOutputStream(Path.of(plugins + '\\' + MSEssentialsName));
                                break;
                            }
                        }
                        BufferedReader MSEssentialsPluginReader = null;
                        BufferedWriter MSEssentialsPluginWriter = null;
                        if (inStream != null && ouStream != null) {
                            MSEssentialsPluginReader = new BufferedReader(new InputStreamReader(inStream, StandardCharsets.UTF_8));
                            MSEssentialsPluginWriter = new BufferedWriter(new OutputStreamWriter(ouStream, StandardCharsets.UTF_8));
                        }
                        if (files != null && files.length != 0) {
                            for (String s : files) {
                                if (s.endsWith(".jar")) {
                                    JarFile currentModule = new JarFile(path + '\\' + s);
                                    for (Enumeration<JarEntry> entries = currentModule.entries(); entries.hasMoreElements(); ) {
                                        JarEntry entry = entries.nextElement();
                                        String currentFile = entry.getName();
                                        if (currentFile.equals("module.yml")) {
                                            STATE state = STATE.READING;
                                            InputStream in = new ZipFile(path + '\\' + s).getInputStream(entry);
                                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                                            String line;
                                            String lineInsidePlugin;
                                            int lineNumber = -1;
                                            int counter = 0;
                                            while ((line = bufferedReader.readLine()) != null) {
                                                switch (state) {
                                                    case READING:
                                                        if (line.contains("loadbefore:")) state = STATE.LOADBEFORE;
                                                        else if (line.contains("softdepend:")) state = STATE.SOFTDEPEND;
                                                        else if (line.contains("depend:")) state = STATE.DEPEND;
                                                        else if (line.contains("commands")) state = STATE.COMMANDS;
                                                        break;
                                                    case LOADBEFORE:
                                                        boolean hasAlready = false;
                                                        if(MSEssentialsPluginReader != null)
                                                            while ((lineInsidePlugin = MSEssentialsPluginReader.readLine()) != null) {
                                                                if (lineInsidePlugin.contains("loadbefore:")) {
                                                                    hasAlready = true;

                                                                }
                                                            }
                                                }
                                                counter++;
//                                if (line.contains("main:")) {
//                                    if (line.contains(": "))
//                                        line = line.substring(line.indexOf(": ") + 2);
//                                    else
//                                        line = line.substring(line.indexOf(":") + 2);
//                                    break;
//                                } else if (line.contains("name:")) {
//                                    if (line.contains(": "))
//                                        name = line.substring(line.indexOf(": ") + 2);
//                                    else
//                                        name = line.substring(line.indexOf(":") + 2);
//                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
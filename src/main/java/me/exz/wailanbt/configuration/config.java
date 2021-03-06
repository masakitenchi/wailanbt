package me.exz.wailanbt.configuration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.exz.wailanbt.handler.NBTHandler;
import me.exz.wailanbt.reference.Reference;
import me.exz.wailanbt.util.LogHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class config {
    public static JsonObject configJson;
    public static File modConfigurationDirectory;
    private static File configDir;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init(File dir, EntityPlayer player) {
        configDir = new File(dir, Reference.MOD_ID);
        if (!configDir.exists()) {
            try {
                configDir.mkdir();
                    File defaultFile = new File(configDir, "default.json");
                    defaultFile.createNewFile();
                    LogHelper.info("Empty config created");
                player.addChatMessage(new ChatComponentTranslation("wailanbt.info.CreateEmptyConfig"));

            } catch (Exception e) {
                LogHelper.info("Create config file failed");
                player.addChatMessage(new ChatComponentTranslation("wailanbt.info.FailCreateEmptyConfig"));
                e.printStackTrace();
            }
        }
        loadConfig(player);
    }

    public static void loadConfig(EntityPlayer player) {
        configJson = new JsonObject();
        NBTHandler.manager = new ScriptEngineManager(null);
        NBTHandler.engine = NBTHandler.manager.getEngineByName("javascript");
        NBTHandler.scriptSet = new HashSet<String>();
//        try {
//            NBTHandler.engine.eval("var names={}");
//        } catch (ScriptException e) {
//            e.printStackTrace();
//        }
//        for (Object item : Item.itemRegistry) {
//            String ID = String.valueOf(Item.itemRegistry.getIDForObject(item));
//            String name = StatCollector.translateToLocal(((Item)item).getUnlocalizedName()+".name").trim();
//            try {
//                NBTHandler.engine.eval("names['"+ID+"']='"+name+"'");
//            } catch (ScriptException e) {
//                e.printStackTrace();
//            }
//        }
        File[] configFiles = configDir.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });
        if (!(configFiles == null)) {
            for (File configFile : configFiles) {
                if (configFile.isFile()) {
                    try {
                        InputStream inputStream = new FileInputStream(configFile);
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        JsonParser parser = new JsonParser();
                        JsonElement jsonElementCurrent = parser.parse(inputStreamReader);
                        if (jsonElementCurrent.isJsonObject()) {
                            JsonObject jsonObjectCurrent = jsonElementCurrent.getAsJsonObject();
                            mergeJson(jsonObjectCurrent);
                        } else {
                            LogHelper.error("Parse " + configFile.getName() + " failed");
                            player.addChatComponentMessage(new ChatComponentText(String.format(StatCollector.translateToLocal("wailanbt.info.JsonContentError"),configFile.getName())));
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                        LogHelper.error("Error parsing file '" + configFile.getName() + "'. Possible error: " + e.getCause().getMessage());
                        player.addChatComponentMessage(new ChatComponentText(String.format(StatCollector.translateToLocal("wailanbt.info.ParsingError"),configFile.getName())));
                    }
                }
            }
        }
        LogHelper.info("Config Loaded");
        System.out.println(configJson);
        player.addChatMessage(new ChatComponentTranslation("wailanbt.info.ConfigLoaded"));
    }

    private static void mergeJson(JsonObject jsonObjectCurrent) {
        Set<Map.Entry<String, JsonElement>> holdItemA = jsonObjectCurrent.entrySet();
        try {
            for (Map.Entry<String, JsonElement> holdItem : holdItemA) {
                if (configJson.has(holdItem.getKey())) {
                    JsonObject configJsonHoldItem = configJson.get(holdItem.getKey()).getAsJsonObject();
                    Set<Map.Entry<String, JsonElement>> teA = holdItem.getValue().getAsJsonObject().entrySet();
                    for (Map.Entry<String, JsonElement> te : teA) {
                        if (!configJsonHoldItem.has(te.getKey())) {
                            configJsonHoldItem.add(te.getKey(), te.getValue());
                        }
                    }
                    configJson.remove(holdItem.getKey());
                    configJson.add(holdItem.getKey(), configJsonHoldItem);
                } else {
                    configJson.add(holdItem.getKey(), holdItem.getValue());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


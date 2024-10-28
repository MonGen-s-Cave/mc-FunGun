package hu.kxtsoo.fungun.util;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import hu.kxtsoo.fungun.FunGun;
import hu.kxtsoo.fungun.abilities.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigUtil {

    private final JavaPlugin plugin;
    public static ConfigUtil configUtil;
    private YamlDocument config;
    private YamlDocument effects;
    private YamlDocument guis;
    private YamlDocument messages;
    private final Map<String, YamlDocument> effectsMap = new HashMap<>();
    private final Map<String, YamlDocument> abilitiesMap = new HashMap<>();

    public ConfigUtil(JavaPlugin plugin) {
        this.plugin = plugin;
        setupConfig();
        loadEffects();
        loadAbilities();
        setupGUIs();
        setupMessages();
    }

    public void setupConfig() {
        try {
            File configFile = new File(plugin.getDataFolder(), "config.yml");
            if (!configFile.exists()) {
                plugin.saveResource("config.yml", false);
            }

            config = YamlDocument.create(configFile,
                    Objects.requireNonNull(plugin.getResource("config.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setKeepAll(true)
                            .setVersioning(new BasicVersioning("config-version")).build());

            config.update();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error loading or creating config.yml: " + ex.getMessage());
        }
    }

    public void loadEffects() {
        File effectsFolder = new File(plugin.getDataFolder(), "effects");
        if (!effectsFolder.exists()) {
            effectsFolder.mkdirs();
        }

        effectsMap.clear();

        String[] effectFiles = {
                "abyssaldepths.yml", "aqua.yml", "aurora.yml", "cherryblossom.yml", "cosmic.yml", "crimsonflare.yml",
                "crystalfrost.yml", "eclipse.yml", "embers.yml", "fiery.yml", "frosty.yml",
                "galactic.yml", "glaregrove.yml", "glaremoss.yml", "hellfire.yml",
                "junglespirit.yml", "lava.yml", "magicforest.yml", "mystical.yml",
                "mythicflames.yml", "netherwisp.yml", "phantomwhirl.yml", "soulstorm.yml",
                "sparklingglow.yml", "stormy.yml", "tempestfury.yml", "twilightblossom.yml",
                "wardenfury.yml", "wildfiretempest.yml", "wispflight.yml", "wizardsfantasy.yml"
        };

        for (String effectFileName : effectFiles) {
            File effectFile = new File(effectsFolder, effectFileName);
            if (!effectFile.exists()) {
                plugin.saveResource("effects/" + effectFileName, false);
            }
        }

        File[] loadedEffectFiles = effectsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (loadedEffectFiles != null && loadedEffectFiles.length > 0) {
            for (File effectFile : loadedEffectFiles) {
                try {
                    String effectName = effectFile.getName().replace(".yml", "").toUpperCase();
                    YamlDocument effectDocument = YamlDocument.create(effectFile,
                            GeneralSettings.builder().setUseDefaults(false).build(),
                            LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                            UpdaterSettings.builder().setVersioning(new BasicVersioning("effects-version")).build());

                    effectsMap.put(effectName, effectDocument);
                } catch (IOException ex) {
                    plugin.getLogger().severe("Error loading effect file: " + effectFile.getName() + " - " + ex.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("No effect files found in the effects folder.");
        }
    }

    public void loadAbilities() {
        File abilitiesFolder = new File(plugin.getDataFolder(), "abilities");
        if (!abilitiesFolder.exists()) {
            abilitiesFolder.mkdirs();
        }

        abilitiesMap.clear();

        String[] abilitiesFiles = {
                "dash.yml", "explosion.yml", "meteor.yml", "mountflight.yml", "batswarm.yml"
        };

        for (String abilitiesFileName : abilitiesFiles) {
            File effectFile = new File(abilitiesFolder, abilitiesFileName);
            if (!effectFile.exists()) {
                plugin.saveResource("abilities/" + abilitiesFileName, false);
            }
        }

        File[] loadedAbilityFiles = abilitiesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (loadedAbilityFiles != null && loadedAbilityFiles.length > 0) {
            for (File abilityFile : loadedAbilityFiles) {
                try {
                    String abilityName = abilityFile.getName().replace(".yml", "").toUpperCase();
                    YamlDocument abilityDocument = YamlDocument.create(abilityFile,
                            GeneralSettings.builder().setUseDefaults(false).build(),
                            LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                            UpdaterSettings.builder().setVersioning(new BasicVersioning("abilities-version")).build());

                    abilitiesMap.put(abilityName, abilityDocument);
                } catch (IOException ex) {
                    plugin.getLogger().severe("Error loading ability file: " + abilityFile.getName() + " - " + ex.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("No ability files found in the abilities folder.");
        }
    }

    public void setupGUIs() {
        try {
            File guisFile = new File(plugin.getDataFolder(), "guis.yml");
            if (!guisFile.exists()) {
                plugin.saveResource("guis.yml", false);
            }

            guis = YamlDocument.create(guisFile,
                    Objects.requireNonNull(plugin.getResource("guis.yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setKeepAll(true)
                            .setVersioning(new BasicVersioning("guis-version")).build());

            guis.update();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error loading or creating guis.yml: " + ex.getMessage());
        }
    }

    public void setupMessages() {
        generateDefaultLocales();

        String locale = config.getString("locale", "en");
        File messagesFile = new File(plugin.getDataFolder() + File.separator + "locale", locale + ".yml");

        if (!messagesFile.exists()) {
            plugin.saveResource("locale" + File.separator + locale + ".yml", false);
        }

        try {
            messages = YamlDocument.create(messagesFile,
                    Objects.requireNonNull(plugin.getResource("locale/" + locale + ".yml")),
                    GeneralSettings.builder().setUseDefaults(false).build(),
                    LoaderSettings.DEFAULT, DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("messages-version"))
                            .setKeepAll(true)
                            .build());

            messages.update();
        } catch (IOException ex) {
            plugin.getLogger().severe("Error loading or creating message files " + ex.getMessage());
        }
    }

    private void generateDefaultLocales() {
        String[] locales = {"en", "hu"};
        for (String locale : locales) {
            File localeFile = new File(plugin.getDataFolder(), "locale" + File.separator + locale + ".yml");
            if (!localeFile.exists()) {
                plugin.saveResource("locale" + File.separator + locale + ".yml", false);
            }
        }
    }

    public String getMessage(String key) {
        Object messageObj = messages.get(key, "Message not found");

        if (messageObj instanceof String) {
            String message = ChatUtil.colorizeHex((String) messageObj);
            String prefix = ChatUtil.colorizeHex(config.getString("prefix", ""));
            if (message.contains("%prefix%")) {
                return message.replace("%prefix%", prefix);
            }
            return message;
        } else if (messageObj instanceof List) {
            List<String> messageList = (List<String>) messageObj;
            String prefix = ChatUtil.colorizeHex(config.getString("prefix", ""));
            messageList = messageList.stream()
                    .map(ChatUtil::colorizeHex)
                    .map(msg -> msg.contains("%prefix%") ? msg.replace("%prefix%", prefix) : msg)
                    .toList();
            return String.join("\n", messageList);
        }

        return "Invalid message format";
    }

    public YamlDocument getConfig() {
        return config;
    }

    public YamlDocument getEffect(String effectName) {
        return effectsMap.get(effectName.toUpperCase());
    }

    public int getEffectCount() {
        return effectsMap.size();
    }

    public Map<String, YamlDocument> getEffectsMap() {
        return effectsMap;
    }

    public int getEffectCooldown(String effectName) {
        YamlDocument effectConfig = effectsMap.get(effectName.toUpperCase());
        if (effectConfig != null) {
            return effectConfig.getInt("effect.options.cooldown", 5);
        }
        return 5;
    }

    public YamlDocument getAbility(String abilityName) {
        return abilitiesMap.get(abilityName.toUpperCase());
    }

    public Map<String, YamlDocument> getAbilitiesMap() {
        return abilitiesMap;
    }

    public int getAbilityCount() {
        return abilitiesMap.size();
    }

    public AbilityHandler getAbilityHandler(String abilityName) {
        YamlDocument abilityConfig = abilitiesMap.get(abilityName.toUpperCase());
        if (abilityConfig != null) {
            switch (abilityName.toUpperCase()) {
                case "DASH":
                    return new DashAbility(FunGun.getInstance().getCooldownManager());
                case "EXPLOSION":
                    return new ExplosionAbility(FunGun.getInstance().getCooldownManager(), FunGun.getInstance());
                case "METEOR":
                    return new MeteorAbility(FunGun.getInstance().getCooldownManager(), FunGun.getInstance());
                case "MOUNTFLIGHT":
                    return new MountFlightAbility(FunGun.getInstance().getCooldownManager(), FunGun.getInstance());
                case "BATSWARM":
                    return new BatSwarmAbility(FunGun.getInstance().getCooldownManager(), FunGun.getInstance());
                default:
                    FunGun.getInstance().getLogger().warning("No handler found for ability: " + abilityName);
                    return null;
            }
        }
        return null;
    }

    public YamlDocument getGUIs() {
        return guis;
    }

    public void reloadConfig() {
        setupConfig();
        setupMessages();
        setupGUIs();
    }

    public void reloadEffects() {
        loadEffects();
    }

    public void reloadAbilities() {
        loadAbilities();
    }
}
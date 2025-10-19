package com.xolby.economy.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ConfigGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigGenerator.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = Paths.get("config/economy/config.json");

    public static ConfigData loadOrCreate() {
        try {
            // Créer le dossier config/economy s'il n'existe pas
            Files.createDirectories(CONFIG_PATH.getParent());

            if (Files.exists(CONFIG_PATH)) {
                LOGGER.info("Chargement de la configuration depuis config.json");
                return loadConfig();
            } else {
                LOGGER.info("Création du fichier config.json par défaut");
                return createDefaultConfig();
            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la gestion du fichier config.json", e);
            return createDefaultConfigData();
        }
    }

    private static ConfigData loadConfig() {
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData config = GSON.fromJson(reader, ConfigData.class);
            if (config == null || config.shopItems == null || config.savingsTiers == null) {
                LOGGER.warn("Configuration invalide, recréation du fichier par défaut");
                return createDefaultConfig();
            }
            return config;
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la lecture du config.json", e);
            return createDefaultConfig();
        }
    }

    private static ConfigData createDefaultConfig() {
        ConfigData config = createDefaultConfigData();
        saveConfig(config);
        return config;
    }

    private static ConfigData createDefaultConfigData() {
        ConfigData config = new ConfigData();

        // Configuration des items du shop
        config.shopItems = new LinkedHashMap<>();
        
        // Minerais et lingots
        config.shopItems.put("minecraft:diamond", 100.0);
        config.shopItems.put("minecraft:iron_ingot", 10.0);
        config.shopItems.put("minecraft:gold_ingot", 20.0);
        config.shopItems.put("minecraft:emerald", 50.0);
        config.shopItems.put("minecraft:netherite_ingot", 500.0);
        config.shopItems.put("minecraft:copper_ingot", 5.0);
        
        // Ressources de base
        config.shopItems.put("minecraft:coal", 2.0);
        config.shopItems.put("minecraft:redstone", 3.0);
        config.shopItems.put("minecraft:lapis_lazuli", 4.0);
        config.shopItems.put("minecraft:quartz", 3.0);
        
        // Nourriture
        config.shopItems.put("minecraft:wheat", 1.0);
        config.shopItems.put("minecraft:apple", 3.0);
        config.shopItems.put("minecraft:carrot", 1.5);
        config.shopItems.put("minecraft:potato", 1.5);
        config.shopItems.put("minecraft:beef", 5.0);
        config.shopItems.put("minecraft:porkchop", 5.0);
        config.shopItems.put("minecraft:chicken", 4.0);
        config.shopItems.put("minecraft:mutton", 4.0);
        config.shopItems.put("minecraft:bread", 2.0);
        
        // Bois
        config.shopItems.put("minecraft:oak_log", 2.0);
        config.shopItems.put("minecraft:spruce_log", 2.0);
        config.shopItems.put("minecraft:birch_log", 2.0);
        config.shopItems.put("minecraft:jungle_log", 2.5);
        config.shopItems.put("minecraft:acacia_log", 2.0);
        config.shopItems.put("minecraft:dark_oak_log", 2.5);
        
        // Blocs de construction
        config.shopItems.put("minecraft:stone", 0.5);
        config.shopItems.put("minecraft:cobblestone", 0.3);
        config.shopItems.put("minecraft:dirt", 0.1);
        config.shopItems.put("minecraft:sand", 0.5);
        config.shopItems.put("minecraft:gravel", 0.5);
        config.shopItems.put("minecraft:glass", 1.0);
        
        // Items rares
        config.shopItems.put("minecraft:ender_pearl", 25.0);
        config.shopItems.put("minecraft:blaze_rod", 15.0);
        config.shopItems.put("minecraft:ghast_tear", 20.0);
        config.shopItems.put("minecraft:slime_ball", 8.0);

        // Configuration des paliers d'épargne
        config.savingsTiers = new ArrayList<>();

        SavingsTier bronze = new SavingsTier();
        bronze.name = "Bronze";
        bronze.minAmount = 0.0;
        bronze.interestRate = 0.01; // 1%
        bronze.description = "Palier de départ - Taux d'intérêt de 1%";
        bronze.color = "§7"; // Gris
        config.savingsTiers.add(bronze);

        SavingsTier argent = new SavingsTier();
        argent.name = "Argent";
        argent.minAmount = 1000.0;
        argent.interestRate = 0.02; // 2%
        argent.description = "Bon début - Taux d'intérêt de 2%";
        argent.color = "§f"; // Blanc
        config.savingsTiers.add(argent);

        SavingsTier or = new SavingsTier();
        or.name = "Or";
        or.minAmount = 5000.0;
        or.interestRate = 0.03; // 3%
        or.description = "Épargnant confirmé - Taux d'intérêt de 3%";
        or.color = "§6"; // Or
        config.savingsTiers.add(or);

        SavingsTier platine = new SavingsTier();
        platine.name = "Platine";
        platine.minAmount = 10000.0;
        platine.interestRate = 0.05; // 5%
        platine.description = "Expert financier - Taux d'intérêt de 5%";
        platine.color = "§b"; // Cyan
        config.savingsTiers.add(platine);

        SavingsTier diamant = new SavingsTier();
        diamant.name = "Diamant";
        diamant.minAmount = 25000.0;
        diamant.interestRate = 0.07; // 7%
        diamant.description = "Elite économique - Taux d'intérêt de 7%";
        diamant.color = "§3"; // Bleu foncé
        config.savingsTiers.add(diamant);

        SavingsTier emeraude = new SavingsTier();
        emeraude.name = "Émeraude";
        emeraude.minAmount = 50000.0;
        emeraude.interestRate = 0.10; // 10%
        emeraude.description = "Magnat millionnaire - Taux d'intérêt de 10%";
        emeraude.color = "§a"; // Vert
        config.savingsTiers.add(emeraude);

        // Configuration générale
        config.settings = new Settings();
        config.settings.currencySymbol = "$";
        config.settings.sellPriceMultiplier = 0.75; // 75% du prix d'achat
        config.settings.startingMoney = 100.0;
        config.settings.maxTransferAmount = 100000.0;

        return config;
    }

    public static void saveConfig(ConfigData config) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
                LOGGER.info("Fichier config.json sauvegardé avec succès");
            }
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la sauvegarde du config.json", e);
        }
    }

    public static class ConfigData {
        public Map<String, Double> shopItems;
        public List<SavingsTier> savingsTiers;
        public Settings settings;
    }

    public static class SavingsTier {
        public String name;
        public double minAmount;
        public double interestRate;
        public String description;
        public String color;
    }

    public static class Settings {
        public String currencySymbol;
        public double sellPriceMultiplier;
        public double startingMoney;
        public double maxTransferAmount;
    }
}
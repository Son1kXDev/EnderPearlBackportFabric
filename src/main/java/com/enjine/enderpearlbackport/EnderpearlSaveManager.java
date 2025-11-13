package com.enjine.enderpearlbackport;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class EnderpearlSaveManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "saved_pearls.json";

    private static final Map<UUID, List<EnderpearlData>> SAVED = new HashMap<>();
    private static boolean loaded = false;

    private static Path getConfigFile() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path modDir = configDir.resolve("enderpearlbackport_fabric");
        try {
            Files.createDirectories(modDir);
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        return modDir.resolve(FILE_NAME);
    }

    private static synchronized void loadIfNeeded() {
        if (loaded) return;
        loaded = true;

        Path file = getConfigFile();
        if (!Files.exists(file)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) return;

            JsonObject rootObj = root.getAsJsonObject();
            SAVED.clear();

            for (Map.Entry<String, JsonElement> entry : rootObj.entrySet()) {
                UUID playerId = UUID.fromString(entry.getKey());
                JsonArray array = entry.getValue().getAsJsonArray();
                List<EnderpearlData> pearls = new ArrayList<>();

                for (JsonElement el : array) {
                    JsonObject obj = el.getAsJsonObject();

                    UUID pearlId = UUID.fromString(obj.get("pearlId").getAsString());
                    String worldStr = obj.get("world").getAsString();
                    Identifier worldId = Identifier.tryParse(worldStr);
                    if (worldId == null) continue;

                    RegistryKey<World> worldKey =
                            RegistryKey.of(RegistryKeys.WORLD, worldId);

                    JsonArray pos = obj.getAsJsonArray("position");
                    JsonArray vel = obj.getAsJsonArray("velocity");

                    Vec3d position = new Vec3d(
                            pos.get(0).getAsDouble(),
                            pos.get(1).getAsDouble(),
                            pos.get(2).getAsDouble()
                    );

                    Vec3d velocity = new Vec3d(
                            vel.get(0).getAsDouble(),
                            vel.get(1).getAsDouble(),
                            vel.get(2).getAsDouble()
                    );

                    pearls.add(new EnderpearlData(
                            pearlId,
                            playerId,
                            worldKey,
                            position,
                            velocity
                    ));
                }

                if (!pearls.isEmpty()) {
                    SAVED.put(playerId, pearls);
                }
            }

        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private static synchronized void saveNow() {
        Path file = getConfigFile();
        JsonObject root = new JsonObject();

        for (Map.Entry<UUID, List<EnderpearlData>> entry : SAVED.entrySet()) {
            JsonArray arr = new JsonArray();
            for (EnderpearlData data : entry.getValue()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("pearlId", data.pearlId.toString());
                obj.addProperty("world", data.worldKey.getValue().toString());

                JsonArray pos = new JsonArray();
                pos.add(data.position.x);
                pos.add(data.position.y);
                pos.add(data.position.z);
                obj.add("position", pos);

                JsonArray vel = new JsonArray();
                vel.add(data.velocity.x);
                vel.add(data.velocity.y);
                vel.add(data.velocity.z);
                obj.add("velocity", vel);

                arr.add(obj);
            }
            root.add(entry.getKey().toString(), arr);
        }

        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            GSON.toJson(root, writer);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }


    public static synchronized void savePearlsForPlayer(
            MinecraftServer server,
            UUID playerId,
            List<EnderpearlData> pearls
    ) {
        loadIfNeeded();
        if (pearls.isEmpty()) {
            SAVED.remove(playerId);
        } else {
            SAVED.put(playerId, new ArrayList<>(pearls));
        }
        saveNow();
    }


    public static synchronized List<EnderpearlData> popPearlsForPlayer(
            MinecraftServer server,
            UUID playerId
    ) {
        loadIfNeeded();
        List<EnderpearlData> list = SAVED.remove(playerId);
        if (list == null) {
            return Collections.emptyList();
        }
        saveNow();
        return list;
    }
}

package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.*;

public class EnderpearlPersistentState extends PersistentState {

    private final Map<UUID, List<EnderpearlRecord>> data = new HashMap<>();

    public static EnderpearlPersistentState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                EnderpearlPersistentState::fromNbt,
                EnderpearlPersistentState::new,
                "enderpearl_backport"
        );
    }

    public void savePearls(UUID playerId, List<EnderpearlRecord> pearls) {
        if (pearls == null || pearls.isEmpty()) data.remove(playerId);
        else data.put(playerId, List.copyOf(pearls));
        markDirty();
    }

    public List<EnderpearlRecord> popPearls(UUID playerId) {
        List<EnderpearlRecord> list = data.remove(playerId);
        markDirty();
        return list == null ? List.of() : list;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound playersNbt = new NbtCompound();
        for (Map.Entry<UUID, List<EnderpearlRecord>> entry : data.entrySet()) {
            NbtList list = new NbtList();
            for (EnderpearlRecord r : entry.getValue()) {
                NbtCompound pearlNbt = new NbtCompound();
                pearlNbt.putUuid("pearlId", r.pearlId());
                pearlNbt.putString("dim", r.dimensionId());
                pearlNbt.putDouble("x", r.x());
                pearlNbt.putDouble("y", r.y());
                pearlNbt.putDouble("z", r.z());
                pearlNbt.putDouble("vx", r.vx());
                pearlNbt.putDouble("vy", r.vy());
                pearlNbt.putDouble("vz", r.vz());
                list.add(pearlNbt);
            }
            playersNbt.put(entry.getKey().toString(), list);
        }
        nbt.put("players", playersNbt);
        return nbt;
    }

    public static EnderpearlPersistentState fromNbt(NbtCompound nbt) {
        EnderpearlPersistentState state = new EnderpearlPersistentState();
        if (nbt.contains("players")) {
            NbtCompound playersNbt = nbt.getCompound("players");
            for (String key : playersNbt.getKeys()) {
                UUID playerId = UUID.fromString(key);
                NbtList list = playersNbt.getList(key, 10);
                List<EnderpearlRecord> pearls = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    NbtCompound pearlNbt = list.getCompound(i);
                    pearls.add(new EnderpearlRecord(
                            pearlNbt.getUuid("pearlId"),
                            pearlNbt.getString("dim"),
                            pearlNbt.getDouble("x"), pearlNbt.getDouble("y"), pearlNbt.getDouble("z"),
                            pearlNbt.getDouble("vx"), pearlNbt.getDouble("vy"), pearlNbt.getDouble("vz")
                    ));
                }
                state.data.put(playerId, pearls);
            }
        }
        return state;
    }
}

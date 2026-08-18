package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.*;

public class EnderpearlPersistentState extends PersistentState {

    private final Map<UUID, List<EnderpearlRecord>> data = new HashMap<>();
    private boolean migratedToTickets = false;

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

    public List<EnderpearlRecord> getPearls(UUID playerId) {
        List<EnderpearlRecord> list = data.get(playerId);
        return list == null ? List.of() : new ArrayList<>(list);
    }

    public void clearPearls(UUID playerId) {
        data.remove(playerId);
        markDirty();
    }

    public boolean isMigratedToTickets() {
        return migratedToTickets;
    }

    public void setMigratedToTickets(boolean value) {
        this.migratedToTickets = value;
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean("migrated_to_tickets", migratedToTickets);

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

        state.migratedToTickets = nbt.getBoolean("migrated_to_tickets");

        if (nbt.contains("players")) {
            NbtCompound playersNbt = nbt.getCompound("players");
            for (String key : playersNbt.getKeys()) {
                UUID playerId;
                try { playerId = UUID.fromString(key); }
                catch (Exception e) { continue; }

                NbtList list = playersNbt.getList(key, 10);
                List<EnderpearlRecord> pearls = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    NbtCompound pearlNbt = list.getCompound(i);
                    UUID pearlId;
                    try { pearlId = pearlNbt.getUuid("pearlId"); }
                    catch (Exception e) { continue; }

                    pearls.add(new EnderpearlRecord(
                            pearlId,
                            pearlNbt.getString("dim"),
                            pearlNbt.getDouble("x"), pearlNbt.getDouble("y"), pearlNbt.getDouble("z"),
                            pearlNbt.getDouble("vx"), pearlNbt.getDouble("vy"), pearlNbt.getDouble("vz")
                    ));
                }
                if (!pearls.isEmpty()) state.data.put(playerId, pearls);
            }
        }
        return state;
    }
}

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
        else data.put(playerId, new ArrayList<>(pearls));
        markDirty();
    }

    public List<EnderpearlRecord> getPearls(UUID playerId) {
        List<EnderpearlRecord> list = data.get(playerId);
        return list == null ? Collections.emptyList() : new ArrayList<>(list);
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

        for (Map.Entry<UUID, List<EnderpearlRecord>> entry : data.entrySet()) {
            NbtList arr = new NbtList();
            for (EnderpearlRecord r : entry.getValue()) {
                NbtCompound t = new NbtCompound();
                t.putString("pearlId", r.pearlId().toString());
                t.putString("dim", r.dimensionId());
                t.putDouble("x", r.x()); t.putDouble("y", r.y()); t.putDouble("z", r.z());
                t.putDouble("vx", r.vx()); t.putDouble("vy", r.vy()); t.putDouble("vz", r.vz());
                arr.add(t);
            }
            nbt.put(entry.getKey().toString(), arr);
        }
        return nbt;
    }

    public static EnderpearlPersistentState fromNbt(NbtCompound nbt) {
        EnderpearlPersistentState s = new EnderpearlPersistentState();

        s.migratedToTickets = nbt.getBoolean("migrated_to_tickets");

        if (nbt.contains("players")) {
            NbtCompound playersNbt = nbt.getCompound("players");
            for (String key : playersNbt.getKeys()) {
                UUID playerId;
                try { playerId = UUID.fromString(key); }
                catch (Exception e) { continue; }

                NbtList list = playersNbt.getList(key, 10);
                List<EnderpearlRecord> pearls = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    NbtCompound t = list.getCompound(i);
                    UUID pearlId;
                    try { pearlId = UUID.fromString(t.getString("pearlId")); }
                    catch (Exception e) {
                        try { pearlId = t.getUuid("pearlId"); }
                        catch (Exception e2) { continue; }
                    }

                    pearls.add(new EnderpearlRecord(
                            pearlId,
                            t.getString("dim"),
                            t.getDouble("x"), t.getDouble("y"), t.getDouble("z"),
                            t.getDouble("vx"), t.getDouble("vy"), t.getDouble("vz")
                    ));
                }
                if (!pearls.isEmpty()) s.data.put(playerId, pearls);
            }
        }

        for (String playerKey : nbt.getKeys()) {
            if (playerKey.equals("migrated_to_tickets") || playerKey.equals("players")) continue;

            UUID playerId;
            try { playerId = UUID.fromString(playerKey); }
            catch (Exception e) { continue; }

            NbtList arr = nbt.getList(playerKey, 10);
            List<EnderpearlRecord> pearls = new ArrayList<>();

            for (int i = 0; i < arr.size(); i++) {
                NbtCompound t = arr.getCompound(i);

                UUID pearlId;
                try { pearlId = UUID.fromString(t.getString("pearlId")); }
                catch (Exception e) { continue; }

                pearls.add(new EnderpearlRecord(
                        pearlId,
                        t.getString("dim"),
                        t.getDouble("x"), t.getDouble("y"), t.getDouble("z"),
                        t.getDouble("vx"), t.getDouble("vy"), t.getDouble("vz")
                ));
            }

            if (!pearls.isEmpty()) s.data.put(playerId, pearls);
        }

        return s;
    }
}

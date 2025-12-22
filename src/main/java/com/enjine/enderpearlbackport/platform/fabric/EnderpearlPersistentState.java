package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.*;

public class EnderpearlPersistentState extends PersistentState {

    public static final Type<EnderpearlPersistentState> TYPE =
            new Type<>(
                    EnderpearlPersistentState::new,
                    EnderpearlPersistentState::fromNbt,
                    null
            );

    private final Map<UUID, List<EnderpearlRecord>> data = new HashMap<>();

    public static EnderpearlPersistentState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE, "enderpearl_backport");
    }

    public void savePearls(UUID playerId, List<EnderpearlRecord> pearls) {
        if (pearls == null || pearls.isEmpty()) data.remove(playerId);
        else data.put(playerId, new ArrayList<>(pearls));
        markDirty();
    }

    public List<EnderpearlRecord> popPearls(UUID playerId) {
        List<EnderpearlRecord> list = data.remove(playerId);
        if (list == null) return Collections.emptyList();
        markDirty();
        return list;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
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

    public static EnderpearlPersistentState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        EnderpearlPersistentState s = new EnderpearlPersistentState();

        for (String playerKey : nbt.getKeys()) {
            UUID playerId;
            try { playerId = UUID.fromString(playerKey); }
            catch (Exception e) { continue; }

            NbtList arr = nbt.getList(playerKey, NbtElement.COMPOUND_TYPE);
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

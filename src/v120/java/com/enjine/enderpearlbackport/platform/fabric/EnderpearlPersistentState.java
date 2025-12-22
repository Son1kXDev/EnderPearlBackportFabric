package com.enjine.enderpearlbackport.platform.fabric;

import com.enjine.enderpearlbackport.common.data.EnderpearlRecord;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        return nbt;
    }

    public static EnderpearlPersistentState fromNbt(NbtCompound nbt) {
        return new EnderpearlPersistentState();
    }
}

package com.outlook.hxsw.settlements.engine.data;

import com.outlook.hxsw.settlements.SettlementsMain;
import com.outlook.hxsw.settlements.engine.schedule.*;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class SettlementsProxy extends ScheduleController {
    public static SettlementsProxy get(MinecraftServer server) {
        return INSTANCES.computeIfAbsent(server, SettlementsProxy::loadOrCreate);
    }

    public void save() {
        run(getSavingTask());
    }

    public void stopAndSave() {
        stopSidecar();
        try (var dataAccessor = getData()) {
            saveData(dataAccessor.get(), getPhase(), savePath);
        }
    }

    private static final Map<MinecraftServer, SettlementsProxy> INSTANCES = new WeakHashMap<>(1);

    private static final String SAVE_NAME = "settlements.dat";
    private static final String NBT_DATA_KEY = "settlements_data";
    private static final String NBT_PHASE_KEY = "phase";

    private final Path savePath;

    private SettlementsProxy(Path savePath, SettlementsData data, int phase) {
        super(data, phase);
        data.registerToSidecar(getSidecar());
        this.savePath = savePath;
    }

    private static Path getSavePath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve(SAVE_NAME);
    }

    private static SettlementsProxy loadOrCreate(MinecraftServer server) {
        Path savePath = getSavePath(server);
        if (Files.exists(savePath)) {
            return loadFromSave(savePath);
        }
        return create(server, savePath);
    }

    private static SettlementsProxy create(MinecraftServer server, Path savePath) {
        SettlementsData data = new SettlementsData();
        return new SettlementsProxy(savePath, data, 1);
    }

    private static SettlementsProxy loadFromSave(Path savePath) {
        CompoundTag tag;
        try {
            tag = NbtIo.readCompressed(savePath, NbtAccounter.unlimitedHeap());
        } catch (IOException e) {
            SettlementsMain.LOGGER.error("Error reading settlements data", e);
            throw new IllegalStateException("Error reading settlements data");
        }
        int phase = tag.getInt(NBT_PHASE_KEY);
        SettlementsData data = SettlementsData.CODEC.parse(NbtOps.INSTANCE, tag.get(NBT_DATA_KEY)).getOrThrow();

        return new SettlementsProxy(savePath, data, phase);
    }

    private Consumer<Scheduler.Sidecar> getSavingTask() {
        Path savePath = this.savePath;
        return sidecar -> SettlementsProxy.saveData(sidecar.data(), sidecar.getPhase(), savePath);
    }

    private static void saveData(SettlementsData data, int phase, Path savePath) {
        CompoundTag tag = new CompoundTag();
        var savedData = SettlementsData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow();
        tag.put(NBT_DATA_KEY, savedData);
        tag.putInt(NBT_PHASE_KEY, phase);
        try {
            NbtIo.writeCompressed(tag, Files.newOutputStream(savePath));
        } catch (IOException e) {
            SettlementsMain.LOGGER.error("Error reading settlements data", e);
            throw new IllegalStateException("Error reading settlements data");
        }
    }
}

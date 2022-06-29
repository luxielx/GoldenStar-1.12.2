// 
// Decompiled by Procyon v0.5.36
// 

package net.minecraft.server.v1_12_R1;

import co.aikar.util.Counter;
import com.destroystokyo.paper.PaperWorldConfig;
import com.destroystokyo.paper.exception.ServerInternalException;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_12_R1.util.UnsafeList;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.generator.BlockPopulator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Chunk {
    private static Logger e;
    public static ChunkSection a;
    public static ChunkSection EMPTY_CHUNK_SECTION;
    private ChunkSection[] sections;
    private byte[] g;
    private int[] h;
    private boolean[] i;
    private boolean j;
    public World world;
    public int[] heightMap;
    public Long scheduledForUnload;
    private static Logger logger;
    public int locX;
    public int locZ;
    private boolean m;
    public Map<BlockPosition, TileEntity> tileEntities;
    public List<Entity>[] entitySlices;
    public Counter<String> entityCounts;
    public Counter<String> tileEntityCounts;
    PaperLightingQueue.LightingQueue lightingQueue;
    private boolean done;
    private boolean lit;
    private boolean r;
    private boolean s;
    private boolean t;
    private long lastSaved;
    private int v;
    private long w;
    private int x;
    private ConcurrentLinkedQueue<BlockPosition> y;
    public boolean d;
    protected TObjectIntHashMap<Class> entityCount;
    private int[] itemCounts;
    private int[] inventoryEntityCounts;
    private int neighbors;
    public long chunkKey;
    public org.bukkit.Chunk bukkitChunk;
    public boolean mustSave;

    public boolean isLoaded() {
        return this.j;
    }

    private boolean isTicked() {
        return this.r;
    }

    public void setShouldUnload(boolean unload) {
        this.d = unload;
    }

    public boolean isUnloading() {
        return this.d;
    }

    public boolean areNeighborsLoaded(int radius) {
        switch (radius) {
            case 2: {
                return this.neighbors == 33554431;
            }
            case 1: {
                int mask = 473536;
                return (this.neighbors & 0x739C0) == 0x739C0;
            }
            default: {
                throw new UnsupportedOperationException(String.valueOf(radius));
            }
        }
    }

    public void setNeighborLoaded(int x, int z) {
        this.neighbors |= 1 << x * 5 + 12 + z;
    }

    public void setNeighborUnloaded(int x, int z) {
        this.neighbors &= ~(1 << x * 5 + 12 + z);
    }

    public Chunk(World world, int i, int j) {
        this.entityCounts = (Counter<String>) new Counter();
        this.tileEntityCounts = (Counter<String>) new Counter();
        this.lightingQueue = new PaperLightingQueue.LightingQueue(this);
        this.entityCount = (TObjectIntHashMap<Class>) new TObjectIntHashMap();
        this.itemCounts = new int[16];
        this.inventoryEntityCounts = new int[16];
        this.neighbors = 4096;
        this.sections = new ChunkSection[16];
        this.g = new byte[256];
        this.h = new int[256];
        this.i = new boolean[256];
        this.tileEntities = new TileEntityHashMap();
        this.x = 4096;
        this.y = Queues.newConcurrentLinkedQueue();
        this.entitySlices = (List<Entity>[]) new List[16];
        this.world = world;
        this.locX = i;
        this.locZ = j;
        this.heightMap = new int[256];
        for (int k = 0; k < this.entitySlices.length; ++k) {
            this.entitySlices[k] = Collections.synchronizedList(new UnsafeList<Entity>());
        }
        Arrays.fill(this.h, -999);
        Arrays.fill(this.g, (byte) (-1));
        this.bukkitChunk = (org.bukkit.Chunk) new CraftChunk(this);
        this.chunkKey = ChunkCoordIntPair.a(this.locX, this.locZ);
    }

    public Chunk(World world, ChunkSnapshot chunksnapshot, int i, int j) {
        this(world, i, j);
        boolean flag = true;
        boolean flag2 = world.worldProvider.m();
        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                for (int i2 = 0; i2 < 256; ++i2) {
                    IBlockData iblockdata = chunksnapshot.a(k, i2, l);
                    if (iblockdata.getMaterial() != Material.AIR) {
                        int j2 = i2 >> 4;
                        if (this.sections[j2] == Chunk.a) {
                            this.sections[j2] = new ChunkSection(j2 << 4, flag2,
                                    world.chunkPacketBlockController.getPredefinedBlockData(this, j2));
                        }
                        this.sections[j2].setType(k, i2 & 0xF, l, iblockdata);
                    }
                }
            }
        }
    }

    public boolean a(int i, int j) {
        return i == this.locX && j == this.locZ;
    }

    public int e(BlockPosition blockposition) {
        return this.b(blockposition.getX() & 0xF, blockposition.getZ() & 0xF);
    }

    public int b(int i, int j) {
        return this.heightMap[j << 4 | i];
    }

    @Nullable
    private ChunkSection y() {
        for (int i = this.sections.length - 1; i >= 0; --i) {
            if (this.sections[i] != Chunk.a) {
                return this.sections[i];
            }
        }
        return null;
    }

    public int g() {
        ChunkSection chunksection = this.y();
        return (chunksection == null) ? 0 : chunksection.getYPosition();
    }

    public ChunkSection[] getSections() {
        return this.sections;
    }

    public void initLighting() {
        int i = this.g();
        this.v = Integer.MAX_VALUE;
        for (int j = 0; j < 16; ++j) {
            for (int k = 0; k < 16; ++k) {
                this.h[j + (k << 4)] = -999;
                int l = i + 16;
                while (l > 0) {
                    if (this.d(j, l - 1, k) == 0) {
                        --l;
                    } else {
                        if ((this.heightMap[k << 4 | j] = l) < this.v) {
                            this.v = l;
                            break;
                        }
                        break;
                    }
                }
                if (this.world.worldProvider.m()) {
                    l = 15;
                    int i2 = i + 16 - 1;
                    do {
                        int j2 = this.d(j, i2, k);
                        if (j2 == 0 && l != 15) {
                            j2 = 1;
                        }
                        l -= j2;
                        if (l > 0) {
                            ChunkSection chunksection = this.sections[i2 >> 4];
                            if (chunksection == Chunk.a) {
                                continue;
                            }
                            chunksection.a(j, i2 & 0xF, k, l);
                            this.world.m(new BlockPosition((this.locX << 4) + j, i2, (this.locZ << 4) + k));
                        }
                    } while (--i2 > 0 && l > 0);
                }
            }
        }
        this.s = true;
    }

    private void d(int i, int j) {
        this.i[i + j * 16] = true;
        this.m = true;
    }

    private void h(boolean flag) {
        this.world.methodProfiler.a("recheckGaps");
        if (this.areNeighborsLoaded(1)) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    if (this.i[i + j * 16]) {
                        this.i[i + j * 16] = false;
                        int k = this.b(i, j);
                        int l = this.locX * 16 + i;
                        int i2 = this.locZ * 16 + j;
                        int j2 = Integer.MAX_VALUE;
                        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                            j2 = Math.min(j2,
                                    this.world.d(l + enumdirection.getAdjacentX(), i2 + enumdirection.getAdjacentZ()));
                        }
                        this.b(l, i2, j2);
                        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                            this.b(l + enumdirection.getAdjacentX(), i2 + enumdirection.getAdjacentZ(), k);
                        }
                        if (flag) {
                            this.world.methodProfiler.b();
                            return;
                        }
                    }
                }
            }
            this.m = false;
        }
        this.world.methodProfiler.b();
    }

    private void b(int i, int j, int k) {
        int l = this.world.getHighestBlockYAt(new BlockPosition(i, 0, j)).getY();
        if (l > k) {
            this.a(i, j, k, l + 1);
        } else if (l < k) {
            this.a(i, j, l, k + 1);
        }
    }

    private void a(int i, int j, int k, int l) {
        if (l > k && this.areNeighborsLoaded(1)) {
            for (int i2 = k; i2 < l; ++i2) {
                this.world.c(EnumSkyBlock.SKY, new BlockPosition(i, i2, j));
            }
            this.s = true;
        }
    }

    private void c(int i, int j, int k) {
        int i2;
        int l = i2 = (this.heightMap[k << 4 | i] & 0xFF);
        if (j > l) {
            i2 = j;
        }
        while (i2 > 0 && this.d(i, i2 - 1, k) == 0) {
            --i2;
        }
        if (i2 != l) {
            this.world.a(i + this.locX * 16, k + this.locZ * 16, i2, l);
            this.heightMap[k << 4 | i] = i2;
            int j2 = this.locX * 16 + i;
            int k2 = this.locZ * 16 + k;
            if (this.world.worldProvider.m()) {
                if (i2 < l) {
                    for (int l2 = i2; l2 < l; ++l2) {
                        ChunkSection chunksection = this.sections[l2 >> 4];
                        if (chunksection != Chunk.a) {
                            chunksection.a(i, l2 & 0xF, k, 15);
                            this.world.m(new BlockPosition((this.locX << 4) + i, l2, (this.locZ << 4) + k));
                        }
                    }
                } else {
                    for (int l2 = l; l2 < i2; ++l2) {
                        ChunkSection chunksection = this.sections[l2 >> 4];
                        if (chunksection != Chunk.a) {
                            chunksection.a(i, l2 & 0xF, k, 0);
                            this.world.m(new BlockPosition((this.locX << 4) + i, l2, (this.locZ << 4) + k));
                        }
                    }
                }
                int l2 = 15;
                while (i2 > 0 && l2 > 0) {
                    --i2;
                    int i3 = this.d(i, i2, k);
                    if (i3 == 0) {
                        i3 = 1;
                    }
                    l2 -= i3;
                    if (l2 < 0) {
                        l2 = 0;
                    }
                    ChunkSection chunksection2 = this.sections[i2 >> 4];
                    if (chunksection2 != Chunk.a) {
                        chunksection2.a(i, i2 & 0xF, k, l2);
                    }
                }
            }
            int l2 = this.heightMap[k << 4 | i];
            int i3;
            int j3;
            if ((j3 = l2) < (i3 = l)) {
                i3 = l2;
                j3 = l;
            }
            if (l2 < this.v) {
                this.v = l2;
            }
            if (this.world.worldProvider.m()) {
                for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                    this.a(j2 + enumdirection.getAdjacentX(), k2 + enumdirection.getAdjacentZ(), i3, j3);
                }
                this.a(j2, k2, i3, j3);
            }
            this.s = true;
        }
    }

    public int b(BlockPosition blockposition) {
        return this.getBlockData(blockposition).c();
    }

    private int d(int i, int j, int k) {
        return this.a(i, j, k).c();
    }

    public IBlockData getBlockData(BlockPosition pos) {
        return this.getBlockData(pos.getX(), pos.getY(), pos.getZ());
    }

    public IBlockData getBlockData(int x, int y, int z) {
        int i = y >> 4;
        if (y >= 0 && i < this.sections.length && this.sections[i] != null) {
            return this.sections[i].blockIds.a((y & 0xF) << 8 | (z & 0xF) << 4 | (x & 0xF));
        }
        return Blocks.AIR.getBlockData();
    }

    public IBlockData a(int i, int j, int k) {
        return this.getBlockData(i, j, k);
    }

    public IBlockData unused(int i, int j, int k) {
        if (this.world.N() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            IBlockData iblockdata = null;
            if (j == 60) {
                iblockdata = Blocks.BARRIER.getBlockData();
            }
            if (j == 70) {
                iblockdata = ChunkProviderDebug.c(i, k);
            }
            return (iblockdata == null) ? Blocks.AIR.getBlockData() : iblockdata;
        }
        try {
            if (j >= 0 && j >> 4 < this.sections.length) {
                ChunkSection chunksection = this.sections[j >> 4];
                if (chunksection != Chunk.a) {
                    return chunksection.getType(i & 0xF, j & 0xF, k & 0xF);
                }
            }
            return Blocks.AIR.getBlockData();
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Getting block state");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being got");
            crashreportsystemdetails.a("Location", (CrashReportCallable) new CrashReportCallable() {
                public String a() throws Exception {
                    return CrashReportSystemDetails.a(i, j, k);
                }

                public Object call() throws Exception {
                    return this.a();
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    @Nullable
    public IBlockData a(BlockPosition blockposition, IBlockData iblockdata) {
        TileEntity tileentity;
        int k;
        int l;
        int i = blockposition.getX() & 15;
        int j = blockposition.getY();
        if (j >= this.h[l = (k = blockposition.getZ() & 15) << 4 | i] - 1) {
            this.h[l] = -999;
        }
        int i1 = this.heightMap[l];
        IBlockData iblockdata1 = this.getBlockData(blockposition);
        if (iblockdata1 == iblockdata) {
            return null;
        }
        Block block = iblockdata.getBlock();
        Block block1 = iblockdata1.getBlock();
        ChunkSection chunksection = this.sections[j >> 4];
        boolean flag = false;
        if (chunksection == a) {
            if (block == Blocks.AIR) {
                return null;
            }
            this.sections[j >> 4] = chunksection = new ChunkSection(j >> 4 << 4, this.world.worldProvider.m(),
                    this.world.chunkPacketBlockController.getPredefinedBlockData(this, j >> 4));
            flag = j >= i1;
        }
        chunksection.setType(i, j & 15, k, iblockdata);
        if (block1 != block) {
            if (!this.world.isClientSide) {
                block1.remove(this.world, blockposition, iblockdata1);
            } else if (block1 instanceof ITileEntity) {
                this.world.s(blockposition);
            }
        }
        if (chunksection.getType(i, j & 15, k).getBlock() != block) {
            return null;
        }
        if (flag) {
            this.initLighting();
        } else {
            this.runOrQueueLightUpdate(() -> {
                int j1 = iblockdata.c();
                int k1 = iblockdata1.c();
                if (j1 > 0) {
                    if (j >= i1) {
                        this.c(i, j + 1, k);
                    }
                } else if (j == i1 - 1) {
                    this.c(i, j, k);
                }
                if (j1 != k1 && (j1 < k1 || this.getBrightness(EnumSkyBlock.SKY, blockposition) > 0
                        || this.getBrightness(EnumSkyBlock.BLOCK, blockposition) > 0)) {
                    this.d(i, k);
                }
            });
        }
        if (block1 instanceof ITileEntity && (tileentity = this.a(blockposition, EnumTileEntityState.CHECK)) != null) {
            tileentity.invalidateBlockCache();
        }
        if (!(this.world.isClientSide || block1 == block
                || this.world.captureBlockStates && !(block instanceof BlockTileEntity))) {
            block.onPlace(this.world, blockposition, iblockdata);
        }
        if (block instanceof ITileEntity) {
            tileentity = this.a(blockposition, EnumTileEntityState.CHECK);
            if (tileentity == null) {
                tileentity = ((ITileEntity) block).a(this.world, block.toLegacyData(iblockdata));
                this.world.setTileEntity(blockposition, tileentity);
            }
            if (tileentity != null) {
                tileentity.invalidateBlockCache();
            }
        }
        this.s = true;
        return iblockdata1;
    }

    public int getBrightness(EnumSkyBlock enumskyblock, BlockPosition blockposition) {
        int i = blockposition.getX() & 0xF;
        int j = blockposition.getY();
        int k = blockposition.getZ() & 0xF;
        ChunkSection chunksection = this.sections[j >> 4];
        return (chunksection == Chunk.a) ? (this.c(blockposition) ? enumskyblock.c : 0)
                : ((enumskyblock == EnumSkyBlock.SKY)
                ? (this.world.worldProvider.m() ? chunksection.b(i, j & 0xF, k) : 0)
                : ((enumskyblock == EnumSkyBlock.BLOCK) ? chunksection.c(i, j & 0xF, k) : enumskyblock.c));
    }

    public void a(EnumSkyBlock enumskyblock, BlockPosition blockposition, int i) {
        int j = blockposition.getX() & 0xF;
        int k = blockposition.getY();
        int l = blockposition.getZ() & 0xF;
        ChunkSection chunksection = this.sections[k >> 4];
        if (chunksection == Chunk.a) {
            chunksection = new ChunkSection(k >> 4 << 4, this.world.worldProvider.m(),
                    this.world.chunkPacketBlockController.getPredefinedBlockData(this, k >> 4));
            this.sections[k >> 4] = chunksection;
            this.initLighting();
        }
        this.s = true;
        if (enumskyblock == EnumSkyBlock.SKY) {
            if (this.world.worldProvider.m()) {
                chunksection.a(j, k & 0xF, l, i);
            }
        } else if (enumskyblock == EnumSkyBlock.BLOCK) {
            chunksection.b(j, k & 0xF, l, i);
        }
    }

    public int getLightSubtracted(BlockPosition blockposition, int i) {
        return this.a(blockposition, i);
    }

    public int a(BlockPosition blockposition, int i) {
        int j = blockposition.getX() & 0xF;
        int k = blockposition.getY();
        int l = blockposition.getZ() & 0xF;
        ChunkSection chunksection = this.sections[k >> 4];
        if (chunksection == Chunk.a) {
            return (this.world.worldProvider.m() && i < EnumSkyBlock.SKY.c) ? (EnumSkyBlock.SKY.c - i) : 0;
        }
        int i2 = this.world.worldProvider.m() ? chunksection.b(j, k & 0xF, l) : 0;
        i2 -= i;
        int j2 = chunksection.c(j, k & 0xF, l);
        if (j2 > i2) {
            i2 = j2;
        }
        return i2;
    }

    public void a(Entity entity) {
        this.t = true;
        int i = MathHelper.floor(entity.locX / 16.0);
        int j = MathHelper.floor(entity.locZ / 16.0);
        if (i != this.locX || j != this.locZ) {
            Chunk.e.warn("Wrong location! ({}, {}) should be ({}, {}), {}", (Object) i, (Object) j, (Object) this.locX,
                    (Object) this.locZ, (Object) entity);
            entity.die();
            return;
        }
        int k = MathHelper.floor(entity.locY / 16.0);
        if (k < 0) {
            k = 0;
        }
        if (k >= this.entitySlices.length) {
            k = this.entitySlices.length - 1;
        }
        entity.aa = true;
        entity.ab = this.locX;
        entity.ac = k;
        entity.ad = this.locZ;
        List<Entity> entitySlice = this.entitySlices[k];
        boolean inThis = entitySlice.contains(entity);
        List<Entity> currentSlice = (List<Entity>) entity.entitySlice;
        if (inThis || (currentSlice != null && currentSlice.contains(entity))) {
            if (currentSlice == entitySlice || inThis) {
                return;
            }
            Chunk chunk = entity.getCurrentChunk();
            if (chunk != null) {
                chunk.removeEntity(entity);
            } else {
                this.removeEntity(entity);
            }
            currentSlice.remove(entity);
        }
        (entity.entitySlice = entitySlice).add(entity);
        this.markDirty();
        entity.setCurrentChunk(this);
        this.entityCounts.increment(entity.getMinecraftKeyString());
        if (entity instanceof EntityItem) {
            int[] itemCounts = this.itemCounts;
            int n = k;
            ++itemCounts[n];
        } else if (entity instanceof IInventory) {
            int[] inventoryEntityCounts = this.inventoryEntityCounts;
            int n2 = k;
            ++inventoryEntityCounts[n2];
        }
        if (entity instanceof EntityInsentient) {
            EntityInsentient entityinsentient = (EntityInsentient) entity;
            if (entityinsentient.isTypeNotPersistent() && entityinsentient.isPersistent()) {
                return;
            }
        }
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            if (creatureType.a().isAssignableFrom(entity.getClass())) {
                this.entityCount.adjustOrPutValue(creatureType.a(), 1, 1);
            }
        }
    }

    public void removeEntity(Entity entity) {
        this.b(entity);
    }

    public void b(Entity entity) {
        this.a(entity, entity.ac);
    }

    public void a(Entity entity, int i) {
        if (i < 0) {
            i = 0;
        }
        if (i >= this.entitySlices.length) {
            i = this.entitySlices.length - 1;
        }
        if (entity.entitySlice == null || !entity.entitySlice.contains(entity)
                || this.entitySlices[i] == entity.entitySlice) {
            entity.entitySlice = null;
        }
        if (!this.entitySlices[i].remove(entity)) {
            return;
        }
        this.markDirty();
        entity.setCurrentChunk((Chunk) null);
        this.entityCounts.decrement(entity.getMinecraftKeyString());
        if (entity instanceof EntityItem) {
            int[] itemCounts = this.itemCounts;
            int n = i;
            --itemCounts[n];
        } else if (entity instanceof IInventory) {
            int[] inventoryEntityCounts = this.inventoryEntityCounts;
            int n2 = i;
            --inventoryEntityCounts[n2];
        }
        if (entity instanceof EntityInsentient) {
            EntityInsentient entityinsentient = (EntityInsentient) entity;
            if (entityinsentient.isTypeNotPersistent() && entityinsentient.isPersistent()) {
                return;
            }
        }
        for (EnumCreatureType creatureType : EnumCreatureType.values()) {
            if (creatureType.a().isAssignableFrom(entity.getClass())) {
                this.entityCount.adjustValue(creatureType.a(), -1);
            }
        }
    }

    public boolean c(BlockPosition blockposition) {
        int i = blockposition.getX() & 0xF;
        int j = blockposition.getY();
        int k = blockposition.getZ() & 0xF;
        return j >= this.heightMap[k << 4 | i];
    }

    @Nullable
    private TileEntity g(BlockPosition blockposition) {
        IBlockData iblockdata = this.getBlockData(blockposition);
        Block block = iblockdata.getBlock();
        return block.isTileEntity()
                ? ((ITileEntity) block).a(this.world, iblockdata.getBlock().toLegacyData(iblockdata))
                : null;
    }

    @Nullable
    public TileEntity getTileEntityImmediately(BlockPosition pos) {
        return this.a(pos, EnumTileEntityState.IMMEDIATE);
    }

    @Nullable
    public TileEntity a(BlockPosition blockposition, EnumTileEntityState chunk_enumtileentitystate) {
        TileEntity tileentity = null;
        if (this.world.captureBlockStates) {
            tileentity = this.world.capturedTileEntities.get(blockposition);
        }
        if (tileentity == null) {
            tileentity = this.tileEntities.get(blockposition);
        }
        if (tileentity == null) {
            if (chunk_enumtileentitystate == EnumTileEntityState.IMMEDIATE) {
                tileentity = this.g(blockposition);
                this.world.setTileEntity(blockposition, tileentity);
            } else if (chunk_enumtileentitystate == EnumTileEntityState.QUEUED) {
                this.y.add(blockposition);
            }
        } else if (tileentity.y()) {
            this.tileEntities.remove(blockposition);
            return null;
        }
        return tileentity;
    }

    public void a(TileEntity tileentity) {
        this.a(tileentity.getPosition(), tileentity);
        if (this.j) {
            this.world.a(tileentity);
        }
    }

    public void a(BlockPosition blockposition, TileEntity tileentity) {
        tileentity.a(this.world);
        tileentity.setPosition(blockposition);
        if (this.getBlockData(blockposition).getBlock() instanceof ITileEntity) {
            if (this.tileEntities.containsKey(blockposition)) {
                this.tileEntities.get(blockposition).z();
            }
            tileentity.A();
            this.tileEntities.put(blockposition, tileentity);
        } else if (tileentity instanceof TileEntityMobSpawner && CraftMagicNumbers
                .getMaterial(this.getBlockData(blockposition).getBlock()) != org.bukkit.Material.MOB_SPAWNER) {
            this.tileEntities.remove(blockposition);
        } else {
            ServerInternalException e = new ServerInternalException("Attempted to place a tile entity (" + tileentity
                    + ") at " + tileentity.position.getX() + "," + tileentity.position.getY() + ","
                    + tileentity.position.getZ() + " ("
                    + CraftMagicNumbers.getMaterial(this.getBlockData(blockposition).getBlock())
                    + ") where there was no entity tile!\nChunk coordinates: " + this.locX * 16 + "," + this.locZ * 16);
            e.printStackTrace();
            ServerInternalException.reportInternalException((Throwable) e);
            if (this.world.paperConfig.removeCorruptTEs) {
                this.removeTileEntity(tileentity.getPosition());
                this.markDirty();
                Bukkit.getLogger().info("Removing corrupt tile entity");
            }
        }
    }

    public void removeTileEntity(BlockPosition blockposition) {
        this.d(blockposition);
    }

    public void d(BlockPosition blockposition) {
        if (this.j) {
            TileEntity tileentity = this.tileEntities.remove(blockposition);
            if (tileentity != null) {
                tileentity.z();
            }
        }
    }

    public void addEntities() {
        this.j = true;
        this.world.b((Collection) this.tileEntities.values());
        List[] aentityslice = this.entitySlices;
        int i = aentityslice.length;
        List<Entity> toAdd = new ArrayList<Entity>(32);
        for (List entityslice : aentityslice) {
            PaperWorldConfig.DuplicateUUIDMode mode = this.world.paperConfig.duplicateUUIDMode;
            if (mode == PaperWorldConfig.DuplicateUUIDMode.WARN || mode == PaperWorldConfig.DuplicateUUIDMode.DELETE
                    || mode == PaperWorldConfig.DuplicateUUIDMode.SAFE_REGEN) {
                Map<UUID, Entity> thisChunk = new HashMap<UUID, Entity>();
                Iterator<Entity> iterator = entityslice.iterator();
                while (iterator.hasNext()) {
                    Entity entity = iterator.next();
                    if (!entity.dead) {
                        if (entity.valid) {
                            continue;
                        }
                        Entity other = ((WorldServer) this.world).entitiesByUUID.get(entity.uniqueID);
                        if (other == null || other.dead || this.world.getEntityUnloadQueue().contains(other)) {
                            other = thisChunk.get(entity.uniqueID);
                        }
                        if (mode == PaperWorldConfig.DuplicateUUIDMode.SAFE_REGEN && other != null && !other.dead
                                && !this.world.getEntityUnloadQueue().contains(other)
                                && Objects.equals(other.getSaveID(), entity.getSaveID())
                                && entity.getBukkitEntity().getLocation().distance(other.getBukkitEntity()
                                .getLocation()) < this.world.paperConfig.duplicateUUIDDeleteRange) {
                            if (World.DEBUG_ENTITIES) {
                                Chunk.logger.warn("[DUPE-UUID] Duplicate UUID found used by " + other
                                        + ", deleted entity " + entity
                                        + " because it was near the duplicate and likely an actual duplicate. See https://github.com/PaperMC/Paper/issues/1223 for discussion on what this is about.");
                            }
                            entity.die();
                            iterator.remove();
                        } else {
                            if (other != null && !other.dead) {
                                switch (mode) {
                                    case SAFE_REGEN: {
                                        entity.setUUID(UUID.randomUUID());
                                        if (World.DEBUG_ENTITIES) {
                                            Chunk.logger.warn("[DUPE-UUID] Duplicate UUID found used by " + other
                                                    + ", regenerated UUID for " + entity
                                                    + ". See https://github.com/PaperMC/Paper/issues/1223 for discussion on what this is about.");
                                            break;
                                        }
                                        break;
                                    }
                                    case DELETE: {
                                        if (World.DEBUG_ENTITIES) {
                                            Chunk.logger.warn("[DUPE-UUID] Duplicate UUID found used by " + other
                                                    + ", deleted entity " + entity
                                                    + ". See https://github.com/PaperMC/Paper/issues/1223 for discussion on what this is about.");
                                        }
                                        entity.die();
                                        iterator.remove();
                                        break;
                                    }
                                    default: {
                                        if (World.DEBUG_ENTITIES) {
                                            Chunk.logger.warn("[DUPE-UUID] Duplicate UUID found used by " + other
                                                    + ", doing nothing to " + entity
                                                    + ". See https://github.com/PaperMC/Paper/issues/1223 for discussion on what this is about.");
                                            break;
                                        }
                                        break;
                                    }
                                }
                            }
                            thisChunk.put(entity.uniqueID, entity);
                        }
                    }
                }
            }
            toAdd.addAll(entityslice);
        }
        this.world.addChunkEntities((Collection) toAdd);
    }

    public void removeEntities() {
        this.j = false;
        for (TileEntity tileentity : this.tileEntities.values()) {
            if (tileentity instanceof IInventory) {
                for (HumanEntity h : Lists.newArrayList(((IInventory) tileentity).getViewers())) {
                    if (h instanceof CraftHumanEntity) {
                        ((CraftHumanEntity) h).getHandle().closeInventory(InventoryCloseEvent.Reason.UNLOADED);
                    }
                }
            }
            this.world.b(tileentity);
        }
        List[] aentityslice = this.entitySlices;
        for (int i = aentityslice.length, j = 0; j < i; ++j) {
            List<Entity> newList = Lists.newArrayList(aentityslice[j]);
            Iterator<Entity> iter = newList.iterator();
            while (iter.hasNext()) {
                Entity entity = iter.next();
                if (entity instanceof IInventory) {
                    for (HumanEntity h2 : Lists.newArrayList(((IInventory) entity).getViewers())) {
                        if (h2 instanceof CraftHumanEntity) {
                            ((CraftHumanEntity) h2).getHandle().closeInventory(InventoryCloseEvent.Reason.UNLOADED);
                        }
                    }
                }
                entity.setCurrentChunk((Chunk) null);
                entity.entitySlice = null;
                if (entity instanceof EntityPlayer) {
                    iter.remove();
                }
            }
            this.world.c((Collection) newList);
        }
    }

    public void markDirty() {
        this.s = true;
    }

    public void a(@Nullable Entity entity, AxisAlignedBB axisalignedbb, List<Entity> list,
                  Predicate<? super Entity> predicate) {
        int i = MathHelper.floor((axisalignedbb.b - 2.0) / 16.0);
        int j = MathHelper.floor((axisalignedbb.e + 2.0) / 16.0);
        i = MathHelper.clamp(i, 0, this.entitySlices.length - 1);
        j = MathHelper.clamp(j, 0, this.entitySlices.length - 1);
        for (int k = i; k <= j; ++k) {
            if (!this.entitySlices[k].isEmpty()) {
                synchronized (entitySlices[k]) {
                    Iterator<Entity> iterator = this.entitySlices[k].iterator();
                    if (predicate != IEntitySelector.c || this.inventoryEntityCounts[k] > 0) {
                        while (iterator.hasNext()) {
                            Entity entity2 = iterator.next();
                            if (entity2.getBoundingBox().c(axisalignedbb) && entity2 != entity) {
                                if (predicate == null || predicate.apply(entity2)) {
                                    list.add(entity2);
                                }
                                Entity[] aentity = entity2.bb();
                                if (aentity == null) {
                                    continue;
                                }
                                Entity[] aentity2 = aentity;
                                for (int l = aentity.length, i2 = 0; i2 < l; ++i2) {
                                    Entity entity3 = aentity2[i2];
                                    if (entity3 != entity && entity3.getBoundingBox().c(axisalignedbb)
                                            && (predicate == null || predicate.apply(entity3))) {
                                        list.add(entity3);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public <T extends Entity> void a(Class<? extends T> oclass, AxisAlignedBB axisalignedbb, List<T> list,
                                     Predicate<? super T> predicate) {
        int i = MathHelper.floor((axisalignedbb.b - 2.0) / 16.0);
        int j = MathHelper.floor((axisalignedbb.e + 2.0) / 16.0);
        i = MathHelper.clamp(i, 0, this.entitySlices.length - 1);
        j = MathHelper.clamp(j, 0, this.entitySlices.length - 1);
        int[] counts;
        if (EntityItem.class.isAssignableFrom(oclass)) {
            counts = this.itemCounts;
        } else if (IInventory.class.isAssignableFrom(oclass)) {
            counts = this.inventoryEntityCounts;
        } else {
            counts = null;
        }
        for (int k = i; k <= j; ++k) {
            if (counts == null || counts[k] > 0) {
                for (Entity entity : this.entitySlices[k]) {
                    if (oclass.isInstance(entity) && entity.getBoundingBox().c(axisalignedbb)
                            && (predicate == null || predicate.apply(oclass.cast(entity)))) {
                        list.add(oclass.cast(entity));
                    }
                }
            }
        }
    }

    public boolean a(boolean flag) {
        return (flag && ((this.t && this.world.getTime() != this.lastSaved) || this.s)) || ((this.s || this.t)
                && this.world.getTime() >= this.lastSaved + this.world.paperConfig.autoSavePeriod);
    }

    public Random a(long i) {
        return new Random(this.world.getSeed() + this.locX * this.locX * 4987142 + this.locX * 5947611
                + this.locZ * this.locZ * 4392871L + this.locZ * 389711 ^ i);
    }

    public boolean isEmpty() {
        return false;
    }

    public void loadNearby(IChunkProvider ichunkprovider, ChunkGenerator chunkgenerator, boolean newChunk) {
        this.world.timings.syncChunkLoadPostTimer.startTiming();
        Server server = (Server) this.world.getServer();
        if (server != null) {
            server.getPluginManager().callEvent((Event) new ChunkLoadEvent(this.bukkitChunk, newChunk));
        }
        for (int x = -2; x < 3; ++x) {
            for (int z = -2; z < 3; ++z) {
                if (x != 0 || z != 0) {
                    Chunk neighbor = this.getWorld().getChunkIfLoaded(this.locX + x, this.locZ + z);
                    if (neighbor != null) {
                        neighbor.setNeighborLoaded(-x, -z);
                        this.setNeighborLoaded(x, z);
                    }
                }
            }
        }
        this.world.timings.syncChunkLoadPostTimer.stopTiming();
        this.world.timings.syncChunkLoadPopulateNeighbors.startTiming();
        Chunk chunk = MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX, this.locZ - 1);
        Chunk chunk2 = MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX + 1, this.locZ);
        Chunk chunk3 = MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX, this.locZ + 1);
        Chunk chunk4 = MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX - 1, this.locZ);
        if (chunk2 != null && chunk3 != null
                && MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX + 1, this.locZ + 1) != null) {
            this.a(chunkgenerator);
        }
        if (chunk4 != null && chunk3 != null
                && MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX - 1, this.locZ + 1) != null) {
            chunk4.a(chunkgenerator);
        }
        if (chunk != null && chunk2 != null
                && MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX + 1, this.locZ - 1) != null) {
            chunk.a(chunkgenerator);
        }
        if (chunk != null && chunk4 != null) {
            Chunk chunk5 = MCUtil.getLoadedChunkWithoutMarkingActive(ichunkprovider, this.locX - 1, this.locZ - 1);
            if (chunk5 != null) {
                chunk5.a(chunkgenerator);
            }
        }
        this.world.timings.syncChunkLoadPopulateNeighbors.stopTiming();
    }

    protected void a(ChunkGenerator chunkgenerator) {
        if (this.isDone()) {
            if (chunkgenerator.a(this, this.locX, this.locZ)) {
                this.markDirty();
            }
        } else {
            this.o();
            chunkgenerator.recreateStructures(this.locX, this.locZ);
            BlockSand.instaFall = true;
            Random random = new Random();
            random.setSeed(this.world.getSeed());
            long xRand = random.nextLong() / 2L * 2L + 1L;
            long zRand = random.nextLong() / 2L * 2L + 1L;
            random.setSeed(this.locX * xRand + this.locZ * zRand ^ this.world.getSeed());
            org.bukkit.World world = (org.bukkit.World) this.world.getWorld();
            if (world != null) {
                this.world.populating = true;
                try {
                    for (BlockPopulator populator : world.getPopulators()) {
                        populator.populate(world, random, this.bukkitChunk);
                    }
                } finally {
                    this.world.populating = false;
                }
            }
            BlockSand.instaFall = false;
            this.world.getServer().getPluginManager().callEvent((Event) new ChunkPopulateEvent(this.bukkitChunk));
            this.markDirty();
        }
    }

    public BlockPosition f(BlockPosition blockposition) {
        int i = blockposition.getX() & 0xF;
        int j = blockposition.getZ() & 0xF;
        int k = i | j << 4;
        BlockPosition blockposition2 = new BlockPosition(blockposition.getX(), this.h[k], blockposition.getZ());
        if (blockposition2.getY() == -999) {
            int l = this.g() + 15;
            blockposition2 = new BlockPosition(blockposition.getX(), l, blockposition.getZ());
            int i2 = -1;
            while (blockposition2.getY() > 0 && i2 == -1) {
                IBlockData iblockdata = this.getBlockData(blockposition2);
                Material material = iblockdata.getMaterial();
                if (!material.isSolid() && !material.isLiquid()) {
                    blockposition2 = blockposition2.down();
                } else {
                    i2 = blockposition2.getY() + 1;
                }
            }
            this.h[k] = i2;
        }
        return new BlockPosition(blockposition.getX(), this.h[k], blockposition.getZ());
    }

    public void b(boolean flag) {
        if (this.m && this.world.worldProvider.m() && !flag) {
            this.h(this.world.isClientSide);
        }
        this.r = true;
        if (!this.lit && this.done) {
            this.o();
        }
        while (!this.y.isEmpty()) {
            BlockPosition blockposition = this.y.poll();
            if (this.a(blockposition, EnumTileEntityState.CHECK) == null
                    && this.getBlockData(blockposition).getBlock().isTileEntity()) {
                TileEntity tileentity = this.g(blockposition);
                this.world.setTileEntity(blockposition, tileentity);
                this.world.b(blockposition, blockposition);
            }
        }
    }

    public boolean isReady() {
        return !this.world.spigotConfig.randomLightUpdates || (this.isTicked() && this.done && this.lit);
    }

    public boolean j() {
        return this.r;
    }

    public ChunkCoordIntPair k() {
        return new ChunkCoordIntPair(this.locX, this.locZ);
    }

    public boolean c(int i, int j) {
        if (i < 0) {
            i = 0;
        }
        if (j >= 256) {
            j = 255;
        }
        for (int k = i; k <= j; k += 16) {
            ChunkSection chunksection = this.sections[k >> 4];
            if (chunksection != Chunk.a && !chunksection.a()) {
                return false;
            }
        }
        return true;
    }

    public void a(ChunkSection[] achunksection) {
        if (this.sections.length != achunksection.length) {
            Chunk.e.warn("Could not set level chunk sections, array length is {} instead of {}",
                    (Object) achunksection.length, (Object) this.sections.length);
        } else {
            System.arraycopy(achunksection, 0, this.sections, 0, this.sections.length);
        }
    }

    public BiomeBase getBiome(BlockPosition blockposition, WorldChunkManager worldchunkmanager) {
        int i = blockposition.getX() & 0xF;
        int j = blockposition.getZ() & 0xF;
        int k = this.g[j << 4 | i] & 0xFF;
        if (k == 255) {
            BiomeBase biomebase = worldchunkmanager.getBiome(blockposition, Biomes.c);
            k = BiomeBase.a(biomebase);
            this.g[j << 4 | i] = (byte) (k & 0xFF);
        }
        BiomeBase biomebase = BiomeBase.getBiome(k);
        return (biomebase == null) ? Biomes.c : biomebase;
    }

    public byte[] getBiomeIndex() {
        return this.g;
    }

    public void a(byte[] abyte) {
        if (this.g.length != abyte.length) {
            Chunk.e.warn("Could not set level chunk biomes, array length is {} instead of {}", (Object) abyte.length,
                    (Object) this.g.length);
        } else {
            System.arraycopy(abyte, 0, this.g, 0, this.g.length);
        }
    }

    public void m() {
        this.x = 0;
    }

    public void n() {
        if (this.x < 4096) {
            BlockPosition blockposition = new BlockPosition(this.locX << 4, 0, this.locZ << 4);
            for (int i = 0; i < 8; ++i) {
                if (this.x >= 4096) {
                    return;
                }
                int j = this.x % 16;
                int k = this.x / 16 % 16;
                int l = this.x / 256;
                ++this.x;
                for (int i2 = 0; i2 < 16; ++i2) {
                    BlockPosition blockposition2 = blockposition.a(k, (j << 4) + i2, l);
                    boolean flag = i2 == 0 || i2 == 15 || k == 0 || k == 15 || l == 0 || l == 15;
                    if ((this.sections[j] == Chunk.a && flag) || (this.sections[j] != Chunk.a
                            && this.sections[j].getType(k, i2, l).getMaterial() == Material.AIR)) {
                        for (EnumDirection enumdirection : EnumDirection.values()) {
                            BlockPosition blockposition3 = blockposition2.shift(enumdirection);
                            if (this.world.getType(blockposition3).d() > 0) {
                                this.world.w(blockposition3);
                            }
                        }
                        this.world.w(blockposition2);
                    }
                }
            }
        }
    }

    public void o() {
//		this.world.timings.lightChunk.startTiming();
        this.done = true;
        this.lit = true;
        BlockPosition blockposition = new BlockPosition(this.locX << 4, 0, this.locZ << 4);
        if (this.world.worldProvider.m()) {
            if (this.world.areChunksLoadedBetween(blockposition.a(-1, 0, -1),
                    blockposition.a(16, this.world.getSeaLevel(), 16))) {
                Label_0137:
                for (int i = 0; i < 16; ++i) {
                    for (int j = 0; j < 16; ++j) {
                        if (!this.e(i, j)) {
                            this.lit = false;
                            break Label_0137;
                        }
                    }
                }
                if (this.lit) {
                    for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
                        int k = (enumdirection.c() == EnumDirection.EnumAxisDirection.POSITIVE) ? 16 : 1;
                        this.world.getChunkAtWorldCoords(blockposition.shift(enumdirection, k))
                                .a(enumdirection.opposite());
                    }
                    this.z();
                }
            } else {
                this.lit = false;
            }
        }
//		this.world.timings.lightChunk.stopTiming();
    }

    private void z() {
        for (int i = 0; i < this.i.length; ++i) {
            this.i[i] = true;
        }
        this.h(false);
    }

    private void a(EnumDirection enumdirection) {
        if (this.done) {
            if (enumdirection == EnumDirection.EAST) {
                for (int i = 0; i < 16; ++i) {
                    this.e(15, i);
                }
            } else if (enumdirection == EnumDirection.WEST) {
                for (int i = 0; i < 16; ++i) {
                    this.e(0, i);
                }
            } else if (enumdirection == EnumDirection.SOUTH) {
                for (int i = 0; i < 16; ++i) {
                    this.e(i, 15);
                }
            } else if (enumdirection == EnumDirection.NORTH) {
                for (int i = 0; i < 16; ++i) {
                    this.e(i, 0);
                }
            }
        }
    }

    private boolean e(int i, int j) {
        int k = this.g();
        boolean flag = false;
        boolean flag2 = false;
        BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(
                (this.locX << 4) + i, 0, (this.locZ << 4) + j);
        for (int l = k + 16 - 1; l > this.world.getSeaLevel() || (l > 0 && !flag2); --l) {
            blockposition_mutableblockposition.c(blockposition_mutableblockposition.getX(), l,
                    blockposition_mutableblockposition.getZ());
            int i2 = this.b((BlockPosition) blockposition_mutableblockposition);
            if (i2 == 255 && blockposition_mutableblockposition.getY() < this.world.getSeaLevel()) {
                flag2 = true;
            }
            if (!flag && i2 > 0) {
                flag = true;
            } else if (flag && i2 == 0 && !this.world.w((BlockPosition) blockposition_mutableblockposition)) {
                return false;
            }
        }
        for (int l = blockposition_mutableblockposition.getY(); l > 0; --l) {
            blockposition_mutableblockposition.c(blockposition_mutableblockposition.getX(), l,
                    blockposition_mutableblockposition.getZ());
            if (this.getBlockData((BlockPosition) blockposition_mutableblockposition).d() > 0) {
                this.world.w((BlockPosition) blockposition_mutableblockposition);
            }
        }
        return true;
    }

    public boolean p() {
        return this.j;
    }

    public World getWorld() {
        return this.world;
    }

    public int[] r() {
        return this.heightMap;
    }

    public void a(int[] aint) {
        if (this.heightMap.length != aint.length) {
            Chunk.e.warn("Could not set level chunk heightmap, array length is {} instead of {}", (Object) aint.length,
                    (Object) this.heightMap.length);
        } else {
            System.arraycopy(aint, 0, this.heightMap, 0, this.heightMap.length);
        }
    }

    public Map<BlockPosition, TileEntity> getTileEntities() {
        return this.tileEntities;
    }

    public List<Entity>[] getEntitySlices() {
        return this.entitySlices;
    }

    public boolean isDone() {
        return this.done;
    }

    public void d(boolean flag) {
        this.done = flag;
    }

    public boolean v() {
        return this.lit;
    }

    public void e(boolean flag) {
        this.lit = flag;
    }

    public void f(boolean flag) {
        this.s = flag;
    }

    public void g(boolean flag) {
        this.t = flag;
    }

    public void setLastSaved(long i) {
        this.lastSaved = i;
    }

    public int w() {
        return this.v;
    }

    public long x() {
        return this.world.paperConfig.useInhabitedTime ? this.w : 0L;
    }

    public void c(long i) {
        this.w = i;
    }

    public void runOrQueueLightUpdate(Runnable runnable) {
        if (this.world.paperConfig.queueLightUpdates) {
            this.lightingQueue.add(runnable);
        } else {
            runnable.run();
        }
    }

    static {
        e = LogManager.getLogger();
        a = null;
        EMPTY_CHUNK_SECTION = Chunk.a;
        logger = LogManager.getLogger();
    }

    private class TileEntityHashMap extends HashMap<BlockPosition, TileEntity> {
        @Override
        public TileEntity put(BlockPosition key, TileEntity value) {
            TileEntity replaced = super.put(key, value);
            if (replaced != null) {
                replaced.setCurrentChunk((Chunk) null);
                Chunk.this.tileEntityCounts.decrement(replaced.getMinecraftKeyString());
            }
            if (value != null) {
                value.setCurrentChunk(Chunk.this);
                Chunk.this.tileEntityCounts.increment(value.getMinecraftKeyString());
            }
            return replaced;
        }

        @Override
        public TileEntity remove(Object key) {
            TileEntity removed = super.remove(key);
            if (removed != null) {
                removed.setCurrentChunk((Chunk) null);
                Chunk.this.tileEntityCounts.decrement(removed.getMinecraftKeyString());
            }
            return removed;
        }
    }

    public enum EnumTileEntityState {
        IMMEDIATE, QUEUED, CHECK;
    }
}

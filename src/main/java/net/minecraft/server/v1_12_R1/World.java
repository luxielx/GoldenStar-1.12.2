package net.minecraft.server.v1_12_R1;

import co.aikar.timings.TimingHistory;
import co.aikar.timings.WorldTimingsHandler;
import com.destroystokyo.paper.PaperWorldConfig;
import com.destroystokyo.paper.antixray.ChunkPacketBlockController;
import com.destroystokyo.paper.antixray.ChunkPacketBlockControllerAntiXray;
import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.destroystokyo.paper.event.entity.ExperienceOrbMergeEvent;
import com.destroystokyo.paper.event.server.ServerExceptionEvent;
import com.destroystokyo.paper.exception.ServerException;
import com.destroystokyo.paper.exception.ServerInternalException;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.generator.ChunkGenerator;
import org.spigotmc.ActivationRange;
import org.spigotmc.AsyncCatcher;
import org.spigotmc.SpigotWorldConfig;
import org.spigotmc.TickLimiter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class World implements IBlockAccess {

    // ~
    // make entityList thread safe (find synchronized)

    private int a;
    protected boolean d;
    public static final boolean DEBUG_ENTITIES = Boolean.getBoolean("debug.entities");
    ;
    public List<Entity> entityList;
    protected Set<Entity> f;
    public List<TileEntity> tileEntityListTick;
    private List<TileEntity> b;
    private Set<TileEntity> tileEntityListUnload;
    public List<EntityHuman> players;
    public List<Entity> j;
    protected IntHashMap<Entity> entitiesById;
    private long K = 16777215L;
    private int L;
    protected int l;
    protected int m = 1013904223;
    protected float n;
    protected float o;
    protected float p;
    protected float q;
    private int M;
    public Random random;
    public WorldProvider worldProvider;
    protected NavigationListener t;
    protected List<IWorldAccess> u;
    protected IChunkProvider chunkProvider;
    protected IDataManager dataManager;
    public WorldData worldData;
    protected boolean isLoading;
    public PersistentCollection worldMaps;
    protected PersistentVillage villages;
    protected LootTableRegistry B;
    protected AdvancementDataWorld C;
    protected CustomFunctionData D;
    public MethodProfiler methodProfiler;
    private Calendar N;
    public Scoreboard scoreboard;
    public boolean isClientSide;
    public boolean allowMonsters;
    public boolean allowAnimals;
    private boolean O;
    private WorldBorder P;
    int[] J;
    private CraftWorld world;
    public boolean pvpMode;
    public boolean keepSpawnInMemory;
    public ChunkGenerator generator;
    public boolean captureBlockStates;
    public boolean captureTreeGeneration;
    public List<BlockState> capturedBlockStates;
    public List<EntityItem> captureDrops;
    public long ticksPerAnimalSpawns;
    public long ticksPerMonsterSpawns;
    public boolean populating;
    private volatile int tickPosition;
    public SpigotWorldConfig spigotConfig;
    public PaperWorldConfig paperConfig;
    public ChunkPacketBlockController chunkPacketBlockController;
    public WorldTimingsHandler timings;
    //	private boolean guardEntityList;
    public static boolean haveWeSilencedAPhysicsCrash;
    public static String blockLocation;
    private TickLimiter entityLimiter;
    private TickLimiter tileLimiter;
    private int tileTickPosition;
    public Map<Explosion.CacheKey, Float> explosionDensityCache;
    public Map<BlockPosition, TileEntity> capturedTileEntities;

    public Set<Entity> getEntityUnloadQueue() {
        return this.f;
    }

    private int getSkylightSubtracted() {
        return this.L;
    }

    public CraftWorld getWorld() {
        return this.world;
    }

    public CraftServer getServer() {
        return (CraftServer) Bukkit.getServer();
    }

    public Chunk getChunkIfLoaded(BlockPosition blockposition) {
        return ((ChunkProviderServer) this.chunkProvider).getChunkIfLoaded(blockposition.getX() >> 4,
                blockposition.getZ() >> 4);
    }

    public Chunk getChunkIfLoaded(int x, int z) {
        return ((ChunkProviderServer) this.chunkProvider).getChunkIfLoaded(x, z);
    }

    protected World(IDataManager idatamanager, WorldData worlddata, WorldProvider worldprovider,
                    MethodProfiler methodprofiler, boolean flag, ChunkGenerator gen, org.bukkit.World.Environment env) {
        this.a = 63;
        // ~
        this.entityList = Collections.synchronizedList(new ArrayList<Entity>());
        this.f = Sets.newHashSet();
        this.tileEntityListTick = Lists.newArrayList();
        this.b = Lists.newArrayList();
        this.tileEntityListUnload = Sets.newHashSet();
        // ~
        this.players = Collections.synchronizedList(Lists.newArrayList());
        this.j = Lists.newArrayList();
        this.entitiesById = (IntHashMap<Entity>) new IntHashMap<Entity>();
        this.l = new Random().nextInt();
        this.random = new Random();
        this.t = new NavigationListener();
        this.keepSpawnInMemory = true;
        this.captureBlockStates = false;
        this.captureTreeGeneration = false;
        this.capturedBlockStates = Collections.synchronizedList(new ArrayList<BlockState>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            @Override
            public boolean add(BlockState blockState) {
                synchronized (this) {
                    for (BlockState blockState2 : this) {
                        if (blockState2.getLocation().equals(blockState.getLocation())) {
                            return false;
                        }
                    }
                }
                return super.add(blockState);
            }
        });
        this.explosionDensityCache = new HashMap<Explosion.CacheKey, Float>();
        this.capturedTileEntities = Maps.newHashMap();
        this.spigotConfig = new SpigotWorldConfig(worlddata.getName());
        this.paperConfig = new PaperWorldConfig(worlddata.getName(), this.spigotConfig);
        this.chunkPacketBlockController = (ChunkPacketBlockController) (this.paperConfig.antiXray
                ? new ChunkPacketBlockControllerAntiXray(this.paperConfig)
                : ChunkPacketBlockController.NO_OPERATION_INSTANCE);
        this.generator = gen;
        this.world = new CraftWorld((WorldServer) this, gen, env);
        this.ticksPerAnimalSpawns = this.getServer().getTicksPerAnimalSpawns();
        this.ticksPerMonsterSpawns = this.getServer().getTicksPerMonsterSpawns();
        this.u = Lists.newArrayList(new IWorldAccess[]{this.t});
        this.N = Calendar.getInstance();
        this.scoreboard = new Scoreboard();
        this.allowMonsters = true;
        this.allowAnimals = true;
        this.J = new int[32768];
        this.dataManager = idatamanager;
        this.methodProfiler = methodprofiler;
        this.worldData = worlddata;
        this.worldProvider = worldprovider;
        this.isClientSide = flag;
        this.P = worldprovider.getWorldBorder();
        this.getWorldBorder().world = (WorldServer) this;
        this.getWorldBorder().a((IWorldBorderListener) new IWorldBorderListener() {
            public void a(WorldBorder worldborder, double d0) {
                World.this.getServer().getHandle()
                        .sendAll(
                                (Packet<?>) new PacketPlayOutWorldBorder(worldborder,
                                        PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_SIZE),
                                (World) worldborder.world);
            }

            public void a(WorldBorder worldborder, double d0, double d1, long i) {
                World.this.getServer().getHandle()
                        .sendAll(
                                (Packet<?>) new PacketPlayOutWorldBorder(worldborder,
                                        PacketPlayOutWorldBorder.EnumWorldBorderAction.LERP_SIZE),
                                (World) worldborder.world);
            }

            public void a(WorldBorder worldborder, double d0, double d1) {
                World.this.getServer().getHandle()
                        .sendAll(
                                (Packet<?>) new PacketPlayOutWorldBorder(worldborder,
                                        PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_CENTER),
                                (World) worldborder.world);
            }

            public void a(WorldBorder worldborder, int i) {
                World.this.getServer().getHandle().sendAll(
                        (Packet<?>) new PacketPlayOutWorldBorder(worldborder,
                                PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_TIME),
                        (World) worldborder.world);
            }

            public void b(WorldBorder worldborder, int i) {
                World.this.getServer().getHandle().sendAll(
                        (Packet<?>) new PacketPlayOutWorldBorder(worldborder,
                                PacketPlayOutWorldBorder.EnumWorldBorderAction.SET_WARNING_BLOCKS),
                        (World) worldborder.world);
            }

            public void b(WorldBorder worldborder, double d0) {
            }

            public void c(WorldBorder worldborder, double d0) {
            }
        });
        this.getServer().addWorld((org.bukkit.World) this.world);
        this.timings = new WorldTimingsHandler(this);
        this.keepSpawnInMemory = this.paperConfig.keepSpawnInMemory;
        this.entityLimiter = new TickLimiter(this.spigotConfig.entityMaxTickTime);
        this.tileLimiter = new TickLimiter(this.spigotConfig.tileMaxTickTime);
    }

    public World b() {
        return this;
    }

    public BiomeBase getBiome(BlockPosition blockposition) {
        if (this.isLoaded(blockposition)) {
            Chunk chunk = this.getChunkAtWorldCoords(blockposition);
            try {
                return chunk.getBiome(blockposition, this.worldProvider.k());
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Getting biome");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Coordinates of biome request");
                crashreportsystemdetails.a("Location", new CrashReportCallable<Object>() {
                    public String a() throws Exception {
                        return CrashReportSystemDetails.a(blockposition);
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                throw new ReportedException(crashreport);
            }
        }
        return this.worldProvider.k().getBiome(blockposition, Biomes.c);
    }

    public WorldChunkManager getWorldChunkManager() {
        return this.worldProvider.k();
    }

    protected abstract IChunkProvider n();

    public void a(WorldSettings worldsettings) {
        this.worldData.d(true);
    }

    @Nullable
    public MinecraftServer getMinecraftServer() {
        return null;
    }

    public IBlockData c(BlockPosition blockposition) {
        BlockPosition blockposition2;
        for (blockposition2 = new BlockPosition(blockposition.getX(), this.getSeaLevel(), blockposition.getZ()); !this
                .isEmpty(blockposition2.up()); blockposition2 = blockposition2.up()) {
        }
        return this.getType(blockposition2);
    }

    private static boolean isValidLocation(BlockPosition blockposition) {
        return blockposition.isValidLocation();
    }

    private static boolean E(BlockPosition blockposition) {
        return blockposition.isInvalidYLocation();
    }

    public boolean isEmpty(BlockPosition blockposition) {
        return this.getType(blockposition).getMaterial() == Material.AIR;
    }

    public boolean isLoaded(BlockPosition blockposition) {
        return this.getChunkIfLoaded(blockposition.getX() >> 4, blockposition.getZ() >> 4) != null;
    }

    public boolean a(BlockPosition blockposition, boolean flag) {
        return this.isChunkLoaded(blockposition.getX() >> 4, blockposition.getZ() >> 4, flag);
    }

    public boolean areChunksLoaded(BlockPosition blockposition, int i) {
        return this.areChunksLoaded(blockposition, i, true);
    }

    public boolean areChunksLoaded(BlockPosition blockposition, int i, boolean flag) {
        return this.isAreaLoaded(blockposition.getX() - i, blockposition.getY() - i, blockposition.getZ() - i,
                blockposition.getX() + i, blockposition.getY() + i, blockposition.getZ() + i, flag);
    }

    public boolean areChunksLoadedBetween(BlockPosition blockposition, BlockPosition blockposition1) {
        return this.areChunksLoadedBetween(blockposition, blockposition1, true);
    }

    public boolean areChunksLoadedBetween(BlockPosition blockposition, BlockPosition blockposition1, boolean flag) {
        return this.isAreaLoaded(blockposition.getX(), blockposition.getY(), blockposition.getZ(),
                blockposition1.getX(), blockposition1.getY(), blockposition1.getZ(), flag);
    }

    public boolean a(StructureBoundingBox structureboundingbox) {
        return this.b(structureboundingbox, true);
    }

    public boolean b(StructureBoundingBox structureboundingbox, boolean flag) {
        return this.isAreaLoaded(structureboundingbox.a, structureboundingbox.b, structureboundingbox.c,
                structureboundingbox.d, structureboundingbox.e, structureboundingbox.f, flag);
    }

    private boolean isAreaLoaded(int i, int j, int k, int l, int i1, int j1, boolean flag) {
        if (i1 >= 0 && j < 256) {
            i >>= 4;
            k >>= 4;
            l >>= 4;
            j1 >>= 4;
            for (int k2 = i; k2 <= l; ++k2) {
                for (int l2 = k; l2 <= j1; ++l2) {
                    if (!this.isChunkLoaded(k2, l2, flag)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    protected abstract boolean isChunkLoaded(int p0, int p1, boolean p2);

    public Chunk getChunkAtWorldCoords(BlockPosition blockposition) {
        return this.getChunkAt(blockposition.getX() >> 4, blockposition.getZ() >> 4);
    }

    public Chunk getChunkAt(int i, int j) {
        return this.chunkProvider.getChunkAt(i, j);
    }

    public boolean b(int i, int j) {
        return this.isChunkLoaded(i, j, false) || this.chunkProvider.e(i, j);
    }

    public boolean setTypeAndData(BlockPosition blockposition, IBlockData iblockdata, int i) {
        if (this.captureTreeGeneration) {
            BlockState blockstate = null;
            synchronized (capturedBlockStates) {
                Iterator<BlockState> it = this.capturedBlockStates.iterator();
                while (it.hasNext()) {
                    BlockState previous = it.next();
                    if (previous.getX() == blockposition.getX() && previous.getY() == blockposition.getY()
                            && previous.getZ() == blockposition.getZ()) {
                        blockstate = previous;
                        it.remove();
                        break;
                    }
                }
            }
            if (blockstate == null) {
                blockstate = (BlockState) CraftBlockState.getBlockState(this, blockposition.getX(),
                        blockposition.getY(), blockposition.getZ(), i);
            }
            blockstate.setTypeId(CraftMagicNumbers.getId(iblockdata.getBlock()));
            blockstate.setRawData((byte) iblockdata.getBlock().toLegacyData(iblockdata));
            this.capturedBlockStates.add(blockstate);
            return true;
        }
        if (blockposition.isInvalidYLocation()) {
            return false;
        }
        if (!this.isClientSide && this.worldData.getType() == WorldType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        }
        Chunk chunk = this.getChunkAtWorldCoords(blockposition);
        Block block = iblockdata.getBlock();
        BlockState blockstate2 = null;
        if (this.captureBlockStates) {
            blockstate2 = this.world.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ())
                    .getState();
            this.capturedBlockStates.add(blockstate2);
        }
        IBlockData iblockdata2 = chunk.a(blockposition, iblockdata);
        if (iblockdata2 == null) {
            if (this.captureBlockStates) {
                this.capturedBlockStates.remove(blockstate2);
            }
            return false;
        }
        if (iblockdata.c() != iblockdata2.c() || iblockdata.d() != iblockdata2.d()) {
            this.methodProfiler.a("checkLight");
            chunk.runOrQueueLightUpdate(() -> this.w(blockposition));
            this.methodProfiler.b();
        }
        if (!this.captureBlockStates) {
            this.notifyAndUpdatePhysics(blockposition, chunk, iblockdata2, iblockdata, i);
        }
        return true;
    }

    public void notifyAndUpdatePhysics(BlockPosition blockposition, Chunk chunk, IBlockData oldBlock,
                                       IBlockData newBlock, int i) {
        if ((i & 0x2) != 0x0 && (!this.isClientSide || (i & 0x4) == 0x0) && (chunk == null || chunk.isReady())) {
            this.notify(blockposition, oldBlock, newBlock, i);
        }
        if (!this.isClientSide && (i & 0x1) != 0x0) {
            this.update(blockposition, oldBlock.getBlock(), true);
            if (newBlock.n()) {
                this.updateAdjacentComparators(blockposition, newBlock.getBlock());
            }
        } else if (!this.isClientSide && (i & 0x10) == 0x0) {
            this.c(blockposition, newBlock.getBlock());
        }
    }

    public boolean setAir(BlockPosition blockposition) {
        return this.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
    }

    public boolean setAir(BlockPosition blockposition, boolean flag) {
        IBlockData iblockdata = this.getType(blockposition);
        Block block = iblockdata.getBlock();
        if (iblockdata.getMaterial() == Material.AIR) {
            return false;
        }
        this.triggerEffect(2001, blockposition, Block.getCombinedId(iblockdata));
        if (flag) {
            block.b(this, blockposition, iblockdata, 0);
        }
        return this.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
    }

    public boolean setTypeUpdate(BlockPosition blockposition, IBlockData iblockdata) {
        return this.setTypeAndData(blockposition, iblockdata, 3);
    }

    public void notify(BlockPosition blockposition, IBlockData iblockdata, IBlockData iblockdata1, int i) {
        for (int j = 0; j < this.u.size(); ++j) {
            this.u.get(j).a(this, blockposition, iblockdata, iblockdata1, i);
        }
    }

    public void update(BlockPosition blockposition, Block block, boolean flag) {
        if (this.worldData.getType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
            if (this.populating) {
                return;
            }
            this.applyPhysics(blockposition, block, flag);
        }
    }

    public void a(int i, int j, int k, int l) {
        if (k > l) {
            int i2 = l;
            l = k;
            k = i2;
        }
        if (this.worldProvider.m()) {
            for (int i2 = k; i2 <= l; ++i2) {
                this.c(EnumSkyBlock.SKY, new BlockPosition(i, i2, j));
            }
        }
        this.b(i, k, j, i, l, j);
    }

    public void b(BlockPosition blockposition, BlockPosition blockposition1) {
        this.b(blockposition.getX(), blockposition.getY(), blockposition.getZ(), blockposition1.getX(),
                blockposition1.getY(), blockposition1.getZ());
    }

    public void b(int i, int j, int k, int l, int i1, int j1) {
        for (int k2 = 0; k2 < this.u.size(); ++k2) {
            this.u.get(k2).a(i, j, k, l, i1, j1);
        }
    }

    public void c(BlockPosition blockposition, Block block) {
        this.b(blockposition.west(), block, blockposition);
        this.b(blockposition.east(), block, blockposition);
        this.b(blockposition.down(), block, blockposition);
        this.b(blockposition.up(), block, blockposition);
        this.b(blockposition.north(), block, blockposition);
        this.b(blockposition.south(), block, blockposition);
    }

    public void applyPhysics(BlockPosition blockposition, Block block, boolean flag) {
        if (this.captureBlockStates) {
            return;
        }
        this.a(blockposition.west(), block, blockposition);
        this.a(blockposition.east(), block, blockposition);
        this.a(blockposition.down(), block, blockposition);
        this.a(blockposition.up(), block, blockposition);
        this.a(blockposition.north(), block, blockposition);
        this.a(blockposition.south(), block, blockposition);
        if (flag) {
            this.c(blockposition, block);
        }
        this.chunkPacketBlockController.updateNearbyBlocks(this, blockposition);
    }

    public void a(BlockPosition blockposition, Block block, EnumDirection enumdirection) {
        if (enumdirection != EnumDirection.WEST) {
            this.a(blockposition.west(), block, blockposition);
        }
        if (enumdirection != EnumDirection.EAST) {
            this.a(blockposition.east(), block, blockposition);
        }
        if (enumdirection != EnumDirection.DOWN) {
            this.a(blockposition.down(), block, blockposition);
        }
        if (enumdirection != EnumDirection.UP) {
            this.a(blockposition.up(), block, blockposition);
        }
        if (enumdirection != EnumDirection.NORTH) {
            this.a(blockposition.north(), block, blockposition);
        }
        if (enumdirection != EnumDirection.SOUTH) {
            this.a(blockposition.south(), block, blockposition);
        }
    }

    public void a(BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        if (!this.isClientSide) {
            IBlockData iblockdata = this.getType(blockposition);
            try {
                CraftWorld world = ((WorldServer) this).getWorld();
                if (world != null && !((WorldServer) this).stopPhysicsEvent) {
                    BlockPhysicsEvent event = new BlockPhysicsEvent(
                            world.getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()),
                            CraftMagicNumbers.getId(block), blockposition1.getX(), blockposition1.getY(),
                            blockposition1.getZ());
                    this.getServer().getPluginManager().callEvent((Event) event);
                    if (event.isCancelled()) {
                        return;
                    }
                }
                iblockdata.doPhysics(this, blockposition, block, blockposition1);
            } catch (StackOverflowError stackoverflowerror) {
                World.haveWeSilencedAPhysicsCrash = true;
                World.blockLocation = blockposition.getX() + ", " + blockposition.getY() + ", " + blockposition.getZ();
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.a(throwable, "Exception while updating neighbours");
                CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being updated");
                crashreportsystemdetails.a("Source block type", new CrashReportCallable<Object>() {
                    public String a() throws Exception {
                        try {
                            return String.format("ID #%d (%s // %s)", Block.getId(block), block.a(),
                                    block.getClass().getCanonicalName());
                        } catch (Throwable throwable) {
                            return "ID #" + Block.getId(block);
                        }
                    }

                    public Object call() throws Exception {
                        return this.a();
                    }
                });
                CrashReportSystemDetails.a(crashreportsystemdetails, blockposition, iblockdata);
                throw new ReportedException(crashreport);
            }
        }
    }

    public void b(BlockPosition blockposition, Block block, BlockPosition blockposition1) {
        if (!this.isClientSide) {
            IBlockData iblockdata = this.getType(blockposition);
            if (iblockdata.getBlock() == Blocks.dk) {
                try {
                    ((BlockObserver) iblockdata.getBlock()).b(iblockdata, this, blockposition, block, blockposition1);
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.a(throwable, "Exception while updating neighbours");
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Block being updated");
                    crashreportsystemdetails.a("Source block type", new CrashReportCallable<Object>() {
                        public String a() throws Exception {
                            try {
                                return String.format("ID #%d (%s // %s)", Block.getId(block), block.a(),
                                        block.getClass().getCanonicalName());
                            } catch (Throwable throwable) {
                                return "ID #" + Block.getId(block);
                            }
                        }

                        public Object call() throws Exception {
                            return this.a();
                        }
                    });
                    CrashReportSystemDetails.a(crashreportsystemdetails, blockposition, iblockdata);
                    throw new ReportedException(crashreport);
                }
            }
        }
    }

    public boolean a(BlockPosition blockposition, Block block) {
        return false;
    }

    public boolean h(BlockPosition blockposition) {
        return this.getChunkAtWorldCoords(blockposition).c(blockposition);
    }

    public boolean i(BlockPosition blockposition) {
        if (blockposition.getY() >= this.getSeaLevel()) {
            return this.h(blockposition);
        }
        BlockPosition blockposition2 = new BlockPosition(blockposition.getX(), this.getSeaLevel(),
                blockposition.getZ());
        if (!this.h(blockposition2)) {
            return false;
        }
        for (blockposition2 = blockposition2.down(); blockposition2.getY() > blockposition
                .getY(); blockposition2 = blockposition2.down()) {
            IBlockData iblockdata = this.getType(blockposition2);
            if (iblockdata.c() > 0 && !iblockdata.getMaterial().isLiquid()) {
                return false;
            }
        }
        return true;
    }

    public int j(BlockPosition blockposition) {
        if (blockposition.getY() < 0) {
            return 0;
        }
        if (blockposition.getY() >= 256) {
            blockposition = new BlockPosition(blockposition.getX(), 255, blockposition.getZ());
        }
        return this.getChunkAtWorldCoords(blockposition).a(blockposition, 0);
    }

    public boolean isLightLevel(BlockPosition blockposition, int level) {
        if (!blockposition.isValidLocation()) {
            return true;
        }
        if (this.getType(blockposition).f()) {
            return this.c(blockposition.up(), false) >= level || this.c(blockposition.east(), false) >= level
                    || this.c(blockposition.west(), false) >= level || this.c(blockposition.south(), false) >= level
                    || this.c(blockposition.north(), false) >= level;
        }
        if (blockposition.getY() >= 256) {
            blockposition = new BlockPosition(blockposition.getX(), 255, blockposition.getZ());
        }
        Chunk chunk = this.getChunkAtWorldCoords(blockposition);
        return chunk.getLightSubtracted(blockposition, this.getSkylightSubtracted()) >= level;
    }

    public int getLightLevel(BlockPosition blockposition) {
        return this.c(blockposition, true);
    }

    public int getLight(BlockPosition blockposition, boolean checkNeighbors) {
        return this.c(blockposition, checkNeighbors);
    }

    public int c(BlockPosition blockposition, boolean flag) {
        if (blockposition.getX() < -30000000 || blockposition.getZ() < -30000000 || blockposition.getX() >= 30000000
                || blockposition.getZ() >= 30000000) {
            return 15;
        }
        if (flag && this.getType(blockposition).f()) {
            int i = this.c(blockposition.up(), false);
            int j = this.c(blockposition.east(), false);
            int k = this.c(blockposition.west(), false);
            int l = this.c(blockposition.south(), false);
            int i2 = this.c(blockposition.north(), false);
            if (j > i) {
                i = j;
            }
            if (k > i) {
                i = k;
            }
            if (l > i) {
                i = l;
            }
            if (i2 > i) {
                i = i2;
            }
            return i;
        }
        if (blockposition.getY() < 0) {
            return 0;
        }
        if (blockposition.getY() >= 256) {
            blockposition = new BlockPosition(blockposition.getX(), 255, blockposition.getZ());
        }
        if (!this.isLoaded(blockposition)) {
            return 0;
        }
        Chunk chunk = this.getChunkAtWorldCoords(blockposition);
        return chunk.a(blockposition, this.L);
    }

    public BlockPosition getHighestBlockYAt(BlockPosition blockposition) {
        return new BlockPosition(blockposition.getX(), this.c(blockposition.getX(), blockposition.getZ()),
                blockposition.getZ());
    }

    public int c(int i, int j) {
        int k;
        if (i >= -30000000 && j >= -30000000 && i < 30000000 && j < 30000000) {
            if (this.isChunkLoaded(i >> 4, j >> 4, true)) {
                k = this.getChunkAt(i >> 4, j >> 4).b(i & 0xF, j & 0xF);
            } else {
                k = 0;
            }
        } else {
            k = this.getSeaLevel() + 1;
        }
        return k;
    }

    @Deprecated
    public int d(int i, int j) {
        if (i < -30000000 || j < -30000000 || i >= 30000000 || j >= 30000000) {
            return this.getSeaLevel() + 1;
        }
        if (!this.isChunkLoaded(i >> 4, j >> 4, true)) {
            return 0;
        }
        Chunk chunk = this.getChunkAt(i >> 4, j >> 4);
        return chunk.w();
    }

    public int getBrightness(EnumSkyBlock enumskyblock, BlockPosition blockposition) {
        if (blockposition.getY() < 0) {
            blockposition = new BlockPosition(blockposition.getX(), 0, blockposition.getZ());
        }
        if (!blockposition.isValidLocation()) {
            return enumskyblock.c;
        }
        if (!this.isLoaded(blockposition)) {
            return enumskyblock.c;
        }
        Chunk chunk = this.getChunkAtWorldCoords(blockposition);
        return chunk.getBrightness(enumskyblock, blockposition);
    }

    public void a(EnumSkyBlock enumskyblock, BlockPosition blockposition, int i) {
        if (blockposition.isValidLocation() && this.isLoaded(blockposition)) {
            Chunk chunk = this.getChunkAtWorldCoords(blockposition);
            chunk.a(enumskyblock, blockposition, i);
            this.m(blockposition);
        }
    }

    public void m(BlockPosition blockposition) {
        for (int i = 0; i < this.u.size(); ++i) {
            this.u.get(i).a(blockposition);
        }
    }

    public float n(BlockPosition blockposition) {
        return this.worldProvider.o()[this.getLightLevel(blockposition)];
    }

    public IBlockData getTypeIfLoaded(BlockPosition blockposition) {
        int x = blockposition.getX();
        int y = blockposition.getY();
        int z = blockposition.getZ();
        if (this.captureTreeGeneration) {
            IBlockData previous = this.getCapturedBlockType(x, y, z);
            if (previous != null) {
                return previous;
            }
        }
        Chunk chunk = ((ChunkProviderServer) this.chunkProvider).getChunkIfLoaded(x >> 4, z >> 4);
        if (chunk != null) {
            return chunk.getBlockData(x, y, z);
        }
        return null;
    }

    public IBlockData getType(BlockPosition blockposition) {
        int x = blockposition.getX();
        int y = blockposition.getY();
        int z = blockposition.getZ();
        if (this.captureTreeGeneration) {
            IBlockData previous = this.getCapturedBlockType(x, y, z);
            if (previous != null) {
                return previous;
            }
        }
        return this.chunkProvider.getChunkAt(x >> 4, z >> 4).getBlockData(x, y, z);
    }

    private IBlockData getCapturedBlockType(int x, int y, int z) {
        synchronized (capturedBlockStates) {
            for (BlockState previous : this.capturedBlockStates) {
                if (previous.getX() == x && previous.getY() == y && previous.getZ() == z) {
                    return CraftMagicNumbers.getBlock(previous.getTypeId()).fromLegacyData((int) previous.getRawData());
                }
            }
            return null;
        }
    }

    public boolean D() {
        return this.L < 4;
    }

    @Nullable
    public MovingObjectPosition rayTrace(Vec3D vec3d, Vec3D vec3d1) {
        return this.rayTrace(vec3d, vec3d1, false, false, false);
    }

    @Nullable
    public MovingObjectPosition rayTrace(Vec3D vec3d, Vec3D vec3d1, boolean flag) {
        return this.rayTrace(vec3d, vec3d1, flag, false, false);
    }

    @Nullable
    public MovingObjectPosition rayTrace(Vec3D vec3d, final Vec3D vec3d1, final boolean flag, final boolean flag1,
                                         final boolean flag2) {
        if (Double.isNaN(vec3d.x) || Double.isNaN(vec3d.y) || Double.isNaN(vec3d.z)) {
            return null;
        }
        if (Double.isNaN(vec3d1.x) || Double.isNaN(vec3d1.y) || Double.isNaN(vec3d1.z)) {
            return null;
        }
        final int i = MathHelper.floor(vec3d1.x);
        final int j = MathHelper.floor(vec3d1.y);
        final int k = MathHelper.floor(vec3d1.z);
        int l = MathHelper.floor(vec3d.x);
        int i2 = MathHelper.floor(vec3d.y);
        int j2 = MathHelper.floor(vec3d.z);
        BlockPosition blockposition = new BlockPosition(l, i2, j2);
        final IBlockData iblockdata = this.getTypeIfLoaded(blockposition);
        if (iblockdata == null) {
            return null;
        }
        final Block block = iblockdata.getBlock();
        if ((!flag1 || iblockdata.d((IBlockAccess) this, blockposition) != Block.k) && block.a(iblockdata, flag)) {
            final MovingObjectPosition movingobjectposition = iblockdata.a(this, blockposition, vec3d, vec3d1);
            if (movingobjectposition != null) {
                return movingobjectposition;
            }
        }
        MovingObjectPosition movingobjectposition2 = null;
        int k2 = 200;
        while (k2-- >= 0) {
            if (Double.isNaN(vec3d.x) || Double.isNaN(vec3d.y) || Double.isNaN(vec3d.z)) {
                return null;
            }
            if (l == i && i2 == j && j2 == k) {
                return flag2 ? movingobjectposition2 : null;
            }
            boolean flag3 = true;
            boolean flag4 = true;
            boolean flag5 = true;
            double d0 = 999.0;
            double d2 = 999.0;
            double d3 = 999.0;
            if (i > l) {
                d0 = l + 1.0;
            } else if (i < l) {
                d0 = l + 0.0;
            } else {
                flag3 = false;
            }
            if (j > i2) {
                d2 = i2 + 1.0;
            } else if (j < i2) {
                d2 = i2 + 0.0;
            } else {
                flag4 = false;
            }
            if (k > j2) {
                d3 = j2 + 1.0;
            } else if (k < j2) {
                d3 = j2 + 0.0;
            } else {
                flag5 = false;
            }
            double d4 = 999.0;
            double d5 = 999.0;
            double d6 = 999.0;
            final double d7 = vec3d1.x - vec3d.x;
            final double d8 = vec3d1.y - vec3d.y;
            final double d9 = vec3d1.z - vec3d.z;
            if (flag3) {
                d4 = (d0 - vec3d.x) / d7;
            }
            if (flag4) {
                d5 = (d2 - vec3d.y) / d8;
            }
            if (flag5) {
                d6 = (d3 - vec3d.z) / d9;
            }
            if (d4 == -0.0) {
                d4 = -1.0E-4;
            }
            if (d5 == -0.0) {
                d5 = -1.0E-4;
            }
            if (d6 == -0.0) {
                d6 = -1.0E-4;
            }
            EnumDirection enumdirection;
            if (d4 < d5 && d4 < d6) {
                enumdirection = ((i > l) ? EnumDirection.WEST : EnumDirection.EAST);
                vec3d = new Vec3D(d0, vec3d.y + d8 * d4, vec3d.z + d9 * d4);
            } else if (d5 < d6) {
                enumdirection = ((j > i2) ? EnumDirection.DOWN : EnumDirection.UP);
                vec3d = new Vec3D(vec3d.x + d7 * d5, d2, vec3d.z + d9 * d5);
            } else {
                enumdirection = ((k > j2) ? EnumDirection.NORTH : EnumDirection.SOUTH);
                vec3d = new Vec3D(vec3d.x + d7 * d6, vec3d.y + d8 * d6, d3);
            }
            l = MathHelper.floor(vec3d.x) - ((enumdirection == EnumDirection.EAST) ? 1 : 0);
            i2 = MathHelper.floor(vec3d.y) - ((enumdirection == EnumDirection.UP) ? 1 : 0);
            j2 = MathHelper.floor(vec3d.z) - ((enumdirection == EnumDirection.SOUTH) ? 1 : 0);
            blockposition = new BlockPosition(l, i2, j2);
            final IBlockData iblockdata2 = this.getTypeIfLoaded(blockposition);
            if (iblockdata2 == null) {
                return null;
            }
            final Block block2 = iblockdata2.getBlock();
            if (flag1 && iblockdata2.getMaterial() != Material.PORTAL
                    && iblockdata2.d((IBlockAccess) this, blockposition) == Block.k) {
                continue;
            }
            if (block2.a(iblockdata2, flag)) {
                final MovingObjectPosition movingobjectposition3 = iblockdata2.a(this, blockposition, vec3d, vec3d1);
                if (movingobjectposition3 != null) {
                    return movingobjectposition3;
                }
                continue;
            } else {
                movingobjectposition2 = new MovingObjectPosition(MovingObjectPosition.EnumMovingObjectType.MISS, vec3d,
                        enumdirection, blockposition);
            }
        }
        return flag2 ? movingobjectposition2 : null;
    }

    public void a(@Nullable EntityHuman entityhuman, BlockPosition blockposition, SoundEffect soundeffect,
                  SoundCategory soundcategory, float f, float f1) {
        this.a(entityhuman, blockposition.getX() + 0.5, blockposition.getY() + 0.5, blockposition.getZ() + 0.5,
                soundeffect, soundcategory, f, f1);
    }

    public void sendSoundEffect(@Nullable EntityHuman fromEntity, double x, double y, double z, SoundEffect soundeffect,
                                SoundCategory soundcategory, float volume, float pitch) {
        this.a(fromEntity, x, y, z, soundeffect, soundcategory, volume, pitch);
    }

    public void a(@Nullable EntityHuman entityhuman, double d0, double d1, double d2, SoundEffect soundeffect,
                  SoundCategory soundcategory, float f, float f1) {
        for (int i = 0; i < this.u.size(); ++i) {
            this.u.get(i).a(entityhuman, soundeffect, soundcategory, d0, d1, d2, f, f1);
        }
    }

    public void a(double d0, double d1, double d2, SoundEffect soundeffect, SoundCategory soundcategory, float f,
                  float f1, boolean flag) {
    }

    public void a(BlockPosition blockposition, @Nullable SoundEffect soundeffect) {
        for (int i = 0; i < this.u.size(); ++i) {
            this.u.get(i).a(soundeffect, blockposition);
        }
    }

    public void addParticle(EnumParticle enumparticle, double d0, double d1, double d2, double d3, double d4, double d5,
                            int... aint) {
        this.a(enumparticle.c(), enumparticle.e(), d0, d1, d2, d3, d4, d5, aint);
    }

    public void a(int i, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
        for (int j = 0; j < this.u.size(); ++j) {
            this.u.get(j).a(i, false, true, d0, d1, d2, d3, d4, d5, aint);
        }
    }

    private void a(int i, boolean flag, double d0, double d1, double d2, double d3, double d4, double d5, int... aint) {
        for (int j = 0; j < this.u.size(); ++j) {
            this.u.get(j).a(i, flag, d0, d1, d2, d3, d4, d5, aint);
        }
    }

    public boolean strikeLightning(Entity entity) {
        this.j.add(entity);
        return true;
    }

    public boolean addEntity(Entity entity) {
        return this.addEntity(entity, CreatureSpawnEvent.SpawnReason.DEFAULT);
    }

    public boolean addEntity(Entity entity, CreatureSpawnEvent.SpawnReason spawnReason) {
        // AsyncCatcher.catchOp("entity add");
        if (entity == null) {
            return false;
        }
        if (entity.valid) {
            MinecraftServer.LOGGER.error("Attempted Double World add on " + entity, new Throwable());
            return true;
        }
        Cancellable event = null;
        if (entity instanceof EntityLiving && !(entity instanceof EntityPlayer)) {
            boolean isAnimal = entity instanceof EntityAnimal || entity instanceof EntityWaterAnimal
                    || entity instanceof EntityGolem;
            boolean isMonster = entity instanceof EntityMonster || entity instanceof EntityGhast
                    || entity instanceof EntitySlime;
            boolean isNpc = entity instanceof NPC;
            if (spawnReason != CreatureSpawnEvent.SpawnReason.CUSTOM && ((isAnimal && !this.allowAnimals)
                    || (isMonster && !this.allowMonsters) || (isNpc && !this.getServer().getServer().getSpawnNPCs()))) {
                entity.dead = true;
                return false;
            }
            event = (Cancellable) CraftEventFactory.callCreatureSpawnEvent((EntityLiving) entity, spawnReason);
        } else if (entity instanceof EntityItem) {
            event = (Cancellable) CraftEventFactory.callItemSpawnEvent((EntityItem) entity);
        } else if (entity.getBukkitEntity() instanceof Projectile) {
            event = (Cancellable) CraftEventFactory.callProjectileLaunchEvent(entity);
        } else if (entity.getBukkitEntity() instanceof Vehicle) {
            event = (Cancellable) CraftEventFactory.callVehicleCreateEvent(entity);
        } else if (entity instanceof EntityExperienceOrb) {
            EntityExperienceOrb xp = (EntityExperienceOrb) entity;
            double radius = this.spigotConfig.expMerge;
            if (radius > 0.0) {
                int maxValue = this.paperConfig.expMergeMaxValue;
                boolean mergeUnconditionally = this.paperConfig.expMergeMaxValue <= 0;
                if (mergeUnconditionally || xp.value < maxValue) {
                    List<Entity> entities = this.getEntities(entity,
                            entity.getBoundingBox().grow(radius, radius, radius));
                    for (Entity e : entities) {
                        if (e instanceof EntityExperienceOrb) {
                            EntityExperienceOrb loopItem = (EntityExperienceOrb) e;
                            if (loopItem.dead || (maxValue > 0 && loopItem.value >= maxValue)
                                    || !new ExperienceOrbMergeEvent((ExperienceOrb) entity.getBukkitEntity(),
                                    (ExperienceOrb) loopItem.getBukkitEntity()).callEvent()) {
                                continue;
                            }
                            final long newTotal = xp.value + loopItem.value;
                            if ((int) newTotal < 0) {
                                continue;
                            }
                            if (maxValue > 0 && newTotal > maxValue) {
                                loopItem.value = (int) (newTotal - maxValue);
                                xp.value = maxValue;
                            } else {
                                final EntityExperienceOrb entityExperienceOrb = xp;
                                entityExperienceOrb.value += loopItem.value;
                                loopItem.die();
                            }
                        }
                    }
                }
            }
        }
        if (event != null && (event.isCancelled() || entity.dead)) {
            entity.dead = true;
            return false;
        }
        int i = MathHelper.floor(entity.locX / 16.0);
        int j = MathHelper.floor(entity.locZ / 16.0);
        boolean flag = true;
        if (entity.origin == null) {
            entity.origin = entity.getBukkitEntity().getLocation();
        }
        if (entity instanceof EntityHuman) {
            flag = true;
        }
        if (!flag && !this.isChunkLoaded(i, j, false)) {
            return false;
        }
        if (entity instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) entity;
            this.players.add(entityhuman);
            this.everyoneSleeping();
        }
        this.getChunkAt(i, j).a(entity);
        if (entity.dead)
            return false;
        this.entityList.add(entity);
        this.b(entity);
        return true;
    }

    protected void b(Entity entity) {
        for (int i = 0; i < this.u.size(); ++i) {
            this.u.get(i).a(entity);
        }
        entity.valid = true;
        entity.shouldBeRemoved = false;
        new EntityAddToWorldEvent((org.bukkit.entity.Entity) entity.getBukkitEntity()).callEvent();
    }

    protected void c(Entity entity) {
        for (int i = 0; i < this.u.size(); ++i) {
            this.u.get(i).b(entity);
        }
        new EntityRemoveFromWorldEvent((org.bukkit.entity.Entity) entity.getBukkitEntity()).callEvent();
        entity.valid = false;
    }

    public void kill(Entity entity) {
        AsyncCatcher.catchOp("entity kill");
        if (entity.isVehicle()) {
            entity.ejectPassengers();
        }
        if (entity.isPassenger()) {
            entity.stopRiding();
        }
        entity.die();
        if (entity instanceof EntityHuman) {
            this.players.remove(entity);
            synchronized (worldMaps.c) {
                for (Object o : this.worldMaps.c) {
                    if (o instanceof WorldMap) {
                        WorldMap map = (WorldMap) o;
                        map.k.remove(entity);
                        // ~
                        synchronized (map.i) {
                            Iterator<WorldMap.WorldMapHumanTracker> iter = map.i.iterator();
                            while (iter.hasNext()) {
                                if (iter.next().trackee == entity) {
                                    map.decorations.remove(entity.getUniqueID());
                                    iter.remove();
                                }
                            }
                        }
                    }
                }
            }
            this.everyoneSleeping();
            this.c(entity);
        }
    }

    public void removeEntity(Entity entity) {
        // AsyncCatcher.catchOp("entity remove");
        entity.b(false);
        entity.die();
        if (entity instanceof EntityHuman) {
            this.players.remove(entity);
            this.everyoneSleeping();
        }
        final int i = entity.ab;
        final int j = entity.ad;
        if (entity.aa && this.isChunkLoaded(i, j, true)) {
            this.getChunkAt(i, j).b(entity);
        }
        entity.shouldBeRemoved = true;
//		if (!this.guardEntityList) {
        // int i = entity.ab;
        // int j = entity.ad;
        // if (entity.aa && this.isChunkLoaded(i, j, true)) {
        // this.getChunkAt(i, j).b(entity);
        // }
        int index = this.entityList.indexOf(entity);
        if (index != -1) {
            if (index <= this.tickPosition) {
                --this.tickPosition;
            }
            this.entityList.remove(index);
        }
//		}
        this.c(entity);
    }

    public void addIWorldAccess(IWorldAccess iworldaccess) {
        this.u.add(iworldaccess);
    }

    private boolean a(@Nullable Entity entity, AxisAlignedBB axisalignedbb, boolean flag,
                      @Nullable List<AxisAlignedBB> list) {
        int i = MathHelper.floor(axisalignedbb.a) - 1;
        int j = MathHelper.f(axisalignedbb.d) + 1;
        int k = MathHelper.floor(axisalignedbb.b) - 1;
        int l = MathHelper.f(axisalignedbb.e) + 1;
        int i2 = MathHelper.floor(axisalignedbb.c) - 1;
        int j2 = MathHelper.f(axisalignedbb.f) + 1;
        WorldBorder worldborder = this.getWorldBorder();
        boolean flag2 = entity != null && entity.bz();
        boolean flag3 = entity != null && this.g(entity);
        IBlockData iblockdata = Blocks.STONE.getBlockData();
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
        try {
            for (int k2 = i; k2 < j; ++k2) {
                for (int l2 = i2; l2 < j2; ++l2) {
                    boolean flag4 = k2 == i || k2 == j - 1;
                    boolean flag5 = l2 == i2 || l2 == j2 - 1;
                    if ((!flag4 || !flag5)
                            && this.isLoaded((BlockPosition) blockposition_pooledblockposition.f(k2, 64, l2))) {
                        for (int i3 = k; i3 < l; ++i3) {
                            if ((!flag4 && !flag5) || i3 != l - 1) {
                                if (flag) {
                                    if (k2 < -30000000 || k2 >= 30000000 || l2 < -30000000 || l2 >= 30000000) {
                                        boolean flag6 = true;
                                        return flag6;
                                    }
                                } else if (entity != null && flag2 == flag3) {
                                    entity.k(!flag3);
                                }
                                blockposition_pooledblockposition.f(k2, i3, l2);
                                IBlockData iblockdata2;
                                if (!flag && !worldborder.a((BlockPosition) blockposition_pooledblockposition)
                                        && flag3) {
                                    iblockdata2 = iblockdata;
                                } else {
                                    iblockdata2 = this.getType((BlockPosition) blockposition_pooledblockposition);
                                }
                                iblockdata2.a(this, (BlockPosition) blockposition_pooledblockposition, axisalignedbb,
                                        (List<AxisAlignedBB>) list, entity, false);
                                if (flag && !list.isEmpty()) {
                                    boolean flag7 = true;
                                    return flag7;
                                }
                            }
                        }
                    }
                }
            }
            return !list.isEmpty();
        } finally {
            blockposition_pooledblockposition.t();
        }
    }

    public List<AxisAlignedBB> getCubes(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        ArrayList<AxisAlignedBB> arraylist = Lists.newArrayList();
        this.a(entity, axisalignedbb, false, arraylist);
        if (entity != null) {
            if (entity instanceof EntityArmorStand && !entity.world.paperConfig.armorStandEntityLookups) {
                return (List<AxisAlignedBB>) arraylist;
            }
            List<?> list = this.getEntities(entity, axisalignedbb.g(0.25));
            for (int i = 0; i < list.size(); ++i) {
                Entity entity2 = (Entity) list.get(i);
                if (!entity.x(entity2)) {
                    AxisAlignedBB axisalignedbb2 = entity2.al();
                    if (axisalignedbb2 != null && axisalignedbb2.c(axisalignedbb)) {
                        arraylist.add(axisalignedbb2);
                    }
                    axisalignedbb2 = entity.j(entity2);
                    if (axisalignedbb2 != null && axisalignedbb2.c(axisalignedbb)) {
                        arraylist.add(axisalignedbb2);
                    }
                }
            }
        }
        return (List<AxisAlignedBB>) arraylist;
    }

    public boolean g(Entity entity) {
        double d0 = this.P.b();
        double d2 = this.P.c();
        double d3 = this.P.d();
        double d4 = this.P.e();
        if (entity.bz()) {
            ++d0;
            ++d2;
            --d3;
            --d4;
        } else {
            --d0;
            --d2;
            ++d3;
            ++d4;
        }
        return entity.locX > d0 && entity.locX < d3 && entity.locZ > d2 && entity.locZ < d4;
    }

    public boolean a(AxisAlignedBB axisalignedbb) {
        return this.a(null, axisalignedbb, true, Lists.newArrayList());
    }

    public int a(float f) {
        float f2 = this.c(f);
        float f3 = 1.0f - (MathHelper.cos(f2 * 6.2831855f) * 2.0f + 0.5f);
        f3 = MathHelper.a(f3, 0.0f, 1.0f);
        f3 = 1.0f - f3;
        f3 *= (float) (1.0 - this.j(f) * 5.0f / 16.0);
        f3 *= (float) (1.0 - this.h(f) * 5.0f / 16.0);
        f3 = 1.0f - f3;
        return (int) (f3 * 11.0f);
    }

    public float c(float f) {
        return this.worldProvider.a(this.worldData.getDayTime(), f);
    }

    public float G() {
        return WorldProvider.a[this.worldProvider.a(this.worldData.getDayTime())];
    }

    public float d(float f) {
        float f2 = this.c(f);
        return f2 * 6.2831855f;
    }

    public BlockPosition p(BlockPosition blockposition) {
        return this.getChunkAtWorldCoords(blockposition).f(blockposition);
    }

    public BlockPosition q(BlockPosition blockposition) {
        Chunk chunk = this.getChunkAtWorldCoords(blockposition);
        BlockPosition blockposition2;
        BlockPosition blockposition3;
        for (blockposition2 = new BlockPosition(blockposition.getX(), chunk.g() + 16,
                blockposition.getZ()); blockposition2.getY() >= 0; blockposition2 = blockposition3) {
            blockposition3 = blockposition2.down();
            Material material = chunk.getBlockData(blockposition3).getMaterial();
            if (material.isSolid() && material != Material.LEAVES) {
                break;
            }
        }
        return blockposition2;
    }

    public boolean b(BlockPosition blockposition, Block block) {
        return true;
    }

    public void a(BlockPosition blockposition, Block block, int i) {
    }

    public void a(BlockPosition blockposition, Block block, int i, int j) {
    }

    public void b(BlockPosition blockposition, Block block, int i, int j) {
    }

    Lock entityLocker = new ReentrantLock();

    public void tickEntities() {
        this.methodProfiler.a("entities");
        this.methodProfiler.a("global");
        for (int i = 0; i < this.j.size(); ++i) {
            Entity entity = this.j.get(i);
            if (entity != null) {
                try {
                    Entity entity3 = entity;
                    ++entity3.ticksLived;
                    entity.B_();
                } catch (Throwable throwable) {
                    CrashReport crashreport = CrashReport.a(throwable, "Ticking entity");
                    CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being ticked");
                    if (entity == null) {
                        crashreportsystemdetails.a("Entity", (Object) "~~NULL~~");
                    } else {
                        entity.appendEntityCrashDetails(crashreportsystemdetails);
                    }
                    throw new ReportedException(crashreport);
                }
                if (entity.dead) {
                    this.j.remove(i--);
                }
            }
        }
        this.methodProfiler.c("remove");
        this.timings.entityRemoval.startTiming();
        this.entityList.removeAll(this.f);
        for (Entity e : this.f) {
            Chunk chunk = e.isAddedToChunk() ? e.getCurrentChunk() : null;
            if (chunk != null) {
                chunk.removeEntity(e);
            }
        }
        for (Entity e : this.f) {
            this.c(e);
        }
        this.f.clear();
        this.l();
        this.timings.entityRemoval.stopTiming();
        this.methodProfiler.c("regular");
        try {
            ActivationRange.activateEntities(this);
        } catch (Exception ignored) {
        }
//		this.timings.entityTick.startTiming();
        new Thread(() -> {
            if (entityLocker.tryLock()) {
                try {
//					this.guardEntityList = true;
                    TimingHistory.entityTicks += this.entityList.size();
//					int entitiesThisCycle = 0;
                    this.tickPosition = 0;
                    while (this.tickPosition < this.entityList.size()) {
                        this.tickPosition = ((this.tickPosition < this.entityList.size()) ? this.tickPosition : 0);
                        Entity entity = this.entityList.get(this.tickPosition);
                        Entity entity2 = entity.bJ();
                        Label_0733:
                        {
                            if (entity2 != null) {
                                if (!entity2.dead && entity2.w(entity)) {
                                    break Label_0733;
                                }
                                entity.stopRiding();
                            }
                            this.methodProfiler.a("tick");
                            if (!entity.dead && !(entity instanceof EntityPlayer)) {
                                try {
//									entity.tickTimer.startTiming();
                                    this.h(entity);
//									entity.tickTimer.stopTiming();
                                } catch (Throwable throwable2) {
//									entity.tickTimer.stopTiming();
                                    String msg = "Entity threw exception at " + entity.world.getWorld().getName() + ":"
                                            + entity.locX + "," + entity.locY + "," + entity.locZ;
                                    System.err.println(msg);
                                    throwable2.printStackTrace();
                                    this.getServer().getPluginManager().callEvent((Event) new ServerExceptionEvent(
                                            (ServerException) new ServerInternalException(msg, throwable2)));
                                    entity.dead = true;
                                    break Label_0733;
                                }
                            }
                            this.methodProfiler.b();
                            this.methodProfiler.a("remove");
                            if (entity.dead) {
                                Chunk chunk2 = entity.isAddedToChunk() ? entity.getCurrentChunk() : null;
                                if (chunk2 != null) {
                                    chunk2.removeEntity(entity);
                                }
//								this.guardEntityList = false;
                                this.entityList.remove(this.tickPosition--);
//								this.guardEntityList = true;
                                this.c(entity);
                            }
                            this.methodProfiler.b();
                        }
                        ++this.tickPosition;
                    }
//					this.guardEntityList = false;
                } finally {
                    entityLocker.unlock();
                }
            }
        }, "Entity tick").start();
//		this.timings.entityTick.stopTiming();
        this.methodProfiler.c("blockEntities");
        this.timings.tileEntityTick.startTiming();
        if (!this.tileEntityListUnload.isEmpty()) {
            Set<TileEntity> toRemove = Collections.newSetFromMap(new IdentityHashMap<TileEntity, Boolean>());
            toRemove.addAll(this.tileEntityListUnload);
            this.tileEntityListTick.removeAll(toRemove);
            this.tileEntityListUnload.clear();
        }
        this.O = true;
        int tilesThisCycle = 0;
        this.tileTickPosition = 0;
        while (this.tileTickPosition < this.tileEntityListTick.size()) {
            this.tileTickPosition = ((this.tileTickPosition < this.tileEntityListTick.size()) ? this.tileTickPosition
                    : 0);
            TileEntity tileentity = this.tileEntityListTick.get(this.tileTickPosition);
            Label_1349:
            {
                if (tileentity == null) {
                    this.getServer().getLogger()
                            .severe("Spigot has detected a null entity and has removed it, preventing a crash");
                    --tilesThisCycle;
                    this.tileEntityListTick.remove(this.tileTickPosition--);
                } else {
                    if (!tileentity.y() && tileentity.u()) {
                        BlockPosition blockposition = tileentity.getPosition();
                        Chunk chunk3 = tileentity.getCurrentChunk();
                        boolean shouldTick = chunk3 != null;
                        if (this.paperConfig.skipEntityTickingInChunksScheduledForUnload) {
                            shouldTick = (shouldTick && !chunk3.isUnloading() && chunk3.scheduledForUnload == null);
                        }
                        if (shouldTick && this.P.a(blockposition)) {
                            try {
                                this.methodProfiler.a(() -> String
                                        .valueOf(TileEntity.a((Class<? extends TileEntity>) tileentity.getClass())));
                                tileentity.tickTimer.startTiming();
                                ((ITickable) tileentity).e();
                                this.methodProfiler.b();
                            } catch (Throwable throwable3) {
                                String msg2 = "TileEntity threw exception at " + tileentity.world.getWorld().getName()
                                        + ":" + tileentity.position.getX() + "," + tileentity.position.getY() + ","
                                        + tileentity.position.getZ();
                                System.err.println(msg2);
                                throwable3.printStackTrace();
                                this.getServer().getPluginManager().callEvent((Event) new ServerExceptionEvent(
                                        (ServerException) new ServerInternalException(msg2, throwable3)));
                                --tilesThisCycle;
                                this.tileEntityListTick.remove(this.tileTickPosition--);
                                break Label_1349;
                            } finally {
                                tileentity.tickTimer.stopTiming();
                            }
                        }
                    }
                    if (tileentity.y()) {
                        --tilesThisCycle;
                        this.tileEntityListTick.remove(this.tileTickPosition--);
                        Chunk chunk4 = tileentity.getCurrentChunk();
                        if (chunk4 != null) {
                            chunk4.removeTileEntity(tileentity.getPosition());
                        }
                    }
                }
            }
            ++this.tileTickPosition;
        }
        this.timings.tileEntityTick.stopTiming();
        this.timings.tileEntityPending.startTiming();
        this.O = false;
        this.methodProfiler.c("pendingBlockEntities");
        if (!this.b.isEmpty()) {
            for (int i2 = 0; i2 < this.b.size(); ++i2) {
                TileEntity tileentity2 = this.b.get(i2);
                if (!tileentity2.y() && this.isLoaded(tileentity2.getPosition())) {
                    Chunk chunk3 = this.getChunkAtWorldCoords(tileentity2.getPosition());
                    IBlockData iblockdata = chunk3.getBlockData(tileentity2.getPosition());
                    chunk3.a(tileentity2.getPosition(), tileentity2);
                    this.notify(tileentity2.getPosition(), iblockdata, iblockdata, 3);
                    this.a(tileentity2);
                }
            }
            this.b.clear();
        }
        this.timings.tileEntityPending.stopTiming();
        TimingHistory.tileEntityTicks += this.tileEntityListTick.size();
        this.methodProfiler.b();
        this.methodProfiler.b();
    }

    protected void l() {
    }

    public boolean a(TileEntity tileentity) {
        boolean flag = true;
        if (flag && tileentity instanceof ITickable && !this.tileEntityListTick.contains(tileentity)) {
            this.tileEntityListTick.add(tileentity);
        }
        if (this.isClientSide) {
            BlockPosition blockposition = tileentity.getPosition();
            IBlockData iblockdata = this.getType(blockposition);
            this.notify(blockposition, iblockdata, iblockdata, 2);
        }
        return flag;
    }

    public void b(Collection<TileEntity> collection) {
        if (this.O) {
            this.b.addAll(collection);
        } else {
            for (TileEntity tileentity : collection) {
                this.a(tileentity);
            }
        }
    }

    public void h(Entity entity) {
        this.entityJoinedWorld(entity, true);
    }

    public void entityJoinedWorld(Entity entity, boolean flag) {
        if (flag && !ActivationRange.checkIfActive(entity)) {
            ++entity.ticksLived;
            entity.inactiveTick();
            return;
        }
        entity.M = entity.locX;
        entity.N = entity.locY;
        entity.O = entity.locZ;
        entity.lastYaw = entity.yaw;
        entity.lastPitch = entity.pitch;
        if (flag && entity.aa) {
            ++entity.ticksLived;
            ++TimingHistory.activatedEntityTicks;
            if (entity.isPassenger()) {
                entity.aE();
            } else {
                entity.B_();
                entity.postTick();
            }
        }
        this.methodProfiler.a("chunkCheck");
        if (Double.isNaN(entity.locX) || Double.isInfinite(entity.locX)) {
            entity.locX = entity.M;
        }
        if (Double.isNaN(entity.locY) || Double.isInfinite(entity.locY)) {
            entity.locY = entity.N;
        }
        if (Double.isNaN(entity.locZ) || Double.isInfinite(entity.locZ)) {
            entity.locZ = entity.O;
        }
        if (Double.isNaN(entity.pitch) || Double.isInfinite(entity.pitch)) {
            entity.pitch = entity.lastPitch;
        }
        if (Double.isNaN(entity.yaw) || Double.isInfinite(entity.yaw)) {
            entity.yaw = entity.lastYaw;
        }
        int i = MathHelper.floor(entity.locX / 16.0);
        int j = Math.min(15, Math.max(0, MathHelper.floor(entity.locY / 16.0)));
        int k = MathHelper.floor(entity.locZ / 16.0);
        if (!entity.aa || entity.ab != i || entity.ac != j || entity.ad != k) {
            if (entity.aa && this.isChunkLoaded(entity.ab, entity.ad, true)) {
                this.getChunkAt(entity.ab, entity.ad).a(entity, entity.ac);
            }
            if (!entity.valid && !entity.bD() && !this.isChunkLoaded(i, k, true)) {
                entity.aa = false;
            } else {
                this.getChunkAt(i, k).a(entity);
            }
        }
        this.methodProfiler.b();
        if (flag && entity.aa) {
            for (Entity entity2 : entity.bF()) {
                if (!entity2.dead && entity2.bJ() == entity) {
                    this.h(entity2);
                } else {
                    entity2.stopRiding();
                }
            }
        }
    }

    public boolean b(AxisAlignedBB axisalignedbb) {
        return this.a(axisalignedbb, (Entity) null);
    }

    public boolean checkNoVisiblePlayerCollisions(AxisAlignedBB axisalignedbb, @Nullable Entity entity) {
        List<?> list = this.getEntities(null, axisalignedbb);
        for (int i = 0; i < list.size(); ++i) {
            Entity entity2 = (Entity) list.get(i);
            if (!(entity instanceof EntityPlayer) || !(entity2 instanceof EntityPlayer) || ((EntityPlayer) entity)
                    .getBukkitEntity().canSee((Player) ((EntityPlayer) entity2).getBukkitEntity())) {
                if (!entity2.dead && entity2.blocksEntitySpawning()) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean a(AxisAlignedBB axisalignedbb, @Nullable Entity entity) {
        List<?> list = this.getEntities(null, axisalignedbb);
        for (int i = 0; i < list.size(); ++i) {
            Entity entity2 = (Entity) list.get(i);
            if (!entity2.dead && entity2.i && entity2 != entity && (entity == null || entity2.x(entity))) {
                return false;
            }
        }
        return true;
    }

    public boolean c(AxisAlignedBB axisalignedbb) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i2 = MathHelper.floor(axisalignedbb.c);
        int j2 = MathHelper.f(axisalignedbb.f);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
        for (int k2 = i; k2 < j; ++k2) {
            for (int l2 = k; l2 < l; ++l2) {
                for (int i3 = i2; i3 < j2; ++i3) {
                    IBlockData iblockdata = this
                            .getType((BlockPosition) blockposition_pooledblockposition.f(k2, l2, i3));
                    if (iblockdata.getMaterial() != Material.AIR) {
                        blockposition_pooledblockposition.t();
                        return true;
                    }
                }
            }
        }
        blockposition_pooledblockposition.t();
        return false;
    }

    public boolean containsLiquid(AxisAlignedBB axisalignedbb) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i2 = MathHelper.floor(axisalignedbb.c);
        int j2 = MathHelper.f(axisalignedbb.f);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
        for (int k2 = i; k2 < j; ++k2) {
            for (int l2 = k; l2 < l; ++l2) {
                for (int i3 = i2; i3 < j2; ++i3) {
                    IBlockData iblockdata = this
                            .getType((BlockPosition) blockposition_pooledblockposition.f(k2, l2, i3));
                    if (iblockdata.getMaterial().isLiquid()) {
                        blockposition_pooledblockposition.t();
                        return true;
                    }
                }
            }
        }
        blockposition_pooledblockposition.t();
        return false;
    }

    public boolean e(AxisAlignedBB axisalignedbb) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i2 = MathHelper.floor(axisalignedbb.c);
        int j2 = MathHelper.f(axisalignedbb.f);
        if (this.isAreaLoaded(i, k, i2, j, l, j2, true)) {
            BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
            for (int k2 = i; k2 < j; ++k2) {
                for (int l2 = k; l2 < l; ++l2) {
                    for (int i3 = i2; i3 < j2; ++i3) {
                        Block block = this.getType((BlockPosition) blockposition_pooledblockposition.f(k2, l2, i3))
                                .getBlock();
                        if (block == Blocks.FIRE || block == Blocks.FLOWING_LAVA || block == Blocks.LAVA) {
                            blockposition_pooledblockposition.t();
                            return true;
                        }
                    }
                }
            }
            blockposition_pooledblockposition.t();
        }
        return false;
    }

    public boolean a(AxisAlignedBB axisalignedbb, Material material, Entity entity) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i2 = MathHelper.floor(axisalignedbb.c);
        int j2 = MathHelper.f(axisalignedbb.f);
        if (!this.isAreaLoaded(i, k, i2, j, l, j2, true)) {
            return false;
        }
        boolean flag = false;
        Vec3D vec3d = Vec3D.a;
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
        for (int k2 = i; k2 < j; ++k2) {
            for (int l2 = k; l2 < l; ++l2) {
                for (int i3 = i2; i3 < j2; ++i3) {
                    blockposition_pooledblockposition.f(k2, l2, i3);
                    IBlockData iblockdata = this.getType((BlockPosition) blockposition_pooledblockposition);
                    Block block = iblockdata.getBlock();
                    if (iblockdata.getMaterial() == material) {
                        double d0 = l2 + 1 - BlockFluids.b(iblockdata.get(BlockFluids.LEVEL));
                        if (l >= d0) {
                            flag = true;
                            vec3d = block.a(this, (BlockPosition) blockposition_pooledblockposition, entity, vec3d);
                        }
                    }
                }
            }
        }
        blockposition_pooledblockposition.t();
        if (vec3d.b() > 0.0 && entity.bo()) {
            vec3d = vec3d.a();
            double d2 = 0.014;
            entity.motX += vec3d.x * 0.014;
            entity.motY += vec3d.y * 0.014;
            entity.motZ += vec3d.z * 0.014;
        }
        return flag;
    }

    public boolean a(AxisAlignedBB axisalignedbb, Material material) {
        int i = MathHelper.floor(axisalignedbb.a);
        int j = MathHelper.f(axisalignedbb.d);
        int k = MathHelper.floor(axisalignedbb.b);
        int l = MathHelper.f(axisalignedbb.e);
        int i2 = MathHelper.floor(axisalignedbb.c);
        int j2 = MathHelper.f(axisalignedbb.f);
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
        for (int k2 = i; k2 < j; ++k2) {
            for (int l2 = k; l2 < l; ++l2) {
                for (int i3 = i2; i3 < j2; ++i3) {
                    if (this.getType((BlockPosition) blockposition_pooledblockposition.f(k2, l2, i3))
                            .getMaterial() == material) {
                        blockposition_pooledblockposition.t();
                        return true;
                    }
                }
            }
        }
        blockposition_pooledblockposition.t();
        return false;
    }

    public Explosion explode(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag) {
        return this.createExplosion(entity, d0, d1, d2, f, false, flag);
    }

    public Explosion createExplosion(@Nullable Entity entity, double d0, double d1, double d2, float f, boolean flag,
                                     boolean flag1) {
        Explosion explosion = new Explosion(this, entity, d0, d1, d2, f, flag, flag1);
        explosion.a();
        explosion.a(true);
        return explosion;
    }

    public float a(Vec3D vec3d, AxisAlignedBB axisalignedbb) {
        double d0 = 1.0 / ((axisalignedbb.d - axisalignedbb.a) * 2.0 + 1.0);
        double d2 = 1.0 / ((axisalignedbb.e - axisalignedbb.b) * 2.0 + 1.0);
        double d3 = 1.0 / ((axisalignedbb.f - axisalignedbb.c) * 2.0 + 1.0);
        double d4 = (1.0 - Math.floor(1.0 / d0) * d0) / 2.0;
        double d5 = (1.0 - Math.floor(1.0 / d3) * d3) / 2.0;
        if (d0 >= 0.0 && d2 >= 0.0 && d3 >= 0.0) {
            int i = 0;
            int j = 0;
            for (float f = 0.0f; f <= 1.0f; f += (float) d0) {
                for (float f2 = 0.0f; f2 <= 1.0f; f2 += (float) d2) {
                    for (float f3 = 0.0f; f3 <= 1.0f; f3 += (float) d3) {
                        double d6 = axisalignedbb.a + (axisalignedbb.d - axisalignedbb.a) * f;
                        double d7 = axisalignedbb.b + (axisalignedbb.e - axisalignedbb.b) * f2;
                        double d8 = axisalignedbb.c + (axisalignedbb.f - axisalignedbb.c) * f3;
                        if (this.rayTrace(new Vec3D(d6 + d4, d7, d8 + d5), vec3d) == null) {
                            ++i;
                        }
                        ++j;
                    }
                }
            }
            return i / j;
        }
        return 0.0f;
    }

    public boolean douseFire(@Nullable EntityHuman entityhuman, BlockPosition blockposition,
                             EnumDirection enumdirection) {
        blockposition = blockposition.shift(enumdirection);
        if (this.getType(blockposition).getBlock() == Blocks.FIRE) {
            this.a(entityhuman, 1009, blockposition, 0);
            this.setAir(blockposition);
            return true;
        }
        return false;
    }

    @Nullable
    public TileEntity getTileEntity(BlockPosition blockposition) {
        if (blockposition.isInvalidYLocation()) {
            return null;
        }
        if (this.capturedTileEntities.containsKey(blockposition)) {
            return this.capturedTileEntities.get(blockposition);
        }
        TileEntity tileentity = null;
        if (this.O) {
            tileentity = this.F(blockposition);
        }
        if (tileentity == null) {
            tileentity = this.getChunkAtWorldCoords(blockposition).a(blockposition,
                    Chunk.EnumTileEntityState.IMMEDIATE);
        }
        if (tileentity == null) {
            tileentity = this.F(blockposition);
        }
        return tileentity;
    }

    @Nullable
    private TileEntity F(BlockPosition blockposition) {
        for (int i = 0; i < this.b.size(); ++i) {
            TileEntity tileentity = this.b.get(i);
            if (!tileentity.y() && tileentity.getPosition().equals((Object) blockposition)) {
                return tileentity;
            }
        }
        return null;
    }

    public void setTileEntity(BlockPosition blockposition, @Nullable TileEntity tileentity) {
        if (!blockposition.isInvalidYLocation() && tileentity != null && !tileentity.y()) {
            if (this.captureBlockStates) {
                tileentity.a(this);
                tileentity.setPosition(blockposition);
                this.capturedTileEntities.put(blockposition, tileentity);
                return;
            }
            if (this.O) {
                tileentity.setPosition(blockposition);
                Iterator<TileEntity> iterator = this.b.iterator();
                while (iterator.hasNext()) {
                    TileEntity tileentity2 = (TileEntity) iterator.next();
                    if (tileentity2.getPosition().equals((Object) blockposition)) {
                        tileentity2.z();
                        iterator.remove();
                    }
                }
                tileentity.a(this);
                this.b.add(tileentity);
            } else {
                this.getChunkAtWorldCoords(blockposition).a(blockposition, tileentity);
                this.a(tileentity);
            }
        }
    }

    public void s(BlockPosition blockposition) {
        TileEntity tileentity = this.getTileEntity(blockposition);
        if (tileentity != null && this.O) {
            tileentity.z();
            this.b.remove(tileentity);
        } else {
            if (tileentity != null) {
                this.b.remove(tileentity);
                this.tileEntityListTick.remove(tileentity);
            }
            this.getChunkAtWorldCoords(blockposition).d(blockposition);
        }
    }

    public void b(TileEntity tileentity) {
        this.tileEntityListUnload.add(tileentity);
    }

    public boolean t(BlockPosition blockposition) {
        AxisAlignedBB axisalignedbb = this.getType(blockposition).d((IBlockAccess) this, blockposition);
        return axisalignedbb != Block.k && axisalignedbb.a() >= 1.0;
    }

    public boolean d(BlockPosition blockposition, boolean flag) {
        if (blockposition.isInvalidYLocation()) {
            return false;
        }
        Chunk chunk = this.chunkProvider.getLoadedChunkAt(blockposition.getX() >> 4, blockposition.getZ() >> 4);
        if (chunk != null && !chunk.isEmpty()) {
            IBlockData iblockdata = this.getType(blockposition);
            return iblockdata.getMaterial().k() && iblockdata.g();
        }
        return flag;
    }

    public void J() {
        int i = this.a(1.0f);
        if (i != this.L) {
            this.L = i;
        }
    }

    public void setSpawnFlags(boolean flag, boolean flag1) {
        this.allowMonsters = flag;
        this.allowAnimals = flag1;
    }

    public void doTick() {
        this.t();
    }

    protected void K() {
        if (this.worldData.hasStorm()) {
            this.o = 1.0f;
            if (this.worldData.isThundering()) {
                this.q = 1.0f;
            }
        }
    }

    protected void t() {
        if (this.worldProvider.m() && !this.isClientSide) {
            boolean flag = this.getGameRules().getBoolean("doWeatherCycle");
            if (flag) {
                int i = this.worldData.z();
                if (i > 0) {
                    --i;
                    this.worldData.i(i);
                    this.worldData.setThunderDuration(this.worldData.isThundering() ? 1 : 2);
                    this.worldData.setWeatherDuration(this.worldData.hasStorm() ? 1 : 2);
                }
                int j = this.worldData.getThunderDuration();
                if (j <= 0) {
                    if (this.worldData.isThundering()) {
                        this.worldData.setThunderDuration(this.random.nextInt(12000) + 3600);
                    } else {
                        this.worldData.setThunderDuration(this.random.nextInt(168000) + 12000);
                    }
                } else {
                    --j;
                    this.worldData.setThunderDuration(j);
                    if (j <= 0) {
                        this.worldData.setThundering(!this.worldData.isThundering());
                    }
                }
                int k = this.worldData.getWeatherDuration();
                if (k <= 0) {
                    if (this.worldData.hasStorm()) {
                        this.worldData.setWeatherDuration(this.random.nextInt(12000) + 12000);
                    } else {
                        this.worldData.setWeatherDuration(this.random.nextInt(168000) + 12000);
                    }
                } else {
                    --k;
                    this.worldData.setWeatherDuration(k);
                    if (k <= 0) {
                        this.worldData.setStorm(!this.worldData.hasStorm());
                    }
                }
            }
            this.p = this.q;
            if (this.worldData.isThundering()) {
                this.q += 0.01;
            } else {
                this.q -= 0.01;
            }
            this.q = MathHelper.a(this.q, 0.0f, 1.0f);
            this.n = this.o;
            if (this.worldData.hasStorm()) {
                this.o += 0.01;
            } else {
                this.o -= 0.01;
            }
            this.o = MathHelper.a(this.o, 0.0f, 1.0f);
            for (int idx = 0; idx < this.players.size(); ++idx) {
                if (this.players.get(idx).world == this) {
                    ((EntityPlayer) this.players.get(idx)).tickWeather();
                }
            }
        }
    }

    protected void j() {
    }

    public void a(BlockPosition blockposition, IBlockData iblockdata, Random random) {
        this.d = true;
        iblockdata.getBlock().b(this, blockposition, iblockdata, random);
        this.d = false;
    }

    public boolean u(BlockPosition blockposition) {
        return this.e(blockposition, false);
    }

    public boolean v(BlockPosition blockposition) {
        return this.e(blockposition, true);
    }

    public boolean e(BlockPosition blockposition, boolean flag) {
        BiomeBase biomebase = this.getBiome(blockposition);
        float f = biomebase.a(blockposition);
        if (f >= 0.15f) {
            return false;
        }
        if (blockposition.getY() >= 0 && blockposition.getY() < 256
                && this.getBrightness(EnumSkyBlock.BLOCK, blockposition) < 10) {
            IBlockData iblockdata = this.getType(blockposition);
            Block block = iblockdata.getBlock();
            if ((block == Blocks.WATER || block == Blocks.FLOWING_WATER) && iblockdata.get(BlockFluids.LEVEL) == 0) {
                if (!flag) {
                    return true;
                }
                boolean flag2 = this.G(blockposition.west()) && this.G(blockposition.east())
                        && this.G(blockposition.north()) && this.G(blockposition.south());
                if (!flag2) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean G(BlockPosition blockposition) {
        return this.getType(blockposition).getMaterial() == Material.WATER;
    }

    public boolean f(BlockPosition blockposition, boolean flag) {
        BiomeBase biomebase = this.getBiome(blockposition);
        float f = biomebase.a(blockposition);
        if (f >= 0.15f) {
            return false;
        }
        if (!flag) {
            return true;
        }
        if (blockposition.getY() >= 0 && blockposition.getY() < 256
                && this.getBrightness(EnumSkyBlock.BLOCK, blockposition) < 10) {
            IBlockData iblockdata = this.getType(blockposition);
            if (iblockdata.getMaterial() == Material.AIR && Blocks.SNOW_LAYER.canPlace(this, blockposition)) {
                return true;
            }
        }
        return false;
    }

    public boolean w(BlockPosition blockposition) {
        boolean flag = false;
        if (this.worldProvider.m()) {
            flag |= this.c(EnumSkyBlock.SKY, blockposition);
        }
        flag |= this.c(EnumSkyBlock.BLOCK, blockposition);
        return flag;
    }

    private int a(BlockPosition blockposition, EnumSkyBlock enumskyblock) {
        if (enumskyblock == EnumSkyBlock.SKY && this.h(blockposition)) {
            return 15;
        }
        IBlockData iblockdata = this.getType(blockposition);
        int i = (enumskyblock == EnumSkyBlock.SKY) ? 0 : iblockdata.d();
        int j = iblockdata.c();
        if (j >= 15 && iblockdata.d() > 0) {
            j = 1;
        }
        if (j < 1) {
            j = 1;
        }
        if (j >= 15) {
            return 0;
        }
        if (i >= 14) {
            return i;
        }
        BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition.s();
        try {
            for (EnumDirection enumdirection : EnumDirection.values()) {
                blockposition_pooledblockposition.j((BaseBlockPosition) blockposition).d(enumdirection);
                int i2 = this.getBrightness(enumskyblock, (BlockPosition) blockposition_pooledblockposition) - j;
                if (i2 > i) {
                    i = i2;
                }
                if (i >= 14) {
                    int j2 = i;
                    return j2;
                }
            }
            return i;
        } finally {
            blockposition_pooledblockposition.t();
        }
    }

    public boolean c(EnumSkyBlock enumskyblock, BlockPosition blockposition) {
        Chunk chunk = this.getChunkIfLoaded(blockposition.getX() >> 4, blockposition.getZ() >> 4);
        if (chunk == null || !chunk.areNeighborsLoaded(1)) {
            return false;
        }
        int i = 0;
        int j = 0;
        this.methodProfiler.a("getBrightness");
        int k = this.getBrightness(enumskyblock, blockposition);
        int l = this.a(blockposition, enumskyblock);
        int i2 = blockposition.getX();
        int j2 = blockposition.getY();
        int k2 = blockposition.getZ();
        if (l > k) {
            this.J[j++] = 133152;
        } else if (l < k) {
            this.J[j++] = (0x20820 | k << 18);
            while (i < j) {
                int l2 = this.J[i++];
                int i3 = (l2 & 0x3F) - 32 + i2;
                int j3 = (l2 >> 6 & 0x3F) - 32 + j2;
                int k3 = (l2 >> 12 & 0x3F) - 32 + k2;
                int l3 = l2 >> 18 & 0xF;
                BlockPosition blockposition2 = new BlockPosition(i3, j3, k3);
                int l4 = this.getBrightness(enumskyblock, blockposition2);
                if (l4 == l3) {
                    this.a(enumskyblock, blockposition2, 0);
                    if (l3 <= 0) {
                        continue;
                    }
                    int i4 = MathHelper.a(i3 - i2);
                    int j4 = MathHelper.a(j3 - j2);
                    int k4 = MathHelper.a(k3 - k2);
                    if (i4 + j4 + k4 >= 17) {
                        continue;
                    }
                    BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                            .s();
                    for (EnumDirection enumdirection : EnumDirection.values()) {
                        int k5 = i3 + enumdirection.getAdjacentX();
                        int l5 = j3 + enumdirection.getAdjacentY();
                        int i6 = k3 + enumdirection.getAdjacentZ();
                        blockposition_pooledblockposition.f(k5, l5, i6);
                        int j6 = Math.max(1, this.getType((BlockPosition) blockposition_pooledblockposition).c());
                        l4 = this.getBrightness(enumskyblock, (BlockPosition) blockposition_pooledblockposition);
                        if (l4 == l3 - j6 && j < this.J.length) {
                            this.J[j++] = (k5 - i2 + 32 | l5 - j2 + 32 << 6 | i6 - k2 + 32 << 12 | l3 - j6 << 18);
                        }
                    }
                    blockposition_pooledblockposition.t();
                }
            }
            i = 0;
        }
        this.methodProfiler.b();
        this.methodProfiler.a("checkedPosition < toCheckCount");
        while (i < j) {
            int l2 = this.J[i++];
            int i3 = (l2 & 0x3F) - 32 + i2;
            int j3 = (l2 >> 6 & 0x3F) - 32 + j2;
            int k3 = (l2 >> 12 & 0x3F) - 32 + k2;
            BlockPosition blockposition3 = new BlockPosition(i3, j3, k3);
            int k6 = this.getBrightness(enumskyblock, blockposition3);
            int l4 = this.a(blockposition3, enumskyblock);
            if (l4 != k6) {
                this.a(enumskyblock, blockposition3, l4);
                if (l4 <= k6) {
                    continue;
                }
                int i4 = Math.abs(i3 - i2);
                int j4 = Math.abs(j3 - j2);
                int k4 = Math.abs(k3 - k2);
                boolean flag = j < this.J.length - 6;
                if (i4 + j4 + k4 >= 17 || !flag) {
                    continue;
                }
                if (this.getBrightness(enumskyblock, blockposition3.west()) < l4) {
                    this.J[j++] = i3 - 1 - i2 + 32 + (j3 - j2 + 32 << 6) + (k3 - k2 + 32 << 12);
                }
                if (this.getBrightness(enumskyblock, blockposition3.east()) < l4) {
                    this.J[j++] = i3 + 1 - i2 + 32 + (j3 - j2 + 32 << 6) + (k3 - k2 + 32 << 12);
                }
                if (this.getBrightness(enumskyblock, blockposition3.down()) < l4) {
                    this.J[j++] = i3 - i2 + 32 + (j3 - 1 - j2 + 32 << 6) + (k3 - k2 + 32 << 12);
                }
                if (this.getBrightness(enumskyblock, blockposition3.up()) < l4) {
                    this.J[j++] = i3 - i2 + 32 + (j3 + 1 - j2 + 32 << 6) + (k3 - k2 + 32 << 12);
                }
                if (this.getBrightness(enumskyblock, blockposition3.north()) < l4) {
                    this.J[j++] = i3 - i2 + 32 + (j3 - j2 + 32 << 6) + (k3 - 1 - k2 + 32 << 12);
                }
                if (this.getBrightness(enumskyblock, blockposition3.south()) >= l4) {
                    continue;
                }
                this.J[j++] = i3 - i2 + 32 + (j3 - j2 + 32 << 6) + (k3 + 1 - k2 + 32 << 12);
            }
        }
        this.methodProfiler.b();
        return true;
    }

    public boolean a(boolean flag) {
        return false;
    }

    @Nullable
    public List<NextTickListEntry> a(Chunk chunk, boolean flag) {
        return null;
    }

    @Nullable
    public List<NextTickListEntry> a(StructureBoundingBox structureboundingbox, boolean flag) {
        return null;
    }

    public List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb) {
        return this.getEntities(entity, axisalignedbb, (Predicate<? super Entity>) IEntitySelector.e);
    }

    public List<Entity> getEntities(@Nullable Entity entity, AxisAlignedBB axisalignedbb,
                                    @Nullable Predicate<? super Entity> predicate) {
        ArrayList<Entity> arraylist = Lists.newArrayList();
        int i = MathHelper.floor((axisalignedbb.a - 2.0) / 16.0);
        int j = MathHelper.floor((axisalignedbb.d + 2.0) / 16.0);
        int k = MathHelper.floor((axisalignedbb.c - 2.0) / 16.0);
        int l = MathHelper.floor((axisalignedbb.f + 2.0) / 16.0);
        for (int i2 = i; i2 <= j; ++i2) {
            for (int j2 = k; j2 <= l; ++j2) {
                if (this.isChunkLoaded(i2, j2, true)) {
                    this.getChunkAt(i2, j2).a(entity, axisalignedbb, arraylist, predicate);
                }
            }
        }
        return (List<Entity>) arraylist;
    }

    public <T extends Entity> List<T> a(Class<? extends T> oclass, Predicate<? super T> predicate) {
        ArrayList<T> arraylist = Lists.newArrayList();
        synchronized (entityList) {
            for (Entity entity : this.entityList) {
                if (entity.shouldBeRemoved) {
                    continue;
                }
                if (!oclass.isAssignableFrom(entity.getClass()) || !predicate.apply((T) entity)) {
                    continue;
                }
                arraylist.add((T) entity);
            }
        }
        return arraylist;
    }

    public <T extends Entity> List<T> b(Class<? extends T> oclass, Predicate<? super T> predicate) {
        ArrayList<T> arraylist = Lists.newArrayList();
        synchronized (players) {
            for (Entity entity : this.players) {
                if (oclass.isAssignableFrom(entity.getClass()) && predicate.apply((T) entity)) {
                    arraylist.add((T) entity);
                }
            }
        }
        return arraylist;
    }

    public <T extends Entity> List<T> a(Class<? extends T> oclass, AxisAlignedBB axisalignedbb) {
        return this.a(oclass, axisalignedbb, (com.google.common.base.Predicate<? super T>) IEntitySelector.e);
    }

    public <T extends Entity> List<T> a(Class<? extends T> oclass, AxisAlignedBB axisalignedbb,
                                        @Nullable Predicate<? super T> predicate) {
        int i = MathHelper.floor((axisalignedbb.a - 2.0) / 16.0);
        int j = MathHelper.f((axisalignedbb.d + 2.0) / 16.0);
        int k = MathHelper.floor((axisalignedbb.c - 2.0) / 16.0);
        int l = MathHelper.f((axisalignedbb.f + 2.0) / 16.0);
        ArrayList<T> arraylist = Lists.newArrayList();
        for (int i2 = i; i2 < j; ++i2) {
            for (int j2 = k; j2 < l; ++j2) {
                if (this.isChunkLoaded(i2, j2, true)) {
                    this.getChunkAt(i2, j2).a((Class<? extends T>) oclass, axisalignedbb, (List<T>) arraylist,
                            (Predicate<? super T>) predicate);
                }
            }
        }
        return (List<T>) arraylist;
    }

    @Nullable
    public <T extends Entity> T a(Class<? extends T> oclass, AxisAlignedBB axisalignedbb, T t0) {
        List<?> list = this.a((Class<? extends Entity>) oclass, axisalignedbb);
        Entity entity = null;
        double d0 = Double.MAX_VALUE;
        for (int i = 0; i < list.size(); ++i) {
            Entity entity2 = (Entity) list.get(i);
            if (entity2 != t0 && IEntitySelector.e.apply((T) entity2)) {
                double d2 = t0.h(entity2);
                if (d2 <= d0) {
                    entity = entity2;
                    d0 = d2;
                }
            }
        }
        return (T) entity;
    }

    @Nullable
    public Entity getEntity(int i) {
        return (Entity) this.entitiesById.get(i);
    }

    public void b(BlockPosition blockposition, TileEntity tileentity) {
        if (this.isLoaded(blockposition)) {
            this.getChunkAtWorldCoords(blockposition).markDirty();
        }
    }

    public int a(Class<?> oclass) {
        int i = 0;
        synchronized (entityList) {
            for (Entity entity : this.entityList) {
                if (entity.shouldBeRemoved)
                    continue;
                if (entity instanceof EntityInsentient) {
                    EntityInsentient entityinsentient = (EntityInsentient) entity;
                    if (entityinsentient.isTypeNotPersistent() && entityinsentient.isPersistent()) {
                        continue;
                    }
                }
                if (oclass.isAssignableFrom(entity.getClass())) {
                    ++i;
                }
            }
        }
        return i;
    }

    public void addChunkEntities(final Collection<Entity> collection) {
        this.a(collection);
    }

    public void a(Collection<Entity> collection) {
        AsyncCatcher.catchOp("entity world add");
        for (Entity entity : collection) {
            if (entity != null && !entity.dead) {
                if (entity.valid) {
                    continue;
                }
                this.entityList.add(entity);
                this.b(entity);
            }
        }
    }

    public void c(Collection<Entity> collection) {
        this.f.addAll(collection);
    }

    public boolean a(Block block, BlockPosition blockposition, boolean flag, EnumDirection enumdirection,
                     @Nullable Entity entity) {
        IBlockData iblockdata = this.getType(blockposition);
        AxisAlignedBB axisalignedbb = flag ? null : block.getBlockData().d((IBlockAccess) this, blockposition);
        boolean defaultReturn = (axisalignedbb == Block.k
                || this.checkNoVisiblePlayerCollisions(axisalignedbb.a(blockposition), entity))
                && ((iblockdata.getMaterial() == Material.ORIENTABLE && block == Blocks.ANVIL)
                || (iblockdata.getMaterial().isReplaceable()
                && block.canPlace(this, blockposition, enumdirection)));
        BlockCanBuildEvent event = new BlockCanBuildEvent(
                this.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()),
                CraftMagicNumbers.getId(block), defaultReturn);
        this.getServer().getPluginManager().callEvent((Event) event);
        return event.isBuildable();
    }

    public int getSeaLevel() {
        return this.a;
    }

    public void b(int i) {
        this.a = i;
    }

    public int getBlockPower(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getType(blockposition).b((IBlockAccess) this, blockposition, enumdirection);
    }

    public WorldType N() {
        return this.worldData.getType();
    }

    public int getBlockPower(BlockPosition blockposition) {
        byte b0 = 0;
        int i = Math.max(b0, this.getBlockPower(blockposition.down(), EnumDirection.DOWN));
        if (i >= 15) {
            return i;
        }
        i = Math.max(i, this.getBlockPower(blockposition.up(), EnumDirection.UP));
        if (i >= 15) {
            return i;
        }
        i = Math.max(i, this.getBlockPower(blockposition.north(), EnumDirection.NORTH));
        if (i >= 15) {
            return i;
        }
        i = Math.max(i, this.getBlockPower(blockposition.south(), EnumDirection.SOUTH));
        if (i >= 15) {
            return i;
        }
        i = Math.max(i, this.getBlockPower(blockposition.west(), EnumDirection.WEST));
        if (i >= 15) {
            return i;
        }
        i = Math.max(i, this.getBlockPower(blockposition.east(), EnumDirection.EAST));
        return (i >= 15) ? i : i;
    }

    public boolean isBlockFacePowered(BlockPosition blockposition, EnumDirection enumdirection) {
        return this.getBlockFacePower(blockposition, enumdirection) > 0;
    }

    public int getBlockFacePower(BlockPosition blockposition, EnumDirection enumdirection) {
        IBlockData iblockdata = this.getType(blockposition);
        return iblockdata.l() ? this.getBlockPower(blockposition)
                : iblockdata.a((IBlockAccess) this, blockposition, enumdirection);
    }

    public boolean isBlockIndirectlyPowered(BlockPosition blockposition) {
        return this.getBlockFacePower(blockposition.down(), EnumDirection.DOWN) > 0
                || this.getBlockFacePower(blockposition.up(), EnumDirection.UP) > 0
                || this.getBlockFacePower(blockposition.north(), EnumDirection.NORTH) > 0
                || this.getBlockFacePower(blockposition.south(), EnumDirection.SOUTH) > 0
                || this.getBlockFacePower(blockposition.west(), EnumDirection.WEST) > 0
                || this.getBlockFacePower(blockposition.east(), EnumDirection.EAST) > 0;
    }

    public int z(BlockPosition blockposition) {
        int i = 0;
        for (EnumDirection enumdirection : EnumDirection.values()) {
            int l = this.getBlockFacePower(blockposition.shift(enumdirection), enumdirection);
            if (l >= 15) {
                return 15;
            }
            if (l > i) {
                i = l;
            }
        }
        return i;
    }

    @Nullable
    public EntityHuman findNearbyPlayer(Entity entity, double d0) {
        return this.a(entity.locX, entity.locY, entity.locZ, d0, false);
    }

    @Nullable
    public EntityHuman b(Entity entity, double d0) {
        return this.a(entity.locX, entity.locY, entity.locZ, d0, true);
    }

    @Nullable
    public EntityHuman a(double d0, double d1, double d2, double d3, boolean flag) {
        Predicate<Entity> predicate = flag ? IEntitySelector.d : IEntitySelector.e;
        return this.a(d0, d1, d2, d3, (Predicate<Entity>) predicate);
    }

    @Nullable
    public EntityHuman a(double d0, double d1, double d2, double d3, Predicate<Entity> predicate) {
        double d4 = -1.0;
        EntityHuman entityhuman = null;
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman2 = this.players.get(i);
            if (entityhuman2 != null) {
                if (!entityhuman2.dead) {
                    if (predicate.apply(entityhuman2)) {
                        double d5 = entityhuman2.d(d0, d1, d2);
                        if ((d3 < 0.0 || d5 < d3 * d3) && (d4 == -1.0 || d5 < d4)) {
                            d4 = d5;
                            entityhuman = entityhuman2;
                        }
                    }
                }
            }
        }
        return entityhuman;
    }

    public boolean isPlayerNearby(double d0, double d1, double d2, double d3) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman = this.players.get(i);
            if (IEntitySelector.e.apply(entityhuman) && entityhuman.affectsSpawning) {
                double d4 = entityhuman.d(d0, d1, d2);
                if (d3 < 0.0 || d4 < d3 * d3) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    public EntityHuman a(Entity entity, double d0, double d1) {
        return this.a(entity.locX, entity.locY, entity.locZ, d0, d1, null, null);
    }

    @Nullable
    public EntityHuman a(BlockPosition blockposition, double d0, double d1) {
        return this.a(blockposition.getX() + 0.5f, blockposition.getY() + 0.5f, blockposition.getZ() + 0.5f, d0, d1,
                null, null);
    }

    @Nullable
    public EntityHuman a(double d0, double d1, double d2, double d3, double d4,
                         @Nullable Function<EntityHuman, Double> function, @Nullable Predicate<EntityHuman> predicate) {
        double d5 = -1.0;
        EntityHuman entityhuman = null;
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman2 = this.players.get(i);
            if (!entityhuman2.abilities.isInvulnerable && entityhuman2.isAlive() && !entityhuman2.isSpectator()
                    && (predicate == null || predicate.apply(entityhuman2))) {
                double d6 = entityhuman2.d(d0, entityhuman2.locY, d2);
                double d7 = d3;
                if (entityhuman2.isSneaking()) {
                    d7 = d3 * 0.800000011920929;
                }
                if (entityhuman2.isInvisible()) {
                    float f = entityhuman2.cW();
                    if (f < 0.1f) {
                        f = 0.1f;
                    }
                    d7 *= 0.7f * f;
                }
                if (function != null) {
                    d7 *= (double) MoreObjects.firstNonNull(function.apply(entityhuman2), (Object) 1.0);
                }
                if ((d4 < 0.0 || Math.abs(entityhuman2.locY - d1) < d4 * d4) && (d3 < 0.0 || d6 < d7 * d7)
                        && (d5 == -1.0 || d6 < d5)) {
                    d5 = d6;
                    entityhuman = entityhuman2;
                }
            }
        }
        return entityhuman;
    }

    @Nullable
    public EntityHuman a(String s) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman = this.players.get(i);
            if (s.equals(entityhuman.getName())) {
                return entityhuman;
            }
        }
        return null;
    }

    @Nullable
    public EntityHuman b(UUID uuid) {
        for (int i = 0; i < this.players.size(); ++i) {
            EntityHuman entityhuman = this.players.get(i);
            if (uuid.equals(entityhuman.getUniqueID())) {
                return entityhuman;
            }
        }
        return null;
    }

    public void checkSession() throws ExceptionWorldConflict {
        this.dataManager.checkSession();
    }

    public long getSeed() {
        return this.worldData.getSeed();
    }

    public long getTime() {
        return this.worldData.getTime();
    }

    public long getDayTime() {
        return this.worldData.getDayTime();
    }

    public void setDayTime(long i) {
        this.worldData.setDayTime(i);
    }

    public BlockPosition getSpawn() {
        BlockPosition blockposition = new BlockPosition(this.worldData.b(), this.worldData.c(), this.worldData.d());
        if (!this.getWorldBorder().a(blockposition)) {
            blockposition = this.getHighestBlockYAt(
                    new BlockPosition(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ()));
        }
        return blockposition;
    }

    public void A(BlockPosition blockposition) {
        this.worldData.setSpawn(blockposition);
    }

    public boolean a(EntityHuman entityhuman, BlockPosition blockposition) {
        return true;
    }

    public void broadcastEntityEffect(Entity entity, byte b0) {
    }

    public IChunkProvider getChunkProvider() {
        return this.chunkProvider;
    }

    public void playBlockAction(BlockPosition blockposition, Block block, int i, int j) {
        this.getType(blockposition).a(this, blockposition, i, j);
    }

    public IDataManager getDataManager() {
        return this.dataManager;
    }

    public WorldData getWorldData() {
        return this.worldData;
    }

    public GameRules getGameRules() {
        return this.worldData.w();
    }

    public void everyoneSleeping() {
    }

    public void checkSleepStatus() {
        if (!this.isClientSide) {
            this.everyoneSleeping();
        }
    }

    public float h(float f) {
        return (this.p + (this.q - this.p) * f) * this.j(f);
    }

    public float j(float f) {
        return this.n + (this.o - this.n) * f;
    }

    public boolean X() {
        return this.h(1.0f) > 0.9;
    }

    public boolean isRaining() {
        return this.j(1.0f) > 0.2;
    }

    public boolean isRainingAt(BlockPosition blockposition) {
        if (!this.isRaining()) {
            return false;
        }
        if (!this.h(blockposition)) {
            return false;
        }
        if (this.p(blockposition).getY() > blockposition.getY()) {
            return false;
        }
        BiomeBase biomebase = this.getBiome(blockposition);
        return !biomebase.c() && !this.f(blockposition, false) && biomebase.d();
    }

    public boolean C(BlockPosition blockposition) {
        BiomeBase biomebase = this.getBiome(blockposition);
        return biomebase.e();
    }

    @Nullable
    public PersistentCollection Z() {
        return this.worldMaps;
    }

    public void a(String s, PersistentBase persistentbase) {
        this.worldMaps.a(s, persistentbase);
    }

    @Nullable
    public PersistentBase a(Class<? extends PersistentBase> oclass, String s) {
        return this.worldMaps.get((Class<? extends PersistentBase>) oclass, s);
    }

    public int b(String s) {
        return this.worldMaps.a(s);
    }

    public void a(int i, BlockPosition blockposition, int j) {
        for (int k = 0; k < this.u.size(); ++k) {
            this.u.get(k).a(i, blockposition, j);
        }
    }

    public void triggerEffect(int i, BlockPosition blockposition, int j) {
        this.a(null, i, blockposition, j);
    }

    public void a(@Nullable EntityHuman entityhuman, int i, BlockPosition blockposition, int j) {
        try {
            for (int k = 0; k < this.u.size(); ++k) {
                this.u.get(k).a(entityhuman, i, blockposition, j);
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Playing level event");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Level event being played");
            crashreportsystemdetails.a("Block coordinates", (Object) CrashReportSystemDetails.a(blockposition));
            crashreportsystemdetails.a("Event source", (Object) entityhuman);
            crashreportsystemdetails.a("Event type", (Object) i);
            crashreportsystemdetails.a("Event data", (Object) j);
            throw new ReportedException(crashreport);
        }
    }

    public int getHeight() {
        return 256;
    }

    public int ab() {
        return this.worldProvider.n() ? 128 : 256;
    }

    public Random a(int i, int j, int k) {
        long l = i * 341873128712L + j * 132897987541L + this.getWorldData().getSeed() + k;
        this.random.setSeed(l);
        return this.random;
    }

    public CrashReportSystemDetails a(CrashReport crashreport) {
        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Affected level", 1);
        crashreportsystemdetails.a("Level name",
                (Object) ((this.worldData == null) ? "????" : this.worldData.getName()));
        crashreportsystemdetails.a("All players", new CrashReportCallable<Object>() {
            public String a() {
                return World.this.players.size() + " total; " + World.this.players;
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Chunk stats", new CrashReportCallable<Object>() {
            public String a() {
                return World.this.chunkProvider.getName();
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        try {
            this.worldData.a(crashreportsystemdetails);
        } catch (Throwable throwable) {
            crashreportsystemdetails.a("Level Data Unobtainable", throwable);
        }
        return crashreportsystemdetails;
    }

    public void c(int i, BlockPosition blockposition, int j) {
        for (int k = 0; k < this.u.size(); ++k) {
            IWorldAccess iworldaccess = this.u.get(k);
            iworldaccess.b(i, blockposition, j);
        }
    }

    public Calendar ae() {
        if (this.getTime() % 600L == 0L) {
            this.N.setTimeInMillis(MinecraftServer.aw());
        }
        return this.N;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void updateAdjacentComparators(BlockPosition blockposition, Block block) {
        for (EnumDirection enumdirection : EnumDirection.EnumDirectionLimit.HORIZONTAL) {
            BlockPosition blockposition2 = blockposition.shift(enumdirection);
            if (this.isLoaded(blockposition2)) {
                IBlockData iblockdata = this.getType(blockposition2);
                if (Blocks.UNPOWERED_COMPARATOR.D(iblockdata)) {
                    iblockdata.doPhysics(this, blockposition2, block, blockposition);
                } else {
                    if (!iblockdata.l()) {
                        continue;
                    }
                    blockposition2 = blockposition2.shift(enumdirection);
                    iblockdata = this.getType(blockposition2);
                    if (!Blocks.UNPOWERED_COMPARATOR.D(iblockdata)) {
                        continue;
                    }
                    iblockdata.doPhysics(this, blockposition2, block, blockposition);
                }
            }
        }
    }

    public DifficultyDamageScaler D(BlockPosition blockposition) {
        long i = 0L;
        float f = 0.0f;
        if (this.isLoaded(blockposition)) {
            f = this.G();
            i = this.getChunkAtWorldCoords(blockposition).x();
        }
        return new DifficultyDamageScaler(this.getDifficulty(), this.getDayTime(), i, f);
    }

    public EnumDifficulty getDifficulty() {
        return this.getWorldData().getDifficulty();
    }

    public int ah() {
        return this.L;
    }

    public void c(int i) {
        this.L = i;
    }

    public void d(int i) {
        this.M = i;
    }

    public PersistentVillage ak() {
        return this.villages;
    }

    public WorldBorder getWorldBorder() {
        return this.P;
    }

    public boolean shouldStayLoaded(int i, int j) {
        return this.e(i, j);
    }

    public boolean e(int i, int j) {
        BlockPosition blockposition = this.getSpawn();
        int k = i * 16 + 8 - blockposition.getX();
        int l = j * 16 + 8 - blockposition.getZ();
        boolean flag = true;
        short keepLoadedRange = this.paperConfig.keepLoadedRange;
        return k >= -keepLoadedRange && k <= keepLoadedRange && l >= -keepLoadedRange && l <= keepLoadedRange
                && this.keepSpawnInMemory;
    }

    public void a(Packet<?> packet) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    public LootTableRegistry getLootTableRegistry() {
        return this.B;
    }

    @Nullable
    public BlockPosition a(String s, BlockPosition blockposition, boolean flag) {
        return null;
    }
}

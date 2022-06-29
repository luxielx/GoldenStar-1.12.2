// 
// Decompiled by Procyon v0.5.36
// 

package net.minecraft.server.v1_12_R1;

import co.aikar.timings.Timing;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.spigotmc.AsyncCatcher;
import org.spigotmc.SlackActivityAccountant;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class PlayerChunkMap {
    private static final Predicate<EntityPlayer> a;
    private static final Predicate<EntityPlayer> b;
    private final WorldServer world;
    private final List<EntityPlayer> managedPlayers;
    private final Long2ObjectMap<PlayerChunk> e;
    private final Set<PlayerChunk> f;
    private final List<PlayerChunk> g;
    private final List<PlayerChunk> h;
    private final List<PlayerChunk> i;
    private int j;
    private long k;
    private boolean l;
    private boolean m;
    private boolean wasNotEmpty;

    public int getViewDistance() {
        return this.j;
    }

    public PlayerChunkMap(final WorldServer worldserver) {
        this.managedPlayers = Lists.newArrayList();
        this.e = new Long2ObjectOpenHashMap(4096);
        this.f = Collections.synchronizedSet(Sets.newHashSet());
        this.g = Lists.newLinkedList();
        this.h = Lists.newLinkedList();
        this.i = Collections.synchronizedList(new ArrayList<>()); // Lists.newArrayList();
        this.l = true;
        this.m = true;
        this.world = worldserver;
        this.a(worldserver.spigotConfig.viewDistance);
    }

    public WorldServer getWorld() {
        return this.world;
    }

//	public List<PlayerChunk> làm_sao_để_em_hiểu_lòng_anh() {
//		return i;
//	}

    public Iterator<Chunk> b() {
//		synchronized (i) {
        final Iterator<PlayerChunk> iterator = ImmutableList.copyOf(i).iterator();
        return new AbstractIterator<Chunk>() {
            protected Chunk a() {
                while (iterator.hasNext()) {
                    final PlayerChunk playerchunk = iterator.next();
                    final Chunk chunk = playerchunk.f();
                    if (chunk == null) {
                        continue;
                    }
                    if (!chunk.v() && chunk.isDone()) {
                        return chunk;
                    }
                    if (!chunk.j()) {
                        return chunk;
                    }
                    if (!playerchunk.a(128.0, PlayerChunkMap.a)) {
                        continue;
                    }
                    return chunk;
                }
                return (Chunk) this.endOfData();
            }

            protected Chunk computeNext() {
                return this.a();
            }
        };
//		}
    }

    public void flush() {
        final long i = this.world.getTime();
        if (i - this.k > 8000L) {
            try (final Timing ignored = this.world.timings.doChunkMapUpdate.startTiming()) {
                this.k = i;
                for (int j = 0; j < this.i.size(); ++j) {
                    final PlayerChunk playerchunk = this.i.get(j);
                    playerchunk.d();
                    playerchunk.c();
                }
            }
        }
        if (!this.f.isEmpty()) {
            try (final Timing ignored = this.world.timings.doChunkMapToUpdate.startTiming()) {
                synchronized (f) {
                    for (final PlayerChunk playerchunk : this.f) {
                        playerchunk.d();
                    }
                }
                this.f.clear();
            }
        }
        if (this.l && i % 4L == 0L) {
            this.l = false;
            try (final Timing ignored = this.world.timings.doChunkMapSortMissing.startTiming()) {
                Collections.sort(this.h, new Comparator() {
                    public int a(final PlayerChunk playerchunk, final PlayerChunk playerchunk1) {
                        return ComparisonChain.start().compare(playerchunk.g(), playerchunk1.g()).result();
                    }

                    @Override
                    public int compare(final Object object, final Object object1) {
                        return this.a((PlayerChunk) object, (PlayerChunk) object1);
                    }
                });
            }
        }
        if (this.m && i % 4L == 2L) {
            this.m = false;
            try (final Timing ignored = this.world.timings.doChunkMapSortSendToPlayers.startTiming()) {
                Collections.sort(this.g, new Comparator() {
                    public int a(final PlayerChunk playerchunk, final PlayerChunk playerchunk1) {
                        return ComparisonChain.start().compare(playerchunk.g(), playerchunk1.g()).result();
                    }

                    @Override
                    public int compare(final Object object, final Object object1) {
                        return this.a((PlayerChunk) object, (PlayerChunk) object1);
                    }
                });
            }
        }
        if (!this.h.isEmpty()) {
            try (final Timing ignored = this.world.timings.doChunkMapPlayersNeedingChunks.startTiming()) {
                final SlackActivityAccountant activityAccountant = this.world
                        .getMinecraftServer().slackActivityAccountant;
                activityAccountant.startActivity(0.5);
                int chunkGensAllowed = this.world.paperConfig.maxChunkGensPerTick;
                final Iterator<PlayerChunk> iterator2 = this.h.iterator();
                while (iterator2.hasNext()) {
                    final PlayerChunk playerchunk2 = iterator2.next();
                    if (playerchunk2.f() == null) {
                        final boolean flag = playerchunk2.a((Predicate) PlayerChunkMap.b);
                        if (flag && !playerchunk2.chunkExists && chunkGensAllowed-- <= 0) {
                            continue;
                        }
                        if (!playerchunk2.a(flag)) {
                            continue;
                        }
                        iterator2.remove();
                        if (playerchunk2.b()) {
                            this.g.remove(playerchunk2);
                        }
                        if (activityAccountant.activityTimeIsExhausted()) {
                            break;
                        }
                        continue;
                    } else {
                        iterator2.remove();
                    }
                }
                activityAccountant.endActivity();
            }
        }
        if (!this.g.isEmpty()) {
            int j = this.world.paperConfig.maxChunkSendsPerTick;
            try (final Timing ignored = this.world.timings.doChunkMapPendingSendToPlayers.startTiming()) {
                final Iterator<PlayerChunk> iterator3 = this.g.iterator();
                while (iterator3.hasNext()) {
                    final PlayerChunk playerchunk3 = iterator3.next();
                    if (playerchunk3.b()) {
                        iterator3.remove();
                        if (--j < 0) {
                            break;
                        }
                        continue;
                    }
                }
            }
        }
        if (this.managedPlayers.isEmpty()) {
            try (final Timing ignored = this.world.timings.doChunkMapUnloadChunks.startTiming()) {
                final WorldProvider worldprovider = this.world.worldProvider;
                if (!worldprovider.e() && !this.world.savingDisabled) {
                    this.world.getChunkProviderServer().b();
                }
            }
        }
    }

    public boolean a(final int i, final int j) {
        final long k = d(i, j);
        return this.e.get(k) != null;
    }

    @Nullable
    public PlayerChunk getChunk(final int i, final int j) {
        return (PlayerChunk) this.e.get(d(i, j));
    }

    private PlayerChunk c(final int i, final int j) {
        final long k = d(i, j);
        PlayerChunk playerchunk = (PlayerChunk) this.e.get(k);
        if (playerchunk == null) {
            playerchunk = new PlayerChunk(this, i, j);
            this.e.put(k, playerchunk);
            this.i.add(playerchunk);
            if (playerchunk.f() == null) {
                this.h.add(playerchunk);
            }
            if (!playerchunk.b()) {
                this.g.add(playerchunk);
            }
        }
        return playerchunk;
    }

    public final boolean isChunkInUse(final int x, final int z) {
        final PlayerChunk pi = this.getChunk(x, z);
        return pi != null && pi.c.size() > 0;
    }

    public void flagDirty(final BlockPosition blockposition) {
        final int i = blockposition.getX() >> 4;
        final int j = blockposition.getZ() >> 4;
        final PlayerChunk playerchunk = this.getChunk(i, j);
        if (playerchunk != null) {
            playerchunk.a(blockposition.getX() & 0xF, blockposition.getY(), blockposition.getZ() & 0xF);
        }
    }

    public void addPlayer(final EntityPlayer entityplayer) {
        final int i = (int) entityplayer.locX >> 4;
        final int j = (int) entityplayer.locZ >> 4;
        entityplayer.d = entityplayer.locX;
        entityplayer.e = entityplayer.locZ;
        final List<ChunkCoordIntPair> chunkList = new LinkedList<ChunkCoordIntPair>();
        for (int viewDistance = entityplayer.getViewDistance(), k = i - viewDistance; k <= i + viewDistance; ++k) {
            for (int l = j - viewDistance; l <= j + viewDistance; ++l) {
                chunkList.add(new ChunkCoordIntPair(k, l));
            }
        }
        Collections.sort(chunkList, new ChunkCoordComparator(entityplayer));
        for (final ChunkCoordIntPair pair : chunkList) {
            this.c(pair.x, pair.z).a(entityplayer);
        }
        this.managedPlayers.add(entityplayer);
        this.e();
    }

    public void removePlayer(final EntityPlayer entityplayer) {
        final int i = (int) entityplayer.d >> 4;
        final int j = (int) entityplayer.e >> 4;
        for (int viewDistance = entityplayer.getViewDistance(), k = i - viewDistance; k <= i + viewDistance; ++k) {
            for (int l = j - viewDistance; l <= j + viewDistance; ++l) {
                final PlayerChunk playerchunk = this.getChunk(k, l);
                if (playerchunk != null) {
                    playerchunk.b(entityplayer);
                }
            }
        }
        this.managedPlayers.remove(entityplayer);
        this.e();
    }

    private boolean a(final int i, final int j, final int k, final int l, final int i1) {
        final int j2 = i - k;
        final int k2 = j - l;
        return j2 >= -i1 && j2 <= i1 && (k2 >= -i1 && k2 <= i1);
    }

    public void movePlayerzz(EntityPlayer entityplayer) {
        new Thread(() -> {
//			movePlayerMT(entityplayer);
        }, "player movement");
    }

    public void movePlayer(EntityPlayer entityplayer) {
        final int i = (int) entityplayer.locX >> 4;
        final int j = (int) entityplayer.locZ >> 4;
        final double d0 = entityplayer.d - entityplayer.locX;
        final double d2 = entityplayer.e - entityplayer.locZ;
        final double d3 = d0 * d0 + d2 * d2;
        if (d3 >= 64.0) {
            final int k = (int) entityplayer.d >> 4;
            final int l = (int) entityplayer.e >> 4;
            final int viewDistance = entityplayer.getViewDistance();
            final int i2 = Math.max(this.getViewDistance(), viewDistance);
            final int j2 = i - k;
            final int k2 = j - l;
            final List<ChunkCoordIntPair> chunksToLoad = new LinkedList<ChunkCoordIntPair>();
            if (j2 != 0 || k2 != 0) {
                for (int l2 = i - i2; l2 <= i + i2; ++l2) {
                    for (int i3 = j - i2; i3 <= j + i2; ++i3) {
                        if (!this.a(l2, i3, k, l, viewDistance)) {
                            chunksToLoad.add(new ChunkCoordIntPair(l2, i3));
                        }
                        if (!this.a(l2 - j2, i3 - k2, i, j, i2)) {
                            final PlayerChunk playerchunk = this.getChunk(l2 - j2, i3 - k2);
                            if (playerchunk != null) {
                                playerchunk.b(entityplayer);
                            }
                        }
                    }
                }
                entityplayer.d = entityplayer.locX;
                entityplayer.e = entityplayer.locZ;
                this.e();
                Collections.sort(chunksToLoad, new ChunkCoordComparator(entityplayer));
                for (final ChunkCoordIntPair pair : chunksToLoad) {
                    this.c(pair.x, pair.z).a(entityplayer);
                }
            }
        }
    }

    public boolean a(final EntityPlayer entityplayer, final int i, final int j) {
        final PlayerChunk playerchunk = this.getChunk(i, j);
        return playerchunk != null && playerchunk.d(entityplayer) && playerchunk.e();
    }

    public final void setViewDistanceForAll(final int viewDistance) {
        this.a(viewDistance);
    }

    public void a(int i) {
        i = MathHelper.clamp(i, 3, 32);
        if (i != this.j) {
            final int j = i - this.j;
            final ArrayList<EntityPlayer> arraylist = Lists.newArrayList(this.managedPlayers);
            for (final EntityPlayer entityplayer : arraylist) {
                this.setViewDistance(entityplayer, i, false);
            }
            this.j = i;
            this.e();
        }
    }

    public void setViewDistance(final EntityPlayer entityplayer, final int i) {
        this.setViewDistance(entityplayer, i, true);
    }

    public void setViewDistance(final EntityPlayer entityplayer, int i, final boolean markSort) {
        i = MathHelper.clamp(i, 3, 32);
        final int oldViewDistance = entityplayer.getViewDistance();
        if (i != oldViewDistance) {
            final int j = i - oldViewDistance;
            final int k = (int) entityplayer.locX >> 4;
            final int l = (int) entityplayer.locZ >> 4;
            if (j > 0) {
                for (int i2 = k - i; i2 <= k + i; ++i2) {
                    for (int j2 = l - i; j2 <= l + i; ++j2) {
                        final PlayerChunk playerchunk = this.c(i2, j2);
                        if (!playerchunk.d(entityplayer)) {
                            playerchunk.a(entityplayer);
                        }
                    }
                }
            } else {
                for (int i2 = k - oldViewDistance; i2 <= k + oldViewDistance; ++i2) {
                    for (int j2 = l - oldViewDistance; j2 <= l + oldViewDistance; ++j2) {
                        if (!this.a(i2, j2, k, l, i)) {
                            this.c(i2, j2).b(entityplayer);
                        }
                    }
                }
                if (markSort) {
                    this.e();
                }
            }
        }
    }

    private void e() {
        this.l = true;
        this.m = true;
    }

    public static int getFurthestViewableBlock(final int i) {
        return i * 16 - 16;
    }

    private static long d(final int i, final int j) {
        return i + 2147483647L | j + 2147483647L << 32;
    }

    public void a(final PlayerChunk playerchunk) {
//		AsyncCatcher.catchOp("Async Player Chunk Add");
        this.f.add(playerchunk);
    }

    public void b(final PlayerChunk playerchunk) {
        AsyncCatcher.catchOp("Async Player Chunk Remove");
        final ChunkCoordIntPair chunkcoordintpair = playerchunk.a();
        final long i = d(chunkcoordintpair.x, chunkcoordintpair.z);
        playerchunk.c();
        this.e.remove(i);
        this.i.remove(playerchunk);
        this.f.remove(playerchunk);
        this.g.remove(playerchunk);
        this.h.remove(playerchunk);
        final Chunk chunk = playerchunk.f();
        if (chunk != null) {
            if (this.world.paperConfig.delayChunkUnloadsBy <= 0L) {
                this.getWorld().getChunkProviderServer().unload(chunk);
            } else {
                chunk.scheduledForUnload = System.currentTimeMillis();
            }
        }
    }

    public void updateViewDistance(final EntityPlayer player, final int distanceIn) {
        final int oldViewDistance = player.getViewDistance();
        int toSet;
        int playerViewDistance = toSet = MathHelper.clamp(distanceIn, 3, 32);
        if (distanceIn < 0) {
            playerViewDistance = -1;
            toSet = this.world.getPlayerChunkMap().getViewDistance();
        }
        if (toSet != oldViewDistance) {
            this.setViewDistance(player, toSet);
            player.setViewDistance(playerViewDistance);
        }
    }

    static {
        a = (Predicate) new Predicate() {
            public boolean a(@Nullable final EntityPlayer entityplayer) {
                return entityplayer != null && !entityplayer.isSpectator();
            }

            public boolean apply(@Nullable final Object object) {
                return this.a((EntityPlayer) object);
            }
        };
        b = (Predicate) new Predicate() {
            public boolean a(@Nullable final EntityPlayer entityplayer) {
                return entityplayer != null && (!entityplayer.isSpectator()
                        || entityplayer.x().getGameRules().getBoolean("spectatorsGenerateChunks"));
            }

            public boolean apply(@Nullable final Object object) {
                return this.a((EntityPlayer) object);
            }
        };
    }

    private static class ChunkCoordComparator implements Comparator<ChunkCoordIntPair> {
        private int x;
        private int z;

        public ChunkCoordComparator(final EntityPlayer entityplayer) {
            this.x = (int) entityplayer.locX >> 4;
            this.z = (int) entityplayer.locZ >> 4;
        }

        @Override
        public int compare(final ChunkCoordIntPair a, final ChunkCoordIntPair b) {
            if (a.equals((Object) b)) {
                return 0;
            }
            final int ax = a.x - this.x;
            final int az = a.z - this.z;
            final int bx = b.x - this.x;
            final int bz = b.z - this.z;
            final int result = (ax - bx) * (ax + bx) + (az - bz) * (az + bz);
            if (result != 0) {
                return result;
            }
            if (ax < 0) {
                if (bx < 0) {
                    return bz - az;
                }
                return -1;
            } else {
                if (bx < 0) {
                    return 1;
                }
                return az - bz;
            }
        }
    }
}

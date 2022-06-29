// 
// Decompiled by Procyon v0.5.30
// 

package net.minecraft.server.v1_12_R1;

import com.google.common.collect.Lists;
import io.netty.util.internal.ConcurrentSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spigotmc.TrackingRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntityTracker {

    // ~
    // c concurrent set
    // multi threading updateplayers

    private static Logger a;
    private WorldServer world;
    private Set<EntityTrackerEntry> c;
    public IntHashMap<EntityTrackerEntry> trackedEntities;
    // ~
    private volatile int e;
    // private Object lock = new Object();

    public EntityTracker(WorldServer worldserver) {
        // ~
        this.c = new ConcurrentSet<EntityTrackerEntry>(); // Collections.synchronizedSet(Sets.newHashSet());
        this.trackedEntities = new IntHashMap<EntityTrackerEntry>();
        this.world = worldserver;
        this.e = PlayerChunkMap.getFurthestViewableBlock(worldserver.spigotConfig.viewDistance);
    }

    public static long a(double d0) {
        return MathHelper.d(d0 * 4096.0);
    }

    public synchronized void track(Entity entity) {
        if (entity instanceof EntityPlayer) {
            this.addEntity(entity, 512, 2);
            EntityPlayer entityplayer = (EntityPlayer) entity;
            // synchronized (c) {
            for (EntityTrackerEntry entitytrackerentry : this.c) {
                if (entitytrackerentry.b() != entityplayer) {
                    entitytrackerentry.updatePlayer(entityplayer);
                }
            }
            // }
        } else if (entity instanceof EntityFishingHook) {
            this.addEntity(entity, 64, 5, true);
        } else if (entity instanceof EntityArrow) {
            this.addEntity(entity, 64, 20, false);
        } else if (entity instanceof EntitySmallFireball) {
            this.addEntity(entity, 64, 10, false);
        } else if (entity instanceof EntityFireball) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntitySnowball) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityLlamaSpit) {
            this.addEntity(entity, 64, 10, false);
        } else if (entity instanceof EntityEnderPearl) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityEnderSignal) {
            this.addEntity(entity, 64, 4, true);
        } else if (entity instanceof EntityEgg) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityPotion) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityThrownExpBottle) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityFireworks) {
            this.addEntity(entity, 64, 10, true);
        } else if (entity instanceof EntityItem) {
            this.addEntity(entity, 64, 20, true);
        } else if (entity instanceof EntityMinecartAbstract) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityBoat) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntitySquid) {
            this.addEntity(entity, 64, 3, true);
        } else if (entity instanceof EntityWither) {
            this.addEntity(entity, 80, 3, false);
        } else if (entity instanceof EntityShulkerBullet) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityBat) {
            this.addEntity(entity, 80, 3, false);
        } else if (entity instanceof EntityEnderDragon) {
            this.addEntity(entity, 160, 3, true);
        } else if (entity instanceof IAnimal) {
            this.addEntity(entity, 80, 3, true);
        } else if (entity instanceof EntityTNTPrimed) {
            this.addEntity(entity, 160, 10, true);
        } else if (entity instanceof EntityFallingBlock) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityHanging) {
            this.addEntity(entity, 160, Integer.MAX_VALUE, false);
        } else if (entity instanceof EntityArmorStand) {
            this.addEntity(entity, 160, 3, true);
        } else if (entity instanceof EntityExperienceOrb) {
            this.addEntity(entity, 160, 20, true);
        } else if (entity instanceof EntityAreaEffectCloud) {
            this.addEntity(entity, 160, 10, true);
        } else if (entity instanceof EntityEnderCrystal) {
            this.addEntity(entity, 256, Integer.MAX_VALUE, false);
        } else if (entity instanceof EntityEvokerFangs) {
            this.addEntity(entity, 160, 2, false);
        }
    }

    public void addEntity(Entity entity, int i, int j) {
        this.addEntity(entity, i, j, false);
    }

    public synchronized void addEntity(Entity entity, int i, int j, boolean flag) {
        // ~
        // AsyncCatcher.catchOp("entity track");
        i = TrackingRange.getEntityTrackingRange(entity, i);
        try {
            if (this.trackedEntities.b(entity.getId())) {
                throw new IllegalStateException("Entity is already tracked!");
            }
            EntityTrackerEntry entitytrackerentry = new EntityTrackerEntry(entity, i, this.e, j, flag);
            this.c.add(entitytrackerentry);
            this.trackedEntities.a(entity.getId(), entitytrackerentry);
            entitytrackerentry.scanPlayers(this.world.players);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Adding entity to track");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity To Track");
            crashreportsystemdetails.a("Tracking range", (Object) (i + " blocks"));
            int finalI = i;
            crashreportsystemdetails.a("Update interval", new CrashReportCallable<String>() {
                public String a() throws Exception {
                    String s = "Once per " + finalI + " ticks";
                    if (finalI == Integer.MAX_VALUE) {
                        s = "Maximum (" + s + ")";
                    }
                    return s;
                }

                public String call() throws Exception {
                    return this.a();
                }
            });
            entity.appendEntityCrashDetails(crashreportsystemdetails);
            ((EntityTrackerEntry) this.trackedEntities.get(entity.getId())).b()
                    .appendEntityCrashDetails(crashreport.a("Entity That Is Already Tracked"));
            try {
                throw new ReportedException(crashreport);
            } catch (ReportedException reportedexception) {
                EntityTracker.a.error("\"Silently\" catching entity tracking error.", (Throwable) reportedexception);
            }
        }
    }

    public synchronized void untrackEntity(Entity entity) {
//		AsyncCatcher.catchOp("entity untrack");
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer) entity;
            // synchronized (c) {
            for (EntityTrackerEntry entitytrackerentry : this.c) {
                entitytrackerentry.a(entityplayer);
            }
            // }
        }
        EntityTrackerEntry entitytrackerentry2 = (EntityTrackerEntry) this.trackedEntities.d(entity.getId());
        if (entitytrackerentry2 != null) {
            this.c.remove(entitytrackerentry2);
            entitytrackerentry2.a();
        }
    }

    // ~
    public void updatePlayers() {
        world.timings.tracker1.startTiming();
        synchronized (world.entityList) {
            world.entityList.forEach(e -> {
                EntityTrackerEntry ete = e.getTracker();
                if (ete != null && ete.b() != null)
                    ete.updateVelocity();
            });
        }
        new Thread(() -> lonmemay()).start();
        world.timings.tracker1.stopTiming();
    }

    public /* synchronized */ void lonmemay() {
        List<EntityPlayer> cac = new ArrayList<EntityPlayer>();
        c.forEach(entitytrackerentry -> {
            entitytrackerentry.track(world.players);
            if (entitytrackerentry.b) {
                Entity entity = entitytrackerentry.b();
                if (entity instanceof EntityPlayer)
                    cac.add((EntityPlayer) entity);
            }
        });
        cac.forEach(mojangngu -> c.forEach(nhuconcho -> nhuconcho.updatePlayer(mojangngu)));
    }

    public /* synchronized */ void a(EntityPlayer entityplayer) {
        // synchronized (c) {
        for (EntityTrackerEntry entitytrackerentry : c) {
            if (entitytrackerentry.b() == entityplayer) {
                entitytrackerentry.scanPlayers(this.world.players);
            } else {
                entitytrackerentry.updatePlayer(entityplayer);
            }
        }
        // }
    }

    public void a(Entity entity, Packet<?> packet) {
        EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) this.trackedEntities.get(entity.getId());
        if (entitytrackerentry != null) {
            entitytrackerentry.broadcast(packet);
        }
    }

    public void sendPacketToEntity(Entity entity, Packet<?> packet) {
        EntityTrackerEntry entitytrackerentry = (EntityTrackerEntry) this.trackedEntities.get(entity.getId());
        if (entitytrackerentry != null) {
            entitytrackerentry.broadcastIncludingSelf(packet);
        }
    }

    public synchronized void untrackPlayer(EntityPlayer entityplayer) {
        // synchronized (c) {
        for (EntityTrackerEntry entitytrackerentry : this.c) {
            entitytrackerentry.clear(entityplayer);
        }
        // }
    }

    public /* synchronized */ void a(EntityPlayer entityplayer, Chunk chunk) {
        ArrayList<Entity> arraylist = Lists.newArrayList();
        ArrayList<Entity> arraylist2 = Lists.newArrayList();
        // synchronized (c) {
        for (EntityTrackerEntry entitytrackerentry : c) {
            Entity entity = entitytrackerentry.b();
            if (entity != entityplayer && entity.ab == chunk.locX && entity.ad == chunk.locZ) {
                entitytrackerentry.updatePlayer(entityplayer);
                if (entity instanceof EntityInsentient && ((EntityInsentient) entity).getLeashHolder() != null) {
                    arraylist.add(entity);
                }
                if (entity.bF().isEmpty()) {
                    continue;
                }
                arraylist2.add(entity);
            }
        }
        // }
        if (!arraylist.isEmpty()) {
            for (Entity entity2 : arraylist) {
                entityplayer.playerConnection.sendPacket((Packet<?>) new PacketPlayOutAttachEntity(entity2,
                        ((EntityInsentient) entity2).getLeashHolder()));
            }
        }
        if (!arraylist2.isEmpty()) {
            for (Entity entity2 : arraylist2) {
                entityplayer.playerConnection.sendPacket((Packet<?>) new PacketPlayOutMount(entity2));
            }
        }
    }

    public synchronized void a(int i) {
        this.e = (i - 1) * 16;
        // synchronized (c) {
        for (EntityTrackerEntry entitytrackerentry : this.c) {
            entitytrackerentry.a(this.e);
        }
        // }
    }

    static {
        a = LogManager.getLogger();
    }
}

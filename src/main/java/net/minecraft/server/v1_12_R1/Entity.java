// 
// Decompiled by Procyon v0.5.30
// 

package net.minecraft.server.v1_12_R1;

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.Timing;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.TravelAgent;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftTravelAgent;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.projectiles.ProjectileSource;
import org.spigotmc.ActivationRange;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public abstract class Entity implements ICommandListener, KeyedObject {
    private static final int CURRENT_LEVEL = 2;
    public static Random SHARED_RANDOM;
    List<Entity> entitySlice;
    protected CraftEntity bukkitEntity;
    EntityTrackerEntry tracker;
    Throwable addedToWorldStack;
    private static final Logger a;
    private static final List<ItemStack> b;
    private static final AxisAlignedBB c;
    private static double f;
    private static int entityCount;
    private int id;
    public boolean i;
    public final List<Entity> passengers;
    protected int j;
    private Entity au;
    public boolean attachedToPlayer;
    public World world;
    public double lastX;
    public double lastY;
    public double lastZ;
    public double locX;
    public double locY;
    public double locZ;
    public double motX;
    public double motY;
    public double motZ;
    public float yaw;
    public float pitch;
    public float lastYaw;
    public float lastPitch;
    private AxisAlignedBB boundingBox;
    public boolean onGround;
    public boolean positionChanged;
    public boolean B;
    public boolean C;
    public boolean velocityChanged;
    protected boolean E;
    private boolean aw;
    public boolean dead;
    public boolean shouldBeRemoved;
    public float width;
    public float length;
    public float I;
    public float J;
    public float K;
    public float fallDistance;
    private int ax;
    private float ay;
    public double M;
    public double N;
    public double O;
    public float P;
    public boolean noclip;
    public float R;
    protected Random random;
    public int ticksLived;
    public int fireTicks;
    public boolean inWater;
    public int noDamageTicks;
    protected boolean justCreated;
    protected boolean fireProof;
    protected DataWatcher datawatcher;
    protected static final DataWatcherObject<Byte> Z;
    private static final DataWatcherObject<Integer> aA;
    private static final DataWatcherObject<String> aB;
    private static final DataWatcherObject<Boolean> aC;
    private static final DataWatcherObject<Boolean> aD;
    private static final DataWatcherObject<Boolean> aE;
    public boolean aa;
    public int ab;
    public int ac;
    public int ad;
    public boolean ah;
    public boolean impulse;
    public int portalCooldown;
    protected boolean ak;
    protected int al;
    public int dimension;
    protected BlockPosition an;
    protected Vec3D ao;
    protected EnumDirection ap;
    private boolean invulnerable;
    protected UUID uniqueID;
    protected String ar;
    private final CommandObjectiveExecutor aG;
    public boolean glowing;
    private final Set<String> aH;
    private boolean aI;
    private final double[] aJ;
    private long aK;
    public boolean valid;
    public ProjectileSource projectileSource;
    public boolean forceExplosionKnockback;
    public Timing tickTimer;
    public Location origin;
    public final byte activationType;
    public final boolean defaultActivationState;
    public long activatedTick;
    public boolean fromMobSpawner;
    public boolean spawnedViaMobSpawner;
    protected int numCollisions;
    private WeakReference<Chunk> currentChunk;
    private String entityKeyString;
    private MinecraftKey entityKey;

    static boolean isLevelAtLeast(final NBTTagCompound tag, final int level) {
        return tag.hasKey("Bukkit.updateLevel") && tag.getInt("Bukkit.updateLevel") >= level;
    }

    public CraftEntity getBukkitEntity() {
        if (this.bukkitEntity == null) {
            this.bukkitEntity = CraftEntity.getEntity(this.world.getServer(), this);
        }
        return this.bukkitEntity;
    }

    public boolean blocksEntitySpawning() {
        return this.i;
    }

    public void setVehicle(final Entity entity) {
        this.au = entity;
    }

    public double getX() {
        return this.locX;
    }

    public double getY() {
        return this.locY;
    }

    public double getZ() {
        return this.locZ;
    }

    public boolean isAddedToChunk() {
        return this.aa;
    }

    public int getChunkX() {
        return this.ab;
    }

    public int getChunkY() {
        return this.ac;
    }

    public int getChunkZ() {
        return this.ad;
    }

    public boolean inPortal() {
        return this.ak;
    }

    public void inactiveTick() {
    }

    public float getBukkitYaw() {
        return this.yaw;
    }

    public Entity(final World world) {
        this.entitySlice = null;
        this.tickTimer = MinecraftTimings.getEntityTimings(this);
        this.activationType = ActivationRange.initializeEntityActivationType(this);
        this.activatedTick = -2147483648L;
        this.numCollisions = 0;
        this.currentChunk = null;
        this.entityKeyString = null;
        this.entityKey = this.getMinecraftKey();
        this.id = Entity.entityCount++;
        this.passengers = Lists.newArrayList();
        this.boundingBox = Entity.c;
        this.width = 0.6f;
        this.length = 1.8f;
        this.ax = 1;
        this.ay = 1.0f;
        this.random = Entity.SHARED_RANDOM;
        this.fireTicks = -this.getMaxFireTicks();
        this.justCreated = true;
        this.uniqueID = MathHelper.a(this.random);
        this.ar = this.uniqueID.toString();
        this.aG = new CommandObjectiveExecutor();
        this.aH = Sets.newHashSet();
        this.aJ = new double[]{0.0, 0.0, 0.0};
        this.world = world;
        this.setPosition(0.0, 0.0, 0.0);
        if (world != null) {
            this.dimension = world.worldProvider.getDimensionManager().getDimensionID();
            this.defaultActivationState = ActivationRange.initializeEntityActivationState(this, world.spigotConfig);
        } else {
            this.defaultActivationState = false;
        }
        (this.datawatcher = new DataWatcher(this)).register((DataWatcherObject) Entity.Z, (Object) (byte) 0);
        this.datawatcher.register((DataWatcherObject) Entity.aA, (Object) 300);
        this.datawatcher.register((DataWatcherObject) Entity.aC, (Object) false);
        this.datawatcher.register((DataWatcherObject) Entity.aB, (Object) "");
        this.datawatcher.register((DataWatcherObject) Entity.aD, (Object) false);
        this.datawatcher.register((DataWatcherObject) Entity.aE, (Object) false);
        this.i();
    }

    public int getId() {
        return this.id;
    }

    public void h(final int i) {
        this.id = i;
    }

    public Set<String> getScoreboardTags() {
        return this.aH;
    }

    public boolean addScoreboardTag(final String s) {
        if (this.aH.size() >= 1024) {
            return false;
        }
        this.aH.add(s);
        return true;
    }

    public boolean removeScoreboardTag(final String s) {
        return this.aH.remove(s);
    }

    public void killEntity() {
        this.die();
    }

    protected abstract void i();

    public DataWatcher getDataWatcher() {
        return this.datawatcher;
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof Entity && ((Entity) object).id == this.id;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    public void die() {
        this.dead = true;
    }

    public void b(final boolean flag) {
    }

    public void setSize(final float f, final float f1) {
        if (f != this.width || f1 != this.length) {
            final float f2 = this.width;
            this.width = f;
            this.length = f1;
            if (this.width < f2) {
                final double d0 = f / 2.0;
                this.a(new AxisAlignedBB(this.locX - d0, this.locY, this.locZ - d0, this.locX + d0,
                        this.locY + this.length, this.locZ + d0));
                return;
            }
            final AxisAlignedBB axisalignedbb = this.getBoundingBox();
            this.a(new AxisAlignedBB(axisalignedbb.a, axisalignedbb.b, axisalignedbb.c, axisalignedbb.a + this.width,
                    axisalignedbb.b + this.length, axisalignedbb.c + this.width));
            if (this.width > f2 && !this.justCreated && !this.world.isClientSide) {
                this.move(EnumMoveType.SELF, f2 - this.width, 0.0, f2 - this.width);
            }
        }
    }

    protected void setYawPitch(float f, float f1) {
        if (Float.isNaN(f)) {
            f = 0.0f;
        }
        if (f == Float.POSITIVE_INFINITY || f == Float.NEGATIVE_INFINITY) {
            if (this instanceof EntityPlayer) {
                this.world.getServer().getLogger()
                        .warning(this.getName() + " was caught trying to crash the server with an invalid yaw");
                ((CraftPlayer) this.getBukkitEntity()).kickPlayer("Infinite yaw (Hacking?)");
            }
            f = 0.0f;
        }
        if (Float.isNaN(f1)) {
            f1 = 0.0f;
        }
        if (f1 == Float.POSITIVE_INFINITY || f1 == Float.NEGATIVE_INFINITY) {
            if (this instanceof EntityPlayer) {
                this.world.getServer().getLogger()
                        .warning(this.getName() + " was caught trying to crash the server with an invalid pitch");
                ((CraftPlayer) this.getBukkitEntity()).kickPlayer("Infinite pitch (Hacking?)");
            }
            f1 = 0.0f;
        }
        this.yaw = f % 360.0f;
        this.pitch = f1 % 360.0f;
    }

    public void setPosition(final double d0, final double d1, final double d2) {
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        final float f = this.width / 2.0f;
        final float f2 = this.length;
        this.a(new AxisAlignedBB(d0 - f, d1, d2 - f, d0 + f, d1 + f2, d2 + f));

        if (this.valid) {
            this.world.entityJoinedWorld(this, false);
        }
    }

    public void B_() {
        if (!this.world.isClientSide) {
            this.setFlag(6, this.aW());
        }
        this.Y();
    }

    public void postTick() {
        if (!this.world.isClientSide && this.world instanceof WorldServer) {
            this.world.methodProfiler.a("portal");
            if (this.ak) {
                final MinecraftServer minecraftserver = this.world.getMinecraftServer();
                if (!this.isPassenger()) {
                    final int i = this.Z();
                    if (this.al++ >= i) {
                        this.al = i;
                        this.portalCooldown = this.aM();
                        byte b0;
                        if (this.world.worldProvider.getDimensionManager().getDimensionID() == -1) {
                            b0 = 0;
                        } else {
                            b0 = -1;
                        }
                        this.b(b0);
                    }
                }
                this.ak = false;
            } else {
                if (this.al > 0) {
                    this.al -= 4;
                }
                if (this.al < 0) {
                    this.al = 0;
                }
            }
            this.I();
            this.world.methodProfiler.b();
        }
    }

    public void Y() {
        this.world.methodProfiler.a("entityBaseTick");
        if (this.isPassenger() && this.bJ().dead) {
            this.stopRiding();
        }
        if (this.j > 0) {
            --this.j;
        }
        this.I = this.J;
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.lastPitch = this.pitch;
        this.lastYaw = this.yaw;
        this.as();
        this.aq();
        if (this.world.isClientSide) {
            this.extinguish();
        } else if (this.fireTicks > 0) {
            if (this.fireProof) {
                this.fireTicks -= 4;
                if (this.fireTicks < 0) {
                    this.extinguish();
                }
            } else {
                if (this.fireTicks % 20 == 0) {
                    this.damageEntity(DamageSource.BURN, 1.0f);
                }
                --this.fireTicks;
            }
        }
        if (this.au()) {
            this.burnFromLava();
            this.fallDistance *= 0.5f;
        }
        this.checkAndDoHeightDamage();
        if (!this.world.isClientSide) {
            this.setFlag(0, this.fireTicks > 0);
        }
        this.justCreated = false;
        this.world.methodProfiler.b();
    }

    private boolean paperNetherCheck() {
        return this.world.paperConfig.netherVoidTopDamage
                && this.world.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER && this.locY >= 128.0;
    }

    protected void checkAndDoHeightDamage() {
        if (this.locY < -64.0 || this.paperNetherCheck()) {
            this.kill();
        }
    }

    protected void I() {
        if (this.portalCooldown > 0) {
            --this.portalCooldown;
        }
    }

    public int Z() {
        return 1;
    }

    protected void burnFromLava() {
        if (!this.fireProof) {
            this.damageEntity(DamageSource.LAVA, 4.0f);
            if (this instanceof EntityLiving) {
                if (this.fireTicks <= 0) {
                    final Block damager = null;
                    final org.bukkit.entity.Entity damagee = (org.bukkit.entity.Entity) this.getBukkitEntity();
                    final EntityCombustEvent combustEvent = (EntityCombustEvent) new EntityCombustByBlockEvent(damager,
                            damagee, 15);
                    this.world.getServer().getPluginManager().callEvent((Event) combustEvent);
                    if (!combustEvent.isCancelled()) {
                        this.setOnFire(combustEvent.getDuration());
                    }
                } else {
                    this.setOnFire(15);
                }
                return;
            }
            this.setOnFire(15);
        }
    }

    public void setOnFire(final int i) {
        int j = i * 20;
        if (this instanceof EntityLiving) {
            j = EnchantmentProtection.a((EntityLiving) this, j);
        }
        if (this.fireTicks < j) {
            this.fireTicks = j;
        }
    }

    public void extinguish() {
        this.fireTicks = 0;
    }

    protected final void kill() {
        this.ac();
    }

    protected void ac() {
        this.die();
    }

    public boolean c(final double d0, final double d1, final double d2) {
        final AxisAlignedBB axisalignedbb = this.getBoundingBox().d(d0, d1, d2);
        return this.b(axisalignedbb);
    }

    private boolean b(final AxisAlignedBB axisalignedbb) {
        return this.world.getCubes(this, axisalignedbb).isEmpty() && !this.world.containsLiquid(axisalignedbb);
    }

    public void move(final EnumMoveType enummovetype, double d0, double d1, double d2) {
        if (this.noclip) {
            this.a(this.getBoundingBox().d(d0, d1, d2));
            this.recalcPosition();
        } else {
            try {
                this.checkBlockCollisions();
            } catch (Throwable throwable) {
                final CrashReport crashreport = CrashReport.a(throwable, "Checking entity block collision");
                final CrashReportSystemDetails crashreportsystemdetails = crashreport
                        .a("Entity being checked for collision");
                this.appendEntityCrashDetails(crashreportsystemdetails);
                throw new ReportedException(crashreport);
            }
            if (d0 == 0.0 && d1 == 0.0 && d2 == 0.0 && this.isVehicle() && this.isPassenger()) {
                return;
            }
            if (enummovetype == EnumMoveType.PISTON) {
                final long i = this.world.getTime();
                if (i != this.aK) {
                    Arrays.fill(this.aJ, 0.0);
                    this.aK = i;
                }
                if (d0 != 0.0) {
                    final int j = EnumDirection.EnumAxis.X.ordinal();
                    final double d3 = MathHelper.a(d0 + this.aJ[j], -0.51, 0.51);
                    d0 = d3 - this.aJ[j];
                    this.aJ[j] = d3;
                    if (Math.abs(d0) <= 9.999999747378752E-6) {
                        return;
                    }
                } else if (d1 != 0.0) {
                    final int j = EnumDirection.EnumAxis.Y.ordinal();
                    final double d3 = MathHelper.a(d1 + this.aJ[j], -0.51, 0.51);
                    d1 = d3 - this.aJ[j];
                    this.aJ[j] = d3;
                    if (Math.abs(d1) <= 9.999999747378752E-6) {
                        return;
                    }
                } else {
                    if (d2 == 0.0) {
                        return;
                    }
                    final int j = EnumDirection.EnumAxis.Z.ordinal();
                    final double d3 = MathHelper.a(d2 + this.aJ[j], -0.51, 0.51);
                    d2 = d3 - this.aJ[j];
                    this.aJ[j] = d3;
                    if (Math.abs(d2) <= 9.999999747378752E-6) {
                        return;
                    }
                }
            }
            this.world.methodProfiler.a("move");
            final double d4 = this.locX;
            final double d5 = this.locY;
            final double d6 = this.locZ;
            if (this.E) {
                this.E = false;
                d0 *= 0.25;
                d1 *= 0.05000000074505806;
                d2 *= 0.25;
                this.motX = 0.0;
                this.motY = 0.0;
                this.motZ = 0.0;
            }
            double d7 = d0;
            final double d8 = d1;
            double d9 = d2;
            if ((enummovetype == EnumMoveType.SELF || enummovetype == EnumMoveType.PLAYER) && this.onGround
                    && this.isSneaking() && this instanceof EntityHuman) {
                final double d10 = 0.05;
                while (d0 != 0.0
                        && this.world.getCubes(this, this.getBoundingBox().d(d0, (double) (-this.P), 0.0)).isEmpty()) {
                    if (d0 < 0.05 && d0 >= -0.05) {
                        d0 = 0.0;
                    } else if (d0 > 0.0) {
                        d0 -= 0.05;
                    } else {
                        d0 += 0.05;
                    }
                    d7 = d0;
                }
                while (d2 != 0.0
                        && this.world.getCubes(this, this.getBoundingBox().d(0.0, (double) (-this.P), d2)).isEmpty()) {
                    if (d2 < 0.05 && d2 >= -0.05) {
                        d2 = 0.0;
                    } else if (d2 > 0.0) {
                        d2 -= 0.05;
                    } else {
                        d2 += 0.05;
                    }
                    d9 = d2;
                }
                while (d0 != 0.0 && d2 != 0.0
                        && this.world.getCubes(this, this.getBoundingBox().d(d0, (double) (-this.P), d2)).isEmpty()) {
                    if (d0 < 0.05 && d0 >= -0.05) {
                        d0 = 0.0;
                    } else if (d0 > 0.0) {
                        d0 -= 0.05;
                    } else {
                        d0 += 0.05;
                    }
                    d7 = d0;
                    if (d2 < 0.05 && d2 >= -0.05) {
                        d2 = 0.0;
                    } else if (d2 > 0.0) {
                        d2 -= 0.05;
                    } else {
                        d2 += 0.05;
                    }
                    d9 = d2;
                }
            }
            final List<AxisAlignedBB> list = this.world.getCubes(this, this.getBoundingBox().b(d0, d1, d2));
            final AxisAlignedBB axisalignedbb = this.getBoundingBox();
            if (d1 != 0.0) {
                for (int k = 0, l = list.size(); k < l; ++k) {
                    d1 = list.get(k).b(this.getBoundingBox(), d1);
                }
                this.a(this.getBoundingBox().d(0.0, d1, 0.0));
            }
            if (d0 != 0.0) {
                for (int k = 0, l = list.size(); k < l; ++k) {
                    d0 = list.get(k).a(this.getBoundingBox(), d0);
                }
                if (d0 != 0.0) {
                    this.a(this.getBoundingBox().d(d0, 0.0, 0.0));
                }
            }
            if (d2 != 0.0) {
                for (int k = 0, l = list.size(); k < l; ++k) {
                    d2 = list.get(k).c(this.getBoundingBox(), d2);
                }
                if (d2 != 0.0) {
                    this.a(this.getBoundingBox().d(0.0, 0.0, d2));
                }
            }
            final boolean flag = this.onGround || (d1 != d8 && d1 < 0.0);
            if (this.P > 0.0f && flag && (d7 != d0 || d9 != d2)) {
                final double d11 = d0;
                final double d12 = d1;
                final double d13 = d2;
                final AxisAlignedBB axisalignedbb2 = this.getBoundingBox();
                this.a(axisalignedbb);
                d1 = this.P;
                final List<AxisAlignedBB> list2 = this.world.getCubes(this, this.getBoundingBox().b(d7, d1, d9));
                AxisAlignedBB axisalignedbb3 = this.getBoundingBox();
                final AxisAlignedBB axisalignedbb4 = axisalignedbb3.b(d7, 0.0, d9);
                double d14 = d1;
                for (int i2 = 0, j2 = list2.size(); i2 < j2; ++i2) {
                    d14 = list2.get(i2).b(axisalignedbb4, d14);
                }
                axisalignedbb3 = axisalignedbb3.d(0.0, d14, 0.0);
                double d15 = d7;
                for (int k2 = 0, l2 = list2.size(); k2 < l2; ++k2) {
                    d15 = list2.get(k2).a(axisalignedbb3, d15);
                }
                axisalignedbb3 = axisalignedbb3.d(d15, 0.0, 0.0);
                double d16 = d9;
                for (int i3 = 0, j3 = list2.size(); i3 < j3; ++i3) {
                    d16 = list2.get(i3).c(axisalignedbb3, d16);
                }
                axisalignedbb3 = axisalignedbb3.d(0.0, 0.0, d16);
                AxisAlignedBB axisalignedbb5 = this.getBoundingBox();
                double d17 = d1;
                for (int k3 = 0, l3 = list2.size(); k3 < l3; ++k3) {
                    d17 = list2.get(k3).b(axisalignedbb5, d17);
                }
                axisalignedbb5 = axisalignedbb5.d(0.0, d17, 0.0);
                double d18 = d7;
                for (int i4 = 0, j4 = list2.size(); i4 < j4; ++i4) {
                    d18 = list2.get(i4).a(axisalignedbb5, d18);
                }
                axisalignedbb5 = axisalignedbb5.d(d18, 0.0, 0.0);
                double d19 = d9;
                for (int k4 = 0, l4 = list2.size(); k4 < l4; ++k4) {
                    d19 = list2.get(k4).c(axisalignedbb5, d19);
                }
                axisalignedbb5 = axisalignedbb5.d(0.0, 0.0, d19);
                final double d20 = d15 * d15 + d16 * d16;
                final double d21 = d18 * d18 + d19 * d19;
                if (d20 > d21) {
                    d0 = d15;
                    d2 = d16;
                    d1 = -d14;
                    this.a(axisalignedbb3);
                } else {
                    d0 = d18;
                    d2 = d19;
                    d1 = -d17;
                    this.a(axisalignedbb5);
                }
                for (int i5 = 0, j5 = list2.size(); i5 < j5; ++i5) {
                    d1 = list2.get(i5).b(this.getBoundingBox(), d1);
                }
                this.a(this.getBoundingBox().d(0.0, d1, 0.0));
                if (d11 * d11 + d13 * d13 >= d0 * d0 + d2 * d2) {
                    d0 = d11;
                    d1 = d12;
                    d2 = d13;
                    this.a(axisalignedbb2);
                }
            }
            this.world.methodProfiler.b();
            this.world.methodProfiler.a("rest");
            this.recalcPosition();
            this.positionChanged = (d7 != d0 || d9 != d2);
            this.B = (d1 != d8);
            this.onGround = (this.B && d8 < 0.0);
            this.C = (this.positionChanged || this.B);
            final int l = MathHelper.floor(this.locX);
            final int k5 = MathHelper.floor(this.locY - 0.20000000298023224);
            final int l5 = MathHelper.floor(this.locZ);
            BlockPosition blockposition = new BlockPosition(l, k5, l5);
            IBlockData iblockdata = this.world.getType(blockposition);
            if (iblockdata.getMaterial() == Material.AIR) {
                final BlockPosition blockposition2 = blockposition.down();
                final IBlockData iblockdata2 = this.world.getType(blockposition2);
                final net.minecraft.server.v1_12_R1.Block block = iblockdata2.getBlock();
                if (block instanceof BlockFence || block instanceof BlockCobbleWall
                        || block instanceof BlockFenceGate) {
                    iblockdata = iblockdata2;
                    blockposition = blockposition2;
                }
            }
            this.a(d1, this.onGround, iblockdata, blockposition);
            if (d7 != d0) {
                this.motX = 0.0;
            }
            if (d9 != d2) {
                this.motZ = 0.0;
            }
            final net.minecraft.server.v1_12_R1.Block block2 = iblockdata.getBlock();
            if (d8 != d1) {
                block2.a(this.world, this);
            }
            if (this.positionChanged && this.getBukkitEntity() instanceof Vehicle) {
                final Vehicle vehicle = (Vehicle) this.getBukkitEntity();
                Block bl = this.world.getWorld().getBlockAt(MathHelper.floor(this.locX), MathHelper.floor(this.locY),
                        MathHelper.floor(this.locZ));
                if (d7 > d0) {
                    bl = bl.getRelative(BlockFace.EAST);
                } else if (d7 < d0) {
                    bl = bl.getRelative(BlockFace.WEST);
                } else if (d9 > d2) {
                    bl = bl.getRelative(BlockFace.SOUTH);
                } else if (d9 < d2) {
                    bl = bl.getRelative(BlockFace.NORTH);
                }
                if (bl.getType() != org.bukkit.Material.AIR) {
                    final VehicleBlockCollisionEvent event = new VehicleBlockCollisionEvent(vehicle, bl);
                    this.world.getServer().getPluginManager().callEvent((Event) event);
                }
            }
            if (this.playStepSound() && (!this.onGround || !this.isSneaking() || !(this instanceof EntityHuman))
                    && !this.isPassenger()) {
                final double d22 = this.locX - d4;
                double d23 = this.locY - d5;
                final double d14 = this.locZ - d6;
                if (block2 != Blocks.LADDER) {
                    d23 = 0.0;
                }
                if (block2 != null && this.onGround) {
                    block2.stepOn(this.world, blockposition, this);
                }
                this.J += (float) (MathHelper.sqrt(d22 * d22 + d14 * d14) * 0.6);
                this.K += (float) (MathHelper.sqrt(d22 * d22 + d23 * d23 + d14 * d14) * 0.6);
                if (this.K > this.ax && iblockdata.getMaterial() != Material.AIR) {
                    this.ax = (int) this.K + 1;
                    if (this.isInWater()) {
                        final Entity entity = (this.isVehicle() && this.bE() != null) ? this.bE() : this;
                        final float f = (entity == this) ? 0.35f : 0.4f;
                        float f2 = MathHelper.sqrt(entity.motX * entity.motX * 0.20000000298023224
                                + entity.motY * entity.motY + entity.motZ * entity.motZ * 0.20000000298023224) * f;
                        if (f2 > 1.0f) {
                            f2 = 1.0f;
                        }
                        this.a(this.ae(), f2, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
                    } else {
                        this.a(blockposition, block2);
                    }
                } else if (this.K > this.ay && this.ah() && iblockdata.getMaterial() == Material.AIR) {
                    this.ay = this.d(this.K);
                }
            }
            final boolean flag2 = this.an();
            if (this.world.e(this.getBoundingBox().shrink(0.001))) {
                this.burn(1.0f);
                if (!flag2) {
                    ++this.fireTicks;
                    if (this.fireTicks == 0) {
                        final EntityCombustEvent event2 = (EntityCombustEvent) new EntityCombustByBlockEvent(
                                (Block) null, (org.bukkit.entity.Entity) this.getBukkitEntity(), 8);
                        this.world.getServer().getPluginManager().callEvent((Event) event2);
                        if (!event2.isCancelled()) {
                            this.setOnFire(event2.getDuration());
                        }
                    }
                }
            } else if (this.fireTicks <= 0) {
                this.fireTicks = -this.getMaxFireTicks();
            }
            if (flag2 && this.isBurning()) {
                this.a(SoundEffects.bW, 0.7f, 1.6f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
                this.fireTicks = -this.getMaxFireTicks();
            }
            this.world.methodProfiler.b();
        }
    }

    public void recalcPosition() {
        final AxisAlignedBB axisalignedbb = this.getBoundingBox();
        this.locX = (axisalignedbb.a + axisalignedbb.d) / 2.0;
        this.locY = axisalignedbb.b;
        this.locZ = (axisalignedbb.c + axisalignedbb.f) / 2.0;
        if (this.valid) {
            this.world.entityJoinedWorld(this, false);
        }
    }

    protected SoundEffect ae() {
        return SoundEffects.ca;
    }

    protected SoundEffect af() {
        return SoundEffects.bZ;
    }

    protected void checkBlockCollisions() {
        final AxisAlignedBB axisalignedbb = this.getBoundingBox();
        final BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                .d(axisalignedbb.a + 0.001, axisalignedbb.b + 0.001, axisalignedbb.c + 0.001);
        final BlockPosition.PooledBlockPosition blockposition_pooledblockposition2 = BlockPosition.PooledBlockPosition
                .d(axisalignedbb.d - 0.001, axisalignedbb.e - 0.001, axisalignedbb.f - 0.001);
        final BlockPosition.PooledBlockPosition blockposition_pooledblockposition3 = BlockPosition.PooledBlockPosition
                .s();
        if (this.world.areChunksLoadedBetween((BlockPosition) blockposition_pooledblockposition,
                (BlockPosition) blockposition_pooledblockposition2)) {
            for (int i = blockposition_pooledblockposition.getX(); i <= blockposition_pooledblockposition2
                    .getX(); ++i) {
                for (int j = blockposition_pooledblockposition.getY(); j <= blockposition_pooledblockposition2
                        .getY(); ++j) {
                    for (int k = blockposition_pooledblockposition.getZ(); k <= blockposition_pooledblockposition2
                            .getZ(); ++k) {
                        blockposition_pooledblockposition3.f(i, j, k);
                        final IBlockData iblockdata = this.world
                                .getType((BlockPosition) blockposition_pooledblockposition3);
                        try {
                            iblockdata.getBlock().a(this.world, (BlockPosition) blockposition_pooledblockposition3,
                                    iblockdata, this);
                            this.a(iblockdata);
                        } catch (Throwable throwable) {
                            final CrashReport crashreport = CrashReport.a(throwable, "Colliding entity with block");
                            final CrashReportSystemDetails crashreportsystemdetails = crashreport
                                    .a("Block being collided with");
                            CrashReportSystemDetails.a(crashreportsystemdetails,
                                    (BlockPosition) blockposition_pooledblockposition3, iblockdata);
                            throw new ReportedException(crashreport);
                        }
                    }
                }
            }
        }
        blockposition_pooledblockposition.t();
        blockposition_pooledblockposition2.t();
        blockposition_pooledblockposition3.t();
    }

    protected void a(final IBlockData iblockdata) {
    }

    protected void a(final BlockPosition blockposition, final net.minecraft.server.v1_12_R1.Block block) {
        SoundEffectType soundeffecttype = block.getStepSound();
        if (this.world.getType(blockposition.up()).getBlock() == Blocks.SNOW_LAYER) {
            soundeffecttype = Blocks.SNOW_LAYER.getStepSound();
            this.a(soundeffecttype.d(), soundeffecttype.a() * 0.15f, soundeffecttype.b());
        } else if (!block.getBlockData().getMaterial().isLiquid()) {
            this.a(soundeffecttype.d(), soundeffecttype.a() * 0.15f, soundeffecttype.b());
        }
    }

    protected float d(final float f) {
        return 0.0f;
    }

    protected boolean ah() {
        return false;
    }

    public void a(final SoundEffect soundeffect, final float f, final float f1) {
        if (!this.isSilent()) {
            this.world.a(null, this.locX, this.locY, this.locZ, soundeffect, this.bK(), f, f1);
        }
    }

    public boolean isSilent() {
        return (boolean) this.datawatcher.get((DataWatcherObject) Entity.aD);
    }

    public void setSilent(final boolean flag) {
        this.datawatcher.set((DataWatcherObject) Entity.aD, (Object) flag);
    }

    public boolean isNoGravity() {
        return (boolean) this.datawatcher.get((DataWatcherObject) Entity.aE);
    }

    public void setNoGravity(final boolean flag) {
        this.datawatcher.set((DataWatcherObject) Entity.aE, (Object) flag);
    }

    protected boolean playStepSound() {
        return true;
    }

    protected void a(final double d0, final boolean flag, final IBlockData iblockdata,
                     final BlockPosition blockposition) {
        if (flag) {
            if (this.fallDistance > 0.0f) {
                iblockdata.getBlock().fallOn(this.world, blockposition, this, this.fallDistance);
            }
            this.fallDistance = 0.0f;
        } else if (d0 < 0.0) {
            this.fallDistance -= (float) d0;
        }
    }

    @Nullable
    public AxisAlignedBB al() {
        return null;
    }

    protected void burn(final float i) {
        if (!this.fireProof) {
            this.damageEntity(DamageSource.FIRE, i);
        }
    }

    public final boolean isFireProof() {
        return this.fireProof;
    }

    public void e(final float f, final float f1) {
        if (this.isVehicle()) {
            for (final Entity entity : this.bF()) {
                entity.e(f, f1);
            }
        }
    }

    public boolean an() {
        if (this.inWater) {
            return true;
        }
        final BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                .d(this.locX, this.locY, this.locZ);
        if (!this.world.isRainingAt((BlockPosition) blockposition_pooledblockposition) && !this.world.isRainingAt(
                (BlockPosition) blockposition_pooledblockposition.e(this.locX, this.locY + this.length, this.locZ))) {
            blockposition_pooledblockposition.t();
            return false;
        }
        blockposition_pooledblockposition.t();
        return true;
    }

    public boolean isInWater() {
        return this.inWater;
    }

    public boolean ap() {
        return this.world.a(this.getBoundingBox().grow(0.0, -20.0, 0.0).shrink(0.001), Material.WATER, this);
    }

    public boolean aq() {
        return this.doWaterMovement();
    }

    public boolean doWaterMovement() {
        if (this.bJ() instanceof EntityBoat) {
            this.inWater = false;
        } else if (this.world.a(this.getBoundingBox().grow(0.0, -0.4000000059604645, 0.0).shrink(0.001), Material.WATER,
                this)) {
            if (!this.inWater && !this.justCreated) {
                this.ar();
            }
            this.fallDistance = 0.0f;
            this.inWater = true;
            this.extinguish();
        } else {
            this.inWater = false;
        }
        return this.inWater;
    }

    protected void ar() {
        final Entity entity = (this.isVehicle() && this.bE() != null) ? this.bE() : this;
        final float f = (entity == this) ? 0.2f : 0.9f;
        float f2 = MathHelper.sqrt(entity.motX * entity.motX * 0.20000000298023224 + entity.motY * entity.motY
                + entity.motZ * entity.motZ * 0.20000000298023224) * f;
        if (f2 > 1.0f) {
            f2 = 1.0f;
        }
        this.a(this.af(), f2, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.4f);
        final float f3 = MathHelper.floor(this.getBoundingBox().b);
        for (int i = 0; i < 1.0f + this.width * 20.0f; ++i) {
            final float f4 = (this.random.nextFloat() * 2.0f - 1.0f) * this.width;
            final float f5 = (this.random.nextFloat() * 2.0f - 1.0f) * this.width;
            this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX + f4, f3 + 1.0f, this.locZ + f5, this.motX,
                    this.motY - this.random.nextFloat() * 0.2f, this.motZ, new int[0]);
        }
        for (int i = 0; i < 1.0f + this.width * 20.0f; ++i) {
            final float f4 = (this.random.nextFloat() * 2.0f - 1.0f) * this.width;
            final float f5 = (this.random.nextFloat() * 2.0f - 1.0f) * this.width;
            this.world.addParticle(EnumParticle.WATER_SPLASH, this.locX + f4, f3 + 1.0f, this.locZ + f5, this.motX,
                    this.motY, this.motZ, new int[0]);
        }
    }

    public void as() {
        if (this.isSprinting() && !this.isInWater()) {
            this.at();
        }
    }

    protected void at() {
        final int i = MathHelper.floor(this.locX);
        final int j = MathHelper.floor(this.locY - 0.20000000298023224);
        final int k = MathHelper.floor(this.locZ);
        final BlockPosition blockposition = new BlockPosition(i, j, k);
        final IBlockData iblockdata = this.world.getType(blockposition);
        if (iblockdata.i() != EnumRenderType.INVISIBLE) {
            this.world.addParticle(EnumParticle.BLOCK_CRACK, this.locX + (this.random.nextFloat() - 0.5) * this.width,
                    this.getBoundingBox().b + 0.1, this.locZ + (this.random.nextFloat() - 0.5) * this.width,
                    -this.motX * 4.0, 1.5, -this.motZ * 4.0,
                    net.minecraft.server.v1_12_R1.Block.getCombinedId(iblockdata));
        }
    }

    public boolean a(final Material material) {
        if (this.bJ() instanceof EntityBoat) {
            return false;
        }
        final double d0 = this.locY + this.getHeadHeight();
        final BlockPosition blockposition = new BlockPosition(this.locX, d0, this.locZ);
        final IBlockData iblockdata = this.world.getType(blockposition);
        if (iblockdata.getMaterial() == material) {
            final float f = BlockFluids.b(iblockdata.getBlock().toLegacyData(iblockdata)) - 0.11111111f;
            final float f2 = blockposition.getY() + 1 - f;
            final boolean flag = d0 < f2;
            return (flag || !(this instanceof EntityHuman)) && flag;
        }
        return false;
    }

    public boolean au() {
        return this.world.a(this.getBoundingBox().grow(-0.10000000149011612, -0.4000000059604645, -0.10000000149011612),
                Material.LAVA);
    }

    public void b(float f, float f1, float f2, final float f3) {
        float f4 = f * f + f1 * f1 + f2 * f2;
        if (f4 >= 1.0E-4f) {
            f4 = MathHelper.c(f4);
            if (f4 < 1.0f) {
                f4 = 1.0f;
            }
            f4 = f3 / f4;
            f *= f4;
            f1 *= f4;
            f2 *= f4;
            final float f5 = MathHelper.sin(this.yaw * 0.017453292f);
            final float f6 = MathHelper.cos(this.yaw * 0.017453292f);
            this.motX += f * f6 - f2 * f5;
            this.motY += f1;
            this.motZ += f2 * f6 + f * f5;
        }
    }

    public float aw() {
        final BlockPosition.MutableBlockPosition blockposition_mutableblockposition = new BlockPosition.MutableBlockPosition(
                MathHelper.floor(this.locX), 0, MathHelper.floor(this.locZ));
        if (this.world.isLoaded((BlockPosition) blockposition_mutableblockposition)) {
            blockposition_mutableblockposition.p(MathHelper.floor(this.locY + this.getHeadHeight()));
            return this.world.n((BlockPosition) blockposition_mutableblockposition);
        }
        return 0.0f;
    }

    public void spawnIn(final World world) {
        if (world == null) {
            this.die();
            this.world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
            return;
        }
        this.world = world;
    }

    public void setLocation(final double d0, final double d1, final double d2, final float f, float f1) {
        this.locX = MathHelper.a(d0, -3.0E7, 3.0E7);
        this.locY = d1;
        this.locZ = MathHelper.a(d2, -3.0E7, 3.0E7);
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        f1 = MathHelper.a(f1, -90.0f, 90.0f);
        this.yaw = f;
        this.pitch = f1;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        final double d3 = this.lastYaw - f;
        if (d3 < -180.0) {
            this.lastYaw += 360.0f;
        }
        if (d3 >= 180.0) {
            this.lastYaw -= 360.0f;
        }
        this.world.getChunkAt((int) Math.floor(this.locX) >> 4, (int) Math.floor(this.locZ) >> 4);
        this.setPosition(this.locX, this.locY, this.locZ);
        this.setYawPitch(f, f1);
    }

    public void setPositionRotation(final BlockPosition blockposition, final float f, final float f1) {
        this.setPositionRotation(blockposition.getX() + 0.5, blockposition.getY(), blockposition.getZ() + 0.5, f, f1);
    }

    public void setPositionRotation(final double d0, final double d1, final double d2, final float f, final float f1) {
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        this.lastX = this.locX;
        this.lastY = this.locY;
        this.lastZ = this.locZ;
        this.M = this.locX;
        this.N = this.locY;
        this.O = this.locZ;
        this.yaw = f;
        this.pitch = f1;
        this.setPosition(this.locX, this.locY, this.locZ);
    }

    public float g(final Entity entity) {
        final float f = (float) (this.locX - entity.locX);
        final float f2 = (float) (this.locY - entity.locY);
        final float f3 = (float) (this.locZ - entity.locZ);
        return MathHelper.c(f * f + f2 * f2 + f3 * f3);
    }

    public double d(final double d0, final double d1, final double d2) {
        final double d3 = this.locX - d0;
        final double d4 = this.locY - d1;
        final double d5 = this.locZ - d2;
        return d3 * d3 + d4 * d4 + d5 * d5;
    }

    public double c(final BlockPosition blockposition) {
        return blockposition.distanceSquared(this.locX, this.locY, this.locZ);
    }

    public double d(final BlockPosition blockposition) {
        return blockposition.g(this.locX, this.locY, this.locZ);
    }

    public double e(final double d0, final double d1, final double d2) {
        final double d3 = this.locX - d0;
        final double d4 = this.locY - d1;
        final double d5 = this.locZ - d2;
        return MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);
    }

    public double h(final Entity entity) {
        final double d0 = this.locX - entity.locX;
        final double d2 = this.locY - entity.locY;
        final double d3 = this.locZ - entity.locZ;
        return d0 * d0 + d2 * d2 + d3 * d3;
    }

    public void d(final EntityHuman entityhuman) {
    }

    public void collide(final Entity entity) {
        if (!this.x(entity) && !entity.noclip && !this.noclip) {
            double d0 = entity.locX - this.locX;
            double d2 = entity.locZ - this.locZ;
            double d3 = MathHelper.a(d0, d2);
            if (d3 >= 0.009999999776482582) {
                d3 = MathHelper.sqrt(d3);
                d0 /= d3;
                d2 /= d3;
                double d4 = 1.0 / d3;
                if (d4 > 1.0) {
                    d4 = 1.0;
                }
                d0 *= d4;
                d2 *= d4;
                d0 *= 0.05000000074505806;
                d2 *= 0.05000000074505806;
                d0 *= 1.0f - this.R;
                d2 *= 1.0f - this.R;
                if (!this.isVehicle()) {
                    this.f(-d0, 0.0, -d2);
                }
                if (!entity.isVehicle()) {
                    entity.f(d0, 0.0, d2);
                }
            }
        }
    }

    public void f(final double d0, final double d1, final double d2) {
        this.motX += d0;
        this.motY += d1;
        this.motZ += d2;
        this.impulse = true;
    }

    protected void ax() {
        this.velocityChanged = true;
    }

    public boolean damageEntity(final DamageSource damagesource, final float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        }
        this.ax();
        return false;
    }

    public Vec3D e(final float f) {
        if (f == 1.0f) {
            return this.f(this.pitch, this.yaw);
        }
        final float f2 = this.lastPitch + (this.pitch - this.lastPitch) * f;
        final float f3 = this.lastYaw + (this.yaw - this.lastYaw) * f;
        return this.f(f2, f3);
    }

    protected final Vec3D f(final float f, final float f1) {
        final float f2 = MathHelper.cos(-f1 * 0.017453292f - 3.1415927f);
        final float f3 = MathHelper.sin(-f1 * 0.017453292f - 3.1415927f);
        final float f4 = -MathHelper.cos(-f * 0.017453292f);
        final float f5 = MathHelper.sin(-f * 0.017453292f);
        return new Vec3D((double) (f3 * f4), (double) f5, (double) (f2 * f4));
    }

    public Vec3D f(final float f) {
        if (f == 1.0f) {
            return new Vec3D(this.locX, this.locY + this.getHeadHeight(), this.locZ);
        }
        final double d0 = this.lastX + (this.locX - this.lastX) * f;
        final double d2 = this.lastY + (this.locY - this.lastY) * f + this.getHeadHeight();
        final double d3 = this.lastZ + (this.locZ - this.lastZ) * f;
        return new Vec3D(d0, d2, d3);
    }

    public boolean isInteractable() {
        return false;
    }

    public boolean isCollidable() {
        return false;
    }

    public void runKillTrigger(final Entity entity, final int kills, final DamageSource damageSource) {
        this.a(entity, kills, damageSource);
    }

    public void a(final Entity entity, final int i, final DamageSource damagesource) {
        if (entity instanceof EntityPlayer) {
            CriterionTriggers.c.a((EntityPlayer) entity, this, damagesource);
        }
    }

    public boolean c(final NBTTagCompound nbttagcompound) {
        final String s = this.getSaveID();
        if (!this.dead && s != null) {
            nbttagcompound.setString("id", s);
            this.save(nbttagcompound);
            return true;
        }
        return false;
    }

    public boolean d(final NBTTagCompound nbttagcompound) {
        final String s = this.getSaveID();
        if (!this.dead && s != null && !this.isPassenger()) {
            nbttagcompound.setString("id", s);
            this.save(nbttagcompound);
            return true;
        }
        return false;
    }

    public static void b(final DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.ENTITY, (DataInspector) new DataInspector() {
            public NBTTagCompound a(final DataConverter dataconverter, final NBTTagCompound nbttagcompound,
                                    final int i) {
                if (nbttagcompound.hasKeyOfType("Passengers", 9)) {
                    final NBTTagList nbttaglist = nbttagcompound.getList("Passengers", 10);
                    for (int j = 0; j < nbttaglist.size(); ++j) {
                        nbttaglist.a(j, (NBTBase) dataconverter.a((DataConverterType) DataConverterTypes.ENTITY,
                                nbttaglist.get(j), i));
                    }
                }
                return nbttagcompound;
            }
        });
    }

    public NBTTagCompound save(final NBTTagCompound nbttagcompound) {
        try {
            nbttagcompound.set("Pos", (NBTBase) this.a(this.locX, this.locY, this.locZ));
            nbttagcompound.set("Motion", (NBTBase) this.a(this.motX, this.motY, this.motZ));
            if (Float.isNaN(this.yaw)) {
                this.yaw = 0.0f;
            }
            if (Float.isNaN(this.pitch)) {
                this.pitch = 0.0f;
            }
            nbttagcompound.set("Rotation", (NBTBase) this.a(this.yaw, this.pitch));
            nbttagcompound.setFloat("FallDistance", this.fallDistance);
            nbttagcompound.setShort("Fire", (short) this.fireTicks);
            nbttagcompound.setShort("Air", (short) this.getAirTicks());
            nbttagcompound.setBoolean("OnGround", this.onGround);
            nbttagcompound.setInt("Dimension", this.dimension);
            nbttagcompound.setBoolean("Invulnerable", this.invulnerable);
            nbttagcompound.setInt("PortalCooldown", this.portalCooldown);
            nbttagcompound.a("UUID", this.getUniqueID());
            nbttagcompound.setLong("WorldUUIDLeast", this.world.getDataManager().getUUID().getLeastSignificantBits());
            nbttagcompound.setLong("WorldUUIDMost", this.world.getDataManager().getUUID().getMostSignificantBits());
            nbttagcompound.setInt("Bukkit.updateLevel", 2);
            nbttagcompound.setInt("Spigot.ticksLived", this.ticksLived);
            if (this.hasCustomName()) {
                nbttagcompound.setString("CustomName", this.getCustomName());
            }
            if (this.getCustomNameVisible()) {
                nbttagcompound.setBoolean("CustomNameVisible", this.getCustomNameVisible());
            }
            this.aG.b(nbttagcompound);
            if (this.isSilent()) {
                nbttagcompound.setBoolean("Silent", this.isSilent());
            }
            if (this.isNoGravity()) {
                nbttagcompound.setBoolean("NoGravity", this.isNoGravity());
            }
            if (this.glowing) {
                nbttagcompound.setBoolean("Glowing", this.glowing);
            }
            if (!this.aH.isEmpty()) {
                final NBTTagList nbttaglist = new NBTTagList();
                for (final String s : this.aH) {
                    nbttaglist.add((NBTBase) new NBTTagString(s));
                }
                nbttagcompound.set("Tags", (NBTBase) nbttaglist);
            }
            this.b(nbttagcompound);
            if (this.isVehicle()) {
                final NBTTagList nbttaglist = new NBTTagList();
                for (final Entity entity : this.bF()) {
                    final NBTTagCompound nbttagcompound2 = new NBTTagCompound();
                    if (entity.c(nbttagcompound2)) {
                        nbttaglist.add((NBTBase) nbttagcompound2);
                    }
                }
                if (!nbttaglist.isEmpty()) {
                    nbttagcompound.set("Passengers", (NBTBase) nbttaglist);
                }
            }
            if (this.origin != null) {
                nbttagcompound.set("Paper.Origin",
                        (NBTBase) this.createList(this.origin.getX(), this.origin.getY(), this.origin.getZ()));
            }
            if (this.spawnedViaMobSpawner) {
                nbttagcompound.setBoolean("Paper.FromMobSpawner", true);
            }
            return nbttagcompound;
        } catch (Throwable throwable) {
            final CrashReport crashreport = CrashReport.a(throwable, "Saving entity NBT");
            final CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being saved");
            this.appendEntityCrashDetails(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    public void f(final NBTTagCompound nbttagcompound) {
        try {
            final NBTTagList nbttaglist = nbttagcompound.getList("Pos", 6);
            final NBTTagList nbttaglist2 = nbttagcompound.getList("Motion", 6);
            final NBTTagList nbttaglist3 = nbttagcompound.getList("Rotation", 5);
            this.motX = nbttaglist2.f(0);
            this.motY = nbttaglist2.f(1);
            this.motZ = nbttaglist2.f(2);
            this.locX = nbttaglist.f(0);
            this.locY = nbttaglist.f(1);
            this.locZ = nbttaglist.f(2);
            this.M = this.locX;
            this.N = this.locY;
            this.O = this.locZ;
            this.lastX = this.locX;
            this.lastY = this.locY;
            this.lastZ = this.locZ;
            this.yaw = nbttaglist3.g(0);
            this.pitch = nbttaglist3.g(1);
            this.lastYaw = this.yaw;
            this.lastPitch = this.pitch;
            this.setHeadRotation(this.yaw);
            this.h(this.yaw);
            this.fallDistance = nbttagcompound.getFloat("FallDistance");
            this.fireTicks = nbttagcompound.getShort("Fire");
            this.setAirTicks(nbttagcompound.getShort("Air"));
            this.onGround = nbttagcompound.getBoolean("OnGround");
            if (nbttagcompound.hasKey("Dimension")) {
                this.dimension = nbttagcompound.getInt("Dimension");
            }
            this.invulnerable = nbttagcompound.getBoolean("Invulnerable");
            this.portalCooldown = nbttagcompound.getInt("PortalCooldown");
            if (nbttagcompound.b("UUID")) {
                this.uniqueID = nbttagcompound.a("UUID");
                this.ar = this.uniqueID.toString();
            }
            this.setPosition(this.locX, this.locY, this.locZ);
            this.setYawPitch(this.yaw, this.pitch);
            if (nbttagcompound.hasKeyOfType("CustomName", 8)) {
                this.setCustomName(nbttagcompound.getString("CustomName"));
            }
            this.setCustomNameVisible(nbttagcompound.getBoolean("CustomNameVisible"));
            this.aG.a(nbttagcompound);
            this.setSilent(nbttagcompound.getBoolean("Silent"));
            this.setNoGravity(nbttagcompound.getBoolean("NoGravity"));
            this.g(nbttagcompound.getBoolean("Glowing"));
            if (nbttagcompound.hasKeyOfType("Tags", 9)) {
                this.aH.clear();
                final NBTTagList nbttaglist4 = nbttagcompound.getList("Tags", 8);
                for (int i = Math.min(nbttaglist4.size(), 1024), j = 0; j < i; ++j) {
                    this.aH.add(nbttaglist4.getString(j));
                }
            }
            this.a(nbttagcompound);
            if (this.aA()) {
                this.setPosition(this.locX, this.locY, this.locZ);
            }
            if (this instanceof EntityLiving) {
                final EntityLiving entity = (EntityLiving) this;
                this.ticksLived = nbttagcompound.getInt("Spigot.ticksLived");
                if (entity instanceof EntityTameableAnimal && !isLevelAtLeast(nbttagcompound, 2)
                        && !nbttagcompound.getBoolean("PersistenceRequired")) {
                    final EntityInsentient entityinsentient = (EntityInsentient) entity;
                    entityinsentient.persistent = !entityinsentient.isTypeNotPersistent();
                }
            }
            final double limit = (this.getBukkitEntity() instanceof Vehicle) ? 100.0 : 10.0;
            if (Math.abs(this.motX) > limit) {
                this.motX = 0.0;
            }
            if (Math.abs(this.motY) > limit) {
                this.motY = 0.0;
            }
            if (Math.abs(this.motZ) > limit) {
                this.motZ = 0.0;
            }
            if (this instanceof EntityPlayer) {
                final Server server = Bukkit.getServer();
                org.bukkit.World bworld = null;
                final String worldName = nbttagcompound.getString("world");
                if (nbttagcompound.hasKey("WorldUUIDMost") && nbttagcompound.hasKey("WorldUUIDLeast")) {
                    final UUID uid = new UUID(nbttagcompound.getLong("WorldUUIDMost"),
                            nbttagcompound.getLong("WorldUUIDLeast"));
                    bworld = server.getWorld(uid);
                } else {
                    bworld = server.getWorld(worldName);
                }
                if (bworld == null) {
                    final EntityPlayer entityPlayer = (EntityPlayer) this;
                    bworld = (org.bukkit.World) ((CraftServer) server).getServer()
                            .getWorldServer(entityPlayer.dimension).getWorld();
                }
                this.spawnIn((World) ((bworld == null) ? null : ((CraftWorld) bworld).getHandle()));
            }
            final NBTTagList originTag = nbttagcompound.getList("Paper.Origin", 6);
            if (!originTag.isEmpty()) {
                this.origin = new Location((org.bukkit.World) this.world.getWorld(), originTag.getDoubleAt(0),
                        originTag.getDoubleAt(1), originTag.getDoubleAt(2));
            }
            this.spawnedViaMobSpawner = nbttagcompound.getBoolean("Paper.FromMobSpawner");
        } catch (Throwable throwable) {
            final CrashReport crashreport = CrashReport.a(throwable, "Loading entity NBT");
            final CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Entity being loaded");
            this.appendEntityCrashDetails(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    protected boolean aA() {
        return true;
    }

    public void setCurrentChunk(final Chunk chunk) {
        this.currentChunk = ((chunk != null) ? new WeakReference<Chunk>(chunk) : null);
    }

    public Chunk getCurrentChunk() {
        final Chunk chunk = (this.currentChunk != null) ? this.currentChunk.get() : null;
        return (chunk != null && chunk.isLoaded()) ? chunk
                : (this.isAddedToChunk() ? this.world.getChunkIfLoaded(this.getChunkX(), this.getChunkZ()) : null);
    }

    public Chunk getCurrentChunkAt(final int x, final int z) {
        if (this.getChunkX() == x && this.getChunkZ() == z) {
            final Chunk chunk = this.getCurrentChunk();
            if (chunk != null) {
                return chunk;
            }
        }
        return this.world.getChunkIfLoaded(x, z);
    }

    public Chunk getChunkAtLocation() {
        return this.getCurrentChunkAt((int) Math.floor(this.locX) >> 4, (int) Math.floor(this.locZ) >> 4);
    }

    public MinecraftKey getMinecraftKey() {
        if (this.entityKey == null) {
            this.entityKey = EntityTypes.getKey(this);
            this.entityKeyString = ((this.entityKey != null) ? this.entityKey.toString() : null);
        }
        return this.entityKey;
    }

    public String getMinecraftKeyString() {
        this.getMinecraftKey();
        return this.entityKeyString;
    }

    @Nullable
    public final String getSaveID() {
        return this.getMinecraftKeyString();
    }

    protected abstract void a(final NBTTagCompound p0);

    protected abstract void b(final NBTTagCompound p0);

    protected NBTTagList createList(final double... adouble) {
        return this.a(adouble);
    }

    protected NBTTagList a(final double... adouble) {
        final NBTTagList nbttaglist = new NBTTagList();
        for (final double d0 : adouble) {
            nbttaglist.add((NBTBase) new NBTTagDouble(d0));
        }
        return nbttaglist;
    }

    protected NBTTagList a(final float... afloat) {
        final NBTTagList nbttaglist = new NBTTagList();
        for (final float f : afloat) {
            nbttaglist.add((NBTBase) new NBTTagFloat(f));
        }
        return nbttaglist;
    }

    @Nullable
    public EntityItem a(final Item item, final int i) {
        return this.a(item, i, 0.0f);
    }

    @Nullable
    public EntityItem a(final Item item, final int i, final float f) {
        return this.a(new ItemStack(item, i, 0), f);
    }

    @Nullable
    public final EntityItem dropItem(final ItemStack itemstack, final float offset) {
        return this.a(itemstack, offset);
    }

    @Nullable
    public EntityItem a(final ItemStack itemstack, final float f) {
        if (itemstack.isEmpty()) {
            return null;
        }
        if (this instanceof EntityLiving && !((EntityLiving) this).forceDrops) {
            ((EntityLiving) this).drops.add(CraftItemStack.asBukkitCopy(itemstack));
            return null;
        }
        final EntityItem entityitem = new EntityItem(this.world, this.locX, this.locY + f, this.locZ, itemstack);
        entityitem.q();
        this.world.addEntity((Entity) entityitem);
        return entityitem;
    }

    public boolean isAlive() {
        return !this.dead;
    }

    public boolean inBlock() {
        if (this.noclip) {
            return false;
        }
        final BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                .s();
        for (int i = 0; i < 8; ++i) {
            final int j = MathHelper.floor(this.locY + ((i >> 0) % 2 - 0.5f) * 0.1f + this.getHeadHeight());
            final int k = MathHelper.floor(this.locX + ((i >> 1) % 2 - 0.5f) * this.width * 0.8f);
            final int l = MathHelper.floor(this.locZ + ((i >> 2) % 2 - 0.5f) * this.width * 0.8f);
            if (blockposition_pooledblockposition.getX() != k || blockposition_pooledblockposition.getY() != j
                    || blockposition_pooledblockposition.getZ() != l) {
                blockposition_pooledblockposition.f(k, j, l);
                if (this.world.getType((BlockPosition) blockposition_pooledblockposition).r()) {
                    blockposition_pooledblockposition.t();
                    return true;
                }
            }
        }
        blockposition_pooledblockposition.t();
        return false;
    }

    public boolean b(final EntityHuman entityhuman, final EnumHand enumhand) {
        return false;
    }

    @Nullable
    public AxisAlignedBB j(final Entity entity) {
        return null;
    }

    public void aE() {
        final Entity entity = this.bJ();
        if (this.isPassenger() && entity.dead) {
            this.stopRiding();
        } else {
            this.motX = 0.0;
            this.motY = 0.0;
            this.motZ = 0.0;
            this.B_();
            if (this.isPassenger()) {
                entity.k(this);
            }
        }
    }

    public void k(final Entity entity) {
        if (this.w(entity)) {
            entity.setPosition(this.locX, this.locY + this.aG() + entity.aF(), this.locZ);
        }
    }

    public double aF() {
        return 0.0;
    }

    public double aG() {
        return this.length * 0.75;
    }

    public boolean startRiding(final Entity entity) {
        return this.a(entity, false);
    }

    public boolean a(final Entity entity, final boolean flag) {
        for (Entity entity2 = entity; entity2.au != null; entity2 = entity2.au) {
            if (entity2.au == this) {
                return false;
            }
        }
        if (!flag && (!this.n(entity) || !entity.q(this))) {
            return false;
        }
        if (this.isPassenger()) {
            this.stopRiding();
        }
        (this.au = entity).o(this);
        return true;
    }

    protected boolean n(final Entity entity) {
        return this.j <= 0;
    }

    public void ejectPassengers() {
        for (int i = this.passengers.size() - 1; i >= 0; --i) {
            this.passengers.get(i).stopRiding();
        }
    }

    public void stopRiding() {
        if (this.au != null) {
            final Entity entity = this.au;
            this.au = null;
            entity.p(this);
        }
    }

    protected void o(final Entity entity) {
        if (entity == this) {
            throw new IllegalArgumentException("Entities cannot become a passenger of themselves");
        }
        if (entity.bJ() != this) {
            throw new IllegalStateException("Use x.startRiding(y), not y.addPassenger(x)");
        }
        Preconditions.checkState(!entity.passengers.contains(this), "Circular entity riding! %s %s", (Object) this,
                (Object) entity);
        final CraftEntity craft = (CraftEntity) entity.getBukkitEntity().getVehicle();
        final Entity orig = (craft == null) ? null : craft.getHandle();
        if (this.getBukkitEntity() instanceof Vehicle && entity.getBukkitEntity() instanceof LivingEntity
                && entity.world.isChunkLoaded((int) entity.locX >> 4, (int) entity.locZ >> 4, false)) {
            final VehicleEnterEvent event = new VehicleEnterEvent((Vehicle) this.getBukkitEntity(),
                    (org.bukkit.entity.Entity) entity.getBukkitEntity());
            Bukkit.getPluginManager().callEvent((Event) event);
            final CraftEntity craftn = (CraftEntity) entity.getBukkitEntity().getVehicle();
            final Entity n = (craftn == null) ? null : craftn.getHandle();
            if (event.isCancelled() || n != orig) {
                return;
            }
        }
        final EntityMountEvent event2 = new EntityMountEvent((org.bukkit.entity.Entity) entity.getBukkitEntity(),
                (org.bukkit.entity.Entity) this.getBukkitEntity());
        Bukkit.getPluginManager().callEvent((Event) event2);
        if (event2.isCancelled()) {
            return;
        }
        if (!this.world.isClientSide && entity instanceof EntityHuman && !(this.bE() instanceof EntityHuman)) {
            this.passengers.add(0, entity);
        } else {
            this.passengers.add(entity);
        }
    }

    protected void p(final Entity entity) {
        if (entity.bJ() == this) {
            throw new IllegalStateException("Use x.stopRiding(y), not y.removePassenger(x)");
        }
        entity.setVehicle(this);
        final CraftEntity craft = (CraftEntity) entity.getBukkitEntity().getVehicle();
        final Entity orig = (craft == null) ? null : craft.getHandle();
        if (this.getBukkitEntity() instanceof Vehicle && entity.getBukkitEntity() instanceof LivingEntity) {
            final VehicleExitEvent event = new VehicleExitEvent((Vehicle) this.getBukkitEntity(),
                    (LivingEntity) entity.getBukkitEntity());
            Bukkit.getPluginManager().callEvent((Event) event);
            final CraftEntity craftn = (CraftEntity) entity.getBukkitEntity().getVehicle();
            final Entity n = (craftn == null) ? null : craftn.getHandle();
            if (event.isCancelled() || n != orig) {
                return;
            }
        }
        if (!new EntityDismountEvent((org.bukkit.entity.Entity) entity.getBukkitEntity(),
                (org.bukkit.entity.Entity) this.getBukkitEntity()).callEvent()) {
            return;
        }
        entity.setVehicle(null);
        this.passengers.remove(entity);
        entity.j = 60;
    }

    protected boolean q(final Entity entity) {
        return this.bF().size() < 1;
    }

    public float aI() {
        return 0.0f;
    }

    public Vec3D aJ() {
        return this.f(this.pitch, this.yaw);
    }

    public void e(final BlockPosition blockposition) {
        if (this.portalCooldown > 0) {
            this.portalCooldown = this.aM();
        } else {
            if (!this.world.isClientSide && !blockposition.equals((Object) this.an)) {
                this.an = new BlockPosition((BaseBlockPosition) blockposition);
                final ShapeDetector.ShapeDetectorCollection shapedetector_shapedetectorcollection = Blocks.PORTAL
                        .c(this.world, this.an);
                final double d0 = (shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X)
                        ? shapedetector_shapedetectorcollection.a().getZ()
                        : ((double) shapedetector_shapedetectorcollection.a().getX());
                double d2 = (shapedetector_shapedetectorcollection.getFacing().k() == EnumDirection.EnumAxis.X)
                        ? this.locZ
                        : this.locX;
                d2 = Math.abs(MathHelper.c(
                        d2 - (double) ((shapedetector_shapedetectorcollection.getFacing().e()
                                .c() == EnumDirection.EnumAxisDirection.NEGATIVE) ? 1 : 0),
                        d0, d0 - shapedetector_shapedetectorcollection.d()));
                final double d3 = MathHelper.c(this.locY - 1.0,
                        (double) shapedetector_shapedetectorcollection.a().getY(),
                        (double) (shapedetector_shapedetectorcollection.a().getY()
                                - shapedetector_shapedetectorcollection.e()));
                this.ao = new Vec3D(d2, d3, 0.0);
                this.ap = shapedetector_shapedetectorcollection.getFacing();
            }
            this.ak = true;
        }
    }

    public int aM() {
        return 300;
    }

    public Iterable<ItemStack> aO() {
        return Entity.b;
    }

    public Iterable<ItemStack> getArmorItems() {
        return Entity.b;
    }

    public Iterable<ItemStack> aQ() {
        return (Iterable<ItemStack>) Iterables.concat((Iterable) this.aO(), (Iterable) this.getArmorItems());
    }

    public void setEquipment(final EnumItemSlot enumitemslot, final ItemStack itemstack) {
    }

    public boolean isBurning() {
        final boolean flag = this.world != null && this.world.isClientSide;
        return !this.fireProof && (this.fireTicks > 0 || (flag && this.getFlag(0)));
    }

    public boolean isPassenger() {
        return this.bJ() != null;
    }

    public boolean isVehicle() {
        return !this.bF().isEmpty();
    }

    public boolean isSneaking() {
        return this.getFlag(1);
    }

    public void setSneaking(final boolean flag) {
        this.setFlag(1, flag);
    }

    public boolean isSprinting() {
        return this.getFlag(3);
    }

    public void setSprinting(final boolean flag) {
        this.setFlag(3, flag);
    }

    public boolean aW() {
        return this.glowing || (this.world.isClientSide && this.getFlag(6));
    }

    public void g(final boolean flag) {
        this.glowing = flag;
        if (!this.world.isClientSide) {
            this.setFlag(6, this.glowing);
        }
    }

    public boolean isInvisible() {
        return this.getFlag(5);
    }

    @Nullable
    public ScoreboardTeamBase getTeam() {
        return this.aY();
    }

    @Nullable
    public ScoreboardTeamBase aY() {
        if (!this.world.paperConfig.nonPlayerEntitiesOnScoreboards && !(this instanceof EntityHuman)) {
            return null;
        }
        return (ScoreboardTeamBase) this.world.getScoreboard().getPlayerTeam(this.bn());
    }

    public boolean r(final Entity entity) {
        return this.a(entity.aY());
    }

    public boolean a(final ScoreboardTeamBase scoreboardteambase) {
        return this.aY() != null && this.aY().isAlly(scoreboardteambase);
    }

    public void setInvisible(final boolean flag) {
        this.setFlag(5, flag);
    }

    public boolean getFlag(final int i) {
        return ((byte) this.datawatcher.get((DataWatcherObject) Entity.Z) & 1 << i) != 0x0;
    }

    public void setFlag(final int i, final boolean flag) {
        final byte b0 = (byte) this.datawatcher.get((DataWatcherObject) Entity.Z);
        if (flag) {
            this.datawatcher.set((DataWatcherObject) Entity.Z, (Object) (byte) (b0 | 1 << i));
        } else {
            this.datawatcher.set((DataWatcherObject) Entity.Z, (Object) (byte) (b0 & ~(1 << i)));
        }
    }

    public int getAirTicks() {
        return (int) this.datawatcher.get((DataWatcherObject) Entity.aA);
    }

    public void setAirTicks(final int i) {
        final EntityAirChangeEvent event = new EntityAirChangeEvent((org.bukkit.entity.Entity) this.getBukkitEntity(),
                i);
        event.getEntity().getServer().getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) {
            return;
        }
        this.datawatcher.set((DataWatcherObject) Entity.aA, (Object) event.getAmount());
    }

    public void onLightningStrike(final EntityLightning entitylightning) {
        final org.bukkit.entity.Entity thisBukkitEntity = (org.bukkit.entity.Entity) this.getBukkitEntity();
        final org.bukkit.entity.Entity stormBukkitEntity = (org.bukkit.entity.Entity) entitylightning.getBukkitEntity();
        final PluginManager pluginManager = Bukkit.getPluginManager();
        if (thisBukkitEntity instanceof Hanging) {
            final HangingBreakByEntityEvent hangingEvent = new HangingBreakByEntityEvent((Hanging) thisBukkitEntity,
                    stormBukkitEntity);
            pluginManager.callEvent((Event) hangingEvent);
            if (hangingEvent.isCancelled()) {
                return;
            }
        }
        if (this.fireProof) {
            return;
        }
        CraftEventFactory.entityDamage = (Entity) entitylightning;
        if (!this.damageEntity(DamageSource.LIGHTNING, 5.0f)) {
            CraftEventFactory.entityDamage = null;
            return;
        }
        ++this.fireTicks;
        if (this.fireTicks == 0) {
            final EntityCombustByEntityEvent entityCombustEvent = new EntityCombustByEntityEvent(stormBukkitEntity,
                    thisBukkitEntity, 8);
            pluginManager.callEvent((Event) entityCombustEvent);
            if (!entityCombustEvent.isCancelled()) {
                this.setOnFire(entityCombustEvent.getDuration());
            }
        }
    }

    public void onKill(final EntityLiving entityLiving) {
        this.b(entityLiving);
    }

    public void b(final EntityLiving entityliving) {
    }

    protected boolean i(final double d0, final double d1, final double d2) {
        final BlockPosition blockposition = new BlockPosition(d0, d1, d2);
        final double d3 = d0 - blockposition.getX();
        final double d4 = d1 - blockposition.getY();
        final double d5 = d2 - blockposition.getZ();
        if (!this.world.a(this.getBoundingBox())) {
            return false;
        }
        EnumDirection enumdirection = EnumDirection.UP;
        double d6 = Double.MAX_VALUE;
        if (!this.world.t(blockposition.west()) && d3 < d6) {
            d6 = d3;
            enumdirection = EnumDirection.WEST;
        }
        if (!this.world.t(blockposition.east()) && 1.0 - d3 < d6) {
            d6 = 1.0 - d3;
            enumdirection = EnumDirection.EAST;
        }
        if (!this.world.t(blockposition.north()) && d5 < d6) {
            d6 = d5;
            enumdirection = EnumDirection.NORTH;
        }
        if (!this.world.t(blockposition.south()) && 1.0 - d5 < d6) {
            d6 = 1.0 - d5;
            enumdirection = EnumDirection.SOUTH;
        }
        if (!this.world.t(blockposition.up()) && 1.0 - d4 < d6) {
            d6 = 1.0 - d4;
            enumdirection = EnumDirection.UP;
        }
        final float f = this.random.nextFloat() * 0.2f + 0.1f;
        final float f2 = enumdirection.c().a();
        if (enumdirection.k() == EnumDirection.EnumAxis.X) {
            this.motX = f2 * f;
            this.motY *= 0.75;
            this.motZ *= 0.75;
        } else if (enumdirection.k() == EnumDirection.EnumAxis.Y) {
            this.motX *= 0.75;
            this.motY = f2 * f;
            this.motZ *= 0.75;
        } else if (enumdirection.k() == EnumDirection.EnumAxis.Z) {
            this.motX *= 0.75;
            this.motY *= 0.75;
            this.motZ = f2 * f;
        }
        return true;
    }

    public void ba() {
        this.E = true;
        this.fallDistance = 0.0f;
    }

    public String getName() {
        if (this.hasCustomName()) {
            return this.getCustomName();
        }
        String s = EntityTypes.b(this);
        if (s == null) {
            s = "generic";
        }
        return LocaleI18n.get("entity." + s + ".name");
    }

    @Nullable
    public Entity[] bb() {
        return null;
    }

    public boolean s(final Entity entity) {
        return this == entity;
    }

    public float getHeadRotation() {
        return 0.0f;
    }

    public void setHeadRotation(final float f) {
    }

    public void h(final float f) {
    }

    public boolean bd() {
        return true;
    }

    public boolean t(final Entity entity) {
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s['%s'/%d, uuid='%s', l='%s', x=%.2f, y=%.2f, z=%.2f, cx=%d, cd=%d, tl=%d, v=%b, d=%b]",
                this.getClass().getSimpleName(), this.getName(), this.id, this.uniqueID.toString(),
                (this.world == null) ? "~NULL~" : this.world.getWorldData().getName(), this.locX, this.locY, this.locZ,
                this.getChunkX(), this.getChunkZ(), this.ticksLived, this.valid, this.dead);
    }

    public boolean isInvulnerable(final DamageSource damagesource) {
        return this.invulnerable && damagesource != DamageSource.OUT_OF_WORLD && !damagesource.u();
    }

    public boolean be() {
        return this.invulnerable;
    }

    public void setInvulnerable(final boolean flag) {
        this.invulnerable = flag;
    }

    public void u(final Entity entity) {
        this.setPositionRotation(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
    }

    private void a(final Entity entity) {
        final NBTTagCompound nbttagcompound = entity.save(new NBTTagCompound());
        nbttagcompound.remove("Dimension");
        this.f(nbttagcompound);
        this.portalCooldown = entity.portalCooldown;
        this.an = entity.an;
        this.ao = entity.ao;
        this.ap = entity.ap;
    }

    @Nullable
    public Entity b(final int i) {
        if (this.world.isClientSide || this.dead) {
            return null;
        }
        this.world.methodProfiler.a("changeDimension");
        final MinecraftServer minecraftserver = this.C_();
        WorldServer exitWorld = null;
        if (this.dimension < 10) {
            for (final WorldServer world : minecraftserver.worlds) {
                if (world.dimension == i) {
                    exitWorld = world;
                }
            }
        }
        final BlockPosition blockposition = null;
        final Location enter = this.getBukkitEntity().getLocation();
        Location exit;
        if (exitWorld != null) {
            if (blockposition != null) {
                exit = new Location((org.bukkit.World) exitWorld.getWorld(), (double) blockposition.getX(),
                        (double) blockposition.getY(), (double) blockposition.getZ());
            } else {
                exit = minecraftserver.getPlayerList().calculateTarget(enter,
                        (World) minecraftserver.getWorldServer(i));
            }
        } else {
            exit = null;
        }
        final boolean useTravelAgent = exitWorld != null && (this.dimension != 1 || exitWorld.dimension != 1);
        final TravelAgent agent = (TravelAgent) ((exit != null)
                ? ((CraftWorld) exit.getWorld()).getHandle().getTravelAgent()
                : CraftTravelAgent.DEFAULT);
        final boolean oldCanCreate = agent.getCanCreatePortal();
        agent.setCanCreatePortal(false);
        final EntityPortalEvent event = new EntityPortalEvent((org.bukkit.entity.Entity) this.getBukkitEntity(), enter,
                exit, agent);
        event.useTravelAgent(useTravelAgent);
        event.getEntity().getServer().getPluginManager().callEvent((Event) event);
        if (event.isCancelled() || event.getTo() == null || event.getTo().getWorld() == null || !this.isAlive()) {
            agent.setCanCreatePortal(oldCanCreate);
            return null;
        }
        exit = (event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(event.getTo()) : event.getTo());
        agent.setCanCreatePortal(oldCanCreate);
        final Entity entity = this.teleportTo(exit, true);
        this.world.methodProfiler.b();
        return entity;
    }

    public Entity teleportTo(final Location exit, final boolean portal) {
        if (!this.dead) {
            final WorldServer worldserver = ((CraftWorld) this.getBukkitEntity().getLocation().getWorld()).getHandle();
            final WorldServer worldserver2 = ((CraftWorld) exit.getWorld()).getHandle();
            final int i = worldserver2.dimension;
            this.dimension = i;
            this.world.removeEntity(this);
            this.dead = false;
            this.world.methodProfiler.a("reposition");
            worldserver2.getMinecraftServer().getPlayerList().repositionEntity(this, exit, portal);
            this.world.methodProfiler.c("reloading");
            final Entity entity = EntityTypes.a((Class) this.getClass(), (World) worldserver2);
            if (entity != null) {
                entity.a(this);
                final boolean flag = entity.attachedToPlayer;
                entity.attachedToPlayer = true;
                worldserver2.addEntity(entity);
                entity.attachedToPlayer = flag;
                worldserver2.entityJoinedWorld(entity, false);
                this.getBukkitEntity().setHandle(entity);
                entity.bukkitEntity = this.getBukkitEntity();
                if (this instanceof EntityInsentient) {
                    ((EntityInsentient) this).unleash(true, false);
                }
            }
            this.dead = true;
            this.world.methodProfiler.b();
            worldserver.m();
            worldserver2.m();
            return entity;
        }
        return null;
    }

    public boolean bf() {
        return true;
    }

    public float a(final Explosion explosion, final World world, final BlockPosition blockposition,
                   final IBlockData iblockdata) {
        return iblockdata.getBlock().a(this);
    }

    public boolean a(final Explosion explosion, final World world, final BlockPosition blockposition,
                     final IBlockData iblockdata, final float f) {
        return true;
    }

    public int bg() {
        return 3;
    }

    public Vec3D getPortalOffset() {
        return this.ao;
    }

    public EnumDirection getPortalDirection() {
        return this.ap;
    }

    public boolean isIgnoreBlockTrigger() {
        return false;
    }

    public void appendEntityCrashDetails(final CrashReportSystemDetails crashreportsystemdetails) {
        crashreportsystemdetails.a("Entity Type", (CrashReportCallable) new CrashReportCallable() {
            public String a() throws Exception {
                return EntityTypes.a(Entity.this) + " (" + Entity.this.getClass().getCanonicalName() + ")";
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Entity ID", (Object) this.id);
        crashreportsystemdetails.a("Entity Name", (CrashReportCallable) new CrashReportCallable() {
            public String a() throws Exception {
                return Entity.this.getName();
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Entity's Exact location",
                (Object) String.format("%.2f, %.2f, %.2f", this.locX, this.locY, this.locZ));
        crashreportsystemdetails.a("Entity's Block location", (Object) CrashReportSystemDetails
                .a(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ)));
        crashreportsystemdetails.a("Entity's Momentum",
                (Object) String.format("%.2f, %.2f, %.2f", this.motX, this.motY, this.motZ));
        crashreportsystemdetails.a("Entity's Passengers", (CrashReportCallable) new CrashReportCallable() {
            public String a() throws Exception {
                return Entity.this.bF().toString();
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Entity's Vehicle", (CrashReportCallable) new CrashReportCallable() {
            public String a() throws Exception {
                return Entity.this.bJ().toString();
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
    }

    public void setUUID(final UUID uuid) {
        this.a(uuid);
    }

    public void a(final UUID uuid) {
        this.uniqueID = uuid;
        this.ar = this.uniqueID.toString();
    }

    public UUID getUniqueID() {
        return this.uniqueID;
    }

    public String bn() {
        return this.ar;
    }

    public boolean bo() {
        return this.pushedByWater();
    }

    public boolean pushedByWater() {
        return true;
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        final ChatComponentText chatcomponenttext = new ChatComponentText(
                ScoreboardTeam.getPlayerDisplayName(this.aY(), this.getName()));
        chatcomponenttext.getChatModifier().setChatHoverable(this.bv());
        chatcomponenttext.getChatModifier().setInsertion(this.bn());
        return (IChatBaseComponent) chatcomponenttext;
    }

    public void setCustomName(String s) {
        if (s.length() > 256) {
            s = s.substring(0, 256);
        }
        this.datawatcher.set((DataWatcherObject) Entity.aB, (Object) s);
    }

    public String getCustomName() {
        return (String) this.datawatcher.get((DataWatcherObject) Entity.aB);
    }

    public boolean hasCustomName() {
        return !((String) this.datawatcher.get((DataWatcherObject) Entity.aB)).isEmpty();
    }

    public void setCustomNameVisible(final boolean flag) {
        this.datawatcher.set((DataWatcherObject) Entity.aC, (Object) flag);
    }

    public boolean getCustomNameVisible() {
        return (boolean) this.datawatcher.get((DataWatcherObject) Entity.aC);
    }

    public void enderTeleportTo(final double d0, final double d1, final double d2) {
        this.aI = true;
        this.setPositionRotation(d0, d1, d2, this.yaw, this.pitch);
        this.world.entityJoinedWorld(this, false);
    }

    public void a(final DataWatcherObject<?> datawatcherobject) {
    }

    public EnumDirection getDirection() {
        return EnumDirection.fromType2(MathHelper.floor(this.yaw * 4.0f / 360.0f + 0.5) & 0x3);
    }

    public EnumDirection bu() {
        return this.getDirection();
    }

    protected ChatHoverable bv() {
        final NBTTagCompound nbttagcompound = new NBTTagCompound();
        final MinecraftKey minecraftkey = EntityTypes.a(this);
        nbttagcompound.setString("id", this.bn());
        if (minecraftkey != null) {
            nbttagcompound.setString("type", minecraftkey.toString());
        }
        nbttagcompound.setString("name", this.getName());
        return new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ENTITY,
                (IChatBaseComponent) new ChatComponentText(nbttagcompound.toString()));
    }

    public boolean a(final EntityPlayer entityplayer) {
        return true;
    }

    public AxisAlignedBB getBoundingBox() {
        return this.boundingBox;
    }

    public void a(final AxisAlignedBB axisalignedbb) {
        final double a = axisalignedbb.a;
        final double b = axisalignedbb.b;
        final double c = axisalignedbb.c;
        double d = axisalignedbb.d;
        double e = axisalignedbb.e;
        double f = axisalignedbb.f;
        double len = axisalignedbb.d - axisalignedbb.a;
        if (len < 0.0) {
            d = a;
        }
        if (len > 64.0) {
            d = a + 64.0;
        }
        len = axisalignedbb.e - axisalignedbb.b;
        if (len < 0.0) {
            e = b;
        }
        if (len > 64.0) {
            e = b + 64.0;
        }
        len = axisalignedbb.f - axisalignedbb.c;
        if (len < 0.0) {
            f = c;
        }
        if (len > 64.0) {
            f = c + 64.0;
        }
        this.boundingBox = new AxisAlignedBB(a, b, c, d, e, f);
    }

    public float getHeadHeight() {
        return this.length * 0.85f;
    }

    public boolean bz() {
        return this.aw;
    }

    public void k(final boolean flag) {
        this.aw = flag;
    }

    public boolean c(final int i, final ItemStack itemstack) {
        return false;
    }

    public void sendMessage(final IChatBaseComponent ichatbasecomponent) {
    }

    public boolean a(final int i, final String s) {
        return true;
    }

    public BlockPosition getChunkCoordinates() {
        return new BlockPosition(this.locX, this.locY + 0.5, this.locZ);
    }

    public Vec3D d() {
        return new Vec3D(this.locX, this.locY, this.locZ);
    }

    public World getWorld() {
        return this.world;
    }

    public Entity f() {
        return this;
    }

    public boolean getSendCommandFeedback() {
        return false;
    }

    public void a(final CommandObjectiveExecutor.EnumCommandResult commandobjectiveexecutor_enumcommandresult,
                  final int i) {
        if (this.world != null && !this.world.isClientSide) {
            this.aG.a(this.world.getMinecraftServer(), (ICommandListener) this,
                    commandobjectiveexecutor_enumcommandresult, i);
        }
    }

    @Nullable
    public MinecraftServer C_() {
        return this.world.getMinecraftServer();
    }

    public CommandObjectiveExecutor bA() {
        return this.aG;
    }

    public void v(final Entity entity) {
        this.aG.a(entity.bA());
    }

    public EnumInteractionResult a(final EntityHuman entityhuman, final Vec3D vec3d, final EnumHand enumhand) {
        return EnumInteractionResult.PASS;
    }

    public boolean bB() {
        return false;
    }

    protected void a(final EntityLiving entityliving, final Entity entity) {
        if (entity instanceof EntityLiving) {
            EnchantmentManager.a((EntityLiving) entity, (Entity) entityliving);
        }
        EnchantmentManager.b(entityliving, entity);
    }

    public void b(final EntityPlayer entityplayer) {
    }

    public void c(final EntityPlayer entityplayer) {
    }

    public float a(final EnumBlockRotation enumblockrotation) {
        final float f = MathHelper.g(this.yaw);
        switch (enumblockrotation) {
            case CLOCKWISE_180: {
                return f + 180.0f;
            }
            case COUNTERCLOCKWISE_90: {
                return f + 270.0f;
            }
            case CLOCKWISE_90: {
                return f + 90.0f;
            }
            default: {
                return f;
            }
        }
    }

    public float a(final EnumBlockMirror enumblockmirror) {
        final float f = MathHelper.g(this.yaw);
        switch (enumblockmirror) {
            case LEFT_RIGHT: {
                return -f;
            }
            case FRONT_BACK: {
                return 180.0f - f;
            }
            default: {
                return f;
            }
        }
    }

    public boolean bC() {
        return false;
    }

    public boolean bD() {
        final boolean flag = this.aI;
        this.aI = false;
        return flag;
    }

    @Nullable
    public Entity bE() {
        return null;
    }

    public List<Entity> bF() {
        return this.passengers.isEmpty() ? Collections.emptyList() : Lists.newArrayList((Iterable) this.passengers);
    }

    public boolean w(final Entity entity) {
        for (final Entity entity2 : this.bF()) {
            if (entity2.equals(entity)) {
                return true;
            }
        }
        return false;
    }

    public Collection<Entity> bG() {
        final HashSet hashset = Sets.newHashSet();
        this.a(Entity.class, hashset);
        return (Collection<Entity>) hashset;
    }

    public <T extends Entity> Collection<T> b(final Class<T> oclass) {
        final HashSet hashset = Sets.newHashSet();
        this.a(oclass, hashset);
        return (Collection<T>) hashset;
    }

    private <T extends Entity> void a(final Class<T> oclass, final Set<T> set) {
        for (final Entity entity : this.bF()) {
            if (oclass.isAssignableFrom(entity.getClass())) {
                set.add((T) entity);
            }
            entity.a((Class<Entity>) oclass, (Set<Entity>) set);
        }
    }

    public Entity getVehicle() {
        Entity entity;
        for (entity = this; entity.isPassenger(); entity = entity.bJ()) {
        }
        return entity;
    }

    public boolean x(final Entity entity) {
        return this.getVehicle() == entity.getVehicle();
    }

    public boolean y(final Entity entity) {
        for (final Entity entity2 : this.bF()) {
            if (entity2.equals(entity)) {
                return true;
            }
            if (entity2.y(entity)) {
                return true;
            }
        }
        return false;
    }

    public boolean bI() {
        final Entity entity = this.bE();
        return (entity instanceof EntityHuman) ? ((EntityHuman) entity).cZ() : (!this.world.isClientSide);
    }

    @Nullable
    Entity getVehicleDirect() {
        return this.bJ();
    }

    @Nullable
    public Entity bJ() {
        return this.au;
    }

    public EnumPistonReaction getPushReaction() {
        return EnumPistonReaction.NORMAL;
    }

    public SoundCategory getDeathSoundCategory() {
        return this.bK();
    }

    public SoundCategory bK() {
        return SoundCategory.NEUTRAL;
    }

    public int getMaxFireTicks() {
        return 1;
    }

    // ~
    public EntityTrackerEntry getTracker() {
        return tracker;
    }

//	// ticklocker
//	private Lock tickLocker = new ReentrantLock();
//
//	public Lock getTickLocker() {
//		return tickLocker;
//	}

    static {
        Entity.SHARED_RANDOM = new Random() {
            private boolean locked = false;

            @Override
            public synchronized void setSeed(final long seed) {
                if (this.locked) {
                    LogManager.getLogger().error("Ignoring setSeed on Entity.SHARED_RANDOM", new Throwable());
                } else {
                    super.setSeed(seed);
                    this.locked = true;
                }
            }
        };
        a = LogManager.getLogger();
        b = Collections.emptyList();
        c = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        Entity.f = 1.0;
        Entity.entityCount = 1;
        Z = DataWatcher.a((Class) Entity.class, DataWatcherRegistry.a);
        aA = DataWatcher.a((Class) Entity.class, DataWatcherRegistry.b);
        aB = DataWatcher.a((Class) Entity.class, DataWatcherRegistry.d);
        aC = DataWatcher.a((Class) Entity.class, DataWatcherRegistry.h);
        aD = DataWatcher.a((Class) Entity.class, DataWatcherRegistry.h);
        aE = DataWatcher.a((Class) Entity.class, DataWatcherRegistry.h);
    }
}

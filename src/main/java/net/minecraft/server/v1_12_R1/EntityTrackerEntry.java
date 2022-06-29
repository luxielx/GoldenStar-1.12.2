package net.minecraft.server.v1_12_R1;

import com.google.common.collect.ImmutableSet;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EntityTrackerEntry {

    // ~
    // concurrent hashmap trackedplayermap
    // remove async catcher

    // private static Logger c;
    private Entity tracker;
    private int e;
    // ~
    private volatile int f;
    private int g;
    private long xLoc;
    private long yLoc;
    private long zLoc;
    private int yRot;
    private int xRot;
    private int headYaw;
    private double n;
    private double o;
    private double p;
    public int a;
    private double q;
    private double r;
    private double s;
    private boolean isMoving;
    private boolean u;
    private int v;
    private List<Entity> w;
    private boolean x;
    private boolean y;
    public boolean b;
    public Map<EntityPlayer, Boolean> trackedPlayerMap;
    public Set<EntityPlayer> trackedPlayers;

    public static Map<String, List<String>> rendered = Collections.synchronizedMap(new HashMap<String, List<String>>());

    public EntityTrackerEntry(Entity entity, int i, int j, int k, boolean flag) {
        this.w = Collections.emptyList();
        // ~
        this.trackedPlayerMap = new HashMap<EntityPlayer, Boolean>();
        this.trackedPlayers = this.trackedPlayerMap.keySet();
        entity.tracker = this;
        this.tracker = entity;
        this.e = i;
        this.f = j;
        this.g = k;
        this.u = flag;
        this.xLoc = EntityTracker.a(entity.locX);
        this.yLoc = EntityTracker.a(entity.locY);
        this.zLoc = EntityTracker.a(entity.locZ);
        this.yRot = MathHelper.d(entity.yaw * 256.0f / 360.0f);
        this.xRot = MathHelper.d(entity.pitch * 256.0f / 360.0f);
        this.headYaw = MathHelper.d(entity.getHeadRotation() * 256.0f / 360.0f);
        this.y = entity.onGround;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof EntityTrackerEntry
                && ((EntityTrackerEntry) object).tracker.getId() == this.tracker.getId();
    }

    @Override
    public int hashCode() {
        return this.tracker.getId();
    }

    public synchronized void track(List<EntityHuman> list) {
        this.b = false;
        if (!this.isMoving || this.tracker.d(this.q, this.r, this.s) > 16.0) {
            this.q = this.tracker.locX;
            this.r = this.tracker.locY;
            this.s = this.tracker.locZ;
            this.isMoving = true;
            this.b = true;
            this.scanPlayers(list);
        }
        List<Entity> list2 = this.tracker.bF();
        if (!list2.equals(this.w)) {
            this.w = (List<Entity>) list2;
            this.broadcastIncludingSelf((Packet<?>) new PacketPlayOutMount(this.tracker));
        }
        if (this.tracker instanceof EntityItemFrame && this.a % 20 == 0) {
            EntityItemFrame entityitemframe = (EntityItemFrame) this.tracker;
            ItemStack itemstack = entityitemframe.getItem();
            if (itemstack != null && itemstack.getItem() instanceof ItemWorldMap) {
                WorldMap worldmap = Items.FILLED_MAP.getSavedMap(itemstack, this.tracker.world);
                for (EntityHuman entityhuman : this.trackedPlayers) {
                    // ~
                    EntityPlayer entityplayer = (EntityPlayer) entityhuman;
                    String name = entityplayer.getName();
                    if (!rendered.containsKey(name))
                        rendered.put(name, Collections.synchronizedList(new ArrayList<String>()));
                    List<String> set = rendered.get(name);
                    synchronized (worldmap) {
                        if (!set.contains(worldmap.id) || worldmap.id.equals("map_19")) {
                            worldmap.a(entityplayer, itemstack);
                            Packet<?> packet = Items.FILLED_MAP.a(itemstack, this.tracker.world, entityplayer);
                            if (packet != null) {
                                entityplayer.playerConnection.sendPacket(packet);
                                set.add(worldmap.id);
                            }
                        }
                    }
                }
            }
            this.d();
        }
        if (this.a % this.g == 0 || this.tracker.impulse || this.tracker.getDataWatcher().a()) {
            if (this.tracker.isPassenger()) {
                int i = MathHelper.d(this.tracker.yaw * 256.0f / 360.0f);
                int j = MathHelper.d(this.tracker.pitch * 256.0f / 360.0f);
                boolean flag = Math.abs(i - this.yRot) >= 1 || Math.abs(j - this.xRot) >= 1;
                if (flag) {
                    this.broadcast((Packet<?>) new PacketPlayOutEntity.PacketPlayOutEntityLook(this.tracker.getId(),
                            (byte) i, (byte) j, this.tracker.onGround));
                    this.yRot = i;
                    this.xRot = j;
                }
                this.xLoc = EntityTracker.a(this.tracker.locX);
                this.yLoc = EntityTracker.a(this.tracker.locY);
                this.zLoc = EntityTracker.a(this.tracker.locZ);
                this.d();
                this.x = true;
            } else {
                ++this.v;
                long k = EntityTracker.a(this.tracker.locX);
                long l = EntityTracker.a(this.tracker.locY);
                long i2 = EntityTracker.a(this.tracker.locZ);
                int j2 = MathHelper.d(this.tracker.yaw * 256.0f / 360.0f);
                int k2 = MathHelper.d(this.tracker.pitch * 256.0f / 360.0f);
                long l2 = k - this.xLoc;
                long i3 = l - this.yLoc;
                long j3 = i2 - this.zLoc;
                Object object = null;
                boolean flag2 = l2 * l2 + i3 * i3 + j3 * j3 >= 128L || this.a % 60 == 0;
                boolean flag3 = Math.abs(j2 - this.yRot) >= 1 || Math.abs(k2 - this.xRot) >= 1;
                if (this.a > 0 || this.tracker instanceof EntityArrow) {
                    if (flag2) {
                        this.xLoc = k;
                        this.yLoc = l;
                        this.zLoc = i2;
                    }
                    if (flag3) {
                        this.yRot = j2;
                        this.xRot = k2;
                    }
                    if (l2 >= -32768L && l2 < 32768L && i3 >= -32768L && i3 < 32768L && j3 >= -32768L && j3 < 32768L
                            && this.v <= 400 && !this.x && this.y == this.tracker.onGround) {
                        if ((!flag2 || !flag3) && !(this.tracker instanceof EntityArrow)) {
                            if (flag2) {
                                object = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(this.tracker.getId(), l2,
                                        i3, j3, this.tracker.onGround);
                            } else if (flag3) {
                                object = new PacketPlayOutEntity.PacketPlayOutEntityLook(this.tracker.getId(),
                                        (byte) j2, (byte) k2, this.tracker.onGround);
                            }
                        } else {
                            object = new PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook(this.tracker.getId(), l2,
                                    i3, j3, (byte) j2, (byte) k2, this.tracker.onGround);
                        }
                    } else {
                        this.y = this.tracker.onGround;
                        this.v = 0;
                        if (this.tracker instanceof EntityPlayer) {
                            this.scanPlayers(new ArrayList<EntityHuman>(this.trackedPlayers));
                        }
                        this.c();
                        object = new PacketPlayOutEntityTeleport(this.tracker);
                    }
                }
                boolean flag4 = this.u;
                if (this.tracker instanceof EntityLiving && ((EntityLiving) this.tracker).cP()) {
                    flag4 = true;
                }
                if (flag4 && this.a > 0) {
                    double d0 = this.tracker.motX - this.n;
                    double d2 = this.tracker.motY - this.o;
                    double d3 = this.tracker.motZ - this.p;
                    // double d4 = 0.02;
                    double d5 = d0 * d0 + d2 * d2 + d3 * d3;
                    if (d5 > 4.0E-4 || (d5 > 0.0 && this.tracker.motX == 0.0 && this.tracker.motY == 0.0
                            && this.tracker.motZ == 0.0)) {
                        this.n = this.tracker.motX;
                        this.o = this.tracker.motY;
                        this.p = this.tracker.motZ;
                        this.broadcast((Packet<?>) new PacketPlayOutEntityVelocity(this.tracker.getId(), this.n, this.o,
                                this.p));
                    }
                }
                if (object != null) {
                    if (object instanceof PacketPlayOutEntityTeleport) {
                        this.broadcast((Packet<?>) object);
                    } else {
                        PacketPlayOutEntityTeleport teleportPacket = null;
                        for (Map.Entry<EntityPlayer, Boolean> viewer : this.trackedPlayerMap.entrySet()) {
                            if (viewer.getValue()) {
                                viewer.setValue(false);
                                if (teleportPacket == null) {
                                    teleportPacket = new PacketPlayOutEntityTeleport(this.tracker);
                                }
                                viewer.getKey().playerConnection.sendPacket((Packet<?>) teleportPacket);
                            } else {
                                viewer.getKey().playerConnection.sendPacket((Packet<?>) object);
                            }
                        }
                    }
                }
                this.d();
                this.x = false;
            }
            int i = MathHelper.d(this.tracker.getHeadRotation() * 256.0f / 360.0f);
            if (Math.abs(i - this.headYaw) >= 1) {
                this.broadcast((Packet<?>) new PacketPlayOutEntityHeadRotation(this.tracker, (byte) i));
                this.headYaw = i;
            }
            this.tracker.impulse = false;
        }
        ++this.a;
    }

    public void updateVelocity() {
        if (this.tracker.velocityChanged) {
            boolean cancelled = false;
            if (this.tracker instanceof EntityPlayer) {
                Player player = (Player) this.tracker.getBukkitEntity();
                Vector velocity = player.getVelocity();
                PlayerVelocityEvent event = new PlayerVelocityEvent(player, velocity.clone());
                this.tracker.world.getServer().getPluginManager().callEvent((Event) event);
                if (event.isCancelled()) {
                    cancelled = true;
                } else if (!velocity.equals((Object) event.getVelocity())) {
                    player.setVelocity(event.getVelocity());
                }
            }
            if (!cancelled) {
                this.broadcastIncludingSelf((Packet<?>) new PacketPlayOutEntityVelocity(this.tracker));
            }
            this.tracker.velocityChanged = false;
        }
    }

    private void d() {
        DataWatcher datawatcher = this.tracker.getDataWatcher();
        if (datawatcher.a()) {
            this.broadcastIncludingSelf(new PacketPlayOutEntityMetadata(this.tracker.getId(), datawatcher, false));
        }
        if (this.tracker instanceof EntityLiving) {
            AttributeMapServer attributemapserver = (AttributeMapServer) ((EntityLiving) this.tracker)
                    .getAttributeMap();
            Set<AttributeInstance> set = attributemapserver.getAttributes();
            if (!set.isEmpty()) {
                if (this.tracker instanceof EntityPlayer) {
                    ((EntityPlayer) this.tracker).getBukkitEntity()
                            .injectScaledMaxHealth((Collection<AttributeInstance>) set, false);
                }
                this.broadcastIncludingSelf((Packet<?>) new PacketPlayOutUpdateAttributes(this.tracker.getId(),
                        (Collection<AttributeInstance>) set));
            }
            set.clear();
        }
    }

    public synchronized void broadcast(Packet<?> packet) {
        for (EntityPlayer entityplayer : this.trackedPlayers) {
            entityplayer.playerConnection.sendPacket((Packet<?>) packet);
        }
    }

    public void broadcastIncludingSelf(Packet<?> packet) {
        this.broadcast(packet);
        if (this.tracker instanceof EntityPlayer) {
            ((EntityPlayer) this.tracker).playerConnection.sendPacket((Packet<?>) packet);
        }
    }

    public synchronized void a() {
        for (EntityPlayer entityplayer : this.trackedPlayers) {
            this.tracker.c(entityplayer);
            entityplayer.c(this.tracker);
        }
    }

    public synchronized void a(EntityPlayer entityplayer) {
        if (this.trackedPlayers.contains(entityplayer)) {
            this.tracker.c(entityplayer);
            entityplayer.c(this.tracker);
            this.trackedPlayers.remove(entityplayer);
        }
    }

    public synchronized void updatePlayer(EntityPlayer entityplayer) {
        // AsyncCatcher.catchOp("player tracker update");
        if (entityplayer != this.tracker) {
            if (this.c(entityplayer)) {
                if (!this.trackedPlayers.contains(entityplayer)
                        && (this.e(entityplayer) || this.tracker.attachedToPlayer)) {
                    if (this.tracker instanceof EntityPlayer) {
                        Player player = (Player) ((EntityPlayer) this.tracker).getBukkitEntity();
                        if (!entityplayer.getBukkitEntity().canSee(player)) {
                            return;
                        }
                    }
                    entityplayer.removeQueue.remove(this.tracker.getId());
                    this.trackedPlayerMap.put(entityplayer, true);
                    Packet<?> packet = this.e();
                    entityplayer.playerConnection.sendPacket(packet);
                    if (!this.tracker.getDataWatcher().d()) {
                        entityplayer.playerConnection
                                .sendPacket((Packet<?>) new PacketPlayOutEntityMetadata(this.tracker.getId(),
                                        this.tracker.getDataWatcher(), true));
                    }
                    boolean flag = this.u;
                    if (this.tracker instanceof EntityLiving) {
                        AttributeMapServer attributemapserver = (AttributeMapServer) ((EntityLiving) this.tracker)
                                .getAttributeMap();
                        Collection<AttributeInstance> collection = attributemapserver.c();
                        if (this.tracker.getId() == entityplayer.getId()) {
                            ((EntityPlayer) this.tracker).getBukkitEntity().injectScaledMaxHealth(collection, false);
                        }
                        if (!collection.isEmpty()) {
                            entityplayer.playerConnection.sendPacket(
                                    (Packet<?>) new PacketPlayOutUpdateAttributes(this.tracker.getId(), collection));
                        }
                        if (((EntityLiving) this.tracker).cP()) {
                            flag = true;
                        }
                    }
                    this.n = this.tracker.motX;
                    this.o = this.tracker.motY;
                    this.p = this.tracker.motZ;
                    if (flag && !(packet instanceof PacketPlayOutSpawnEntityLiving)) {
                        entityplayer.playerConnection
                                .sendPacket((Packet<?>) new PacketPlayOutEntityVelocity(this.tracker.getId(),
                                        this.tracker.motX, this.tracker.motY, this.tracker.motZ));
                    }
                    if (this.tracker instanceof EntityLiving) {
                        for (EnumItemSlot enumitemslot : EnumItemSlot.values()) {
                            ItemStack itemstack = ((EntityLiving) this.tracker).getEquipment(enumitemslot);
                            if (!itemstack.isEmpty()) {
                                entityplayer.playerConnection
                                        .sendPacket((Packet<?>) new PacketPlayOutEntityEquipment(this.tracker.getId(),
                                                enumitemslot, itemstack));
                            }
                        }
                    }
                    if (this.tracker instanceof EntityHuman) {
                        EntityHuman entityhuman = (EntityHuman) this.tracker;
                        if (entityhuman.isSleeping()) {
                            entityplayer.playerConnection.sendPacket(
                                    (Packet<?>) new PacketPlayOutBed(entityhuman, new BlockPosition(this.tracker)));
                        }
                    }
                    this.headYaw = MathHelper.d(this.tracker.getHeadRotation() * 256.0f / 360.0f);
                    this.broadcast((Packet<?>) new PacketPlayOutEntityHeadRotation(this.tracker, (byte) this.headYaw));
                    if (this.tracker instanceof EntityLiving) {
                        EntityLiving entityliving = (EntityLiving) this.tracker;
                        for (MobEffect mobeffect : entityliving.getEffects()) {
                            entityplayer.playerConnection.sendPacket(
                                    (Packet<?>) new PacketPlayOutEntityEffect(this.tracker.getId(), mobeffect));
                        }
                    }
                    if (!this.tracker.bF().isEmpty()) {
                        entityplayer.playerConnection.sendPacket((Packet<?>) new PacketPlayOutMount(this.tracker));
                    }
                    if (this.tracker.isPassenger()) {
                        entityplayer.playerConnection.sendPacket((Packet<?>) new PacketPlayOutMount(this.tracker.bJ()));
                    }
                    this.tracker.b(entityplayer);
                    entityplayer.d(this.tracker);
                    this.updatePassengers(entityplayer);
                }
            } else if (this.trackedPlayers.contains(entityplayer)) {
                this.trackedPlayers.remove(entityplayer);
                this.tracker.c(entityplayer);
                entityplayer.c(this.tracker);
                this.updatePassengers(entityplayer);
            }
        }
    }

    public boolean c(EntityPlayer entityplayer) {
        if (this.tracker.isPassenger()) {
            return isTrackedBy(this.tracker.getVehicle(), entityplayer);
        }
        return hasPassengerInRange(this.tracker, entityplayer) || this.isInRangeOfPlayer(entityplayer);
    }

    private static boolean hasPassengerInRange(Entity entity, EntityPlayer entityplayer) {
        if (!entity.isVehicle()) {
            return false;
        }
        for (Entity passenger : entity.passengers) {
            if (passenger.tracker != null && passenger.tracker.isInRangeOfPlayer(entityplayer)) {
                return true;
            }
            if (passenger.isVehicle() && hasPassengerInRange(passenger, entityplayer)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isTrackedBy(Entity entity, EntityPlayer entityplayer) {
//		synchronized (entity.tracker) {
        try {
            return entity == entityplayer || (entity.tracker != null
                    && ImmutableSet.copyOf(entity.tracker.trackedPlayers).contains(entityplayer));
        } catch (ConcurrentModificationException e) {
            return false;
        }
//		}
    }

    private void updatePassengers(EntityPlayer player) {
        if (this.tracker.isVehicle()) {
            this.tracker.passengers.forEach(e -> {
                if (e.tracker != null) {
                    e.tracker.updatePlayer(player);
                }
                return;
            });
            player.playerConnection.sendPacket((Packet<?>) new PacketPlayOutMount(this.tracker));
        }
    }

    private boolean isInRangeOfPlayer(EntityPlayer entityplayer) {
        double d0 = entityplayer.locX - this.xLoc / 4096.0;
        double d2 = entityplayer.locZ - this.zLoc / 4096.0;
        int i = Math.min(this.e, this.f);
        return d0 >= -i && d0 <= i && d2 >= -i && d2 <= i && this.tracker.a(entityplayer);
    }

    private boolean e(EntityPlayer entityplayer) {
        return entityplayer.x().getPlayerChunkMap().a(entityplayer, this.tracker.ab, this.tracker.ad);
    }

    public void scanPlayers(List<EntityHuman> list) {
        for (int i = 0; i < list.size(); ++i) {
            this.updatePlayer((EntityPlayer) list.get(i));
        }
    }

    private Packet<?> e() {
        if (this.tracker.dead) {
            return null;
        }
        if (this.tracker instanceof EntityPlayer) {
            return (Packet<?>) new PacketPlayOutNamedEntitySpawn((EntityHuman) this.tracker);
        }
        if (this.tracker instanceof IAnimal) {
            this.headYaw = MathHelper.d(this.tracker.getHeadRotation() * 256.0f / 360.0f);
            return (Packet<?>) new PacketPlayOutSpawnEntityLiving((EntityLiving) this.tracker);
        }
        if (this.tracker instanceof EntityPainting) {
            return (Packet<?>) new PacketPlayOutSpawnEntityPainting((EntityPainting) this.tracker);
        }
        if (this.tracker instanceof EntityItem) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 2, 1);
        }
        if (this.tracker instanceof EntityMinecartAbstract) {
            EntityMinecartAbstract entityminecartabstract = (EntityMinecartAbstract) this.tracker;
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 10, entityminecartabstract.v().a());
        }
        if (this.tracker instanceof EntityBoat) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 1);
        }
        if (this.tracker instanceof EntityExperienceOrb) {
            return (Packet<?>) new PacketPlayOutSpawnEntityExperienceOrb((EntityExperienceOrb) this.tracker);
        }
        if (this.tracker instanceof EntityFishingHook) {
            EntityHuman entityhuman = ((EntityFishingHook) this.tracker).l();
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 90,
                    (entityhuman == null) ? this.tracker.getId() : entityhuman.getId());
        }
        if (this.tracker instanceof EntitySpectralArrow) {
            Entity entity = ((EntitySpectralArrow) this.tracker).shooter;
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 91,
                    1 + ((entity == null) ? this.tracker.getId() : entity.getId()));
        }
        if (this.tracker instanceof EntityTippedArrow) {
            Entity entity = ((EntityArrow) this.tracker).shooter;
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 60,
                    1 + ((entity == null) ? this.tracker.getId() : entity.getId()));
        }
        if (this.tracker instanceof EntitySnowball) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 61);
        }
        if (this.tracker instanceof EntityLlamaSpit) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 68);
        }
        if (this.tracker instanceof EntityPotion) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 73);
        }
        if (this.tracker instanceof EntityThrownExpBottle) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 75);
        }
        if (this.tracker instanceof EntityEnderPearl) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 65);
        }
        if (this.tracker instanceof EntityEnderSignal) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 72);
        }
        if (this.tracker instanceof EntityFireworks) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 76);
        }
        if (this.tracker instanceof EntityFireball) {
            EntityFireball entityfireball = (EntityFireball) this.tracker;
            PacketPlayOutSpawnEntity packetplayoutspawnentity = null;
            byte b0 = 63;
            if (this.tracker instanceof EntitySmallFireball) {
                b0 = 64;
            } else if (this.tracker instanceof EntityDragonFireball) {
                b0 = 93;
            } else if (this.tracker instanceof EntityWitherSkull) {
                b0 = 66;
            }
            if (entityfireball.shooter != null) {
                packetplayoutspawnentity = new PacketPlayOutSpawnEntity(this.tracker, (int) b0,
                        ((EntityFireball) this.tracker).shooter.getId());
            } else {
                packetplayoutspawnentity = new PacketPlayOutSpawnEntity(this.tracker, (int) b0, 0);
            }
            packetplayoutspawnentity.a((int) (entityfireball.dirX * 8000.0));
            packetplayoutspawnentity.b((int) (entityfireball.dirY * 8000.0));
            packetplayoutspawnentity.c((int) (entityfireball.dirZ * 8000.0));
            return (Packet<?>) packetplayoutspawnentity;
        }
        if (this.tracker instanceof EntityShulkerBullet) {
            PacketPlayOutSpawnEntity packetplayoutspawnentity2 = new PacketPlayOutSpawnEntity(this.tracker, 67, 0);
            packetplayoutspawnentity2.a((int) (this.tracker.motX * 8000.0));
            packetplayoutspawnentity2.b((int) (this.tracker.motY * 8000.0));
            packetplayoutspawnentity2.c((int) (this.tracker.motZ * 8000.0));
            return (Packet<?>) packetplayoutspawnentity2;
        }
        if (this.tracker instanceof EntityEgg) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 62);
        }
        if (this.tracker instanceof EntityEvokerFangs) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 79);
        }
        if (this.tracker instanceof EntityTNTPrimed) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 50);
        }
        if (this.tracker instanceof EntityEnderCrystal) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 51);
        }
        if (this.tracker instanceof EntityFallingBlock) {
            EntityFallingBlock entityfallingblock = (EntityFallingBlock) this.tracker;
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 70,
                    Block.getCombinedId(entityfallingblock.getBlock()));
        }
        if (this.tracker instanceof EntityArmorStand) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 78);
        }
        if (this.tracker instanceof EntityItemFrame) {
            EntityItemFrame entityitemframe = (EntityItemFrame) this.tracker;
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 71,
                    entityitemframe.direction.get2DRotationValue(), entityitemframe.getBlockPosition());
        }
        if (this.tracker instanceof EntityLeash) {
            EntityLeash entityleash = (EntityLeash) this.tracker;
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 77, 0, entityleash.getBlockPosition());
        }
        if (this.tracker instanceof EntityAreaEffectCloud) {
            return (Packet<?>) new PacketPlayOutSpawnEntity(this.tracker, 3);
        }
        throw new IllegalArgumentException("Don't know how to add " + this.tracker.getClass() + "!");
    }

    public synchronized void clear(EntityPlayer entityplayer) {
        // AsyncCatcher.catchOp("player tracker clear");
        if (this.trackedPlayers.contains(entityplayer)) {
            this.trackedPlayers.remove(entityplayer);
            this.tracker.c(entityplayer);
            entityplayer.c(this.tracker);
            this.updatePassengers(entityplayer);
        }
    }

    public Entity b() {
        return this.tracker;
    }

    public void a(int i) {
        this.f = i;
    }

    public void c() {
        this.isMoving = false;
    }
}

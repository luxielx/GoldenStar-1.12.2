package net.minecraft.server.v1_12_R1;

import com.destroystokyo.paper.event.player.PlayerLocaleChangeEvent;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChangedMainHandEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.MainHand;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@SuppressWarnings("deprecation")
public class EntityPlayer extends EntityHuman implements ICrafting {

    // ~
    // ConcurrentLinkedDeque

    private static Logger bV;
    public String locale;
    public long lastSave;
    public PlayerConnection playerConnection;
    public MinecraftServer server;
    public PlayerInteractManager playerInteractManager;
    public double d;
    public double e;
    public Deque<Integer> removeQueue;
    private AdvancementDataPlayer bY;
    private ServerStatisticManager bZ;
    private float ca;
    private int cb;
    private int cc;
    private int cd;
    private int ce;
    private int cf;
    private float lastHealthSent;
    private int ch;
    private boolean ci;
    public int lastSentExp;
    public int invulnerableTicks;
    private EntityHuman.EnumChatVisibility cl;
    private boolean cm;
    private long cn;
    private Entity co;
    public boolean worldChangeInvuln;
    private boolean cq;
    private RecipeBookServer cr;
    private Vec3D cs;
    private int ct;
    private boolean cu;
    private Vec3D cv;
    private int containerCounter;
    public boolean f;
    public int ping;
    public boolean viewingCredits;
    private int viewDistance;
    private int containerUpdateDelay;
    public boolean queueHealthUpdatePacket;
    public PacketPlayOutUpdateHealth queuedHealthUpdatePacket;
    public String displayName;
    public IChatBaseComponent listName;
    public Location compassTarget;
    public int newExp;
    public int newLevel;
    public int newTotalExp;
    public boolean keepLevel;
    public double maxHealthCache;
    public boolean joining;
    public boolean sentListPacket;
    public long timeOffset;
    public boolean relativeTime;
    public WeatherType weather;
    private float pluginRainPosition;
    private float pluginRainPositionPrevious;

    private void setHasSeenCredits(boolean has) {
        this.cq = has;
    }

    public int getViewDistance() {
        return (this.viewDistance == -1) ? ((WorldServer) this.world).getPlayerChunkMap().getViewDistance()
                : this.viewDistance;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
    }

    public EntityPlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile,
                        PlayerInteractManager playerinteractmanager) {
        super((World) worldserver, gameprofile);
        this.locale = null;
        this.lastSave = MinecraftServer.currentTick;
        // ~
        this.removeQueue = new ConcurrentLinkedDeque<Integer>();
        this.ca = Float.MIN_VALUE;
        this.cb = Integer.MIN_VALUE;
        this.cc = Integer.MIN_VALUE;
        this.cd = Integer.MIN_VALUE;
        this.ce = Integer.MIN_VALUE;
        this.cf = Integer.MIN_VALUE;
        this.lastHealthSent = -1.0E8f;
        this.ch = -99999999;
        this.ci = true;
        this.lastSentExp = -99999999;
        this.invulnerableTicks = 60;
        this.cm = true;
        this.cn = System.currentTimeMillis();
        this.cr = new RecipeBookServer();
        this.viewDistance = -1;
        this.queueHealthUpdatePacket = false;
        this.newExp = 0;
        this.newLevel = 0;
        this.newTotalExp = 0;
        this.keepLevel = false;
        this.joining = true;
        this.sentListPacket = false;
        this.timeOffset = 0L;
        this.relativeTime = true;
        this.weather = null;
        playerinteractmanager.player = this;
        this.playerInteractManager = playerinteractmanager;
        BlockPosition blockposition = this.getSpawnPoint(minecraftserver, worldserver);
        this.server = minecraftserver;
        this.bZ = minecraftserver.getPlayerList().getStatisticManager(this);
        this.bY = minecraftserver.getPlayerList().h(this);
        this.P = 1.0f;
        this.setPositionRotation(blockposition, 0.0f, 0.0f);
        while (!worldserver.getCubes((Entity) this, this.getBoundingBox()).isEmpty() && this.locY < 255.0) {
            this.setPosition(this.locX, this.locY + 1.0, this.locZ);
        }
        this.displayName = this.getName();
        this.canPickUpLoot = true;
        this.maxHealthCache = this.getMaxHealth();
    }

    public BlockPosition getSpawnPoint(MinecraftServer minecraftserver, WorldServer worldserver) {
        BlockPosition blockposition = worldserver.getSpawn();
        if (worldserver.worldProvider.m() && worldserver.getWorldData().getGameType() != EnumGamemode.ADVENTURE) {
            int i = Math.max(0, minecraftserver.a(worldserver));
            int j = MathHelper.floor(
                    worldserver.getWorldBorder().b((double) blockposition.getX(), (double) blockposition.getZ()));
            if (j < i) {
                i = j;
            }
            if (j <= 1) {
                i = 1;
            }
            blockposition = worldserver
                    .q(blockposition.a(this.random.nextInt(i * 2 + 1) - i, 0, this.random.nextInt(i * 2 + 1) - i));
        }
        return blockposition;
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (this.locY > 300.0) {
            this.locY = 257.0;
        }
        if (nbttagcompound.hasKeyOfType("playerGameType", 99)) {
            if (this.C_().getForceGamemode()) {
                this.playerInteractManager.setGameMode(this.C_().getGamemode());
            } else {
                this.playerInteractManager.setGameMode(EnumGamemode.getById(nbttagcompound.getInt("playerGameType")));
            }
        }
        if (nbttagcompound.hasKeyOfType("enteredNetherPosition", 10)) {
            NBTTagCompound nbttagcompound2 = nbttagcompound.getCompound("enteredNetherPosition");
            this.cv = new Vec3D(nbttagcompound2.getDouble("x"), nbttagcompound2.getDouble("y"),
                    nbttagcompound2.getDouble("z"));
        }
        this.cq = nbttagcompound.getBoolean("seenCredits");
        if (nbttagcompound.hasKeyOfType("recipeBook", 10)) {
            this.cr.a(nbttagcompound.getCompound("recipeBook"));
        }
        this.getBukkitEntity().readExtraData(nbttagcompound);
    }

    public static void a(DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.PLAYER, (DataInspector) new DataInspector() {
            public NBTTagCompound a(DataConverter dataconverter, NBTTagCompound nbttagcompound, int i) {
                if (nbttagcompound.hasKeyOfType("RootVehicle", 10)) {
                    NBTTagCompound nbttagcompound2 = nbttagcompound.getCompound("RootVehicle");
                    if (nbttagcompound2.hasKeyOfType("Entity", 10)) {
                        nbttagcompound2.set("Entity", dataconverter.a((DataConverterType) DataConverterTypes.ENTITY,
                                nbttagcompound2.getCompound("Entity"), i));
                    }
                }
                return nbttagcompound;
            }
        });
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("playerGameType", this.playerInteractManager.getGameMode().getId());
        nbttagcompound.setBoolean("seenCredits", this.cq);
        if (this.cv != null) {
            NBTTagCompound nbttagcompound2 = new NBTTagCompound();
            nbttagcompound2.setDouble("x", this.cv.x);
            nbttagcompound2.setDouble("y", this.cv.y);
            nbttagcompound2.setDouble("z", this.cv.z);
            nbttagcompound.set("enteredNetherPosition", nbttagcompound2);
        }
        Entity entity = this.getVehicle();
        Entity entity2 = this.bJ();
        if (entity2 != null && entity != this && entity.b((Class) EntityPlayer.class).size() == 1) {
            NBTTagCompound nbttagcompound3 = new NBTTagCompound();
            NBTTagCompound nbttagcompound4 = new NBTTagCompound();
            entity.d(nbttagcompound4);
            nbttagcompound3.a("Attach", entity2.getUniqueID());
            nbttagcompound3.set("Entity", nbttagcompound4);
            nbttagcompound.set("RootVehicle", nbttagcompound3);
        }
        nbttagcompound.set("recipeBook", this.cr.c());
        this.getBukkitEntity().setExtraData(nbttagcompound);
    }

    public void spawnIn(World world) {
        super.spawnIn(world);
        if (world == null) {
            this.dead = false;
            BlockPosition position = null;
            if (this.spawnWorld != null && !this.spawnWorld.equals("")) {
                CraftWorld cworld = (CraftWorld) Bukkit.getServer().getWorld(this.spawnWorld);
                if (cworld != null && this.getBed() != null) {
                    world = (World) cworld.getHandle();
                    position = EntityHuman.getBed((World) cworld.getHandle(), this.getBed(), false);
                }
            }
            if (world == null || position == null) {
                world = ((CraftWorld) Bukkit.getServer().getWorlds().get(0)).getHandle();
                position = world.getSpawn();
            }
            this.world = world;
            this.setPosition(position.getX() + 0.5, (double) position.getY(), position.getZ() + 0.5);
        }
        this.dimension = ((WorldServer) this.world).dimension;
        this.playerInteractManager.a((WorldServer) world);
    }

    public void levelDown(int i) {
        super.levelDown(i);
        this.lastSentExp = -1;
    }

    public void enchantDone(ItemStack itemstack, int i) {
        super.enchantDone(itemstack, i);
        this.lastSentExp = -1;
    }

    public void syncInventory() {
        this.activeContainer.addSlotListener((ICrafting) this);
    }

    public void enterCombat() {
        super.enterCombat();
        this.playerConnection.sendPacket((Packet) new PacketPlayOutCombatEvent(this.getCombatTracker(),
                PacketPlayOutCombatEvent.EnumCombatEventType.ENTER_COMBAT));
    }

    public void exitCombat() {
        super.exitCombat();
        this.playerConnection.sendPacket((Packet) new PacketPlayOutCombatEvent(this.getCombatTracker(),
                PacketPlayOutCombatEvent.EnumCombatEventType.END_COMBAT));
    }

    protected void a(IBlockData iblockdata) {
        CriterionTriggers.d.a(this, iblockdata);
    }

    protected ItemCooldown l() {
        return (ItemCooldown) new ItemCooldownPlayer(this);
    }

    public void B_() {
        if (this.joining) {
            this.joining = false;
        }
        this.playerInteractManager.a();
        --this.invulnerableTicks;
        if (this.noDamageTicks > 0) {
            --this.noDamageTicks;
        }
        if (--this.containerUpdateDelay <= 0) {
            this.activeContainer.b();
            this.containerUpdateDelay = this.world.paperConfig.containerUpdateTickRate;
        }
        if (!this.world.isClientSide && !this.activeContainer.canUse((EntityHuman) this)) {
            this.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
            this.activeContainer = this.defaultContainer;
        }
        while (!this.removeQueue.isEmpty()) {
            int i = Math.min(this.removeQueue.size(), Integer.MAX_VALUE);
            int[] aint = new int[i];
            Iterator iterator = this.removeQueue.iterator();
            Integer integer;
            for (int j = 0; j < i && (integer = this.removeQueue.poll()) != null; aint[j++] = integer) {
            }
            this.playerConnection.sendPacket((Packet) new PacketPlayOutEntityDestroy(aint));
        }
        Entity entity = this.getSpecatorTarget();
        if (entity != this) {
            if (entity.isAlive()) {
                this.setLocation(entity.locX, entity.locY, entity.locZ, entity.yaw, entity.pitch);
                this.server.getPlayerList().d(this);
                if (this.isSneaking()) {
                    this.setSpectatorTarget((Entity) this);
                }
            } else {
                this.setSpectatorTarget((Entity) this);
            }
        }
        CriterionTriggers.v.a(this);
        if (this.cs != null) {
            CriterionTriggers.t.a(this, this.cs, this.ticksLived - this.ct);
        }
        this.bY.b(this);
    }

    public void playerTick() {
        try {
            super.B_();
            for (int i = 0; i < this.inventory.getSize(); ++i) {
                ItemStack itemstack = this.inventory.getItem(i).cloneItemStack();
                if (!itemstack.isEmpty() && itemstack.getItem().f()) {
                    new Thread(() -> {
                        Packet packet = ((ItemWorldMapBase) itemstack.getItem()).a(itemstack, this.world,
                                (EntityHuman) this);
                        if (packet != null) {
                            this.playerConnection.sendPacket(packet);
                        }
                    }, "map render").start();
                }
            }
            if (this.getHealth() != this.lastHealthSent || this.ch != this.foodData.getFoodLevel()
                    || this.foodData.getSaturationLevel() == 0.0f != this.ci) {
                this.playerConnection
                        .sendPacket((Packet) new PacketPlayOutUpdateHealth(this.getBukkitEntity().getScaledHealth(),
                                this.foodData.getFoodLevel(), this.foodData.getSaturationLevel()));
                this.lastHealthSent = this.getHealth();
                this.ch = this.foodData.getFoodLevel();
                this.ci = (this.foodData.getSaturationLevel() == 0.0f);
            }
            if (this.getHealth() + this.getAbsorptionHearts() != this.ca) {
                this.ca = this.getHealth() + this.getAbsorptionHearts();
                this.a(IScoreboardCriteria.g, MathHelper.f(this.ca));
            }
            if (this.foodData.getFoodLevel() != this.cb) {
                this.cb = this.foodData.getFoodLevel();
                this.a(IScoreboardCriteria.h, MathHelper.f((float) this.cb));
            }
            if (this.getAirTicks() != this.cc) {
                this.cc = this.getAirTicks();
                this.a(IScoreboardCriteria.i, MathHelper.f((float) this.cc));
            }
            if (this.maxHealthCache != this.getMaxHealth()) {
                this.getBukkitEntity().updateScaledHealth();
            }
            if (this.getArmorStrength() != this.cd) {
                this.cd = this.getArmorStrength();
                this.a(IScoreboardCriteria.j, MathHelper.f((float) this.cd));
            }
            if (this.expTotal != this.cf) {
                this.cf = this.expTotal;
                this.a(IScoreboardCriteria.k, MathHelper.f((float) this.cf));
            }
            if (this.expLevel != this.ce) {
                this.ce = this.expLevel;
                this.a(IScoreboardCriteria.l, MathHelper.f((float) this.ce));
            }
            if (this.expTotal != this.lastSentExp) {
                this.lastSentExp = this.expTotal;
                this.playerConnection
                        .sendPacket((Packet) new PacketPlayOutExperience(this.exp, this.expTotal, this.expLevel));
            }
            if (this.ticksLived % 20 == 0) {
                CriterionTriggers.o.a(this);
            }
            if (this.oldLevel == -1) {
                this.oldLevel = this.expLevel;
            }
            if (this.oldLevel != this.expLevel) {
                CraftEventFactory.callPlayerLevelChangeEvent(this.world.getServer().getPlayer(this), this.oldLevel,
                        this.expLevel);
                this.oldLevel = this.expLevel;
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Ticking player");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Player being ticked");
            this.appendEntityCrashDetails(crashreportsystemdetails);
            throw new ReportedException(crashreport);
        }
    }

    private void a(IScoreboardCriteria iscoreboardcriteria, int i) {
        Collection collection = this.world.getServer().getScoreboardManager().getScoreboardScores(iscoreboardcriteria,
                this.getName(), new ArrayList());
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            ScoreboardScore scoreboardscore = (ScoreboardScore) iterator.next(); // CraftBukkit - Use our scores instead
            scoreboardscore.setScore(i);
        }
    }

    public void die(DamageSource damagesource) {
        boolean flag = this.world.getGameRules().getBoolean("showDeathMessages");
        if (this.dead) {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutCombatEvent(this.getCombatTracker(),
                    PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED, flag));
            return;
        }
        List<org.bukkit.inventory.ItemStack> loot = new ArrayList<org.bukkit.inventory.ItemStack>(
                this.inventory.getSize());
        boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory") || this.isSpectator();
        if (!keepInventory) {
            for (ItemStack item : this.inventory.getContents()) {
                if (!item.isEmpty() && !EnchantmentManager.shouldNotDrop(item)) {
                    loot.add((org.bukkit.inventory.ItemStack) CraftItemStack.asCraftMirror(item));
                }
            }
        }
        IChatBaseComponent chatmessage = this.getCombatTracker().getDeathMessage();
        String deathmessage = chatmessage.toPlainText();
        PlayerDeathEvent event = CraftEventFactory.callPlayerDeathEvent(this, (List) loot, deathmessage, keepInventory);
        if (event.isCancelled()) {
            if (this.getHealth() <= 0.0f) {
                this.setHealth((float) event.getReviveHealth());
            }
            return;
        }
        this.playerConnection.sendPacket((Packet) new PacketPlayOutCombatEvent(this.getCombatTracker(),
                PacketPlayOutCombatEvent.EnumCombatEventType.ENTITY_DIED, flag));
        String deathMessage = event.getDeathMessage();
        if (deathMessage != null && deathMessage.length() > 0 && flag) {
            if (deathMessage.equals(deathmessage)) {
                ScoreboardTeamBase scoreboardteambase = this.aY();
                if (scoreboardteambase != null && scoreboardteambase
                        .getDeathMessageVisibility() != ScoreboardTeamBase.EnumNameTagVisibility.ALWAYS) {
                    if (scoreboardteambase
                            .getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OTHER_TEAMS) {
                        this.server.getPlayerList().a((EntityHuman) this, chatmessage);
                    } else if (scoreboardteambase
                            .getDeathMessageVisibility() == ScoreboardTeamBase.EnumNameTagVisibility.HIDE_FOR_OWN_TEAM) {
                        this.server.getPlayerList().b((EntityHuman) this, chatmessage);
                    }
                } else {
                    this.server.getPlayerList().sendMessage(chatmessage);
                }
            } else {
                this.server.getPlayerList().sendMessage(CraftChatMessage.fromString(deathMessage));
            }
        }
        this.releaseShoulderEntities();
        if (!event.getKeepInventory()) {
            this.inventory.clear();
        }
        this.closeInventory(InventoryCloseEvent.Reason.DEATH);
        this.setSpectatorTarget((Entity) this);
        Collection collection = this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.d,
                this.getName(), (Collection) new ArrayList());
        Iterator i = collection.iterator();
        while (i.hasNext()) {
            ScoreboardScore scoreboardscore = (ScoreboardScore) i.next();
            scoreboardscore.incrementScore();
        }
        EntityLiving entityliving = this.ci();
        if (entityliving != null) {
            EntityTypes.MonsterEggInfo entitytypes_monsteregginfo = EntityTypes.eggInfo
                    .get(EntityTypes.a((Entity) entityliving));
            if (entitytypes_monsteregginfo != null) {
                this.b(entitytypes_monsteregginfo.killedByEntityStatistic);
            }
            entityliving.a((Entity) this, this.bb, damagesource);
        }
        this.b(StatisticList.A);
        this.a(StatisticList.h);
        this.extinguish();
        this.setFlag(0, false);
        this.getCombatTracker().g();
    }

    public void a(Entity entity, int i, DamageSource damagesource) {
        if (entity != this) {
            super.a(entity, i, damagesource);
            this.addScore(i);
            Collection<ScoreboardScore> collection = (Collection<ScoreboardScore>) this.world.getServer()
                    .getScoreboardManager()
                    .getScoreboardScores(IScoreboardCriteria.f, this.getName(), (Collection) new ArrayList());
            if (entity instanceof EntityHuman) {
                this.b(StatisticList.D);
                this.world.getServer().getScoreboardManager().getScoreboardScores(IScoreboardCriteria.e, this.getName(),
                        (Collection) collection);
            } else {
                this.b(StatisticList.B);
            }
            collection.addAll(this.E(entity));
            Iterator<ScoreboardScore> iterator = collection.iterator();
            while (iterator.hasNext()) {
                iterator.next().incrementScore();
            }
            CriterionTriggers.b.a(this, entity, damagesource);
        }
    }

    private Collection<ScoreboardScore> E(Entity entity) {
        String s = (entity instanceof EntityHuman) ? entity.getName() : entity.bn();
        ScoreboardTeam scoreboardteam = this.getScoreboard().getPlayerTeam(this.getName());
        if (scoreboardteam != null) {
            int i = scoreboardteam.getColor().b();
            if (i >= 0 && i < IScoreboardCriteria.n.length) {
                for (ScoreboardObjective scoreboardobjective : this.getScoreboard()
                        .getObjectivesForCriteria(IScoreboardCriteria.n[i])) {
                    ScoreboardScore scoreboardscore = this.getScoreboard().getPlayerScoreForObjective(s,
                            scoreboardobjective);
                    scoreboardscore.incrementScore();
                }
            }
        }
        ScoreboardTeam scoreboardteam2 = this.getScoreboard().getPlayerTeam(s);
        if (scoreboardteam2 != null) {
            int j = scoreboardteam2.getColor().b();
            if (j >= 0 && j < IScoreboardCriteria.m.length) {
                return (Collection<ScoreboardScore>) this.world.getServer().getScoreboardManager()
                        .getScoreboardScores(IScoreboardCriteria.m[j], this.getName(), (Collection) new ArrayList());
            }
        }
        return Lists.newArrayList();
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        if (this.isInvulnerable(damagesource)) {
            return false;
        }
        boolean flag = this.server.aa() && this.canPvP() && "fall".equals(damagesource.translationIndex);
        if (!flag && this.invulnerableTicks > 0 && damagesource != DamageSource.OUT_OF_WORLD) {
            return false;
        }
        if (damagesource instanceof EntityDamageSource) {
            Entity entity = damagesource.getEntity();
            if (entity instanceof EntityHuman && !this.a((EntityHuman) entity)) {
                return false;
            }
            if (entity instanceof EntityArrow) {
                EntityArrow entityarrow = (EntityArrow) entity;
                if (entityarrow.shooter instanceof EntityHuman && !this.a((EntityHuman) entityarrow.shooter)) {
                    return false;
                }
            }
        }

        this.queueHealthUpdatePacket = true;
        final boolean damaged = super.damageEntity(damagesource, f);
        this.queueHealthUpdatePacket = false;
        if (this.queuedHealthUpdatePacket != null) {
            this.playerConnection.sendPacket((Packet) this.queuedHealthUpdatePacket);
            this.queuedHealthUpdatePacket = null;
        }
        return damaged;
    }

    public boolean a(EntityHuman entityhuman) {
        return this.canPvP() && super.a(entityhuman);
    }

    private boolean canPvP() {
        return this.world.pvpMode;
    }

    @Nullable
    public Entity b(int i) {
        if (this.isSleeping()) {
            return (Entity) this;
        }
        if (this.dimension == 0 && i == -1) {
            this.cv = new Vec3D(this.locX, this.locY, this.locZ);
        } else if (this.dimension != -1 && i != 0) {
            this.cv = null;
        }
        if (this.dimension == 1 && i == 1) {
            this.worldChangeInvuln = true;
            this.world.kill((Entity) this);
            if (!this.viewingCredits) {
                this.viewingCredits = true;
                if (this.world.paperConfig.disableEndCredits) {
                    this.setHasSeenCredits(true);
                }
                this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(4, this.cq ? 0.0f : 1.0f));
                this.cq = true;
            }
            return (Entity) this;
        }
        if (this.dimension == 0 && i == 1) {
            i = 1;
        }
        PlayerTeleportEvent.TeleportCause cause = (this.dimension == 1 || i == 1)
                ? PlayerTeleportEvent.TeleportCause.END_PORTAL
                : PlayerTeleportEvent.TeleportCause.NETHER_PORTAL;
        this.server.getPlayerList().changeDimension(this, i, cause);
        this.playerConnection.sendPacket((Packet) new PacketPlayOutWorldEvent(1032, BlockPosition.ZERO, 0, false));
        this.lastSentExp = -1;
        this.lastHealthSent = -1.0f;
        this.ch = -1;
        return (Entity) this;
    }

    public boolean a(EntityPlayer entityplayer) {
        return entityplayer.isSpectator() ? (this.getSpecatorTarget() == this)
                : (!this.isSpectator() && super.a(entityplayer));
    }

    private void a(TileEntity tileentity) {
        if (tileentity != null) {
            PacketPlayOutTileEntityData packetplayouttileentitydata = tileentity.getUpdatePacket();
            if (packetplayouttileentitydata != null) {
                this.playerConnection.sendPacket((Packet) packetplayouttileentitydata);
            }
        }
    }

    public void receive(Entity entity, int i) {
        super.receive(entity, i);
        this.activeContainer.b();
    }

    public EntityHuman.EnumBedResult a(BlockPosition blockposition) {
        EntityHuman.EnumBedResult entityhuman_enumbedresult = super.a(blockposition);
        if (entityhuman_enumbedresult == EntityHuman.EnumBedResult.OK) {
            this.b(StatisticList.ab);
            PacketPlayOutBed packetplayoutbed = new PacketPlayOutBed((EntityHuman) this, blockposition);
            this.x().getTracker().a((Entity) this, (Packet<?>) packetplayoutbed);
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
            this.playerConnection.sendPacket((Packet) packetplayoutbed);
            CriterionTriggers.p.a(this);
        }
        return entityhuman_enumbedresult;
    }

    public void a(boolean flag, boolean flag1, boolean flag2) {
        if (!this.sleeping) {
            return;
        }
        if (this.isSleeping()) {
            this.x().getTracker().sendPacketToEntity((Entity) this,
                    (Packet<?>) new PacketPlayOutAnimation((Entity) this, 2));
        }
        super.a(flag, flag1, flag2);
        if (this.playerConnection != null) {
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        }
    }

    public boolean a(Entity entity, boolean flag) {
        Entity entity2 = this.bJ();
        if (!super.a(entity, flag)) {
            return false;
        }
        Entity entity3 = this.bJ();
        if (entity3 != entity2 && this.playerConnection != null) {
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        }
        return true;
    }

    public void stopRiding() {
        Entity entity = this.bJ();
        super.stopRiding();
        Entity entity2 = this.bJ();
        if (entity2 != entity && this.playerConnection != null) {
            this.playerConnection.a(this.locX, this.locY, this.locZ, this.yaw, this.pitch);
        }
        if (entity instanceof EntityPlayer) {
            WorldServer worldServer = (WorldServer) entity.getWorld();
            worldServer.tracker.untrackEntity((Entity) this);
            worldServer.tracker.track((Entity) this);
        }
    }

    public boolean isInvulnerable(DamageSource damagesource) {
        return super.isInvulnerable(damagesource) || this.L();
    }

    protected void a(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {
    }

    protected void b(BlockPosition blockposition) {
        if (!this.isSpectator()) {
            super.b(blockposition);
        }
    }

    public void a(double d0, boolean flag) {
        int i = MathHelper.floor(this.locX);
        int j = MathHelper.floor(this.locY - 0.20000000298023224);
        int k = MathHelper.floor(this.locZ);
        BlockPosition blockposition = new BlockPosition(i, j, k);
        IBlockData iblockdata = this.world.getType(blockposition);
        if (iblockdata.getMaterial() == Material.AIR) {
            BlockPosition blockposition2 = blockposition.down();
            IBlockData iblockdata2 = this.world.getType(blockposition2);
            Block block = iblockdata2.getBlock();
            if (block instanceof BlockFence || block instanceof BlockCobbleWall || block instanceof BlockFenceGate) {
                blockposition = blockposition2;
                iblockdata = iblockdata2;
            }
        }
        super.a(d0, flag, iblockdata, blockposition);
    }

    public void openSign(TileEntitySign tileentitysign) {
        tileentitysign.a((EntityHuman) this);
        this.playerConnection.sendPacket((Packet) new PacketPlayOutOpenSignEditor(tileentitysign.getPosition()));
    }

    public int nextContainerCounter() {
        return this.containerCounter = this.containerCounter % 100 + 1;
    }

    public void openTileEntity(ITileEntityContainer itileentitycontainer) {
        boolean cancelled = itileentitycontainer instanceof ILootable && ((ILootable) itileentitycontainer).b() != null
                && this.isSpectator();
        Container container = CraftEventFactory.callInventoryOpenEvent(this,
                itileentitycontainer.createContainer(this.inventory, (EntityHuman) this), cancelled);
        if (container == null) {
            return;
        }
        this.nextContainerCounter();
        this.activeContainer = container;
        this.playerConnection.sendPacket((Packet) new PacketPlayOutOpenWindow(this.containerCounter,
                itileentitycontainer.getContainerName(), itileentitycontainer.getScoreboardDisplayName()));
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener((ICrafting) this);
    }

    public void openContainer(IInventory iinventory) {
        boolean cancelled = false;
        if (iinventory instanceof ITileInventory) {
            ITileInventory itileinventory = (ITileInventory) iinventory;
            cancelled = (itileinventory.isLocked() && !this.a(itileinventory.getLock()) && !this.isSpectator());
        }
        Container container;
        if (iinventory instanceof ITileEntityContainer) {
            if (iinventory instanceof TileEntity) {
                Preconditions.checkArgument(((TileEntity) iinventory).getWorld() != null,
                        (Object) "Container must have world to be opened");
            }
            container = ((ITileEntityContainer) iinventory).createContainer(this.inventory, (EntityHuman) this);
        } else {
            container = (Container) new ContainerChest((IInventory) this.inventory, iinventory, (EntityHuman) this);
        }
        container = CraftEventFactory.callInventoryOpenEvent(this, container, cancelled);
        if (container == null && !cancelled) {
            iinventory.closeContainer((EntityHuman) this);
            return;
        }
        if (iinventory instanceof ILootable && ((ILootable) iinventory).b() != null && this.isSpectator()) {
            this.a(new ChatMessage("container.spectatorCantOpen", new Object[0])
                    .setChatModifier(new ChatModifier().setColor(EnumChatFormat.RED)), true);
        } else {
            if (this.activeContainer != this.defaultContainer) {
                this.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
            }
            if (iinventory instanceof ITileInventory) {
                ITileInventory itileinventory2 = (ITileInventory) iinventory;
                if (itileinventory2.isLocked() && !this.a(itileinventory2.getLock()) && !this.isSpectator()) {
                    this.playerConnection.sendPacket((Packet) new PacketPlayOutChat(
                            (IChatBaseComponent) new ChatMessage("container.isLocked",
                                    new Object[]{iinventory.getScoreboardDisplayName()}),
                            ChatMessageType.GAME_INFO));
                    this.playerConnection.sendPacket((Packet) new PacketPlayOutNamedSoundEffect(SoundEffects.ab,
                            SoundCategory.BLOCKS, this.locX, this.locY, this.locZ, 1.0f, 1.0f));
                    iinventory.closeContainer((EntityHuman) this);
                    return;
                }
            }
            this.nextContainerCounter();
            if (iinventory instanceof ITileEntityContainer) {
                this.activeContainer = container;
                this.playerConnection.sendPacket((Packet) new PacketPlayOutOpenWindow(this.containerCounter,
                        ((ITileEntityContainer) iinventory).getContainerName(), iinventory.getScoreboardDisplayName(),
                        iinventory.getSize()));
            } else {
                this.activeContainer = container;
                this.playerConnection.sendPacket((Packet) new PacketPlayOutOpenWindow(this.containerCounter,
                        "minecraft:container", iinventory.getScoreboardDisplayName(), iinventory.getSize()));
            }
            this.activeContainer.windowId = this.containerCounter;
            this.activeContainer.addSlotListener((ICrafting) this);
        }
    }

    public void openTrade(IMerchant imerchant) {
        Container container = CraftEventFactory.callInventoryOpenEvent(this,
                (Container) new ContainerMerchant(this.inventory, imerchant, this.world));
        if (container == null) {
            return;
        }
        this.nextContainerCounter();
        this.activeContainer = container;
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener((ICrafting) this);
        InventoryMerchant inventorymerchant = ((ContainerMerchant) this.activeContainer).e();
        IChatBaseComponent ichatbasecomponent = imerchant.getScoreboardDisplayName();
        this.playerConnection.sendPacket((Packet) new PacketPlayOutOpenWindow(this.containerCounter,
                "minecraft:villager", ichatbasecomponent, inventorymerchant.getSize()));
        MerchantRecipeList merchantrecipelist = imerchant.getOffers((EntityHuman) this);
        if (merchantrecipelist != null) {
            PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
            packetdataserializer.writeInt(this.containerCounter);
            merchantrecipelist.a(packetdataserializer);
            this.playerConnection
                    .sendPacket((Packet) new PacketPlayOutCustomPayload("MC|TrList", packetdataserializer));
        }
    }

    public void openHorseInventory(EntityHorseAbstract entityhorseabstract, IInventory iinventory) {
        Container container = CraftEventFactory.callInventoryOpenEvent(this,
                (Container) new ContainerHorse((IInventory) this.inventory, iinventory, entityhorseabstract,
                        (EntityHuman) this));
        if (container == null) {
            iinventory.closeContainer((EntityHuman) this);
            return;
        }
        if (this.activeContainer != this.defaultContainer) {
            this.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW);
        }
        this.nextContainerCounter();
        this.playerConnection.sendPacket((Packet) new PacketPlayOutOpenWindow(this.containerCounter, "EntityHorse",
                iinventory.getScoreboardDisplayName(), iinventory.getSize(), entityhorseabstract.getId()));
        this.activeContainer = container;
        this.activeContainer.windowId = this.containerCounter;
        this.activeContainer.addSlotListener((ICrafting) this);
    }

    public void a(ItemStack itemstack, EnumHand enumhand) {
        Item item = itemstack.getItem();
        if (item == Items.WRITTEN_BOOK) {
            PacketDataSerializer packetdataserializer = new PacketDataSerializer(Unpooled.buffer());
            packetdataserializer.a((Enum) enumhand);
            this.playerConnection.sendPacket((Packet) new PacketPlayOutCustomPayload("MC|BOpen", packetdataserializer));
        }
    }

    public void a(TileEntityCommand tileentitycommand) {
        tileentitycommand.c(true);
        this.a((TileEntity) tileentitycommand);
    }

    public void a(Container container, int i, ItemStack itemstack) {
        if (!(container.getSlot(i) instanceof SlotResult)) {
            if (container == this.defaultContainer) {
                CriterionTriggers.e.a(this, this.inventory);
            }
            if (!this.f) {
                this.playerConnection.sendPacket((Packet) new PacketPlayOutSetSlot(container.windowId, i, itemstack));
            }
        }
    }

    public void updateInventory(Container container) {
        this.a(container, (NonNullList<ItemStack>) container.a());
    }

    public void a(Container container, NonNullList<ItemStack> nonnulllist) {
        this.playerConnection
                .sendPacket((Packet) new PacketPlayOutWindowItems(container.windowId, (NonNullList) nonnulllist));
        this.playerConnection.sendPacket((Packet) new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        if (EnumSet.of(InventoryType.CRAFTING, InventoryType.WORKBENCH).contains(container.getBukkitView().getType())) {
            this.playerConnection.sendPacket(
                    (Packet) new PacketPlayOutSetSlot(container.windowId, 0, container.getSlot(0).getItem()));
        }
    }

    public void setContainerData(Container container, int i, int j) {
        this.playerConnection.sendPacket((Packet) new PacketPlayOutWindowData(container.windowId, i, j));
    }

    public void setContainerData(Container container, IInventory iinventory) {
        for (int i = 0; i < iinventory.h(); ++i) {
            this.playerConnection
                    .sendPacket((Packet) new PacketPlayOutWindowData(container.windowId, i, iinventory.getProperty(i)));
        }
    }

    public void closeInventory() {
        this.closeInventory(InventoryCloseEvent.Reason.UNKNOWN);
    }

    public void closeInventory(InventoryCloseEvent.Reason reason) {
        CraftEventFactory.handleInventoryCloseEvent((EntityHuman) this, reason);
        this.playerConnection.sendPacket((Packet) new PacketPlayOutCloseWindow(this.activeContainer.windowId));
        this.r();
    }

    public void broadcastCarriedItem() {
        if (!this.f) {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutSetSlot(-1, -1, this.inventory.getCarried()));
        }
    }

    public void r() {
        this.activeContainer.b((EntityHuman) this);
        this.activeContainer = this.defaultContainer;
    }

    public void a(float f, float f1, boolean flag, boolean flag1) {
        if (this.isPassenger()) {
            if (f >= -1.0f && f <= 1.0f) {
                this.be = f;
            }
            if (f1 >= -1.0f && f1 <= 1.0f) {
                this.bg = f1;
            }
            this.bd = flag;
            this.setSneaking(flag1);
        }
    }

    public void a(Statistic statistic, int i) {
        if (statistic != null) {
            this.bZ.b((EntityHuman) this, statistic, i);
            for (ScoreboardObjective scoreboardobjective : this.getScoreboard()
                    .getObjectivesForCriteria(statistic.f())) {
                this.getScoreboard().getPlayerScoreForObjective(this.getName(), scoreboardobjective).addScore(i);
            }
        }
    }

    public void a(Statistic statistic) {
        if (statistic != null) {
            this.bZ.setStatistic((EntityHuman) this, statistic, 0);
            for (ScoreboardObjective scoreboardobjective : this.getScoreboard()
                    .getObjectivesForCriteria(statistic.f())) {
                this.getScoreboard().getPlayerScoreForObjective(this.getName(), scoreboardobjective).setScore(0);
            }
        }
    }

    public void a(List<IRecipe> list) {
        this.cr.a((List) list, this);
    }

    public void a(MinecraftKey[] aminecraftkey) {
        ArrayList arraylist = Lists.newArrayList();
        for (MinecraftKey minecraftkey : aminecraftkey) {
            if (CraftingManager.a(minecraftkey) == null) {
                Bukkit.getLogger().warning("Ignoring grant of non existent recipe " + minecraftkey);
            } else {
                arraylist.add(CraftingManager.a(minecraftkey));
            }
        }
        this.a((List<IRecipe>) arraylist);
    }

    public void b(List<IRecipe> list) {
        this.cr.b((List) list, this);
    }

    public void s() {
        this.cu = true;
        this.ejectPassengers();
        if (this.isPassenger() && this.getVehicleDirect() instanceof EntityPlayer) {
            this.stopRiding();
        }
        if (this.sleeping) {
            this.a(true, false, false);
        }
    }

    public boolean t() {
        return this.cu;
    }

    public void triggerHealthUpdate() {
        this.lastHealthSent = -1.0E8f;
        this.lastSentExp = -1;
    }

    public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
        for (IChatBaseComponent component : ichatbasecomponent) {
            this.sendMessage(component);
        }
    }

    public void a(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.playerConnection.sendPacket((Packet) new PacketPlayOutChat(ichatbasecomponent,
                flag ? ChatMessageType.GAME_INFO : ChatMessageType.CHAT));
    }

    protected void v() {
        if (!this.activeItem.isEmpty() && this.isHandRaised()) {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutEntityStatus((Entity) this, (byte) 9));
            super.v();
        }
    }

    public void copyFrom(EntityPlayer entityplayer, boolean flag) {
        if (flag) {
            this.inventory.a(entityplayer.inventory);
            this.setHealth(entityplayer.getHealth());
            this.foodData = entityplayer.foodData;
            this.expLevel = entityplayer.expLevel;
            this.expTotal = entityplayer.expTotal;
            this.exp = entityplayer.exp;
            this.setScore(entityplayer.getScore());
            this.an = entityplayer.an;
            this.ao = entityplayer.ao;
            this.ap = entityplayer.ap;
        } else if (this.world.getGameRules().getBoolean("keepInventory") || entityplayer.isSpectator()) {
            this.inventory.a(entityplayer.inventory);
            this.expLevel = entityplayer.expLevel;
            this.expTotal = entityplayer.expTotal;
            this.exp = entityplayer.exp;
            this.setScore(entityplayer.getScore());
        }
        this.bS = entityplayer.bS;
        this.enderChest = entityplayer.enderChest;
        this.getDataWatcher().set(EntityPlayer.br, entityplayer.getDataWatcher().get(EntityPlayer.br));
        this.lastSentExp = -1;
        this.lastHealthSent = -1.0f;
        this.ch = -1;
        if (this.removeQueue != entityplayer.removeQueue) {
            this.removeQueue.addAll((Collection<? extends Integer>) entityplayer.removeQueue);
        }
        this.cq = entityplayer.cq;
        this.cv = entityplayer.cv;
        this.setShoulderEntityLeft(entityplayer.getShoulderEntityLeft());
        this.setShoulderEntityRight(entityplayer.getShoulderEntityRight());
    }

    protected void a(MobEffect mobeffect) {
        super.a(mobeffect);
        this.playerConnection.sendPacket((Packet) new PacketPlayOutEntityEffect(this.getId(), mobeffect));
        if (mobeffect.getMobEffect() == MobEffects.LEVITATION) {
            this.ct = this.ticksLived;
            this.cs = new Vec3D(this.locX, this.locY, this.locZ);
        }
        CriterionTriggers.z.a(this);
    }

    protected void a(MobEffect mobeffect, boolean flag) {
        super.a(mobeffect, flag);
        this.playerConnection.sendPacket((Packet) new PacketPlayOutEntityEffect(this.getId(), mobeffect));
        CriterionTriggers.z.a(this);
    }

    protected void b(MobEffect mobeffect) {
        super.b(mobeffect);
        this.playerConnection
                .sendPacket((Packet) new PacketPlayOutRemoveEntityEffect(this.getId(), mobeffect.getMobEffect()));
        if (mobeffect.getMobEffect() == MobEffects.LEVITATION) {
            this.cs = null;
        }
        CriterionTriggers.z.a(this);
    }

    public void enderTeleportTo(double d0, double d1, double d2) {
        this.playerConnection.a(d0, d1, d2, this.yaw, this.pitch);
    }

    public void a(Entity entity) {
        this.x().getTracker().sendPacketToEntity((Entity) this, (Packet<?>) new PacketPlayOutAnimation(entity, 4));
    }

    public void b(Entity entity) {
        this.x().getTracker().sendPacketToEntity((Entity) this, (Packet<?>) new PacketPlayOutAnimation(entity, 5));
    }

    public void updateAbilities() {
        if (this.playerConnection != null) {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutAbilities(this.abilities));
            this.G();
        }
    }

    public WorldServer x() {
        return (WorldServer) this.world;
    }

    public void a(EnumGamemode enumgamemode) {
        if (enumgamemode == this.playerInteractManager.getGameMode()) {
            return;
        }
        PlayerGameModeChangeEvent event = new PlayerGameModeChangeEvent((Player) this.getBukkitEntity(),
                GameMode.getByValue(enumgamemode.getId()));
        this.world.getServer().getPluginManager().callEvent((Event) event);
        if (event.isCancelled()) {
            return;
        }
        this.playerInteractManager.setGameMode(enumgamemode);
        this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(3, (float) enumgamemode.getId()));
        if (enumgamemode == EnumGamemode.SPECTATOR) {
            this.releaseShoulderEntities();
            this.stopRiding();
        } else {
            this.setSpectatorTarget((Entity) this);
        }
        this.updateAbilities();
        this.cE();
    }

    public boolean isSpectator() {
        return this.playerInteractManager.getGameMode() == EnumGamemode.SPECTATOR;
    }

    public boolean z() {
        return this.playerInteractManager.getGameMode() == EnumGamemode.CREATIVE;
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.playerConnection.sendPacket((Packet) new PacketPlayOutChat(ichatbasecomponent));
    }

    public boolean a(int i, String s) {
        if ("@".equals(s)) {
            return this.getBukkitEntity().hasPermission("minecraft.command.selector");
        }
        if ("".equals(s)) {
            return this.getBukkitEntity().isOp();
        }
        return this.getBukkitEntity().hasPermission("minecraft.command." + s);
    }

    public String A() {
        String s = this.playerConnection.networkManager.getSocketAddress().toString();
        s = s.substring(s.indexOf("/") + 1);
        s = s.substring(0, s.indexOf(":"));
        return s;
    }

    public void a(PacketPlayInSettings packetplayinsettings) {
        if (this.getMainHand() != packetplayinsettings.getMainHand()) {
            PlayerChangedMainHandEvent event = new PlayerChangedMainHandEvent((Player) this.getBukkitEntity(),
                    (this.getMainHand() == EnumMainHand.LEFT) ? MainHand.LEFT : MainHand.RIGHT);
            this.server.server.getPluginManager().callEvent((Event) event);
        }
        String oldLocale = this.locale;
        this.locale = packetplayinsettings.a();
        if (!this.locale.equals(oldLocale)) {
            new PlayerLocaleChangeEvent((Player) this.getBukkitEntity(), oldLocale, this.locale).callEvent();
        }
        oldLocale = ((oldLocale != null) ? oldLocale : "en_us");
        if (!oldLocale.equals(packetplayinsettings.a())) {
            org.bukkit.event.player.PlayerLocaleChangeEvent event2 = new org.bukkit.event.player.PlayerLocaleChangeEvent(
                    (Player) this.getBukkitEntity(), packetplayinsettings.a());
            this.server.server.getPluginManager().callEvent((Event) event2);
        }
        this.cl = packetplayinsettings.c();
        this.cm = packetplayinsettings.d();
        this.getDataWatcher().set(EntityPlayer.br, (byte) packetplayinsettings.e());
        this.getDataWatcher().set(EntityPlayer.bs,
                (byte) ((packetplayinsettings.getMainHand() != EnumMainHand.LEFT) ? 1 : 0));
    }

    public EntityHuman.EnumChatVisibility getChatFlags() {
        return this.cl;
    }

    public void setResourcePack(String s, String s1) {
        this.playerConnection.sendPacket((Packet) new PacketPlayOutResourcePackSend(s, s1));
    }

    public BlockPosition getChunkCoordinates() {
        return new BlockPosition(this.locX, this.locY + 0.5, this.locZ);
    }

    public void resetIdleTimer() {
        this.cn = MinecraftServer.aw();
    }

    public ServerStatisticManager getStatisticManager() {
        return this.bZ;
    }

    public RecipeBookServer F() {
        return this.cr;
    }

    public void c(Entity entity) {
        if (entity instanceof EntityHuman) {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutEntityDestroy(new int[]{entity.getId()}));
        } else {
            this.removeQueue.add(entity.getId());
        }
    }

    public void d(Entity entity) {
        this.removeQueue.remove(entity.getId());
    }

    protected void G() {
        if (this.isSpectator()) {
            this.bY();
            this.setInvisible(true);
        } else {
            super.G();
        }
        x().getTracker().a(this);
    }

    public Entity getSpecatorTarget() {
        return (Entity) ((this.co == null) ? this : this.co);
    }

    public void setSpectatorTarget(Entity entity) {
        Entity entity2 = this.getSpecatorTarget();
        this.co = (Entity) ((entity == null) ? this : entity);
        if (entity2 != this.co) {
            this.playerConnection.sendPacket(new PacketPlayOutCamera(this.co));
            this.playerConnection.a(this.co.locX, this.co.locY, this.co.locZ, this.yaw, this.pitch,
                    PlayerTeleportEvent.TeleportCause.SPECTATE);
        }
    }

    protected void I() {
        if (this.portalCooldown > 0 && !this.worldChangeInvuln) {
            --this.portalCooldown;
        }
    }

    public void attack(Entity entity) {
        if (this.playerInteractManager.getGameMode() == EnumGamemode.SPECTATOR) {
            this.setSpectatorTarget(entity);
        } else {
            super.attack(entity);
        }
    }

    public long J() {
        return this.cn;
    }

    @Nullable
    public IChatBaseComponent getPlayerListName() {
        return this.listName;
    }

    public void a(EnumHand enumhand) {
        super.a(enumhand);
        this.ds();
    }

    public boolean L() {
        return this.worldChangeInvuln;
    }

    public void M() {
        this.worldChangeInvuln = false;
    }

    public void N() {
        if (!CraftEventFactory.callToggleGlideEvent((EntityLiving) this, true).isCancelled()) {
            this.setFlag(7, true);
        }
    }

    public void O() {
        if (!CraftEventFactory.callToggleGlideEvent((EntityLiving) this, false).isCancelled()) {
            this.setFlag(7, true);
            this.setFlag(7, false);
        }
    }

    public AdvancementDataPlayer getAdvancementData() {
        return this.bY;
    }

    @Nullable
    public Vec3D Q() {
        return this.cv;
    }

    public long getPlayerTime() {
        if (this.relativeTime) {
            return this.world.getDayTime() + this.timeOffset;
        }
        return this.world.getDayTime() - this.world.getDayTime() % 24000L + this.timeOffset;
    }

    public WeatherType getPlayerWeather() {
        return this.weather;
    }

    public void setPlayerWeather(WeatherType type, boolean plugin) {
        if (!plugin && this.weather != null) {
            return;
        }
        if (plugin) {
            this.weather = type;
        }
        if (type == WeatherType.DOWNFALL) {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(2, 0.0f));
        } else {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(1, 0.0f));
        }
    }

    public void updateWeather(float oldRain, float newRain, float oldThunder, float newThunder) {
        if (this.weather == null) {
            if (oldRain != newRain) {
                this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(7, newRain));
            }
        } else if (this.pluginRainPositionPrevious != this.pluginRainPosition) {
            this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(7, this.pluginRainPosition));
        }
        if (oldThunder != newThunder) {
            if (this.weather == WeatherType.DOWNFALL || this.weather == null) {
                this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(8, newThunder));
            } else {
                this.playerConnection.sendPacket((Packet) new PacketPlayOutGameStateChange(8, 0.0f));
            }
        }
    }

    public void tickWeather() {
        if (this.weather == null) {
            return;
        }
        this.pluginRainPositionPrevious = this.pluginRainPosition;
        if (this.weather == WeatherType.DOWNFALL) {
            this.pluginRainPosition += 0.01;
        } else {
            this.pluginRainPosition -= 0.01;
        }
        this.pluginRainPosition = MathHelper.a(this.pluginRainPosition, 0.0f, 1.0f);
    }

    public void resetPlayerWeather() {
        this.weather = null;
        this.setPlayerWeather(this.world.getWorldData().hasStorm() ? WeatherType.DOWNFALL : WeatherType.CLEAR, false);
    }

    public String toString() {
        return super.toString() + "(" + this.getName() + " at " + this.locX + "," + this.locY + "," + this.locZ + ")";
    }

    public void forceSetPositionRotation(double x, double y, double z, float yaw, float pitch) {
        this.setPositionRotation(x, y, z, yaw, pitch);
        this.playerConnection.syncPosition();
    }

    protected boolean isFrozen() {
        return super.isFrozen() || (this.playerConnection != null && this.playerConnection.isDisconnected());
    }

    public Scoreboard getScoreboard() {
        return this.getBukkitEntity().getScoreboard().getHandle();
    }

    public void reset() {
        float exp = 0.0f;
        boolean keepInventory = this.world.getGameRules().getBoolean("keepInventory");
        if (this.keepLevel || keepInventory) {
            exp = this.exp;
            this.newTotalExp = this.expTotal;
            this.newLevel = this.expLevel;
        }
        this.setHealth(this.getMaxHealth());
        this.fireTicks = 0;
        this.fallDistance = 0.0f;
        this.foodData = new FoodMetaData((EntityHuman) this);
        this.expLevel = this.newLevel;
        this.expTotal = this.newTotalExp;
        this.exp = 0.0f;
        this.setArrowCount(this.deathTicks = 0);
        this.removeAllEffects();
        this.updateEffects = true;
        this.activeContainer = this.defaultContainer;
        this.killer = null;
        this.lastDamager = null;
        this.combatTracker = new CombatTracker((EntityLiving) this);
        this.lastSentExp = -1;
        if (this.keepLevel || keepInventory) {
            this.exp = exp;
        } else {
            this.giveExp(this.newExp);
        }
        this.keepLevel = false;
    }

    public CraftPlayer getBukkitEntity() {
        return (CraftPlayer) super.getBukkitEntity();
    }

    static {
        bV = LogManager.getLogger();
    }
}

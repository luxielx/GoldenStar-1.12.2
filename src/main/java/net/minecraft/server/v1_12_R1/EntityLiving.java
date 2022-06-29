package net.minecraft.server.v1_12_R1;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.attribute.CraftAttributeMap;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public abstract class EntityLiving extends Entity {
    private static final Logger a = LogManager.getLogger();
    private static final UUID b = UUID.fromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");
    private static final AttributeModifier c = new AttributeModifier(b, "Sprinting speed boost", 0.30000001192092896, 2)
            .a(false);
    protected static final DataWatcherObject<Byte> at = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.a);
    public static final DataWatcherObject<Float> HEALTH = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.c);
    private static final DataWatcherObject<Integer> g = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.b);
    private static final DataWatcherObject<Boolean> h = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.h);
    private static final DataWatcherObject<Integer> br = DataWatcher.a(EntityLiving.class, DataWatcherRegistry.b);
    private AttributeMapBase attributeMap;
    public CombatTracker combatTracker = new CombatTracker(this);
    public final Map<MobEffectList, MobEffect> effects = Maps.newConcurrentMap();
    private final NonNullList<ItemStack> bv;
    private final NonNullList<ItemStack> bw;
    public boolean au;
    public EnumHand av;
    public int aw;
    public int ax;
    public int hurtTicks;
    public int az;
    public float aA;
    public int deathTicks;
    public float aC;
    public float aD;
    protected int aE;
    public float aF;
    public float aG;
    public float aH;
    public int maxNoDamageTicks;
    public float aJ;
    public float aK;
    public float aL;
    public float aM;
    public float aN;
    public float aO;
    public float aP;
    public float aQ;
    public float aR;
    public EntityHuman killer;
    public int lastDamageByPlayerTime;
    protected boolean aU;
    protected int ticksFarFromPlayer;
    protected float aW;
    protected float aX;
    protected float aY;
    protected float aZ;
    protected float ba;
    protected int bb;
    public float lastDamage;
    protected boolean bd;
    public float be;
    public float bf;
    public float bg;
    public float bh;
    protected int bi;
    protected double bj;
    protected double bk;
    protected double bl;
    protected double bm;
    protected double bn;
    public boolean updateEffects;
    public EntityLiving lastDamager;
    public int hurtTimestamp;
    private EntityLiving bA;
    private int bB;
    private float bC;
    private int bD;
    private float bE;
    protected ItemStack activeItem;
    protected int bp;
    protected int bq;
    private BlockPosition bF;
    private DamageSource bG;
    private long bH;
    public int expToDrop;
    public int maxAirTicks = 300;
    boolean forceDrops;
    ArrayList<org.bukkit.inventory.ItemStack> drops = new ArrayList<org.bukkit.inventory.ItemStack>();
    public CraftAttributeMap craftAttributes;
    public boolean collides = true;
    public boolean canPickUpLoot;
    public boolean silentDeath = false;
    private boolean isTickingEffects = false;
    private List<Object> effectsToProcess = Collections.synchronizedList(Lists.newArrayList());
    public int shieldBlockingDelay;

    protected void setDying(boolean dying) {
        this.aU = dying;
    }

    protected boolean isDying() {
        return this.aU;
    }

    protected int getKillCount() {
        return this.bb;
    }

    public float getBukkitYaw() {
        return this.getHeadRotation();
    }

    public void inactiveTick() {
        super.inactiveTick();
        ++this.ticksFarFromPlayer;
    }

    public void killEntity() {
        this.damageEntity(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
    }

    public EntityLiving(World world) {
        super(world);
        this.shieldBlockingDelay = this.world.paperConfig.shieldBlockingDelay;
        this.bv = NonNullList.a(2, ItemStack.a);
        this.bw = NonNullList.a(4, ItemStack.a);
        this.maxNoDamageTicks = 20;
        this.aR = 0.02f;
        this.updateEffects = true;
        this.activeItem = ItemStack.a;
        this.initAttributes();
        this.datawatcher.set(HEALTH,
                Float.valueOf((float) this.getAttributeInstance(GenericAttributes.maxHealth).getValue()));
        this.i = true;
        this.aM = (float) ((Math.random() + 1.0) * 0.009999999776482582);
        this.setPosition(this.locX, this.locY, this.locZ);
        this.aL = (float) Math.random() * 12398.0f;
        this.aP = this.yaw = (float) (Math.random() * 6.2831854820251465);
        this.P = 0.6f;
    }

    protected void i() {
        this.datawatcher.register(at, (byte) 0);
        this.datawatcher.register(g, 0);
        this.datawatcher.register(h, false);
        this.datawatcher.register(br, 0);
        this.datawatcher.register(HEALTH, Float.valueOf(1.0f));
    }

    protected void initAttributes() {
        this.getAttributeMap().b(GenericAttributes.maxHealth);
        this.getAttributeMap().b(GenericAttributes.c);
        this.getAttributeMap().b(GenericAttributes.MOVEMENT_SPEED);
        this.getAttributeMap().b(GenericAttributes.h);
        this.getAttributeMap().b(GenericAttributes.i);
    }

    protected void a(double d0, boolean flag, IBlockData iblockdata, BlockPosition blockposition) {
        if (!this.isInWater()) {
            this.aq();
        }
        if (!this.world.isClientSide && this.fallDistance > 3.0f && flag) {
            float f = MathHelper.f((float) (this.fallDistance - 3.0f));
            if (iblockdata.getMaterial() != Material.AIR) {
                double d1 = Math.min((double) (0.2f + f / 15.0f), 2.5);
                int i = (int) (150 * d1);
                if (this instanceof EntityPlayer) {
                    ((WorldServer) this.world).sendParticles((EntityPlayer) this, EnumParticle.BLOCK_DUST, false,
                            this.locX, this.locY, this.locZ, i, 0.0, 0.0, 0.0, 0.15000000596046448,
                            Block.getCombinedId(iblockdata));
                } else {
                    ((WorldServer) this.world).a(EnumParticle.BLOCK_DUST, this.locX, this.locY, this.locZ, i, 0.0, 0.0,
                            0.0, 0.15000000596046448, Block.getCombinedId(iblockdata));
                }
            }
        }
        super.a(d0, flag, iblockdata, blockposition);
    }

    public boolean canBreatheUnderwater() {
        return this.bN();
    }

    public boolean bN() {
        return false;
    }

    public void Y() {
        boolean flag1;
        this.aC = this.aD;
        super.Y();
        this.world.methodProfiler.a("livingEntityBaseTick");
        boolean flag = this instanceof EntityHuman;
        if (this.isAlive()) {
            double d0;
            double d1;
            if (this.inBlock()) {
                this.damageEntity(DamageSource.STUCK, 1.0f);
            } else if (flag && !this.world.getWorldBorder().a(this.getBoundingBox())
                    && (d0 = this.world.getWorldBorder().a((Entity) this)
                    + this.world.getWorldBorder().getDamageBuffer()) < 0.0
                    && (d1 = this.world.getWorldBorder().getDamageAmount()) > 0.0) {
                this.damageEntity(DamageSource.STUCK, Math.max(1, MathHelper.floor((double) (-d0 * d1))));
            }
        }
        if (this.isFireProof() || this.world.isClientSide) {
            this.extinguish();
        }
        boolean bl = flag1 = flag && ((EntityHuman) this).abilities.isInvulnerable;
        if (this.isAlive()) {
            BlockPosition blockposition;
            if (this.a(Material.WATER)) {
                if (!(this.canBreatheUnderwater() || this.hasEffect(MobEffects.WATER_BREATHING) || flag1)) {
                    this.setAirTicks(this.d(this.getAirTicks()));
                    if (this.getAirTicks() == -20) {
                        this.setAirTicks(0);
                        for (int i = 0; i < 8; ++i) {
                            float f = this.random.nextFloat() - this.random.nextFloat();
                            float f1 = this.random.nextFloat() - this.random.nextFloat();
                            float f2 = this.random.nextFloat() - this.random.nextFloat();
                            this.world.addParticle(EnumParticle.WATER_BUBBLE, this.locX + (double) f,
                                    this.locY + (double) f1, this.locZ + (double) f2, this.motX, this.motY, this.motZ,
                                    new int[0]);
                        }
                        this.damageEntity(DamageSource.DROWN, 2.0f);
                    }
                }
                if (!this.world.isClientSide && this.isPassenger() && this.bJ() instanceof EntityLiving) {
                    this.stopRiding();
                }
            } else if (this.getAirTicks() != 300) {
                this.setAirTicks(this.maxAirTicks);
            }
            if (!this.world.isClientSide
                    && !Objects.equal(this.bF, (blockposition = new BlockPosition((Entity) this)))) {
                this.bF = blockposition;
                this.b(blockposition);
            }
        }
        if (this.isAlive() && this.an()) {
            this.extinguish();
        }
        this.aJ = this.aK;
        if (this.hurtTicks > 0) {
            --this.hurtTicks;
        }
        if (this.noDamageTicks > 0 && !(this instanceof EntityPlayer)) {
            --this.noDamageTicks;
        }
        if (this.getHealth() <= 0.0f) {
            this.bO();
        }
        if (this.lastDamageByPlayerTime > 0) {
            --this.lastDamageByPlayerTime;
        } else {
            this.killer = null;
        }
        if (this.bA != null && !this.bA.isAlive()) {
            this.bA = null;
        }
        if (this.lastDamager != null) {
            if (!this.lastDamager.isAlive()) {
                this.a((EntityLiving) null);
            } else if (this.ticksLived - this.hurtTimestamp > 100) {
                this.a((EntityLiving) null);
            }
        }
        this.tickPotionEffects();
        this.aZ = this.aY;
        this.aO = this.aN;
        this.aQ = this.aP;
        this.lastYaw = this.yaw;
        this.lastPitch = this.pitch;
        this.world.methodProfiler.b();
    }

    public int getExpReward() {
        int exp = this.getExpValue(this.killer);
        if (!this.world.isClientSide && (this.lastDamageByPlayerTime > 0 || this.alwaysGivesExp())
                && this.isDropExperience() && this.world.getGameRules().getBoolean("doMobLoot")) {
            return exp;
        }
        return 0;
    }

    protected void b(BlockPosition blockposition) {
        int i = EnchantmentManager.a((Enchantment) Enchantments.j, (EntityLiving) this);
        if (i > 0) {
            EnchantmentFrostWalker.a((EntityLiving) this, (World) this.world, (BlockPosition) blockposition, i);
        }
    }

    public boolean isBaby() {
        return false;
    }

    protected void bO() {
        ++this.deathTicks;
        if (this.deathTicks >= 20 && !this.dead) {
            int i;
            int j;
            for (i = this.expToDrop; i > 0; i -= j) {
                j = EntityExperienceOrb.getOrbValue(i);
                Object attacker = this.killer != null ? this.killer : this.lastDamager;
                this.world
                        .addEntity(
                                (Entity) new EntityExperienceOrb(this.world, this.locX, this.locY, this.locZ, j,
                                        this instanceof EntityPlayer ? ExperienceOrb.SpawnReason.PLAYER_DEATH
                                                : ExperienceOrb.SpawnReason.ENTITY_DEATH,
                                        (Entity) attacker, (Entity) this));
            }
            this.expToDrop = 0;
            this.die();
            for (i = 0; i < 20; ++i) {
                double d0 = this.random.nextGaussian() * 0.02;
                double d1 = this.random.nextGaussian() * 0.02;
                double d2 = this.random.nextGaussian() * 0.02;
                this.world.addParticle(EnumParticle.EXPLOSION_NORMAL,
                        this.locX + (double) (this.random.nextFloat() * this.width * 2.0f) - (double) this.width,
                        this.locY + (double) (this.random.nextFloat() * this.length),
                        this.locZ + (double) (this.random.nextFloat() * this.width * 2.0f) - (double) this.width, d0,
                        d1, d2, new int[0]);
            }
        }
    }

    protected boolean isDropExperience() {
        return !this.isBaby();
    }

    protected int d(int i) {
        int j = EnchantmentManager.getOxygenEnchantmentLevel((EntityLiving) this);
        return j > 0 && this.random.nextInt(j + 1) > 0 ? i : i - 1;
    }

    protected int getExpValue(EntityHuman entityhuman) {
        return 0;
    }

    protected boolean alwaysGivesExp() {
        return false;
    }

    public Random getRandom() {
        return this.random;
    }

    @Nullable
    public EntityLiving getLastDamager() {
        return this.lastDamager;
    }

    public int bT() {
        return this.hurtTimestamp;
    }

    public void a(@Nullable EntityLiving entityliving) {
        this.lastDamager = entityliving;
        this.hurtTimestamp = this.ticksLived;
    }

    public EntityLiving bU() {
        return this.bA;
    }

    public int bV() {
        return this.bB;
    }

    public void z(Entity entity) {
        this.bA = entity instanceof EntityLiving ? (EntityLiving) entity : null;
        this.bB = this.ticksLived;
    }

    public int bW() {
        return this.ticksFarFromPlayer;
    }

    protected void a_(ItemStack itemstack) {
        if (!itemstack.isEmpty()) {
            SoundEffect soundeffect = SoundEffects.q;
            Item item = itemstack.getItem();
            if (item instanceof ItemArmor) {
                soundeffect = ((ItemArmor) item).d().b();
            } else if (item == Items.cS) {
                soundeffect = SoundEffects.p;
            }
            this.a(soundeffect, 1.0f, 1.0f);
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        ItemStack itemstack;
        nbttagcompound.setFloat("Health", this.getHealth());
        nbttagcompound.setShort("HurtTime", (short) this.hurtTicks);
        nbttagcompound.setInt("HurtByTimestamp", this.hurtTimestamp);
        nbttagcompound.setShort("DeathTime", (short) this.deathTicks);
        nbttagcompound.setFloat("AbsorptionAmount", this.getAbsorptionHearts());
        for (EnumItemSlot enumitemslot : EnumItemSlot.values()) {
            itemstack = this.getEquipment(enumitemslot);
            if (itemstack.isEmpty())
                continue;
            this.getAttributeMap().a(itemstack.a(enumitemslot));
        }
        nbttagcompound.set("Attributes", (NBTBase) GenericAttributes.a((AttributeMapBase) this.getAttributeMap()));
        for (EnumItemSlot enumitemslot : EnumItemSlot.values()) {
            itemstack = this.getEquipment(enumitemslot);
            if (itemstack.isEmpty())
                continue;
            this.getAttributeMap().b(itemstack.a(enumitemslot));
        }
        if (!this.effects.isEmpty()) {
            NBTTagList nbttaglist = new NBTTagList();
            for (MobEffect mobeffect : this.effects.values()) {
                nbttaglist.add((NBTBase) mobeffect.a(new NBTTagCompound()));
            }
            nbttagcompound.set("ActiveEffects", (NBTBase) nbttaglist);
        }
        nbttagcompound.setBoolean("FallFlying", this.cP());
    }

    public void a(NBTTagCompound nbttagcompound) {
        float absorptionAmount = nbttagcompound.getFloat("AbsorptionAmount");
        if (Float.isNaN(absorptionAmount)) {
            absorptionAmount = 0.0f;
        }
        this.setAbsorptionHearts(absorptionAmount);
        if (nbttagcompound.hasKeyOfType("Attributes", 9) && this.world != null && !this.world.isClientSide) {
            GenericAttributes.a((AttributeMapBase) this.getAttributeMap(),
                    (NBTTagList) nbttagcompound.getList("Attributes", 10));
        }
        if (nbttagcompound.hasKeyOfType("ActiveEffects", 9)) {
            NBTTagList nbttaglist = nbttagcompound.getList("ActiveEffects", 10);
            for (int i = 0; i < nbttaglist.size(); ++i) {
                NBTTagCompound nbttagcompound1 = nbttaglist.get(i);
                MobEffect mobeffect = MobEffect.b((NBTTagCompound) nbttagcompound1);
                if (mobeffect == null)
                    continue;
                this.effects.put(mobeffect.getMobEffect(), mobeffect);
            }
        }
        if (nbttagcompound.hasKey("Bukkit.MaxHealth")) {
            NBTBase nbtbase = nbttagcompound.get("Bukkit.MaxHealth");
            if (nbtbase.getTypeId() == 5) {
                this.getAttributeInstance(GenericAttributes.maxHealth).setValue(((NBTTagFloat) nbtbase).asDouble());
            } else if (nbtbase.getTypeId() == 3) {
                this.getAttributeInstance(GenericAttributes.maxHealth).setValue(((NBTTagInt) nbtbase).asDouble());
            }
        }
        if (nbttagcompound.hasKeyOfType("Health", 99)) {
            this.setHealth(nbttagcompound.getFloat("Health"));
        }
        this.hurtTicks = nbttagcompound.getShort("HurtTime");
        this.deathTicks = nbttagcompound.getShort("DeathTime");
        this.hurtTimestamp = nbttagcompound.getInt("HurtByTimestamp");
        if (nbttagcompound.hasKeyOfType("Team", 8)) {
            String s = nbttagcompound.getString("Team");
            boolean flag = this.world.getScoreboard().addPlayerToTeam(this.bn(), s);
            if (!flag) {
                a.warn("Unable to add mob to team \"" + s + "\" (that team probably doesn't exist)");
            }
        }
        if (nbttagcompound.getBoolean("FallFlying")) {
            this.setFlag(7, true);
        }
    }

    protected void tickPotionEffects() {
        Iterator<MobEffectList> iterator = this.effects.keySet().iterator();
        this.isTickingEffects = true;
        try {
            while (iterator.hasNext()) {
                MobEffectList mobeffectlist = iterator.next();
                MobEffect mobeffect = this.effects.get(mobeffectlist);
                if (!mobeffect.tick(this)) {
                    if (this.world.isClientSide)
                        continue;
                    iterator.remove();
                    this.b(mobeffect);
                    continue;
                }
                if (mobeffect.getDuration() % 600 != 0)
                    continue;
                this.a(mobeffect, false);
            }
        } catch (ConcurrentModificationException mobeffectlist) {
            // empty catch block
        }
        this.isTickingEffects = false;
        synchronized (effectsToProcess) {
            for (Object e : this.effectsToProcess) {
                if (e instanceof MobEffect) {
                    this.addEffect((MobEffect) e);
                    continue;
                }
                this.removeEffect((MobEffectList) e);
            }
        }
        this.effectsToProcess.clear();
        if (this.updateEffects) {
            if (!this.world.isClientSide) {
                this.G();
            }
            this.updateEffects = false;
        }
        int i = this.datawatcher.get(g);
        boolean flag = this.datawatcher.get(h);
        if (i > 0) {
            boolean flag1 = this.isInvisible() ? this.random.nextInt(15) == 0 : this.random.nextBoolean();
            if (flag) {
                flag1 &= this.random.nextInt(5) == 0;
            }
            if (flag1 && i > 0) {
                double d0 = (double) (i >> 16 & 255) / 255.0;
                double d1 = (double) (i >> 8 & 255) / 255.0;
                double d2 = (double) (i >> 0 & 255) / 255.0;
                this.world.addParticle(flag ? EnumParticle.SPELL_MOB_AMBIENT : EnumParticle.SPELL_MOB,
                        this.locX + (this.random.nextDouble() - 0.5) * (double) this.width,
                        this.locY + this.random.nextDouble() * (double) this.length,
                        this.locZ + (this.random.nextDouble() - 0.5) * (double) this.width, d0, d1, d2, new int[0]);
            }
        }
    }

    protected void G() {
        if (this.effects.isEmpty()) {
            this.bY();
            this.setInvisible(false);
        } else {
            Collection<MobEffect> collection = this.effects.values();
            this.datawatcher.set(h, EntityLiving.a(collection));
            this.datawatcher.set(g, PotionUtil.a(collection));
            this.setInvisible(this.hasEffect(MobEffects.INVISIBILITY));
        }
    }

    public static boolean a(Collection<MobEffect> collection) {
        MobEffect mobeffect;
        Iterator<MobEffect> iterator = collection.iterator();
        do {
            if (iterator.hasNext())
                continue;
            return true;
        } while ((mobeffect = iterator.next()).isAmbient());
        return false;
    }

    protected void bY() {
        this.datawatcher.set(h, false);
        this.datawatcher.set(g, 0);
    }

    public void removeAllEffects() {
        if (!this.world.isClientSide) {
            Iterator<MobEffect> iterator = this.effects.values().iterator();
            while (iterator.hasNext()) {
                this.b(iterator.next());
                iterator.remove();
            }
        }
    }

    public Collection<MobEffect> getEffects() {
        return this.effects.values();
    }

    public Map<MobEffectList, MobEffect> cb() {
        return this.effects;
    }

    public boolean hasEffect(MobEffectList mobeffectlist) {
        return this.effects.containsKey(mobeffectlist);
    }

    @Nullable
    public MobEffect getEffect(MobEffectList mobeffectlist) {
        return this.effects.get(mobeffectlist);
    }

    public void addEffect(MobEffect mobeffect) {
//		AsyncCatcher.catchOp((String) "effect add");
        if (this.isTickingEffects) {
            this.effectsToProcess.add(mobeffect);
            return;
        }
        if (this.d(mobeffect)) {
            MobEffect mobeffect1 = this.effects.get(mobeffect.getMobEffect());
            if (mobeffect1 == null) {
                this.effects.put(mobeffect.getMobEffect(), mobeffect);
                this.a(mobeffect);
            } else {
                mobeffect1.a(mobeffect);
                this.a(mobeffect1, true);
            }
        }
    }

    public boolean d(MobEffect mobeffect) {
        MobEffectList mobeffectlist;
        return this.getMonsterType() != EnumMonsterType.UNDEAD
                || (mobeffectlist = mobeffect.getMobEffect()) != MobEffects.REGENERATION
                && mobeffectlist != MobEffects.POISON;
    }

    public boolean cc() {
        return this.getMonsterType() == EnumMonsterType.UNDEAD;
    }

    @Nullable
    public MobEffect c(@Nullable MobEffectList mobeffectlist) {
        if (this.isTickingEffects) {
            this.effectsToProcess.add(mobeffectlist);
            return null;
        }
        return this.effects.remove(mobeffectlist);
    }

    public void removeEffect(MobEffectList mobeffectlist) {
        MobEffect mobeffect = this.c(mobeffectlist);
        if (mobeffect != null) {
            this.b(mobeffect);
        }
    }

    protected void a(MobEffect mobeffect) {
        this.updateEffects = true;
        if (!this.world.isClientSide) {
            mobeffect.getMobEffect().b(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }
    }

    protected void a(MobEffect mobeffect, boolean flag) {
        this.updateEffects = true;
        if (flag && !this.world.isClientSide) {
            MobEffectList mobeffectlist = mobeffect.getMobEffect();
            mobeffectlist.a(this, this.getAttributeMap(), mobeffect.getAmplifier());
            mobeffectlist.b(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }
    }

    protected void b(MobEffect mobeffect) {
        this.updateEffects = true;
        if (!this.world.isClientSide) {
            mobeffect.getMobEffect().a(this, this.getAttributeMap(), mobeffect.getAmplifier());
        }
    }

    public void heal(float f) {
        this.heal(f, EntityRegainHealthEvent.RegainReason.CUSTOM);
    }

    public void heal(float f, EntityRegainHealthEvent.RegainReason regainReason) {
        this.heal(f, regainReason, false);
    }

    public void heal(float f, EntityRegainHealthEvent.RegainReason regainReason, boolean isFastRegen) {
        float f1 = this.getHealth();
        if (f1 > 0.0f) {
            EntityRegainHealthEvent event = new EntityRegainHealthEvent(
                    (org.bukkit.entity.Entity) this.getBukkitEntity(), (double) f, regainReason, isFastRegen);
            this.world.getServer().getPluginManager().callEvent((Event) event);
            if (!event.isCancelled()) {
                this.setHealth((float) ((double) this.getHealth() + event.getAmount()));
            }
        }
    }

    public final float getHealth() {
        if (this instanceof EntityPlayer) {
            return (float) ((EntityPlayer) this).getBukkitEntity().getHealth();
        }
        return (this.datawatcher.get(HEALTH)).floatValue();
    }

    public void setHealth(float f) {
        if (Float.isNaN(f)) {
            f = this.getMaxHealth();
            if (this.valid) {
                System.err.println("[NAN-HEALTH] " + this.getName() + " had NaN health set");
            }
        }
        if (this instanceof EntityPlayer) {
            CraftPlayer player = ((EntityPlayer) this).getBukkitEntity();
            if (f < 0.0f) {
                player.setRealHealth(0.0);
            } else if ((double) f > player.getMaxHealth()) {
                player.setRealHealth(player.getMaxHealth());
            } else {
                player.setRealHealth((double) f);
            }
            player.updateScaledHealth();
            return;
        }
        this.datawatcher.set(HEALTH, Float.valueOf(MathHelper.a((float) f, (float) 0.0f, (float) this.getMaxHealth())));
    }

    public boolean damageEntity(DamageSource damagesource, float f) {
        boolean flag2;
        boolean knockbackCancelled;
        if (this.isInvulnerable(damagesource)) {
            return false;
        }
        if (this.world.isClientSide) {
            return false;
        }
        this.ticksFarFromPlayer = 0;
        if (this.getHealth() <= 0.0f) {
            return false;
        }
        if (damagesource.o() && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }
        float f1 = f;
        boolean flag = f > 0.0f && this.applyBlockingModifier(damagesource);
        this.aG = 1.5f;
        boolean flag1 = true;
        if ((float) this.noDamageTicks > (float) this.maxNoDamageTicks / 2.0f) {
            if (f <= this.lastDamage) {
                this.forceExplosionKnockback = true;
                return false;
            }
            if (!this.damageEntity0(damagesource, f - this.lastDamage)) {
                return false;
            }
            this.lastDamage = f;
            flag1 = false;
        } else {
            if (!this.damageEntity0(damagesource, f)) {
                return false;
            }
            this.lastDamage = f;
            this.noDamageTicks = this.maxNoDamageTicks;
            this.hurtTicks = this.az = 10;
        }
        if (this instanceof EntityAnimal) {
            ((EntityAnimal) this).resetLove();
            if (this instanceof EntityTameableAnimal) {
                ((EntityTameableAnimal) this).getGoalSit().setSitting(false);
            }
        }
        this.aA = 0.0f;
        Entity entity1 = damagesource.getEntity();
        if (entity1 != null) {
            EntityWolf entitywolf;
            if (entity1 instanceof EntityLiving) {
                this.a((EntityLiving) entity1);
            }
            if (entity1 instanceof EntityHuman) {
                this.lastDamageByPlayerTime = 100;
                this.killer = (EntityHuman) entity1;
            } else if (entity1 instanceof EntityWolf && (entitywolf = (EntityWolf) entity1).isTamed()) {
                this.lastDamageByPlayerTime = 100;
                this.killer = null;
            }
        }
        boolean bl = knockbackCancelled = this.world.paperConfig.disableExplosionKnockback && damagesource.isExplosion()
                && this instanceof EntityHuman;
        if (flag1) {
            if (flag) {
                this.world.broadcastEntityEffect((Entity) this, (byte) 29);
            } else if (damagesource instanceof EntityDamageSource && ((EntityDamageSource) damagesource).x()) {
                this.world.broadcastEntityEffect((Entity) this, (byte) 33);
            } else {
                byte b0 = damagesource == DamageSource.DROWN ? (byte) 36 : (damagesource.o() ? (byte) 37 : 2);
                if (!knockbackCancelled) {
                    this.world.broadcastEntityEffect((Entity) this, b0);
                }
            }
            if (damagesource != DamageSource.DROWN && (!flag || f > 0.0f)) {
                this.ax();
            }
            if (entity1 != null) {
                double d0 = entity1.locX - this.locX;
                double d1 = entity1.locZ - this.locZ;
                while (d0 * d0 + d1 * d1 < 1.0E-4) {
                    d0 = (Math.random() - Math.random()) * 0.01;
                    d1 = (Math.random() - Math.random()) * 0.01;
                }
                this.aA = (float) (MathHelper.c((double) d1, (double) d0) * 57.2957763671875 - (double) this.yaw);
                this.a(entity1, 0.4f, d0, d1);
            } else {
                this.aA = (float) ((Math.random() * 2.0) * 180);
            }
        }
        if (knockbackCancelled) {
            this.world.broadcastEntityEffect((Entity) this, (byte) 2);
        }
        if (this.getHealth() <= 0.0f) {
            if (!this.e(damagesource)) {
                this.silentDeath = !flag1;
                this.die(damagesource);
                this.silentDeath = false;
            }
        } else if (flag1) {
            this.c(damagesource);
        }
        boolean bl2 = flag2 = !flag || f > 0.0f;
        if (flag2) {
            this.bG = damagesource;
            this.bH = this.world.getTime();
        }
        if (this instanceof EntityPlayer) {
            CriterionTriggers.h.a((EntityPlayer) this, damagesource, f1, f, flag);
        }
        if (entity1 instanceof EntityPlayer) {
            CriterionTriggers.g.a((EntityPlayer) entity1, (Entity) this, damagesource, f1, f, flag);
        }
        return flag2;
    }

    protected void c(EntityLiving entityliving) {
        entityliving.a(this, 0.5f, this.locX - entityliving.locX, this.locZ - entityliving.locZ);
    }

    private boolean e(DamageSource damagesource) {
        if (damagesource.ignoresInvulnerability()) {
            return false;
        }
        ItemStack itemstack = null;
        EnumHand[] aenumhand = EnumHand.values();
        int i = aenumhand.length;
        ItemStack itemstack1 = ItemStack.a;
        for (int j = 0; j < i; ++j) {
            EnumHand enumhand = aenumhand[j];
            itemstack1 = this.b(enumhand);
            if (itemstack1.getItem() != Items.cY)
                continue;
            itemstack = itemstack1.cloneItemStack();
            break;
        }
        EntityResurrectEvent event = new EntityResurrectEvent((LivingEntity) this.getBukkitEntity());
        event.setCancelled(itemstack == null);
        this.world.getServer().getPluginManager().callEvent((Event) event);
        if (!event.isCancelled()) {
            if (!itemstack1.isEmpty()) {
                itemstack1.subtract(1);
            }
            if (itemstack != null && this instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) this;
                entityplayer.b(StatisticList.b((Item) Items.cY));
                CriterionTriggers.A.a(entityplayer, itemstack);
            }
            this.setHealth(1.0f);
            this.removeAllEffects();
            this.addEffect(new MobEffect(MobEffects.REGENERATION, 900, 1));
            this.addEffect(new MobEffect(MobEffects.ABSORBTION, 100, 1));
            this.world.broadcastEntityEffect((Entity) this, (byte) 35);
        }
        return !event.isCancelled();
    }

    @Nullable
    public DamageSource ce() {
        if (this.world.getTime() - this.bH > 40L) {
            this.bG = null;
        }
        return this.bG;
    }

    protected void c(DamageSource damagesource) {
        SoundEffect soundeffect = this.d(damagesource);
        if (soundeffect != null) {
            this.a(soundeffect, this.cq(), this.cr());
        }
    }

    private boolean applyBlockingModifier(DamageSource damagesource) {
        Vec3D vec3d;
        if (!damagesource.ignoresArmor() && this.isBlocking() && (vec3d = damagesource.v()) != null) {
            Vec3D vec3d1 = this.e(1.0f);
            Vec3D vec3d2 = vec3d.a(new Vec3D(this.locX, this.locY, this.locZ)).a();
            vec3d2 = new Vec3D(vec3d2.x, 0.0, vec3d2.z);
            if (vec3d2.b(vec3d1) < 0.0) {
                return true;
            }
        }
        return false;
    }

    public void b(ItemStack itemstack) {
        this.a(SoundEffects.dw, 0.8f, 0.8f + this.world.random.nextFloat() * 0.4f);
        for (int i = 0; i < 5; ++i) {
            Vec3D vec3d = new Vec3D(((double) this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0);
            vec3d = vec3d.a(-this.pitch * 0.017453292f);
            vec3d = vec3d.b(-this.yaw * 0.017453292f);
            double d0 = (double) (-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3D vec3d1 = new Vec3D(((double) this.random.nextFloat() - 0.5) * 0.3, d0, 0.6);
            vec3d1 = vec3d1.a(-this.pitch * 0.017453292f);
            vec3d1 = vec3d1.b(-this.yaw * 0.017453292f);
            vec3d1 = vec3d1.add(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ);
            this.world.addParticle(EnumParticle.ITEM_CRACK, vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x, vec3d.y + 0.05,
                    vec3d.z, new int[]{Item.getId((Item) itemstack.getItem())});
        }
    }

    public void die(DamageSource damagesource) {
        if (!this.aU) {
            Entity entity = damagesource.getEntity();
            EntityLiving entityliving = this.ci();
            this.aU = true;
            EntityDeathEvent deathEvent = null;
            if (!this.world.isClientSide) {
                int i = 0;
                if (entity instanceof EntityHuman) {
                    i = EnchantmentManager.g((EntityLiving) ((EntityLiving) entity));
                }
                if (this.isDropExperience() && this.world.getGameRules().getBoolean("doMobLoot")) {
                    boolean flag = this.lastDamageByPlayerTime > 0;
                    this.a(flag, i, damagesource);
                    deathEvent = CraftEventFactory.callEntityDeathEvent((EntityLiving) this, this.drops);
                    this.drops = new ArrayList();
                } else {
                    deathEvent = CraftEventFactory.callEntityDeathEvent((EntityLiving) this);
                }
            }
            if (deathEvent == null || !deathEvent.isCancelled()) {
                if (this.getKillCount() >= 0 && entityliving != null) {
                    entityliving.runKillTrigger((Entity) this, this.getKillCount(), damagesource);
                }
                if (entity != null) {
                    entity.onKill(this);
                }
                this.getCombatTracker().reset();
                this.setDying(true);
                this.world.broadcastEntityEffect((Entity) this, (byte) 3);
            } else {
                this.setDying(false);
                this.setHealth((float) deathEvent.getReviveHealth());
            }
        }
    }

    protected void a(boolean flag, int i, DamageSource damagesource) {
        this.dropDeathLoot(flag, i);
        this.dropEquipment(flag, i);
    }

    protected void dropEquipment(boolean flag, int i) {
    }

    public void a(Entity entity, float f, double d0, double d1) {
        if (this.random.nextDouble() >= this.getAttributeInstance(GenericAttributes.c).getValue()) {
            this.impulse = true;
            float f1 = MathHelper.sqrt((double) (d0 * d0 + d1 * d1));
            double oldMotX = this.motX;
            double oldMotY = this.motY;
            double oldMotZ = this.motZ;
            this.motX /= 2.0;
            this.motZ /= 2.0;
            this.motX -= d0 / (double) f1 * (double) f;
            this.motZ -= d1 / (double) f1 * (double) f;
            if (this.onGround) {
                this.motY /= 2.0;
                this.motY += (double) f;
                if (this.motY > 0.4000000059604645) {
                    this.motY = 0.4000000059604645;
                }
            }
            Vector delta = new Vector(this.motX - oldMotX, this.motY - oldMotY, this.motZ - oldMotZ);
            this.motX = oldMotX;
            this.motY = oldMotY;
            this.motZ = oldMotZ;
            if (entity == null || new EntityKnockbackByEntityEvent((LivingEntity) this.getBukkitEntity(),
                    (org.bukkit.entity.Entity) entity.getBukkitEntity(), f, delta).callEvent()) {
                this.motX += delta.getX();
                this.motY += delta.getY();
                this.motZ += delta.getZ();
            }
        }
    }

    @Nullable
    protected SoundEffect d(DamageSource damagesource) {
        return SoundEffects.bX;
    }

    @Nullable
    public SoundEffect getDeathSoundEffect() {
        return this.cf();
    }

    @Nullable
    protected SoundEffect cf() {
        return SoundEffects.bS;
    }

    protected SoundEffect e(int i) {
        return i > 4 ? SoundEffects.bQ : SoundEffects.bY;
    }

    protected void dropDeathLoot(boolean flag, int i) {
    }

    public boolean m_() {
        int i = MathHelper.floor((double) this.locX);
        int j = MathHelper.floor((double) this.getBoundingBox().b);
        int k = MathHelper.floor((double) this.locZ);
        if (this instanceof EntityHuman && ((EntityHuman) this).isSpectator()) {
            return false;
        }
        BlockPosition blockposition = new BlockPosition(i, j, k);
        IBlockData iblockdata = this.world.getType(blockposition);
        Block block = iblockdata.getBlock();
        return block != Blocks.LADDER && block != Blocks.VINE
                ? block instanceof BlockTrapdoor && this.a(blockposition, iblockdata)
                : true;
    }

    private boolean a(BlockPosition blockposition, IBlockData iblockdata) {
        IBlockData iblockdata1;
        return (Boolean) iblockdata.get((IBlockState) BlockTrapdoor.OPEN) != false
                && (iblockdata1 = this.world.getType(blockposition.down())).getBlock() == Blocks.LADDER && iblockdata1
                .get((IBlockState) BlockLadder.FACING) == iblockdata.get((IBlockState) BlockTrapdoor.FACING);
    }

    public boolean isAlive() {
        return !this.dead && this.getHealth() > 0.0f;
    }

    public void e(float f, float f1) {
        super.e(f, f1);
        MobEffect mobeffect = this.getEffect(MobEffects.JUMP);
        float f2 = mobeffect == null ? 0.0f : (float) (mobeffect.getAmplifier() + 1);
        int i = MathHelper.f((float) ((f - 3.0f - f2) * f1));
        if (i > 0) {
            if (!this.damageEntity(DamageSource.FALL, i)) {
                return;
            }
            this.a(this.e(i), 1.0f, 1.0f);
            int j = MathHelper.floor((double) this.locX);
            int k = MathHelper.floor((double) (this.locY - 0.20000000298023224));
            int l = MathHelper.floor((double) this.locZ);
            IBlockData iblockdata = this.world.getType(new BlockPosition(j, k, l));
            if (iblockdata.getMaterial() != Material.AIR) {
                SoundEffectType soundeffecttype = iblockdata.getBlock().getStepSound();
                this.a(soundeffecttype.g(), soundeffecttype.a() * 0.5f, soundeffecttype.b() * 0.75f);
            }
        }
    }

    public int getArmorStrength() {
        AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.h);
        return MathHelper.floor((double) attributeinstance.getValue());
    }

    protected void damageArmor(float f) {
    }

    protected void damageShield(float f) {
    }

    protected float applyArmorModifier(DamageSource damagesource, float f) {
        if (!damagesource.ignoresArmor()) {
            f = CombatMath.a((float) f, (float) this.getArmorStrength(),
                    (float) ((float) this.getAttributeInstance(GenericAttributes.i).getValue()));
        }
        return f;
    }

    protected float applyMagicModifier(DamageSource damagesource, float f) {
        if (damagesource.isStarvation()) {
            return f;
        }
        if (f <= 0.0f) {
            return 0.0f;
        }
        int i = EnchantmentManager.a(this.getArmorItems(), (DamageSource) damagesource);
        if (i > 0) {
            f = CombatMath.a((float) f, (float) i);
        }
        return f;
    }

    protected boolean damageEntity0(final DamageSource damagesource, float f) {
        if (!this.isInvulnerable(damagesource)) {
            Function<Double, Double> absorption;
            float absorptionModifier;
            boolean human = this instanceof EntityHuman;
            float originalDamage = f;
            Function<Double, Double> hardHat = new Function<Double, Double>() {

                public Double apply(Double f) {
                    if (!(damagesource != DamageSource.ANVIL && damagesource != DamageSource.FALLING_BLOCK
                            || EntityLiving.this.getEquipment(EnumItemSlot.HEAD).isEmpty())) {
                        return -(f - f * 0.75);
                    }
                    return -0.0;
                }
            };
            float hardHatModifier = (hardHat.apply((double) f)).floatValue();
            Function<Double, Double> blocking = new Function<Double, Double>() {

                public Double apply(Double f) {
                    return -(EntityLiving.this.applyBlockingModifier(damagesource) ? f : 0.0);
                }
            };
            float blockingModifier = (blocking.apply((double) (f += hardHatModifier))).floatValue();
            Function<Double, Double> armor = new Function<Double, Double>() {

                public Double apply(Double f) {
                    return -(f - (double) EntityLiving.this.applyArmorModifier(damagesource, f.floatValue()));
                }
            };
            float armorModifier = (armor.apply((double) (f += blockingModifier))).floatValue();
            Function<Double, Double> resistance = new Function<Double, Double>() {

                public Double apply(Double f) {
                    if (!damagesource.isStarvation() && EntityLiving.this.hasEffect(MobEffects.RESISTANCE)
                            && damagesource != DamageSource.OUT_OF_WORLD) {
                        int i = (EntityLiving.this.getEffect(MobEffects.RESISTANCE).getAmplifier() + 1) * 5;
                        int j = 25 - i;
                        float f1 = f.floatValue() * (float) j;
                        return -(f - (double) (f1 / 25.0f));
                    }
                    return -0.0;
                }
            };
            float resistanceModifier = (resistance.apply((double) (f += armorModifier))).floatValue();
            f += resistanceModifier;
            Function<Double, Double> magic = new Function<Double, Double>() {

                public Double apply(Double f) {
                    return -(f - (double) EntityLiving.this.applyMagicModifier(damagesource, f.floatValue()));
                }
            };
            float magicModifier = (magic.apply((double) f)).floatValue();
            try {
                EntityDamageEvent event = CraftEventFactory.handleLivingEntityDamageEvent((Entity) this,
                        (DamageSource) damagesource, (double) originalDamage, (double) hardHatModifier,
                        (double) blockingModifier, (double) armorModifier, (double) resistanceModifier,
                        (double) magicModifier,
                        (double) (absorptionModifier = ((absorption = new Function<Double, Double>() {

                            public Double apply(Double f) {
                                return -Math.max(
                                        f - Math.max(f - (double) EntityLiving.this.getAbsorptionHearts(), 0.0), 0.0);
                            }
                        }).apply((double) (f += magicModifier))).floatValue()), hardHat, blocking, armor, resistance,
                        magic, absorption);
                if (event.isCancelled()) {
                    return false;
                }

                f = (float) event.getFinalDamage();
                if ((damagesource == DamageSource.ANVIL || damagesource == DamageSource.FALLING_BLOCK)
                        && this.getEquipment(EnumItemSlot.HEAD) != null) {
                    this.getEquipment(EnumItemSlot.HEAD).damage((int) (event.getDamage() * 4.0
                            + (double) this.random.nextFloat() * event.getDamage() * 2.0), this);
                }
                if (!damagesource.ignoresArmor()) {
                    float armorDamage = (float) (event.getDamage()
                            + event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING)
                            + event.getDamage(EntityDamageEvent.DamageModifier.HARD_HAT));
                    this.damageArmor(armorDamage);
                }
                if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0.0) {
                    this.damageShield((float) (-event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING)));
                    Entity entity = damagesource.i();
                    if (entity instanceof EntityLiving) {
                        this.c((EntityLiving) entity);
                    }
                }
                absorptionModifier = (float) (-event.getDamage(EntityDamageEvent.DamageModifier.ABSORPTION));
                this.setAbsorptionHearts(Math.max(this.getAbsorptionHearts() - absorptionModifier, 0.0f));
                if (f > 0.0f || !human) {
                    if (human) {
                        ((EntityHuman) this).applyExhaustion(damagesource.getExhaustionCost());
                        if (f < 3.4028235E37f) {
                            ((EntityHuman) this).a(StatisticList.z, Math.round(f * 10.0f));
                        }
                    }
                    float f2 = this.getHealth();
                    this.setHealth(f2 - f);
                    this.getCombatTracker().trackDamage(damagesource, f2, f);
                    if (!human) {
                        this.setAbsorptionHearts(this.getAbsorptionHearts() - f);
                    }
                    return true;
                }
                if (event.getDamage(EntityDamageEvent.DamageModifier.BLOCKING) < 0.0) {
                    if (this instanceof EntityPlayer) {
                        CriterionTriggers.h.a((EntityPlayer) this, damagesource, f, originalDamage, true);
                    }
                    if (damagesource.getEntity() instanceof EntityPlayer) {
                        CriterionTriggers.g.a((EntityPlayer) damagesource.getEntity(), (Entity) this, damagesource, f,
                                originalDamage, true);
                    }
                    return false;
                }
            } catch (Exception e) {

            }
            return originalDamage > 0.0f;

        }
        return false;
    }

    public CombatTracker getCombatTracker() {
        return this.combatTracker;
    }

    @Nullable
    public EntityLiving ci() {
        return this.combatTracker.c() != null ? this.combatTracker.c()
                : (this.killer != null ? this.killer : (this.lastDamager != null ? this.lastDamager : null));
    }

    public final float getMaxHealth() {
        return (float) this.getAttributeInstance(GenericAttributes.maxHealth).getValue();
    }

    public final int getArrowCount() {
        return this.datawatcher.get(br);
    }

    public final void setArrowCount(int i) {
        this.datawatcher.set(br, i);
    }

    private int p() {
        return this.hasEffect(MobEffects.FASTER_DIG) ? 6 - (1 + this.getEffect(MobEffects.FASTER_DIG).getAmplifier())
                : (this.hasEffect(MobEffects.SLOWER_DIG)
                ? 6 + (1 + this.getEffect(MobEffects.SLOWER_DIG).getAmplifier()) * 2
                : 6);
    }

    public void a(EnumHand enumhand) {
        if (!this.au || this.aw >= this.p() / 2 || this.aw < 0) {
            this.aw = -1;
            this.au = true;
            this.av = enumhand;
            if (this.world instanceof WorldServer) {
                ((WorldServer) this.world).getTracker().a((Entity) this,
                        (Packet) new PacketPlayOutAnimation((Entity) this, enumhand == EnumHand.MAIN_HAND ? 0 : 3));
            }
        }
    }

    protected void ac() {
        this.damageEntity(DamageSource.OUT_OF_WORLD, 4.0f);
    }

    protected void cl() {
        int i = this.p();
        if (this.au) {
            ++this.aw;
            if (this.aw >= i) {
                this.aw = 0;
                this.au = false;
            }
        } else {
            this.aw = 0;
        }
        this.aD = (float) this.aw / (float) i;
    }

    public AttributeInstance getAttributeInstance(IAttribute iattribute) {
        return this.getAttributeMap().a(iattribute);
    }

    public AttributeMapBase getAttributeMap() {
        if (this.attributeMap == null) {
            this.attributeMap = new AttributeMapServer();
            this.craftAttributes = new CraftAttributeMap(this.attributeMap);
        }
        return this.attributeMap;
    }

    public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEFINED;
    }

    public ItemStack getItemInMainHand() {
        return this.getEquipment(EnumItemSlot.MAINHAND);
    }

    public ItemStack getItemInOffHand() {
        return this.getEquipment(EnumItemSlot.OFFHAND);
    }

    public ItemStack b(EnumHand enumhand) {
        if (enumhand == EnumHand.MAIN_HAND) {
            return this.getEquipment(EnumItemSlot.MAINHAND);
        }
        if (enumhand == EnumHand.OFF_HAND) {
            return this.getEquipment(EnumItemSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + enumhand);
    }

    public void a(EnumHand enumhand, ItemStack itemstack) {
        if (enumhand == EnumHand.MAIN_HAND) {
            this.setSlot(EnumItemSlot.MAINHAND, itemstack);
        } else {
            if (enumhand != EnumHand.OFF_HAND) {
                throw new IllegalArgumentException("Invalid hand " + enumhand);
            }
            this.setSlot(EnumItemSlot.OFFHAND, itemstack);
        }
    }

    public boolean a(EnumItemSlot enumitemslot) {
        return !this.getEquipment(enumitemslot).isEmpty();
    }

    public abstract Iterable<ItemStack> getArmorItems();

    public abstract ItemStack getEquipment(EnumItemSlot var1);

    public abstract void setSlot(EnumItemSlot var1, ItemStack var2);

    public void setSprinting(boolean flag) {
        super.setSprinting(flag);
        AttributeInstance attributeinstance = this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        if (attributeinstance.a(b) != null) {
            attributeinstance.c(c);
        }
        if (flag) {
            attributeinstance.b(c);
        }
    }

    public float getDeathSoundVolume() {
        return this.cq();
    }

    protected float cq() {
        return 1.0f;
    }

    public float getDeathSoundPitch() {
        return this.cr();
    }

    protected float cr() {
        return this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f
                : (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isFrozen() {
        return this.getHealth() <= 0.0f;
    }

    public void A(Entity entity) {
        if (!(entity instanceof EntityBoat) && !(entity instanceof EntityHorseAbstract)) {
            double d1 = entity.locX;
            double d2 = entity.getBoundingBox().b + (double) entity.length;
            double d0 = entity.locZ;
            EnumDirection enumdirection = entity.bu();
            if (enumdirection != null) {
                EnumDirection enumdirection1 = enumdirection.e();
                int[][] aint = new int[][]{{0, 1}, {0, -1}, {-1, 1}, {-1, -1}, {1, 1}, {1, -1}, {-1, 0},
                        {1, 0}, {0, 1}};
                double d3 = Math.floor(this.locX) + 0.5;
                double d4 = Math.floor(this.locZ) + 0.5;
                double d5 = this.getBoundingBox().d - this.getBoundingBox().a;
                double d6 = this.getBoundingBox().f - this.getBoundingBox().c;
                AxisAlignedBB axisalignedbb = new AxisAlignedBB(d3 - d5 / 2.0, entity.getBoundingBox().b, d4 - d6 / 2.0,
                        d3 + d5 / 2.0, Math.floor(entity.getBoundingBox().b) + (double) this.length, d4 + d6 / 2.0);
                int[][] aint1 = aint;
                int i = aint.length;
                for (int j = 0; j < i; ++j) {
                    int[] aint2 = aint1[j];
                    double d7 = enumdirection.getAdjacentX() * aint2[0] + enumdirection1.getAdjacentX() * aint2[1];
                    double d8 = enumdirection.getAdjacentZ() * aint2[0] + enumdirection1.getAdjacentZ() * aint2[1];
                    double d9 = d3 + d7;
                    double d10 = d4 + d8;
                    AxisAlignedBB axisalignedbb1 = axisalignedbb.d(d7, 0.0, d8);
                    if (!this.world.a(axisalignedbb1)) {
                        if (this.world.getType(new BlockPosition(d9, this.locY, d10)).q()) {
                            this.enderTeleportTo(d9, this.locY + 1.0, d10);
                            return;
                        }
                        BlockPosition blockposition = new BlockPosition(d9, this.locY - 1.0, d10);
                        if (!this.world.getType(blockposition).q()
                                && this.world.getType(blockposition).getMaterial() != Material.WATER)
                            continue;
                        d1 = d9;
                        d2 = this.locY + 1.0;
                        d0 = d10;
                        continue;
                    }
                    if (this.world.a(axisalignedbb1.d(0.0, 1.0, 0.0))
                            || !this.world.getType(new BlockPosition(d9, this.locY + 1.0, d10)).q())
                        continue;
                    d1 = d9;
                    d2 = this.locY + 2.0;
                    d0 = d10;
                }
            }
            this.enderTeleportTo(d1, d2, d0);
        } else {
            double d11 = (double) (this.width / 2.0f + entity.width / 2.0f) + 0.4;
            float f = entity instanceof EntityBoat ? 0.0f
                    : 1.5707964f * (float) (this.getMainHand() == EnumMainHand.RIGHT ? -1 : 1);
            float f1 = -MathHelper.sin((float) (-this.yaw * 0.017453292f - 3.1415927f + f));
            float f2 = -MathHelper.cos((float) (-this.yaw * 0.017453292f - 3.1415927f + f));
            double d0 = Math.abs(f1) > Math.abs(f2) ? d11 / (double) Math.abs(f1) : d11 / (double) Math.abs(f2);
            double d12 = this.locX + (double) f1 * d0;
            double d13 = this.locZ + (double) f2 * d0;
            this.setPosition(d12, entity.locY + (double) entity.length + 0.001, d13);
            if (this.world.a(this.getBoundingBox())) {
                this.setPosition(d12, entity.locY + (double) entity.length + 1.001, d13);
                if (this.world.a(this.getBoundingBox())) {
                    this.setPosition(entity.locX, entity.locY + (double) this.length + 0.001, entity.locZ);
                }
            }
        }
    }

    protected float ct() {
        return 0.42f;
    }

    protected void cu() {
        this.motY = this.ct();
        if (this.hasEffect(MobEffects.JUMP)) {
            this.motY += (double) ((float) (this.getEffect(MobEffects.JUMP).getAmplifier() + 1) * 0.1f);
        }
        if (this.isSprinting()) {
            float f = this.yaw * 0.017453292f;
            this.motX -= (double) (MathHelper.sin((float) f) * 0.2f);
            this.motZ += (double) (MathHelper.cos((float) f) * 0.2f);
        }
        this.impulse = true;
    }

    protected void cv() {
        this.motY += 0.03999999910593033;
    }

    protected void cw() {
        this.motY += 0.03999999910593033;
    }

    protected float cx() {
        return 0.8f;
    }

    public void a(float f, float f1, float f2) {
        double d2;
        double d1;
        double d0;
        if (this.cC() || this.bI()) {
            float f3;
            if (!(!this.isInWater() || this instanceof EntityHuman && ((EntityHuman) this).abilities.isFlying)) {
                d2 = this.locY;
                float f4 = this.cx();
                f3 = 0.02f;
                float f5 = EnchantmentManager.e((EntityLiving) this);
                if (f5 > 3.0f) {
                    f5 = 3.0f;
                }
                if (!this.onGround) {
                    f5 *= 0.5f;
                }
                if (f5 > 0.0f) {
                    f4 += (0.54600006f - f4) * f5 / 3.0f;
                    f3 += (this.cy() - f3) * f5 / 3.0f;
                }
                this.b(f, f1, f2, f3);
                this.move(EnumMoveType.SELF, this.motX, this.motY, this.motZ);
                this.motX *= (double) f4;
                this.motY *= 0.800000011920929;
                this.motZ *= (double) f4;
                if (!this.isNoGravity()) {
                    this.motY -= 0.02;
                }
                if (this.positionChanged
                        && this.c(this.motX, this.motY + 0.6000000238418579 - this.locY + d2, this.motZ)) {
                    this.motY = 0.30000001192092896;
                }
            } else if (!(!this.au() || this instanceof EntityHuman && ((EntityHuman) this).abilities.isFlying)) {
                d2 = this.locY;
                this.b(f, f1, f2, 0.02f);
                this.move(EnumMoveType.SELF, this.motX, this.motY, this.motZ);
                this.motX *= 0.5;
                this.motY *= 0.5;
                this.motZ *= 0.5;
                if (!this.isNoGravity()) {
                    this.motY -= 0.02;
                }
                if (this.positionChanged
                        && this.c(this.motX, this.motY + 0.6000000238418579 - this.locY + d2, this.motZ)) {
                    this.motY = 0.30000001192092896;
                }
            } else if (this.cP()) {
                if (this.world.paperConfig.elytraHitWallDamage) {
                    double d4;
                    double d5;
                    float f8;
                    if (this.motY > -0.5) {
                        this.fallDistance = 1.0f;
                    }
                    Vec3D vec3d = this.aJ();
                    float f6 = this.pitch * 0.017453292f;
                    d0 = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
                    d1 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ);
                    double d3 = vec3d.b();
                    float f7 = MathHelper.cos((float) f6);
                    f7 = (float) ((double) f7 * (double) f7 * Math.min(1.0, d3 / 0.4));
                    this.motY += -0.08 + (double) f7 * 0.06;
                    if (this.motY < 0.0 && d0 > 0.0) {
                        d4 = this.motY * -0.1 * (double) f7;
                        this.motY += d4;
                        this.motX += vec3d.x * d4 / d0;
                        this.motZ += vec3d.z * d4 / d0;
                    }
                    if (f6 < 0.0f) {
                        d4 = d1 * (double) (-MathHelper.sin((float) f6)) * 0.04;
                        this.motY += d4 * 3.2;
                        this.motX -= vec3d.x * d4 / d0;
                        this.motZ -= vec3d.z * d4 / d0;
                    }
                    if (d0 > 0.0) {
                        this.motX += (vec3d.x / d0 * d1 - this.motX) * 0.1;
                        this.motZ += (vec3d.z / d0 * d1 - this.motZ) * 0.1;
                    }
                    this.motX *= 0.9900000095367432;
                    this.motY *= 0.9800000190734863;
                    this.motZ *= 0.9900000095367432;
                    this.move(EnumMoveType.SELF, this.motX, this.motY, this.motZ);
                    if (this.positionChanged && !this.world.isClientSide
                            && (f8 = (float) ((d5 = d1
                            - (d4 = Math.sqrt(this.motX * this.motX + this.motZ * this.motZ))) * 10.0
                            - 3.0)) > 0.0f) {
                        this.a(this.e((int) f8), 1.0f, 1.0f);
                        this.damageEntity(DamageSource.FLY_INTO_WALL, f8);
                    }
                }
                if (this.onGround && !this.world.isClientSide && this.getFlag(7) && !CraftEventFactory
                        .callToggleGlideEvent((EntityLiving) this, (boolean) false).isCancelled()) {
                    this.setFlag(7, false);
                }
            } else {
                float f9 = 0.91f;
                BlockPosition.PooledBlockPosition blockposition_pooledblockposition = BlockPosition.PooledBlockPosition
                        .d((double) this.locX, (double) (this.getBoundingBox().b - 1.0), (double) this.locZ);
                if (this.onGround) {
                    f9 = this.world.getType((BlockPosition) blockposition_pooledblockposition).getBlock().frictionFactor
                            * 0.91f;
                }
                float f4 = 0.16277136f / (f9 * f9 * f9);
                f3 = this.onGround ? this.cy() * f4 : this.aR;
                this.b(f, f1, f2, f3);
                f9 = 0.91f;
                if (this.onGround) {
                    f9 = this.world
                            .getType((BlockPosition) blockposition_pooledblockposition.e((double) this.locX,
                                    (double) (this.getBoundingBox().b - 1.0), (double) this.locZ))
                            .getBlock().frictionFactor * 0.91f;
                }
                if (this.m_()) {
                    boolean flag;
                    float f5 = 0.15f;
                    this.motX = MathHelper.a((double) this.motX, (double) -0.15000000596046448,
                            (double) 0.15000000596046448);
                    this.motZ = MathHelper.a((double) this.motZ, (double) -0.15000000596046448,
                            (double) 0.15000000596046448);
                    this.fallDistance = 0.0f;
                    if (this.motY < -0.15) {
                        this.motY = -0.15;
                    }
                    boolean bl = flag = this.isSneaking() && this instanceof EntityHuman;
                    if (flag && this.motY < 0.0) {
                        this.motY = 0.0;
                    }
                }
                this.move(EnumMoveType.SELF, this.motX, this.motY, this.motZ);
                if (this.positionChanged && this.m_()) {
                    this.motY = 0.2;
                }
                if (this.hasEffect(MobEffects.LEVITATION)) {
                    this.motY += (0.05 * (double) (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1)
                            - this.motY) * 0.2;
                } else {
                    blockposition_pooledblockposition.e(this.locX, 0.0, this.locZ);
                    if (!(!this.world.isClientSide
                            || this.world.isLoaded((BlockPosition) blockposition_pooledblockposition) && this.world
                            .getChunkAtWorldCoords((BlockPosition) blockposition_pooledblockposition).p())) {
                        this.motY = this.locY > 0.0 ? -0.1 : 0.0;
                    } else if (!this.isNoGravity()) {
                        this.motY -= 0.08;
                    }
                }
                this.motY *= 0.9800000190734863;
                this.motX *= (double) f9;
                this.motZ *= (double) f9;
                blockposition_pooledblockposition.t();
            }
        }
        this.aF = this.aG;
        d2 = this.locX - this.lastX;
        d1 = this instanceof EntityBird ? this.locY - this.lastY : 0.0;
        float f10 = MathHelper.sqrt((double) (d2 * d2 + d1 * d1 + (d0 = this.locZ - this.lastZ) * d0)) * 4.0f;
        if (f10 > 1.0f) {
            f10 = 1.0f;
        }
        this.aG += (f10 - this.aG) * 0.4f;
        this.aH += this.aG;
    }

    public float cy() {
        return this.bC;
    }

    public void k(float f) {
        this.bC = f;
    }

    public boolean B(Entity entity) {
        this.z(entity);
        return false;
    }

    public boolean isSleeping() {
        return false;
    }

    public void B_() {
        super.B_();
        this.cI();
        if (!this.world.isClientSide) {
            int i = this.getArrowCount();
            if (i > 0) {
                if (this.ax <= 0) {
                    this.ax = 20 * (30 - i);
                }
                --this.ax;
                if (this.ax <= 0) {
                    this.setArrowCount(i - 1);
                }
            }
            block8:
            for (EnumItemSlot enumitemslot : EnumItemSlot.values()) {
                ItemStack itemstack;
                switch (enumitemslot.a()) {
                    case HAND: {
                        itemstack = (ItemStack) this.bv.get(enumitemslot.b());
                        break;
                    }
                    case ARMOR: {
                        itemstack = (ItemStack) this.bw.get(enumitemslot.b());
                        break;
                    }
                    default: {
                        continue block8;
                    }
                }
                ItemStack itemstack1 = this.getEquipment(enumitemslot);
                if (ItemStack.matches(itemstack1, itemstack))
                    continue;
                if (this instanceof EntityPlayer && enumitemslot.getType() == EnumItemSlot.Function.ARMOR) {
                    org.bukkit.inventory.ItemStack oldItem = CraftItemStack.asBukkitCopy((ItemStack) itemstack);
                    org.bukkit.inventory.ItemStack newItem = CraftItemStack.asBukkitCopy((ItemStack) itemstack1);
                    new PlayerArmorChangeEvent((Player) this.getBukkitEntity(),
                            PlayerArmorChangeEvent.SlotType.valueOf((String) enumitemslot.name()), oldItem, newItem)
                            .callEvent();
                }
                ((WorldServer) this.world).getTracker().a((Entity) this,
                        (Packet) new PacketPlayOutEntityEquipment(this.getId(), enumitemslot, itemstack1));
                if (!itemstack.isEmpty()) {
                    this.getAttributeMap().a(itemstack.a(enumitemslot));
                }
                if (!itemstack1.isEmpty()) {
                    this.getAttributeMap().b(itemstack1.a(enumitemslot));
                }
                switch (enumitemslot.a()) {
                    case HAND: {
                        this.bv.set(enumitemslot.b(), (itemstack1.isEmpty() ? ItemStack.a : itemstack1.cloneItemStack()));
                        continue block8;
                    }
                    case ARMOR: {
                        this.bw.set(enumitemslot.b(), (itemstack1.isEmpty() ? ItemStack.a : itemstack1.cloneItemStack()));
                    }
                }
            }
            if (this.ticksLived % 20 == 0) {
                this.getCombatTracker().g();
            }
            if (!this.glowing) {
                boolean flag = this.hasEffect(MobEffects.GLOWING);
                if (this.getFlag(6) != flag) {
                    this.setFlag(6, flag);
                }
            }
        }
        this.n();
        double d0 = this.locX - this.lastX;
        double d1 = this.locZ - this.lastZ;
        float f = (float) (d0 * d0 + d1 * d1);
        float f1 = this.aN;
        float f2 = 0.0f;
        this.aW = this.aX;
        float f3 = 0.0f;
        if (f > 0.0025000002f) {
            f3 = 1.0f;
            f2 = (float) Math.sqrt(f) * 3.0f;
            float f4 = (float) MathHelper.c((double) d1, (double) d0) * 57.295776f - 90.0f;
            float f5 = MathHelper.e((float) (MathHelper.g((float) this.yaw) - f4));
            f1 = 95.0f < f5 && f5 < 265.0f ? f4 - 180.0f : f4;
        }
        if (this.aD > 0.0f) {
            f1 = this.yaw;
        }
        if (!this.onGround) {
            f3 = 0.0f;
        }
        this.aX += (f3 - this.aX) * 0.3f;
        this.world.methodProfiler.a("headTurn");
        f2 = this.g(f1, f2);
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("rangeChecks");
        while (this.yaw - this.lastYaw < -180.0f) {
            this.lastYaw -= 360.0f;
        }
        while (this.yaw - this.lastYaw >= 180.0f) {
            this.lastYaw += 360.0f;
        }
        while (this.aN - this.aO < -180.0f) {
            this.aO -= 360.0f;
        }
        while (this.aN - this.aO >= 180.0f) {
            this.aO += 360.0f;
        }
        while (this.pitch - this.lastPitch < -180.0f) {
            this.lastPitch -= 360.0f;
        }
        while (this.pitch - this.lastPitch >= 180.0f) {
            this.lastPitch += 360.0f;
        }
        while (this.aP - this.aQ < -180.0f) {
            this.aQ -= 360.0f;
        }
        while (this.aP - this.aQ >= 180.0f) {
            this.aQ += 360.0f;
        }
        this.world.methodProfiler.b();
        this.aY += f2;
        this.bq = this.cP() ? ++this.bq : 0;
    }

    protected float g(float f, float f1) {
        boolean flag;
        float f2 = MathHelper.g((float) (f - this.aN));
        this.aN += f2 * 0.3f;
        float f3 = MathHelper.g((float) (this.yaw - this.aN));
        boolean bl = flag = f3 < -90.0f || f3 >= 90.0f;
        if (f3 < -75.0f) {
            f3 = -75.0f;
        }
        if (f3 >= 75.0f) {
            f3 = 75.0f;
        }
        this.aN = this.yaw - f3;
        if (f3 * f3 > 2500.0f) {
            this.aN += f3 * 0.2f;
        }
        if (flag) {
            f1 *= -1.0f;
        }
        return f1;
    }

    public void n() {
        if (this.bD > 0) {
            --this.bD;
        }
        if (this.bi > 0 && !this.bI()) {
            double d0 = this.locX + (this.bj - this.locX) / (double) this.bi;
            double d1 = this.locY + (this.bk - this.locY) / (double) this.bi;
            double d2 = this.locZ + (this.bl - this.locZ) / (double) this.bi;
            double d3 = MathHelper.g((double) (this.bm - (double) this.yaw));
            this.yaw = (float) ((double) this.yaw + d3 / (double) this.bi);
            this.pitch = (float) ((double) this.pitch + (this.bn - (double) this.pitch) / (double) this.bi);
            --this.bi;
            this.setPosition(d0, d1, d2);
            this.setYawPitch(this.yaw, this.pitch);
        } else if (!this.cC()) {
            this.motX *= 0.98;
            this.motY *= 0.98;
            this.motZ *= 0.98;
        }
        if (Math.abs(this.motX) < 0.003) {
            this.motX = 0.0;
        }
        if (Math.abs(this.motY) < 0.003) {
            this.motY = 0.0;
        }
        if (Math.abs(this.motZ) < 0.003) {
            this.motZ = 0.0;
        }
        this.world.methodProfiler.a("ai");
        if (this.isFrozen()) {
            this.bd = false;
            this.be = 0.0f;
            this.bg = 0.0f;
            this.bh = 0.0f;
        } else if (this.cC()) {
            this.world.methodProfiler.a("newAi");
            this.doTick();
            this.world.methodProfiler.b();
        }
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("jump");
        if (this.bd) {
            if (this.isInWater()) {
                this.cv();
            } else if (this.au()) {
                this.cw();
            } else if (this.onGround && this.bD == 0) {
                this.cu();
                this.bD = 10;
            }
        } else {
            this.bD = 0;
        }
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("travel");
        this.be *= 0.98f;
        this.bg *= 0.98f;
        this.bh *= 0.9f;
        this.r();
        this.a(this.be, this.bf, this.bg);
        this.world.methodProfiler.b();
        this.world.methodProfiler.a("push");
        this.cB();
        this.world.methodProfiler.b();
    }

    private void r() {
        boolean flag = this.getFlag(7);
        if (flag && !this.onGround && !this.isPassenger()) {
            ItemStack itemstack = this.getEquipment(EnumItemSlot.CHEST);
            if (itemstack.getItem() == Items.cS && ItemElytra.d((ItemStack) itemstack)) {
                flag = true;
                if (!this.world.isClientSide && (this.bq + 1) % 20 == 0) {
                    itemstack.damage(1, this);
                }
            } else {
                flag = false;
            }
        } else {
            flag = false;
        }
        if (!this.world.isClientSide && flag != this.getFlag(7)
                && !CraftEventFactory.callToggleGlideEvent((EntityLiving) this, (boolean) flag).isCancelled()) {
            this.setFlag(7, flag);
        }
    }

    protected void doTick() {
    }

    protected void cB() {
        List list = this.world.getEntities((Entity) this, this.getBoundingBox(), IEntitySelector.a((Entity) this));
        if (!list.isEmpty()) {
            int j;
            int i = this.world.getGameRules().c("maxEntityCramming");
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                j = 0;
                for (int k = 0; k < list.size(); ++k) {
                    if (((Entity) list.get(k)).isPassenger())
                        continue;
                    ++j;
                }
                if (j > i - 1) {
                    this.damageEntity(DamageSource.CRAMMING, 6.0f);
                }
            }
            this.numCollisions = Math.max(0, this.numCollisions - this.world.paperConfig.maxCollisionsPerEntity);
            for (j = 0; j < list.size() && this.numCollisions < this.world.paperConfig.maxCollisionsPerEntity; ++j) {
                Entity entity = (Entity) list.get(j);
                ++entity.numCollisions;
                ++this.numCollisions;
                this.C(entity);
            }
        }
    }

    protected void C(Entity entity) {
        entity.collide((Entity) this);
    }

    public void stopRiding() {
        Entity entity = this.bJ();
        super.stopRiding();
        if (entity != null && entity != this.bJ() && !this.world.isClientSide) {
            this.A(entity);
        }
    }

    public void aE() {
        super.aE();
        this.aW = this.aX;
        this.aX = 0.0f;
        this.fallDistance = 0.0f;
    }

    public void l(boolean flag) {
        this.bd = flag;
    }

    public void receive(Entity entity, int i) {
        if (!entity.dead && !this.world.isClientSide) {
            EntityTracker entitytracker = ((WorldServer) this.world).getTracker();
            if (entity instanceof EntityItem || entity instanceof EntityArrow
                    || entity instanceof EntityExperienceOrb) {
                entitytracker.a(entity, (Packet) new PacketPlayOutCollect(entity.getId(), this.getId(), i));
            }
        }
    }

    public boolean hasLineOfSight(Entity entity) {
        return this.world.rayTrace(new Vec3D(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ),
                new Vec3D(entity.locX, entity.locY + (double) entity.getHeadHeight(), entity.locZ), false, true,
                false) == null;
    }

    public Vec3D e(float f) {
        if (f == 1.0f) {
            return this.f(this.pitch, this.aP);
        }
        float f1 = this.lastPitch + (this.pitch - this.lastPitch) * f;
        float f2 = this.aQ + (this.aP - this.aQ) * f;
        return this.f(f1, f2);
    }

    public boolean cC() {
        return !this.world.isClientSide;
    }

    public boolean isInteractable() {
        return !this.dead && this.collides;
    }

    public boolean isCollidable() {
        return this.isAlive() && !this.m_() && this.collides;
    }

    protected void ax() {
        this.velocityChanged = this.random.nextDouble() >= this.getAttributeInstance(GenericAttributes.c).getValue();
    }

    public float getHeadRotation() {
        return this.aP;
    }

    public void setHeadRotation(float f) {
        this.aP = f;
    }

    public void h(float f) {
        this.aN = f;
    }

    public float getAbsorptionHearts() {
        return this.bE;
    }

    public void setAbsorptionHearts(float f) {
        if (f < 0.0f || Float.isNaN(f)) {
            f = 0.0f;
        }
        this.bE = f;
    }

    public void enterCombat() {
    }

    public void exitCombat() {
    }

    protected void cE() {
        this.updateEffects = true;
    }

    public abstract EnumMainHand getMainHand();

    public boolean isHandRaised() {
        return (((Byte) this.datawatcher.get(EntityLiving.at)).byteValue() & 1) > 0;
    }

    public EnumHand cH() {
        return (((Byte) this.datawatcher.get(EntityLiving.at)).byteValue() & 2) > 0 ? EnumHand.OFF_HAND
                : EnumHand.MAIN_HAND;
    }

    protected void cI() {
        if (this.isHandRaised()) {
            ItemStack itemstack = this.b(this.cH());
            if (itemstack == this.activeItem) {
                if (this.cK() <= 25 && this.cK() % 4 == 0) {
                    this.b(this.activeItem, 5);
                }
                if (--this.bp == 0 && !this.world.isClientSide) {
                    this.v();
                }
            } else {
                this.cN();
            }
        }
    }

    public void c(EnumHand enumhand) {
        ItemStack itemstack = this.b(enumhand);
        if (!itemstack.isEmpty() && !this.isHandRaised()) {
            this.activeItem = itemstack;
            this.bp = itemstack.m();
            if (!this.world.isClientSide) {
                int i = 1;
                if (enumhand == EnumHand.OFF_HAND) {
                    i |= 2;
                }
                this.datawatcher.set(at, ((byte) i));
            }
        }
    }

    public void a(DataWatcherObject<?> datawatcherobject) {
        super.a(datawatcherobject);
        if (at.equals(datawatcherobject) && this.world.isClientSide) {
            if (this.isHandRaised() && this.activeItem.isEmpty()) {
                this.activeItem = this.b(this.cH());
                if (!this.activeItem.isEmpty()) {
                    this.bp = this.activeItem.m();
                }
            } else if (!this.isHandRaised() && !this.activeItem.isEmpty()) {
                this.activeItem = ItemStack.a;
                this.bp = 0;
            }
        }
    }

    protected void b(ItemStack itemstack, int i) {
        if (!itemstack.isEmpty() && this.isHandRaised()) {
            if (itemstack.n() == EnumAnimation.DRINK) {
                this.a(SoundEffects.bT, 0.5f, this.world.random.nextFloat() * 0.1f + 0.9f);
            }
            if (itemstack.n() == EnumAnimation.EAT) {
                for (int j = 0; j < i; ++j) {
                    Vec3D vec3d = new Vec3D(((double) this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1,
                            0.0);
                    vec3d = vec3d.a(-this.pitch * 0.017453292f);
                    vec3d = vec3d.b(-this.yaw * 0.017453292f);
                    double d0 = (double) (-this.random.nextFloat()) * 0.6 - 0.3;
                    Vec3D vec3d1 = new Vec3D(((double) this.random.nextFloat() - 0.5) * 0.3, d0, 0.6);
                    vec3d1 = vec3d1.a(-this.pitch * 0.017453292f);
                    vec3d1 = vec3d1.b(-this.yaw * 0.017453292f);
                    vec3d1 = vec3d1.add(this.locX, this.locY + (double) this.getHeadHeight(), this.locZ);
                    if (itemstack.usesData()) {
                        this.world.addParticle(EnumParticle.ITEM_CRACK, vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x,
                                vec3d.y + 0.05, vec3d.z,
                                new int[]{Item.getId((Item) itemstack.getItem()), itemstack.getData()});
                        continue;
                    }
                    this.world.addParticle(EnumParticle.ITEM_CRACK, vec3d1.x, vec3d1.y, vec3d1.z, vec3d.x,
                            vec3d.y + 0.05, vec3d.z, new int[]{Item.getId((Item) itemstack.getItem())});
                }
                this.a(SoundEffects.bU, 0.5f + 0.5f * (float) this.random.nextInt(2),
                        (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
            }
        }
    }

    protected void v() {
        if (!this.activeItem.isEmpty() && this.isHandRaised()) {
            ItemStack itemstack;
            PlayerItemConsumeEvent event = null;
            this.b(this.activeItem, 16);
            if (this instanceof EntityPlayer) {
                org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitCopy((ItemStack) this.activeItem);
                event = new PlayerItemConsumeEvent((Player) this.getBukkitEntity(), craftItem);
                this.world.getServer().getPluginManager().callEvent((Event) event);
                if (event.isCancelled()) {
                    ((EntityPlayer) this).getBukkitEntity().updateInventory();
                    ((EntityPlayer) this).getBukkitEntity().updateScaledHealth();
                    return;
                }
                itemstack = craftItem.equals(event.getItem()) ? this.activeItem.a(this.world, this)
                        : CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) event.getItem()).a(this.world,
                        this);
            } else {
                itemstack = this.activeItem.a(this.world, this);
            }
            ItemStack defaultReplacement = itemstack;
            if (event != null && event.getReplacement() != null) {
                itemstack = CraftItemStack.asNMSCopy((org.bukkit.inventory.ItemStack) event.getReplacement());
            }
            this.a(this.cH(), itemstack);
            this.cN();
            if (this instanceof EntityPlayer && !Objects.equal(defaultReplacement, itemstack)) {
                ((EntityPlayer) this).getBukkitEntity().updateInventory();
            }
        }
    }

    public ItemStack getActiveItem() {
        return this.cJ();
    }

    public ItemStack cJ() {
        return this.activeItem;
    }

    public int getItemUseRemainingTime() {
        return this.cK();
    }

    public int cK() {
        return this.bp;
    }

    public int getHandRaisedTime() {
        return this.cL();
    }

    public int cL() {
        return this.isHandRaised() ? this.activeItem.m() - this.cK() : 0;
    }

    public void clearActiveItem() {
        if (!this.activeItem.isEmpty()) {
            this.activeItem.a(this.world, this, this.cK());
        }
        this.cN();
    }

    public void cN() {
        if (!this.world.isClientSide) {
            this.datawatcher.set(at, Byte.valueOf((byte) 0));
        }
        this.activeItem = ItemStack.a;
        this.bp = 0;
    }

    public boolean isBlocking() {
        if (this.isHandRaised() && !this.activeItem.isEmpty()) {
            Item item = this.activeItem.getItem();
            return item.f(this.activeItem) != EnumAnimation.BLOCK ? false
                    : item.e(this.activeItem) - this.bp >= this.getShieldBlockingDelay();
        }
        return false;
    }

    public boolean cP() {
        return this.getFlag(7);
    }

    public boolean j(double d0, double d1, double d2) {
        boolean flag1;
        double d3 = this.locX;
        double d4 = this.locY;
        double d5 = this.locZ;
        this.locX = d0;
        this.locY = d1;
        this.locZ = d2;
        boolean flag = false;
        BlockPosition blockposition = new BlockPosition((Entity) this);
        World world = this.world;
        Random random = this.getRandom();
        if (world.isLoaded(blockposition)) {
            flag1 = false;
            while (!flag1 && blockposition.getY() > 0) {
                BlockPosition blockposition1 = blockposition.down();
                IBlockData iblockdata = world.getType(blockposition1);
                if (iblockdata.getMaterial().isSolid()) {
                    flag1 = true;
                    continue;
                }
                this.locY -= 1.0;
                blockposition = blockposition1;
            }
            if (flag1) {
                EntityTeleportEvent teleport = new EntityTeleportEvent(
                        (org.bukkit.entity.Entity) this.getBukkitEntity(),
                        new Location((org.bukkit.World) this.world.getWorld(), d3, d4, d5),
                        new Location((org.bukkit.World) this.world.getWorld(), this.locX, this.locY, this.locZ));
                this.world.getServer().getPluginManager().callEvent((Event) teleport);
                if (!teleport.isCancelled()) {
                    Location to = teleport.getTo();
                    this.enderTeleportTo(to.getX(), to.getY(), to.getZ());
                    if (world.getCubes((Entity) this, this.getBoundingBox()).isEmpty()
                            && !world.containsLiquid(this.getBoundingBox())) {
                        flag = true;
                    }
                }
            }
        }
        if (!flag) {
            this.enderTeleportTo(d3, d4, d5);
            return false;
        }
        flag1 = true;
        for (int i = 0; i < 128; ++i) {
            double d6 = (double) i / 127.0;
            float f = (random.nextFloat() - 0.5f) * 0.2f;
            float f1 = (random.nextFloat() - 0.5f) * 0.2f;
            float f2 = (random.nextFloat() - 0.5f) * 0.2f;
            double d7 = d3 + (this.locX - d3) * d6 + (random.nextDouble() - 0.5) * (double) this.width * 2.0;
            double d8 = d4 + (this.locY - d4) * d6 + random.nextDouble() * (double) this.length;
            double d9 = d5 + (this.locZ - d5) * d6 + (random.nextDouble() - 0.5) * (double) this.width * 2.0;
            world.addParticle(EnumParticle.PORTAL, d7, d8, d9, (double) f, (double) f1, (double) f2, new int[0]);
        }
        if (this instanceof EntityCreature) {
            ((EntityCreature) this).getNavigation().p();
        }
        return true;
    }

    public boolean cR() {
        return true;
    }

    public boolean cS() {
        return true;
    }

    public int getShieldBlockingDelay() {
        return this.shieldBlockingDelay;
    }

    public void setShieldBlockingDelay(int shieldBlockingDelay) {
        this.shieldBlockingDelay = shieldBlockingDelay;
    }

}
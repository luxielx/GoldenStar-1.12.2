// 
// Decompiled by Procyon v0.5.36
// 

package net.minecraft.server.v1_12_R1;

import co.aikar.timings.MinecraftTimings;
import co.aikar.timings.Timing;
import com.google.common.collect.Sets;
import org.bukkit.entity.ExperienceOrb;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class Block {
    private static final MinecraftKey a;
    public static final RegistryBlocks<MinecraftKey, Block> REGISTRY;
    public static final RegistryBlockID<IBlockData> REGISTRY_ID;
    public static final AxisAlignedBB j;
    @Nullable
    public static final AxisAlignedBB k;
    private CreativeModeTab creativeTab;
    protected boolean l;
    protected int m;
    protected boolean n;
    protected int o;
    protected boolean p;
    protected float strength;
    protected float durability;
    protected boolean s;
    protected boolean t;
    protected boolean isTileEntity;
    protected SoundEffectType stepSound;
    public float w;
    protected final Material material;
    protected final MaterialMapColor y;
    public float frictionFactor;
    protected final BlockStateList blockStateList;
    private IBlockData blockData;
    private String name;
    public Timing timing;

    public Timing getTiming() {
        if (this.timing == null) {
            this.timing = MinecraftTimings.getBlockTiming(this);
        }
        return this.timing;
    }

    public static int getId(final Block block) {
        return Block.REGISTRY.a(block);
    }

    public static int getCombinedId(final IBlockData iblockdata) {
        final Block block = iblockdata.getBlock();
        return getId(block) + (block.toLegacyData(iblockdata) << 12);
    }

    public static Block getById(final int i) {
        return (Block) Block.REGISTRY.getId(i);
    }

    public static IBlockData getByCombinedId(final int i) {
        final int j = i & 0xFFF;
        final int k = i >> 12 & 0xF;
        return getById(j).fromLegacyData(k);
    }

    public static Block asBlock(@Nullable final Item item) {
        return (item instanceof ItemBlock) ? ((ItemBlock) item).getBlock() : Blocks.AIR;
    }

    @Nullable
    public static Block getByName(final String s) {
        final MinecraftKey minecraftkey = new MinecraftKey(s);
        if (Block.REGISTRY.d(minecraftkey)) {
            return (Block) Block.REGISTRY.get(minecraftkey);
        }
        try {
            return (Block) Block.REGISTRY.getId(Integer.parseInt(s));
        } catch (NumberFormatException numberformatexception) {
            return null;
        }
    }

    @Deprecated
    public boolean k(final IBlockData iblockdata) {
        return iblockdata.getMaterial().k() && iblockdata.g();
    }

    @Deprecated
    public boolean l(final IBlockData iblockdata) {
        return this.l;
    }

    @Deprecated
    public boolean a(final IBlockData iblockdata, final Entity entity) {
        return true;
    }

    @Deprecated
    public int m(final IBlockData iblockdata) {
        return this.m;
    }

    @Deprecated
    public int o(final IBlockData iblockdata) {
        return this.o;
    }

    @Deprecated
    public boolean p(final IBlockData iblockdata) {
        return this.p;
    }

    @Deprecated
    public Material q(final IBlockData iblockdata) {
        return this.material;
    }

    @Deprecated
    public MaterialMapColor c(final IBlockData iblockdata, final IBlockAccess iblockaccess,
                              final BlockPosition blockposition) {
        return this.y;
    }

    @Deprecated
    public IBlockData fromLegacyData(final int i) {
        return this.getBlockData();
    }

    public int toLegacyData(final IBlockData iblockdata) {
        if (iblockdata.s().isEmpty()) {
            return 0;
        }
        throw new IllegalArgumentException("Don't know how to convert " + iblockdata + " back into data...");
    }

    @Deprecated
    public IBlockData updateState(final IBlockData iblockdata, final IBlockAccess iblockaccess,
                                  final BlockPosition blockposition) {
        return iblockdata;
    }

    @Deprecated
    public IBlockData a(final IBlockData iblockdata, final EnumBlockRotation enumblockrotation) {
        return iblockdata;
    }

    @Deprecated
    public IBlockData a(final IBlockData iblockdata, final EnumBlockMirror enumblockmirror) {
        return iblockdata;
    }

    public Block(final Material material, final MaterialMapColor materialmapcolor) {
        this.s = true;
        this.stepSound = SoundEffectType.d;
        this.w = 1.0f;
        this.frictionFactor = 0.6f;
        this.material = material;
        this.y = materialmapcolor;
        this.blockStateList = this.getStateList();
        this.w(this.blockStateList.getBlockData());
        this.l = this.getBlockData().p();
        this.m = (this.l ? 255 : 0);
        this.n = !material.blocksLight();
    }

    protected Block(final Material material) {
        this(material, material.r());
    }

    protected Block a(final SoundEffectType soundeffecttype) {
        this.stepSound = soundeffecttype;
        return this;
    }

    protected Block e(final int i) {
        this.m = i;
        return this;
    }

    protected Block a(final float f) {
        this.o = (int) (15.0f * f);
        return this;
    }

    protected Block b(final float f) {
        this.durability = f * 3.0f;
        return this;
    }

    protected static boolean b(final Block block) {
        return block instanceof BlockShulkerBox || block instanceof BlockLeaves || block instanceof BlockTrapdoor
                || block == Blocks.BEACON || block == Blocks.cauldron || block == Blocks.GLASS
                || block == Blocks.GLOWSTONE || block == Blocks.ICE || block == Blocks.SEA_LANTERN
                || block == Blocks.STAINED_GLASS;
    }

    protected static boolean c(final Block block) {
        return b(block) || block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.PISTON_HEAD;
    }

    @Deprecated
    public boolean r(final IBlockData iblockdata) {
        return iblockdata.getMaterial().isSolid() && iblockdata.g();
    }

    @Deprecated
    public boolean isOccluding(final IBlockData iblockdata) {
        return iblockdata.getMaterial().k() && iblockdata.g() && !iblockdata.m();
    }

    @Deprecated
    public boolean t(final IBlockData iblockdata) {
        return this.material.isSolid() && this.getBlockData().g();
    }

    @Deprecated
    public boolean c(final IBlockData iblockdata) {
        return true;
    }

    public boolean b(final IBlockAccess iblockaccess, final BlockPosition blockposition) {
        return !this.material.isSolid();
    }

    @Deprecated
    public EnumRenderType a(final IBlockData iblockdata) {
        return EnumRenderType.MODEL;
    }

    public boolean a(final IBlockAccess iblockaccess, final BlockPosition blockposition) {
        return false;
    }

    protected Block c(final float f) {
        this.strength = f;
        if (this.durability < f * 5.0f) {
            this.durability = f * 5.0f;
        }
        return this;
    }

    protected Block j() {
        this.c(-1.0f);
        return this;
    }

    @Deprecated
    public float a(final IBlockData iblockdata, final World world, final BlockPosition blockposition) {
        return this.strength;
    }

    protected Block a(final boolean flag) {
        this.t = flag;
        return this;
    }

    public boolean isTicking() {
        return this.t;
    }

    public boolean isTileEntity() {
        return this.isTileEntity;
    }

    @Deprecated
    public AxisAlignedBB b(final IBlockData iblockdata, final IBlockAccess iblockaccess,
                           final BlockPosition blockposition) {
        return Block.j;
    }

    @Deprecated
    public EnumBlockFaceShape a(final IBlockAccess iblockaccess, final IBlockData iblockdata,
                                final BlockPosition blockposition, final EnumDirection enumdirection) {
        return EnumBlockFaceShape.SOLID;
    }

    @Deprecated
    public void a(final IBlockData iblockdata, final World world, final BlockPosition blockposition,
                  final AxisAlignedBB axisalignedbb, final List<AxisAlignedBB> list, @Nullable final Entity entity,
                  final boolean flag) {
        a(blockposition, axisalignedbb, list, iblockdata.d((IBlockAccess) world, blockposition));
    }

    protected static void a(final BlockPosition blockposition, final AxisAlignedBB axisalignedbb,
                            final List<AxisAlignedBB> list, @Nullable final AxisAlignedBB axisalignedbb1) {
        if (axisalignedbb1 != Block.k) {
            final AxisAlignedBB axisalignedbb2 = axisalignedbb1.a(blockposition);
            if (axisalignedbb.c(axisalignedbb2)) {
                list.add(axisalignedbb2);
            }
        }
    }

    @Deprecated
    @Nullable
    public AxisAlignedBB a(final IBlockData iblockdata, final IBlockAccess iblockaccess,
                           final BlockPosition blockposition) {
        return iblockdata.e(iblockaccess, blockposition);
    }

    @Deprecated
    public boolean b(final IBlockData iblockdata) {
        return true;
    }

    public boolean a(final IBlockData iblockdata, final boolean flag) {
        return this.m();
    }

    public boolean m() {
        return true;
    }

    public void a(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                  final Random random) {
        this.b(world, blockposition, iblockdata, random);
    }

    public void b(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                  final Random random) {
    }

    public void postBreak(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
    }

    @Deprecated
    public void a(final IBlockData iblockdata, final World world, final BlockPosition blockposition, final Block block,
                  final BlockPosition blockposition1) {
    }

    public int a(final World world) {
        return 10;
    }

    public void onPlace(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
//		AsyncCatcher.catchOp("block onPlace");
    }

    public void remove(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
//		AsyncCatcher.catchOp("block remove");
    }

    public int a(final Random random) {
        return 1;
    }

    public Item getDropType(final IBlockData iblockdata, final Random random, final int i) {
        return Item.getItemOf(this);
    }

    @Deprecated
    public float getDamage(final IBlockData iblockdata, final EntityHuman entityhuman, final World world,
                           final BlockPosition blockposition) {
        final float f = iblockdata.b(world, blockposition);
        return (f < 0.0f) ? 0.0f
                : (entityhuman.hasBlock(iblockdata) ? (entityhuman.b(iblockdata) / f / 30.0f)
                : (entityhuman.b(iblockdata) / f / 100.0f));
    }

    public final void b(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                        final int i) {
        this.dropNaturally(world, blockposition, iblockdata, 1.0f, i);
    }

    public void dropNaturally(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                              final float f, final int i) {
        if (!world.isClientSide) {
            for (int j = this.getDropCount(i, world.random), k = 0; k < j; ++k) {
                if (world.random.nextFloat() < f) {
                    final Item item = this.getDropType(iblockdata, world.random, i);
                    if (item != Items.a) {
                        a(world, blockposition, new ItemStack(item, 1, this.getDropData(iblockdata)));
                    }
                }
            }
        }
    }

    public static void a(final World world, final BlockPosition blockposition, final ItemStack itemstack) {
        if (!world.isClientSide && !itemstack.isEmpty() && world.getGameRules().getBoolean("doTileDrops")) {
            final float f = 0.5f;
            final double d0 = world.random.nextFloat() * 0.5f + 0.25;
            final double d2 = world.random.nextFloat() * 0.5f + 0.25;
            final double d3 = world.random.nextFloat() * 0.5f + 0.25;
            final EntityItem entityitem = new EntityItem(world, blockposition.getX() + d0, blockposition.getY() + d2,
                    blockposition.getZ() + d3, itemstack);
            entityitem.q();
            if (world.captureDrops != null) {
                world.captureDrops.add(entityitem);
            } else {
                world.addEntity((Entity) entityitem);
            }
        }
    }

    protected void dropExperience(final World world, final BlockPosition blockposition, int i,
                                  final EntityPlayer player) {
        if (!world.isClientSide && world.getGameRules().getBoolean("doTileDrops")) {
            while (i > 0) {
                final int j = EntityExperienceOrb.getOrbValue(i);
                i -= j;
                world.addEntity(
                        (Entity) new EntityExperienceOrb(world, blockposition.getX() + 0.5, blockposition.getY() + 0.5,
                                blockposition.getZ() + 0.5, j, ExperienceOrb.SpawnReason.BLOCK_BREAK, (Entity) player));
            }
        }
    }

    public int getDropData(final IBlockData iblockdata) {
        return 0;
    }

    public float a(final Entity entity) {
        return this.durability / 5.0f;
    }

    @Deprecated
    @Nullable
    public MovingObjectPosition a(final IBlockData iblockdata, final World world, final BlockPosition blockposition,
                                  final Vec3D vec3d, final Vec3D vec3d1) {
        return this.a(blockposition, vec3d, vec3d1, iblockdata.e((IBlockAccess) world, blockposition));
    }

    @Nullable
    protected MovingObjectPosition a(final BlockPosition blockposition, final Vec3D vec3d, final Vec3D vec3d1,
                                     final AxisAlignedBB axisalignedbb) {
        final Vec3D vec3d2 = vec3d.a((double) blockposition.getX(), (double) blockposition.getY(),
                (double) blockposition.getZ());
        final Vec3D vec3d3 = vec3d1.a((double) blockposition.getX(), (double) blockposition.getY(),
                (double) blockposition.getZ());
        final MovingObjectPosition movingobjectposition = axisalignedbb.b(vec3d2, vec3d3);
        return (movingobjectposition == null) ? null
                : new MovingObjectPosition(movingobjectposition.pos.add((double) blockposition.getX(),
                (double) blockposition.getY(), (double) blockposition.getZ()), movingobjectposition.direction,
                blockposition);
    }

    public void wasExploded(final World world, final BlockPosition blockposition, final Explosion explosion) {
    }

    public boolean canPlace(final World world, final BlockPosition blockposition, final EnumDirection enumdirection) {
        return this.canPlace(world, blockposition);
    }

    public boolean canPlace(final World world, final BlockPosition blockposition) {
        return world.getType(blockposition).getBlock().material.isReplaceable();
    }

    public boolean interact(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                            final EntityHuman entityhuman, final EnumHand enumhand, final EnumDirection enumdirection, final float f,
                            final float f1, final float f2) {
        return false;
    }

    public void stepOn(final World world, final BlockPosition blockposition, final Entity entity) {
    }

    public IBlockData getPlacedState(final World world, final BlockPosition blockposition,
                                     final EnumDirection enumdirection, final float f, final float f1, final float f2, final int i,
                                     final EntityLiving entityliving) {
        return this.fromLegacyData(i);
    }

    public void attack(final World world, final BlockPosition blockposition, final EntityHuman entityhuman) {
    }

    public Vec3D a(final World world, final BlockPosition blockposition, final Entity entity, final Vec3D vec3d) {
        return vec3d;
    }

    @Deprecated
    public int b(final IBlockData iblockdata, final IBlockAccess iblockaccess, final BlockPosition blockposition,
                 final EnumDirection enumdirection) {
        return 0;
    }

    @Deprecated
    public boolean isPowerSource(final IBlockData iblockdata) {
        return false;
    }

    public void a(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                  final Entity entity) {
    }

    @Deprecated
    public int c(final IBlockData iblockdata, final IBlockAccess iblockaccess, final BlockPosition blockposition,
                 final EnumDirection enumdirection) {
        return 0;
    }

    public void a(final World world, final EntityHuman entityhuman, final BlockPosition blockposition,
                  final IBlockData iblockdata, @Nullable final TileEntity tileentity, final ItemStack itemstack) {
        entityhuman.b(StatisticList.a(this));
        entityhuman.applyExhaustion(0.005f);
        if (this.n() && EnchantmentManager.getEnchantmentLevel(Enchantments.SILK_TOUCH, itemstack) > 0) {
            final ItemStack itemstack2 = this.u(iblockdata);
            a(world, blockposition, itemstack2);
        } else {
            final int i = EnchantmentManager.getEnchantmentLevel(Enchantments.LOOT_BONUS_BLOCKS, itemstack);
            this.b(world, blockposition, iblockdata, i);
        }
    }

    protected boolean n() {
        return this.getBlockData().g() && !this.isTileEntity;
    }

    protected ItemStack u(final IBlockData iblockdata) {
        final Item item = Item.getItemOf(this);
        int i = 0;
        if (item.k()) {
            i = this.toLegacyData(iblockdata);
        }
        return new ItemStack(item, 1, i);
    }

    public int getDropCount(final int i, final Random random) {
        return this.a(random);
    }

    public void postPlace(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                          final EntityLiving entityliving, final ItemStack itemstack) {
    }

    public boolean d() {
        return !this.material.isBuildable() && !this.material.isLiquid();
    }

    public Block c(final String s) {
        this.name = s;
        return this;
    }

    public String getName() {
        return LocaleI18n.get(this.a() + ".name");
    }

    public String a() {
        return "tile." + this.name;
    }

    @Deprecated
    public boolean a(final IBlockData iblockdata, final World world, final BlockPosition blockposition, final int i,
                     final int j) {
        return false;
    }

    public boolean o() {
        return this.s;
    }

    protected Block p() {
        this.s = false;
        return this;
    }

    @Deprecated
    public EnumPistonReaction h(final IBlockData iblockdata) {
        return this.material.getPushReaction();
    }

    public void fallOn(final World world, final BlockPosition blockposition, final Entity entity, final float f) {
        entity.e(f, 1.0f);
    }

    public void a(final World world, final Entity entity) {
        entity.motY = 0.0;
    }

    public ItemStack a(final World world, final BlockPosition blockposition, final IBlockData iblockdata) {
        return new ItemStack(Item.getItemOf(this), 1, this.getDropData(iblockdata));
    }

    public void a(final CreativeModeTab creativemodetab, final NonNullList<ItemStack> nonnulllist) {
        nonnulllist.add(new ItemStack(this));
    }

    public CreativeModeTab q() {
        return this.creativeTab;
    }

    public Block a(final CreativeModeTab creativemodetab) {
        this.creativeTab = creativemodetab;
        return this;
    }

    public void a(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                  final EntityHuman entityhuman) {
    }

    public void h(final World world, final BlockPosition blockposition) {
    }

    public boolean r() {
        return true;
    }

    public boolean a(final Explosion explosion) {
        return true;
    }

    public boolean d(final Block block) {
        return this == block;
    }

    public static boolean a(final Block block, final Block block1) {
        return block != null && block1 != null && (block == block1 || block.d(block1));
    }

    @Deprecated
    public boolean isComplexRedstone(final IBlockData iblockdata) {
        return false;
    }

    @Deprecated
    public int c(final IBlockData iblockdata, final World world, final BlockPosition blockposition) {
        return 0;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList(this, new IBlockState[0]);
    }

    public BlockStateList s() {
        return this.blockStateList;
    }

    protected final void w(final IBlockData iblockdata) {
        this.blockData = iblockdata;
    }

    public final IBlockData getBlockData() {
        return this.blockData;
    }

    public EnumRandomOffset u() {
        return EnumRandomOffset.NONE;
    }

    @Deprecated
    public Vec3D f(final IBlockData iblockdata, final IBlockAccess iblockaccess, final BlockPosition blockposition) {
        final EnumRandomOffset block_enumrandomoffset = this.u();
        if (block_enumrandomoffset == EnumRandomOffset.NONE) {
            return Vec3D.a;
        }
        final long i = MathHelper.c(blockposition.getX(), 0, blockposition.getZ());
        return new Vec3D(((i >> 16 & 0xFL) / 15.0f - 0.5) * 0.5,
                (block_enumrandomoffset == EnumRandomOffset.XYZ) ? (((i >> 20 & 0xFL) / 15.0f - 1.0) * 0.2) : 0.0,
                ((i >> 24 & 0xFL) / 15.0f - 0.5) * 0.5);
    }

    public SoundEffectType getStepSound() {
        return this.stepSound;
    }

    @Override
    public String toString() {
        return "Block{" + Block.REGISTRY.b(this) + "}";
    }

    public static void w() {
        a(0, Block.a, new BlockAir().c("air"));
        a(1, "stone", new BlockStone().c(1.5f).b(10.0f).a(SoundEffectType.d).c("stone"));
        a(2, "grass", new BlockGrass().c(0.6f).a(SoundEffectType.c).c("grass"));
        a(3, "dirt", new BlockDirt().c(0.5f).a(SoundEffectType.b).c("dirt"));
        final Block block = new Block(Material.STONE).c(2.0f).b(10.0f).a(SoundEffectType.d).c("stonebrick")
                .a(CreativeModeTab.b);
        a(4, "cobblestone", block);
        final Block block2 = new BlockWood().c(2.0f).b(5.0f).a(SoundEffectType.a).c("wood");
        a(5, "planks", block2);
        a(6, "sapling", new BlockSapling().c(0.0f).a(SoundEffectType.c).c("sapling"));
        a(7, "bedrock", new BlockNoDrop(Material.STONE).j().b(6000000.0f).a(SoundEffectType.d).c("bedrock").p()
                .a(CreativeModeTab.b));
        a(8, "flowing_water", new BlockFlowing(Material.WATER).c(100.0f).e(3).c("water").p());
        a(9, "water", new BlockStationary(Material.WATER).c(100.0f).e(3).c("water").p());
        a(10, "flowing_lava", new BlockFlowing(Material.LAVA).c(100.0f).a(1.0f).c("lava").p());
        a(11, "lava", new BlockStationary(Material.LAVA).c(100.0f).a(1.0f).c("lava").p());
        a(12, "sand", new BlockSand().c(0.5f).a(SoundEffectType.h).c("sand"));
        a(13, "gravel", new BlockGravel().c(0.6f).a(SoundEffectType.b).c("gravel"));
        a(14, "gold_ore", new BlockOre().c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreGold"));
        a(15, "iron_ore", new BlockOre().c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreIron"));
        a(16, "coal_ore", new BlockOre().c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreCoal"));
        a(17, "log", new BlockLog1().c("log"));
        a(18, "leaves", new BlockLeaves1().c("leaves"));
        a(19, "sponge", new BlockSponge().c(0.6f).a(SoundEffectType.c).c("sponge"));
        a(20, "glass", new BlockGlass(Material.SHATTERABLE, false).c(0.3f).a(SoundEffectType.f).c("glass"));
        a(21, "lapis_ore", new BlockOre().c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreLapis"));
        a(22, "lapis_block", new Block(Material.ORE, MaterialMapColor.I).c(3.0f).b(5.0f).a(SoundEffectType.d)
                .c("blockLapis").a(CreativeModeTab.b));
        a(23, "dispenser", new BlockDispenser().c(3.5f).a(SoundEffectType.d).c("dispenser"));
        final Block block3 = new BlockSandStone().a(SoundEffectType.d).c(0.8f).c("sandStone");
        a(24, "sandstone", block3);
        a(25, "noteblock", new BlockNote().a(SoundEffectType.a).c(0.8f).c("musicBlock"));
        a(26, "bed", new BlockBed().a(SoundEffectType.a).c(0.2f).c("bed").p());
        a(27, "golden_rail", new BlockPoweredRail().c(0.7f).a(SoundEffectType.e).c("goldenRail"));
        a(28, "detector_rail", new BlockMinecartDetector().c(0.7f).a(SoundEffectType.e).c("detectorRail"));
        a(29, "sticky_piston", new BlockPiston(true).c("pistonStickyBase"));
        a(30, "web", new BlockWeb().e(1).c(4.0f).c("web"));
        a(31, "tallgrass", new BlockLongGrass().c(0.0f).a(SoundEffectType.c).c("tallgrass"));
        a(32, "deadbush", new BlockDeadBush().c(0.0f).a(SoundEffectType.c).c("deadbush"));
        a(33, "piston", new BlockPiston(false).c("pistonBase"));
        a(34, "piston_head", new BlockPistonExtension().c("pistonBase"));
        a(35, "wool", new BlockCloth(Material.CLOTH).c(0.8f).a(SoundEffectType.g).c("cloth"));
        a(36, "piston_extension", (Block) new BlockPistonMoving());
        a(37, "yellow_flower", new BlockYellowFlowers().c(0.0f).a(SoundEffectType.c).c("flower1"));
        a(38, "red_flower", new BlockRedFlowers().c(0.0f).a(SoundEffectType.c).c("flower2"));
        final Block block4 = new BlockMushroom().c(0.0f).a(SoundEffectType.c).a(0.125f).c("mushroom");
        a(39, "brown_mushroom", block4);
        final Block block5 = new BlockMushroom().c(0.0f).a(SoundEffectType.c).c("mushroom");
        a(40, "red_mushroom", block5);
        a(41, "gold_block", new Block(Material.ORE, MaterialMapColor.G).c(3.0f).b(10.0f).a(SoundEffectType.e)
                .c("blockGold").a(CreativeModeTab.b));
        a(42, "iron_block", new Block(Material.ORE, MaterialMapColor.i).c(5.0f).b(10.0f).a(SoundEffectType.e)
                .c("blockIron").a(CreativeModeTab.b));
        a(43, "double_stone_slab", new BlockDoubleStep().c(2.0f).b(10.0f).a(SoundEffectType.d).c("stoneSlab"));
        a(44, "stone_slab", new BlockStep().c(2.0f).b(10.0f).a(SoundEffectType.d).c("stoneSlab"));
        final Block block6 = new Block(Material.STONE, MaterialMapColor.E).c(2.0f).b(10.0f).a(SoundEffectType.d)
                .c("brick").a(CreativeModeTab.b);
        a(45, "brick_block", block6);
        a(46, "tnt", new BlockTNT().c(0.0f).a(SoundEffectType.c).c("tnt"));
        a(47, "bookshelf", new BlockBookshelf().c(1.5f).a(SoundEffectType.a).c("bookshelf"));
        a(48, "mossy_cobblestone",
                new Block(Material.STONE).c(2.0f).b(10.0f).a(SoundEffectType.d).c("stoneMoss").a(CreativeModeTab.b));
        a(49, "obsidian", new BlockObsidian().c(50.0f).b(2000.0f).a(SoundEffectType.d).c("obsidian"));
        a(50, "torch", new BlockTorch().c(0.0f).a(0.9375f).a(SoundEffectType.a).c("torch"));
        a(51, "fire", new BlockFire().c(0.0f).a(1.0f).a(SoundEffectType.g).c("fire").p());
        a(52, "mob_spawner", new BlockMobSpawner().c(5.0f).a(SoundEffectType.e).c("mobSpawner").p());
        a(53, "oak_stairs", new BlockStairs(
                block2.getBlockData().set((IBlockState) BlockWood.VARIANT, (Comparable) BlockWood.EnumLogVariant.OAK))
                .c("stairsWood"));
        a(54, "chest", new BlockChest(BlockChest.Type.BASIC).c(2.5f).a(SoundEffectType.a).c("chest"));
        a(55, "redstone_wire", new BlockRedstoneWire().c(0.0f).a(SoundEffectType.d).c("redstoneDust").p());
        a(56, "diamond_ore", new BlockOre().c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreDiamond"));
        a(57, "diamond_block", new Block(Material.ORE, MaterialMapColor.H).c(5.0f).b(10.0f).a(SoundEffectType.e)
                .c("blockDiamond").a(CreativeModeTab.b));
        a(58, "crafting_table", new BlockWorkbench().c(2.5f).a(SoundEffectType.a).c("workbench"));
        a(59, "wheat", new BlockCrops().c("crops"));
        final Block block7 = new BlockSoil().c(0.6f).a(SoundEffectType.b).c("farmland");
        a(60, "farmland", block7);
        a(61, "furnace", new BlockFurnace(false).c(3.5f).a(SoundEffectType.d).c("furnace").a(CreativeModeTab.c));
        a(62, "lit_furnace", new BlockFurnace(true).c(3.5f).a(SoundEffectType.d).a(0.875f).c("furnace"));
        a(63, "standing_sign", new BlockFloorSign().c(1.0f).a(SoundEffectType.a).c("sign").p());
        a(64, "wooden_door", new BlockDoor(Material.WOOD).c(3.0f).a(SoundEffectType.a).c("doorOak").p());
        a(65, "ladder", new BlockLadder().c(0.4f).a(SoundEffectType.j).c("ladder"));
        a(66, "rail", new BlockMinecartTrack().c(0.7f).a(SoundEffectType.e).c("rail"));
        a(67, "stone_stairs", new BlockStairs(block.getBlockData()).c("stairsStone"));
        a(68, "wall_sign", new BlockWallSign().c(1.0f).a(SoundEffectType.a).c("sign").p());
        a(69, "lever", new BlockLever().c(0.5f).a(SoundEffectType.a).c("lever"));
        a(70, "stone_pressure_plate",
                new BlockPressurePlateBinary(Material.STONE, BlockPressurePlateBinary.EnumMobType.MOBS).c(0.5f)
                        .a(SoundEffectType.d).c("pressurePlateStone"));
        a(71, "iron_door", new BlockDoor(Material.ORE).c(5.0f).a(SoundEffectType.e).c("doorIron").p());
        a(72, "wooden_pressure_plate",
                new BlockPressurePlateBinary(Material.WOOD, BlockPressurePlateBinary.EnumMobType.EVERYTHING).c(0.5f)
                        .a(SoundEffectType.a).c("pressurePlateWood"));
        a(73, "redstone_ore",
                new BlockRedstoneOre(false).c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreRedstone").a(CreativeModeTab.b));
        a(74, "lit_redstone_ore",
                new BlockRedstoneOre(true).a(0.625f).c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreRedstone"));
        a(75, "unlit_redstone_torch", new BlockRedstoneTorch(false).c(0.0f).a(SoundEffectType.a).c("notGate"));
        a(76, "redstone_torch",
                new BlockRedstoneTorch(true).c(0.0f).a(0.5f).a(SoundEffectType.a).c("notGate").a(CreativeModeTab.d));
        a(77, "stone_button", new BlockStoneButton().c(0.5f).a(SoundEffectType.d).c("button"));
        a(78, "snow_layer", new BlockSnow().c(0.1f).a(SoundEffectType.i).c("snow").e(0));
        a(79, "ice", new BlockIce().c(0.5f).e(3).a(SoundEffectType.f).c("ice"));
        a(80, "snow", new BlockSnowBlock().c(0.2f).a(SoundEffectType.i).c("snow"));
        a(81, "cactus", new BlockCactus().c(0.4f).a(SoundEffectType.g).c("cactus"));
        a(82, "clay", new BlockClay().c(0.6f).a(SoundEffectType.b).c("clay"));
        a(83, "reeds", new BlockReed().c(0.0f).a(SoundEffectType.c).c("reeds").p());
        a(84, "jukebox", new BlockJukeBox().c(2.0f).b(10.0f).a(SoundEffectType.d).c("jukebox"));
        a(85, "fence", new BlockFence(Material.WOOD, BlockWood.EnumLogVariant.OAK.c()).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("fence"));
        final Block block8 = new BlockPumpkin().c(1.0f).a(SoundEffectType.a).c("pumpkin");
        a(86, "pumpkin", block8);
        a(87, "netherrack", new BlockBloodStone().c(0.4f).a(SoundEffectType.d).c("hellrock"));
        a(88, "soul_sand", new BlockSlowSand().c(0.5f).a(SoundEffectType.h).c("hellsand"));
        a(89, "glowstone",
                new BlockLightStone(Material.SHATTERABLE).c(0.3f).a(SoundEffectType.f).a(1.0f).c("lightgem"));
        a(90, "portal", new BlockPortal().c(-1.0f).a(SoundEffectType.f).a(0.75f).c("portal"));
        a(91, "lit_pumpkin", new BlockPumpkin().c(1.0f).a(SoundEffectType.a).a(1.0f).c("litpumpkin"));
        a(92, "cake", new BlockCake().c(0.5f).a(SoundEffectType.g).c("cake").p());
        a(93, "unpowered_repeater", new BlockRepeater(false).c(0.0f).a(SoundEffectType.a).c("diode").p());
        a(94, "powered_repeater", new BlockRepeater(true).c(0.0f).a(SoundEffectType.a).c("diode").p());
        a(95, "stained_glass",
                new BlockStainedGlass(Material.SHATTERABLE).c(0.3f).a(SoundEffectType.f).c("stainedGlass"));
        a(96, "trapdoor", new BlockTrapdoor(Material.WOOD).c(3.0f).a(SoundEffectType.a).c("trapdoor").p());
        a(97, "monster_egg", new BlockMonsterEggs().c(0.75f).c("monsterStoneEgg"));
        final Block block9 = new BlockSmoothBrick().c(1.5f).b(10.0f).a(SoundEffectType.d).c("stonebricksmooth");
        a(98, "stonebrick", block9);
        a(99, "brown_mushroom_block", new BlockHugeMushroom(Material.WOOD, MaterialMapColor.m, block4).c(0.2f)
                .a(SoundEffectType.a).c("mushroom"));
        a(100, "red_mushroom_block", new BlockHugeMushroom(Material.WOOD, MaterialMapColor.E, block5).c(0.2f)
                .a(SoundEffectType.a).c("mushroom"));
        a(101, "iron_bars", new BlockThin(Material.ORE, true).c(5.0f).b(10.0f).a(SoundEffectType.e).c("fenceIron"));
        a(102, "glass_pane", new BlockThin(Material.SHATTERABLE, false).c(0.3f).a(SoundEffectType.f).c("thinGlass"));
        final Block block10 = new BlockMelon().c(1.0f).a(SoundEffectType.a).c("melon");
        a(103, "melon_block", block10);
        a(104, "pumpkin_stem", new BlockStem(block8).c(0.0f).a(SoundEffectType.a).c("pumpkinStem"));
        a(105, "melon_stem", new BlockStem(block10).c(0.0f).a(SoundEffectType.a).c("pumpkinStem"));
        a(106, "vine", new BlockVine().c(0.2f).a(SoundEffectType.c).c("vine"));
        a(107, "fence_gate",
                new BlockFenceGate(BlockWood.EnumLogVariant.OAK).c(2.0f).b(5.0f).a(SoundEffectType.a).c("fenceGate"));
        a(108, "brick_stairs", new BlockStairs(block6.getBlockData()).c("stairsBrick"));
        a(109, "stone_brick_stairs", new BlockStairs(block9.getBlockData().set((IBlockState) BlockSmoothBrick.VARIANT,
                (Comparable) BlockSmoothBrick.EnumStonebrickType.DEFAULT)).c("stairsStoneBrickSmooth"));
        a(110, "mycelium", new BlockMycel().c(0.6f).a(SoundEffectType.c).c("mycel"));
        a(111, "waterlily", new BlockWaterLily().c(0.0f).a(SoundEffectType.c).c("waterlily"));
        final Block block11 = new BlockNetherbrick().c(2.0f).b(10.0f).a(SoundEffectType.d).c("netherBrick")
                .a(CreativeModeTab.b);
        a(112, "nether_brick", block11);
        a(113, "nether_brick_fence", new BlockFence(Material.STONE, MaterialMapColor.L).c(2.0f).b(10.0f)
                .a(SoundEffectType.d).c("netherFence"));
        a(114, "nether_brick_stairs", new BlockStairs(block11.getBlockData()).c("stairsNetherBrick"));
        a(115, "nether_wart", new BlockNetherWart().c("netherStalk"));
        a(116, "enchanting_table", new BlockEnchantmentTable().c(5.0f).b(2000.0f).c("enchantmentTable"));
        a(117, "brewing_stand", new BlockBrewingStand().c(0.5f).a(0.125f).c("brewingStand"));
        a(118, "cauldron", new BlockCauldron().c(2.0f).c("cauldron"));
        a(119, "end_portal", new BlockEnderPortal(Material.PORTAL).c(-1.0f).b(6000000.0f));
        a(120, "end_portal_frame", new BlockEnderPortalFrame().a(SoundEffectType.f).a(0.125f).c(-1.0f)
                .c("endPortalFrame").b(6000000.0f).a(CreativeModeTab.c));
        a(121, "end_stone", new Block(Material.STONE, MaterialMapColor.e).c(3.0f).b(15.0f).a(SoundEffectType.d)
                .c("whiteStone").a(CreativeModeTab.b));
        a(122, "dragon_egg", new BlockDragonEgg().c(3.0f).b(15.0f).a(SoundEffectType.d).a(0.125f).c("dragonEgg"));
        a(123, "redstone_lamp",
                new BlockRedstoneLamp(false).c(0.3f).a(SoundEffectType.f).c("redstoneLight").a(CreativeModeTab.d));
        a(124, "lit_redstone_lamp", new BlockRedstoneLamp(true).c(0.3f).a(SoundEffectType.f).c("redstoneLight"));
        a(125, "double_wooden_slab", new BlockDoubleWoodStep().c(2.0f).b(5.0f).a(SoundEffectType.a).c("woodSlab"));
        a(126, "wooden_slab", new BlockWoodStep().c(2.0f).b(5.0f).a(SoundEffectType.a).c("woodSlab"));
        a(127, "cocoa", new BlockCocoa().c(0.2f).b(5.0f).a(SoundEffectType.a).c("cocoa"));
        a(128, "sandstone_stairs", new BlockStairs(block3.getBlockData().set((IBlockState) BlockSandStone.TYPE,
                (Comparable) BlockSandStone.EnumSandstoneVariant.SMOOTH)).c("stairsSandStone"));
        a(129, "emerald_ore", new BlockOre().c(3.0f).b(5.0f).a(SoundEffectType.d).c("oreEmerald"));
        a(130, "ender_chest", new BlockEnderChest().c(22.5f).b(1000.0f).a(SoundEffectType.d).c("enderChest").a(0.5f));
        a(131, "tripwire_hook", new BlockTripwireHook().c("tripWireSource"));
        a(132, "tripwire", new BlockTripwire().c("tripWire"));
        a(133, "emerald_block", new Block(Material.ORE, MaterialMapColor.J).c(5.0f).b(10.0f).a(SoundEffectType.e)
                .c("blockEmerald").a(CreativeModeTab.b));
        a(134, "spruce_stairs", new BlockStairs(block2.getBlockData().set((IBlockState) BlockWood.VARIANT,
                (Comparable) BlockWood.EnumLogVariant.SPRUCE)).c("stairsWoodSpruce"));
        a(135, "birch_stairs", new BlockStairs(
                block2.getBlockData().set((IBlockState) BlockWood.VARIANT, (Comparable) BlockWood.EnumLogVariant.BIRCH))
                .c("stairsWoodBirch"));
        a(136, "jungle_stairs", new BlockStairs(block2.getBlockData().set((IBlockState) BlockWood.VARIANT,
                (Comparable) BlockWood.EnumLogVariant.JUNGLE)).c("stairsWoodJungle"));
        a(137, "command_block", new BlockCommand(MaterialMapColor.C).j().b(6000000.0f).c("commandBlock"));
        a(138, "beacon", new BlockBeacon().c("beacon").a(1.0f));
        a(139, "cobblestone_wall", new BlockCobbleWall(block).c("cobbleWall"));
        a(140, "flower_pot", new BlockFlowerPot().c(0.0f).a(SoundEffectType.d).c("flowerPot"));
        a(141, "carrots", new BlockCarrots().c("carrots"));
        a(142, "potatoes", new BlockPotatoes().c("potatoes"));
        a(143, "wooden_button", new BlockWoodButton().c(0.5f).a(SoundEffectType.a).c("button"));
        a(144, "skull", new BlockSkull().c(1.0f).a(SoundEffectType.d).c("skull"));
        a(145, "anvil", new BlockAnvil().c(5.0f).a(SoundEffectType.k).b(2000.0f).c("anvil"));
        a(146, "trapped_chest", new BlockChest(BlockChest.Type.TRAP).c(2.5f).a(SoundEffectType.a).c("chestTrap"));
        a(147, "light_weighted_pressure_plate", new BlockPressurePlateWeighted(Material.ORE, 15, MaterialMapColor.G)
                .c(0.5f).a(SoundEffectType.a).c("weightedPlate_light"));
        a(148, "heavy_weighted_pressure_plate", new BlockPressurePlateWeighted(Material.ORE, 150).c(0.5f)
                .a(SoundEffectType.a).c("weightedPlate_heavy"));
        a(149, "unpowered_comparator",
                new BlockRedstoneComparator(false).c(0.0f).a(SoundEffectType.a).c("comparator").p());
        a(150, "powered_comparator",
                new BlockRedstoneComparator(true).c(0.0f).a(0.625f).a(SoundEffectType.a).c("comparator").p());
        a(151, "daylight_detector", (Block) new BlockDaylightDetector(false));
        a(152, "redstone_block", new BlockPowered(Material.ORE, MaterialMapColor.g).c(5.0f).b(10.0f)
                .a(SoundEffectType.e).c("blockRedstone").a(CreativeModeTab.d));
        a(153, "quartz_ore", new BlockOre(MaterialMapColor.L).c(3.0f).b(5.0f).a(SoundEffectType.d).c("netherquartz"));
        a(154, "hopper", new BlockHopper().c(3.0f).b(8.0f).a(SoundEffectType.e).c("hopper"));
        final Block block12 = new BlockQuartz().a(SoundEffectType.d).c(0.8f).c("quartzBlock");
        a(155, "quartz_block", block12);
        a(156, "quartz_stairs", new BlockStairs(block12.getBlockData().set((IBlockState) BlockQuartz.VARIANT,
                (Comparable) BlockQuartz.EnumQuartzVariant.DEFAULT)).c("stairsQuartz"));
        a(157, "activator_rail", new BlockPoweredRail().c(0.7f).a(SoundEffectType.e).c("activatorRail"));
        a(158, "dropper", new BlockDropper().c(3.5f).a(SoundEffectType.d).c("dropper"));
        a(159, "stained_hardened_clay",
                new BlockStainedHardenedClay().c(1.25f).b(7.0f).a(SoundEffectType.d).c("clayHardenedStained"));
        a(160, "stained_glass_pane", new BlockStainedGlassPane().c(0.3f).a(SoundEffectType.f).c("thinStainedGlass"));
        a(161, "leaves2", new BlockLeaves2().c("leaves"));
        a(162, "log2", new BlockLog2().c("log"));
        a(163, "acacia_stairs", new BlockStairs(block2.getBlockData().set((IBlockState) BlockWood.VARIANT,
                (Comparable) BlockWood.EnumLogVariant.ACACIA)).c("stairsWoodAcacia"));
        a(164, "dark_oak_stairs", new BlockStairs(block2.getBlockData().set((IBlockState) BlockWood.VARIANT,
                (Comparable) BlockWood.EnumLogVariant.DARK_OAK)).c("stairsWoodDarkOak"));
        a(165, "slime", new BlockSlime().c("slime").a(SoundEffectType.l));
        a(166, "barrier", new BlockBarrier().c("barrier"));
        a(167, "iron_trapdoor", new BlockTrapdoor(Material.ORE).c(5.0f).a(SoundEffectType.e).c("ironTrapdoor").p());
        a(168, "prismarine", new BlockPrismarine().c(1.5f).b(10.0f).a(SoundEffectType.d).c("prismarine"));
        a(169, "sea_lantern",
                new BlockSeaLantern(Material.SHATTERABLE).c(0.3f).a(SoundEffectType.f).a(1.0f).c("seaLantern"));
        a(170, "hay_block", new BlockHay().c(0.5f).a(SoundEffectType.c).c("hayBlock").a(CreativeModeTab.b));
        a(171, "carpet", new BlockCarpet().c(0.1f).a(SoundEffectType.g).c("woolCarpet").e(0));
        a(172, "hardened_clay", new BlockHardenedClay().c(1.25f).b(7.0f).a(SoundEffectType.d).c("clayHardened"));
        a(173, "coal_block", new Block(Material.STONE, MaterialMapColor.F).c(5.0f).b(10.0f).a(SoundEffectType.d)
                .c("blockCoal").a(CreativeModeTab.b));
        a(174, "packed_ice", new BlockPackedIce().c(0.5f).a(SoundEffectType.f).c("icePacked"));
        a(175, "double_plant", (Block) new BlockTallPlant());
        a(176, "standing_banner", new BlockBanner.BlockStandingBanner().c(1.0f).a(SoundEffectType.a).c("banner").p());
        a(177, "wall_banner", new BlockBanner.BlockWallBanner().c(1.0f).a(SoundEffectType.a).c("banner").p());
        a(178, "daylight_detector_inverted", (Block) new BlockDaylightDetector(true));
        final Block block13 = new BlockRedSandstone().a(SoundEffectType.d).c(0.8f).c("redSandStone");
        a(179, "red_sandstone", block13);
        a(180, "red_sandstone_stairs", new BlockStairs(block13.getBlockData().set((IBlockState) BlockRedSandstone.TYPE,
                (Comparable) BlockRedSandstone.EnumRedSandstoneVariant.SMOOTH)).c("stairsRedSandStone"));
        a(181, "double_stone_slab2", new BlockDoubleStoneStep2().c(2.0f).b(10.0f).a(SoundEffectType.d).c("stoneSlab2"));
        a(182, "stone_slab2", new BlockStoneStep2().c(2.0f).b(10.0f).a(SoundEffectType.d).c("stoneSlab2"));
        a(183, "spruce_fence_gate", new BlockFenceGate(BlockWood.EnumLogVariant.SPRUCE).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("spruceFenceGate"));
        a(184, "birch_fence_gate", new BlockFenceGate(BlockWood.EnumLogVariant.BIRCH).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("birchFenceGate"));
        a(185, "jungle_fence_gate", new BlockFenceGate(BlockWood.EnumLogVariant.JUNGLE).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("jungleFenceGate"));
        a(186, "dark_oak_fence_gate", new BlockFenceGate(BlockWood.EnumLogVariant.DARK_OAK).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("darkOakFenceGate"));
        a(187, "acacia_fence_gate", new BlockFenceGate(BlockWood.EnumLogVariant.ACACIA).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("acaciaFenceGate"));
        a(188, "spruce_fence", new BlockFence(Material.WOOD, BlockWood.EnumLogVariant.SPRUCE.c()).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("spruceFence"));
        a(189, "birch_fence", new BlockFence(Material.WOOD, BlockWood.EnumLogVariant.BIRCH.c()).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("birchFence"));
        a(190, "jungle_fence", new BlockFence(Material.WOOD, BlockWood.EnumLogVariant.JUNGLE.c()).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("jungleFence"));
        a(191, "dark_oak_fence", new BlockFence(Material.WOOD, BlockWood.EnumLogVariant.DARK_OAK.c()).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("darkOakFence"));
        a(192, "acacia_fence", new BlockFence(Material.WOOD, BlockWood.EnumLogVariant.ACACIA.c()).c(2.0f).b(5.0f)
                .a(SoundEffectType.a).c("acaciaFence"));
        a(193, "spruce_door", new BlockDoor(Material.WOOD).c(3.0f).a(SoundEffectType.a).c("doorSpruce").p());
        a(194, "birch_door", new BlockDoor(Material.WOOD).c(3.0f).a(SoundEffectType.a).c("doorBirch").p());
        a(195, "jungle_door", new BlockDoor(Material.WOOD).c(3.0f).a(SoundEffectType.a).c("doorJungle").p());
        a(196, "acacia_door", new BlockDoor(Material.WOOD).c(3.0f).a(SoundEffectType.a).c("doorAcacia").p());
        a(197, "dark_oak_door", new BlockDoor(Material.WOOD).c(3.0f).a(SoundEffectType.a).c("doorDarkOak").p());
        a(198, "end_rod", new BlockEndRod().c(0.0f).a(0.9375f).a(SoundEffectType.a).c("endRod"));
        a(199, "chorus_plant", new BlockChorusFruit().c(0.4f).a(SoundEffectType.a).c("chorusPlant"));
        a(200, "chorus_flower", new BlockChorusFlower().c(0.4f).a(SoundEffectType.a).c("chorusFlower"));
        final Block block14 = new Block(Material.STONE, MaterialMapColor.s).c(1.5f).b(10.0f).a(SoundEffectType.d)
                .a(CreativeModeTab.b).c("purpurBlock");
        a(201, "purpur_block", block14);
        a(202, "purpur_pillar", new BlockRotatable(Material.STONE, MaterialMapColor.s).c(1.5f).b(10.0f)
                .a(SoundEffectType.d).a(CreativeModeTab.b).c("purpurPillar"));
        a(203, "purpur_stairs", new BlockStairs(block14.getBlockData()).c("stairsPurpur"));
        a(204, "purpur_double_slab",
                new BlockPurpurSlab.Default().c(2.0f).b(10.0f).a(SoundEffectType.d).c("purpurSlab"));
        a(205, "purpur_slab", new BlockPurpurSlab.Half().c(2.0f).b(10.0f).a(SoundEffectType.d).c("purpurSlab"));
        a(206, "end_bricks", new Block(Material.STONE, MaterialMapColor.e).a(SoundEffectType.d).c(0.8f)
                .a(CreativeModeTab.b).c("endBricks"));
        a(207, "beetroots", new BlockBeetroot().c("beetroots"));
        final Block block15 = new BlockGrassPath().c(0.65f).a(SoundEffectType.c).c("grassPath").p();
        a(208, "grass_path", block15);
        a(209, "end_gateway", new BlockEndGateway(Material.PORTAL).c(-1.0f).b(6000000.0f));
        a(210, "repeating_command_block",
                new BlockCommand(MaterialMapColor.A).j().b(6000000.0f).c("repeatingCommandBlock"));
        a(211, "chain_command_block", new BlockCommand(MaterialMapColor.D).j().b(6000000.0f).c("chainCommandBlock"));
        a(212, "frosted_ice", new BlockIceFrost().c(0.5f).e(3).a(SoundEffectType.f).c("frostedIce"));
        a(213, "magma", new BlockMagma().c(0.5f).a(SoundEffectType.d).c("magma"));
        a(214, "nether_wart_block", new Block(Material.GRASS, MaterialMapColor.E).a(CreativeModeTab.b).c(1.0f)
                .a(SoundEffectType.a).c("netherWartBlock"));
        a(215, "red_nether_brick",
                new BlockNetherbrick().c(2.0f).b(10.0f).a(SoundEffectType.d).c("redNetherBrick").a(CreativeModeTab.b));
        a(216, "bone_block", new BlockBone().c("boneBlock"));
        a(217, "structure_void", new BlockStructureVoid().c("structureVoid"));
        a(218, "observer", new BlockObserver().c(3.0f).c("observer"));
        a(219, "white_shulker_box",
                new BlockShulkerBox(EnumColor.WHITE).c(2.0f).a(SoundEffectType.d).c("shulkerBoxWhite"));
        a(220, "orange_shulker_box",
                new BlockShulkerBox(EnumColor.ORANGE).c(2.0f).a(SoundEffectType.d).c("shulkerBoxOrange"));
        a(221, "magenta_shulker_box",
                new BlockShulkerBox(EnumColor.MAGENTA).c(2.0f).a(SoundEffectType.d).c("shulkerBoxMagenta"));
        a(222, "light_blue_shulker_box",
                new BlockShulkerBox(EnumColor.LIGHT_BLUE).c(2.0f).a(SoundEffectType.d).c("shulkerBoxLightBlue"));
        a(223, "yellow_shulker_box",
                new BlockShulkerBox(EnumColor.YELLOW).c(2.0f).a(SoundEffectType.d).c("shulkerBoxYellow"));
        a(224, "lime_shulker_box",
                new BlockShulkerBox(EnumColor.LIME).c(2.0f).a(SoundEffectType.d).c("shulkerBoxLime"));
        a(225, "pink_shulker_box",
                new BlockShulkerBox(EnumColor.PINK).c(2.0f).a(SoundEffectType.d).c("shulkerBoxPink"));
        a(226, "gray_shulker_box",
                new BlockShulkerBox(EnumColor.GRAY).c(2.0f).a(SoundEffectType.d).c("shulkerBoxGray"));
        a(227, "silver_shulker_box",
                new BlockShulkerBox(EnumColor.SILVER).c(2.0f).a(SoundEffectType.d).c("shulkerBoxSilver"));
        a(228, "cyan_shulker_box",
                new BlockShulkerBox(EnumColor.CYAN).c(2.0f).a(SoundEffectType.d).c("shulkerBoxCyan"));
        a(229, "purple_shulker_box",
                new BlockShulkerBox(EnumColor.PURPLE).c(2.0f).a(SoundEffectType.d).c("shulkerBoxPurple"));
        a(230, "blue_shulker_box",
                new BlockShulkerBox(EnumColor.BLUE).c(2.0f).a(SoundEffectType.d).c("shulkerBoxBlue"));
        a(231, "brown_shulker_box",
                new BlockShulkerBox(EnumColor.BROWN).c(2.0f).a(SoundEffectType.d).c("shulkerBoxBrown"));
        a(232, "green_shulker_box",
                new BlockShulkerBox(EnumColor.GREEN).c(2.0f).a(SoundEffectType.d).c("shulkerBoxGreen"));
        a(233, "red_shulker_box", new BlockShulkerBox(EnumColor.RED).c(2.0f).a(SoundEffectType.d).c("shulkerBoxRed"));
        a(234, "black_shulker_box",
                new BlockShulkerBox(EnumColor.BLACK).c(2.0f).a(SoundEffectType.d).c("shulkerBoxBlack"));
        a(235, "white_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.WHITE));
        a(236, "orange_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.ORANGE));
        a(237, "magenta_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.MAGENTA));
        a(238, "light_blue_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.LIGHT_BLUE));
        a(239, "yellow_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.YELLOW));
        a(240, "lime_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.LIME));
        a(241, "pink_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.PINK));
        a(242, "gray_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.GRAY));
        a(243, "silver_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.SILVER));
        a(244, "cyan_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.CYAN));
        a(245, "purple_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.PURPLE));
        a(246, "blue_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.BLUE));
        a(247, "brown_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.BROWN));
        a(248, "green_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.GREEN));
        a(249, "red_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.RED));
        a(250, "black_glazed_terracotta", (Block) new BlockGlazedTerracotta(EnumColor.BLACK));
        a(251, "concrete", new BlockCloth(Material.STONE).c(1.8f).a(SoundEffectType.d).c("concrete"));
        a(252, "concrete_powder", new BlockConcretePowder().c(0.5f).a(SoundEffectType.h).c("concretePowder"));
        a(255, "structure_block", new BlockStructure().j().b(6000000.0f).c("structureBlock"));
        Block.REGISTRY.a();
        for (final Block block16 : Block.REGISTRY) {
            if (block16.material == Material.AIR) {
                block16.p = false;
            } else {
                boolean flag = false;
                final boolean flag2 = block16 instanceof BlockStairs;
                final boolean flag3 = block16 instanceof BlockStepAbstract;
                final boolean flag4 = block16 == block7 || block16 == block15;
                final boolean flag5 = block16.n;
                final boolean flag6 = block16.m == 0;
                if (flag2 || flag3 || flag4 || flag5 || flag6) {
                    flag = true;
                }
                block16.p = flag;
            }
        }
        final HashSet hashset = Sets
                .newHashSet((Object[]) new Block[]{(Block) Block.REGISTRY.get(new MinecraftKey("tripwire"))});
        for (final Block block17 : Block.REGISTRY) {
            if (hashset.contains(block17)) {
                for (int i = 0; i < 15; ++i) {
                    final int j = Block.REGISTRY.a(block17) << 4 | i;
                    Block.REGISTRY_ID.a(block17.fromLegacyData(i), j);
                }
            } else {
                for (final IBlockData iblockdata : block17.s().a()) {
                    final int k = Block.REGISTRY.a(block17) << 4 | block17.toLegacyData(iblockdata);
                    Block.REGISTRY_ID.a(iblockdata, k);
                }
            }
        }
    }

    public int getExpDrop(final World world, final IBlockData data, final int enchantmentLevel) {
        return 0;
    }

    private static void a(final int i, final MinecraftKey minecraftkey, final Block block) {
        Block.REGISTRY.a(i, minecraftkey, block);
    }

    private static void a(final int i, final String s, final Block block) {
        a(i, new MinecraftKey(s), block);
    }

    public static float range(final float min, final float value, final float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static {
        a = new MinecraftKey("air");
        REGISTRY = new RegistryBlocks((Object) Block.a);
        REGISTRY_ID = new RegistryBlockID();
        j = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        k = null;
    }

    public enum EnumRandomOffset {
        NONE, XZ, XYZ;
    }
}

// 
// Decompiled by Procyon v0.5.36
// 

package net.minecraft.server.v1_12_R1;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BlockSapling extends BlockPlant implements IBlockFragilePlantElement {
    public static final BlockStateEnum<BlockWood.EnumLogVariant> TYPE;
    public static final BlockStateInteger STAGE;
    protected static final AxisAlignedBB d;
    public static TreeType treeType;

    protected BlockSapling() {
        this.w(this.blockStateList.getBlockData()
                .set((IBlockState) BlockSapling.TYPE, (Comparable) BlockWood.EnumLogVariant.OAK)
                .set((IBlockState) BlockSapling.STAGE, (Comparable) 0));
        this.a(CreativeModeTab.c);
    }

    public AxisAlignedBB b(final IBlockData iblockdata, final IBlockAccess iblockaccess,
                           final BlockPosition blockposition) {
        return BlockSapling.d;
    }

    public String getName() {
        return LocaleI18n.get(this.a() + "." + BlockWood.EnumLogVariant.OAK.d() + ".name");
    }

    public void b(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                  final Random random) {
        if (!world.isClientSide) {
            super.b(world, blockposition, iblockdata, random);
            if (world.isLightLevel(blockposition.up(), 9) && random
                    .nextInt(Math.max(2, (int) (100.0f / world.spigotConfig.saplingModifier * 7.0f + 0.5f))) == 0) {
                world.captureTreeGeneration = true;
                this.grow(world, blockposition, iblockdata, random);
                world.captureTreeGeneration = false;
                if (world.capturedBlockStates.size() > 0) {
                    final TreeType treeType = BlockSapling.treeType;
                    BlockSapling.treeType = null;
                    final Location location = new Location((org.bukkit.World) world.getWorld(),
                            (double) blockposition.getX(), (double) blockposition.getY(),
                            (double) blockposition.getZ());
                    final List<BlockState> blocks = new ArrayList<BlockState>(world.capturedBlockStates);
                    world.capturedBlockStates.clear();
                    StructureGrowEvent event = null;
                    if (treeType != null) {
                        event = new StructureGrowEvent(location, treeType, false, (Player) null, (List) blocks);
                        Bukkit.getPluginManager().callEvent((Event) event);
                    }
                    if (event == null || !event.isCancelled()) {
                        for (final BlockState blockstate : blocks) {
                            blockstate.update(true);
                        }
                    }
                }
            }
        }
    }

    public void grow(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                     final Random random) {
        if ((int) iblockdata.get((IBlockState) BlockSapling.STAGE) == 0) {
            world.setTypeAndData(blockposition, iblockdata.a((IBlockState) BlockSapling.STAGE), 4);
        } else {
            this.d(world, blockposition, iblockdata, random);
        }
    }

    public void d(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                  final Random random) {
        Object object;
        if (random.nextInt(10) == 0) {
            BlockSapling.treeType = TreeType.BIG_TREE;
            object = new WorldGenBigTree(true);
        } else {
            BlockSapling.treeType = TreeType.TREE;
            object = new WorldGenTrees(true);
        }
        int i = 0;
        int j = 0;
        boolean flag = false;
        switch ((BlockWood.EnumLogVariant) iblockdata.get((IBlockState) BlockSapling.TYPE)) {
            case SPRUCE: {
                Label_0185:
                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (this.a(world, blockposition, i, j, BlockWood.EnumLogVariant.SPRUCE)) {
                            BlockSapling.treeType = TreeType.MEGA_REDWOOD;
                            object = new WorldGenMegaTree(false, random.nextBoolean());
                            flag = true;
                            break Label_0185;
                        }
                    }
                }
                if (!flag) {
                    i = 0;
                    j = 0;
                    BlockSapling.treeType = TreeType.REDWOOD;
                    object = new WorldGenTaiga2(true);
                    break;
                }
                break;
            }
            case BIRCH: {
                BlockSapling.treeType = TreeType.BIRCH;
                object = new WorldGenForest(true, false);
                break;
            }
            case JUNGLE: {
                final IBlockData iblockdata2 = Blocks.LOG.getBlockData().set((IBlockState) BlockLog1.VARIANT,
                        (Comparable) BlockWood.EnumLogVariant.JUNGLE);
                final IBlockData iblockdata3 = Blocks.LEAVES.getBlockData()
                        .set((IBlockState) BlockLeaves1.VARIANT, (Comparable) BlockWood.EnumLogVariant.JUNGLE)
                        .set((IBlockState) BlockLeaves.CHECK_DECAY, (Comparable) false);
                Label_0361:
                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (this.a(world, blockposition, i, j, BlockWood.EnumLogVariant.JUNGLE)) {
                            BlockSapling.treeType = TreeType.JUNGLE;
                            object = new WorldGenJungleTree(true, 10, 20, iblockdata2, iblockdata3);
                            flag = true;
                            break Label_0361;
                        }
                    }
                }
                if (!flag) {
                    i = 0;
                    j = 0;
                    BlockSapling.treeType = TreeType.SMALL_JUNGLE;
                    object = new WorldGenTrees(true, 4 + random.nextInt(7), iblockdata2, iblockdata3, false);
                    break;
                }
                break;
            }
            case ACACIA: {
                BlockSapling.treeType = TreeType.ACACIA;
                object = new WorldGenAcaciaTree(true);
                break;
            }
            case DARK_OAK: {
                Label_0492:
                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (this.a(world, blockposition, i, j, BlockWood.EnumLogVariant.DARK_OAK)) {
                            BlockSapling.treeType = TreeType.DARK_OAK;
                            object = new WorldGenForestTree(true);
                            flag = true;
                            break Label_0492;
                        }
                    }
                }
                if (!flag) {
                    return;
                }
                break;
            }
        }
        final IBlockData iblockdata2 = Blocks.AIR.getBlockData();
        if (flag) {
            world.setTypeAndData(blockposition.a(i, 0, j), iblockdata2, 4);
            world.setTypeAndData(blockposition.a(i + 1, 0, j), iblockdata2, 4);
            world.setTypeAndData(blockposition.a(i, 0, j + 1), iblockdata2, 4);
            world.setTypeAndData(blockposition.a(i + 1, 0, j + 1), iblockdata2, 4);
        } else {
            world.setTypeAndData(blockposition, iblockdata2, 4);
        }
        if (!((WorldGenerator) object).generate(world, random, blockposition.a(i, 0, j))) {
            if (flag) {
                world.setTypeAndData(blockposition.a(i, 0, j), iblockdata, 4);
                world.setTypeAndData(blockposition.a(i + 1, 0, j), iblockdata, 4);
                world.setTypeAndData(blockposition.a(i, 0, j + 1), iblockdata, 4);
                world.setTypeAndData(blockposition.a(i + 1, 0, j + 1), iblockdata, 4);
            } else {
                world.setTypeAndData(blockposition, iblockdata, 4);
            }
        }
    }

    private boolean a(final World world, final BlockPosition blockposition, final int i, final int j,
                      final BlockWood.EnumLogVariant blockwood_enumlogvariant) {
        return this.a(world, blockposition.a(i, 0, j), blockwood_enumlogvariant)
                && this.a(world, blockposition.a(i + 1, 0, j), blockwood_enumlogvariant)
                && this.a(world, blockposition.a(i, 0, j + 1), blockwood_enumlogvariant)
                && this.a(world, blockposition.a(i + 1, 0, j + 1), blockwood_enumlogvariant);
    }

    public boolean a(final World world, final BlockPosition blockposition,
                     final BlockWood.EnumLogVariant blockwood_enumlogvariant) {
        final IBlockData iblockdata = world.getType(blockposition);
        return iblockdata.getBlock() == this
                && iblockdata.get((IBlockState) BlockSapling.TYPE) == blockwood_enumlogvariant;
    }

    public int getDropData(final IBlockData iblockdata) {
        return ((BlockWood.EnumLogVariant) iblockdata.get((IBlockState) BlockSapling.TYPE)).a();
    }

    public void a(final CreativeModeTab creativemodetab, final NonNullList<ItemStack> nonnulllist) {
        for (final BlockWood.EnumLogVariant blockwood_enumlogvariant : BlockWood.EnumLogVariant.values()) {
            nonnulllist.add(new ItemStack((Block) this, 1, blockwood_enumlogvariant.a()));
        }
    }

    public boolean a(final World world, final BlockPosition blockposition, final IBlockData iblockdata,
                     final boolean flag) {
        return true;
    }

    public boolean a(final World world, final Random random, final BlockPosition blockposition,
                     final IBlockData iblockdata) {
        return world.random.nextFloat() < 0.45;
    }

    public void b(final World world, final Random random, final BlockPosition blockposition,
                  final IBlockData iblockdata) {
        this.grow(world, blockposition, iblockdata, random);
    }

    public IBlockData fromLegacyData(final int i) {
        return this.getBlockData()
                .set((IBlockState) BlockSapling.TYPE, (Comparable) BlockWood.EnumLogVariant.a(i & 0x7))
                .set((IBlockState) BlockSapling.STAGE, (Comparable) ((i & 0x8) >> 3));
    }

    public int toLegacyData(final IBlockData iblockdata) {
        final byte b0 = 0;
        int i = b0 | ((BlockWood.EnumLogVariant) iblockdata.get((IBlockState) BlockSapling.TYPE)).a();
        i |= (int) iblockdata.get((IBlockState) BlockSapling.STAGE) << 3;
        return i;
    }

    protected BlockStateList getStateList() {
        return new BlockStateList((Block) this,
                new IBlockState[]{(IBlockState) BlockSapling.TYPE, (IBlockState) BlockSapling.STAGE});
    }

    static {
        TYPE = BlockStateEnum.of("type", (Class) BlockWood.EnumLogVariant.class);
        STAGE = BlockStateInteger.of("stage", 0, 1);
        d = new AxisAlignedBB(0.09999999403953552, 0.0, 0.09999999403953552, 0.8999999761581421, 0.800000011920929,
                0.8999999761581421);
    }
}

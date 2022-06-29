// 
// Decompiled by Procyon v0.5.36
// 

package net.minecraft.server.v1_12_R1;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.world.StructureGrowEvent;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class ItemStack {
    public static final ItemStack a;
    public static final DecimalFormat b;
    private int count;
    private int d;
    private Item item;
    private NBTTagCompound tag;
    private boolean g;
    private int damage;
    private EntityItemFrame i;
    private Block j;
    private boolean k;
    private Block l;
    private boolean m;
    private static final Comparator<NBTTagCompound> enchantSorter;
    private static Item airItem;

    public void setDamage(final int i) {
        this.damage = i;
    }

    public ItemStack(final Block block) {
        this(block, 1);
    }

    public ItemStack(final Block block, final int i) {
        this(block, i, 0);
    }

    public ItemStack(final Block block, final int i, final int j) {
        this(Item.getItemOf(block), i, j);
    }

    public ItemStack(final Item item) {
        this(item, 1);
    }

    public ItemStack(final Item item, final int i) {
        this(item, i, 0);
    }

    private void processEnchantOrder(final NBTTagCompound tag) {
        if (tag == null || !tag.hasKeyOfType("ench", 9)) {
            return;
        }
        final NBTTagList list = tag.getList("ench", 10);
        if (list.size() < 2) {
            return;
        }
        try {
            list.sort((Comparator) ItemStack.enchantSorter);
        } catch (Exception ex) {
        }
    }

    public ItemStack(final Item item, final int i, final int j) {
        this(item, i, j, true);
    }

    public ItemStack(final Item item, final int i, final int j, final boolean convert) {
        this.item = item;
        this.damage = j;
        this.count = i;
        if (MinecraftServer.getServer() != null) {
            this.setData(j);
        }
        if (convert) {
            this.convertStack();
        }
        if (this.damage < 0) {
        }
        this.F();
    }

    public void convertStack() {
        if (MinecraftServer.getServer() != null) {
            if (this.item == Items.BED) {
                return;
            }
            final NBTTagCompound savedStack = new NBTTagCompound();
            this.save(savedStack);
            MinecraftServer.getServer().dataConverterManager.a((DataConverterType) DataConverterTypes.ITEM_INSTANCE,
                    savedStack);
            this.load(savedStack);
        }
    }

    private void F() {
        if (this.g && this == ItemStack.a) {
            throw new AssertionError((Object) "TRAP");
        }
        this.g = this.isEmpty();
    }

    public void load(final NBTTagCompound nbttagcompound) {
        this.item = (nbttagcompound.hasKeyOfType("id", 8) ? Item.b(nbttagcompound.getString("id"))
                : Item.getItemOf(Blocks.AIR));
        this.count = nbttagcompound.getByte("Count");
        this.setData(nbttagcompound.getShort("Damage"));
        if (nbttagcompound.hasKeyOfType("tag", 10)) {
            this.processEnchantOrder(this.tag = (NBTTagCompound) nbttagcompound.getCompound("tag").clone());
            if (this.item != null) {
                this.item.a(this.tag);
            }
        }
    }

    public ItemStack(final NBTTagCompound nbttagcompound) {
        this.load(nbttagcompound);
        this.F();
    }

    public boolean isEmpty() {
        if (ItemStack.airItem == null) {
            ItemStack.airItem = (Item) Item.REGISTRY.get(new MinecraftKey("air"));
        }
        return this == ItemStack.a || this.item == null || this.item == ItemStack.airItem || this.count <= 0
                || this.damage < -32768 || this.damage > 65535;
    }

    public static void a(final DataConverterManager dataconvertermanager) {
        dataconvertermanager.a(DataConverterTypes.ITEM_INSTANCE, (DataInspector) new DataInspectorBlockEntity());
        dataconvertermanager.a(DataConverterTypes.ITEM_INSTANCE, (DataInspector) new DataInspectorEntity());
    }

    public ItemStack cloneAndSubtract(final int i) {
        final int j = Math.min(i, this.count);
        final ItemStack itemstack = this.cloneItemStack();
        itemstack.setCount(j);
        this.subtract(j);
        return itemstack;
    }

    public Item getItem() {
        return this.g ? Item.getItemOf(Blocks.AIR) : this.item;
    }

    public EnumInteractionResult placeItem(final EntityHuman entityhuman, final World world,
                                           final BlockPosition blockposition, final EnumHand enumhand, final EnumDirection enumdirection,
                                           final float f, final float f1, final float f2) {
        final int oldData = this.getData();
        final int oldCount = this.getCount();
        if (!(this.getItem() instanceof ItemBucket)) {
            world.captureBlockStates = true;
            if (this.getItem() instanceof ItemDye && this.getData() == 15) {
                final Block block = world.getType(blockposition).getBlock();
                if (block == Blocks.SAPLING || block instanceof BlockMushroom) {
                    world.captureTreeGeneration = true;
                }
            }
        }
        EnumInteractionResult enuminteractionresult = this.getItem().a(entityhuman, world, blockposition, enumhand,
                enumdirection, f, f1, f2);
        final int newData = this.getData();
        final int newCount = this.getCount();
        this.setCount(oldCount);
        this.setData(oldData);
        world.captureBlockStates = false;
        if (enuminteractionresult == EnumInteractionResult.SUCCESS && world.captureTreeGeneration
                && world.capturedBlockStates.size() > 0) {
            world.captureTreeGeneration = false;
            final Location location = new Location((org.bukkit.World) world.getWorld(), (double) blockposition.getX(),
                    (double) blockposition.getY(), (double) blockposition.getZ());
            final TreeType treeType = BlockSapling.treeType;
            BlockSapling.treeType = null;
            final List<BlockState> blocks = ImmutableList.copyOf(world.capturedBlockStates);
            world.capturedBlockStates.clear();
            StructureGrowEvent event = null;
            if (treeType != null) {
                final boolean isBonemeal = this.getItem() == Items.DYE && oldData == 15;
                event = new StructureGrowEvent(location, treeType, isBonemeal, (Player) entityhuman.getBukkitEntity(),
                        blocks);
                Bukkit.getPluginManager().callEvent((Event) event);
            }
            if (event == null || !event.isCancelled()) {
                if (this.getCount() == oldCount && this.getData() == oldData) {
                    this.setData(newData);
                    this.setCount(newCount);
                }
                for (final BlockState blockstate : blocks) {
                    blockstate.update(true);
                }
            }
            return enuminteractionresult;
        }
        world.captureTreeGeneration = false;
        if (enuminteractionresult == EnumInteractionResult.SUCCESS) {
            BlockPlaceEvent placeEvent = null;
            final List<BlockState> blocks2 = ImmutableList.copyOf(world.capturedBlockStates);
            world.capturedBlockStates.clear();
            if (blocks2.size() > 1) {
                placeEvent = (BlockPlaceEvent) CraftEventFactory.callBlockMultiPlaceEvent(world, entityhuman, enumhand,
                        blocks2, blockposition.getX(), blockposition.getY(), blockposition.getZ());
            } else if (blocks2.size() == 1) {
                placeEvent = CraftEventFactory.callBlockPlaceEvent(world, entityhuman, enumhand,
                        (BlockState) blocks2.get(0), blockposition.getX(), blockposition.getY(), blockposition.getZ());
            }
            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
                enuminteractionresult = EnumInteractionResult.FAIL;
                placeEvent.getPlayer().updateInventory();
                for (final Map.Entry<BlockPosition, TileEntity> e : world.capturedTileEntities.entrySet()) {
                    if (e.getValue() instanceof TileEntityLootable) {
                        ((TileEntityLootable) e.getValue()).clearLootTable();
                    }
                }
                for (final BlockState blockstate2 : blocks2) {
                    blockstate2.update(true, false);
                }
            } else {
                if (this.getCount() == oldCount && this.getData() == oldData) {
                    this.setData(newData);
                    this.setCount(newCount);
                }
                for (final Map.Entry<BlockPosition, TileEntity> e : world.capturedTileEntities.entrySet()) {
                    world.setTileEntity((BlockPosition) e.getKey(), (TileEntity) e.getValue());
                }
                for (final BlockState blockstate2 : blocks2) {
                    final int x = blockstate2.getX();
                    final int y = blockstate2.getY();
                    final int z = blockstate2.getZ();
                    final int updateFlag = ((CraftBlockState) blockstate2).getFlag();
                    final Material mat = blockstate2.getType();
                    final Block oldBlock = CraftMagicNumbers.getBlock(mat);
                    final BlockPosition newblockposition = new BlockPosition(x, y, z);
                    final IBlockData block2 = world.getType(newblockposition);
                    if (!(block2.getBlock() instanceof BlockTileEntity)) {
                        block2.getBlock().onPlace(world, newblockposition, block2);
                    }
                    world.notifyAndUpdatePhysics(newblockposition, (Chunk) null, oldBlock.getBlockData(), block2,
                            updateFlag);
                }
                if (this.item instanceof ItemRecord) {
                    ((BlockJukeBox) Blocks.JUKEBOX).a(world, blockposition, world.getType(blockposition), this);
                    world.a((EntityHuman) null, 1010, blockposition, Item.getId(this.item));
                    this.subtract(1);
                    entityhuman.b(StatisticList.Z);
                }
                if (this.item == Items.SKULL) {
                    BlockPosition bp = blockposition;
                    if (!world.getType(blockposition).getBlock().a((IBlockAccess) world, blockposition)) {
                        if (!world.getType(blockposition).getMaterial().isBuildable()) {
                            bp = null;
                        } else {
                            bp = bp.shift(enumdirection);
                        }
                    }
                    if (bp != null) {
                        final TileEntity te = world.getTileEntity(bp);
                        if (te instanceof TileEntitySkull) {
                            Blocks.SKULL.a(world, bp, (TileEntitySkull) te);
                        }
                    }
                }
                if (this.item instanceof ItemBlock) {
                    final SoundEffectType soundeffecttype = ((ItemBlock) this.item).getBlock().getStepSound();
                    world.a(entityhuman, blockposition, soundeffecttype.e(), SoundCategory.BLOCKS,
                            (soundeffecttype.a() + 1.0f) / 2.0f, soundeffecttype.b() * 0.8f);
                }
                entityhuman.b(StatisticList.b(this.item));
            }
        }
        world.capturedTileEntities.clear();
        world.capturedBlockStates.clear();
        return enuminteractionresult;
    }

    public float a(final IBlockData iblockdata) {
        return this.getItem().getDestroySpeed(this, iblockdata);
    }

    public InteractionResultWrapper<ItemStack> a(final World world, final EntityHuman entityhuman,
                                                 final EnumHand enumhand) {
        return (InteractionResultWrapper<ItemStack>) this.getItem().a(world, entityhuman, enumhand);
    }

    public ItemStack a(final World world, final EntityLiving entityliving) {
        return this.getItem().a(this, world, entityliving);
    }

    public NBTTagCompound save(final NBTTagCompound nbttagcompound) {
        final MinecraftKey minecraftkey = (MinecraftKey) Item.REGISTRY.b(this.item);
        nbttagcompound.setString("id", (minecraftkey == null) ? "minecraft:air" : minecraftkey.toString());
        nbttagcompound.setByte("Count", (byte) this.count);
        nbttagcompound.setShort("Damage", (short) this.damage);
        if (this.tag != null) {
            nbttagcompound.set("tag", this.tag.clone());
        }
        return nbttagcompound;
    }

    public int getMaxStackSize() {
        return this.getItem().getMaxStackSize();
    }

    public boolean isStackable() {
        return this.getMaxStackSize() > 1 && (!this.f() || !this.h());
    }

    public boolean f() {
        return !this.g && this.item.getMaxDurability() > 0
                && (!this.hasTag() || !this.getTag().getBoolean("Unbreakable"));
    }

    public boolean usesData() {
        return this.getItem().k();
    }

    public boolean hasDamage() {
        return this.h();
    }

    public boolean h() {
        return this.f() && this.damage > 0;
    }

    public int getDamage() {
        return this.i();
    }

    public int i() {
        return this.damage;
    }

    public int getData() {
        return this.damage;
    }

    public void setData(int i) {
        if (i == 32767) {
            this.damage = i;
            return;
        }
        if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) != Blocks.AIR && !this.usesData()
                && !this.getItem().usesDurability()) {
            i = 0;
        }
        if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) == Blocks.DOUBLE_PLANT
                && (i > 5 || i < 0)) {
            i = 0;
        }
        this.damage = i;
        if (this.damage < 0) {
        }
    }

    public int k() {
        return this.getItem().getMaxDurability();
    }

    public boolean isDamaged(int i, final Random random, @Nullable final EntityPlayer entityplayer) {
        if (!this.f()) {
            return false;
        }
        if (i > 0) {
            final int j = EnchantmentManager.getEnchantmentLevel(Enchantments.DURABILITY, this);
            int k = 0;
            for (int l = 0; j > 0 && l < i; ++l) {
                if (EnchantmentDurability.a(this, j, random)) {
                    ++k;
                }
            }
            i -= k;
            if (entityplayer != null) {
                final CraftItemStack item = CraftItemStack.asCraftMirror(this);
                final PlayerItemDamageEvent event = new PlayerItemDamageEvent((Player) entityplayer.getBukkitEntity(),
                        (org.bukkit.inventory.ItemStack) item, i);
                Bukkit.getServer().getPluginManager().callEvent((Event) event);
                if (i != event.getDamage() || event.isCancelled()) {
                    event.getPlayer().updateInventory();
                }
                if (event.isCancelled()) {
                    return false;
                }
                i = event.getDamage();
            }
            if (i <= 0) {
                return false;
            }
        }
        if (entityplayer != null && i != 0) {
            CriterionTriggers.s.a(entityplayer, this, this.damage + i);
        }
        this.damage += i;
        return this.damage > this.k();
    }

    public void damage(final int i, final EntityLiving entityliving) {
        if ((!(entityliving instanceof EntityHuman) || !((EntityHuman) entityliving).abilities.canInstantlyBuild)
                && this.f() && this.isDamaged(i, entityliving.getRandom(),
                (entityliving instanceof EntityPlayer) ? (EntityPlayer) entityliving : null)) {
            entityliving.b(this);
            if (this.count == 1 && entityliving instanceof EntityHuman) {
                CraftEventFactory.callPlayerItemBreakEvent((EntityHuman) entityliving, this);
            }
            this.subtract(1);
            if (entityliving instanceof EntityHuman) {
                final EntityHuman entityhuman = (EntityHuman) entityliving;
                entityhuman.b(StatisticList.c(this.item));
            }
            this.damage = 0;
        }
    }

    public void a(final EntityLiving entityliving, final EntityHuman entityhuman) {
        final boolean flag = this.item.a(this, entityliving, (EntityLiving) entityhuman);
        if (flag) {
            entityhuman.b(StatisticList.b(this.item));
        }
    }

    public void a(final World world, final IBlockData iblockdata, final BlockPosition blockposition,
                  final EntityHuman entityhuman) {
        final boolean flag = this.getItem().a(this, world, iblockdata, blockposition, (EntityLiving) entityhuman);
        if (flag) {
            entityhuman.b(StatisticList.b(this.item));
        }
    }

    public boolean b(final IBlockData iblockdata) {
        return this.getItem().canDestroySpecialBlock(iblockdata);
    }

    public boolean a(final EntityHuman entityhuman, final EntityLiving entityliving, final EnumHand enumhand) {
        return this.getItem().a(this, entityhuman, entityliving, enumhand);
    }

    public ItemStack cloneItemStack() {
        final ItemStack itemstack = new ItemStack(this.item, this.count, this.damage, false);
        itemstack.d(this.D());
        if (this.tag != null) {
            itemstack.tag = this.tag.g();
        }
        return itemstack;
    }

    public static boolean equals(final ItemStack itemstack, final ItemStack itemstack1) {
        return (itemstack.isEmpty() && itemstack1.isEmpty())
                || (!itemstack.isEmpty() && !itemstack1.isEmpty() && (itemstack.tag != null || itemstack1.tag == null)
                && (itemstack.tag == null || itemstack.tag.equals((Object) itemstack1.tag)));
    }

    public static boolean fastMatches(final ItemStack itemstack, final ItemStack itemstack1) {
        return (itemstack.isEmpty() && itemstack1.isEmpty())
                || (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.count == itemstack1.count
                && itemstack.item == itemstack1.item && itemstack.damage == itemstack1.damage);
    }

    public static boolean matches(final ItemStack itemstack, final ItemStack itemstack1) {
        return (itemstack.isEmpty() && itemstack1.isEmpty())
                || (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.d(itemstack1));
    }

    private boolean d(final ItemStack itemstack) {
        return this.count == itemstack.count && this.getItem() == itemstack.getItem() && this.damage == itemstack.damage
                && (this.tag != null || itemstack.tag == null)
                && (this.tag == null || this.tag.equals((Object) itemstack.tag));
    }

    public static boolean c(final ItemStack itemstack, final ItemStack itemstack1) {
        return itemstack == itemstack1
                || (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.doMaterialsMatch(itemstack1));
    }

    public static boolean d(final ItemStack itemstack, final ItemStack itemstack1) {
        return itemstack == itemstack1 || (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.b(itemstack1));
    }

    public boolean doMaterialsMatch(final ItemStack itemstack) {
        return !itemstack.isEmpty() && this.item == itemstack.item && this.damage == itemstack.damage;
    }

    public boolean b(final ItemStack itemstack) {
        return this.f() ? (!itemstack.isEmpty() && this.item == itemstack.item) : this.doMaterialsMatch(itemstack);
    }

    public String a() {
        return this.getItem().a(this);
    }

    @Override
    public String toString() {
        return this.count + "x" + this.getItem().getName() + "@" + this.damage;
    }

    public void a(final World world, final Entity entity, final int i, final boolean flag) {
        if (this.d > 0) {
            --this.d;
        }
        if (this.item != null) {
            this.item.a(this, world, entity, i, flag);
        }
    }

    public void a(final World world, final EntityHuman entityhuman, final int i) {
        entityhuman.a(StatisticList.a(this.item), i);
        this.getItem().b(this, world, entityhuman);
    }

    public int getItemUseMaxDuration() {
        return this.m();
    }

    public int m() {
        return this.getItem().e(this);
    }

    public EnumAnimation n() {
        return this.getItem().f(this);
    }

    public void a(final World world, final EntityLiving entityliving, final int i) {
        this.getItem().a(this, world, entityliving, i);
    }

    public boolean hasTag() {
        return !this.g && this.tag != null;
    }

    @Nullable
    public NBTTagCompound getTag() {
        return this.tag;
    }

    public NBTTagCompound c(final String s) {
        if (this.tag != null && this.tag.hasKeyOfType(s, 10)) {
            return this.tag.getCompound(s);
        }
        final NBTTagCompound nbttagcompound = new NBTTagCompound();
        this.a(s, (NBTBase) nbttagcompound);
        return nbttagcompound;
    }

    @Nullable
    public NBTTagCompound d(final String s) {
        return (this.tag != null && this.tag.hasKeyOfType(s, 10)) ? this.tag.getCompound(s) : null;
    }

    public void e(final String s) {
        if (this.tag != null && this.tag.hasKeyOfType(s, 10)) {
            this.tag.remove(s);
        }
    }

    public NBTTagList getEnchantments() {
        return (this.tag != null) ? this.tag.getList("ench", 10) : new NBTTagList();
    }

    public org.bukkit.inventory.ItemStack asBukkitMirror() {
        return (org.bukkit.inventory.ItemStack) CraftItemStack.asCraftMirror(this);
    }

    public org.bukkit.inventory.ItemStack asBukkitCopy() {
        return (org.bukkit.inventory.ItemStack) CraftItemStack.asCraftMirror(this.cloneItemStack());
    }

    public static ItemStack fromBukkitCopy(final org.bukkit.inventory.ItemStack itemstack) {
        return CraftItemStack.asNMSCopy(itemstack);
    }

    public void setTag(@Nullable final NBTTagCompound nbttagcompound) {
        this.processEnchantOrder(this.tag = nbttagcompound);
    }

    public String getName() {
        final NBTTagCompound nbttagcompound = this.d("display");
        if (nbttagcompound != null) {
            if (nbttagcompound.hasKeyOfType("Name", 8)) {
                return nbttagcompound.getString("Name");
            }
            if (nbttagcompound.hasKeyOfType("LocName", 8)) {
                return LocaleI18n.get(nbttagcompound.getString("LocName"));
            }
        }
        return this.getItem().b(this);
    }

    public ItemStack f(final String s) {
        this.c("display").setString("LocName", s);
        return this;
    }

    public ItemStack g(final String s) {
        this.c("display").setString("Name", s);
        return this;
    }

    public void s() {
        final NBTTagCompound nbttagcompound = this.d("display");
        if (nbttagcompound != null) {
            nbttagcompound.remove("Name");
            if (nbttagcompound.isEmpty()) {
                this.e("display");
            }
        }
        if (this.tag != null && this.tag.isEmpty()) {
            this.tag = null;
        }
    }

    public boolean hasName() {
        final NBTTagCompound nbttagcompound = this.d("display");
        return nbttagcompound != null && nbttagcompound.hasKeyOfType("Name", 8);
    }

    public EnumItemRarity v() {
        return this.getItem().g(this);
    }

    public boolean canEnchant() {
        return this.getItem().g_(this) && !this.hasEnchantments();
    }

    public void addEnchantment(final Enchantment enchantment, final int i) {
        if (this.tag == null) {
            this.setTag(new NBTTagCompound());
        }
        if (!this.tag.hasKeyOfType("ench", 9)) {
            this.tag.set("ench", (NBTBase) new NBTTagList());
        }
        final NBTTagList nbttaglist = this.tag.getList("ench", 10);
        final NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setShort("id", (short) Enchantment.getId(enchantment));
        nbttagcompound.setShort("lvl", (short) (byte) i);
        nbttaglist.add((NBTBase) nbttagcompound);
        this.processEnchantOrder(nbttagcompound);
    }

    public boolean hasEnchantments() {
        return this.tag != null && this.tag.hasKeyOfType("ench", 9) && !this.tag.getList("ench", 10).isEmpty();
    }

    public void a(final String s, final NBTBase nbtbase) {
        if (this.tag == null) {
            this.setTag(new NBTTagCompound());
        }
        this.tag.set(s, nbtbase);
    }

    public boolean y() {
        return this.getItem().s();
    }

    public boolean z() {
        return this.i != null;
    }

    public void a(final EntityItemFrame entityitemframe) {
        this.i = entityitemframe;
    }

    @Nullable
    public EntityItemFrame A() {
        return this.g ? null : this.i;
    }

    public int getRepairCost() {
        return (this.hasTag() && this.tag.hasKeyOfType("RepairCost", 3)) ? this.tag.getInt("RepairCost") : 0;
    }

    public void setRepairCost(final int i) {
        if (i == 0) {
            if (this.hasTag()) {
                this.tag.remove("RepairCost");
            }
            return;
        }
        if (!this.hasTag()) {
            this.tag = new NBTTagCompound();
        }
        this.tag.setInt("RepairCost", i);
    }

    public Multimap<String, AttributeModifier> a(final EnumItemSlot enumitemslot) {
        Object object;
        if (this.hasTag() && this.tag.hasKeyOfType("AttributeModifiers", 9)) {
            object = HashMultimap.create();
            final NBTTagList nbttaglist = this.tag.getList("AttributeModifiers", 10);
            for (int i = 0; i < nbttaglist.size(); ++i) {
                final NBTTagCompound nbttagcompound = nbttaglist.get(i);
                final AttributeModifier attributemodifier = GenericAttributes.a(nbttagcompound);
                if (attributemodifier != null
                        && (!nbttagcompound.hasKeyOfType("Slot", 8)
                        || nbttagcompound.getString("Slot").equals(enumitemslot.d()))
                        && attributemodifier.a().getLeastSignificantBits() != 0L
                        && attributemodifier.a().getMostSignificantBits() != 0L) {
                    ((Multimap) object).put((Object) nbttagcompound.getString("AttributeName"),
                            (Object) attributemodifier);
                }
            }
        } else {
            object = this.getItem().a(enumitemslot);
        }
        return (Multimap<String, AttributeModifier>) object;
    }

    public void a(final String s, final AttributeModifier attributemodifier,
                  @Nullable final EnumItemSlot enumitemslot) {
        if (this.tag == null) {
            this.tag = new NBTTagCompound();
        }
        if (!this.tag.hasKeyOfType("AttributeModifiers", 9)) {
            this.tag.set("AttributeModifiers", (NBTBase) new NBTTagList());
        }
        final NBTTagList nbttaglist = this.tag.getList("AttributeModifiers", 10);
        final NBTTagCompound nbttagcompound = GenericAttributes.a(attributemodifier);
        nbttagcompound.setString("AttributeName", s);
        if (enumitemslot != null) {
            nbttagcompound.setString("Slot", enumitemslot.d());
        }
        nbttaglist.add((NBTBase) nbttagcompound);
    }

    @Deprecated
    public void setItem(final Item item) {
        this.item = item;
        this.setData(this.getData());
    }

    public IChatBaseComponent C() {
        final ChatComponentText chatcomponenttext = new ChatComponentText(this.getName());
        if (this.hasName()) {
            chatcomponenttext.getChatModifier().setItalic(Boolean.valueOf(true));
        }
        final IChatBaseComponent ichatbasecomponent = new ChatComponentText("[")
                .addSibling((IChatBaseComponent) chatcomponenttext).a("]");
        if (!this.g) {
            final NBTTagCompound nbttagcompound = this.save(new NBTTagCompound());
            ichatbasecomponent.getChatModifier()
                    .setChatHoverable(new ChatHoverable(ChatHoverable.EnumHoverAction.SHOW_ITEM,
                            (IChatBaseComponent) new ChatComponentText(nbttagcompound.toString())));
            ichatbasecomponent.getChatModifier().setColor(this.v().e);
        }
        return ichatbasecomponent;
    }

    public boolean a(final Block block) {
        if (block == this.j) {
            return this.k;
        }
        this.j = block;
        if (this.hasTag() && this.tag.hasKeyOfType("CanDestroy", 9)) {
            final NBTTagList nbttaglist = this.tag.getList("CanDestroy", 8);
            for (int i = 0; i < nbttaglist.size(); ++i) {
                final Block block2 = Block.getByName(nbttaglist.getString(i));
                if (block2 == block) {
                    return this.k = true;
                }
            }
        }
        return this.k = false;
    }

    public boolean b(final Block block) {
        if (block == this.l) {
            return this.m;
        }
        this.l = block;
        if (this.hasTag() && this.tag.hasKeyOfType("CanPlaceOn", 9)) {
            final NBTTagList nbttaglist = this.tag.getList("CanPlaceOn", 8);
            for (int i = 0; i < nbttaglist.size(); ++i) {
                final Block block2 = Block.getByName(nbttaglist.getString(i));
                if (block2 == block) {
                    return this.m = true;
                }
            }
        }
        return this.m = false;
    }

    public int D() {
        return this.d;
    }

    public void d(final int i) {
        this.d = i;
    }

    public int getCount() {
        return this.g ? 0 : this.count;
    }

    public void setCount(final int i) {
        this.count = i;
        this.F();
    }

    public void add(final int i) {
        this.setCount(this.count + i);
    }

    public void subtract(final int i) {
        this.add(-i);
    }

    static {
        a = new ItemStack((Item) null);
        b = new DecimalFormat("#.##");
        enchantSorter = Comparator.comparingInt(o -> o.getShort("id"));
    }
}

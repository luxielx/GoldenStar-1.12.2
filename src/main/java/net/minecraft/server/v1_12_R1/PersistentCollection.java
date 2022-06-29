// 
// Decompiled by Procyon v0.5.36
// 

package net.minecraft.server.v1_12_R1;

import com.destroystokyo.paper.exception.ServerInternalException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PersistentCollection {
    private final IDataManager b;
    protected Map<String, PersistentBase> a;
    public final List<PersistentBase> c;
    private final Map<String, Short> d;

    public PersistentCollection(final IDataManager idatamanager) {
        this.a = Maps.newHashMap();
        this.c = Collections.synchronizedList(Lists.newArrayList());
        this.d = Maps.newHashMap();
        this.b = idatamanager;
        this.b();
    }

    @Nullable
    public PersistentBase get(final Class<? extends PersistentBase> oclass, final String s) {
        PersistentBase persistentbase = this.a.get(s);
        if (persistentbase != null) {
            return persistentbase;
        }
        if (this.b != null) {
            try {
                final File file = this.b.getDataFile(s);
                if (file != null && file.exists()) {
                    try {
                        persistentbase = (PersistentBase) oclass.getConstructor(String.class).newInstance(s);
                    } catch (Exception exception) {
                        throw new RuntimeException("Failed to instantiate " + oclass, exception);
                    }
                    final FileInputStream fileinputstream = new FileInputStream(file);
                    final NBTTagCompound nbttagcompound = NBTCompressedStreamTools.a((InputStream) fileinputstream);
                    fileinputstream.close();
                    persistentbase.a(nbttagcompound.getCompound("data"));
                }
            } catch (Exception exception2) {
                exception2.printStackTrace();
                ServerInternalException.reportInternalException((Throwable) exception2);
            }
        }
        if (persistentbase != null) {
            this.a.put(s, persistentbase);
            this.c.add(persistentbase);
        }
        return persistentbase;
    }

    public void a(final String s, final PersistentBase persistentbase) {
        if (this.a.containsKey(s)) {
            this.c.remove(this.a.remove(s));
        }
        this.a.put(s, persistentbase);
        this.c.add(persistentbase);
    }

    public void a() {
        for (int i = 0; i < this.c.size(); ++i) {
            final PersistentBase persistentbase = this.c.get(i);
            if (persistentbase.d()) {
                this.a(persistentbase);
                persistentbase.a(false);
            }
        }
    }

    private void a(final PersistentBase persistentbase) {
        if (this.b != null) {
            try {
                final File file = this.b.getDataFile(persistentbase.id);
                if (file != null) {
                    final NBTTagCompound nbttagcompound = new NBTTagCompound();
                    nbttagcompound.set("data", (NBTBase) persistentbase.b(new NBTTagCompound()));
                    final FileOutputStream fileoutputstream = new FileOutputStream(file);
                    NBTCompressedStreamTools.a(nbttagcompound, (OutputStream) fileoutputstream);
                    fileoutputstream.close();
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                ServerInternalException.reportInternalException((Throwable) exception);
            }
        }
    }

    private void b() {
        try {
            this.d.clear();
            if (this.b == null) {
                return;
            }
            final File file = this.b.getDataFile("idcounts");
            if (file != null && file.exists()) {
                final DataInputStream datainputstream = new DataInputStream(new FileInputStream(file));
                final NBTTagCompound nbttagcompound = NBTCompressedStreamTools.a(datainputstream);
                datainputstream.close();
                for (final String s : nbttagcompound.c()) {
                    final NBTBase nbtbase = nbttagcompound.get(s);
                    if (nbtbase instanceof NBTTagShort) {
                        final NBTTagShort nbttagshort = (NBTTagShort) nbtbase;
                        final short short0 = nbttagshort.f();
                        this.d.put(s, short0);
                    }
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int a(final String s) {
        Short oshort = this.d.get(s);
        if (oshort == null) {
            oshort = 0;
        } else {
            oshort = (short) (oshort + 1);
        }
        this.d.put(s, oshort);
        if (this.b == null) {
            return oshort;
        }
        try {
            final File file = this.b.getDataFile("idcounts");
            if (file != null) {
                final NBTTagCompound nbttagcompound = new NBTTagCompound();
                for (final String s2 : this.d.keySet()) {
                    nbttagcompound.setShort(s2, (short) this.d.get(s2));
                }
                final DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(file));
                NBTCompressedStreamTools.a(nbttagcompound, (DataOutput) dataoutputstream);
                dataoutputstream.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return oshort;
    }
}

package net.minecraft.server.v1_12_R1;

import com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class NBTTagCompound extends NBTBase {

    // ~
    // concurrent hashmap

    private static Logger b;
    private static Pattern c;
    public Map<String, NBTBase> map;

    public NBTTagCompound() {
        this.map = new ConcurrentHashMap<String, NBTBase>();
    }

    void write(DataOutput dataoutput) throws IOException {
        for (String s : this.map.keySet()) {
            NBTBase nbtbase = this.map.get(s);
            a(s, nbtbase, dataoutput);
        }
        dataoutput.writeByte(0);
    }

    void load(DataInput datainput, int i, NBTReadLimiter nbtreadlimiter) throws IOException {
        nbtreadlimiter.a(384L);
        if (i > 512) {
            throw new RuntimeException("Tried to read NBT tag with too high complexity, depth > 512");
        }
        this.map.clear();
        byte b0;
        while ((b0 = a(datainput, nbtreadlimiter)) != 0) {
            String s = b(datainput, nbtreadlimiter);
            nbtreadlimiter.a((long) (224 + 16 * s.length()));
            NBTBase nbtbase = a(b0, s, datainput, i + 1, nbtreadlimiter);
            if (this.map.put(s, nbtbase) != null) {
                nbtreadlimiter.a(288L);
            }
        }
    }

    public Set<String> c() {
        return this.map.keySet();
    }

    public byte getTypeId() {
        return 10;
    }

    public int d() {
        return this.map.size();
    }

    public void set(String s, NBTBase nbtbase) {
        this.map.put(s, nbtbase);
    }

    public void setByte(String s, byte b0) {
        this.map.put(s, (NBTBase) new NBTTagByte(b0));
    }

    public void setShort(String s, short short0) {
        this.map.put(s, (NBTBase) new NBTTagShort(short0));
    }

    public void setInt(String s, int i) {
        this.map.put(s, (NBTBase) new NBTTagInt(i));
    }

    public void setLong(String s, long i) {
        this.map.put(s, (NBTBase) new NBTTagLong(i));
    }

    public void setUUID(String prefix, UUID uuid) {
        this.a(prefix, uuid);
    }

    public void a(String s, UUID uuid) {
        this.setLong(s + "Most", uuid.getMostSignificantBits());
        this.setLong(s + "Least", uuid.getLeastSignificantBits());
    }

    public UUID getUUID(String prefix) {
        return this.a(prefix);
    }

    @Nullable
    public UUID a(String s) {
        return new UUID(this.getLong(s + "Most"), this.getLong(s + "Least"));
    }

    public boolean hasUUID(String s) {
        return this.b(s);
    }

    public boolean b(String s) {
        return this.hasKeyOfType(s + "Most", 99) && this.hasKeyOfType(s + "Least", 99);
    }

    public void setFloat(String s, float f) {
        this.map.put(s, (NBTBase) new NBTTagFloat(f));
    }

    public void setDouble(String s, double d0) {
        this.map.put(s, (NBTBase) new NBTTagDouble(d0));
    }

    public void setString(String s, String s1) {
        this.map.put(s, (NBTBase) new NBTTagString(s1));
    }

    public void setByteArray(String s, byte[] abyte) {
        this.map.put(s, (NBTBase) new NBTTagByteArray(abyte));
    }

    public void setIntArray(String s, int[] aint) {
        this.map.put(s, (NBTBase) new NBTTagIntArray(aint));
    }

    public void setBoolean(String s, boolean flag) {
        this.setByte(s, (byte) (flag ? 1 : 0));
    }

    public NBTBase get(String s) {
        return this.map.get(s);
    }

    public byte d(String s) {
        NBTBase nbtbase = this.map.get(s);
        return (byte) ((nbtbase == null) ? 0 : nbtbase.getTypeId());
    }

    public boolean hasKey(String s) {
        return this.map.containsKey(s);
    }

    public boolean hasKeyOfType(String s, int i) {
        byte b0 = this.d(s);
        return b0 == i || (i == 99 && (b0 == 1 || b0 == 2 || b0 == 3 || b0 == 4 || b0 == 5 || b0 == 6));
    }

    public byte getByte(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).g();
            }
        } catch (ClassCastException ex) {
        }
        return 0;
    }

    public short getShort(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).f();
            }
        } catch (ClassCastException ex) {
        }
        return 0;
    }

    public int getInt(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).e();
            }
        } catch (ClassCastException ex) {
        }
        return 0;
    }

    public long getLong(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).d();
            }
        } catch (ClassCastException ex) {
        }
        return 0L;
    }

    public float getFloat(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).i();
            }
        } catch (ClassCastException ex) {
        }
        return 0.0f;
    }

    public double getDouble(String s) {
        try {
            if (this.hasKeyOfType(s, 99)) {
                return ((NBTNumber) this.map.get(s)).asDouble();
            }
        } catch (ClassCastException ex) {
        }
        return 0.0;
    }

    public String getString(String s) {
        try {
            if (this.hasKeyOfType(s, 8)) {
                return this.map.get(s).c_();
            }
        } catch (ClassCastException ex) {
        }
        return "";
    }

    public byte[] getByteArray(String s) {
        try {
            if (this.hasKeyOfType(s, 7)) {
                return ((NBTTagByteArray) this.map.get(s)).c();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 7, classcastexception));
        }
        return new byte[0];
    }

    public int[] getIntArray(String s) {
        try {
            if (this.hasKeyOfType(s, 11)) {
                return ((NBTTagIntArray) this.map.get(s)).d();
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 11, classcastexception));
        }
        return new int[0];
    }

    public NBTTagCompound getCompound(String s) {
        try {
            if (this.hasKeyOfType(s, 10)) {
                return (NBTTagCompound) this.map.get(s);
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 10, classcastexception));
        }
        return new NBTTagCompound();
    }

    public NBTTagList getList(String s, int i) {
        try {
            if (this.d(s) == 9) {
                NBTTagList nbttaglist = (NBTTagList) this.map.get(s);
                if (!nbttaglist.isEmpty() && nbttaglist.g() != i) {
                    return new NBTTagList();
                }
                return nbttaglist;
            }
        } catch (ClassCastException classcastexception) {
            throw new ReportedException(this.a(s, 9, classcastexception));
        }
        return new NBTTagList();
    }

    public boolean getBoolean(String s) {
        return this.getByte(s) != 0;
    }

    public void remove(String s) {
        this.map.remove(s);
    }

    public String toString() {
        StringBuilder stringbuilder = new StringBuilder("{");
        Set<String> object = this.map.keySet();
        if (NBTTagCompound.b.isDebugEnabled()) {
            ArrayList arraylist = Lists.newArrayList((Iterable) this.map.keySet());
            Collections.sort((List<Comparable>) arraylist);
            object = (Set<String>) arraylist;
        }
        for (String s : object) {
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }
            stringbuilder.append(s(s)).append(':').append(this.map.get(s));
        }
        return stringbuilder.append('}').toString();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    private CrashReport a(String s, int i, ClassCastException classcastexception) {
        CrashReport crashreport = CrashReport.a((Throwable) classcastexception, "Reading NBT data");
        CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Corrupt NBT tag", 1);
        crashreportsystemdetails.a("Tag type found", (CrashReportCallable) new CrashReportCallable() {
            public String a() throws Exception {
                return NBTBase.a[NBTTagCompound.this.map.get(s).getTypeId()];
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Tag type expected", (CrashReportCallable) new CrashReportCallable() {
            public String a() throws Exception {
                return NBTBase.a[i];
            }

            public Object call() throws Exception {
                return this.a();
            }
        });
        crashreportsystemdetails.a("Tag name", (Object) s);
        return crashreport;
    }

    public NBTTagCompound g() {
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        for (String s : this.map.keySet()) {
            nbttagcompound.set(s, this.map.get(s).clone());
        }
        return nbttagcompound;
    }

    public boolean equals(Object object) {
        return super.equals(object) && Objects.equals(this.map.entrySet(), ((NBTTagCompound) object).map.entrySet());
    }

    public int hashCode() {
        return super.hashCode() ^ this.map.hashCode();
    }

    private static void a(String s, NBTBase nbtbase, DataOutput dataoutput) throws IOException {
        dataoutput.writeByte(nbtbase.getTypeId());
        if (nbtbase.getTypeId() != 0) {
            dataoutput.writeUTF(s);
            nbtbase.write(dataoutput);
        }
    }

    private static byte a(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        return datainput.readByte();
    }

    private static String b(DataInput datainput, NBTReadLimiter nbtreadlimiter) throws IOException {
        return datainput.readUTF();
    }

    static NBTBase a(byte b0, String s, DataInput datainput, int i,
                     NBTReadLimiter nbtreadlimiter) throws IOException {
        NBTBase nbtbase = NBTBase.createTag(b0);
        try {
            nbtbase.load(datainput, i, nbtreadlimiter);
            return nbtbase;
        } catch (IOException ioexception) {
            CrashReport crashreport = CrashReport.a((Throwable) ioexception, "Loading NBT data");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("NBT Tag");
            crashreportsystemdetails.a("Tag name", (Object) s);
            crashreportsystemdetails.a("Tag type", (Object) b0);
            throw new ReportedException(crashreport);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        for (String s : nbttagcompound.map.keySet()) {
            NBTBase nbtbase = nbttagcompound.map.get(s);
            if (nbtbase.getTypeId() == 10) {
                if (this.hasKeyOfType(s, 10)) {
                    NBTTagCompound nbttagcompound2 = this.getCompound(s);
                    nbttagcompound2.a((NBTTagCompound) nbtbase);
                } else {
                    this.set(s, nbtbase.clone());
                }
            } else {
                this.set(s, nbtbase.clone());
            }
        }
    }

    protected static String s(String s) {
        return NBTTagCompound.c.matcher(s).matches() ? s : NBTTagString.a(s);
    }

    public NBTBase clone() {
        return this.g();
    }

    static {
        b = LogManager.getLogger();
        c = Pattern.compile("[A-Za-z0-9._+-]+");
    }
}

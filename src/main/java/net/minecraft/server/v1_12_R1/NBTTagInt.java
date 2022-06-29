package net.minecraft.server.v1_12_R1;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class NBTTagInt extends NBTNumber {
    private volatile int data;

    NBTTagInt() {
    }

    public NBTTagInt(final int data) {
        this.data = data;
    }

    void write(final DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data);
    }

    void load(final DataInput dataInput, final int n, final NBTReadLimiter nbtReadLimiter) throws IOException {
        nbtReadLimiter.a(96L);
        this.data = dataInput.readInt();
    }

    public byte getTypeId() {
        return 3;
    }

    public String toString() {
        return String.valueOf(this.data);
    }

    public NBTTagInt c() {
        return new NBTTagInt(this.data);
    }

    public boolean equals(final Object o) {
        return super.equals(o) && this.data == ((NBTTagInt) o).data;
    }

    public int hashCode() {
        return super.hashCode() ^ this.data;
    }

    public long d() {
        return this.data;
    }

    public int e() {
        return this.data;
    }

    public short f() {
        return (short) (this.data & 0xFFFF);
    }

    public byte g() {
        return (byte) (this.data & 0xFF);
    }

    public double asDouble() {
        return this.data;
    }

    public float i() {
        return this.data;
    }

    @Override
    public NBTBase clone() {
        return new NBTTagInt(data);
    }
}
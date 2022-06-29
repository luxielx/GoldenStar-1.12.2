// 
// Decompiled by Procyon v0.5.30
// 

package net.minecraft.server.v1_12_R1;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AttributeModifiable implements AttributeInstance {
    private AttributeMapBase a;
    private IAttribute b;
    private Map<Integer, Set<AttributeModifier>> c;
    private Map<String, Set<AttributeModifier>> d;
    private Map<UUID, AttributeModifier> e;
    private double f;
    private boolean g;
    private double h;

    public AttributeModifiable(AttributeMapBase a, IAttribute b) {
        // ~
        this.c = Collections.synchronizedMap(new HashMap<Integer, Set<AttributeModifier>>()); // new
        // ConcurrentHashMap<Integer,
        // Set<AttributeModifier>>();
        this.d = Maps.newHashMap();
        this.e = Maps.newHashMap();
        this.g = true;
        this.a = a;
        this.b = b;
        this.f = b.getDefault();
        for (int i = 0; i < 3; ++i) {
            // ~
            this.c.put(i, Sets.newConcurrentHashSet());
        }
    }

    public IAttribute getAttribute() {
        return this.b;
    }

    public double b() {
        return this.f;
    }

    public void setValue(double f) {
        if (f == this.b()) {
            return;
        }
        this.f = f;
        this.f();
    }

    public Collection<AttributeModifier> a(int n) {
        return this.c.get(n);
    }

    public Collection<AttributeModifier> c() {
        HashSet<AttributeModifier> hashSet = Sets.newHashSet();
        for (int i = 0; i < 3; ++i) {
            hashSet.addAll(this.a(i));
        }
        return (Collection<AttributeModifier>) hashSet;
    }

    @Nullable
    public AttributeModifier a(UUID uuid) {
        return this.e.get(uuid);
    }

    public boolean a(AttributeModifier attributeModifier) {
        return this.e.get(attributeModifier.a()) != null;
    }

    public void b(AttributeModifier attributeModifier) {
        if (this.a(attributeModifier.a()) != null) {
            throw new IllegalArgumentException("Modifier is already applied on this attribute!");
        }
        Set<AttributeModifier> hashSet = this.d.get(attributeModifier.b());
        if (hashSet == null) {
            hashSet = Sets.newHashSet();
            this.d.put(attributeModifier.b(), hashSet);
        }
        this.c.get(attributeModifier.c()).add(attributeModifier);
        hashSet.add(attributeModifier);
        this.e.put(attributeModifier.a(), attributeModifier);
        this.f();
    }

    protected void f() {
        this.g = true;
        this.a.a((AttributeInstance) this);
    }

    public void c(AttributeModifier attributeModifier) {
        for (int i = 0; i < 3; ++i) {
            this.c.get(i).remove(attributeModifier);
        }
        Set<AttributeModifier> set = this.d.get(attributeModifier.b());
        if (set != null) {
            set.remove(attributeModifier);
            if (set.isEmpty()) {
                this.d.remove(attributeModifier.b());
            }
        }
        this.e.remove(attributeModifier.a());
        this.f();
    }

    public void b(UUID uuid) {
        AttributeModifier a = this.a(uuid);
        if (a != null) {
            this.c(a);
        }
    }

    public double getValue() {
        if (this.g) {
            this.h = this.g();
            this.g = false;
        }
        return this.h;
    }

    private double g() {
        double b = this.b();
        Iterator<AttributeModifier> iterator = this.b(0).iterator();
        while (iterator.hasNext()) {
            b += iterator.next().d();
        }
        double n = b;
        Iterator<AttributeModifier> iterator2 = this.b(1).iterator();
        while (iterator2.hasNext()) {
            n += b * iterator2.next().d();
        }
        Iterator<AttributeModifier> iterator3 = this.b(2).iterator();
        while (iterator3.hasNext()) {
            n *= 1.0 + iterator3.next().d();
        }
        return this.b.a(n);
    }

    private Collection<AttributeModifier> b(int n) {
        HashSet<AttributeModifier> hashSet = Sets.newHashSet((Iterable<AttributeModifier>) this.a(n));
        for (IAttribute attribute = this.b.d(); attribute != null; attribute = attribute.d()) {
            AttributeInstance a = this.a.a(attribute);
            if (a != null) {
                hashSet.addAll(a.a(n));
            }
        }
        return (Collection<AttributeModifier>) hashSet;
    }
}

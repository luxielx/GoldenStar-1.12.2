package net.minecraft.server.v1_12_R1;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AttributeMapServer extends AttributeMapBase {

    // ~
    // concurrent hashset e

    private Set<AttributeInstance> e;
    protected Map<String, AttributeInstance> d;

    public AttributeMapServer() {
        // ~
        this.e = Collections.synchronizedSet(new HashSet<AttributeInstance>());
        this.d = new InsensitiveStringMap<AttributeInstance>();
    }

    public AttributeModifiable e(IAttribute attribute) {
        return (AttributeModifiable) super.a(attribute);
    }

    public AttributeModifiable b(String s) {
        AttributeInstance a = super.a(s);
        if (a == null) {
            a = this.d.get(s);
        }
        return (AttributeModifiable) a;
    }

    public AttributeInstance b(IAttribute attribute) {
        AttributeInstance b = super.b(attribute);
        if (attribute instanceof AttributeRanged && ((AttributeRanged) attribute).g() != null) {
            this.d.put(((AttributeRanged) attribute).g(), b);
        }
        return b;
    }

    protected AttributeInstance c(IAttribute attribute) {
        return (AttributeInstance) new AttributeModifiable((AttributeMapBase) this, attribute);
    }

    public void a(AttributeInstance attributeInstance) {
        if (attributeInstance.getAttribute().c()) {
            this.e.add(attributeInstance);
        }
        Iterator<IAttribute> iterator = this.c.get(attributeInstance.getAttribute()).iterator();
        while (iterator.hasNext()) {
            AttributeModifiable e = this.e(iterator.next());
            if (e != null) {
                e.f();
            }
        }
    }

    public Set<AttributeInstance> getAttributes() {
        return this.e;
    }

    public Collection<AttributeInstance> c() {
        HashSet<AttributeInstance> hashSet = Sets.newHashSet();
        for (AttributeInstance attributeInstance : this.a()) {
            if (attributeInstance.getAttribute().c()) {
                hashSet.add(attributeInstance);
            }
        }
        return (Collection<AttributeInstance>) hashSet;
    }
}

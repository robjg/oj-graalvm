package org.oddjob.graalvm;

import org.oddjob.arooa.registry.BeanRegistry;

import javax.script.Bindings;
import java.util.*;

/**
 * Provide script {@link Bindings} from an {@link org.oddjob.arooa.ArooaSession}'s
 * {@link BeanRegistry}.
 */
public class GraalSessionBindings implements Bindings {

    private final BeanRegistry beanRegistry;

    private final Map<String, Object> map;

    private GraalSessionBindings(BeanRegistry beanRegistry,
                                 Map<String, Object> map) {
        this.beanRegistry = beanRegistry;
        this.map = Objects.requireNonNull(map);
    }


    public static Bindings from(BeanRegistry beanRegistry) {

        Map<String, Object> map = new HashMap<>();
        for(String id : beanRegistry.getAllIds()) {
            map.put(id, beanRegistry.lookup(id));
        }

        return new GraalSessionBindings(beanRegistry, map);
    }

    @Override
    public Object put(String name, Object value) {
        // Graal puts all sorts of properties into the bindings
        if (!name.contains(".")) {
            beanRegistry.register(name, value);
        }
        return map.put(name, value);
    }

    @Override
    public void putAll(Map<? extends String, ?> toMerge) {
        for (Map.Entry<? extends String, ?> entry : toMerge.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public Object get(Object key) {
            return map.get(key);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Remove not Supported");
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Read Only");
    }

    @Override
    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<Object> values() {
        return map.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
}

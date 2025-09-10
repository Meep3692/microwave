package ca.awoo.microwave.hell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ECS {
    private static class Component implements Comparable<Component>{
        public final long entity;
        public final Object component;
        public Component(long entity, Object component) {
            this.entity = entity;
            this.component = component;
        }
        @Override
        public int compareTo(Component o) {
            return (int) (entity - o.entity);
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (entity ^ (entity >>> 32));
            result = prime * result + ((component == null) ? 0 : component.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Component other = (Component) obj;
            if (entity != other.entity)
                return false;
            if (component == null) {
                if (other.component != null)
                    return false;
            } else if (!component.equals(other.component))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "Component [entity=" + entity + ", component=" + component + "]";
        }
    }

    private static class ShadowComp {
        public final Class<?> type;
        public final Component component;
        public ShadowComp(Class<?> type, Component component) {
            this.type = type;
            this.component = component;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            result = prime * result + ((component == null) ? 0 : component.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ShadowComp other = (ShadowComp) obj;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            if (component == null) {
                if (other.component != null)
                    return false;
            } else if (!component.equals(other.component))
                return false;
            return true;
        }
        @Override
        public String toString() {
            return "ShadowComp [type=" + type + ", component=" + component + "]";
        }
    }
    private final Map<Class<?>, Map<Long, Object>> components;
    private final Set<ShadowComp> shadow;
    private final Set<ShadowComp> shadowRemove;
    private final Set<Long> shadowKill;
    private long lastEntity = 0;

    private final List<Runnable> eventqueue;
    private final Map<Object, List<Runnable>> removeListeners;

    public ECS(){
        this.components = new HashMap<>();
        this.shadow = new HashSet<>();
        this.shadowRemove = new HashSet<>();
        this.shadowKill = new HashSet<>();
        removeListeners = new HashMap<>();
        eventqueue = new ArrayList<>();
    }

    public void clear(){
        components.clear();
        shadow.clear();
        shadowRemove.clear();
        shadowKill.clear();
        removeListeners.clear();
        eventqueue.clear();
    }
    private long queryAnswers = 0;

    private int queryLayers = 0;

    public synchronized void query(ECSSystem system, Class<?>... comps){
        if(queryLayers == 0){
            synchronized(shadowKill){
                for(long entity : shadowKill){
                    for(Map<Long, Object> map : components.values()){
                        map.remove(entity);
                    }
                }
                shadowKill.clear();
            }
            synchronized(shadowRemove){
                for(ShadowComp s : shadowRemove){
                    if(!components.containsKey(s.type)){
                        continue;
                    }
                    Map<Long, Object> set = components.get(s.type);
                    set.remove(s.component.entity);
                    List<Runnable> listeners = removeListeners.get(s.component.component);
                    if(listeners != null){
                        for(Runnable listener : listeners){
                            eventqueue.add(listener);
                        }
                        removeListeners.remove(s.component.component);
                    }
                }
                shadowRemove.clear();
            }
            synchronized(shadow){
                for(ShadowComp s : shadow){
                    if(!components.containsKey(s.type)){
                        components.put(s.type, new HashMap<>());
                    }
                    Map<Long, Object> map = components.get(s.type);
                    map.put(s.component.entity, s.component.component);
                }
                shadow.clear();
            }
            for(Runnable event : eventqueue){
                event.run();
            }
            eventqueue.clear();
        }
        queryLayers++;
        try{
            @SuppressWarnings("unchecked")
            Map<Long, Object>[] maps = new Map[comps.length];
            for(int i = 0; i < comps.length; i++){
                Map<Long, Object> map = components.get(comps[i]);
                if(map == null){
                    return;
                }
                maps[i] = map;
            }

            Map<Long, Object> smallest = maps[0];

            for(int i = 1; i < maps.length; i++){
                if(maps[i].size() < smallest.size()){
                    smallest = maps[i];
                }
            }

            Object[] components = new Object[comps.length];

            for(long entity : smallest.keySet()){
                int i;
                for(i = 0; i < comps.length; i++){
                    components[i] = maps[i].get(entity);
                    if(components[i] == null){
                        break;
                    }
                }
                if(i < comps.length){
                    continue;
                }
                queryAnswers++;
                system.run(entity, components);
            }
        }finally{
            queryLayers--;
        }
    }

    public long getQueryAnswers(){
        long answers = queryAnswers;
        queryAnswers = 0;
        return answers;
    }

    public <T> void addComponent(long entity, T component){
        synchronized(shadow){
            Class<?> type = component.getClass();
            shadow.add(new ShadowComp(type, new Component(entity, component)));
        }
    }

    public <T> void removeComponent(long entity, T component){
        synchronized(shadowRemove){
            Class<?> type = component.getClass();
            shadowRemove.add(new ShadowComp(type, new Component(entity, component)));
        }
    }

    public long createEntity(){
        return lastEntity++;
    }

    public void removeEntity(long entity){
        synchronized(shadowKill){
            shadowKill.add(entity);
        }
    }

    public void onRemove(Object component, Runnable listener){
        List<Runnable> listeners = removeListeners.get(component);
        if(listeners == null){
            listeners = new ArrayList<>();
            removeListeners.put(component, listeners);
        }
        listeners.add(listener);
    }
}

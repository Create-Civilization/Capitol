package wiiu.mavity.util;

import java.util.function.*;

public class ObjectHolder<V> {

    private V value;

    public ObjectHolder(V initialValue) {
        this.value = initialValue;
    }

    public ObjectHolder() {
        this.value = null;
    }

    public V get() {
        return this.value;
    }

    public void set(V newValue) {
        this.value = newValue;
    }

    public V getOrDefault(V defaultValue) {
        var value = this.get();
        return value != null ? value : defaultValue;
    }

    public String getAsString() {
        return String.valueOf(this.get());
    }

    public void ifPresent(Consumer<V> consumer) {
        var value = this.get();
        if (value != null) consumer.accept(value);
    }

    public void ifPresentOrElse(Consumer<V> consumer, EmptyFunctionalInterface emptyConsumer) {
        var value = this.get();
        if (value != null) consumer.accept(value);
        else emptyConsumer.function();
    }

    public <R> R ifPresentOrElse(Function<V, R> consumer, AdaptiveEmptyFunctionalInterface<R> emptyConsumer) {
        var value = this.get();
        if (value != null) return consumer.apply(value);
        else return emptyConsumer.function();
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{value=" + this.getAsString() + "}";
    }
}
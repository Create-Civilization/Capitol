package wiiu.mavity.util;

import org.jetbrains.annotations.*;

import java.util.function.*;

public class ObjectHolder<V> {

    private @Nullable V value;

    public ObjectHolder(@Nullable V initialValue) {
        this.set(initialValue);
    }

    public ObjectHolder() {
        this.set(null);
    }

    public @Nullable V get() {
        return this.value;
    }

    public void set(@Nullable V newValue) {
        this.value = newValue;
    }

    public V getOrDefault(@NotNull V defaultValue) {
        var value = this.get();
        return value != null ? value : defaultValue;
    }

    public String getAsString() {
        return String.valueOf(this.get());
    }

    public boolean isPresent() {
        return this.get() != null;
    }

    public void ifPresent(@NotNull Consumer<V> consumer) {
        if (this.isPresent()) consumer.accept(this.get());
    }

    public void ifPresentOrElse(@NotNull Consumer<V> consumer, @NotNull EmptyFunctionalInterface emptyConsumer) {
        if (this.isPresent()) consumer.accept(this.get());
        else emptyConsumer.function();
    }

    public <R> R ifPresentOrElse(@NotNull Function<V, R> consumer, @NotNull AdaptiveEmptyFunctionalInterface<R> emptyConsumer) {
        if (this.isPresent()) return consumer.apply(this.get());
        else return emptyConsumer.function();
    }

    public String toString() {
        return this.getClass().getSimpleName() + "{value=" + this.getAsString() + "}";
    }
}
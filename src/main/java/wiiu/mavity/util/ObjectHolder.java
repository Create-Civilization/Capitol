package wiiu.mavity.util;

import org.jetbrains.annotations.*;

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
        return this.isPresent() ? this.get() : defaultValue;
    }

	public V getOrThrow() {
		if (this.isEmpty()) throw new NullPointerException("Value was null!");
		return this.get();
	}

    public String getAsString() {
        return String.valueOf(this.get());
    }

    public boolean isPresent() {
        return !this.isEmpty();
    }

	public boolean isEmpty() {
		return this.get() == null;
	}

    public void ifPresent(@NotNull IfPresentConsumer<V> consumer) {
        consumer.acceptOrDoNothing(this.get());
    }

    public void ifPresentOrElse(@NotNull IfPresentConsumer<V> consumer, @NotNull EmptyFunctionalInterface emptyConsumer) {
        consumer.acceptOrElse(this.get(), emptyConsumer);
    }

    public <R> R ifPresentOrElse(@NotNull IfPresentFunction<V, R> function, @NotNull AdaptiveEmptyFunctionalInterface<R> emptyConsumer) {
		return function.applyOrElse(this.get(), emptyConsumer);
    }

	public Class<?> getType() {
		return this.ifPresentOrElse(V::getClass, () -> Void.class);
	}

    public String toString() {
        return this.getClass().getSimpleName() + "{value=" + this.getAsString() + "}";
    }
}
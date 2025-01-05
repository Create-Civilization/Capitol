package wiiu.mavity.util;

import org.jetbrains.annotations.*;

import java.util.Objects;

public class ObjectHolder<V> implements ObjectHolderLike<V> {

    private @Nullable V value;

    public ObjectHolder(@Nullable V initialValue) {
        this.set(initialValue);
    }

    public ObjectHolder() {
        this.set(null);
    }

	@Override
    public @Nullable V get() {
        return this.value;
    }

	@Override
    public void set(@Nullable V newValue) {
        this.value = newValue;
    }

	public void setFrom(ObjectHolderLike<V> other) {
		this.set(other.get());
	}

	@Override
	public ObjectHolder<V> getHolder() {
		return this;
	}

	public V getOrDefault(@NotNull V defaultValue) {
        return this.isPresent() ? this.get() : defaultValue;
    }

	public @NotNull V getOrThrow() {
		return this.getOrThrow("Value was null!");
	}

	public @NotNull V getOrThrow(String message) {
		return Objects.requireNonNull(this.get(), message);
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
		return this.ifPresentOrElse(V::getClass, () -> Void.TYPE);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof ObjectHolder<?> other)) return false;
		return this.getType() == other.getType();
	}

	public boolean deepEquals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof ObjectHolder<?> other)) return false;
		if (this.getType() != other.getType()) return false;
		if (this.isPresent() && other.isPresent()) return this.getOrThrow().equals(other.getOrThrow());
		return false;
	}

	@Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode()) + "{value=" + this.getAsString() + "}";
    }
}
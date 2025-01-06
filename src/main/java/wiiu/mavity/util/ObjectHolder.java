package wiiu.mavity.util;

import org.jetbrains.annotations.*;
import org.jetbrains.annotations.ApiStatus.*;

import java.util.Objects;

/**
 * A holder class to contain potentially null values of a given object type, with a few extra features for convenience and utility.
 * @param <V> The type of the value.
 */
public class ObjectHolder<V> implements ObjectHolderLike<V> {

	/**
	 * The value contained by this holder.
	 */
    private @Nullable V value;

	/**
	 * Creates a new holder with the initial value set to the one specified in the parameters.
	 * @param initialValue The initial value to set.
	 */
    public ObjectHolder(@Nullable V initialValue) {
        this.set(initialValue);
    }

	/**
	 * Creates a new holder with a null value.
	 */
    public ObjectHolder() {
        this.set(null);
    }

	/**
	 * @return The value contained in this holder.
	 */
	@Override
    public @Nullable V get() {
        return this.value;
    }

	/**
	 * Sets the value contained in this holder to the one specified in the parameters.
	 * @param newValue The new value to set.
	 */
	@Override
    public void set(@Nullable V newValue) {
        this.value = newValue;
    }

	/**
	 * Sets the value contained in this holder to the one contained in the holder specified in the parameters.
	 * @param other The holder to copy the value from.
	 */
	public void setFrom(ObjectHolderLike<V> other) {
		this.set(other.get());
	}

	/**
	 * For usage with {@link ObjectHolderLike} objects.
	 */
	@Override
	public ObjectHolder<V> getHolder() {
		return this;
	}

	/**
	 * @param defaultValue The value to return if the value contained in this holder is null.
	 * @return The value contained in this holder as a non-null value, or the default value if the value is null.
	 */
	public V getOrDefault(@NotNull V defaultValue) {
        return this.isPresent() ? this.get() : defaultValue;
    }

	/**
	 * @return The value contained in this holder as a non-null value, or throws an exception if the value is null.
	 * @throws NullPointerException If the value contained in this object is null.
	 */
	public @NotNull V getOrThrow() throws NullPointerException {
		return this.getOrThrow("Value was null!");
	}

	/**
	 * @return The value contained in this holder as a non-null value, or throws an exception if the value is null.
	 * @param message The message to add to the exception if the value is null.
	 * @throws NullPointerException If the value contained in this object is null.
	 */
	public @NotNull V getOrThrow(String message) throws NullPointerException {
		return Objects.requireNonNull(this.get(), message);
	}

	/**
	 * @return A string representation of the value contained in this holder, see {@link String#valueOf(Object)} for formatting.
	 */
    public String getAsString() {
        return String.valueOf(this.get());
    }

	/**
	 * @return A string representation of the value contained in this holder, formatted for JSON usage.
	 */
	public String getAsJsonString() {
		return this.ifPresentOrElse((value) -> {
			if (!(value instanceof Boolean) && !(value instanceof Integer)) return "\"" + value + "\"";
			else return String.valueOf(value);
		}, () -> "null");
	}

	/**
	 * Forcibly casts an object to the type of the value contained in this holder.
	 * @implNote This method is here for internal use only, DO NOT USE THIS METHOD WHATSOEVER.
	 */
	@SuppressWarnings("unchecked")
	@Experimental
	@Internal
	@NonExtendable
	@VisibleForTesting
	public final void forceSet(Object o) {
		this.set((V) o);
	}

	/**
	 * @return If the value contained in this holder is present (is not null).
	 */
    public boolean isPresent() {
        return !this.isEmpty();
    }

	/**
	 * @return If the value contained in this holder is not present (is null).
	 */
	public boolean isEmpty() {
		return this.get() == null;
	}

	/**
	 * Executes a consumer if the value contained in this holder is present (is not null).
	 * @param consumer The consumer to execute.
	 */
    public void ifPresent(@NotNull IfPresentConsumer<V> consumer) {
        consumer.acceptOrDoNothing(this.get());
    }

	/**
	 * Executes a consumer if the value contained in this holder is present (is not null), otherwise executes an empty function.
	 * @param consumer The consumer to execute.
	 * @param emptyFunction The empty function to execute.
	 */
    public void ifPresentOrElse(@NotNull IfPresentConsumer<V> consumer, @NotNull EmptyFunctionalInterface emptyFunction) {
        consumer.acceptOrElse(this.get(), emptyFunction);
    }

	/**
	 * Executes a function if the value contained in this holder is present (is not null), otherwise executes an empty function.
	 * @param function The function to execute.
	 * @param emptyFunction The empty function to execute.
	 */
    public <R> R ifPresentOrElse(@NotNull IfPresentFunction<V, R> function, @NotNull AdaptiveEmptyFunctionalInterface<R> emptyFunction) {
		return function.applyOrElse(this.get(), emptyFunction);
    }

	/**
	 * @return The class of the value contained in this holder, or {@link Void#TYPE} if the value is null.
	 */
	public Class<?> getType() {
		return this.ifPresentOrElse(V::getClass, () -> Void.TYPE);
	}

	/**
	 * @param obj The object to check against.
	 * @return Whether the object is a holder of the same type, excluding the value contained in the holder.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof ObjectHolder<?> other)) return false;
		return this.getType() == other.getType();
	}

	/**
	 * @param obj The object to check against.
	 * @return Whether the object is a holder of the same type, including the value contained in the holder.
	 */
	public boolean deepEquals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof ObjectHolder<?> other)) return false;
		if (this.getType() != other.getType()) return false;
		if (this.isPresent() && other.isPresent()) return this.getOrThrow().equals(other.getOrThrow());
		return false;
	}

	/**
	 * @return a string representation of the object, formatted as '{@code ObjectHolder@hashCode{value=value}}'.
	 */
	@Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(this.hashCode()) + "{value=" + this.getAsString() + "}";
    }
}
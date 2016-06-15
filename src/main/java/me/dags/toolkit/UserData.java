package me.dags.toolkit;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Data that only needs to be held in memory and not written to disk
 */
public class UserData {

    private final Map<String, Object> data = new HashMap<>();

    /**
     * Get a value by name
     *
     * @param name
     *            the name of the value to get
     * @return the value, or null if absent or of a different type
     */
    public <T> T getRaw(String name) {
        Object value = data.get(name);
        try {
            @SuppressWarnings("unchecked")
            T t = (T) value;
            return t;
        } catch (ClassCastException e) {
            return null;
        }
    }

    /**
     * Get a value by name
     *
     * @param name
     *            the name of the value
     * @return the Optional value
     */
    public <T> Optional<T> get(String name) {
        T value = getRaw(name);
        return Optional.ofNullable(value);
    }

    /**
     * Get a value by name, or add one from the provided supplier if absent
     *
     * @param name
     *            the name of the value to get
     * @param supplier
     *            the supplier that provides a new value if it is absent
     * @return the value
     */
    public <T> T get(String name, Supplier<T> supplier) {
        T value = getRaw(name);
        if (value == null) {
            data.put(name, value = supplier.get());
        }
        return value;
    }

    /**
     * Get a value by name, or return a default value if absent
     *
     * @param name
     *            the name of the value to look up
     * @param defaultValue
     *            the default value
     * @return the value
     */
    public <T> T getOrElse(String name, T defaultValue) {
        T value = getRaw(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Transforms the current value of the option
     *
     * @param name
     *            the name of the value to transform
     * @param transformer
     *            the function that transforms the current value to another
     * @return the resulting value of the transformation - may be null
     */
    public <T> T transformRaw(String name, Function<T, T> transformer) {
        T value = getRaw(name);
        if (value != null) {
            data.put(name, value = transformer.apply(value));
        }
        return value;
    }

    /**
     * Transforms the current value of the option, or the supplied value if
     * absent
     *
     * @param name
     *            the name of the value to transform
     * @param transformer
     *            the function that transforms the current value to another
     * @param supplier
     *            a supplier that provides a default value if it isn't currently
     *            present
     * @return the transformed value
     */
    public <T> T transform(String name, Function<T, T> transformer, Supplier<T> supplier) {
        T value = get(name, supplier);
        data.put(name, value = transformer.apply(value));
        return value;
    }

    /**
     * Remove a value by name
     *
     * @param name
     *            the name of the value to remove
     * @return true if the value was present and has been removed, else false
     */
    public boolean remove(String name) {
        return data.remove(name) != null;
    }

    /**
     * Set the value of a named value
     *
     * @param name
     *            the name of the value to set
     * @param value
     *            the value
     */
    public void set(String name, Object value) {
        data.put(name, value);
    }

    @Override
    public String toString() {
        return data.toString();
    }
}

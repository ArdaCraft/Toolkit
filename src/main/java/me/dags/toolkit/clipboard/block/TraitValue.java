package me.dags.toolkit.clipboard.block;

/**
 * @author dags <dags@dags.me>
 */
public interface TraitValue {

    default boolean matches(Object object) {
        return object.toString().equals(name());
    }

    String name();
}
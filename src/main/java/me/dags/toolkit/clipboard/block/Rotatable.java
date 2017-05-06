package me.dags.toolkit.clipboard.block;

/**
 * @author dags <dags@dags.me>
 */
public interface Rotatable<T> {

    T rotate(Axis axis, int angle);
}

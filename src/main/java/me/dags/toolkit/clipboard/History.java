package me.dags.toolkit.clipboard;

import org.spongepowered.api.block.BlockSnapshot;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class History {

    private final LinkedList<List<BlockSnapshot>> history = new LinkedList<>();
    private final int size;

    public History(int size) {
        this.size = size;
    }

    public List<BlockSnapshot> popRecord() {
        return history.removeLast();
    }

    public List<BlockSnapshot> nextRecord() {
        List<BlockSnapshot> list = new LinkedList<>();
        if (history.size() < size) {
            history.add(list);
        } else {
            history.removeFirst();
            history.add(list);
        }
        return list;
    }
}

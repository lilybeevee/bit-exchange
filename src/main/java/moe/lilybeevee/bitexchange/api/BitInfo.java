package moe.lilybeevee.bitexchange.api;

import net.minecraft.item.Item;

public class BitInfo {
    public Item item;
    public long value;
    public int research;
    public boolean resource;

    public BitInfo(long value, int research, boolean resource) {
        this.item = null;
        this.value = value;
        this.research = research;
        this.resource = resource;
    }

    public BitInfo item(Item item) {
        this.item = item;
        return this;
    }

    public BitInfo copy() {
        return new BitInfo(value, research, resource).item(item);
    }
}

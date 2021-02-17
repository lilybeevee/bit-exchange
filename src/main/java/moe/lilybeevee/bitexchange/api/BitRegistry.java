package moe.lilybeevee.bitexchange.api;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.*;

public class BitRegistry {
    public static final HashMap<Item, BitInfo> MAP = new HashMap<>();
    public static final List<BitInfo> LIST = Lists.newArrayList();
    private static final List<BitRegistryBuilder> builders = Lists.newArrayList();

    public static void registerBuilder(BitRegistryBuilder builder) {
        builders.add(builder);
    }

    public static void build(MinecraftServer server) {
        MAP.clear();
        LIST.clear();
        for (BitRegistryBuilder builder : builders) {
            builder.build(server);
        }
        for (BitInfo info : MAP.values()) {
            if (info.value > 0) {
                LIST.add(info);
            }
        }
    }

    public static void add(String id, BitInfo info) {
        add(id, info, true);;
    }
    public static void add(String id, BitInfo info, boolean override) {
        if (id.startsWith("#")) {
            id = id.substring(1);
            Tag<Item> tag = ServerTagManagerHolder.getTagManager().getItems().getTagOrEmpty(new Identifier(id));
            if (tag == null) {
                throw new JsonSyntaxException("Invalid or unsupported tag '" + id + "'");
            }
            for (Item item : tag.values()) {
                add(item, info.copy(), override);
            }
        } else {
            String finalId = id;
            Item item = Registry.ITEM.getOrEmpty(new Identifier(id)).orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported id '" + finalId + "'"));
            add(item, info.copy(), override);
        }
    }

    public static void add(Item item, BitInfo info) {
        add(item, info, true);
    }
    public static void add(Item item, BitInfo info, boolean override) {
        if (override || !MAP.containsKey(item)) {
            MAP.put(item, info.item(item));
        }
    }

    public static long get(Item item) {
        if (MAP.containsKey(item)) {
            return MAP.get(item).value;
        } else {
            return 0;
        }
    }

    public static BitInfo getInfo(Item item) {
        return MAP.getOrDefault(item, null);
    }

    public static int getResearch(Item item) {
        if (MAP.containsKey(item)) {
            return MAP.get(item).research;
        } else {
            return 0;
        }
    }

    public static boolean isResource(Item item) {
        if (MAP.containsKey(item)) {
            return MAP.get(item).resource;
        } else {
            return false;
        }
    }

    public static List<BitInfo> getAll() {
        return LIST;
    }
}

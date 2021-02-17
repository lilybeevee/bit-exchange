package moe.lilybeevee.bitexchange.registrybuilder;

import com.google.common.collect.Lists;
import com.google.gson.*;
import moe.lilybeevee.bitexchange.BitExchange;
import moe.lilybeevee.bitexchange.api.BitInfo;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import moe.lilybeevee.bitexchange.api.BitRegistryBuilder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class DataRegistryBuilder implements BitRegistryBuilder {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    private static final List<JsonObject> JSON_OBJECTS = Lists.newArrayList();
    private static final HashMap<JsonObject, Identifier> JSON_IDS = new HashMap<>();
    private static final HashMap<JsonObject, Long> processed = new HashMap<>();
    private static final HashSet<String> addingIds = new HashSet<>();

    @Override
    public void build(MinecraftServer server) {
        for (JsonObject json : JSON_OBJECTS) {
            parseJson(json);
        }
        processed.clear();
        addingIds.clear();
    }

    public static void loadResources(ResourceManager manager) {
        JSON_OBJECTS.clear();
        JSON_IDS.clear();
        for (Identifier id : manager.findResources("bit_registry", path -> path.endsWith(".json"))) {
            try (InputStream stream = manager.getResource(id).getInputStream()) {
                Reader reader = new BufferedReader(new InputStreamReader(stream));
                JsonArray array = JsonHelper.deserialize(GSON, reader, JsonArray.class);
                for (JsonElement elem : array) {
                    JsonObject object = elem.getAsJsonObject();
                    if (!object.has("id") && !object.has("ids")) {
                        throw new JsonSyntaxException("Json must have an 'id' or 'ids' field");
                    }
                    JSON_OBJECTS.add(object);
                    JSON_IDS.put(object, id);
                }
            } catch (Exception e) {
                BitExchange.error("Error occured while loading resource " + id.toString(), e);
            }
        }
    }

    private static void parseJson(JsonObject json) {
        try {
            boolean override = JsonHelper.getBoolean(json, "override", true);
            long value = parseJsonBits(json);
            int research = JsonHelper.getInt(json, "research", 1);
            boolean resource = JsonHelper.getBoolean(json, "resource", true);

            if (json.has("id")) {
                String id = JsonHelper.getString(json, "id");
                BitRegistry.add(id, new BitInfo(value, research, resource), override);
            } else if (json.has("ids")) {
                for (JsonElement elem : JsonHelper.getArray(json, "ids")) {
                    BitRegistry.add(elem.getAsString(), new BitInfo(value, research, resource), override);
                }
            }
        } catch (Exception e) {
            BitExchange.error("Error occured while loading resource " + JSON_IDS.get(json).toString(), e);
        }
    }

    private static long parseJsonBits(JsonObject json) {
        if (processed.containsKey(json)) {
            return processed.get(json);
        }
        long value = JsonHelper.getLong(json, "value", 0);
        if (json.has("value_ref")) {
            String addStr = JsonHelper.getString(json, "value_ref");
            String[] ids = addStr.split(",");
            for (String idStr : ids) {
                String id = idStr;
                int count = 1;

                int multIndex = idStr.indexOf("*");
                if (multIndex >= 0) {
                    id = idStr.substring(0, multIndex);
                    count = Integer.parseInt(idStr.substring(idStr.indexOf("*") + 1));
                }

                if (addingIds.contains(id)) {
                    throw new JsonSyntaxException("Value of " + id + " is circular");
                }
                addingIds.add(id);
                try {
                    value += (getBitsFromID(id) * count);
                    addingIds.remove(id);
                } catch (Exception e) {
                    addingIds.remove(id);
                    throw e;
                }
            }
        }
        processed.put(json, value);
        return value;
    }

    private static long getBitsFromID(String id) {
        for (JsonObject json : JSON_OBJECTS) {
            boolean found = false;
            if (json.has("id")) {
                found = JsonHelper.getString(json, "id").equals(id);
            } else if (json.has("ids")) {
                for (JsonElement elem : JsonHelper.getArray(json, "ids")) {
                    if (elem.getAsString().equals(id)) {
                        found = true;
                        break;
                    }
                }
            }
            if (found) {
                return parseJsonBits(json);
            }
        }
        return 0;
    }
}

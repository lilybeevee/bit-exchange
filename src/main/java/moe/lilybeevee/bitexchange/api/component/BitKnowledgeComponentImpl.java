package moe.lilybeevee.bitexchange.api.component;

import com.google.common.collect.Lists;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import moe.lilybeevee.bitexchange.BitComponents;
import moe.lilybeevee.bitexchange.api.BitInfo;
import moe.lilybeevee.bitexchange.api.BitRegistry;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitKnowledgeComponentImpl implements BitKnowledgeComponent, AutoSyncedComponent {
    private Map<Item, Integer> knowledge = new HashMap<>();
    private final Object provider;

    public BitKnowledgeComponentImpl(Object provider) {
        this.provider = provider;
    }

    @Override
    public int getKnowledge(Item item) {
        return knowledge.getOrDefault(item, 0);
    }

    @Override
    public int addKnowledge(Item item, int count) {
        int current = knowledge.getOrDefault(item, 0);
        int added = Math.min(count, BitRegistry.getResearch(item) - current);
        knowledge.put(item, current + added);
        BitComponents.KNOWLEDGE.sync(provider);
        return added;
    }

    @Override
    public boolean getLearned(Item item) {
        BitInfo info = BitRegistry.getInfo(item);
        return info != null && knowledge.getOrDefault(item, 0) >= info.research;
    }


    @Override
    public Map<Item, Integer> getAllKnowledge() {
        return new HashMap<>(knowledge);
    }

    @Override
    public void setAllKnowledge(Map<Item, Integer> knowledge) {
        this.knowledge = knowledge;
        BitComponents.KNOWLEDGE.sync(provider);
    }

    @Override
    public List<Item> getAllLearned() {
        List<Item> result = Lists.newArrayList();
        for (Map.Entry<Item, Integer> entry : knowledge.entrySet()) {
            BitInfo info = BitRegistry.getInfo(entry.getKey());
            if (info != null && entry.getValue() >= info.research) {
                result.add(entry.getKey());
            }
        }
        return result;
    }


    @Override
    public void readFromNbt(CompoundTag tag) {
        ListTag list = (ListTag)tag.get("Knowledge");

        knowledge.clear();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag subtag = list.getCompound(i);

            Item item = Registry.ITEM.get(new Identifier(subtag.getString("id")));
            int value = subtag.getInt("value");

            knowledge.put(item, value);
        }
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        ListTag list = new ListTag();
        tag.put("Knowledge", list);

        int i = 0;
        for (Map.Entry<Item, Integer> entry : knowledge.entrySet()) {
            CompoundTag subtag = new CompoundTag();
            list.addTag(i++, subtag);

            subtag.putString("id", Registry.ITEM.getId(entry.getKey()).toString());
            subtag.putInt("value", entry.getValue());
        }
    }
}

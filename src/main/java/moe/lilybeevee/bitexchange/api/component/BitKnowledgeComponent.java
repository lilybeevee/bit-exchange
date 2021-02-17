package moe.lilybeevee.bitexchange.api.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.item.Item;

import java.util.List;
import java.util.Map;

public interface BitKnowledgeComponent extends Component {
    int getKnowledge(Item item);
    int addKnowledge(Item item, int count);

    Map<Item, Integer> getAllKnowledge();
    void setAllKnowledge(Map<Item, Integer> knowledge);

    boolean getLearned(Item item);
    List<Item> getAllLearned();
}

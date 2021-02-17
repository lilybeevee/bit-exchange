package moe.lilybeevee.bitexchange;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import moe.lilybeevee.bitexchange.api.component.BitKnowledgeComponentImpl;
import moe.lilybeevee.bitexchange.api.component.BitKnowledgeComponent;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.minecraft.util.Identifier;

public class BitComponents implements EntityComponentInitializer {
    public static final ComponentKey<BitKnowledgeComponent> KNOWLEDGE =
            ComponentRegistry.getOrCreate(new Identifier(BitExchange.MOD_ID, "knowledge"), BitKnowledgeComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(KNOWLEDGE, BitKnowledgeComponentImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
    }
}

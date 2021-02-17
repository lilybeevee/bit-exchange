package moe.lilybeevee.bitexchange.block;

import moe.lilybeevee.bitexchange.block.entity.BitResearcherBlockEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BitResearcherBlock extends BlockWithEntity {
    public BitResearcherBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockView world) {
        return new BitResearcherBlockEntity();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof BitResearcherBlockEntity) {
            ((BitResearcherBlockEntity)blockEntity).owner = placer.getUuid();
        }
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        world.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_CAT_AMBIENT, SoundCategory.BLOCKS, 1f, 1f, true);

        if (!world.isClient) {
            BitResearcherBlockEntity blockEntity = (BitResearcherBlockEntity)world.getBlockEntity(pos);

            if (player.getUuid().equals(blockEntity.owner)) {
                //This will call the createScreenHandlerFactory method from BlockWithEntity, which will return our blockEntity casted to
                //a namedScreenHandlerFactory. If your block class does not extend BlockWithEntity, it needs to implement createScreenHandlerFactory.
                NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);

                if (screenHandlerFactory != null) {
                    //With this call the server will request the client to open the appropriate Screenhandler
                    player.openHandledScreen(screenHandlerFactory);
                }
            } else {
                player.sendMessage(new LiteralText("This Bit Researcher is not yours"), true);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof BitResearcherBlockEntity) {
                ItemScatterer.spawn(world, pos, (BitResearcherBlockEntity)blockEntity);
                // update comparators
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }
}

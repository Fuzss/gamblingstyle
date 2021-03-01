package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class CMoveIngredientsMessage extends Message<CMoveIngredientsMessage> {

    private int currentRecipeIndex;
    private boolean clearSlots;
    private boolean quickMove;
    private boolean skipMove;

    @SuppressWarnings("unused")
    public CMoveIngredientsMessage() {

    }

    public CMoveIngredientsMessage(int currentRecipeIndex, boolean clearSlots, boolean quickMove, boolean skipMove) {

        this.currentRecipeIndex = currentRecipeIndex;
        this.clearSlots = clearSlots;
        this.quickMove = quickMove;
        this.skipMove = skipMove;
    }

    @Override
    public void write(ByteBuf buf) {

        buf.writeInt(this.currentRecipeIndex);
        buf.writeBoolean(this.clearSlots);
        buf.writeBoolean(this.quickMove);
        buf.writeBoolean(this.skipMove);
    }

    @Override
    public void read(ByteBuf buf) {

        this.currentRecipeIndex = buf.readInt();
        this.clearSlots = buf.readBoolean();
        this.quickMove = buf.readBoolean();
        this.skipMove = buf.readBoolean();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new MoveIngredientsProcessor();
    }

    private class MoveIngredientsProcessor implements MessageProcessor {

        @Override
        public void accept(EntityPlayer player) {

            if (player.openContainer instanceof ContainerVillager) {

                ((ContainerVillager) player.openContainer).handleClickedButtonItems(CMoveIngredientsMessage.this.currentRecipeIndex, CMoveIngredientsMessage.this.clearSlots, CMoveIngredientsMessage.this.quickMove, CMoveIngredientsMessage.this.skipMove);
            }
        }

    }

}
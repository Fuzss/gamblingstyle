package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;

public class CSelectedRecipeMessage extends Message<CSelectedRecipeMessage> {

    private int currentRecipeIndex;
    private boolean clearSlots;

    @SuppressWarnings("unused")
    public CSelectedRecipeMessage() {

    }

    public CSelectedRecipeMessage(int currentRecipeIndex, boolean clearSlots) {

        this.currentRecipeIndex = currentRecipeIndex;
        this.clearSlots = clearSlots;
    }

    @Override
    public void write(ByteBuf buf) {

        buf.writeInt(this.currentRecipeIndex);
        buf.writeBoolean(this.clearSlots);
    }

    @Override
    public void read(ByteBuf buf) {

        this.currentRecipeIndex = buf.readInt();
        this.clearSlots = buf.readBoolean();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new SelectedRecipeProcessor();
    }

    private class SelectedRecipeProcessor implements MessageProcessor {

        @Override
        public void accept(EntityPlayer player) {

            if (player.openContainer instanceof ContainerVillager) {

                ContainerVillager openContainer = (ContainerVillager) player.openContainer;
                openContainer.setCurrentRecipeIndex(CSelectedRecipeMessage.this.currentRecipeIndex);
                if (CSelectedRecipeMessage.this.clearSlots) {

                    openContainer.clearTradingSlots();
                }
            }
        }

    }
}
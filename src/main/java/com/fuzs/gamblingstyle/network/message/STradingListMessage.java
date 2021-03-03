package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.client.gui.GuiVillager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.village.MerchantRecipeList;

import java.io.IOException;

public class STradingListMessage extends Message<STradingListMessage> {

    private PacketBuffer data;

    @SuppressWarnings("unused")
    public STradingListMessage() {

    }

    public STradingListMessage(PacketBuffer bufIn) {

        this.data = bufIn;
        if (bufIn.writerIndex() > 1048576) {

            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void read(ByteBuf buf) {

        int bytes = buf.readableBytes();
        if (bytes >= 0 && bytes <= 1048576) {

            this.data = new PacketBuffer(buf.readBytes(bytes));
        } else {

            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
    }

    @Override
    public void write(ByteBuf buf) {

        synchronized (this.data) {

            this.data.markReaderIndex();
            buf.writeBytes(this.data);
            this.data.resetReaderIndex();
        }
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new TradingListProcessor();
    }

    private class TradingListProcessor implements MessageProcessor {

        @Override
        public void accept(EntityPlayer player) {

            try {

                Minecraft mc = Minecraft.getMinecraft();
                int windowId = STradingListMessage.this.data.readInt();
                GuiScreen screen = mc.currentScreen;
                if (screen instanceof GuiVillager && windowId == mc.player.openContainer.windowId) {

                    MerchantRecipeList merchantRecipes = MerchantRecipeList.readFromBuf(STradingListMessage.this.data);
                    ((GuiVillager) screen).setMerchantRecipes(merchantRecipes);
                }
            } catch (IOException e) {

                GamblingStyle.LOGGER.error("Couldn't load trade info", e);
            } finally {

                if (STradingListMessage.this.data != null) {

                    STradingListMessage.this.data.release();
                }
            }
        }

    }

}

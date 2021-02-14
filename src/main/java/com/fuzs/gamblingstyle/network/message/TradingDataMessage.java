package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.GamblingStyle;
import com.fuzs.gamblingstyle.inventory.ContainerVillager;
import com.fuzs.gamblingstyle.util.IPrivateAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class TradingDataMessage extends Message<TradingDataMessage> implements IPrivateAccessor {

    private int channel;
    private PacketBuffer data;

    @SuppressWarnings("unused")
    public TradingDataMessage() {

    }

    public TradingDataMessage(int channelIn, PacketBuffer bufIn) {

        this.channel = channelIn;
        this.data = bufIn;
        if (bufIn.writerIndex() > 32767) {

            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void read(ByteBuf buf) {

        this.channel = buf.readUnsignedByte();
        int bytes = buf.readableBytes();
        if (bytes >= 0 && bytes <= 32767) {

            this.data = new PacketBuffer(buf.readBytes(bytes));
        } else {

            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void write(ByteBuf buf) {

        buf.writeByte(this.channel);
        synchronized (this.data) {

            this.data.markReaderIndex();
            buf.writeBytes(this.data);
            this.data.resetReaderIndex();
        }
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new TradingDataProcessor();
    }

    private class TradingDataProcessor implements MessageProcessor {

        @Override
        public void accept(EntityPlayer player) {

            switch (TradingDataMessage.this.channel) {

                case 0:

                    this.selectTrade(player.openContainer);
                    break;

                case 1:

                    this.setLastTrade(player.world);
                    break;

                case 2:

                    this.handleClick(player.openContainer);
                    break;
            }
        }

        private void selectTrade(Container openContainer) {

            try {

                int currentRecipeIndex = TradingDataMessage.this.data.readUnsignedByte();
                boolean clearTradingSlots = TradingDataMessage.this.data.readBoolean();
                if (openContainer instanceof ContainerVillager) {

                    ((ContainerVillager) openContainer).setCurrentRecipeIndex(currentRecipeIndex);
                    if (clearTradingSlots) {

                        ((ContainerVillager) openContainer).clearTradingSlots();
                    }
                }
            } catch (Exception e) {

                GamblingStyle.LOGGER.error("Couldn't select trade", e);
            }
        }

        private void setLastTrade(World world) {

            try {

                int wealth = TradingDataMessage.this.data.readUnsignedByte();
                int entityId = TradingDataMessage.this.data.readInt();
                Entity entity = world.getEntityByID(entityId);
                if (entity instanceof EntityVillager) {

                    TradingDataMessage.this.setWealth((EntityVillager) entity, wealth);
                }
            } catch (Exception e) {

                GamblingStyle.LOGGER.error("Couldn't set wealth", e);
            }
        }

        private void handleClick(Container openContainer) {

            try {

                int recipeIndex = TradingDataMessage.this.data.readUnsignedByte();
                boolean clear = TradingDataMessage.this.data.readBoolean();
                boolean quickMove = TradingDataMessage.this.data.readBoolean();
                boolean skipMove = TradingDataMessage.this.data.readBoolean();
                if (openContainer instanceof ContainerVillager) {

                    ((ContainerVillager) openContainer).handleClickedButtonItems(recipeIndex, clear, quickMove, skipMove);
                }
            } catch (Exception e) {

                GamblingStyle.LOGGER.error("Couldn't populate trading slots", e);
            }
        }

    }
}

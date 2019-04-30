package fuzs.tradinggui.network.messages;

import fuzs.tradinggui.inventory.ContainerVillager;
import fuzs.tradinggui.util.IPrivateAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageTradingData extends MessageBase<MessageTradingData> implements IPrivateAccessor {

    private static final Logger LOGGER = LogManager.getLogger();
    private int channel;
    private PacketBuffer data;

    public MessageTradingData() {
    }

    public MessageTradingData(int channelIn, PacketBuffer bufIn) {
        this.channel = channelIn;
        this.data = bufIn;

        if (bufIn.writerIndex() > 32767)
        {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.channel = buf.readUnsignedByte();
        int i = buf.readableBytes();

        if (i >= 0 && i <= 32767)
        {
            this.data = new PacketBuffer(buf.readBytes(i));
        }
        else
        {
            throw new IllegalArgumentException("Payload may not be larger than 32767 bytes");
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.channel);
        synchronized(this.data) {
            this.data.markReaderIndex();
            buf.writeBytes(this.data);
            this.data.resetReaderIndex();
        }
    }

    @Override
    public void handleClientSide(MessageTradingData message, EntityPlayer player) {

    }

    @Override
    public void handleServerSide(MessageTradingData message, EntityPlayer player) {

        switch (message.getChannelId()) {

            case 0:

                try
                {
                    int k = message.getBufferData().readInt();
                    Container container = player.openContainer;

                    if (container instanceof ContainerVillager)
                    {
                        ((ContainerVillager)container).setCurrentRecipeIndex(k);
                        System.out.println("Package received: Select trade");
                    }
                }
                catch (Exception exception5)
                {
                    LOGGER.error("Couldn't select trade", exception5);
                }
                break;

            case 1:

                try
                {
                    int k = message.getBufferData().readUnsignedByte();
                    int l = message.getBufferData().readInt();
                    Entity entity = player.world.getEntityByID(l);

                    if (entity instanceof EntityVillager) {
                        this.setWealth((EntityVillager) entity, k);
                        System.out.println("Package received: Set wealth");
                    }
                }
                catch (Exception exception5)
                {
                    LOGGER.error("Couldn't set wealth", exception5);
                }
                break;

        }

    }

    private int getChannelId()
    {
        return this.channel;
    }

    private PacketBuffer getBufferData()
    {
        return this.data;
    }
}

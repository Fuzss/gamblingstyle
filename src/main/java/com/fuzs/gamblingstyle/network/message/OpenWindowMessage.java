package com.fuzs.gamblingstyle.network.message;

import com.fuzs.gamblingstyle.client.gui.GuiVillager;
import com.fuzs.gamblingstyle.util.IPrivateAccessor;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class OpenWindowMessage extends Message<OpenWindowMessage> implements IPrivateAccessor {

    private int windowId;
    private ITextComponent windowTitle;
    private int slotCount;
    private int entityId;
    private int wealth;

    @SuppressWarnings("unused")
    public OpenWindowMessage() {

    }

    public OpenWindowMessage(int windowIdIn, ITextComponent windowTitleIn, int slotCountIn, int entityIdIn, int wealth) {

        this.windowId = windowIdIn;
        this.windowTitle = windowTitleIn;
        this.slotCount = slotCountIn;
        this.entityId = entityIdIn;
        this.wealth = wealth;
    }

    @Override
    public void read(ByteBuf buf) {

        this.windowId = buf.readUnsignedByte();
        this.windowTitle = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
        this.slotCount = buf.readUnsignedByte();
        this.wealth = buf.readUnsignedByte();
        this.entityId = buf.readInt();
    }

    @Override
    public void write(ByteBuf buf) {

        buf.writeByte(this.windowId);
        ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(this.windowTitle));
        buf.writeByte(this.slotCount);
        buf.writeByte(this.wealth);
        buf.writeInt(this.entityId);
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new OpenWindowProcessor();
    }

    private class OpenWindowProcessor implements MessageProcessor {

        @Override
        public void accept(EntityPlayer player) {

            World worldIn = player.world;
            Entity entity = worldIn.getEntityByID(OpenWindowMessage.this.entityId);
            if (entity instanceof EntityVillager) {

                OpenWindowMessage.this.setWealth((EntityVillager) entity, OpenWindowMessage.this.wealth);
                GuiVillager guiContainer = new GuiVillager(player.inventory, new NpcMerchant(player, OpenWindowMessage.this.windowTitle), (EntityVillager) entity, worldIn);
                Minecraft.getMinecraft().displayGuiScreen(guiContainer);
                player.openContainer.windowId = OpenWindowMessage.this.windowId;
            }
        }

    }
}
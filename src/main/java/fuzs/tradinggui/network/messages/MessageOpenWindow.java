package fuzs.tradinggui.network.messages;

import fuzs.tradinggui.inventory.GuiVillager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageOpenWindow extends MessageBase<MessageOpenWindow>
{
    private int windowId;
    private ITextComponent windowTitle;
    private int slotCount;
    private int entityId;

    //Required because MineMaarten said so :P
    public MessageOpenWindow() {
    }

    public MessageOpenWindow(int windowIdIn, ITextComponent windowTitleIn, int slotCountIn, int entityIdIn)
    {
        this.windowId = windowIdIn;
        this.windowTitle = windowTitleIn;
        this.slotCount = slotCountIn;
        this.entityId = entityIdIn;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.windowId = buf.readUnsignedByte();
        this.windowTitle = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
        this.slotCount = buf.readUnsignedByte();
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.windowId);
        ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(this.windowTitle));
        buf.writeByte(this.slotCount);
        buf.writeInt(this.entityId);
    }

    @Override
    public void handleClientSide(MessageOpenWindow message, EntityPlayer player) {
        World worldIn = player.world;
        Entity entity = worldIn.getEntityByID(message.entityId);
        Minecraft.getMinecraft().displayGuiScreen(new GuiVillager(player.inventory, new NpcMerchant(player, message.getWindowTitle()), (EntityVillager) entity, worldIn));
        player.openContainer.windowId = message.getWindowId();
    }

    @Override
    public void handleServerSide(MessageOpenWindow message, EntityPlayer player) {
    }

    @SideOnly(Side.CLIENT)
    public int getWindowId()
    {
        return this.windowId;
    }

    @SideOnly(Side.CLIENT)
    public ITextComponent getWindowTitle()
    {
        return this.windowTitle;
    }

    @SideOnly(Side.CLIENT)
    public int getSlotCount()
    {
        return this.slotCount;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasSlots()
    {
        return this.slotCount > 0;
    }

    @SideOnly(Side.CLIENT)
    public int getEntityId()
    {
        return this.entityId;
    }
}
package fuzs.tradinggui.network.messages;

import fuzs.tradinggui.inventory.GuiVillager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.NpcMerchant;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MessageOpenWindow extends MessageBase<MessageOpenWindow>
{
    private int windowId;
    private ITextComponent windowTitle;
    private int slotCount;

    //Required because MineMaarten said so :P
    public MessageOpenWindow() {
    }

    public MessageOpenWindow(int windowIdIn, ITextComponent windowTitleIn, int slotCountIn)
    {
        this.windowId = windowIdIn;
        this.windowTitle = windowTitleIn;
        this.slotCount = slotCountIn;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.windowId = buf.readUnsignedByte();
        this.windowTitle = ITextComponent.Serializer.jsonToComponent(ByteBufUtils.readUTF8String(buf));
        this.slotCount = buf.readUnsignedByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(this.windowId);
        ByteBufUtils.writeUTF8String(buf, ITextComponent.Serializer.componentToJson(this.windowTitle));
        buf.writeByte(this.slotCount);
    }

    @Override
    public void handleClientSide(MessageOpenWindow message, EntityPlayer player) {
        Minecraft.getMinecraft().displayGuiScreen(new GuiVillager(player.inventory, new NpcMerchant(player, message.getWindowTitle()), player.world));
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
}
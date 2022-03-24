package fuzs.gamblingstyle.config;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.annotation.Config;

public class ClientConfig extends AbstractConfig {
    @Config(description = "Color bag inventories on tooltips according to the bag's color.")
    public boolean colorfulTooltips = true;
    @Config(description = "Seeing bag inventory contents requires shift to be held.")
    public boolean contentsRequireShift = true;
    @Config(name = "render_slot_overlay", description = "Render a white overlay over the slot the next item will be taken out when right-clicking the shulker box item.")
    public boolean slotOverlay = true;

    public ClientConfig() {
        super("");
    }
}

package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketOutboundEvent;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.getPlayerController;

@RegisterMod(
    name = "AntiHunger",
    description = "Don't use hunger for travelling",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class AntiHunger extends ToggleMod {
  @FieldMapping(parentClass = CPlayerPacket.class, value = "onGround")
  private final ReflectionField<Boolean> CPacketPlayer_onGround;

  @SubscribeEvent
  public void onPacketSending(PacketOutboundEvent event) {
    if(getLocalPlayer() == null || getLocalPlayer().isElytraFlying()) {
      // this will break elytra flying
      return;
    }

    if (event.getPacket() instanceof CPlayerPacket) {
      CPlayerPacket packet = (CPlayerPacket) event.getPacket();
      if ((getLocalPlayer().fallDistance <= 0.0F)
          && !getPlayerController().getIsHittingBlock()) {
        CPacketPlayer_onGround.set(packet, false);
      } else {
        CPacketPlayer_onGround.set(packet, true);
      }
    }

    if (event.getPacket() instanceof CEntityActionPacket) {
      CEntityActionPacket packet = (CEntityActionPacket) event.getPacket();
      if (packet.getAction() == CEntityActionPacket.Action.START_SPRINTING
          || packet.getAction() == CEntityActionPacket.Action.STOP_SPRINTING) {
        event.setCanceled(true);
      }
    }
  }
}


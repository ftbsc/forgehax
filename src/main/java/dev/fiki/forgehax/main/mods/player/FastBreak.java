package dev.fiki.forgehax.main.mods.player;

import dev.fiki.forgehax.api.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.mapper.FieldMapping;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static dev.fiki.forgehax.main.Common.getPlayerController;

@RegisterMod(
    name = "FastBreak",
    description = "Break blocks faster",
    category = Category.PLAYER
)
@RequiredArgsConstructor
public class FastBreak extends ToggleMod {
  @FieldMapping(parentClass = PlayerController.class, value = "blockHitDelay")
  private final ReflectionField<Integer> PlayerController_blockHitDelay;

  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (getPlayerController() != null) {
      PlayerController_blockHitDelay.set(getPlayerController(), 0);
    }
  }
}

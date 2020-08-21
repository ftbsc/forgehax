package dev.fiki.forgehax.main.util.entity;

import dev.fiki.forgehax.main.services.HotbarSelectionService;
import dev.fiki.forgehax.main.services.HotbarSelectionService.ResetFunction;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory.InvItem.SlotWrapper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static dev.fiki.forgehax.main.Common.getLocalPlayer;
import static dev.fiki.forgehax.main.Common.sendNetworkPacket;

public class LocalPlayerInventory {

  public static PlayerInventory getInventory() {
    return getLocalPlayer().inventory;
  }

  public static Container getContainer() {
    return getLocalPlayer().container;
  }

  public static Container getOpenContainer() {
    return getLocalPlayer().openContainer;
  }

  public static int getHotbarSize() {
    return PlayerInventory.getHotbarSize();
  }

  public static List<InvItem> getMainInventory() {
    AtomicInteger next = new AtomicInteger(0);
    return getInventory()
        .mainInventory
        .stream()
        .map(item -> new InvItem.Base(item, next.getAndIncrement()))
        .collect(Collectors.toList());
  }

  public static List<InvItem> getSlotInventory() {
    return getContainer()
        .inventorySlots
        .stream()
        .map(SlotWrapper::new)
        .collect(Collectors.toList());
  }

  public static List<InvItem> getMainInventory(int start, int end) {
    return getMainInventory().subList(start, end);
  }

  public static List<InvItem> getSlotInventory(int start, int end) {
    return getSlotInventory().subList(start, end);
  }

  public static List<InvItem> getStorageInventory() {
    return getMainInventory(9, 27);
  }

  public static List<InvItem> getSlotStorageInventory() {
    return getSlotInventory(9, 36);
  }

  public static List<InvItem> getHotbarInventory() {
    return getMainInventory(0, getHotbarSize());
  }

  public static InvItem getMouseHeld() {
    return newInvItem(getInventory().getItemStack(), -999);
  }

  public static InvItem getSelected() {
    return getMainInventory().get(getInventory().currentItem);
  }

  public static ResetFunction setSelected(int index, boolean reset, Predicate<Long> condition) {
    return HotbarSelectionService.getInstance().setSelected(index, reset, condition);
  }

  public static ResetFunction setSelected(InvItem inv, boolean reset, Predicate<Long> condition) {
    return setSelected(inv.getIndex(), reset, condition);
  }

  public static ResetFunction setSelected(int index, Predicate<Long> condition) {
    Objects.requireNonNull(condition);
    return setSelected(index, true, condition);
  }

  public static ResetFunction setSelected(InvItem inv, Predicate<Long> condition) {
    return setSelected(inv.getIndex(), condition);
  }

  public static ResetFunction setSelected(int index) {
    return setSelected(index, ticks -> true);
  }

  public static ResetFunction setSelected(InvItem invItem) {
    return setSelected(invItem.getIndex());
  }

  public static ResetFunction forceSelected(int index) {
    return setSelected(index, false, null);
  }

  public static ResetFunction forceSelected(InvItem inv) {
    return forceSelected(inv.getIndex());
  }

  public static void resetSelected() {
    HotbarSelectionService.getInstance().resetSelected();
  }

  public static InvItem getOffhand() {
    return newInvItem(getLocalPlayer().getHeldItemOffhand(), 36);
  }

  public static int getHotbarDistance(InvItem item) {
    int max = LocalPlayerInventory.getHotbarSize() - 1;
    return item.getIndex() > max ? 0 : max - Math.abs(getSelected().getIndex() - item.getIndex());
  }

  public static void sendWindowClick(int slotIdIn, int usedButtonIn, ClickType modeIn, ItemStack clickedItemIn) {
    sendNetworkPacket(new CClickWindowPacket(
        0,
        slotIdIn,
        usedButtonIn,
        modeIn,
        clickedItemIn,
        getOpenContainer().getNextTransactionID(getInventory())));
  }

  public static ItemStack sendWindowClick(InvItem item, int usedButtonIn, ClickType modeIn) {
    if (item.getIndex() == -1) {
      throw new IllegalArgumentException();
    }
    ItemStack ret;
    sendWindowClick(
        item.getSlotNumber(),
        usedButtonIn,
        modeIn,
        ret =
            getOpenContainer()
                .slotClick(item.getSlotNumber(), usedButtonIn, modeIn, getLocalPlayer()));
    return ret;
  }

  public static InvItem newInvItem(ItemStack itemStack, int index) {
    return new InvItem.Base(itemStack, index);
  }

  public static InvItem newInvItem(Slot slot) {
    return new SlotWrapper(slot);
  }

  public abstract static class InvItem implements Comparable<InvItem> {

    public static final InvItem EMPTY =
        new InvItem() {
          @Override
          public ItemStack getItemStack() {
            return ItemStack.EMPTY;
          }

          @Override
          public Item getItem() {
            return Items.AIR;
          }

          @Override
          public int getIndex() {
            return -1;
          }
        };

    public abstract ItemStack getItemStack();

    public Item getItem() {
      return getItemStack().getItem();
    }

    public abstract int getIndex();

    public int getSlotNumber() {
      switch (getIndex()) {
        case 36:
          return 45;
        default:
          // TODO: make this work for all container types
          // 9 = the crafting result, 4x crafting boxes, and 4
          // 36 = main inventory size
          int row = getIndex() / 9;
          int idx = getIndex() % 9;
          return 9 + 36 - ((row * 9) + (9 - idx));
      }
    }

    public boolean isNull() {
      return getItemStack().isEmpty();
    }

    public boolean nonNull() {
      return !isNull();
    }

    public boolean isEmpty() {
      return getItemStack().isEmpty();
    }

    public boolean nonEmpty() {
      return !isEmpty();
    }

    public boolean isDamageable() {
      return getItemStack().isDamageable();
    }

    public boolean isItemDamageable() {
      return getItem().isDamageable();
    }

    public boolean isStackable() {
      return getItemStack().isStackable();
    }

    public int getDamage() {
      return isDamageable() ? getItemStack().getDamage() : 0;
    }

    public int getDurability() {
      return isDamageable() ? (getItemStack().getMaxDamage() - getItemStack().getDamage()) : 0;
    }

    public int getStackCount() {
      return getItemStack().getCount();
    }

    public int getMaxStackCount() {
      return getItemStack().getMaxStackSize();
    }

    public boolean isStackMaxed() {
      return getStackCount() >= getMaxStackCount();
    }

    public boolean isItemsEqual(InvItem other) {
      return getItemStack().isItemEqualIgnoreDurability(other.getItemStack());
    }

    @Override
    public boolean equals(Object obj) {
      return this == obj
          || (obj instanceof InvItem
          && getIndex() == ((InvItem) obj).getIndex()
          && getItemStack().equals(((InvItem) obj).getItemStack()));
    }

    @Override
    public int hashCode() {
      return Objects.hash(getItemStack(), getIndex());
    }

    @Override
    public int compareTo(InvItem o) {
      return Integer.compare(getIndex(), o.getIndex());
    }

    protected static class Base extends InvItem {

      private final ItemStack itemStack;
      private final int index;

      protected Base(ItemStack itemStack, int index) {
        this.itemStack = itemStack;
        this.index = index;
      }

      @Override
      public ItemStack getItemStack() {
        return itemStack;
      }

      @Override
      public int getIndex() {
        return index;
      }
    }

    protected static class SlotWrapper extends InvItem {

      private final Slot slot;

      protected SlotWrapper(Slot slot) {
        this.slot = slot;
      }

      @Override
      public ItemStack getItemStack() {
        return slot.getStack();
      }

      @Override
      public int getIndex() {
        int row = (getSlotNumber() / 9) - 1;
        int idx = getSlotNumber() % 9;
        return 36 - ((row * 9) + (9 - idx)); // inverse of what is done for ::getIndex()
      }

      @Override
      public int getSlotNumber() {
        return slot.slotNumber;
      }
    }
  }
}

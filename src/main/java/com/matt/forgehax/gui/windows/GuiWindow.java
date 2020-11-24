package com.matt.forgehax.gui.windows;

import static com.matt.forgehax.Helper.getModManager;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.util.color.Colors.GRAY;
import static com.matt.forgehax.util.color.Colors.WHITE;

import com.matt.forgehax.gui.ClickGui;
import com.matt.forgehax.gui.windows.GuiWindowMod;
import com.matt.forgehax.gui.windows.GuiWindowSetting;
import com.matt.forgehax.mods.ActiveModList;
import com.matt.forgehax.mods.services.GuiService;
import com.matt.forgehax.util.color.Color;
import com.matt.forgehax.util.draw.SurfaceHelper;
import java.io.IOException;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Created by Babbaj on 9/5/2017.
 */
public abstract class GuiWindow {

  GuiService gui = getModManager().get(GuiService.class).get();

  public boolean isHidden; // whether or not not to show everything below the header

  public final String title;

  public int posX, headerY, windowY;
  public int bottomX, bottomY;

  // coords of where the window is being dragged from
  private int dragX, dragY;

  private boolean dragging;

  public int width, height; // width of the window

  enum MouseButtons {
    LEFT(0),
    RIGHT(1),
    MIDDLE(2);

    protected final int id;
    MouseButtons(final int mouseID) {
      this.id = mouseID;
    }
  }

  GuiWindow(String titleIn) {
    this.title = titleIn;
    width = SurfaceHelper.getTextWidth(title) + 15;
  }

  public void setPosition(int x, int y) {
    this.posX = x;
    this.headerY = y;
  }

  private String getTitle() {
    return title;
  }

  boolean isMouseInHeader(int mouseX, int mouseY) {
    return (mouseX > posX && mouseX < posX + width && mouseY > headerY && mouseY < headerY + 20);
  }
  
  /**
   * 0 == Left Click 1 == Right Click 2 == Middle Click
   */
  public void mouseClicked(int mouseX, int mouseY, int state) {
    if (state == 0) {
      if (isMouseInHeader(mouseX, mouseY)) {
        dragging = true;

        dragX = mouseX - posX;
        dragY = mouseY - headerY;
      }
    } else if (state == 1 && isMouseInHeader(mouseX, mouseY)) {
      isHidden = !isHidden;
    }
  }

  public void mouseReleased(int x, int y, int state) {
    dragging = false;
  }

  public void handleMouseInput(int x, int y) throws IOException {
    // scrolling
  }

  public void keyTyped(char typedChar, int keyCode) throws IOException {
    // text input
  }

  public void drawWindow(int mouseX, int mouseY) {
    ClickGui.scaledRes = new ScaledResolution(MC);
    int color = gui.color.get().toBuffer();
    if (dragging) {
      posX = mouseX - dragX;
      headerY = mouseY - dragY;
    }
    drawHeader();
    windowY = headerY + 21;
    if (!isHidden) {
      int actualHeight = (int) Math.min(height, ClickGui.scaledRes.getScaledHeight() * 
                          getModManager().get(GuiService.class).get().max_height.get());
      
      SurfaceHelper.drawOutlinedRectShaded(
        posX, windowY, width, actualHeight, color, 80, 3);
    }
  }
  
  public void drawTooltip(int mouseX, int mouseY) {}

  public void drawHeader() {
    // draw the title of the window
    Color c = gui.color.get();
    int color = Color.of(c.getRed() + 22, c.getGreen() + 22, c.getBlue() + 22, c.getAlpha()).toBuffer();
    SurfaceHelper.drawOutlinedRectShaded(
      posX, headerY, width, 20, color, 50, 5);
    SurfaceHelper.drawTextShadowCentered(
        getTitle(), posX + width / 2f, headerY + 10, WHITE.toBuffer());
  }
}

package com.kahzerx.kahzerxmod.extensions.shopExtension.gui.components.back;

import com.kahzerx.kahzerxmod.extensions.shopExtension.gui.GuiPlayer;
import com.kahzerx.kahzerxmod.extensions.shopExtension.gui.Renderer;
import com.kahzerx.kahzerxmod.extensions.shopExtension.gui.components.Component;

public class SolidBack extends Component {
    private byte fillColor;
    private int x;
    private int y;
    private int width;
    private int height;
    public SolidBack(byte fillColor) {
        this.fillColor = fillColor;
    }

    public void setDimensions(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.setBounds(x, y, x + width, y + height);
    }

    @Override
    public void render(GuiPlayer guiPlayer) {
        Renderer.fill(guiPlayer, x, y, width, height, fillColor);
    }
}

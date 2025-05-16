package com.createcivilization.capitol.gui.interactables;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.gui.base.Asset;
import com.createcivilization.capitol.gui.base.Interactable;
import com.createcivilization.capitol.gui.base.SmartScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static org.lwjgl.glfw.GLFW.*;

public abstract class TextInputInteractable implements Interactable.BlitInteractable {

	private final Asset.Blit idleBlit;
	private final Component placeHolderText;
	private StringBuilder value = new StringBuilder();
	private int x, y;
	private final int color;
	boolean dropShadow;
	SmartScreen smartScreen;

	public TextInputInteractable(Asset.Blit idleBlit, @Nullable Component placeHolderText, int x, int y, @Nullable Integer color, boolean dropShadow) {
		this.idleBlit = idleBlit;
		this.placeHolderText = placeHolderText == null ? Component.empty() : placeHolderText;
		this.color = color == null ? Color.WHITE.getRGB() : color;
		this.dropShadow = dropShadow;
		this.x = x;
		this.y = y;
	}

	private boolean isHidden = false;

	@Override
	public void render(GuiGraphics guiGraphics) {
		BlitInteractable.super.render(guiGraphics);

		if (getValue().isEmpty()) guiGraphics.drawString(ClientConstants.INSTANCE.font, placeHolderText.getString(), this.x + 3, this.y + ClientConstants.INSTANCE.font.lineHeight / 2 - 2, Color.GRAY.getRGB(), dropShadow);
		else guiGraphics.drawString(ClientConstants.INSTANCE.font, getValue() + (isActive() && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : ""), this.x + 3, this.y + ClientConstants.INSTANCE.font.lineHeight / 2 - 2, color, dropShadow);
	}


	@Override
	public void init(SmartScreen smartScreen) {
		this.smartScreen = smartScreen;
	}

	@Override
	public void input(int keyCode, int scanCode, int modifiers) {
		if (!isActive()) return;

		if (keyCode == GLFW_KEY_BACKSPACE && !value.isEmpty()) {
			value.deleteCharAt(value.length() - 1);
			return;
		}

		String keyName = glfwGetKeyName(keyCode, scanCode);
		if (keyName != null && keyName.length() == 1) {
			char character = keyName.charAt(0);
			if ((modifiers & GLFW_MOD_SHIFT) != 0) {
				character = Character.toUpperCase(character);
			}
			value.append(character);
		}
	}

	private boolean isActive() {
		return this.smartScreen.lastClick == this;
	}

	public String getValue() {
		return value.toString();
	}

	public void setValue(String val) {
		this.value = new StringBuilder(val);
	}

	@Override
	public boolean isHidden() {
		return isHidden;
	}

	@Override
	public void show() {
		isHidden = false;
	}

	@Override
	public void hide() {
		isHidden = true;
	}

	@Override
	public Asset.Blit getBlit() {
		return idleBlit;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}
}
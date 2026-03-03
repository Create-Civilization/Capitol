package com.createcivilization.capitol.old.old.gui.interactables;

import com.createcivilization.capitol.old.old.constants.ClientConstants;
import com.createcivilization.capitol.old.old.gui.base.Asset;
import com.createcivilization.capitol.old.old.gui.base.Interactable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public abstract class ButtonInteractable implements Interactable.BlitInteractable {

	private final Asset.Blit idleBlit;
	private final Asset.Blit activeBlit;
	private Asset.Blit currentBlit;
	private final Component text;
	private int x, y;
	private final int color;
	boolean dropShadow;

	public ButtonInteractable(Asset.Blit idleBlit, @Nullable Asset.Blit activeBlit, @Nullable Component text, int x, int y, @Nullable Integer color, boolean dropShadow) {
		this.idleBlit = idleBlit;
		this.activeBlit = activeBlit;
		this.currentBlit = activeBlit == null ? idleBlit : activeBlit;
		this.text = text == null ? Component.empty() : text;
		this.color = color == null ? Color.WHITE.getRGB() : color;
		this.dropShadow = dropShadow;
		this.x = x;
		this.y = y;
	}

	private boolean isHidden;

	@Override
	public void render(GuiGraphics guiGraphics) {
		BlitInteractable.super.render(guiGraphics);
		guiGraphics.drawString(ClientConstants.INSTANCE.font, this.text, this.x + getBlit().getBlitWidth() / 2 - ClientConstants.INSTANCE.font.width(this.text.getString()) / 2, this.y + ClientConstants.INSTANCE.font.lineHeight / 2 - 2, this.color, this.dropShadow);
	}

	@Override
	public Interactable clickStart(int x, int y) {
		if (isHidden()) return null;
		if (activeBlit != null) this.currentBlit = activeBlit;
		return this;
	}

	@Override
	public void clickRelease(int x, int y) {
		BlitInteractable.super.clickRelease(x,y);
		if (activeBlit != null) this.currentBlit = idleBlit;
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
		return currentBlit;
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

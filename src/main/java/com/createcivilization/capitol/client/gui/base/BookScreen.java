package com.createcivilization.capitol.client.gui.base;

import com.createcivilization.capitol.Capitol;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class BookScreen extends SmartScreen {
	protected static final Asset ASSET = new Asset(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "textures/gui/book_gui.png"), 421, 212);
	protected static final Asset.Blit BACKGROUND = ASSET.blit(0, 0, 295, 179);

	protected int leftPos, topPos, rightPos;

	@Override
	protected void init() {
		super.init();
		int halfWidth = BACKGROUND.getBlitWidth() / 2;
		this.leftPos = (this.width / 2) - halfWidth;
		this.rightPos = leftPos + halfWidth;
		this.topPos = (this.height - BACKGROUND.getBlitHeight()) / 2;
	}

	@Override
	protected Asset.Blit getBackgroundBlit() {
		return BACKGROUND;
	}

	protected BookScreen(Component title) {
		super(title);
	}
}

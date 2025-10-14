package com.createcivilization.capitol.old.gui.base;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Asset {
	private final ResourceLocation texture;
	private final int textureWidth, textureHeight;

	public Asset(ResourceLocation texture, int textureWidth, int textureHeight) {
		this.texture = texture;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	}

	public Blit blit(int startX, int startY, int width, int height) {
		return new Blit(startX, startY, width, height);
	}

	public class Blit {
		private int startX, startY, blitWidth, blitHeight;

		public Blit(int startX, int startY, int blitWidth, int blitHeight) {
			this.startX = startX;
			this.startY = startY;
			this.blitWidth = blitWidth;
			this.blitHeight = blitHeight;
		}

		public void renderBlit(GuiGraphics guiGraphics, int x, int y) {
			guiGraphics.blit(texture, x, y, startX, startY, blitWidth, blitHeight, textureWidth, textureHeight);
		}

		public void setPosition(@Nullable Integer x, @Nullable Integer y) {
			startX = x == null ? startX : x;
			startY = y == null ? startY : y;
		}

		public void setSize(@Nullable Integer width, @Nullable Integer height) {
			blitWidth = width == null ? blitWidth : width;
			blitHeight = height == null ? blitHeight : height;
		}

		public int getBlitWidth() {
			return blitWidth;
		}
		public int getBlitHeight() {
			return blitHeight;
		}
	}
}

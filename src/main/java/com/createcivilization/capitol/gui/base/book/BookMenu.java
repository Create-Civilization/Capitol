package com.createcivilization.capitol.gui.base.book;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.gui.base.Asset;
import com.createcivilization.capitol.gui.base.Interactable;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.interactables.ButtonInteractable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.IntStream;

public abstract class BookMenu extends SmartScreen {

	private static final Asset ASSET = new Asset(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "textures/gui/book_gui.png"), 421, 212);
	private static final Asset.Blit BACKGROUND = ASSET.blit(0, 0, 295, 179);
	Tabs tabs = new Tabs();
	int leftPos, rightPos, topPos;


	protected BookMenu(Component title) {
		super(title);
		addInteractable(tabs);
	}

	@Override
	protected Asset.Blit getBackgroundBlit() {
		return BACKGROUND;
	}

	@Override
	protected void init() {
		super.init();
		int halfWidth = BACKGROUND.getBlitWidth()/2;
		this.leftPos = (this.width / 2) - halfWidth;
		this.rightPos = leftPos + halfWidth;
		this.topPos = (this.height - BACKGROUND.getBlitHeight()) / 2;
		this.tabs.setX(this.rightPos + 20);
		this.tabs.setY(this.topPos - 16);
	}

	private class Tabs extends Interactable.InteractableBundle {

		int x,y;

		private Tabs() {
			this.setInteractableList(IntStream.range(0, 5).boxed().map(integer -> (Interactable) new Tab(296 + (integer * 23), (integer * 23), 0)).toList());
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
			this.getInteractableList().forEach(tab -> tab.setX(tab.getX() + getX()));
		}

		@Override
		public void setY(int y) {
			this.y = y;
			this.getInteractableList().forEach(tab -> tab.setY(tab.getY() + getY()));
		}

		private class Tab extends ButtonInteractable {

			public Tab(int off, int x, int y) {
				super(ASSET.blit(off, 0, 15, 23), x, y);
			}

			@Override
			public void clickStart(int x, int y) {
				getBlit().setSize(null, 28);
				setY(getY() - 5);
			}

			@Override
			public void clickRelease(int x, int y) {
				getBlit().setSize(null, 23);
				setY(getY() + 5);
			}
		}
	}
}

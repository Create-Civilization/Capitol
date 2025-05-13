package com.createcivilization.capitol.gui.screen;

import com.createcivilization.capitol.Capitol;
import com.createcivilization.capitol.gui.base.Asset;
import com.createcivilization.capitol.gui.base.Interactable;
import com.createcivilization.capitol.gui.base.Page;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.interactables.ButtonInteractable;
import com.createcivilization.capitol.gui.pages.CreateTeamPage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class BookMenu extends SmartScreen {

	private static final Asset ASSET = new Asset(ResourceLocation.fromNamespaceAndPath(Capitol.MOD_ID, "textures/gui/book_gui.png"), 421, 212);
	private static final Asset.Blit BACKGROUND = ASSET.blit(0, 0, 295, 179);
	public int currentPage;
	Tabs tabs = new Tabs();
	int startingTab;
	Tabs.Tab currentTab;
	public int leftPos, rightPos, topPos;


	public BookMenu(int startingTab, int startingPage) {
		super(Component.literal("Book"));
		addInteractable(tabs);
		this.startingTab = startingTab;
		this.currentPage = startingPage;
	}

	@Override
	protected Asset.Blit getBackgroundBlit() {
		return BACKGROUND;
	}

	@Override
	protected void init() {
		int halfWidth = BACKGROUND.getBlitWidth()/2;
		this.leftPos = (this.width / 2) - halfWidth;
		this.rightPos = leftPos + halfWidth;
		this.topPos = (this.height - BACKGROUND.getBlitHeight()) / 2;
		super.init();
		this.tabs.setX(this.rightPos + 20);
		this.tabs.setY(this.topPos - 16);
		((Tabs.Tab) this.tabs.getInteractableList().get(this.startingTab)).select();
	}

	private class Tabs extends Interactable.InteractableBundle {

		int x,y;

		private Tabs() {
			List<List<Page>> pages = List.of(
				List.of(

				),
				List.of(

				),
				List.of(

				),
				List.of(
					new CreateTeamPage()
				),
				List.of(

				)
			);
			this.setInteractableList(IntStream.range(0, 5).boxed().map(integer -> (Interactable) new Tab(296 + (integer * 23), (integer * 23), 0, pages.get(integer))).toList());
		}

		@Override
		public void init(SmartScreen smartScreen) {
			this.getInteractableList().forEach(interactable -> interactable.init(smartScreen));
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

			BookMenu bookMenu;
			List<Page> pages;

			boolean active = false;

			public Tab(int off, int x, int y, List<Page> pages) {
				super(ASSET.blit(off, 0, 15, 23), x, y);
				this.pages = pages;
			}

			@Override
			public void init(SmartScreen smartScreen) {
				this.bookMenu = (BookMenu) smartScreen;
				int i = 0;
				Capitol.LOGGER.info("left {} right {}", bookMenu.leftPos, bookMenu.rightPos);
				for (Page page : this.pages) {
					page.setX(i++ % 2 == 0 ? bookMenu.leftPos : bookMenu.rightPos);
					page.setY(bookMenu.topPos);
				}
			}

			@Override
			public void clickStart(int x, int y) {
				if (bookMenu.currentTab == this) return;
				if (bookMenu.currentTab != null) bookMenu.currentTab.deactivate();
				bookMenu.currentPage = 0;
				getBlit().setSize(null, 28);
				setY(getY() - 2);
				bookMenu.currentTab = this;
				active = true;
			}

			public void select() {
				getBlit().setSize(null, 28);
				setY(getY() - 5);
				bookMenu.currentTab = this;
			}

			@Override
			public void hovered(int x, int y) {
				if (bookMenu.currentTab == this) return;
				getBlit().setSize(null, 26);
				setY(getY() - 3);
			}

			@Override
			public void hoverLeave(int x, int y) {
				if (bookMenu.currentTab == this) return;
				getBlit().setSize(null, 23);
				setY(getY() + 3);
			}

			public void deactivate() {
				getBlit().setSize(null, 23);
				setY(getY() + 5);
				active = false;
			}

			@Override
			public void render(GuiGraphics guiGraphics) {
				super.render(guiGraphics);
				if (!active) return;
				renderIfExists(this.bookMenu.currentPage, guiGraphics);
				renderIfExists(this.bookMenu.currentPage+1, guiGraphics);
			}

			public void renderIfExists(int page, GuiGraphics guiGraphics) {
				if (page < 0 || page >= this.pages.size()) return;
				this.pages.get(page).render(guiGraphics);
			}
		}
	}
}

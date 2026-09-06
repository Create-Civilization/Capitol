package com.createcivilization.capitol.client.gui.screen;

import com.createcivilization.capitol.client.gui.base.*;
import com.createcivilization.capitol.client.gui.interactables.ButtonInteractable;
import com.createcivilization.capitol.client.gui.interactables.TextInputInteractable;
import com.createcivilization.capitol.client.gui.pages.CapitolBlockPage;
import com.createcivilization.capitol.client.gui.pages.DisplayTeamPage;
import com.createcivilization.capitol.client.gui.pages.InvitePlayerPage;
import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.data.Team;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class CapitolBookMenu extends BookScreen {

	private static int lastTab = 0;
	private static int lastPage = 0;

	public int currentPage;
	Tabs tabs;
	int startingTab;
	Tabs.Tab currentTab;
	List<PageHandler> pageHandlers;

	public CapitolBookMenu(Team team, BlockPos capitolPos, boolean isCapital, CapitolTier tier, boolean canUpgrade) {
		super(Component.translatable("screen.capitol.capitol_block.title"));
		this.pageHandlers = List.of(
			new AttackHandler(),
			new DefenseHandler(),
			new SupportHandler(),
			new InfoHandler(team, capitolPos),
			new SettingsHandler(capitolPos, isCapital, tier, canUpgrade)
		);
		this.tabs = new Tabs(isCapital);
		addInteractable(tabs);
		pageHandlers.forEach(pageHandler -> {
			pageHandler.hide();
			addInteractable(pageHandler);
		});
		// attack/defense tabs are hidden outside the Capital, so don't try to start on one
		this.startingTab = !isCapital && lastTab < 2 ? 2 : lastTab;
		this.currentPage = lastPage;
	}

	@Override
	protected void init() {
		int halfWidth = BACKGROUND.getBlitWidth() / 2;
		this.leftPos = (this.width / 2) - halfWidth;
		this.rightPos = leftPos + halfWidth;
		this.topPos = (this.height - BACKGROUND.getBlitHeight()) / 2;
		this.tabs.setX(this.rightPos + 20);
		this.tabs.setY(this.topPos - 16);
		super.init();
		((Tabs.Tab) this.tabs.getInteractableList().get(this.startingTab)).select();
	}

	private static class Tabs extends Interactable.InteractableBundle {

		int x, y;

		private Tabs(boolean isCapital) {
			this.setInteractableList(
				IntStream.range(0, 5).boxed().map(
					integer -> (Interactable) new Tab(296 + (integer * 23), (integer * 23), 0, integer)
				).toList()
			);
			// the red (attack) and blue (defense) bookmarks are Capital-only
			if (!isCapital) {
				getInteractableList().get(0).hide();
				getInteractableList().get(1).hide();
			}
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

		private static class Tab extends ButtonInteractable {

			CapitolBookMenu bookMenu;

			int index;

			public Tab(int off, int x, int y, int index) {
				super(ASSET.blit(off, 0, 15, 23), null, null, x, y, null, true);
				this.index = index;
			}

			@Override
			public void init(SmartScreen smartScreen) {
				this.bookMenu = (CapitolBookMenu) smartScreen;
			}

			@Override
			public Interactable clickStart(int x, int y) {
				if (bookMenu.currentTab == this) return null;
				if (bookMenu.currentTab != null) bookMenu.currentTab.deactivate();
				bookMenu.currentPage = 0;
				getBlit().setSize(null, 28);
				setY(getY() - 2);
				bookMenu.currentTab = this;
				bookMenu.pageHandlers.get(this.index).show();
				lastTab = this.index;
				return this;
			}

			public void select() {
				getBlit().setSize(null, 28);
				setY(getY() - 5);
				bookMenu.currentTab = this;
				bookMenu.pageHandlers.get(this.index).show();
				lastTab = this.index;
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
				bookMenu.pageHandlers.get(this.index).hide();
			}

			@Override
			public void render(GuiGraphics guiGraphics) {
				super.render(guiGraphics);
			}
		}
	}

	private static class AttackHandler extends PageHandler {
		public AttackHandler() {
			super(List.of());
		}
	}

	private static class DefenseHandler extends PageHandler {
		public DefenseHandler() {
			super(List.of());
		}
	}

	private static class SupportHandler extends PageHandler {
		public SupportHandler() {
			super(List.of(
				new InvitePlayerPage()
			));
		}
	}

	private static class InfoHandler extends PageHandler {
		public InfoHandler(Team team, BlockPos capitolPos) {
			super(List.of(new DisplayTeamPage(team, capitolPos)));
		}
	}

	private static class SettingsHandler extends PageHandler {
		public SettingsHandler(BlockPos capitolPos, boolean isCapital, CapitolTier tier, boolean canUpgrade) {
			super(List.of(new CapitolBlockPage(capitolPos, isCapital, tier, canUpgrade)));
		}
	}

	private abstract static class PageHandler extends Interactable.InteractableBundle {

		int x, y;
		CapitolBookMenu bookMenu;
		PageFlipper backFlipper = new PageFlipper.Back(24, 158);
		PageFlipper nextFlipper = new PageFlipper.Next(251, 158);

		public PageHandler(List<Interactable> pages) {
			setInteractableList(pages);
		}

		@Override
		public void render(GuiGraphics guiGraphics) {
			if (isHidden()) return;

			List<Interactable> list = getInteractableList();
			int plusOne = this.bookMenu.currentPage + 1;

			if (this.bookMenu.currentPage >= 0 && this.bookMenu.currentPage < list.size()) {
				list.get(this.bookMenu.currentPage).render(guiGraphics);

				if (plusOne >= list.size())
					nextFlipper.hide();
			}

			if (plusOne >= 0 && plusOne < list.size()) {
				list.get(plusOne).render(guiGraphics);

				if (this.bookMenu.currentPage - 1 < 0)
					backFlipper.hide();
			}

			backFlipper.render(guiGraphics);
			nextFlipper.render(guiGraphics);
		}

		@Override
		public void init(SmartScreen smartScreen) {
			this.bookMenu = (CapitolBookMenu) smartScreen;
			int i = 0;
			for (Interactable interactable : getInteractableList()) {
				interactable.init(bookMenu);
				interactable.setX(i++ % 2 == 0 ? bookMenu.leftPos : bookMenu.rightPos);
				interactable.setY(bookMenu.topPos);
			}

			backFlipper.init(smartScreen);
			nextFlipper.init(smartScreen);
			backFlipper.setX(bookMenu.leftPos + 24);
			backFlipper.setY(bookMenu.topPos + 158);
			nextFlipper.setX(bookMenu.leftPos + 251);
			nextFlipper.setY(bookMenu.topPos + 158);
		}

		@Override
		public Interactable clickStart(int x, int y) {
			return super.clickStart(x, y);
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

		public static class PageFlipper extends ButtonInteractable {

			boolean held = false;
			int add;
			CapitolBookMenu bookMenu;

			public PageFlipper(Asset.Blit idleBlit, Asset.@Nullable Blit activeBlit, int x, int y, int add) {
				super(idleBlit, activeBlit, null, x, y, null, false);
				this.add = add;
			}

			@Override
			public void init(SmartScreen smartScreen) {
				this.bookMenu = (CapitolBookMenu) smartScreen;
			}

			@Override
			public Interactable clickStart(int x, int y) {
				super.clickStart(x, y);

				if (!held) {
					this.bookMenu.currentPage += this.add;
					lastPage = this.bookMenu.currentPage;
					this.held = true;
				}

				return this;
			}

			@Override
			public void clickRelease(int x, int y) {
				super.clickRelease(x, y);

				if (held) this.held = false;
			}

			public static class Next extends PageFlipper {
				public Next(int x, int y) {
					super(ASSET.blit(0, 188, 21, 8), ASSET.blit(0, 180, 21, 8), x, y, 2);
				}
			}

			public static class Back extends PageFlipper {
				public Back(int x, int y) {
					super(ASSET.blit(21, 188, 21, 8), ASSET.blit(21, 180, 21, 8), x, y, -2);
				}
			}
		}
	}

	public static class Button extends ButtonInteractable {
		Runnable onActivated;

		public Button(int x, int y, Component text, Runnable runnable) {
			super(ASSET.blit(42, 180, 106, 13), ASSET.blit(148, 180, 106, 13), text, x, y, null, true);
			this.onActivated = runnable;
		}

		@Override
		public Interactable clickStart(int x, int y) {
			if (!(super.clickStart(x, y) instanceof Interactable interactable)) return null;
			this.onActivated.run();
			return interactable;
		}
	}

	public static class TextInput extends TextInputInteractable {
		public TextInput(@Nullable Component placeHolderText, int x, int y, @Nullable List<String> autoCorrect) {
			super(ASSET.blit(148, 193, 106, 13), placeHolderText, x, y, autoCorrect, null, true);
		}
	}

	public static void addTableEntry(Page page, Component title, Component value, int y) {
		page.addInteractable(new Interactable.TextInteractable(title, 13, 14 + y, null, null));
		page.addInteractable(new Interactable.TextInteractable(value, 130 - Minecraft.getInstance().font.width(value), 14 + y, null, null));
	}
}

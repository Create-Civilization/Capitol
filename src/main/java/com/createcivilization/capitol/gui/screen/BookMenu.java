package com.createcivilization.capitol.gui.screen;

import com.createcivilization.capitol.constants.ClientConstants;
import com.createcivilization.capitol.gui.base.Asset;
import com.createcivilization.capitol.gui.base.BookScreen;
import com.createcivilization.capitol.gui.base.Interactable;
import com.createcivilization.capitol.gui.base.SmartScreen;
import com.createcivilization.capitol.gui.interactables.ButtonInteractable;
import com.createcivilization.capitol.gui.interactables.TextInputInteractable;
import com.createcivilization.capitol.gui.pages.DisplayTeam;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class BookMenu extends BookScreen {

	public int currentPage;
	Tabs tabs = new Tabs();
	int startingTab;
	Tabs.Tab currentTab;
	List<PageHandler> pageHandlers = List.of(
		new AttackHandler(),
		new DefenseHandler(),
		new SupportHandler(),
		new InfoHandler(),
		new SettingsHandler()
	);


	public BookMenu(int startingTab, int startingPage) {
		super(Component.literal("Book"));
		addInteractable(tabs);
		pageHandlers.forEach(pageHandler -> {
			pageHandler.hide();
			addInteractable(pageHandler);
		});
		this.startingTab = startingTab;
		this.currentPage = startingPage;
	}

	public BookMenu() {
		this(ClientConstants.lastTab, ClientConstants.lastPage);
	}

	@Override
	protected void init() {
		int halfWidth = BACKGROUND.getBlitWidth()/2;
		this.leftPos = (this.width / 2) - halfWidth;
		this.rightPos = leftPos + halfWidth;
		this.topPos = (this.height - BACKGROUND.getBlitHeight()) / 2;
		this.tabs.setX(this.rightPos + 20);
		this.tabs.setY(this.topPos - 16);
		super.init();
		((Tabs.Tab) this.tabs.getInteractableList().get(this.startingTab)).select();
	}

	private static class Tabs extends Interactable.InteractableBundle {

		int x,y;

		private Tabs() {
			this.setInteractableList(
				IntStream.range(0, 5).boxed().map(
					integer -> (Interactable) new Tab(296 + (integer * 23), (integer * 23), 0, integer)
				).toList()
			);
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

			BookMenu bookMenu;

			int index;

			public Tab(int off, int x, int y, int index) {
				super(ASSET.blit(off, 0, 15, 23), null, null, x, y, null, true);
				this.index = index;
			}

			@Override
			public void init(SmartScreen smartScreen) {
				this.bookMenu = (BookMenu) smartScreen;
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
				return this;
			}

			public void select() {
				getBlit().setSize(null, 28);
				setY(getY() - 5);
				bookMenu.currentTab = this;
				bookMenu.pageHandlers.get(this.index).show();
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
			super(List.of());
		}
	}
	private static class InfoHandler extends PageHandler {

		public InfoHandler() {
			super(List.of(
				new DisplayTeam()
			));
		}
	}
	private static class SettingsHandler extends PageHandler {

		public SettingsHandler() {
			super(List.of());
		}
	}

	private abstract static class PageHandler extends Interactable.InteractableBundle {

		int x,y;

		public PageHandler(List<Interactable> pages) {
			setInteractableList(pages);
		}

		@Override
		public void init(SmartScreen smartScreen) {
			BookMenu bookMenu = (BookMenu) smartScreen;
			int i = 0;
			for (Interactable interactable : getInteractableList()) {
				interactable.init(bookMenu);
				interactable.setX(i++ % 2 == 0 ? bookMenu.leftPos : bookMenu.rightPos);
				interactable.setY(bookMenu.topPos);
			}
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
		public TextInput(@Nullable Component placeHolderText, int x, int y) {
			super(ASSET.blit(148, 193, 106, 13), placeHolderText, x, y, null, true);
		}
	}
}

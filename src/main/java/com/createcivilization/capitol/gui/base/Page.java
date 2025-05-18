package com.createcivilization.capitol.gui.base;

import com.createcivilization.capitol.constants.ClientConstants;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class Page extends Interactable.InteractableBundle {

	int x,y;

	public Page(Component title) {
		setInteractableList(List.of(new TextInteractable(title, 72 - ClientConstants.INSTANCE.font.width(title.getString()) / 2, 14, null, null)));
	}

	public void addInteractable(Interactable interactable) {
		List<Interactable> interactables = new ArrayList<>(getInteractableList());
		interactables.add(interactable);
		setInteractableList(interactables);
	}

	public void remInteractable(Interactable interactable) {
		List<Interactable> interactables = new ArrayList<>(getInteractableList());
		interactables.remove(interactable);
		setInteractableList(interactables);
	}

	@Override
	public void init(SmartScreen smartScreen) {
		super.init(smartScreen);
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public void setX(int x) {
		this.x = x;
		getInteractableList().forEach(interactable -> interactable.setX(x + interactable.getX()));
	}

	@Override
	public void setY(int y) {
		this.y = y;
		getInteractableList().forEach(interactable -> interactable.setY(y + interactable.getY()));
	}
}

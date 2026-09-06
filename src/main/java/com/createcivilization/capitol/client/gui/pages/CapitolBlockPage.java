package com.createcivilization.capitol.client.gui.pages;

import com.createcivilization.capitol.client.gui.base.Interactable;
import com.createcivilization.capitol.client.gui.base.Page;
import com.createcivilization.capitol.client.gui.screen.CapitolBookMenu;
import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.networking.packets.C2SUpgradeCapitolBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.Color;

// rightmost tab of the block's book: shows what kind of block this is, plus
// an upgrade button (Village -> Town -> City) for non-Capital blocks.
public class CapitolBlockPage extends Page {

	public CapitolBlockPage(BlockPos capitolPos, boolean isCapital, CapitolTier tier, boolean canUpgrade) {
		super(Component.literal("Capitol Block"));

		String type = isCapital ? "Capital" : (tier != null ? Character.toUpperCase(tier.name().charAt(0)) + tier.name().substring(1).toLowerCase() : "Unknown");
		CapitolBookMenu.addTableEntry(this, Component.literal("Type:"), Component.literal(type), 28);

		CapitolTier next = tier == null ? null : tier.upgraded();
		if (canUpgrade && next != null) {
			addInteractable(new CapitolBookMenu.Button(20, 70, Component.literal("Upgrade to " + niceName(next)), () ->
				PacketDistributor.sendToServer(new C2SUpgradeCapitolBlock(capitolPos))
			));
		} else if (isCapital) {
			addInteractable(note("The Capital cannot be upgraded", 86));
		} else if (next == null) {
			addInteractable(note("Already at maximum tier", 86));
		} else {
			addInteractable(note("Missing manage permission", 86));
		}
	}

	private static Interactable note(String text, int y) {
		return new Interactable.TextInteractable(Component.literal(text), 20, y, Color.GRAY, true);
	}

	private static String niceName(CapitolTier tier) {
		return Character.toUpperCase(tier.name().charAt(0)) + tier.name().substring(1).toLowerCase();
	}
}

package com.createcivilization.capitol.client.gui.pages;

import com.createcivilization.capitol.client.gui.base.Interactable;
import com.createcivilization.capitol.client.gui.base.Page;
import com.createcivilization.capitol.client.gui.screen.CapitolBookMenu;
import com.createcivilization.capitol.common.data.CapitolMember;
import com.createcivilization.capitol.common.data.CapitolTier;
import com.createcivilization.capitol.common.networking.packets.C2SSetCapitolBlockMayor;
import com.createcivilization.capitol.common.networking.packets.C2SUpgradeCapitolBlock;
import com.createcivilization.capitol.common.networking.packets.S2COpenCapitolScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.awt.Color;

// rightmost tab of the block's book: shows what kind of block this is, who runs
// it, an upgrade button (Village -> Town -> City) for non-Capital blocks, and a
// way to assign a mayor to said block.
public class CapitolBlockPage extends Page {

	public CapitolBlockPage(S2COpenCapitolScreen payload) {
		super(Component.literal("Capitol Block"));

		String type = payload.isCapital() ? "Capital" : niceName(payload.tier());
		String name = payload.blockName() == null ? "Unnamed" : payload.blockName();
		String mayor = payload.mayorName() == null ? "None" : payload.mayorName();

		CapitolBookMenu.addTableEntry(this, Component.literal("Name:"), Component.literal(name), 14);
		CapitolBookMenu.addTableEntry(this, Component.literal("Type:"), Component.literal(type), 28);
		CapitolBookMenu.addTableEntry(this, Component.literal("Mayor:"), Component.literal(mayor), 42);

		CapitolTier next = payload.tier() == null ? null : payload.tier().upgraded();
		if (payload.canUpgrade() && next != null) {
			addInteractable(new CapitolBookMenu.Button(20, 70, Component.literal("Upgrade to " + niceName(next)), () ->
				PacketDistributor.sendToServer(new C2SUpgradeCapitolBlock(payload.capitolPos()))
			));
		} else if (payload.isCapital()) {
			addInteractable(note("The Capital cannot be upgraded", 86));
		} else if (next == null) {
			addInteractable(note("Already at maximum tier", 86));
		} else {
			addInteractable(note("Missing manage permission", 86));
		}

		// pick a mayor from the team (Capital is always run by the team leader)
		if (payload.canAssignMayor()) {
			int y = 104;
			for (CapitolMember member : payload.members()) {
				addInteractable(new CapitolBookMenu.Button(20, y, Component.literal(member.name()), () ->
					PacketDistributor.sendToServer(new C2SSetCapitolBlockMayor(payload.capitolPos(), member.uuid()))
				));
				y += 13;
			}
		} else if (payload.isCapital()) {
			addInteractable(note("Mayor is always the team leader", 104));
		}
	}

	private static Interactable note(String text, int y) {
		return new Interactable.TextInteractable(Component.literal(text), 20, y, Color.GRAY, true);
	}

	private static String niceName(CapitolTier tier) {
		if (tier == null) return "Village";
		return Character.toUpperCase(tier.name().charAt(0)) + tier.name().substring(1).toLowerCase();
	}
}
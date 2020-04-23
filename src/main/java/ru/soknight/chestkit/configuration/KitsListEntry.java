package ru.soknight.chestkit.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

@Data
@NoArgsConstructor
public class KitsListEntry {
	
	private boolean bold = false;
	private boolean italic = false;
	private boolean strikethrough = false;
	private boolean underlined = false;
	private boolean useHover = true;
	
	private ChatColor color = ChatColor.WHITE;
	private String hover;
	
	public KitsListEntry(ChatColor color, boolean bold, boolean italic, boolean strikethrough, boolean underlined, boolean useHover, String hover) {
		this.color = color;
		this.bold = bold;
		this.italic = italic;
		this.strikethrough = strikethrough;
		this.underlined = underlined;
		this.useHover = useHover;
		this.hover = hover;
	}
	
	public TextComponent applyStyle(TextComponent text, KitInstance kit, String cooldown) {
		text.setColor(color);
		text.setBold(bold);
		text.setItalic(italic);
		text.setStrikethrough(strikethrough);
		text.setUnderlined(underlined);
		if(useHover) {
			String customHover = hover.replace("%id%", kit.getId())
					.replace("%kit%", kit.getDisplayname()).replace("%time%", cooldown);
			text.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, TextComponent.fromLegacyText(customHover)));
		}
		return text;
	}
	
	public String applyStyle(String text) {
		text = color + text;
		if(bold) text = ChatColor.BOLD + text;
		if(italic) text = ChatColor.ITALIC + text;
		if(strikethrough) text = ChatColor.STRIKETHROUGH + text;
		if(underlined) text = ChatColor.UNDERLINE + text;
		return text;
	}
	
	public enum KitsListEntryState {
		AVAILABLE, COOLDOWNED, SINGLE, UNAVAILABLE;
	}
	
}

package me.quared.hubpvp.util;

import net.md_5.bungee.api.ChatColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class StringUtil {

	private StringUtil() {
		throw new UnsupportedOperationException("This class is not meant to be instantiated.");
	}

	public static String colorize(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static List<String> colorize(List<String> strings) {
		List<String> finalList = new ArrayList<>();
		for (String s : strings) finalList.add(colorize(s));
		return finalList;
	}

}

package com.dt2PerfectBossFailure;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.EnumSet;
import java.util.Set;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

// Settings for notification, ideally checkboxes so multiple can be used
// Chatbox
// Notify
// Audio (like the boost lost audio when using divines/antipoisons
@ConfigGroup("dt2perfectBossNotifier")
public interface dt2pbfConfig extends net.runelite.client.config.Config
{
	@ConfigItem(
		position = 0,
		keyName = "infobox",
		name = "Display infobox",
		description = "Show infobox displaying kill perfection status"
	)
	default boolean infobox()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "chatMessage",
		name = "Chat messages",
		description = "Show message in chat when failing a perfect kill"
	)
	default boolean chatbox()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "notifyRepeatedly",
		name = "Notify repeat failure",
		description = "Show failure message/play sound for each mistake"
	)
	default boolean notifyRepeatedly()
	{
		return false;
	}

	@ConfigSection(
		name="Sound",
		description="Sound notification",
		position=1,
		closedByDefault = true
	)
	String soundNotification = "soundNotification";

	@ConfigItem(
		keyName = "audiblyNotify",
		name = "Play sound",
		description = "Play a sound on failure",
		section = soundNotification
	)
	default boolean audiblyNotify()
	{
		return false;
	}

	@ConfigItem(
		keyName = "soundSelection",
		name = "Notification Sound",
		description = "A RuneScape sound ID to be notified with",
		section = soundNotification
	)
	default int soundSelection()
	{
		return 1043;
	}

	@ConfigSection(
		name="Highlight",
		description="Show kill status on boss",
		position=2,
		closedByDefault = true
	)
	String highlightSection = "highlightSection";

	@ConfigItem(
		position = 0,
		keyName = "feather",
		name = "Outline feather",
		description = "",
		section=highlightSection
	)
	default int feather() {return 2;}

	@ConfigItem(
		position = 0,
		keyName = "highlightWidth",
		name = "Highlight width",
		description = "Width of the edge",
		section=highlightSection
	)
	default double borderWidth() {return 2;}

	@ConfigItem(
		position = 1,
		keyName = "highlightDuke",
		name = "Duke",
		description="Highlight Duke",
		section=highlightSection
	)
	default overlayTypes highlightDuke() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 2,
		keyName = "dukePerfectHighlight",
		name = "Duke perfect highlight color",
		description = "The color Duke is highlighted when you haven't made a mistake",
		section=highlightSection
	)
	default Color dukePerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 3,
		keyName = "dukeFailureHighlight",
		name = "Duke failure highlight color",
		description = "The color Duke is highlighted when you've made a mistake",
		section=highlightSection
	)
	default Color dukeFailure() {return Color.red;}

	@ConfigItem(
		position = 4,
		keyName = "highlightWhisperer",
		name = "Whisperer",
		description="Highlight Whisperer",
		section=highlightSection
	)
	default overlayTypes highlightWhisperer() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "dukePerfectHighlight",
		name = "Whisperer perfect highlight color",
		description = "The color Whisperer is highlighted when you haven't made a mistake",
		section=highlightSection
	)
	default Color whispererPerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 6,
		keyName = "whispererFailureHighlight",
		name = "Whisperer failure highlight color",
		description = "The color Whisperer is highlighted when you've made a mistake",
		section=highlightSection
	)
	default Color whispererFailure() {return Color.red;}

	@ConfigItem(
		position = 7,
		keyName = "highlightVardorvis",
		name = "Vardorvis",
		description="Highlight Vardorvis",
		section=highlightSection
	)
	default overlayTypes highlightVardorvis() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 8,
		keyName = "vardorvisPerfectHighlight",
		name = "Vardorvis perfect highlight color",
		description = "The color Vardorvis is highlighted when you haven't made a mistake",
		section=highlightSection
	)
	default Color vardorvisPerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 9,
		keyName = "vardorvisFailureHighlight",
		name = "Vardorvis failure highlight color",
		description = "The color Vardorvis is highlighted when you've made a mistake",
		section=highlightSection
	)
	default Color vardorvisFailure() {return Color.red;}

	@ConfigItem(
		position = 10,
		keyName = "highlightLeviathan",
		name = "Leviathan",
		description="Highlight Leviathan",
		section=highlightSection
	)
	default overlayTypes highlightLeviathan() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 11,
		keyName = "leviathanPerfectHighlight",
		name = "Leviathan perfect highlight color",
		description = "The color Duke is highlighted when you haven't made a mistake",
		section=highlightSection
	)
	default Color leviathanPerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 12,
		keyName = "leviathanFailureHighlight",
		name = "Leviathan failure highlight color",
		description = "The color Leviathan is highlighted when you've made a mistake",
		section=highlightSection
	)
	default Color leviathanFailure() {return Color.red;}
}

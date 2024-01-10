package com.dt2PerfectBossFailure;

import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
// Settings for notification, ideally checkboxes so multiple can be used
// Chatbox
// Notify
// Audio (like the boost lost audio when using divines/antipoisons
@ConfigGroup("dt2perfectBossNotifier")
public interface dt2pbfConfig extends net.runelite.client.config.Config
{
	@ConfigItem(
		keyName = "infobox",
		name = "Display infobox",
		description = "Show infobox displaying kill perfection status"
	)
	default boolean infobox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chatMessage",
		name = "Chat messages",
		description = "Show message in chat when failing a perfect kill"
	)
	default boolean chatbox()
	{
		return true;
	}

	@ConfigItem(
		keyName = "notifyRepeatedly",
		name = "Notify repeat failure",
		description = "Show failure message for each mistake"
	)
	default boolean notifyRepeatedly()
	{
		return false;
	}

	@ConfigItem(
		keyName = "audiblyNotify",
		name = "Play sound",
		description = "Play a sound on failure"
	)
	default boolean audiblyNotify()
	{
		return false;
	}

	@ConfigItem(
		keyName = "soundSelection",
		name = "Notification Sound",
		description = "A RuneScape sound ID to be notified with"
	)
	default int soundSelection()
	{
		return 1043;
	}


}

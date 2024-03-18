package com.dt2PerfectBossFailure;

import com.dt2PerfectBossFailure.bosses.Duke;
import com.dt2PerfectBossFailure.bosses.Leviathan;
import com.dt2PerfectBossFailure.bosses.Vardorvis;
import com.dt2PerfectBossFailure.bosses.Whisperer;
import com.google.errorprone.annotations.Var;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.IterableHashTable;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
@PluginDescriptor(
	name = "Desert Treasure 2 Perfect Boss Notifier",
	description="Notifies player when failing perfect kill conditions",
	tags= {"desert", "treasure", "dt2", "perfect"}
)
public class d2tpbfPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private dt2pbfConfig config;
	@Inject
	private ChatMessageManager chatMessageManager;
	@Inject
	private InfoBoxManager infoBoxManager;
	@Inject
	private EventBus eventBus;

	@Inject
	private Duke duke;
	@Inject
	private Whisperer whisperer;
	@Inject
	private Vardorvis vardorvis;
	@Inject
	private Leviathan leviathan;

	private InfoBox infoBox;
	public String currentReason = "Perfect";
	public boolean notified = false;
	public NPC currentBoss;
	private WorldPoint lastLocation = null;
	//Regions
	private static final int[] BOSS_REGION_IDS = {4405, 8291, 12132, 10595};

	private static final String[] BOSS_NAMES = {"Duke Sucellus", "Vardorvis", "Leviathan", "The Whisperer"};

	@Override
	protected void startUp() throws Exception {
		eventBus.register(duke);
		eventBus.register(whisperer);
		eventBus.register(vardorvis);
		eventBus.register(leviathan);
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(duke);
		eventBus.unregister(whisperer);
		eventBus.unregister(vardorvis);
		eventBus.unregister(leviathan);
		removeInfobox();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!inBossRegion())
		{
			lastLocation = null;
			return;
		}
		lastLocation = client.getLocalPlayer().getWorldLocation();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (config.infobox() && ArrayUtils.contains(BOSS_NAMES, event.getNpc().getName()) && !event.getNpc().getName().contains("Head"))
		{
			currentBoss = event.getNpc();
			reset();
		}
	}

	@Subscribe
	public void onActorDeath(ActorDeath event)
	{
		if (inBossRegion())
		{
			if (event.getActor().equals(client.getLocalPlayer()))
			{
				reset();
			}
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{

		if (event.getActor().getName()==null)
		{
			return;
		}
		// Vardorvis' head scuffs the contains check
		if (ArrayUtils.contains(BOSS_NAMES, event.getActor().getName()) && !event.getActor().getName().contains("Head"))
		{
			reset();
			removeInfobox();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			if (!inBossRegion())
			{
				removeInfobox();
				reset();
				return;
			}
			reset();
		}
	}


	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if(configChanged.getGroup() != "dt2perfectBossNotifier")
		{
			return;
		}
		switch(configChanged.getKey())
		{
			case "infobox":
			{
				if (Boolean.parseBoolean(configChanged.getNewValue()))
				{
					reset();
				}
				else
				{
					removeInfobox();
				}
			}
		}
	}

	public boolean checkCollision(GraphicsObject object)
	{
		if(lastLocation == null)
		{
			return false;
		}
		LocalPoint localPoint = object.getLocation();
		WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
		return lastLocation.equals(worldPoint);
	}

	private boolean inBossRegion()
	{
		if (client.getMapRegions() == null)
		{
			return false;
		}
		for(int region : BOSS_REGION_IDS)
		{
			if (ArrayUtils.contains(client.getMapRegions(),region))
			{
				return true;
			}
		}
		return false;
	}

	public void notifyFailure(String bossName, String reason)
	{
		currentReason = reason;
		if (notified && !config.notifyRepeatedly())
		{
			return;
		}

		if (config.chatbox())
		{
			String chatMessage = new ChatMessageBuilder()
				.append(ChatColorType.HIGHLIGHT)
				.append("Perfect " + bossName + " failed: " + reason)
				.build();

			chatMessageManager.queue(QueuedMessage.builder()
				.type(ChatMessageType.FRIENDSCHATNOTIFICATION)
				.runeLiteFormattedMessage(chatMessage)
				.build());
		}

		if (infoBox != null)
		{
			removeInfobox();
		}
		createInfobox(true, reason);

		if (config.audiblyNotify())
		{
			client.playSoundEffect(config.soundSelection());
		}

		notified = true;
	}

	private void reset()
	{
		currentReason = "Perfect";
		if (infoBox != null)
		{
			removeInfobox();
		}
		if(config.infobox())
		{
			createInfobox(false,currentReason);
		}
		notified = false;
	}

	private void createInfobox(Boolean failed, String reason)
	{
		if (!config.infobox())
		{
			return;
		}

		if (infoBox == null && inBossRegion())
		{
			BufferedImage icon;
			if (failed)
			{
				icon = ImageUtil.loadImageResource(d2tpbfPlugin.class, "/icons/X_mark.png");
			}
			else
			{
				icon = ImageUtil.loadImageResource(d2tpbfPlugin.class, "/icons/Yes_check.png");
			}
			infoBox = new dt2pbfInfobox(this);
			infoBox.setImage(icon);
			infoBoxManager.addInfoBox(infoBox);
		}
	}

	private void removeInfobox()
	{
		if (infoBox != null)
		{
			infoBoxManager.removeInfoBox(infoBox);
			infoBox = null;
		}
	}

	@Provides
	dt2pbfConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(dt2pbfConfig.class);
	}
}

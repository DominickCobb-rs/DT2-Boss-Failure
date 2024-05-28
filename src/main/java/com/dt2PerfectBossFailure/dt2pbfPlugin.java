package com.dt2PerfectBossFailure;

import com.dt2PerfectBossFailure.bossFailure.Duke;
import com.dt2PerfectBossFailure.bossFailure.Leviathan;
import com.dt2PerfectBossFailure.bossFailure.Vardorvis;
import com.dt2PerfectBossFailure.bossFailure.Whisperer;
import com.dt2PerfectBossFailure.vardorvisUtils.VardorvisPillarHider;
import com.dt2PerfectBossFailure.vardorvisUtils.VardorvisPillarOverlay;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.NPC;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ActorDeath;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.ClientThread;
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
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
@PluginDescriptor(
	name = "DT2 Mistake Tracker",
	description="Informs the player of mistakes resulting in imperfect kills",
	tags= {"desert", "treasure", "dt2", "perfect"}
)
public class dt2pbfPlugin extends Plugin
{
	@Inject
	public Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private dt2pbfConfig config;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private InfoBoxManager infoBoxManager;

	@Inject
	private EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Duke duke;

	@Inject
	private Whisperer whisperer;

	@Inject
	private Vardorvis vardorvis;

	@Inject
	private Leviathan leviathan;

	@Inject
	private dt2pbfBossOverlay dt2pbfBossOverlay;

	@Inject
	public VardorvisPillarHider vardorvisPillarHider;

	@Inject
	private VardorvisPillarOverlay vardorvisPillarOverlay;

	private InfoBox infoBox;
	public String initialReason = "Perfect";
	public ArrayList<String> reasons = new ArrayList<String>();
	public boolean notified = false;
	public WorldPoint lastLocation = null;
	//Regions
	private static final int[] BOSS_REGION_IDS = {4405, 8291, 12132, 10595};

	private static final String[] BOSS_NAMES = {"duke sucellus", "vardorvis", "the leviathan", "the whisperer","odd figure"};

	@Override
	protected void startUp() throws Exception
	{
		eventBus.register(vardorvisPillarHider);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invoke(vardorvisPillarHider::hide);
		}
		overlayManager.add(vardorvisPillarOverlay);
		eventBus.register(duke);
		eventBus.register(whisperer);
		eventBus.register(vardorvis);
		eventBus.register(leviathan);
		dt2boss.DUKE.initialize(config.dukePerfect(),config.dukeFailure(),config.highlightDuke());
		dt2boss.WHISPERER.initialize(config.whispererPerfect(),config.whispererFailure(),config.highlightWhisperer());
		dt2boss.VARDORVIS.initialize(config.vardorvisPerfect(),config.whispererFailure(),config.highlightVardorvis());
		dt2boss.LEVIATHAN.initialize(config.leviathanPerfect(),config.leviathanFailure(),config.highlightLeviathan());
		overlayManager.add(dt2pbfBossOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(duke);
		eventBus.unregister(whisperer);
		eventBus.unregister(vardorvis);
		eventBus.unregister(leviathan);
		eventBus.unregister(vardorvisPillarHider);
		removeInfobox();
		overlayManager.remove(dt2pbfBossOverlay);
		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
		overlayManager.remove(vardorvisPillarOverlay);
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
		if (config.infobox() && ArrayUtils.contains(BOSS_NAMES, event.getNpc().getName().toLowerCase()) && !event.getNpc().getName().contains("Head"))
		{
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
		if (ArrayUtils.contains(BOSS_NAMES, event.getActor().getName().toLowerCase().trim()) && !event.getActor().getName().toLowerCase().contains("head"))
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
		if(!configChanged.getGroup().equalsIgnoreCase(config.DT2_UTILITIES_CONFIG_GROUP))
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
			break;
			case "highlightDuke":
			{
				dt2boss.DUKE.setRender(config.highlightDuke());
			}
			break;

			case "highlightWhisperer":
			{
				dt2boss.WHISPERER.setRender(config.highlightWhisperer());
			}
			break;

			case "highlightVardorvis":
			{
				dt2boss.VARDORVIS.setRender(config.highlightVardorvis());
			}
			break;

			case "highlightLeviathan":
			{
				dt2boss.LEVIATHAN.setRender(config.highlightLeviathan());
			}
			break;
			// Perfect Colors
			case "dukePerfectHighlight":
			{
				dt2boss.DUKE.setPerfectColor(config.dukePerfect());
			}
			break;

			case "whispererPerfectHighlight":
			{
				dt2boss.WHISPERER.setPerfectColor(config.whispererPerfect());
			}
			break;

			case "vardorvisPerfectHighlight":
			{
				dt2boss.VARDORVIS.setPerfectColor(config.vardorvisPerfect());
			}
			break;

			case "leviathanPerfectHighlight":
			{
				dt2boss.LEVIATHAN.setPerfectColor(config.leviathanPerfect());
			}
			break;

			// Failure Colors
			case "dukeFailureHighlight":
			{
				dt2boss.DUKE.setFailureColor(config.dukeFailure());
			}
			break;

			case "whispererFailureHighlight":
			{
				dt2boss.WHISPERER.setFailureColor(config.whispererFailure());
			}
			break;

			case "vardorvisFailureHighlight":
			{
				dt2boss.VARDORVIS.setFailureColor(config.vardorvisFailure());
			}
			break;

			case "leviathanFailureHighlight":
			{
				dt2boss.LEVIATHAN.setFailureColor(config.leviathanFailure());
			}
			break;
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
		if (initialReason == "Perfect")
		{
			initialReason = reason;
		}
		reasons.add(reason);
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
		initialReason = "Perfect";
		if (infoBox != null)
		{
			removeInfobox();
		}
		if(config.infobox())
		{
			createInfobox(false, initialReason);
		}
		notified = false;
		if(reasons!=null)
		{
			reasons.clear();
		}
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
				icon = ImageUtil.loadImageResource(dt2pbfPlugin.class, "/icons/X_mark.png");
			}
			else
			{
				icon = ImageUtil.loadImageResource(dt2pbfPlugin.class, "/icons/Yes_check.png");
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

package com.dt2PerfectBossFailure;

import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GameState;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
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
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
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
	private InfoBox infoBox;
	public boolean notified = false;


	//Regions
	private static final int VARDORVIS_REGION_ID = 4405;

	private static final int DUKE_REGION_ID = 12132;
	private static final int WHISPERER_REGION_ID = 10595;
	private static final int LEVIATHAN_REGION_ID = 8291;

	// Vardorvis
	private static final String VARDORVIS = "Vardorvis";
	private static final String VARDORVIS_PROJECTILE_MESSAGE = "You've been injured and can't use protection prayers!";
	private static final String VARDORVIS_AXE_MESSAGE = "You have started bleeding!";
	private static final String VARDORVIS_CAPTCHA_MESSAGE = "The tendrils tighten around you, damaging you in the process!";
	private static final int[] VARDORVIS_ATTACKS = {10340, 10341, 10342, 10343};
	private static final int VARDORVIS_SPIKE = 2512;

	// Duke
	private static final int DUKE_VENT = 12198;
	private static final int DUKE_MAGIC_PROJECTILE = 2434;
	private static final String DUKE = "Duke Sucellus";
	private static final String EXTREMITY_FREEZE_MESSAGE = "You've been frozen in place!";
	private static final String DUKE_FREEZE_MESSAGE = "You have been frozen!";
	//WHY ARE THERE SO MANY
	private static final int[] DUKE_IDS = {NpcID.DUKE_SUCELLUS_12167, NpcID.DUKE_SUCELLUS, NpcID.DUKE_SUCELLUS_12191, NpcID.DUKE_SUCELLUS_12192, NpcID.DUKE_SUCELLUS_12193, NpcID.DUKE_SUCELLUS_12194,
		NpcID.DUKE_SUCELLUS_12195, NpcID.DUKE_SUCELLUS_12196};

	// Leviathan
	private static final String LEVIATHAN = "The Leviathan";
	private static final int[] LEVIATHAN_IDS = {NpcID.THE_LEVIATHAN, NpcID.THE_LEVIATHAN_12215, NpcID.THE_LEVIATHAN_12219, NpcID.THE_LEVIATHAN_12221};
	private static final int LEVIATHAN_RANGED_PROJECTILE = 2487;
	private static final int LEVIATHAN_MAGE_PROJECTILE = 2489;
	private static final int LEVIATHAN_MELEE_PROJECTILE = 2488;
	private static final int[] LEVIATHAN_PROJECTILE_IDS = {LEVIATHAN_MAGE_PROJECTILE, LEVIATHAN_MELEE_PROJECTILE, LEVIATHAN_RANGED_PROJECTILE};
	private static final int[] LEVIATHAN_SPECIAL_ATTACK_ANIMATIONS = {10285, 10286, 10287, 10290};
	private final List<LeviathanProjectile> activeLeviathanProjectiles = new ArrayList<>();
	private boolean leviathanSpecial = false;

	// Whisperer
	private static final String WHISPERER = "The Whisperer";
	private static final int WHISPERER_MELEE = 10234;
	private static final int[] WHISPERER_SPLASH = {2447, 2448, 2449, 2450};
	private static final String[] BOSS_NAMES = {DUKE, VARDORVIS, LEVIATHAN, WHISPERER};

	@Override
	protected void shutDown() throws Exception
	{
		removeInfobox();
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		Iterator<LeviathanProjectile> iterator = activeLeviathanProjectiles.iterator();
		while (iterator.hasNext())
		{
			LeviathanProjectile projectile = iterator.next();
			if (projectile.getCycle() == client.getGameCycle())
			{
				checkLeviathanProjectile(projectile);
				iterator.remove();
				return;
			}
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		if (!inBossRegion() || notified)
		{
			return;
		}
		if (event.getProjectile().getId() == DUKE_MAGIC_PROJECTILE)
		{
			notifyFailure(DUKE, "You were hit by Duke Sucellus's magic attack.");
		}
		if (ArrayUtils.contains(LEVIATHAN_PROJECTILE_IDS, event.getProjectile().getId()))
		{
			// Prevent duplicate projectiles
			Iterator<LeviathanProjectile> iterator = activeLeviathanProjectiles.iterator();
			while (iterator.hasNext())
			{
				LeviathanProjectile projectile = iterator.next();
				if (projectile.getCycle() == event.getProjectile().getEndCycle())
				{
					return;
				}
			}
			activeLeviathanProjectiles.add(new LeviathanProjectile(event.getProjectile()));
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!inBossRegion() || (notified && !config.notifyRepeatedly()) || event.getActor().getName() == null)
		{
			return;
		}
		NPC npc;
		if (event.getActor() instanceof NPC)
		{
			npc = (NPC) event.getActor();
		}
		else
		{
			return;
		}
		// Vardorvis Auto-Attacks
		if (npc.isInteracting())
		{
			if (npc.getInteracting().equals(client.getLocalPlayer()))
			{
				if (npc.getId() == NpcID.VARDORVIS)
				{
					if (ArrayUtils.contains(VARDORVIS_ATTACKS, npc.getAnimation()) && client.getServerVarbitValue(Prayer.PROTECT_FROM_MELEE.getVarbit()) != 1 && client.getServerVarbitValue(Prayer.RP_DAMPEN_MELEE.getVarbit()) != 1)
					{
						notifyFailure(VARDORVIS, "You were hit off-prayer.");
						return;
					}
				}

			}
		}
		// Leviathan Specials
		if (ArrayUtils.contains(LEVIATHAN_IDS, npc.getId()))
		{
			// Leviathan is always in one of these animations prior to damage hitting the player,
			// so we can reliably assume the hitsplatApplied will come after the leviathanSpecial has been set
			leviathanSpecial = ArrayUtils.contains(LEVIATHAN_SPECIAL_ATTACK_ANIMATIONS, npc.getAnimation());
		}
	}


	// Vardorvis dash attack
	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		if (!inBossRegion() || notified)
		{
			return;
		}
		if (event.getGraphicsObject().getId() == VARDORVIS_SPIKE)
		{
			if (checkCollision(event.getGraphicsObject()))
			{
				notifyFailure(VARDORVIS, "You were hit by a ground spike.");
			}
		}
	}

	// Duke: Extremities/Gaze
	// Vardorvis: Axe/Tendrils/Heads
	@Subscribe
	public void onChatMessage(ChatMessage message)
	{
		if (message.getType() != ChatMessageType.GAMEMESSAGE || !inBossRegion() || (notified && !config.notifyRepeatedly()))
		{
			return;
		}

		if (message.getMessage().contains(EXTREMITY_FREEZE_MESSAGE))
		{
			notifyFailure(DUKE, "You were frozen.");
		}

		if (message.getMessage().contains(DUKE_FREEZE_MESSAGE))
		{
			notifyFailure(DUKE, "Duke gazed upon you.");
		}

		if (message.getMessage().contains(VARDORVIS_PROJECTILE_MESSAGE))
		{
			notifyFailure(VARDORVIS, "You were hit by a head projectile off-prayer.");
		}

		if (message.getMessage().contains(VARDORVIS_AXE_MESSAGE))
		{
			notifyFailure(VARDORVIS, "You were hit by an axe.");
		}

		if (message.getMessage().contains(VARDORVIS_CAPTCHA_MESSAGE))
		{
			notifyFailure(VARDORVIS, "You failed to complete the captcha.");
		}
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		if (config.infobox() && ArrayUtils.contains(BOSS_NAMES, event.getNpc().getName()))
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

	// Duke: Autos
	// Leviathan: Post-Enrage autos, special attacks
	// Whisperer: Missed flicks, special attacks
	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (!inBossRegion() || (notified && !config.notifyRepeatedly()))
		{
			return;
		}
		Actor target = hitsplatApplied.getActor();

		if (!(target instanceof Player))
		{
			return;
		}
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (hitsplat.isMine() && target == client.getLocalPlayer() && hitsplat.getHitsplatType() != HitsplatID.BLOCK_ME)
		{
			List<NPC> npcs = client.getNpcs();
			for (NPC npc : npcs)
			{
				// Leviathan
				if (npc.getId() == NpcID.THE_LEVIATHAN)
				{
					boolean pathfinder = false;
					for (NPC n : npcs)
					{
						if (n.getId() == NpcID.ABYSSAL_PATHFINDER)
						{
							pathfinder = true;
						}
					}
					if (leviathanSpecial || pathfinder)
					{
						notifyFailure(LEVIATHAN, "You took avoidable damage.");
						return;
					}
				}
				// Whisperer
				if (npc.getId() == NpcID.THE_WHISPERER)
				{
					if (npc.getAnimation() == WHISPERER_MELEE && client.getServerVarbitValue(Prayer.PROTECT_FROM_MELEE.getVarbit()) == 1 && client.getServerVarbitValue(Prayer.RP_DAMPEN_MELEE.getVarbit()) == 1)
					{
						return;
					}
					Deque<GraphicsObject> graphicsObjects = client.getGraphicsObjects();
					for (GraphicsObject object : graphicsObjects)
					{
						if (ArrayUtils.contains(WHISPERER_SPLASH, object.getId()))
						{
							if (checkCollision(object) && hitsplat.getAmount() == 20)
							{
								return;
							}
						}
					}
					notifyFailure(WHISPERER, "You took avoidable damage.");
				}
				// Vents
				if (npc.getId() == DUKE_VENT)
				{
					WorldArea ventArea = new WorldArea(npc.getWorldLocation(), 2, 2);
					if (client.getLocalPlayer().getWorldLocation().isInArea(ventArea))
					{
						notifyFailure(DUKE, "You breathed in gas.");
						return;
					}
				}
				// Duke auto
				if (ArrayUtils.contains(DUKE_IDS, npc.getId()))
				{
					WorldArea dukeMeleeArea = new WorldArea(npc.getWorldLocation().getX(), npc.getWorldLocation().getY() - 1, 7, 1, npc.getWorldLocation().getPlane());
					if (client.getLocalPlayer().getWorldLocation().isInArea(dukeMeleeArea))
					{
						if (hitsplat.getAmount() > 11)
						{
							notifyFailure(DUKE, "You were hit by Duke Sucellus' slam attack.");
							return;
						}
					}
				}
			}
		}
	}

	private void checkLeviathanProjectile(LeviathanProjectile projectile)
	{
		switch (projectile.getType())
		{
			case LEVIATHAN_MAGE_PROJECTILE:
			{
				if (client.getServerVarbitValue(Prayer.PROTECT_FROM_MAGIC.getVarbit()) != 1 && client.getServerVarbitValue(Prayer.RP_DAMPEN_MAGIC.getVarbit()) != 1)
				{
					notifyFailure(LEVIATHAN, "You were hit by a magic attack off prayer.");
					return;
				}
			}
			break;
			case LEVIATHAN_RANGED_PROJECTILE:
			{
				if (client.getServerVarbitValue(Prayer.PROTECT_FROM_MISSILES.getVarbit()) != 1 && client.getServerVarbitValue(Prayer.RP_DAMPEN_RANGED.getVarbit()) != 1)
				{
					notifyFailure(LEVIATHAN, "You were hit by a ranged attack off prayer.");
					return;
				}
			}
			break;
			case LEVIATHAN_MELEE_PROJECTILE:
			{
				if (client.getServerVarbitValue(Prayer.PROTECT_FROM_MELEE.getVarbit()) != 1 && client.getServerVarbitValue(Prayer.RP_DAMPEN_MELEE.getVarbit()) != 1)
				{
					notifyFailure(LEVIATHAN, "You were hit by a melee attack off prayer.");
					return;
				}
			}
			break;
		}
	}

	private boolean checkCollision(GraphicsObject object)
	{
		LocalPoint localPoint = object.getLocation();
		WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
		return client.getLocalPlayer().getWorldLocation().equals(worldPoint);
	}

	private boolean inBossRegion()
	{
		if (client.getMapRegions() == null)
		{
			return false;
		}
		//Vardorvis
		if (ArrayUtils.contains(client.getMapRegions(), VARDORVIS_REGION_ID))
		{
			return true;
		}
		//Duke
		if (ArrayUtils.contains(client.getMapRegions(), DUKE_REGION_ID))
		{
			return true;
		}
		//Whisperer
		if (ArrayUtils.contains(client.getMapRegions(), WHISPERER_REGION_ID))
		{
			return true;
		}
		//Leviathan
		return ArrayUtils.contains(client.getMapRegions(), LEVIATHAN_REGION_ID);
	}

	private void notifyFailure(String bossName, String reason)
	{
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
			createInfobox(true);
		}

		if (config.audiblyNotify())
		{
			client.playSoundEffect(config.soundSelection());
		}

		notified = true;
	}

	private void reset()
	{
		if (infoBox != null && config.infobox())
		{
			removeInfobox();
			createInfobox(false);
		}
		activeLeviathanProjectiles.clear();
		notified = false;
	}

	private void createInfobox(Boolean failed)
	{
		if (infoBox == null)
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

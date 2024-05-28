package com.dt2PerfectBossFailure.bossFailure;

import com.dt2PerfectBossFailure.dt2pbfPlugin;
import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.google.inject.Inject;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class Duke
{
	@Inject
	private Client client;

	@Inject
	private dt2pbfPlugin plugin;

	@Inject
	private dt2pbfConfig config;

	// Duke
	private int projectileEndTick = -1;
	private static final int DUKE_VENT = 12198;
	private static final int DUKE_MAGIC_PROJECTILE = 2434;
	private static final String DUKE = "Duke Sucellus";
	private static final String EXTREMITY_FREEZE_MESSAGE = "You've been frozen in place!";
	private static final String DUKE_FREEZE_MESSAGE = "You have been frozen!";
	private static final int DUKE_REGION_ID = 12132;
	private static final int[] DUKE_ICICLES = {2440, 2441, 2442, 2443};
	//WHY ARE THERE SO MANY
	public static final int[] DUKE_IDS = {NpcID.DUKE_SUCELLUS_12167, NpcID.DUKE_SUCELLUS, NpcID.DUKE_SUCELLUS_12191, NpcID.DUKE_SUCELLUS_12192, NpcID.DUKE_SUCELLUS_12193, NpcID.DUKE_SUCELLUS_12194,
		NpcID.DUKE_SUCELLUS_12195, NpcID.DUKE_SUCELLUS_12196};

	private boolean inDukeRegion()
	{
		return ArrayUtils.contains(client.getMapRegions(), DUKE_REGION_ID);
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if(projectileEndTick == -1)
		{
			return;
		}
		if(projectileEndTick <= client.getGameCycle())
		{
			projectileEndTick = -1;
		}
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		if (!inDukeRegion() || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}
		if (event.getProjectile().getId() == DUKE_MAGIC_PROJECTILE && projectileEndTick == -1)
		{
			projectileEndTick = event.getProjectile().getEndCycle()+1;
			plugin.notifyFailure(DUKE, "You were hit by Duke Sucellus's magic attack.");
		}
	}

	// Duke: Extremities/Gaze
	@Subscribe
	public void onChatMessage(ChatMessage message)
	{
		if (message.getType() != ChatMessageType.GAMEMESSAGE || !inDukeRegion() || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}

		if (message.getMessage().contains(EXTREMITY_FREEZE_MESSAGE))
		{
			plugin.notifyFailure(DUKE, "You were frozen.");
		}

		if (message.getMessage().contains(DUKE_FREEZE_MESSAGE))
		{
			plugin.notifyFailure(DUKE, "Duke gazed upon you.");
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (!inDukeRegion() || (plugin.notified && !config.notifyRepeatedly()))
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
			Deque<GraphicsObject> graphicsObjects = client.getGraphicsObjects();
			Iterator<GraphicsObject> iterator = graphicsObjects.iterator();
			while (iterator.hasNext())
			{
				GraphicsObject obj = iterator.next();
				if (ArrayUtils.contains(DUKE_ICICLES, obj.getId()) && checkCollision(obj))
				{
						plugin.notifyFailure(DUKE, "You were hit by Duke's melee");
						return;
				}
			}
			/*
			* TODO: Track active vents and check how long they've been active.
			* Race condition where duke melees as soon as the vent is active but the vent isn't applying the damage
			*/
			List<NPC> npcs = client.getNpcs();
			for (NPC npc : npcs)
			{
				// Vents
				if (npc.getId() == DUKE_VENT)
				{
					WorldArea ventArea = new WorldArea(npc.getWorldLocation(), 3, 3);
					if (client.getLocalPlayer().getWorldLocation().isInArea(ventArea))
					{
						plugin.notifyFailure(DUKE, "You took damage from the Duke's vents.");
						return;
					}
				}
			}
		}
	}

	private boolean checkCollision(GraphicsObject obj)
	{
		LocalPoint localPoint = obj.getLocation();
		WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
		return worldPoint.equals(plugin.lastLocation);
	}
}

package com.dt2PerfectBossFailure.bosses;

import com.dt2PerfectBossFailure.dt2pbfPlugin;
import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.google.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicsObjectCreated;
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
	private int iciclesPopTick = -1;
	private static final int DUKE_VENT = 12198;
	private static final int DUKE_MAGIC_PROJECTILE = 2434;
	private static final String DUKE = "Duke Sucellus";
	private static final String EXTREMITY_FREEZE_MESSAGE = "You've been frozen in place!";
	private static final String DUKE_FREEZE_MESSAGE = "You have been frozen!";
	private static final int DUKE_REGION_ID = 12132;
	private static final int[] DUKE_ICICLES = {2430,2431,2432,2433};
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
		if(projectileEndTick == -1 || iciclesPopTick == -1)
		{
			return;
		}
		if(iciclesPopTick == client.getTickCount() && isHuggingDuke())
		{
			iciclesPopTick = -1;
			plugin.notifyFailure(DUKE,"You were hit by Duke's melee.");
		}
		if(projectileEndTick == client.getTickCount())
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
			List<NPC> npcs = client.getNpcs();
			for (NPC npc : npcs)
			{
				// Vents
				if (npc.getId() == DUKE_VENT)
				{
					WorldArea ventArea = new WorldArea(npc.getWorldLocation(), 2, 2);
					if (client.getLocalPlayer().getWorldLocation().isInArea(ventArea))
					{
						plugin.notifyFailure(DUKE, "You took damage from the Duke's vents.");
						return;
					}
				}
			}
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated graphicsObjectCreated)
	{
		if(!inDukeRegion() || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}
		if(ArrayUtils.contains(DUKE_ICICLES,graphicsObjectCreated.getGraphicsObject().getId()))
		{
			iciclesPopTick = client.getTickCount() + 2;
		}
	}

	private boolean isHuggingDuke()
	{
		List<NPC> npcs = client.getNpcs();
		for (NPC npc : npcs)
		{
			if(ArrayUtils.contains(DUKE_IDS,npc.getId()))
			{
				WorldArea dukeMeleeArea = new WorldArea(npc.getWorldLocation().getX(), npc.getWorldLocation().getY() - 1, 7, 1, npc.getWorldLocation().getPlane());
				if (client.getLocalPlayer().getWorldLocation().isInArea(dukeMeleeArea))
				{
					return true;
				}
			}
		}
		return false;
	}
}

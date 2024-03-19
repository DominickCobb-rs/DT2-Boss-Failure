package com.dt2PerfectBossFailure.bosses;

import com.dt2PerfectBossFailure.d2tpbfPlugin;
import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.google.inject.Inject;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ActorSpotAnim;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.IterableHashTable;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class Leviathan
{
	@Inject
	private Client client;

	@Inject
	private d2tpbfPlugin plugin;

	@Inject
	private dt2pbfConfig config;

	// Leviathan
	private static final String LEVIATHAN = "The Leviathan";
	private static final int[] LEVIATHAN_IDS = {NpcID.THE_LEVIATHAN, NpcID.THE_LEVIATHAN_12215, NpcID.THE_LEVIATHAN_12219, NpcID.THE_LEVIATHAN_12221};
	private static final int LEVIATHAN_RANGED_SPOT_ANIM = 2490;
	private static final int LEVIATHAN_MAGE_SPOT_ANIM = 2492;
	private static final int LEVIATHAN_MELEE_SPOT_ANIM = 2491;
	private static final int[] LEVIATHAN_SPECIAL_ATTACK_ANIMATIONS = {10285, 10286, 10287, 10290};
	private static final int[] LEVIATHAN_SMOKE_BLAST_ANIM = {10290, 10287};
	private static final int[] LEVIATHAN_LIGHTNING_ANIM = {10285, 10286};
	private static final int BOULDER_MOVE_ANIM = 1114;
	private static final int[] LEVIATHAN_BOULDER_SHADOWS = {2475, 2476, 2477, 2478, 2479, 2480};
	private int leviathanSpecial = -1;
	private static final int LEVIATHAN_REGION_ID = 8291;

	private boolean inLeviathanRegion()
	{
		return ArrayUtils.contains(client.getMapRegions(), LEVIATHAN_REGION_ID);
	}

	@Subscribe
	public void onGraphicChanged(GraphicChanged graphicChanged)
	{
		if(!inLeviathanRegion() || !graphicChanged.getActor().equals(client.getLocalPlayer()) || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}
		// Check projectiles
		IterableHashTable<ActorSpotAnim> spotAnims = client.getLocalPlayer().getSpotAnims();
		Iterator<ActorSpotAnim> animsIterator = spotAnims.iterator();
		while (animsIterator.hasNext())
		{
			ActorSpotAnim anim = animsIterator.next();
			if(anim.getId() == LEVIATHAN_RANGED_SPOT_ANIM)
			{
				plugin.notifyFailure(LEVIATHAN,"You were hit by a ranged projectile off prayer.");
				return;
			}
			if(anim.getId() == LEVIATHAN_MELEE_SPOT_ANIM)
			{
				plugin.notifyFailure(LEVIATHAN,"You were hit by a melee projectile off prayer.");
				return;
			}
			if(anim.getId() == LEVIATHAN_MAGE_SPOT_ANIM)
			{
				plugin.notifyFailure(LEVIATHAN,"You were hit by a mage projectile off prayer.");
				return;
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!inLeviathanRegion() || event.getActor().getName() == null || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}
		NPC npc;
		if (event.getActor() instanceof NPC)
		{
			npc = (NPC) event.getActor();
			// Leviathan Specials
			if (ArrayUtils.contains(LEVIATHAN_IDS, npc.getId()))
			{
				// Leviathan is always in one of these animations prior to damage hitting the player,
				// so we can reliably assume the hitsplatApplied will come after the leviathanSpecial has been set
				leviathanSpecial = ArrayUtils.contains(LEVIATHAN_SPECIAL_ATTACK_ANIMATIONS, npc.getAnimation()) ? npc.getAnimation() : -1;
			}
		}
		else if (event.getActor().equals(client.getLocalPlayer()) && event.getActor().getAnimation() == BOULDER_MOVE_ANIM)
		{
			plugin.notifyFailure(LEVIATHAN, "You were hit by falling rubble");
		}
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (!inLeviathanRegion() || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}
		if (!(hitsplatApplied.getActor() == client.getLocalPlayer()))
		{
			return;
		}
		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (hitsplat.isMine() && hitsplat.getHitsplatType() != HitsplatID.BLOCK_ME)
		{
			Deque<GraphicsObject> graphicsObjects = client.getGraphicsObjects();
			Iterator<GraphicsObject> iterator = graphicsObjects.iterator();
			while (iterator.hasNext())
			{
				GraphicsObject obj = iterator.next();
				if (ArrayUtils.contains(LEVIATHAN_BOULDER_SHADOWS, obj.getId()))
				{
					if (checkCollision(obj))
					{
						log.info("Found possible collision");
						log.info("ObjectID: " + obj.getId());
						log.info("Animation Frame: " + obj.getAnimationFrame());
						if (obj.getAnimationFrame() < 3)
						{
							return;
						}
						plugin.notifyFailure(LEVIATHAN, "You were hit by falling rubble");
						return;
					}
				}
			}

			// Check pathfinder or special (special might be checked differently soon O.O)
			if(ArrayUtils.contains(LEVIATHAN_SPECIAL_ATTACK_ANIMATIONS,leviathanSpecial))
			{
				if(ArrayUtils.contains(LEVIATHAN_SMOKE_BLAST_ANIM,leviathanSpecial))
				{
					plugin.notifyFailure(LEVIATHAN, "You were hit by the smoke blast.");
					return;
				}
				if(ArrayUtils.contains(LEVIATHAN_LIGHTNING_ANIM,leviathanSpecial))
				{
					plugin.notifyFailure(LEVIATHAN, "You were hit by lightning.");
					return;
				}
			}

			List<NPC> npcs = client.getNpcs();
			for (NPC npc : npcs)
			{
				if (npc.getId() == NpcID.ABYSSAL_PATHFINDER)
				{
					plugin.notifyFailure(LEVIATHAN, "You took avoidable damage.");
					return;
				}
			}
		}
	}
	private boolean checkCollision(GraphicsObject obj)
	{
		// WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
		LocalPoint localPoint = obj.getLocation();
		WorldPoint worldPoint = WorldPoint.fromLocal(client, localPoint);
		return worldPoint.equals(plugin.lastLocation);
	}
}

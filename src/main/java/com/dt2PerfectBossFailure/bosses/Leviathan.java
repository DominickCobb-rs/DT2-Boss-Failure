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
	private static final int BOULDER_MOVE_ANIM = 1114;
	private static final int[] LEVIATHAN_BOULDER_SHADOWS = {2475, 2476, 2477};
	private boolean leviathanSpecial = false;
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
			}
			if(anim.getId() == LEVIATHAN_MELEE_SPOT_ANIM)
			{
				plugin.notifyFailure(LEVIATHAN,"You were hit by a melee projectile off prayer.");
			}
			if(anim.getId() == LEVIATHAN_MAGE_SPOT_ANIM)
			{
				plugin.notifyFailure(LEVIATHAN,"You were hit by a mage projectile off prayer.");
			}
			return;
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
				leviathanSpecial = ArrayUtils.contains(LEVIATHAN_SPECIAL_ATTACK_ANIMATIONS, npc.getAnimation());
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
						plugin.notifyFailure(LEVIATHAN, "You took avoidable damage.");
						return;
					}
					// Lastly check shadows
					WorldPoint currentLocation = client.getLocalPlayer().getWorldLocation();
					Deque<GraphicsObject> graphicsObjects = client.getGraphicsObjects();
					Iterator<GraphicsObject> iterator = graphicsObjects.iterator();
					while (iterator.hasNext())
					{
						GraphicsObject obj = iterator.next();
						for (int shadow : LEVIATHAN_BOULDER_SHADOWS)
						{
							if (obj.getId() == shadow)
							{
								LocalPoint localShadow = obj.getLocation();
								WorldPoint worldShadow = WorldPoint.fromLocal(client, localShadow);
								if (worldShadow.equals(currentLocation))
								{
									plugin.notifyFailure(LEVIATHAN, "You were hit by falling rubble");
								}
							}
						}
					}
				}
			}
		}
	}
}

package com.dt2PerfectBossFailure.bosses;

import com.dt2PerfectBossFailure.d2tpbfPlugin;
import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.google.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GraphicsObject;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class Whisperer
{
	@Inject
	private Client client;

	@Inject
	private d2tpbfPlugin plugin;

	@Inject
	private dt2pbfConfig config;

	private static final int WHISPERER_REGION_ID = 10595;
	private static final String WHISPERER = "The Whisperer";
	private static final int WHISPERER_MELEE = 10234;
	private static final int[] WHISPERER_SPLASH = {2447, 2448, 2449, 2450};

	private boolean inWhispererRegion()
	{
		return ArrayUtils.contains(client.getMapRegions(),WHISPERER_REGION_ID);
	}

	@Subscribe
	public void onHitsplatApplied(HitsplatApplied hitsplatApplied)
	{
		if (!inWhispererRegion() || (plugin.notified && !config.notifyRepeatedly()))
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
							if (plugin.checkCollision(object) && hitsplat.getAmount() == 20)
							{
								return;
							}
						}
					}
					plugin.notifyFailure(WHISPERER, "You took avoidable damage.");
				}
			}
		}
	}
}

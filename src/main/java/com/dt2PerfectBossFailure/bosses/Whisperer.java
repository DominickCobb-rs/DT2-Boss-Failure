package com.dt2PerfectBossFailure.bosses;

import com.dt2PerfectBossFailure.dt2pbfPlugin;
import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Hitsplat;
import net.runelite.api.HitsplatID;
import net.runelite.api.NpcID;
import net.runelite.api.Player;
import net.runelite.api.events.HitsplatApplied;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class Whisperer
{
	@Inject
	private Client client;

	@Inject
	private dt2pbfPlugin plugin;

	@Inject
	private dt2pbfConfig config;

	public static final int[] WHISPERER_IDS = {NpcID.THE_WHISPERER,NpcID.THE_WHISPERER_12205,NpcID.THE_WHISPERER_12206,NpcID.THE_WHISPERER_12207};
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
		if (!inWhispererRegion() || !(hitsplatApplied.getActor() instanceof Player) || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}

		Hitsplat hitsplat = hitsplatApplied.getHitsplat();
		if (hitsplat.isMine() && hitsplatApplied.getActor() == client.getLocalPlayer() && hitsplat.getHitsplatType() != HitsplatID.BLOCK_ME)
		{
			plugin.notifyFailure(WHISPERER, "You took avoidable damage.");
		}
	}
}

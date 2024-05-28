package com.dt2PerfectBossFailure.bossFailure;

import com.dt2PerfectBossFailure.dt2pbfPlugin;
import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NpcID;
import net.runelite.api.Prayer;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GraphicsObjectCreated;
import net.runelite.client.eventbus.Subscribe;
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class Vardorvis
{
	@Inject
	private Client client;

	@Inject
	private dt2pbfPlugin plugin;

	@Inject
	private dt2pbfConfig config;

	public static final int[] VARDORVIS_IDS = {NpcID.VARDORVIS,NpcID.VARDORVIS_12224,NpcID.VARDORVIS_12425,NpcID.VARDORVIS_12426,NpcID.VARDORVIS_12228};
	private static final int VARDORVIS_REGION_ID = 4405;
	private static final String VARDORVIS = "Vardorvis";
	private static final String VARDORVIS_PROJECTILE_MESSAGE = "You've been injured and can't use protection prayers!";
	private static final String VARDORVIS_AXE_MESSAGE = "You have started bleeding!";
	private static final String VARDORVIS_CAPTCHA_MESSAGE = "The tendrils tighten around you, damaging you in the process!";
	private static final int[] VARDORVIS_ATTACKS = {10340, 10341, 10342, 10343};
	private static final int VARDORVIS_SPIKE = 2512;

	private boolean inVardorvisRegion()
	{
		if(client.getMapRegions() == null)
		{
			return false;
		}
		return ArrayUtils.contains(client.getMapRegions(), VARDORVIS_REGION_ID);
	}

	// Vardorvis dash attack
	// TODO: Improve this. Can probably be simplified like Duke melees in onHitsplatApplied...
	//  but race conditions
	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		if (!inVardorvisRegion() || plugin.notified)
		{
			return;
		}
		if (event.getGraphicsObject().getId() == VARDORVIS_SPIKE)
		{
			if (plugin.checkCollision(event.getGraphicsObject()))
			{
				plugin.notifyFailure(VARDORVIS, "You were hit by a ground spike.");
			}
		}
	}

	@Subscribe
	public void onAnimationChanged(AnimationChanged event)
	{
		if (!inVardorvisRegion() || event.getActor().getName() == null)
		{
			return;
		}
		NPC npc;
		if (event.getActor() instanceof NPC)
		{
			npc = (NPC) event.getActor();
			// Vardorvis Auto-Attacks
			if (npc.isInteracting())
			{
				if (npc.getInteracting().equals(client.getLocalPlayer()))
				{
					if (npc.getId() == NpcID.VARDORVIS)
					{
						if (ArrayUtils.contains(VARDORVIS_ATTACKS, npc.getAnimation()) && client.getServerVarbitValue(Prayer.PROTECT_FROM_MELEE.getVarbit()) != 1 && client.getServerVarbitValue(Prayer.RP_DAMPEN_MELEE.getVarbit()) != 1)
						{
							plugin.notifyFailure(VARDORVIS, "You were hit off-prayer.");
						}
					}
				}
			}
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage message)
	{
		if (message.getType() != ChatMessageType.GAMEMESSAGE || !inVardorvisRegion() || (plugin.notified && !config.notifyRepeatedly()))
		{
			return;
		}

		if (message.getMessage().contains(VARDORVIS_PROJECTILE_MESSAGE))
		{
			plugin.notifyFailure(VARDORVIS, "You were hit by a head projectile off-prayer.");
		}

		if (message.getMessage().contains(VARDORVIS_AXE_MESSAGE))
		{
			plugin.notifyFailure(VARDORVIS, "You were hit by an axe.");
		}

		if (message.getMessage().contains(VARDORVIS_CAPTCHA_MESSAGE))
		{
			plugin.notifyFailure(VARDORVIS, "You failed to complete the captcha.");
		}
	}
}

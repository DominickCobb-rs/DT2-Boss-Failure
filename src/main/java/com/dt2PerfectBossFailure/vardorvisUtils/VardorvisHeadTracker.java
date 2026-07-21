package com.dt2PerfectBossFailure.vardorvisUtils;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class VardorvisHeadTracker
{
	public static final int HEAD_ID = 12226;
	private static final int MAGIC_PROJECTILE = 2520;
	private static final int RANGE_PROJECTILE = 2521;

	public enum HeadAttackStyle
	{
		MAGE,
		RANGE
	}

	@Inject
	private Client client;

	// NPC index -> attack style, tagged when the head launches its projectile
	@Getter
	private final Map<Integer, HeadAttackStyle> headStyles = new HashMap<>();

	@Subscribe
	public void onProjectileMoved(ProjectileMoved event)
	{
		Projectile projectile = event.getProjectile();

		HeadAttackStyle style;
		if (projectile.getId() == MAGIC_PROJECTILE)
		{
			style = HeadAttackStyle.MAGE;
		}
		else if (projectile.getId() == RANGE_PROJECTILE)
		{
			style = HeadAttackStyle.RANGE;
		}
		else
		{
			return;
		}

		NPC head = resolveSourceHead(projectile);
		if (head != null)
		{
			headStyles.put(head.getIndex(), style);
		}
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		if (event.getNpc().getId() == HEAD_ID)
		{
			headStyles.remove(event.getNpc().getIndex());
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING
			|| event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING)
		{
			headStyles.clear();
		}
	}

	private NPC resolveSourceHead(Projectile projectile)
	{
		Actor source = projectile.getSourceActor();
		if (source instanceof NPC && ((NPC) source).getId() == HEAD_ID)
		{
			return (NPC) source;
		}

		// Fall back to the head closest to the projectile's origin. Compare in
		// local scene coordinates, which are consistent regardless of
		// instancing (the projectile's source WorldPoint and the NPC's
		// WorldPoint can be reported in different coordinate spaces).
		int sourceX = projectile.getX1();
		int sourceY = projectile.getY1();
		NPC closest = null;
		long closestDistance = Long.MAX_VALUE;
		for (NPC npc : client.getNpcs())
		{
			if (npc.getId() != HEAD_ID)
			{
				continue;
			}
			LocalPoint lp = npc.getLocalLocation();
			if (lp == null)
			{
				continue;
			}
			long dx = lp.getX() - sourceX;
			long dy = lp.getY() - sourceY;
			long distance = dx * dx + dy * dy;
			if (distance < closestDistance)
			{
				closestDistance = distance;
				closest = npc;
			}
		}
		return closest;
	}
}

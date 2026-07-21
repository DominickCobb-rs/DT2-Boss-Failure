package com.dt2PerfectBossFailure.vardorvisUtils;

import com.dt2PerfectBossFailure.dt2pbfConfig;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.NPC;
import net.runelite.api.Renderable;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.NpcChanged;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.callback.Hooks;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;

@Slf4j
public class VardorvisAxeHider
{
	public static final int AXE_STATIC = 12225;
	public static final int AXE_MOVING = 12227;

	private static final int VARDORVIS_REGION_ID = 4405;
	// Arena center in region-local coordinates, derived from the pillar object
	// placements which sit symmetrically on the wall ring (x 36-46, y 21-31).
	// The playable 9x9 spans 37-45 x 22-30; axe spawns lie exactly on the
	// cardinal axes and diagonals of this tile.
	private static final int ARENA_CENTER_X = 41;
	private static final int ARENA_CENTER_Y = 26;

	public enum AxeSpawnDirection
	{
		NORTH,
		NORTH_EAST,
		EAST,
		SOUTH_EAST,
		SOUTH,
		SOUTH_WEST,
		WEST,
		NORTH_WEST
	}

	@Inject
	private Client client;

	@Inject
	private dt2pbfConfig config;

	@Inject
	private Hooks hooks;

	private final Hooks.RenderableDrawListener drawListener = this::shouldDraw;

	// NPC index -> spawn direction, classified when the axe first spawns
	private final Map<Integer, AxeSpawnDirection> axeDirections = new HashMap<>();

	// Config cache: shouldDraw runs for every renderable on every frame, so
	// read config once per change instead of through the proxy per call
	private dt2pbfConfig.AxeType hideType;
	private final boolean[] hiddenDirections = new boolean[AxeSpawnDirection.values().length];

	public void startUp()
	{
		updateConfigCache();
		hooks.registerRenderableDrawListener(drawListener);
	}

	public void shutDown()
	{
		hooks.unregisterRenderableDrawListener(drawListener);
		axeDirections.clear();
	}

	@Subscribe
	public void onNpcSpawned(NpcSpawned event)
	{
		trackAxe(event.getNpc());
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event)
	{
		trackAxe(event.getNpc());
	}

	@Subscribe
	public void onNpcDespawned(NpcDespawned event)
	{
		axeDirections.remove(event.getNpc().getIndex());
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING
			|| event.getGameState() == GameState.LOGIN_SCREEN
			|| event.getGameState() == GameState.HOPPING)
		{
			axeDirections.clear();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equalsIgnoreCase(dt2pbfConfig.DT2_UTILITIES_CONFIG_GROUP))
		{
			updateConfigCache();
		}
	}

	private void updateConfigCache()
	{
		hideType = config.axeHideType();
		hiddenDirections[AxeSpawnDirection.NORTH.ordinal()] = config.hideAxeNorth();
		hiddenDirections[AxeSpawnDirection.NORTH_EAST.ordinal()] = config.hideAxeNorthEast();
		hiddenDirections[AxeSpawnDirection.EAST.ordinal()] = config.hideAxeEast();
		hiddenDirections[AxeSpawnDirection.SOUTH_EAST.ordinal()] = config.hideAxeSouthEast();
		hiddenDirections[AxeSpawnDirection.SOUTH.ordinal()] = config.hideAxeSouth();
		hiddenDirections[AxeSpawnDirection.SOUTH_WEST.ordinal()] = config.hideAxeSouthWest();
		hiddenDirections[AxeSpawnDirection.WEST.ordinal()] = config.hideAxeWest();
		hiddenDirections[AxeSpawnDirection.NORTH_WEST.ordinal()] = config.hideAxeNorthWest();
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		if (!config.muteHiddenAxeSounds())
		{
			return;
		}
		Actor source = event.getSource();
		if (source instanceof NPC && isHidden((NPC) source))
		{
			event.consume();
		}
	}

	private void trackAxe(NPC npc)
	{
		int id = npc.getId();
		if (id != AXE_STATIC && id != AXE_MOVING)
		{
			return;
		}
		if (axeDirections.containsKey(npc.getIndex()))
		{
			return;
		}
		AxeSpawnDirection direction = classify(npc);
		if (direction != null)
		{
			axeDirections.put(npc.getIndex(), direction);
		}
	}

	private AxeSpawnDirection classify(NPC axe)
	{
		// Template coordinates so this works in the awakened instance too
		WorldPoint axePoint = WorldPoint.fromLocalInstance(client, axe.getLocalLocation());
		if (axePoint == null || axePoint.getRegionID() != VARDORVIS_REGION_ID)
		{
			return null;
		}
		int dx = axePoint.getRegionX() - ARENA_CENTER_X;
		int dy = axePoint.getRegionY() - ARENA_CENTER_Y;
		if (dx == 0 && dy == 0)
		{
			return null;
		}
		// 0 = east, 90 = north; bucket into 45 degree octants
		double angle = Math.toDegrees(Math.atan2(dy, dx));
		int octant = (int) Math.floor(((angle + 360 + 22.5) % 360) / 45);
		switch (octant)
		{
			case 0:
				return AxeSpawnDirection.EAST;
			case 1:
				return AxeSpawnDirection.NORTH_EAST;
			case 2:
				return AxeSpawnDirection.NORTH;
			case 3:
				return AxeSpawnDirection.NORTH_WEST;
			case 4:
				return AxeSpawnDirection.WEST;
			case 5:
				return AxeSpawnDirection.SOUTH_WEST;
			case 6:
				return AxeSpawnDirection.SOUTH;
			default:
				return AxeSpawnDirection.SOUTH_EAST;
		}
	}

	private boolean shouldDraw(Renderable renderable, boolean drawingUI)
	{
		return !(renderable instanceof NPC) || !isHidden((NPC) renderable);
	}

	public boolean isHidden(NPC npc)
	{
		int id = npc.getId();
		if (id != AXE_STATIC && id != AXE_MOVING)
		{
			return false;
		}
		if (id == AXE_STATIC && hideType == dt2pbfConfig.AxeType.Moving)
		{
			return false;
		}
		if (id == AXE_MOVING && hideType == dt2pbfConfig.AxeType.Static)
		{
			return false;
		}
		AxeSpawnDirection direction = axeDirections.get(npc.getIndex());
		return direction != null && hiddenDirections[direction.ordinal()];
	}
}

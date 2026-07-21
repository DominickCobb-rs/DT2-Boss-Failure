package com.dt2PerfectBossFailure.vardorvisUtils;

import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.dt2PerfectBossFailure.dt2pbfPlugin;
import com.dt2PerfectBossFailure.overlayTypes;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

@Slf4j
public class VardorvisHeadOverlay extends Overlay
{
	private final Client client;
	private final dt2pbfConfig config;
	private final dt2pbfPlugin plugin;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	private VardorvisHeadOverlay(Client client, dt2pbfPlugin plugin, dt2pbfConfig config, ModelOutlineRenderer modelOutlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (config.highlightMageHead() == overlayTypes.NONE && config.highlightRangeHead() == overlayTypes.NONE)
		{
			return null;
		}
		if (plugin.vardorvisHeadTracker.getHeadStyles().isEmpty())
		{
			return null;
		}

		Stroke stroke = new BasicStroke((float) config.headHighlightWidth());
		for (NPC npc : client.getNpcs())
		{
			if (npc.getId() != VardorvisHeadTracker.HEAD_ID)
			{
				continue;
			}
			VardorvisHeadTracker.HeadAttackStyle style = plugin.vardorvisHeadTracker.getHeadStyles().get(npc.getIndex());
			if (style == null)
			{
				continue;
			}

			overlayTypes renderType;
			Color color;
			Color fillColor;
			if (style == VardorvisHeadTracker.HeadAttackStyle.MAGE)
			{
				renderType = config.highlightMageHead();
				color = config.mageHeadColor();
				fillColor = config.mageHeadFillColor();
			}
			else
			{
				renderType = config.highlightRangeHead();
				color = config.rangeHeadColor();
				fillColor = config.rangeHeadFillColor();
			}
			if (renderType == overlayTypes.NONE)
			{
				continue;
			}
			switch (renderType)
			{
				case HULL:
					Shape hull = npc.getConvexHull();
					if (hull != null)
					{
						OverlayUtil.renderPolygon(graphics, hull, color, fillColor, stroke);
					}
					break;
				case TILE:
					Polygon poly = npc.getCanvasTilePoly();
					if (poly != null)
					{
						OverlayUtil.renderPolygon(graphics, poly, color, fillColor, stroke);
					}
					break;
				case TRUE_TILE:
					NPCComposition npcComposition = npc.getTransformedComposition();
					int size = npcComposition != null ? npcComposition.getSize() : 1;
					LocalPoint lp = LocalPoint.fromWorld(client, npc.getWorldLocation());
					if (lp != null)
					{
						lp = new LocalPoint(lp.getX() + size * 128 / 2 - 64, lp.getY() + size * 128 / 2 - 64);
						Polygon tile = Perspective.getCanvasTileAreaPoly(client, lp, size);
						if (tile != null)
						{
							OverlayUtil.renderPolygon(graphics, tile, color, fillColor, stroke);
						}
					}
					break;
				case OUTLINE:
					modelOutlineRenderer.drawOutline(npc, (int) config.headHighlightWidth(), color, config.feather());
					break;
			}
		}
		return null;
	}
}

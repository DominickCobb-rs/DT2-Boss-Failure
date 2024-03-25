/*
 * Copyright (c) 2022, Buchus <http://github.com/MoreBuchus>
 * Copyright (c) 2023, geheur <http://github.com/geheur>
 * Copyright (c) 2021, LeikvollE <http://github.com/LeikvollE>
 * Copyright (c) 2024, DominickCobb-rs <http://github.com/DominickCobb-rs>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.dt2PerfectBossFailure;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import lombok.extern.slf4j.Slf4j;
import javax.inject.Inject;
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
import org.apache.commons.lang3.ArrayUtils;

@Slf4j
public class dt2pbfBossOverlay extends Overlay {
	@Inject
	private Client client;

	@Inject
	private dt2pbfPlugin plugin;

	@Inject
	private dt2pbfConfig config;

	@Inject
	private ModelOutlineRenderer modelOutlineRenderer;

	public dt2pbfBossOverlay()
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Stroke stroke = new BasicStroke((float) config.borderWidth());
		for (dt2boss boss : dt2boss.values())
		{
			if (boss.render()==overlayTypes.NONE)
			{
				continue;
			}
			for (NPC npc : client.getNpcs())
			{
				if (ArrayUtils.contains(boss.getIds(), npc.getId()))
				{
					NPCComposition npcComposition = npc.getTransformedComposition();
					int size = npcComposition.getSize();
					Color color = plugin.notified ? boss.getFailureColor() : boss.getPerfectColor();
					int alpha = (int) (color.getAlpha() * 0.3);
					Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
					switch (boss.render())
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
						// Snippets from the BetterNPCHighlightOverlay.java in both of the following
						case TRUE_TILE:
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
							modelOutlineRenderer.drawOutline(npc,(int)config.borderWidth(),color,config.feather());
							break;

					}
				}
			}
		}
		return null;
	}
}

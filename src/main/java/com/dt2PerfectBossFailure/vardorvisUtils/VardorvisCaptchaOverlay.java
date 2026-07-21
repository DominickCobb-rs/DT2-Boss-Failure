package com.dt2PerfectBossFailure.vardorvisUtils;

import com.dt2PerfectBossFailure.dt2pbfConfig;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
public class VardorvisCaptchaOverlay extends Overlay
{
	// VARDORVIS_QTE interface (group 833). The clickable spores are the
	// QTE_MODEL_1..QTE_MODEL_6 children (indices 6-11).
	private static final int QTE_GROUP = 833;
	private static final int FIRST_SPORE_CHILD = 6;
	private static final int LAST_SPORE_CHILD = 11;
	private static final int ARC = 8;

	private final Client client;
	private final dt2pbfConfig config;

	@Inject
	private VardorvisCaptchaOverlay(Client client, dt2pbfConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.captchaHoverHighlight() && !config.captchaHighlightAll())
		{
			return null;
		}

		// Bail early if the captcha interface isn't open
		Widget root = client.getWidget((QTE_GROUP << 16));
		if (root == null || root.isHidden())
		{
			return null;
		}

		Point mouse = client.getMouseCanvasPosition();
		Stroke hoverStroke = new BasicStroke((float) config.captchaHoverWidth());
		Stroke allStroke = new BasicStroke(1f);

		for (int child = FIRST_SPORE_CHILD; child <= LAST_SPORE_CHILD; child++)
		{
			Widget spore = client.getWidget((QTE_GROUP << 16) | child);
			if (spore == null || spore.isHidden())
			{
				continue;
			}
			Rectangle bounds = spore.getBounds();
			if (bounds == null || bounds.width <= 0 || bounds.height <= 0)
			{
				continue;
			}

			boolean hovered = config.captchaHoverHighlight()
				&& mouse != null && mouse.getX() >= 0
				&& bounds.contains(mouse.getX(), mouse.getY());

			if (hovered)
			{
				Color color = config.captchaHoverColor();
				int alpha = (int) (color.getAlpha() * 0.5);
				Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
				graphics.setColor(fill);
				graphics.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, ARC, ARC);
				graphics.setStroke(hoverStroke);
				graphics.setColor(color);
				graphics.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, ARC, ARC);
			}
			else if (config.captchaHighlightAll())
			{
				graphics.setStroke(allStroke);
				graphics.setColor(config.captchaAllColor());
				graphics.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, ARC, ARC);
			}
		}

		return null;
	}
}

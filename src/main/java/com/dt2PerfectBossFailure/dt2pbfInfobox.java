package com.dt2PerfectBossFailure;

import java.awt.Color;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

public class dt2pbfInfobox extends InfoBox
{
	private final dt2pbfPlugin plugin;

	@Inject
	public dt2pbfInfobox(dt2pbfPlugin plugin)
	{
		super(null, plugin);
		this.plugin = plugin;
		setPriority(InfoBoxPriority.MED);
	}

	@Override
	public String getText()
	{
		return "";
	}
	@Override
	public Color getTextColor()
	{
		return Color.WHITE;
	}

	@Override
	public String getTooltip()
	{
		return plugin.initialReason;
	}

}

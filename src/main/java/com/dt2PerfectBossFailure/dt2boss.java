package com.dt2PerfectBossFailure;

import static com.dt2PerfectBossFailure.bossFailure.Duke.DUKE_IDS;
import static com.dt2PerfectBossFailure.bossFailure.Leviathan.LEVIATHAN_IDS;
import static com.dt2PerfectBossFailure.bossFailure.Vardorvis.VARDORVIS_IDS;
import static com.dt2PerfectBossFailure.bossFailure.Whisperer.WHISPERER_IDS;
import java.awt.Color;

public enum dt2boss
{
	DUKE(DUKE_IDS,Color.WHITE,Color.WHITE,overlayTypes.NONE),
	WHISPERER(WHISPERER_IDS,Color.WHITE,Color.WHITE,overlayTypes.NONE),
	VARDORVIS(VARDORVIS_IDS,Color.WHITE,Color.WHITE,overlayTypes.NONE),
	LEVIATHAN(LEVIATHAN_IDS,Color.WHITE,Color.WHITE,overlayTypes.NONE);
	public int[] ids;
	public Color perfectColor;
	public Color failureColor;
	public overlayTypes render;

	dt2boss(int[] boss_ids, Color color1, Color color2, overlayTypes render)
	{
		this.ids = boss_ids;
		this.perfectColor = color1;
		this.failureColor = color2;
		this.render = render;
	}

	public void initialize(Color perfectColor, Color failureColor, overlayTypes render)
	{
		this.perfectColor=perfectColor;
		this.failureColor=failureColor;
		this.render=render;
	}

	public int[] getIds() {
		return ids;
	}
	public Color getPerfectColor() {
		return perfectColor;
	}

	public void setPerfectColor(Color perfectColor) {
		this.perfectColor = perfectColor;
	}

	public Color getFailureColor() {
		return failureColor;
	}

	public void setFailureColor(Color failureColor) {
		this.failureColor = failureColor;
	}

	public overlayTypes render()
	{
		return this.render;
	}

	public void setRender(overlayTypes render)
	{
		this.render = render;
	}
}


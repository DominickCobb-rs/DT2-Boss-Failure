package com.dt2PerfectBossFailure;

import net.runelite.api.Projectile;

public class LeviathanProjectile
{
	private final int type;
	private final int cycle;

	public LeviathanProjectile(Projectile projectile)
	{
		this.type=projectile.getId();
		this.cycle=projectile.getEndCycle();
	}

	public int getCycle()
	{
		return this.cycle;
	}
	public int getType()
	{
		return this.type;
	}
}

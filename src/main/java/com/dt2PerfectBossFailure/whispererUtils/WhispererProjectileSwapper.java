/*
	BSD 2-Clause License

	Copyright (c) 2023, InfernoStats

	Redistribution and use in source and binary forms, with or without
	modification, are permitted provided that the following conditions are met:

	1. Redistributions of source code must retain the above copyright notice, this
	   list of conditions and the following disclaimer.

	2. Redistributions in binary form must reproduce the above copyright notice,
	   this list of conditions and the following disclaimer in the documentation
	   and/or other materials provided with the distribution.

	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
	AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
	IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
	DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
	FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
	DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
	SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
	CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
	OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
	OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.dt2PerfectBossFailure.whispererUtils;

import com.dt2PerfectBossFailure.dt2pbfConfig;
import com.dt2PerfectBossFailure.dt2pbfPlugin;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Projectile;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;

@Slf4j
public class WhispererProjectileSwapper
{
	// Vardorvis' Head Projectile IDs
	private static final int MAGIC_PROJECTILE = 2445;
	private static final int RANGE_PROJECTILE = 2444;

	private final Client client;
	private final dt2pbfConfig config;
	private final dt2pbfPlugin plugin;

	@Inject
	private WhispererProjectileSwapper(Client client, dt2pbfPlugin plugin, dt2pbfConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
	}

	@Subscribe
	public void onProjectileMoved(ProjectileMoved projectileMoved)
	{
		if(config.whispererProjectileStyle()== dt2pbfConfig.WhispererStyle.Default)
			return;
		Projectile projectile = projectileMoved.getProjectile();
		if (projectile.getId() == RANGE_PROJECTILE)
		{
			replaceProjectile(projectile, config.whispererProjectileStyle().getRange());
		}
		else if (projectile.getId() == MAGIC_PROJECTILE)
		{
			replaceProjectile(projectile, config.whispererProjectileStyle().getMagic());
		}
	}

	private void replaceProjectile(Projectile projectile, int projectileId)
	{
		Projectile p = client.createProjectile(projectileId,
			projectile.getFloor(),
			projectile.getX1(), projectile.getY1(),
			projectile.getHeight(),
			projectile.getStartCycle(), projectile.getEndCycle(),
			projectile.getSlope(),
			projectile.getStartHeight(), projectile.getEndHeight(),
			projectile.getInteracting(),
			projectile.getTarget().getX(), projectile.getTarget().getY());

		client.getProjectiles().addLast(p);
		projectile.setEndCycle(0);
	}
}

package com.dt2PerfectBossFailure;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class dt2pbfTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(dt2pbfPlugin.class);
		RuneLite.main(args);
	}
}
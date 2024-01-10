package com.dt2PerfectBossFailure;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class dt2pbfTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(d2tpbfPlugin.class);
		RuneLite.main(args);
	}
}
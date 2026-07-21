/*
	BSD 2-Clause License

	Copyright (c) 2024, zom
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
package com.dt2PerfectBossFailure;

import java.awt.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;


@ConfigGroup(dt2pbfConfig.DT2_UTILITIES_CONFIG_GROUP)
public interface dt2pbfConfig extends net.runelite.client.config.Config
{
	String DT2_UTILITIES_CONFIG_GROUP = "dt2perfectBossNotifier";

	// Perfect Kills section: alerts, sound, and boss highlights
	@ConfigSection(
		name="Perfect Kills",
		description="Alerts, sound, and boss highlights for perfect kill tracking",
		position=0,
		closedByDefault = true
	)
	String perfectKills = "perfectKills";

	@ConfigItem(
		position = 0,
		keyName = "infobox",
		name = "Display infobox",
		description = "Show an infobox with the current kill's perfection status",
		section = perfectKills
	)
	default boolean infobox()
	{
		return true;
	}

	@ConfigItem(
		position = 1,
		keyName = "chatMessage",
		name = "Chat messages",
		description = "Show a chat message when you fail a perfect kill",
		section = perfectKills
	)
	default boolean chatbox()
	{
		return true;
	}

	@ConfigItem(
		position = 2,
		keyName = "notifyRepeatedly",
		name = "Notify repeat failure",
		description = "Show the failure message and play the sound for every mistake, instead of only the first",
		section = perfectKills
	)
	default boolean notifyRepeatedly()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "audiblyNotify",
		name = "Play sound",
		description = "Play a sound when you fail a perfect kill",
		section = perfectKills
	)
	default boolean audiblyNotify()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "soundSelection",
		name = "Notification Sound",
		description = "The RuneScape sound effect ID to play on failure",
		section = perfectKills
	)
	default int soundSelection()
	{
		return 1043;
	}

	@ConfigItem(
		position = 5,
		keyName = "highlightWidth",
		name = "Highlight width",
		description = "Width of the boss highlight border",
		section = perfectKills
	)
	default double borderWidth() {return 2;}

	@ConfigItem(
		position = 6,
		keyName = "feather",
		name = "Outline feather",
		description = "Feathering of the outline highlight edge",
		section = perfectKills
	)
	default int feather() {return 2;}

	@ConfigItem(
		position = 7,
		keyName = "highlightDuke",
		name = "Duke",
		description = "Highlight Duke with your kill status",
		section = perfectKills
	)
	default overlayTypes highlightDuke() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 8,
		keyName = "dukePerfectHighlight",
		name = "Duke perfect color",
		description = "The color Duke is highlighted while the kill is perfect",
		section = perfectKills
	)
	default Color dukePerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 9,
		keyName = "dukeFailureHighlight",
		name = "Duke failure color",
		description = "The color Duke is highlighted after a mistake",
		section = perfectKills
	)
	default Color dukeFailure() {return Color.red;}

	@ConfigItem(
		position = 10,
		keyName = "highlightWhisperer",
		name = "Whisperer",
		description = "Highlight the Whisperer with your kill status",
		section = perfectKills
	)
	default overlayTypes highlightWhisperer() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 11,
		keyName = "whispererPerfectHighlight",
		name = "Whisperer perfect color",
		description = "The color the Whisperer is highlighted while the kill is perfect",
		section = perfectKills
	)
	default Color whispererPerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 12,
		keyName = "whispererFailureHighlight",
		name = "Whisperer failure color",
		description = "The color the Whisperer is highlighted after a mistake",
		section = perfectKills
	)
	default Color whispererFailure() {return Color.red;}

	@ConfigItem(
		position = 13,
		keyName = "highlightVardorvis",
		name = "Vardorvis",
		description = "Highlight Vardorvis with your kill status",
		section = perfectKills
	)
	default overlayTypes highlightVardorvis() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 14,
		keyName = "vardorvisPerfectHighlight",
		name = "Vardorvis perfect color",
		description = "The color Vardorvis is highlighted while the kill is perfect",
		section = perfectKills
	)
	default Color vardorvisPerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 15,
		keyName = "vardorvisFailureHighlight",
		name = "Vardorvis failure color",
		description = "The color Vardorvis is highlighted after a mistake",
		section = perfectKills
	)
	default Color vardorvisFailure() {return Color.red;}

	@ConfigItem(
		position = 16,
		keyName = "highlightLeviathan",
		name = "Leviathan",
		description = "Highlight the Leviathan with your kill status",
		section = perfectKills
	)
	default overlayTypes highlightLeviathan() {
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 17,
		keyName = "leviathanPerfectHighlight",
		name = "Leviathan perfect color",
		description = "The color the Leviathan is highlighted while the kill is perfect",
		section = perfectKills
	)
	default Color leviathanPerfect() {return Color.green;}

	@Alpha
	@ConfigItem(
		position = 18,
		keyName = "leviathanFailureHighlight",
		name = "Leviathan failure color",
		description = "The color the Leviathan is highlighted after a mistake",
		section = perfectKills
	)
	default Color leviathanFailure() {return Color.red;}

	// Vardorvis pillars
	@ConfigSection(
		name="Vardorvis pillars",
		description="Show or hide pillars in Vardorvis' arena",
		position=1,
		closedByDefault = true
	)
	String vardorvisPillars = "vardorvisPillars";

	@ConfigItem(
		position = 1,
		keyName = "hidePillars",
		name = "Hide pillars",
		description = "Hide the pillars in Vardorvis' arena",
		section = vardorvisPillars
	)
	default boolean hidePillars()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "drawPillarLocation",
		name = "Draw pillars",
		description = "Display the pillar locations where you can't click",
		section = vardorvisPillars
	)
	default boolean drawPillarLocation()
	{
		return true;
	}

	@ConfigItem(
		position = 3,
		keyName = "borderColor",
		name = "Border color",
		description = "Border color of the tiles marking where the pillars stood",
		section = vardorvisPillars
	)
	default Color borderColor()
	{
		return new Color(0x64FFFF00, true);
	}

	@ConfigItem(
		position = 4,
		keyName = "borderWidth",
		name = "Border Width",
		description = "Width of the marked tile border",
		section = vardorvisPillars
	)
	default double pillarBorderWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 5,
		keyName = "fillOpacity",
		name = "Fill Opacity",
		description = "Opacity of the tile fill color",
		section = vardorvisPillars
	)
	@Range(
		max = 255
	)
	default int fillOpacity()
	{
		return 50;
	}

	// Vardorvis axes
	@ConfigSection(
		name="Vardorvis axes",
		description="Hide specific axe spawns in Vardorvis' arena to declutter the fight",
		position=2,
		closedByDefault = true
	)
	String vardorvisAxes = "vardorvisAxes";

	enum AxeType
	{
		Static,
		Moving,
		Both
	}

	@ConfigItem(
		position = 0,
		keyName = "axeHideType",
		name = "Axes to hide",
		description = "Hide static axes, moving axes, or both, for the spawns selected below",
		section = vardorvisAxes
	)
	default AxeType axeHideType()
	{
		return AxeType.Both;
	}

	@ConfigItem(
		position = 1,
		keyName = "hideAxeNorth",
		name = "Hide north axe",
		description = "Hide axes from the north spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeNorth()
	{
		return false;
	}

	@ConfigItem(
		position = 2,
		keyName = "hideAxeNorthEast",
		name = "Hide north-east axe",
		description = "Hide axes from the north-east spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeNorthEast()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "hideAxeEast",
		name = "Hide east axe",
		description = "Hide axes from the east spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeEast()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "hideAxeSouthEast",
		name = "Hide south-east axe",
		description = "Hide axes from the south-east spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeSouthEast()
	{
		return false;
	}

	@ConfigItem(
		position = 5,
		keyName = "hideAxeSouth",
		name = "Hide south axe",
		description = "Hide axes from the south spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeSouth()
	{
		return false;
	}

	@ConfigItem(
		position = 6,
		keyName = "hideAxeSouthWest",
		name = "Hide south-west axe",
		description = "Hide axes from the south-west spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeSouthWest()
	{
		return false;
	}

	@ConfigItem(
		position = 7,
		keyName = "hideAxeWest",
		name = "Hide west axe",
		description = "Hide axes from the west spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeWest()
	{
		return false;
	}

	@ConfigItem(
		position = 8,
		keyName = "hideAxeNorthWest",
		name = "Hide north-west axe",
		description = "Hide axes from the north-west spawn",
		section = vardorvisAxes
	)
	default boolean hideAxeNorthWest()
	{
		return false;
	}

	@ConfigItem(
		position = 9,
		keyName = "muteHiddenAxeSounds",
		name = "Mute hidden axe sounds",
		description = "Silence the sound effects of axes that are hidden",
		section = vardorvisAxes
	)
	default boolean muteHiddenAxeSounds()
	{
		return false;
	}

	@ConfigItem(
		position = 10,
		keyName = "axeHighlight",
		name = "Highlight axes",
		description = "Highlight axes; axes you hide are skipped",
		section = vardorvisAxes
	)
	default overlayTypes axeHighlight()
	{
		return overlayTypes.NONE;
	}

	@ConfigItem(
		position = 11,
		keyName = "axeHighlightType",
		name = "Axes to highlight",
		description = "Highlight static axes, moving axes, or both",
		section = vardorvisAxes
	)
	default AxeType axeHighlightType()
	{
		return AxeType.Both;
	}

	@Alpha
	@ConfigItem(
		position = 12,
		keyName = "axeHighlightColor",
		name = "Axe border color",
		description = "The outline color of the axe highlight",
		section = vardorvisAxes
	)
	default Color axeHighlightColor()
	{
		return Color.RED;
	}

	@Alpha
	@ConfigItem(
		position = 13,
		keyName = "axeFillColor",
		name = "Axe fill color",
		description = "The fill color of the axe highlight; set alpha to 0 for outline only",
		section = vardorvisAxes
	)
	default Color axeFillColor()
	{
		return new Color(255, 0, 0, 0);
	}

	@ConfigItem(
		position = 14,
		keyName = "axeHighlightWidth",
		name = "Axe highlight width",
		description = "Width of the axe highlight border",
		section = vardorvisAxes
	)
	default double axeHighlightWidth()
	{
		return 2;
	}

	// Vardorvis heads
	@ConfigSection(
		name="Vardorvis heads",
		description="Highlight Vardorvis' heads by attack style (mage/range)",
		position=3,
		closedByDefault = true
	)
	String vardorvisHeads = "vardorvisHeads";

	@ConfigItem(
		position = 0,
		keyName = "highlightMageHead",
		name = "Mage head",
		description = "Highlight the head throwing a mage attack",
		section = vardorvisHeads
	)
	default overlayTypes highlightMageHead()
	{
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 1,
		keyName = "mageHeadColor",
		name = "Mage border color",
		description = "The outline color of the mage head highlight",
		section = vardorvisHeads
	)
	default Color mageHeadColor()
	{
		return new Color(160, 32, 240);
	}

	@Alpha
	@ConfigItem(
		position = 2,
		keyName = "mageHeadFillColor",
		name = "Mage fill color",
		description = "The fill color of the mage head highlight; set alpha to 0 for outline only",
		section = vardorvisHeads
	)
	default Color mageHeadFillColor()
	{
		return new Color(160, 32, 240, 50);
	}

	@ConfigItem(
		position = 3,
		keyName = "highlightRangeHead",
		name = "Range head",
		description = "Highlight the head throwing a ranged attack",
		section = vardorvisHeads
	)
	default overlayTypes highlightRangeHead()
	{
		return overlayTypes.NONE;
	}

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "rangeHeadColor",
		name = "Range border color",
		description = "The outline color of the range head highlight",
		section = vardorvisHeads
	)
	default Color rangeHeadColor()
	{
		return Color.GREEN;
	}

	@Alpha
	@ConfigItem(
		position = 5,
		keyName = "rangeHeadFillColor",
		name = "Range fill color",
		description = "The fill color of the range head highlight; set alpha to 0 for outline only",
		section = vardorvisHeads
	)
	default Color rangeHeadFillColor()
	{
		return new Color(0, 255, 0, 50);
	}

	@ConfigItem(
		position = 6,
		keyName = "headHighlightWidth",
		name = "Highlight width",
		description = "Width of the head highlight border",
		section = vardorvisHeads
	)
	default double headHighlightWidth()
	{
		return 2;
	}

	// Vardorvis captcha
	@ConfigSection(
		name="Vardorvis captcha",
		description="Feedback for the spore captcha (quick-time event)",
		position=4,
		closedByDefault = true
	)
	String vardorvisCaptcha = "vardorvisCaptcha";

	@ConfigItem(
		position = 0,
		keyName = "captchaHoverHighlight",
		name = "Highlight hovered spore",
		description = "Highlight the spore your cursor is hovering over during the captcha",
		section = vardorvisCaptcha
	)
	default boolean captchaHoverHighlight()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		position = 1,
		keyName = "captchaHoverColor",
		name = "Hover color",
		description = "Color of the highlight on the spore under your cursor",
		section = vardorvisCaptcha
	)
	default Color captchaHoverColor()
	{
		return new Color(255, 255, 0, 130);
	}

	@ConfigItem(
		position = 2,
		keyName = "captchaHoverWidth",
		name = "Border width",
		description = "Width of the hover highlight border",
		section = vardorvisCaptcha
	)
	default double captchaHoverWidth()
	{
		return 2;
	}

	@ConfigItem(
		position = 3,
		keyName = "captchaHighlightAll",
		name = "Outline all spores",
		description = "Draw an outline around every spore",
		section = vardorvisCaptcha
	)
	default boolean captchaHighlightAll()
	{
		return false;
	}

	@Alpha
	@ConfigItem(
		position = 4,
		keyName = "captchaAllColor",
		name = "Outline color",
		description = "Color of the outline drawn around every spore",
		section = vardorvisCaptcha
	)
	default Color captchaAllColor()
	{
		return new Color(255, 255, 255, 90);
	}

	@ConfigSection(
		name="Projectile Swaps",
		description="Swap projectiles per boss",
		position=5,
		closedByDefault = true
	)
	String projectileSwaps = "projectileSwaps";

	// Prayer-disable projectile styles, as (range, magic) spot-anim id pairs.
	// A boss set to its own style is left untouched; every other entry is a
	// real range/magic dual attack from another boss. IDs cross-checked
	// against the Projectile Override plugin (Loze-Put/projectile-override).
	@Getter
	@AllArgsConstructor
	enum ProjectileStyle
	{
		Akkha("Akkha", 2255, 2253),
		Cerberus("Cerberus", 1245, 1242),
		CorruptedHunllef("Corrupted Hunllef", 1712, 1708),
		DemonicGorilla("Demonic Gorilla", 1302, 1304),
		Doom("Doom of Mokhaiotl", 3380, 3379),
		Hueycoatl("Hueycoatl", 2972, 2975),
		Hunllef("Hunllef", 1711, 1707),
		Hydra("Hydra", 1663, 1662),
		Inferno("Jal-Ak (Inferno blob)", 1378, 1380),
		KalphiteQueen("Kalphite Queen", 288, 280),
		KreeArra("Kree'arra", 1199, 1200),
		Leviathan("Leviathan", 2487, 2489),
		Manticore("Manticore", 2683, 2681),
		Nightmare("Nightmare", 1766, 1764),
		CoX("Olm spheres", 1343, 1341),
		Muspah("Phantom Muspah", 2329, 2327),
		Scurrius("Scurrius", 2642, 2640),
		ToB("Sotetseg", 1607, 1606),
		TormentedDemon("Tormented Demon", 2857, 2853),
		Vardorvis("Vardorvis", 2521, 2520),
		ToA("Wardens (phase 2)", 2241, 2224),
		WardensDivine("Wardens (phase 3)", 2206, 2208),
		Whisperer("Whisperer", 2444, 2445),
		Zulrah("Zulrah", 1044, 1046);

		private final String displayName;
		private final int range;
		private final int magic;

		@Override
		public String toString()
		{
			return displayName;
		}
	}

	@ConfigItem(
		keyName = "whispererProjectileStyle",
		name = "Whisperer projectiles",
		description = "The projectile style to replace the Whisperer's prayer-disable attack with",
		section = projectileSwaps,
		position = 0
	)
	default ProjectileStyle whispererProjectileStyle()
	{
		return ProjectileStyle.Whisperer;
	}

	@ConfigItem(
		keyName = "vardorvisProjectileStyle",
		name = "Vardorvis projectiles",
		description = "The projectile style to replace Vardorvis' head prayer-disable attack with",
		section = projectileSwaps,
		position = 1
	)
	default ProjectileStyle vardorvisProjectileStyle()
	{
		return ProjectileStyle.Vardorvis;
	}
}

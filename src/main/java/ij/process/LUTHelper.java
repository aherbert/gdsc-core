package ij.process;

import java.awt.Color;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2016 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

import ij.process.LUT;

/**
 * Contains functions for ImageJ LUTs
 */
public class LUTHelper
{
	public enum LutColour
	{
		//@formatter:off
		RED_HOT{ public String getName() { return "Red-Hot"; }}, 
		ICE{ public String getName() { return "Ice";}}, 
		RAINBOW{ public String getName() { return "Rainbow"; }}, 
		FIRE{ public String getName() { return "Fire"; }}, 
		FIRE_LIGHT{ public String getName() { return "FireLight"; }}, 
		FIRE_GLOW{ public String getName() { return "FireGlow"; }}, 
		RED_YELLOW{ public String getName() { return "Red-Yellow"; }}, 
		RED{ public String getName() { return "Red"; }},
		GREEN{ public String getName() { return "Green"; }}, 
		BLUE{ public String getName() { return "Blue"; }}, 
		CYAN{ public String getName() { return "Cyan"; }}, 
		MAGENTA{ public String getName() { return "Magenta"; }}, 
		YELLOW{ public String getName() { return "Yellow"; }},
		RED_BLUE{ public String getName() { return "Red-Blue"; }}, 
		RED_CYAN{ public String getName() { return "Red-Cyan"; }};
		//@formatter:on

		@Override
		public String toString()
		{
			return getName();
		}

		/**
		 * Gets the name.
		 *
		 * @return the name
		 */
		abstract public String getName();
	}

	/**
	 * List of the LUT names
	 */
	public static final String[] luts;

	/** The LUT with distinct colours. */
	private static final LUT cachedLUT;

	static
	{
		LutColour[] l = LutColour.values();
		luts = new String[l.length];
		for (int i = 0; i < l.length; i++)
			luts[i] = l[i].getName();

		// Create a colour LUT so that all colours from 1-255 are distinct.
		// This was produced using http://phrogz.net/css/distinct-colors.html
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];

		int i = 1;
		rgb(255, 0, 0, reds, greens, blues, i++);
		rgb(153, 135, 115, reds, greens, blues, i++);
		rgb(76, 217, 54, reds, greens, blues, i++);
		rgb(0, 136, 255, reds, greens, blues, i++);
		rgb(129, 105, 140, reds, greens, blues, i++);
		rgb(242, 0, 0, reds, greens, blues, i++);
		rgb(255, 170, 0, reds, greens, blues, i++);
		rgb(0, 51, 0, reds, greens, blues, i++);
		rgb(0, 102, 191, reds, greens, blues, i++);
		rgb(204, 0, 255, reds, greens, blues, i++);
		rgb(166, 0, 0, reds, greens, blues, i++);
		rgb(191, 128, 0, reds, greens, blues, i++);
		rgb(127, 255, 145, reds, greens, blues, i++);
		rgb(0, 75, 140, reds, greens, blues, i++);
		rgb(184, 54, 217, reds, greens, blues, i++);
		rgb(51, 0, 0, reds, greens, blues, i++);
		rgb(115, 77, 0, reds, greens, blues, i++);
		rgb(83, 166, 94, reds, greens, blues, i++);
		rgb(0, 48, 89, reds, greens, blues, i++);
		rgb(230, 128, 255, reds, greens, blues, i++);
		rgb(255, 128, 128, reds, greens, blues, i++);
		rgb(76, 51, 0, reds, greens, blues, i++);
		rgb(38, 77, 43, reds, greens, blues, i++);
		rgb(128, 196, 255, reds, greens, blues, i++);
		rgb(46, 26, 51, reds, greens, blues, i++);
		rgb(153, 77, 77, reds, greens, blues, i++);
		rgb(51, 34, 0, reds, greens, blues, i++);
		rgb(143, 191, 150, reds, greens, blues, i++);
		rgb(191, 225, 255, reds, greens, blues, i++);
		rgb(71, 0, 77, reds, greens, blues, i++);
		rgb(76, 38, 38, reds, greens, blues, i++);
		rgb(229, 172, 57, reds, greens, blues, i++);
		rgb(0, 140, 37, reds, greens, blues, i++);
		rgb(77, 90, 102, reds, greens, blues, i++);
		rgb(133, 35, 140, reds, greens, blues, i++);
		rgb(51, 26, 26, reds, greens, blues, i++);
		rgb(153, 115, 38, reds, greens, blues, i++);
		rgb(0, 89, 24, reds, greens, blues, i++);
		rgb(0, 102, 255, reds, greens, blues, i++);
		rgb(99, 51, 102, reds, greens, blues, i++);
		rgb(255, 191, 191, reds, greens, blues, i++);
		rgb(255, 213, 128, reds, greens, blues, i++);
		rgb(0, 255, 102, reds, greens, blues, i++);
		rgb(0, 61, 153, reds, greens, blues, i++);
		rgb(188, 143, 191, reds, greens, blues, i++);
		rgb(115, 86, 86, reds, greens, blues, i++);
		rgb(178, 149, 89, reds, greens, blues, i++);
		rgb(115, 230, 161, reds, greens, blues, i++);
		rgb(0, 20, 51, reds, greens, blues, i++);
		rgb(255, 0, 238, reds, greens, blues, i++);
		rgb(64, 48, 48, reds, greens, blues, i++);
		rgb(102, 85, 51, reds, greens, blues, i++);
		rgb(191, 255, 217, reds, greens, blues, i++);
		rgb(128, 179, 255, reds, greens, blues, i++);
		rgb(255, 191, 251, reds, greens, blues, i++);
		rgb(76, 10, 0, reds, greens, blues, i++);
		rgb(204, 187, 153, reds, greens, blues, i++);
		rgb(115, 153, 130, reds, greens, blues, i++);
		rgb(83, 116, 166, reds, greens, blues, i++);
		rgb(77, 57, 75, reds, greens, blues, i++);
		rgb(255, 89, 64, reds, greens, blues, i++);
		rgb(255, 204, 0, reds, greens, blues, i++);
		rgb(77, 102, 87, reds, greens, blues, i++);
		rgb(57, 80, 115, reds, greens, blues, i++);
		rgb(179, 0, 143, reds, greens, blues, i++);
		rgb(191, 67, 48, reds, greens, blues, i++);
		rgb(166, 133, 0, reds, greens, blues, i++);
		rgb(0, 204, 109, reds, greens, blues, i++);
		rgb(153, 173, 204, reds, greens, blues, i++);
		rgb(115, 0, 92, reds, greens, blues, i++);
		rgb(115, 40, 29, reds, greens, blues, i++);
		rgb(102, 82, 0, reds, greens, blues, i++);
		rgb(0, 128, 68, reds, greens, blues, i++);
		rgb(105, 119, 140, reds, greens, blues, i++);
		rgb(64, 0, 51, reds, greens, blues, i++);
		rgb(217, 123, 108, reds, greens, blues, i++);
		rgb(217, 184, 54, reds, greens, blues, i++);
		rgb(0, 51, 27, reds, greens, blues, i++);
		rgb(0, 68, 255, reds, greens, blues, i++);
		rgb(230, 57, 195, reds, greens, blues, i++);
		rgb(115, 65, 57, reds, greens, blues, i++);
		rgb(76, 65, 19, reds, greens, blues, i++);
		rgb(26, 102, 66, reds, greens, blues, i++);
		rgb(0, 48, 179, reds, greens, blues, i++);
		rgb(255, 128, 229, reds, greens, blues, i++);
		rgb(204, 160, 153, reds, greens, blues, i++);
		rgb(204, 184, 102, reds, greens, blues, i++);
		rgb(83, 166, 127, reds, greens, blues, i++);
		rgb(0, 34, 128, reds, greens, blues, i++);
		rgb(191, 96, 172, reds, greens, blues, i++);
		rgb(217, 58, 0, reds, greens, blues, i++);
		rgb(127, 115, 64, reds, greens, blues, i++);
		rgb(153, 204, 180, reds, greens, blues, i++);
		rgb(0, 24, 89, reds, greens, blues, i++);
		rgb(77, 38, 69, reds, greens, blues, i++);
		rgb(127, 34, 0, reds, greens, blues, i++);
		rgb(255, 242, 191, reds, greens, blues, i++);
		rgb(0, 255, 170, reds, greens, blues, i++);
		rgb(51, 92, 204, reds, greens, blues, i++);
		rgb(255, 0, 170, reds, greens, blues, i++);
		rgb(89, 24, 0, reds, greens, blues, i++);
		rgb(153, 145, 115, reds, greens, blues, i++);
		rgb(0, 179, 119, reds, greens, blues, i++);
		rgb(29, 52, 115, reds, greens, blues, i++);
		rgb(153, 0, 102, reds, greens, blues, i++);
		rgb(51, 14, 0, reds, greens, blues, i++);
		rgb(102, 97, 77, reds, greens, blues, i++);
		rgb(115, 230, 191, reds, greens, blues, i++);
		rgb(19, 34, 77, reds, greens, blues, i++);
		rgb(89, 0, 60, reds, greens, blues, i++);
		rgb(255, 115, 64, reds, greens, blues, i++);
		rgb(64, 61, 48, reds, greens, blues, i++);
		rgb(0, 255, 204, reds, greens, blues, i++);
		rgb(128, 162, 255, reds, greens, blues, i++);
		rgb(153, 77, 128, reds, greens, blues, i++);
		rgb(166, 75, 41, reds, greens, blues, i++);
		rgb(255, 238, 0, reds, greens, blues, i++);
		rgb(0, 128, 102, reds, greens, blues, i++);
		rgb(32, 40, 64, reds, greens, blues, i++);
		rgb(115, 86, 105, reds, greens, blues, i++);
		rgb(255, 162, 128, reds, greens, blues, i++);
		rgb(153, 143, 0, reds, greens, blues, i++);
		rgb(0, 77, 61, reds, greens, blues, i++);
		rgb(191, 208, 255, reds, greens, blues, i++);
		rgb(255, 0, 136, reds, greens, blues, i++);
		rgb(191, 121, 96, reds, greens, blues, i++);
		rgb(115, 107, 0, reds, greens, blues, i++);
		rgb(13, 51, 43, reds, greens, blues, i++);
		rgb(57, 62, 77, reds, greens, blues, i++);
		rgb(178, 0, 95, reds, greens, blues, i++);
		rgb(140, 89, 70, reds, greens, blues, i++);
		rgb(51, 48, 13, reds, greens, blues, i++);
		rgb(57, 115, 103, reds, greens, blues, i++);
		rgb(0, 27, 204, reds, greens, blues, i++);
		rgb(127, 0, 68, reds, greens, blues, i++);
		rgb(76, 48, 38, reds, greens, blues, i++);
		rgb(255, 247, 128, reds, greens, blues, i++);
		rgb(191, 255, 242, reds, greens, blues, i++);
		rgb(0, 8, 64, reds, greens, blues, i++);
		rgb(51, 0, 27, reds, greens, blues, i++);
		rgb(255, 208, 191, reds, greens, blues, i++);
		rgb(191, 188, 143, reds, greens, blues, i++);
		rgb(57, 77, 73, reds, greens, blues, i++);
		rgb(102, 116, 204, reds, greens, blues, i++);
		rgb(217, 54, 141, reds, greens, blues, i++);
		rgb(153, 125, 115, reds, greens, blues, i++);
		rgb(238, 255, 0, reds, greens, blues, i++);
		rgb(0, 255, 238, reds, greens, blues, i++);
		rgb(143, 150, 191, reds, greens, blues, i++);
		rgb(255, 128, 196, reds, greens, blues, i++);
		rgb(255, 102, 0, reds, greens, blues, i++);
		rgb(167, 179, 0, reds, greens, blues, i++);
		rgb(0, 179, 167, reds, greens, blues, i++);
		rgb(0, 0, 255, reds, greens, blues, i++);
		rgb(115, 57, 88, reds, greens, blues, i++);
		rgb(191, 77, 0, reds, greens, blues, i++);
		rgb(59, 64, 0, reds, greens, blues, i++);
		rgb(115, 230, 222, reds, greens, blues, i++);
		rgb(0, 0, 153, reds, greens, blues, i++);
		rgb(51, 26, 39, reds, greens, blues, i++);
		rgb(140, 56, 0, reds, greens, blues, i++);
		rgb(160, 166, 83, reds, greens, blues, i++);
		rgb(77, 153, 148, reds, greens, blues, i++);
		rgb(0, 0, 140, reds, greens, blues, i++);
		rgb(255, 191, 225, reds, greens, blues, i++);
		rgb(64, 26, 0, reds, greens, blues, i++);
		rgb(74, 77, 38, reds, greens, blues, i++);
		rgb(153, 204, 201, reds, greens, blues, i++);
		rgb(0, 0, 128, reds, greens, blues, i++);
		rgb(166, 124, 146, reds, greens, blues, i++);
		rgb(255, 140, 64, reds, greens, blues, i++);
		rgb(102, 128, 0, reds, greens, blues, i++);
		rgb(38, 51, 50, reds, greens, blues, i++);
		rgb(0, 0, 102, reds, greens, blues, i++);
		rgb(255, 0, 102, reds, greens, blues, i++);
		rgb(191, 105, 48, reds, greens, blues, i++);
		rgb(71, 89, 0, reds, greens, blues, i++);
		rgb(0, 238, 255, reds, greens, blues, i++);
		rgb(128, 128, 255, reds, greens, blues, i++);
		rgb(153, 0, 61, reds, greens, blues, i++);
		rgb(127, 70, 32, reds, greens, blues, i++);
		rgb(195, 230, 57, reds, greens, blues, i++);
		rgb(0, 179, 191, reds, greens, blues, i++);
		rgb(77, 77, 153, reds, greens, blues, i++);
		rgb(102, 0, 41, reds, greens, blues, i++);
		rgb(89, 49, 22, reds, greens, blues, i++);
		rgb(229, 255, 128, reds, greens, blues, i++);
		rgb(0, 131, 140, reds, greens, blues, i++);
		rgb(191, 191, 255, reds, greens, blues, i++);
		rgb(76, 19, 42, reds, greens, blues, i++);
		rgb(255, 179, 128, reds, greens, blues, i++);
		rgb(103, 115, 57, reds, greens, blues, i++);
		rgb(0, 60, 64, reds, greens, blues, i++);
		rgb(105, 105, 140, reds, greens, blues, i++);
		rgb(204, 102, 143, reds, greens, blues, i++);
		rgb(191, 134, 96, reds, greens, blues, i++);
		rgb(242, 255, 191, reds, greens, blues, i++);
		rgb(51, 99, 102, reds, greens, blues, i++);
		rgb(29, 26, 51, reds, greens, blues, i++);
		rgb(255, 0, 68, reds, greens, blues, i++);
		rgb(140, 98, 70, reds, greens, blues, i++);
		rgb(170, 255, 0, reds, greens, blues, i++);
		rgb(115, 150, 153, reds, greens, blues, i++);
		rgb(121, 96, 191, reds, greens, blues, i++);
		rgb(166, 0, 44, reds, greens, blues, i++);
		rgb(51, 36, 26, reds, greens, blues, i++);
		rgb(110, 166, 0, reds, greens, blues, i++);
		rgb(0, 204, 255, reds, greens, blues, i++);
		rgb(48, 38, 77, reds, greens, blues, i++);
		rgb(76, 0, 20, reds, greens, blues, i++);
		rgb(255, 217, 191, reds, greens, blues, i++);
		rgb(170, 204, 102, reds, greens, blues, i++);
		rgb(0, 102, 128, reds, greens, blues, i++);
		rgb(126, 57, 230, reds, greens, blues, i++);
		rgb(204, 51, 92, reds, greens, blues, i++);
		rgb(191, 163, 143, reds, greens, blues, i++);
		rgb(140, 153, 115, reds, greens, blues, i++);
		rgb(48, 163, 191, reds, greens, blues, i++);
		rgb(84, 38, 153, reds, greens, blues, i++);
		rgb(255, 128, 162, reds, greens, blues, i++);
		rgb(115, 98, 86, reds, greens, blues, i++);
		rgb(34, 64, 0, reds, greens, blues, i++);
		rgb(22, 76, 89, reds, greens, blues, i++);
		rgb(49, 22, 89, reds, greens, blues, i++);
		rgb(153, 77, 97, reds, greens, blues, i++);
		rgb(64, 54, 48, reds, greens, blues, i++);
		rgb(141, 217, 54, reds, greens, blues, i++);
		rgb(128, 230, 255, reds, greens, blues, i++);
		rgb(179, 128, 255, reds, greens, blues, i++);
		rgb(217, 163, 177, reds, greens, blues, i++);
		rgb(255, 136, 0, reds, greens, blues, i++);
		rgb(117, 153, 77, reds, greens, blues, i++);
		rgb(191, 242, 255, reds, greens, blues, i++);
		rgb(80, 57, 115, reds, greens, blues, i++);
		rgb(89, 67, 73, reds, greens, blues, i++);
		rgb(191, 102, 0, reds, greens, blues, i++);
		rgb(39, 51, 26, reds, greens, blues, i++);
		rgb(0, 170, 255, reds, greens, blues, i++);
		rgb(163, 143, 191, reds, greens, blues, i++);
		rgb(217, 0, 29, reds, greens, blues, i++);
		rgb(140, 75, 0, reds, greens, blues, i++);
		rgb(180, 204, 153, reds, greens, blues, i++);
		rgb(0, 128, 191, reds, greens, blues, i++);
		rgb(87, 77, 102, reds, greens, blues, i++);
		rgb(140, 0, 19, reds, greens, blues, i++);
		rgb(102, 54, 0, reds, greens, blues, i++);
		rgb(68, 77, 57, reds, greens, blues, i++);
		rgb(0, 94, 140, reds, greens, blues, i++);
		rgb(43, 38, 51, reds, greens, blues, i++);
		rgb(102, 0, 14, reds, greens, blues, i++);
		rgb(64, 34, 0, reds, greens, blues, i++);
		rgb(102, 255, 0, reds, greens, blues, i++);
		rgb(0, 51, 77, reds, greens, blues, i++);
		rgb(136, 0, 255, reds, greens, blues, i++);
		rgb(217, 54, 76, reds, greens, blues, i++);

		cachedLUT = new LUT(reds, greens, blues);
	}

	private static void rgb(int r, int g, int b, byte[] reds, byte[] greens, byte[] blues, int i)
	{
		reds[i] = (byte) (r & 255);
		greens[i] = (byte) (g & 255);
		blues[i] = (byte) (b & 255);
	}

	/**
	 * Create a colour LUT so that all colours from 1-255 are distinct.
	 * 
	 * @return The LUT
	 */
	public static LUT getColorModel()
	{
		return cachedLUT;
	}

	/**
	 * Build a custom LUT
	 * 
	 * @param lut
	 *            The LUT to create
	 * @return the LUT
	 */
	public static LUT createLUT(int lut)
	{
		if (lut >= 0 && lut < luts.length)
			return createLUT(LutColour.values()[lut]);
		return null;
	}

	/**
	 * Build a custom LUT
	 * 
	 * @param lut
	 *            The LUT to create
	 * @return the LUT
	 */
	public static LUT createLUT(LutColour lut)
	{
		return createLUT(lut, false);
	}

	/**
	 * Build a custom LUT.
	 *
	 * @param lut
	 *            The LUT to create
	 * @param includeBlack
	 *            Set to true to include black at index 0
	 * @return the LUT
	 */
	public static LUT createLUT(LutColour lut, boolean includeBlack)
	{
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];
		int nColors;
		switch (lut)
		{
			case RED_HOT:
			default:
				nColors = setColours(reds, greens, blues, Color.red, Color.yellow, Color.WHITE);
				break;
			case ICE:
				nColors = ice(reds, greens, blues);
				break;
			case RAINBOW:
				nColors = rainbow(reds, greens, blues);
				break;
			case FIRE:
				nColors = fire(reds, greens, blues);
				break;
			case FIRE_LIGHT:
				nColors = firelight(reds, greens, blues);
				break;
			case FIRE_GLOW:
				nColors = fireglow(reds, greens, blues);
				break;
			case RED_YELLOW:
				nColors = setColours(reds, greens, blues, Color.red, Color.yellow);
				break;
			case RED_BLUE:
				nColors = setColours(reds, greens, blues, Color.red, Color.blue);
				break;
			case RED_CYAN:
				nColors = setColours(reds, greens, blues, Color.red, Color.cyan);
				break;
			case RED:
				nColors = setColours(reds, greens, blues, Color.red);
				break;
			case GREEN:
				nColors = setColours(reds, greens, blues, Color.green);
				break;
			case BLUE:
				nColors = setColours(reds, greens, blues, Color.blue);
				break;
			case CYAN:
				nColors = setColours(reds, greens, blues, Color.cyan);
				break;
			case MAGENTA:
				nColors = setColours(reds, greens, blues, Color.magenta);
				break;
			case YELLOW:
				nColors = setColours(reds, greens, blues, Color.yellow);
				break;
		}
		if (nColors < 256)
			interpolate(reds, greens, blues, nColors);
		if (includeBlack)
			reds[0] = greens[0] = blues[0] = 0;
		return new LUT(reds, greens, blues);
	}

	private static int rainbow(byte[] reds, byte[] greens, byte[] blues)
	{
		// Using HSV vary the Hue from 300 (magenta) to Red (0)
		int n = 0;
		for (int h = 300; h >= 0; h -= 2)
		{
			Color c = Color.getHSBColor(h / 360.0f, 1, 1);
			reds[n] = (byte) c.getRed();
			greens[n] = (byte) c.getGreen();
			blues[n] = (byte) c.getBlue();
			n++;
		}
		return n;
	}

	private static int setColours(byte[] reds, byte[] greens, byte[] blues, Color... colours)
	{
		int n = 0;
		if (colours.length == 1)
		{
			reds[n] = (byte) (colours[0].getRed() / 2);
			greens[n] = (byte) (colours[0].getGreen() / 2);
			blues[n] = (byte) (colours[0].getBlue() / 2);
			n++;
		}

		for (Color colour : colours)
		{
			reds[n] = (byte) colour.getRed();
			greens[n] = (byte) colour.getGreen();
			blues[n] = (byte) colour.getBlue();
			n++;
		}
		return n;
	}

	/**
	 * Copied from ij.plugin.LutLoader
	 * 
	 * @param reds
	 * @param greens
	 * @param blues
	 * @return
	 */
	private static int ice(byte[] reds, byte[] greens, byte[] blues)
	{
		int[] r = { 0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186, 201, 217, 229, 242, 250, 250, 250, 250,
				251, 250, 250, 250, 250, 251, 251, 243, 230 };
		int[] g = { 156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125, 107, 93, 81, 87, 92, 97, 95, 93, 93, 90,
				85, 69, 64, 54, 47, 35, 19, 0, 4, 0 };
		int[] b = { 140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246, 250, 251, 250, 250, 245, 230, 230, 222,
				202, 180, 163, 142, 123, 114, 106, 94, 84, 64, 26, 27 };
		for (int i = 0; i < r.length; i++)
		{
			reds[i] = (byte) r[i];
			greens[i] = (byte) g[i];
			blues[i] = (byte) b[i];
		}
		return r.length;
	}

	/**
	 * Copied from ij.plugin.LutLoader
	 * 
	 * @param reds
	 * @param greens
	 * @param blues
	 * @return
	 */
	private static int fire(byte[] reds, byte[] greens, byte[] blues)
	{
		int[] r = { 0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255 };
		int[] g = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219,
				234, 248, 255, 255, 255, 255 };
		int[] b = { 0, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 35, 98, 160, 223, 255 };
		for (int i = 0; i < r.length; i++)
		{
			reds[i] = (byte) r[i];
			greens[i] = (byte) g[i];
			blues[i] = (byte) b[i];
		}
		return r.length;
	}

	/**
	 * Adapted from ij.plugin.LutLoader to remove the dark colours
	 * 
	 * @param reds
	 * @param greens
	 * @param blues
	 * @return
	 */
	private static int firelight(byte[] reds, byte[] greens, byte[] blues)
	{
		int[] r = { //0, 0, 1, 25, 49, 
				73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255 };
		int[] g = { //0, 0, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255, 255,
				255, 255 };
		int[] b = { //0, 61, 96, 130, 165, 
				192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98, 160, 223,
				255 };
		for (int i = 0; i < r.length; i++)
		{
			reds[i] = (byte) r[i];
			greens[i] = (byte) g[i];
			blues[i] = (byte) b[i];
		}
		return r.length;
	}

	/**
	 * Adapted from ij.plugin.LutLoader to remove the dark colours and near white colours
	 * 
	 * @param reds
	 * @param greens
	 * @param blues
	 * @return
	 */
	private static int fireglow(byte[] reds, byte[] greens, byte[] blues)
	{
		int[] r = { //0, 0, 1, 25, 49, 
				73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255
				//, 255, 255, 255 
				};
		int[] g = { //0, 0, 0, 0, 0, 
				0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255
				//, 255, 255, 255 
				};
		int[] b = { //0, 61, 96, 130, 165, 
				192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 35, 98
				//, 160, 223, 255 
				};
		for (int i = 0; i < r.length; i++)
		{
			reds[i] = (byte) r[i];
			greens[i] = (byte) g[i];
			blues[i] = (byte) b[i];
		}
		return r.length;
	}
	
	/**
	 * Copied from ij.plugin.LutLoader.
	 * 
	 * @param reds
	 * @param greens
	 * @param blues
	 * @param nColors
	 */
	private static void interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors)
	{
		byte[] r = new byte[nColors];
		byte[] g = new byte[nColors];
		byte[] b = new byte[nColors];
		System.arraycopy(reds, 0, r, 0, nColors);
		System.arraycopy(greens, 0, g, 0, nColors);
		System.arraycopy(blues, 0, b, 0, nColors);
		double scale = nColors / 256.0;
		int i1, i2;
		double fraction;
		for (int i = 0; i < 256; i++)
		{
			i1 = (int) (i * scale);
			i2 = i1 + 1;
			if (i2 == nColors)
				i2 = nColors - 1;
			fraction = i * scale - i1;
			//IJ.write(i+" "+i1+" "+i2+" "+fraction);
			reds[i] = (byte) ((1.0 - fraction) * (r[i1] & 255) + fraction * (r[i2] & 255));
			greens[i] = (byte) ((1.0 - fraction) * (g[i1] & 255) + fraction * (g[i2] & 255));
			blues[i] = (byte) ((1.0 - fraction) * (b[i1] & 255) + fraction * (b[i2] & 255));
		}
	}

	/**
	 * Get a colour from the LUT. The LUT is assumed to have a 256 colours in the table.
	 *
	 * @param lut
	 *            the lut
	 * @param i
	 *            The position in the LUT (from 0-255)
	 * @return a colour
	 */
	public static Color getColour(LUT lut, int i)
	{
		if (i < 0)
			i = 0;
		if (i > 255)
			i = 255;
		return new Color(lut.getRGB(i));
	}

	/**
	 * Get a colour from the LUT. If the total is equal or less than 256 then the lut can be assumed for an 8-bit image.
	 * If above 256 then the colour is assumed for a 16-bit image and so the position is scaled linearly to 0-255 to
	 * find the colour. The uses the {@link #getColour(LUT, int, int, int)} method.
	 *
	 * @param lut
	 *            the lut
	 * @param n
	 *            The position in the series (zero-based)
	 * @param total
	 *            The total in the series
	 * @return a colour
	 */
	public static Color getColour(LUT lut, int n, int total)
	{
		if (total <= 255)
		{
			// Assume 8-bit image
			return getColour(lut, n);
		}

		// Use behaviour for 16-bit images 
		return getColour(lut, n, 0, total);
	}

	/**
	 * Get a colour from the LUT. Used for 16-bit images.
	 *
	 * @param lut
	 *            the lut
	 * @param value
	 *            the value
	 * @param minimum
	 *            the minimum display value
	 * @param maximum
	 *            the maximum display value
	 * @return a colour
	 */
	public static Color getColour(LUT lut, int value, int minimum, int maximum)
	{
		// Logic copied from ShortProcessor.create8BitImage
		if (minimum < 0)
			minimum = 0;
		if (maximum > 65535)
			maximum = 65535;

		double scale = 256.0 / (maximum - minimum + 1);
		value = value - minimum;
		if (value < 0)
			value = 0;
		value = (int) (value * scale + 0.5);
		if (value > 255)
			value = 255;

		return new Color(lut.getRGB(value));
	}

	/**
	 * Get a colour from the LUT. Used for 32-bit images.
	 *
	 * @param lut
	 *            the lut
	 * @param value
	 *            the value
	 * @param minimum
	 *            the minimum
	 * @param maximum
	 *            the maximum
	 * @return a colour
	 */
	public static Color getColour(LUT lut, float value, float minimum, float maximum)
	{
		// Logic copied from FloatProcessor.create8BitImage

		// No range check on the input min/max

		float scale = 255f / (maximum - minimum);
		value = value - minimum;
		if (value < 0f)
			value = 0f;
		int ivalue = (int) ((value * scale) + 0.5f);
		if (ivalue > 255)
			ivalue = 255;

		return new Color(lut.getRGB(ivalue));
	}

	/**
	 * Get a colour from the LUT ignoring zero. If the total is equal or less than 256 then the lut can be assumed for
	 * an 8-bit image.
	 * If above 256 then the colour is assumed for a 16-bit image and so the position is scaled linearly to 1-255 to
	 * find the colour. The uses the {@link #getNonZeroColour(LUT, int, int, int)} method.
	 *
	 * @param lut
	 *            the lut
	 * @param n
	 *            The position in the series (zero-based)
	 * @param total
	 *            The total in the series
	 * @return a colour
	 */
	public static Color getNonZeroColour(LUT lut, int n, int total)
	{
		if (total <= 256)
		{
			// Assume 8-bit image
			return getColour(lut, n);
		}

		// Use behaviour for 16-bit images 
		return getNonZeroColour(lut, n, 1, total);
	}

	/**
	 * Get a colour from the LUT ignoring zero. Used for 16-bit images.
	 *
	 * @param lut
	 *            the lut
	 * @param value
	 *            the value
	 * @param minimum
	 *            the minimum display value (mapped to 1)
	 * @param maximum
	 *            the maximum display value (mapped to 255)
	 * @return a colour
	 */
	public static Color getNonZeroColour(LUT lut, int value, int minimum, int maximum)
	{
		// Logic copied from ShortProcessor.create8BitImage
		if (minimum < 0)
			minimum = 0;
		if (maximum > 65535)
			maximum = 65535;

		double scale = 255.0 / (maximum - minimum + 1);
		value = value - minimum;
		if (value < 0)
			value = 0;
		value = 1 + (int) (value * scale + 0.5);
		if (value > 255)
			value = 255;

		return new Color(lut.getRGB(value));
	}

	/**
	 * Get a colour from the LUT ignoring zero. Used for 32-bit images.
	 *
	 * @param lut
	 *            the lut
	 * @param value
	 *            the value
	 * @param minimum
	 *            the minimum display value (mapped to 1)
	 * @param maximum
	 *            the maximum display value (mapped to 255)
	 * @return a colour
	 */
	public static Color getNonZeroColour(LUT lut, float value, float minimum, float maximum)
	{
		// Logic copied from FloatProcessor.create8BitImage

		// No range check on the input min/max

		float scale = 254f / (maximum - minimum);
		value = value - minimum;
		if (value < 0f)
			value = 0f;
		int ivalue = 1 + (int) ((value * scale) + 0.5f);
		if (ivalue > 255)
			ivalue = 255;

		return new Color(lut.getRGB(ivalue));
	}

	/**
	 * Provide mapping for values to the range of 0-255 for a 8-bit image.
	 */
	public interface LUTMapper
	{
		/**
		 * Map the value to a value between 0 and 255.
		 *
		 * @param value
		 *            the value
		 * @return the mapped value
		 */
		public int map(float value);

		/**
		 * Map the value to a value between 0 and 255.
		 *
		 * @param value
		 *            the value
		 * @return the mapped value
		 */
		public float mapf(float value);

		/**
		 * Gets the colour.
		 *
		 * @param lut
		 *            the lut
		 * @param value
		 *            the value
		 * @return the colour
		 */
		public Color getColour(LUT lut, float value);

		/**
		 * Gets the min value output by {@link #map(float)}
		 *
		 * @return the min value
		 */
		public int getMin();

		/**
		 * Gets the max value output by {@link #map(float)}
		 *
		 * @return the max value
		 */
		public int getMax();
	}

	/**
	 * Provide no mapping
	 */
	public static class NullLUTMapper implements LUTMapper
	{
		/**
		 * Rounds the input to the nearest int and truncates to the range 0-255.
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#map(float)
		 */
		public int map(float value)
		{
			if (value < 0f)
				return 0;
			if (value > 255f)
				return 255;
			return (int) Math.round(value);
		}

		/**
		 * Provide no mapping (returns the input value)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#mapf(float)
		 */
		public float mapf(float value)
		{
			return value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#getColour(ij.process.LUT, float)
		 */
		public Color getColour(LUT lut, float value)
		{
			return new Color(lut.getRGB(map(value)));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#getMin()
		 */
		public int getMin()
		{
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#getMax()
		 */
		public int getMax()
		{
			return 255;
		}
	}

	/**
	 * Provide a default map for a value to the range 0-255. Functionality will match that of rendered 32-bit images.
	 */
	public static class DefaultLUTMapper extends NullLUTMapper
	{
		final float minimum, maximum, scale;

		/**
		 * Instantiates a new default LUT mapper.
		 *
		 * @param minimum
		 *            the minimum
		 * @param maximum
		 *            the maximum
		 */
		public DefaultLUTMapper(float minimum, float maximum)
		{
			this.maximum = maximum;
			this.minimum = minimum;
			scale = 255f / (maximum - minimum);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#map(float)
		 */
		public int map(float value)
		{
			value = value - minimum;
			if (value < 0f)
				value = 0f;
			int ivalue = (int) ((value * scale) + 0.5f);
			if (ivalue > 255)
				ivalue = 255;
			return ivalue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#mapf(float)
		 */
		public float mapf(float value)
		{
			return map(value);
		}
	}

	/**
	 * Provide a default map for a value to the range 1-255
	 */
	public static class NonZeroLUTMapper extends NullLUTMapper
	{
		final float minimum, maximum, scale;

		/**
		 * Instantiates a new non zero LUT mapper.
		 *
		 * @param minimum
		 *            the minimum
		 * @param maximum
		 *            the maximum
		 */
		public NonZeroLUTMapper(float minimum, float maximum)
		{
			this.maximum = maximum;
			this.minimum = minimum;
			scale = 254f / (maximum - minimum);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#map(float)
		 */
		public int map(float value)
		{
			value = value - minimum;
			if (value < 0f)
				value = 0f;
			int ivalue = 1 + (int) ((value * scale) + 0.5f);
			if (ivalue > 255)
				ivalue = 255;
			return ivalue;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.LUTMapper#mapf(float)
		 */
		public float mapf(float value)
		{
			return map(value);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ij.process.LUTHelper.NullLUTMapper#getMin()
		 */
		@Override
		public int getMin()
		{
			return 1;
		}
	}
}

/*-
 * #%L
 * Genome Damage and Stability Centre ImageJ Core Package
 *
 * Contains code used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2018 Alex Herbert
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
package ij.process;

import java.awt.Color;

/**
 * Contains functions for ImageJ LUTs.
 */
public class LUTHelper
{
	/**
	 * The LUT Colour.
	 */
	public enum LutColour
	{
		//@formatter:off
		/** The red hot LUT. */
		RED_HOT{ @Override
		public String getName() { return "Red-Hot"; }},

		/** The ice LUT. */
		ICE{ @Override
		public String getName() { return "Ice";}},

		/** The rainbow LUT. */
		RAINBOW{ @Override
		public String getName() { return "Rainbow"; }},

		/** The fire LUT. */
		FIRE{ @Override
		public String getName() { return "Fire"; }},

		/** The fire light LUT. */
		FIRE_LIGHT{ @Override
		public String getName() { return "FireLight"; }},

		/** The fire glow LUT. */
		FIRE_GLOW{ @Override
		public String getName() { return "FireGlow"; }},

		/** The red yellow LUT. */
		RED_YELLOW{ @Override
		public String getName() { return "Red-Yellow"; }},

		/** The red LUT. */
		RED{ @Override
		public String getName() { return "Red"; }},

		/** The green LUT. */
		GREEN{ @Override
		public String getName() { return "Green"; }},

		/** The blue LUT. */
		BLUE{ @Override
		public String getName() { return "Blue"; }},

		/** The cyan LUT. */
		CYAN{ @Override
		public String getName() { return "Cyan"; }},

		/** The magenta LUT. */
		MAGENTA{ @Override
		public String getName() { return "Magenta"; }},

		/** The yellow LUT. */
		YELLOW{ @Override
		public String getName() { return "Yellow"; }},

		/** The red blue LUT. */
		RED_BLUE{ @Override
		public String getName() { return "Red-Blue"; }},

		/** The intense LUT. */
		INTENSE{ @Override
		public String getName() { return "Intense"; }
			@Override
			public boolean isDistinct() { return true;}},

		/** The pimp LUT. */
		PIMP{ @Override
		public String getName() { return "Pimp"; }
			@Override
			public boolean isDistinct() { return true;}},

		/** The pimp light LUT. */
		PIMP_LIGHT{ @Override
		public String getName() { return "PimpLight"; }
			@Override
			public boolean isDistinct() { return true;}},

		/** The distinct LUT. */
		DISTINCT{ @Override
		public String getName() { return "Distinct"; }
			@Override
			public boolean isDistinct() { return true;}},

		/** The red cyan LUT. */
		RED_CYAN{ @Override
		public String getName() { return "Red-Cyan"; }},

		/** The grays LUT. */
		GRAYS{ @Override
		public String getName() { return "Grays"; }};
		//@formatter:on

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
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

		/**
		 * Checks if is a distinct colour set.
		 *
		 * @return true, if is distinct
		 */
		public boolean isDistinct()
		{
			return false;
		}

		/**
		 * Get the LUT colour for the given number.
		 *
		 * @param lut
		 *            the lut number
		 * @return the lut colour
		 */
		public static LutColour forNumber(int lut)
		{
			final LutColour[] values = LutColour.values();
			if (lut >= 0 && lut < values.length)
				return values[lut];
			return null;
		}
	}

	/** List of the LUT names. */
	public static final String[] luts;

	/** LUTs with distinct colours. */

	// Finding distinct colours using the Hue, Chroma, Lightness scales to set constraints.
	// http://tools.medialab.sciences-po.fr/iwanthue/
	// Colours were generated for 256 distinct colours using the force-vector method and then randomly ordered.

	// Intense = H 0:360, C 20-100, L 15-80
	private static final int[][] intense = new int[][] { { 165, 54, 0 }, { 255, 138, 168 }, { 189, 0, 40 },
			{ 56, 158, 0 }, { 248, 180, 197 }, { 1, 200, 181 }, { 151, 0, 25 }, { 146, 0, 46 }, { 59, 24, 62 },
			{ 242, 191, 53 }, { 83, 3, 8 }, { 64, 93, 0 }, { 233, 189, 162 }, { 176, 208, 151 }, { 117, 95, 125 },
			{ 53, 24, 76 }, { 111, 222, 100 }, { 254, 0, 31 }, { 0, 61, 170 }, { 131, 0, 161 }, { 121, 65, 0 },
			{ 0, 100, 199 }, { 199, 0, 80 }, { 255, 80, 165 }, { 6, 226, 146 }, { 184, 82, 255 }, { 255, 169, 183 },
			{ 60, 112, 0 }, { 194, 207, 54 }, { 255, 142, 129 }, { 98, 0, 155 }, { 128, 48, 0 }, { 0, 84, 142 },
			{ 132, 218, 128 }, { 255, 120, 158 }, { 155, 118, 0 }, { 123, 82, 72 }, { 20, 180, 0 }, { 195, 163, 255 },
			{ 0, 177, 126 }, { 237, 192, 82 }, { 112, 0, 27 }, { 73, 56, 225 }, { 167, 0, 186 }, { 46, 53, 0 },
			{ 242, 148, 255 }, { 87, 199, 0 }, { 1, 33, 148 }, { 250, 185, 126 }, { 59, 1, 102 }, { 187, 132, 255 },
			{ 236, 187, 175 }, { 142, 211, 200 }, { 1, 205, 150 }, { 103, 140, 255 }, { 162, 0, 47 }, { 68, 219, 207 },
			{ 98, 213, 240 }, { 223, 177, 255 }, { 160, 110, 255 }, { 128, 208, 243 }, { 109, 155, 255 },
			{ 129, 0, 116 }, { 0, 133, 30 }, { 84, 0, 85 }, { 0, 146, 211 }, { 155, 214, 119 }, { 0, 138, 68 },
			{ 0, 162, 139 }, { 33, 41, 0 }, { 210, 65, 244 }, { 255, 122, 177 }, { 218, 40, 0 }, { 206, 203, 73 },
			{ 0, 66, 151 }, { 201, 139, 0 }, { 210, 0, 104 }, { 152, 95, 0 }, { 167, 137, 167 }, { 88, 0, 44 },
			{ 128, 220, 67 }, { 92, 0, 0 }, { 2, 151, 185 }, { 0, 49, 165 }, { 132, 108, 0 }, { 0, 206, 49 },
			{ 0, 61, 41 }, { 32, 218, 233 }, { 38, 23, 100 }, { 200, 102, 255 }, { 255, 156, 112 }, { 106, 0, 76 },
			{ 255, 114, 204 }, { 250, 0, 204 }, { 115, 46, 220 }, { 0, 85, 162 }, { 255, 162, 221 }, { 99, 120, 255 },
			{ 68, 12, 70 }, { 211, 152, 255 }, { 187, 149, 127 }, { 0, 2, 140 }, { 1, 103, 94 }, { 240, 174, 0 },
			{ 2, 162, 157 }, { 149, 208, 0 }, { 182, 211, 38 }, { 34, 109, 0 }, { 201, 84, 0 }, { 0, 176, 109 },
			{ 0, 178, 69 }, { 154, 142, 0 }, { 67, 24, 23 }, { 0, 91, 71 }, { 0, 78, 21 }, { 223, 0, 57 },
			{ 255, 67, 83 }, { 0, 60, 12 }, { 1, 114, 90 }, { 77, 2, 57 }, { 255, 113, 114 }, { 136, 121, 255 },
			{ 110, 178, 255 }, { 153, 161, 125 }, { 136, 24, 0 }, { 45, 198, 255 }, { 1, 109, 244 }, { 41, 64, 0 },
			{ 107, 44, 0 }, { 1, 144, 156 }, { 0, 141, 101 }, { 59, 47, 0 }, { 83, 39, 0 }, { 255, 143, 102 },
			{ 255, 117, 90 }, { 83, 55, 0 }, { 255, 181, 81 }, { 255, 98, 98 }, { 176, 0, 156 }, { 174, 183, 255 },
			{ 0, 173, 78 }, { 1, 209, 85 }, { 0, 50, 129 }, { 153, 0, 119 }, { 0, 131, 102 }, { 0, 97, 97 },
			{ 187, 101, 0 }, { 45, 32, 64 }, { 255, 163, 91 }, { 190, 0, 25 }, { 0, 83, 178 }, { 0, 47, 28 },
			{ 204, 127, 0 }, { 194, 180, 255 }, { 167, 68, 0 }, { 116, 218, 155 }, { 74, 14, 38 }, { 255, 107, 253 },
			{ 2, 139, 231 }, { 78, 0, 133 }, { 232, 174, 255 }, { 94, 112, 0 }, { 0, 76, 229 }, { 255, 64, 140 },
			{ 255, 99, 145 }, { 0, 64, 111 }, { 0, 117, 147 }, { 106, 150, 0 }, { 0, 131, 234 }, { 255, 142, 40 },
			{ 255, 173, 174 }, { 105, 96, 0 }, { 0, 208, 245 }, { 145, 213, 167 }, { 67, 77, 0 }, { 203, 201, 138 },
			{ 255, 132, 234 }, { 82, 42, 54 }, { 171, 213, 2 }, { 246, 31, 219 }, { 138, 0, 184 }, { 255, 117, 194 },
			{ 221, 0, 147 }, { 225, 106, 255 }, { 124, 0, 52 }, { 255, 76, 195 }, { 255, 123, 248 }, { 137, 172, 255 },
			{ 0, 48, 89 }, { 147, 218, 11 }, { 107, 0, 115 }, { 17, 44, 1 }, { 1, 146, 191 }, { 255, 130, 61 },
			{ 223, 195, 142 }, { 210, 66, 0 }, { 244, 189, 98 }, { 0, 112, 228 }, { 179, 0, 98 }, { 71, 22, 10 },
			{ 255, 83, 42 }, { 175, 0, 116 }, { 0, 192, 232 }, { 216, 201, 51 }, { 187, 207, 125 }, { 186, 0, 134 },
			{ 239, 0, 118 }, { 193, 0, 186 }, { 223, 0, 176 }, { 253, 0, 99 }, { 0, 54, 218 }, { 0, 88, 218 },
			{ 234, 76, 253 }, { 173, 212, 67 }, { 255, 169, 135 }, { 203, 106, 0 }, { 52, 0, 158 }, { 227, 0, 134 },
			{ 0, 101, 61 }, { 85, 74, 0 }, { 92, 0, 32 }, { 116, 0, 8 }, { 164, 118, 133 }, { 1, 151, 245 },
			{ 0, 26, 161 }, { 99, 100, 255 }, { 255, 186, 30 }, { 139, 0, 150 }, { 63, 29, 3 }, { 235, 194, 0 },
			{ 21, 36, 74 }, { 117, 217, 179 }, { 147, 217, 70 }, { 90, 131, 0 }, { 2, 23, 117 }, { 90, 182, 255 },
			{ 0, 88, 13 }, { 255, 67, 117 }, { 250, 181, 176 }, { 226, 185, 230 }, { 40, 226, 127 }, { 84, 44, 42 },
			{ 1, 185, 184 }, { 244, 28, 0 }, { 217, 197, 131 }, { 125, 66, 240 }, { 1, 121, 173 }, { 255, 173, 49 },
			{ 187, 207, 106 }, { 0, 34, 93 }, { 170, 200, 248 }, { 255, 16, 75 }, { 255, 142, 155 }, { 143, 0, 69 },
			{ 212, 188, 247 }, { 0, 142, 13 } };

	// Pimp = H 0:360, C 30-100, L 25-70
	private static final int[][] pimp = new int[][] { { 110, 20, 88 }, { 18, 69, 14 }, { 78, 194, 41 }, { 175, 0, 30 },
			{ 234, 149, 134 }, { 45, 23, 179 }, { 1, 173, 133 }, { 184, 173, 69 }, { 217, 0, 115 }, { 104, 39, 16 },
			{ 131, 0, 93 }, { 0, 181, 163 }, { 210, 118, 255 }, { 62, 143, 0 }, { 26, 133, 0 }, { 204, 152, 223 },
			{ 178, 115, 0 }, { 255, 97, 210 }, { 105, 14, 109 }, { 158, 141, 255 }, { 253, 137, 141 },
			{ 243, 126, 245 }, { 175, 119, 158 }, { 182, 111, 115 }, { 139, 0, 22 }, { 69, 194, 86 }, { 88, 191, 109 },
			{ 5, 71, 3 }, { 1, 76, 200 }, { 234, 0, 181 }, { 99, 42, 33 }, { 124, 46, 0 }, { 171, 0, 115 },
			{ 255, 93, 99 }, { 255, 46, 157 }, { 199, 167, 93 }, { 103, 175, 254 }, { 65, 63, 0 }, { 255, 46, 173 },
			{ 117, 23, 16 }, { 170, 176, 111 }, { 183, 107, 0 }, { 242, 119, 255 }, { 223, 156, 101 },
			{ 252, 133, 174 }, { 107, 51, 223 }, { 250, 129, 210 }, { 138, 186, 19 }, { 109, 187, 130 },
			{ 94, 34, 100 }, { 1, 108, 166 }, { 237, 43, 227 }, { 155, 112, 255 }, { 207, 136, 0 }, { 222, 0, 63 },
			{ 249, 0, 168 }, { 167, 156, 0 }, { 46, 44, 145 }, { 237, 106, 255 }, { 255, 119, 101 }, { 148, 112, 70 },
			{ 175, 0, 46 }, { 231, 155, 17 }, { 99, 187, 160 }, { 0, 175, 253 }, { 0, 102, 73 }, { 255, 118, 78 },
			{ 0, 95, 175 }, { 218, 75, 0 }, { 255, 132, 149 }, { 173, 96, 255 }, { 1, 173, 74 }, { 179, 141, 97 },
			{ 0, 98, 34 }, { 238, 116, 0 }, { 255, 43, 193 }, { 37, 50, 135 }, { 118, 18, 38 }, { 113, 31, 0 },
			{ 1, 111, 34 }, { 59, 81, 252 }, { 134, 72, 0 }, { 251, 15, 18 }, { 38, 154, 0 }, { 104, 145, 100 },
			{ 255, 137, 107 }, { 180, 155, 255 }, { 0, 144, 63 }, { 164, 180, 39 }, { 170, 49, 0 }, { 110, 45, 0 },
			{ 1, 61, 116 }, { 255, 115, 41 }, { 0, 76, 125 }, { 30, 55, 123 }, { 183, 149, 199 }, { 198, 142, 112 },
			{ 143, 0, 67 }, { 127, 62, 80 }, { 177, 0, 106 }, { 64, 123, 0 }, { 216, 142, 149 }, { 0, 153, 210 },
			{ 125, 69, 102 }, { 1, 174, 221 }, { 117, 135, 0 }, { 84, 61, 17 }, { 106, 8, 195 }, { 255, 101, 124 },
			{ 137, 107, 0 }, { 0, 87, 195 }, { 12, 170, 255 }, { 89, 41, 90 }, { 255, 113, 144 }, { 210, 0, 101 },
			{ 53, 57, 103 }, { 130, 92, 0 }, { 213, 164, 17 }, { 123, 75, 0 }, { 255, 50, 213 }, { 112, 25, 59 },
			{ 251, 135, 163 }, { 134, 88, 129 }, { 247, 145, 87 }, { 225, 1, 139 }, { 146, 0, 132 }, { 166, 0, 70 },
			{ 114, 190, 38 }, { 238, 0, 209 }, { 195, 75, 253 }, { 255, 95, 154 }, { 167, 177, 92 }, { 255, 117, 123 },
			{ 207, 0, 150 }, { 119, 189, 69 }, { 205, 0, 46 }, { 235, 137, 220 }, { 107, 36, 28 }, { 129, 0, 53 },
			{ 0, 134, 110 }, { 88, 165, 255 }, { 164, 125, 0 }, { 97, 191, 83 }, { 198, 0, 203 }, { 152, 0, 175 },
			{ 211, 0, 83 }, { 57, 108, 0 }, { 199, 42, 0 }, { 128, 64, 238 }, { 249, 141, 124 }, { 204, 150, 234 },
			{ 95, 48, 0 }, { 128, 157, 255 }, { 163, 98, 89 }, { 241, 149, 67 }, { 214, 163, 48 }, { 55, 50, 120 },
			{ 120, 0, 138 }, { 125, 154, 0 }, { 212, 0, 69 }, { 97, 91, 255 }, { 0, 173, 3 }, { 1, 83, 240 },
			{ 87, 70, 241 }, { 118, 4, 74 }, { 1, 176, 110 }, { 255, 66, 94 }, { 224, 157, 79 }, { 67, 99, 0 },
			{ 24, 69, 26 }, { 255, 93, 66 }, { 144, 0, 46 }, { 95, 104, 255 }, { 230, 93, 0 }, { 67, 44, 123 },
			{ 220, 60, 240 }, { 1, 149, 117 }, { 161, 92, 98 }, { 214, 147, 227 }, { 255, 129, 188 }, { 114, 111, 163 },
			{ 255, 117, 221 }, { 145, 183, 91 }, { 136, 182, 133 }, { 177, 107, 123 }, { 190, 0, 177 }, { 0, 105, 241 },
			{ 0, 112, 235 }, { 1, 52, 178 }, { 238, 146, 0 }, { 206, 164, 101 }, { 255, 93, 168 }, { 197, 167, 111 },
			{ 0, 122, 237 }, { 195, 165, 0 }, { 59, 192, 145 }, { 155, 0, 40 }, { 0, 101, 143 }, { 255, 132, 54 },
			{ 255, 133, 19 }, { 177, 161, 107 }, { 177, 162, 223 }, { 86, 87, 136 }, { 66, 34, 149 }, { 183, 0, 61 },
			{ 175, 74, 0 }, { 0, 65, 217 }, { 0, 83, 47 }, { 0, 186, 202 }, { 1, 106, 204 }, { 127, 118, 255 },
			{ 151, 183, 45 }, { 158, 0, 96 }, { 138, 183, 114 }, { 126, 0, 20 }, { 254, 143, 2 }, { 153, 0, 1 },
			{ 81, 32, 132 }, { 0, 141, 236 }, { 149, 168, 241 }, { 95, 90, 0 }, { 90, 56, 0 }, { 101, 53, 31 },
			{ 202, 0, 24 }, { 1, 148, 44 }, { 95, 40, 74 }, { 1, 53, 213 }, { 48, 142, 255 }, { 185, 172, 88 },
			{ 105, 93, 43 }, { 221, 139, 248 }, { 146, 39, 0 }, { 214, 159, 122 }, { 235, 145, 164 }, { 189, 0, 158 },
			{ 101, 0, 170 }, { 205, 89, 255 }, { 141, 59, 0 }, { 71, 185, 0 }, { 137, 138, 0 }, { 0, 123, 70 },
			{ 185, 153, 0 }, { 238, 148, 112 }, { 184, 0, 146 }, { 121, 0, 101 }, { 255, 41, 96 }, { 255, 61, 131 },
			{ 61, 81, 0 }, { 171, 85, 0 }, { 174, 5, 0 }, { 106, 35, 44 }, { 67, 51, 100 }, { 0, 182, 212 },
			{ 0, 71, 142 }, { 164, 0, 84 }, { 99, 81, 0 } };

	// Pimp = H 0:360, C 30-100, L 35-90
	private static final int[][] pimpLight = new int[][] { { 19, 70, 196 }, { 255, 156, 240 }, { 173, 110, 255 },
			{ 70, 73, 159 }, { 167, 0, 97 }, { 216, 238, 58 }, { 88, 68, 156 }, { 134, 164, 114 }, { 232, 183, 0 },
			{ 255, 98, 166 }, { 255, 151, 115 }, { 255, 146, 79 }, { 190, 0, 21 }, { 107, 59, 156 }, { 177, 97, 255 },
			{ 204, 0, 64 }, { 191, 0, 148 }, { 141, 54, 48 }, { 255, 125, 123 }, { 140, 159, 213 }, { 147, 57, 0 },
			{ 248, 229, 45 }, { 128, 23, 206 }, { 184, 70, 0 }, { 2, 146, 225 }, { 204, 0, 136 }, { 114, 104, 0 },
			{ 0, 217, 152 }, { 255, 52, 196 }, { 0, 113, 94 }, { 90, 52, 222 }, { 240, 0, 155 }, { 1, 150, 123 },
			{ 255, 115, 58 }, { 113, 135, 187 }, { 113, 75, 15 }, { 58, 255, 152 }, { 0, 163, 121 }, { 211, 182, 0 },
			{ 214, 189, 136 }, { 255, 44, 130 }, { 2, 198, 223 }, { 84, 190, 0 }, { 148, 21, 135 }, { 0, 118, 192 },
			{ 136, 92, 58 }, { 196, 48, 0 }, { 103, 64, 142 }, { 0, 199, 202 }, { 42, 94, 22 }, { 255, 85, 30 },
			{ 65, 102, 150 }, { 134, 0, 185 }, { 134, 61, 30 }, { 255, 79, 148 }, { 209, 76, 254 }, { 84, 58, 188 },
			{ 143, 251, 102 }, { 183, 144, 255 }, { 123, 69, 14 }, { 187, 0, 51 }, { 119, 57, 132 }, { 230, 112, 255 },
			{ 220, 1, 92 }, { 138, 245, 227 }, { 255, 160, 49 }, { 230, 144, 255 }, { 79, 89, 38 }, { 143, 54, 21 },
			{ 230, 179, 255 }, { 0, 210, 85 }, { 223, 0, 1 }, { 99, 122, 0 }, { 158, 32, 47 }, { 255, 101, 249 },
			{ 255, 211, 141 }, { 41, 153, 0 }, { 198, 0, 174 }, { 122, 125, 0 }, { 255, 99, 111 }, { 1, 164, 238 },
			{ 255, 145, 185 }, { 24, 255, 177 }, { 232, 0, 57 }, { 252, 224, 147 }, { 114, 254, 124 },
			{ 255, 111, 222 }, { 118, 218, 255 }, { 47, 251, 239 }, { 144, 221, 0 }, { 126, 254, 81 },
			{ 173, 178, 255 }, { 202, 0, 117 }, { 173, 0, 60 }, { 115, 43, 176 }, { 0, 161, 90 }, { 175, 247, 84 },
			{ 187, 152, 0 }, { 253, 46, 227 }, { 187, 195, 252 }, { 121, 47, 156 }, { 255, 171, 119 }, { 216, 99, 0 },
			{ 222, 233, 156 }, { 114, 111, 162 }, { 49, 92, 48 }, { 255, 24, 207 }, { 255, 91, 62 }, { 68, 209, 0 },
			{ 154, 109, 73 }, { 0, 248, 112 }, { 95, 166, 255 }, { 132, 52, 112 }, { 1, 149, 91 }, { 199, 142, 0 },
			{ 2, 214, 195 }, { 1, 68, 220 }, { 165, 4, 73 }, { 128, 91, 0 }, { 211, 0, 206 }, { 194, 42, 225 },
			{ 224, 149, 163 }, { 248, 1, 132 }, { 86, 158, 0 }, { 153, 246, 173 }, { 136, 40, 137 }, { 175, 159, 105 },
			{ 206, 241, 7 }, { 2, 111, 195 }, { 201, 188, 0 }, { 40, 81, 149 }, { 149, 102, 0 }, { 79, 229, 44 },
			{ 144, 40, 107 }, { 2, 178, 234 }, { 215, 155, 255 }, { 255, 147, 250 }, { 255, 198, 12 },
			{ 158, 152, 255 }, { 255, 171, 169 }, { 182, 234, 0 }, { 255, 127, 231 }, { 255, 118, 0 }, { 96, 85, 0 },
			{ 242, 230, 89 }, { 255, 213, 71 }, { 236, 230, 120 }, { 149, 249, 137 }, { 225, 0, 108 },
			{ 195, 238, 186 }, { 128, 135, 255 }, { 255, 137, 203 }, { 255, 15, 96 }, { 0, 84, 175 }, { 183, 246, 59 },
			{ 255, 121, 206 }, { 67, 100, 0 }, { 0, 194, 119 }, { 0, 175, 72 }, { 87, 255, 97 }, { 206, 111, 255 },
			{ 246, 96, 0 }, { 255, 24, 151 }, { 201, 102, 0 }, { 1, 206, 171 }, { 89, 248, 247 }, { 126, 66, 44 },
			{ 0, 131, 49 }, { 75, 66, 236 }, { 0, 175, 17 }, { 208, 238, 119 }, { 127, 91, 255 }, { 148, 47, 58 },
			{ 190, 0, 43 }, { 164, 22, 28 }, { 165, 0, 163 }, { 141, 172, 0 }, { 110, 113, 255 }, { 0, 95, 211 },
			{ 255, 168, 85 }, { 67, 90, 19 }, { 150, 40, 82 }, { 230, 185, 233 }, { 195, 128, 115 }, { 73, 144, 0 },
			{ 0, 93, 144 }, { 145, 204, 0 }, { 116, 138, 0 }, { 28, 95, 38 }, { 144, 247, 188 }, { 255, 117, 159 },
			{ 0, 115, 71 }, { 255, 59, 68 }, { 166, 82, 0 }, { 242, 0, 79 }, { 202, 241, 95 }, { 200, 85, 255 },
			{ 0, 161, 28 }, { 237, 171, 255 }, { 245, 0, 108 }, { 254, 225, 108 }, { 168, 0, 201 }, { 18, 130, 255 },
			{ 255, 157, 209 }, { 1, 150, 142 }, { 236, 53, 0 }, { 153, 17, 119 }, { 143, 0, 168 }, { 160, 249, 103 },
			{ 143, 110, 0 }, { 82, 67, 165 }, { 0, 78, 176 }, { 255, 162, 137 }, { 231, 1, 197 }, { 112, 164, 0 },
			{ 85, 75, 130 }, { 66, 109, 68 }, { 220, 165, 131 }, { 71, 126, 0 }, { 238, 158, 0 }, { 255, 174, 101 },
			{ 195, 194, 0 }, { 235, 169, 0 }, { 255, 110, 134 }, { 131, 57, 232 }, { 176, 0, 124 }, { 202, 174, 255 },
			{ 108, 68, 113 }, { 174, 127, 255 }, { 255, 194, 127 }, { 255, 200, 86 }, { 142, 77, 75 },
			{ 152, 128, 179 }, { 255, 186, 235 }, { 237, 229, 141 }, { 0, 199, 237 }, { 0, 219, 106 },
			{ 184, 242, 154 }, { 0, 123, 230 }, { 255, 191, 137 }, { 255, 140, 33 }, { 152, 87, 0 }, { 255, 109, 227 },
			{ 184, 0, 201 }, { 1, 135, 186 }, { 2, 94, 230 }, { 163, 35, 0 }, { 112, 158, 255 }, { 255, 127, 108 },
			{ 232, 139, 0 }, { 190, 145, 105 }, { 0, 145, 148 }, { 167, 109, 145 }, { 164, 163, 0 }, { 255, 74, 68 },
			{ 61, 253, 208 } };

	// This was produced using http://phrogz.net/css/distinct-colors.html
	private static final int[][] distinct = new int[][] { { 0, 0, 0 }, { 255, 0, 0 }, { 153, 135, 115 },
			{ 76, 217, 54 }, { 0, 136, 255 }, { 129, 105, 140 }, { 242, 0, 0 }, { 255, 170, 0 }, { 0, 51, 0 },
			{ 0, 102, 191 }, { 204, 0, 255 }, { 166, 0, 0 }, { 191, 128, 0 }, { 127, 255, 145 }, { 0, 75, 140 },
			{ 184, 54, 217 }, { 51, 0, 0 }, { 115, 77, 0 }, { 83, 166, 94 }, { 0, 48, 89 }, { 230, 128, 255 },
			{ 255, 128, 128 }, { 76, 51, 0 }, { 38, 77, 43 }, { 128, 196, 255 }, { 46, 26, 51 }, { 153, 77, 77 },
			{ 51, 34, 0 }, { 143, 191, 150 }, { 191, 225, 255 }, { 71, 0, 77 }, { 76, 38, 38 }, { 229, 172, 57 },
			{ 0, 140, 37 }, { 77, 90, 102 }, { 133, 35, 140 }, { 51, 26, 26 }, { 153, 115, 38 }, { 0, 89, 24 },
			{ 0, 102, 255 }, { 99, 51, 102 }, { 255, 191, 191 }, { 255, 213, 128 }, { 0, 255, 102 }, { 0, 61, 153 },
			{ 188, 143, 191 }, { 115, 86, 86 }, { 178, 149, 89 }, { 115, 230, 161 }, { 0, 20, 51 }, { 255, 0, 238 },
			{ 64, 48, 48 }, { 102, 85, 51 }, { 191, 255, 217 }, { 128, 179, 255 }, { 255, 191, 251 }, { 76, 10, 0 },
			{ 204, 187, 153 }, { 115, 153, 130 }, { 83, 116, 166 }, { 77, 57, 75 }, { 255, 89, 64 }, { 255, 204, 0 },
			{ 77, 102, 87 }, { 57, 80, 115 }, { 179, 0, 143 }, { 191, 67, 48 }, { 166, 133, 0 }, { 0, 204, 109 },
			{ 153, 173, 204 }, { 115, 0, 92 }, { 115, 40, 29 }, { 102, 82, 0 }, { 0, 128, 68 }, { 105, 119, 140 },
			{ 64, 0, 51 }, { 217, 123, 108 }, { 217, 184, 54 }, { 0, 51, 27 }, { 0, 68, 255 }, { 230, 57, 195 },
			{ 115, 65, 57 }, { 76, 65, 19 }, { 26, 102, 66 }, { 0, 48, 179 }, { 255, 128, 229 }, { 204, 160, 153 },
			{ 204, 184, 102 }, { 83, 166, 127 }, { 0, 34, 128 }, { 191, 96, 172 }, { 217, 58, 0 }, { 127, 115, 64 },
			{ 153, 204, 180 }, { 0, 24, 89 }, { 77, 38, 69 }, { 127, 34, 0 }, { 255, 242, 191 }, { 0, 255, 170 },
			{ 51, 92, 204 }, { 255, 0, 170 }, { 89, 24, 0 }, { 153, 145, 115 }, { 0, 179, 119 }, { 29, 52, 115 },
			{ 153, 0, 102 }, { 51, 14, 0 }, { 102, 97, 77 }, { 115, 230, 191 }, { 19, 34, 77 }, { 89, 0, 60 },
			{ 255, 115, 64 }, { 64, 61, 48 }, { 0, 255, 204 }, { 128, 162, 255 }, { 153, 77, 128 }, { 166, 75, 41 },
			{ 255, 238, 0 }, { 0, 128, 102 }, { 32, 40, 64 }, { 115, 86, 105 }, { 255, 162, 128 }, { 153, 143, 0 },
			{ 0, 77, 61 }, { 191, 208, 255 }, { 255, 0, 136 }, { 191, 121, 96 }, { 115, 107, 0 }, { 13, 51, 43 },
			{ 57, 62, 77 }, { 178, 0, 95 }, { 140, 89, 70 }, { 51, 48, 13 }, { 57, 115, 103 }, { 0, 27, 204 },
			{ 127, 0, 68 }, { 76, 48, 38 }, { 255, 247, 128 }, { 191, 255, 242 }, { 0, 8, 64 }, { 51, 0, 27 },
			{ 255, 208, 191 }, { 191, 188, 143 }, { 57, 77, 73 }, { 102, 116, 204 }, { 217, 54, 141 },
			{ 153, 125, 115 }, { 238, 255, 0 }, { 0, 255, 238 }, { 143, 150, 191 }, { 255, 128, 196 }, { 255, 102, 0 },
			{ 167, 179, 0 }, { 0, 179, 167 }, { 0, 0, 255 }, { 115, 57, 88 }, { 191, 77, 0 }, { 59, 64, 0 },
			{ 115, 230, 222 }, { 0, 0, 153 }, { 51, 26, 39 }, { 140, 56, 0 }, { 160, 166, 83 }, { 77, 153, 148 },
			{ 0, 0, 140 }, { 255, 191, 225 }, { 64, 26, 0 }, { 74, 77, 38 }, { 153, 204, 201 }, { 0, 0, 128 },
			{ 166, 124, 146 }, { 255, 140, 64 }, { 102, 128, 0 }, { 38, 51, 50 }, { 0, 0, 102 }, { 255, 0, 102 },
			{ 191, 105, 48 }, { 71, 89, 0 }, { 0, 238, 255 }, { 128, 128, 255 }, { 153, 0, 61 }, { 127, 70, 32 },
			{ 195, 230, 57 }, { 0, 179, 191 }, { 77, 77, 153 }, { 102, 0, 41 }, { 89, 49, 22 }, { 229, 255, 128 },
			{ 0, 131, 140 }, { 191, 191, 255 }, { 76, 19, 42 }, { 255, 179, 128 }, { 103, 115, 57 }, { 0, 60, 64 },
			{ 105, 105, 140 }, { 204, 102, 143 }, { 191, 134, 96 }, { 242, 255, 191 }, { 51, 99, 102 }, { 29, 26, 51 },
			{ 255, 0, 68 }, { 140, 98, 70 }, { 170, 255, 0 }, { 115, 150, 153 }, { 121, 96, 191 }, { 166, 0, 44 },
			{ 51, 36, 26 }, { 110, 166, 0 }, { 0, 204, 255 }, { 48, 38, 77 }, { 76, 0, 20 }, { 255, 217, 191 },
			{ 170, 204, 102 }, { 0, 102, 128 }, { 126, 57, 230 }, { 204, 51, 92 }, { 191, 163, 143 }, { 140, 153, 115 },
			{ 48, 163, 191 }, { 84, 38, 153 }, { 255, 128, 162 }, { 115, 98, 86 }, { 34, 64, 0 }, { 22, 76, 89 },
			{ 49, 22, 89 }, { 153, 77, 97 }, { 64, 54, 48 }, { 141, 217, 54 }, { 128, 230, 255 }, { 179, 128, 255 },
			{ 217, 163, 177 }, { 255, 136, 0 }, { 117, 153, 77 }, { 191, 242, 255 }, { 80, 57, 115 }, { 89, 67, 73 },
			{ 191, 102, 0 }, { 39, 51, 26 }, { 0, 170, 255 }, { 163, 143, 191 }, { 217, 0, 29 }, { 140, 75, 0 },
			{ 180, 204, 153 }, { 0, 128, 191 }, { 87, 77, 102 }, { 140, 0, 19 }, { 102, 54, 0 }, { 68, 77, 57 },
			{ 0, 94, 140 }, { 43, 38, 51 }, { 102, 0, 14 }, { 64, 34, 0 }, { 102, 255, 0 }, { 0, 51, 77 },
			{ 136, 0, 255 }, { 217, 54, 76 } };

	static
	{
		final LutColour[] l = LutColour.values();
		luts = new String[l.length];
		for (int i = 0; i < l.length; i++)
			luts[i] = l[i].getName();
	}

	private static LUT fromRGBValues(byte[] r, byte[] g, byte[] b, int[][] values, boolean includeBlack)
	{
		if (values.length != 256)
			throw new RuntimeException("The LUT colours must have 256 values");
		for (int i = (includeBlack) ? 1 : 0; i < 256; i++)
		{
			final int[] rgb = values[i];
			r[i] = (byte) (rgb[0] & 255);
			g[i] = (byte) (rgb[1] & 255);
			b[i] = (byte) (rgb[2] & 255);
		}
		return new LUT(r, g, b);
	}

	/**
	 * Create a colour LUT so that all colours from 1-255 are distinct.
	 *
	 * @return The LUT
	 */
	public static LUT getColorModel()
	{
		// For legacy comptability. There are now other choices in the LutColour enum for distinct colours
		return createLUT(LutColour.DISTINCT);
	}

	/**
	 * Build a custom LUT.
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
	 * Build a custom LUT.
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
		final byte[] reds = new byte[256];
		final byte[] greens = new byte[256];
		final byte[] blues = new byte[256];
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
			case INTENSE:
				return fromRGBValues(reds, greens, blues, intense, includeBlack);
			case PIMP:
				return fromRGBValues(reds, greens, blues, pimp, includeBlack);
			case PIMP_LIGHT:
				return fromRGBValues(reds, greens, blues, pimpLight, includeBlack);
			case DISTINCT:
				return fromRGBValues(reds, greens, blues, distinct, includeBlack);
			case GRAYS:
				nColors = grays(reds, greens, blues);
				break;
		}
		interpolate(reds, greens, blues, nColors, includeBlack);
		return new LUT(reds, greens, blues);
	}

	private static int rainbow(byte[] reds, byte[] greens, byte[] blues)
	{
		// Using HSV vary the Hue from 300 (magenta) to Red (0)
		int n = 0;
		for (int h = 300; h >= 0; h -= 2)
		{
			final Color c = Color.getHSBColor(h / 360.0f, 1, 1);
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

		for (final Color colour : colours)
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
	 *            the reds
	 * @param greens
	 *            the greens
	 * @param blues
	 *            the blues
	 * @return the number of colours
	 */
	private static int ice(byte[] reds, byte[] greens, byte[] blues)
	{
		final int[] r = { 0, 0, 0, 0, 0, 0, 19, 29, 50, 48, 79, 112, 134, 158, 186, 201, 217, 229, 242, 250, 250, 250,
				250, 251, 250, 250, 250, 250, 251, 251, 243, 230 };
		final int[] g = { 156, 165, 176, 184, 190, 196, 193, 184, 171, 162, 146, 125, 107, 93, 81, 87, 92, 97, 95, 93,
				93, 90, 85, 69, 64, 54, 47, 35, 19, 0, 4, 0 };
		final int[] b = { 140, 147, 158, 166, 170, 176, 209, 220, 234, 225, 236, 246, 250, 251, 250, 250, 245, 230, 230,
				222, 202, 180, 163, 142, 123, 114, 106, 94, 84, 64, 26, 27 };
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
	 *            the reds
	 * @param greens
	 *            the greens
	 * @param blues
	 *            the blues
	 * @return the number of colours
	 */
	private static int fire(byte[] reds, byte[] greens, byte[] blues)
	{
		final int[] r = { 0, 0, 1, 25, 49, 73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255,
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255 };
		final int[] g = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205,
				219, 234, 248, 255, 255, 255, 255 };
		final int[] b = { 0, 61, 96, 130, 165, 192, 220, 227, 210, 181, 151, 122, 93, 64, 35, 5, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 35, 98, 160, 223, 255 };
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
	 *            the reds
	 * @param greens
	 *            the greens
	 * @param blues
	 *            the blues
	 * @return the number of colours
	 */
	private static int grays(byte[] reds, byte[] greens, byte[] blues)
	{
		for (int i = 0; i < 256; i++)
		{
			reds[i] = (byte) i;
			greens[i] = (byte) i;
			blues[i] = (byte) i;
		}
		return 256;
	}

	/**
	 * Adapted from ij.plugin.LutLoader to remove the dark colours
	 *
	 * @param reds
	 *            the reds
	 * @param greens
	 *            the greens
	 * @param blues
	 *            the blues
	 * @return the number of colours
	 */
	private static int firelight(byte[] reds, byte[] greens, byte[] blues)
	{
		final int[] r = { //0, 0, 1, 25, 49,
				73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255, 255, 255, 255 };
		final int[] g = { //0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255, 255,
				255, 255 };
		final int[] b = { //0, 61, 96, 130, 165,
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
	 *            the reds
	 * @param greens
	 *            the greens
	 * @param blues
	 *            the blues
	 * @return the number of colours
	 * 
	 */
	private static int fireglow(byte[] reds, byte[] greens, byte[] blues)
	{
		final int[] r = { //0, 0, 1, 25, 49,
				73, 98, 122, 146, 162, 173, 184, 195, 207, 217, 229, 240, 252, 255, 255, 255, 255, 255, 255, 255, 255,
				255, 255, 255
				//, 255, 255, 255
		};
		final int[] g = { //0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0, 0, 14, 35, 57, 79, 101, 117, 133, 147, 161, 175, 190, 205, 219, 234, 248, 255
				//, 255, 255, 255
		};
		final int[] b = { //0, 61, 96, 130, 165,
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
	 * Adapted from ij.plugin.LutLoader.
	 *
	 * @param reds
	 *            the reds
	 * @param greens
	 *            the greens
	 * @param blues
	 *            the blues
	 * @param nColors
	 *            the number of colours
	 * @param includeBlack
	 *            Set to true to include black at index zero
	 */
	private static void interpolate(byte[] reds, byte[] greens, byte[] blues, int nColors, boolean includeBlack)
	{
		// nColors should be at least 2 and max 256.
		if (nColors == 256)
		{
			if (includeBlack)
				reds[0] = greens[0] = blues[0] = 0;
			return;
		}

		// Interpolate so that 0/1 is the first colour (depending on black)
		// and 255 is the final colour.

		// Copy the original input colours
		final byte[] r = new byte[nColors];
		final byte[] g = new byte[nColors];
		final byte[] b = new byte[nColors];
		System.arraycopy(reds, 0, r, 0, nColors);
		System.arraycopy(greens, 0, g, 0, nColors);
		System.arraycopy(blues, 0, b, 0, nColors);

		int total = 256, j = 0;
		if (includeBlack)
			// Check if the input already has black
			if (reds[0] != 0 || greens[0] != 0 || blues[0] != 0)
			{
				// Not black so reduce the interpolation range and make an explicit black.
				total = 255;
				j = 1;
				reds[0] = greens[0] = blues[0] = 0;
			}

		// Bug fix
		// ij.plugin.LutLoader used nColors / 256.0;
		// This made all the colours from 128-255 the same for 2 colour interpolation as i1==i2
		final double scale = (double) (nColors - 1) / total;
		int i1, i2;
		double fraction;
		for (int i = 0; i < total; i++, j++)
		{
			i1 = (int) (i * scale);
			i2 = i1 + 1;
			if (i2 == nColors)
				i2 = nColors - 1;
			fraction = i * scale - i1;
			//IJ.log(i+" "+i1+" "+i2+" "+fraction);
			reds[j] = (byte) ((1.0 - fraction) * (r[i1] & 255) + fraction * (r[i2] & 255));
			greens[j] = (byte) ((1.0 - fraction) * (g[i1] & 255) + fraction * (g[i2] & 255));
			blues[j] = (byte) ((1.0 - fraction) * (b[i1] & 255) + fraction * (b[i2] & 255));
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
			// Assume 8-bit image
			return getColour(lut, n);

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

		final double scale = 256.0 / (maximum - minimum + 1);
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

		final float scale = 255f / (maximum - minimum);
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
			// Assume 8-bit image
			return getColour(lut, n);

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

		final double scale = 255.0 / (maximum - minimum + 1);
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

		final float scale = 254f / (maximum - minimum);
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
		 * Gets the min value output by {@link #map(float)}.
		 *
		 * @return the min value
		 */
		public int getMin();

		/**
		 * Gets the max value output by {@link #map(float)}.
		 *
		 * @return the max value
		 */
		public int getMax();
	}

	/**
	 * Provide no mapping.
	 */
	public static class NullLUTMapper implements LUTMapper
	{

		/**
		 * Rounds the input to the nearest int and truncates to the range 0-255.
		 *
		 * @param value
		 *            the value
		 * @return the int
		 * @see ij.process.LUTHelper.LUTMapper#map(float)
		 */
		@Override
		public int map(float value)
		{
			if (value < 0f)
				return 0;
			if (value > 255f)
				return 255;
			return Math.round(value);
		}

		/**
		 * Provide no mapping (returns the input value).
		 *
		 * @param value
		 *            the value
		 * @return the float
		 * @see ij.process.LUTHelper.LUTMapper#mapf(float)
		 */
		@Override
		public float mapf(float value)
		{
			return value;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ij.process.LUTHelper.LUTMapper#getColour(ij.process.LUT, float)
		 */
		@Override
		public Color getColour(LUT lut, float value)
		{
			return new Color(lut.getRGB(map(value)));
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ij.process.LUTHelper.LUTMapper#getMin()
		 */
		@Override
		public int getMin()
		{
			return 0;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ij.process.LUTHelper.LUTMapper#getMax()
		 */
		@Override
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
		/** The minimum. */
		final float minimum;
		/** The maximum. */
		final float maximum;
		/** The scale. */
		final float scale;

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
		@Override
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
		@Override
		public float mapf(float value)
		{
			return map(value);
		}
	}

	/**
	 * Provide a default map for a value to the range 1-255.
	 */
	public static class NonZeroLUTMapper extends NullLUTMapper
	{
		/** The minimum. */
		final float minimum;
		/** The maximum. */
		final float maximum;
		/** The scale. */
		final float scale;

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
		@Override
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
		@Override
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

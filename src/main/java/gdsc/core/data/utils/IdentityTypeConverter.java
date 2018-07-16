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
package gdsc.core.data.utils;

/**
 * Perform no conversion
 *
 * @param <T> the generic type
 */
public class IdentityTypeConverter<T> extends AbstractTypeConverter<T>
{
	/**
	 * Instantiates a new identity unit converter.
	 *
	 * @param units
	 *            the units (can be null)
	 */
	public IdentityTypeConverter(T units)
	{
		super(units, units, true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.units.UnitConverter#convert(double)
	 */
	@Override
	public double convert(double value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.data.utils.Converter#convert(float)
	 */
	@Override
	public float convert(float value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.data.utils.Converter#convertBack(double)
	 */
	@Override
	public double convertBack(double value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.data.utils.Converter#convertBack(float)
	 */
	@Override
	public float convertBack(float value)
	{
		return value;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.data.utils.Converter#getFunction()
	 */
	@Override
	public String getFunction()
	{
		return "x";
	}
}

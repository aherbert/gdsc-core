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

import gdsc.core.utils.Maths;

/**
 * Perform conversion by multiplication
 *
 * @param <T> the generic type
 */
public class MultiplyTypeConverter<T> extends AbstractTypeConverter<T>
{
	/** The multiplication. */
	protected final double multiplication;

	/**
	 * Instantiates a new multiplication unit converter.
	 *
	 * @param from
	 *            unit to convert from
	 * @param to
	 *            unit to convert to
	 * @param multiplication
	 *            the multiplication
	 * @throws ConversionException
	 *             If the input units are null
	 * @throws ConversionException
	 *             If the multiplication is not finite
	 */
	public MultiplyTypeConverter(T from, T to, double multiplication)
	{
		super(from, to);
		if (!Maths.isFinite(multiplication))
			throw new ConversionException("multiplication must be finite");
		this.multiplication = multiplication;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.units.UnitConverter#convert(double)
	 */
	@Override
	public double convert(double value)
	{
		return value * multiplication;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.data.utils.Converter#convertBack(double)
	 */
	@Override
	public double convertBack(double value)
	{
		return value / multiplication;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.data.utils.Converter#getFunction()
	 */
	@Override
	public String getFunction()
	{
		return "x * " + multiplication;
	}
}

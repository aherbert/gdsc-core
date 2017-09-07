package gdsc.core.data.utils;

import gdsc.core.utils.Maths;

/*----------------------------------------------------------------------------- 
 * GDSC Software
 * 
 * Copyright (C) 2017 Alex Herbert
 * Genome Damage and Stability Centre
 * University of Sussex, UK
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *---------------------------------------------------------------------------*/

/**
 * Perform conversion by multiplication
 */
public class MultiplyTypeConverter<T> extends AbstractTypeConverter<T>
{
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
	public double convert(double value)
	{
		return value * multiplication;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.utils.Converter#convertBack(double)
	 */
	public double convertBack(double value)
	{
		return value / multiplication;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.data.utils.Converter#getFunction()
	 */
	public String getFunction()
	{
		return "x * " + multiplication;
	}
}
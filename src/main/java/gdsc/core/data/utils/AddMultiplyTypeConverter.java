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
 * Perform conversion by addition then multiplication
 */
public class AddMultiplyTypeConverter<T> extends MultiplyTypeConverter<T>
{
	private final double addition;

	/**
	 * Instantiates a new add then multiplication unit converter.
	 *
	 * @param from
	 *            unit to convert from
	 * @param to
	 *            unit to convert to
	 * @param addition
	 *            the value to add before multiplication
	 * @param multiplication
	 *            the multiplication
	 * @throws ConversionException
	 *             If the input units are null
	 * @throws ConversionException
	 *             If the multiplication is not finite
	 * @throws ConversionException
	 *             If the addition is not finite
	 */
	public AddMultiplyTypeConverter(T from, T to, double addition, double multiplication)
	{
		super(from, to, multiplication);
		if (!Maths.isFinite(addition))
			throw new ConversionException("addition must be finite");
		this.addition = addition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.utils.MultiplyTypeConverter#convert(double)
	 */
	public double convert(double value)
	{
		return (value + addition) * multiplication;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.core.data.utils.MultiplyTypeConverter#convertBack(double)
	 */
	public double convertBack(double value)
	{
		return (value / multiplication) - addition;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gdsc.smlm.data.utils.Converter#getFunction()
	 */
	public String getFunction()
	{
		return "(x + " + addition + ") * " + multiplication;
	}
}
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
 * Base class for converters.
 *
 * @param <T> the generic type
 */
public abstract class AbstractTypeConverter<T> implements TypeConverter<T>
{
	private final T from, to;

	/**
	 * Instantiates a new abstract unit converter.
	 *
	 * @param from
	 *            unit to convert from
	 * @param to
	 *            unit to convert to
	 * @throws ConversionException
	 *             If the input units are null
	 */
	public AbstractTypeConverter(T from, T to)
	{
		if (from == null)
			throw new ConversionException("From unit is null");
		if (to == null)
			throw new ConversionException("To unit is null");
		this.from = from;
		this.to = to;
	}

	/**
	 * Instantiates a new abstract unit converter.
	 *
	 * @param from
	 *            unit to convert from
	 * @param to
	 *            unit to convert to
	 * @param suppressExceptions
	 *            the suppress exceptions flag
	 * @throws ConversionException
	 *             If the input units are null (and exception are not suppressed)
	 */
	AbstractTypeConverter(T from, T to, boolean suppressExceptions)
	{
		if (from == null && !suppressExceptions)
			throw new ConversionException("From unit is null");
		if (to == null && !suppressExceptions)
			throw new ConversionException("To unit is null");
		this.from = from;
		this.to = to;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.data.utils.Converter#convert(float)
	 */
	@Override
	public float convert(float value)
	{
		return (float) convert((double) value);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.core.data.utils.Converter#convertBack(float)
	 */
	@Override
	public float convertBack(float value)
	{
		return (float) convertBack((double) value);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.units.UnitConverter#from()
	 */
	@Override
	public T from()
	{
		return from;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see gdsc.smlm.units.UnitConverter#to()
	 */
	@Override
	public T to()
	{
		return to;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return to + " = f(x=" + from + ") = " + getFunction();
	}
}

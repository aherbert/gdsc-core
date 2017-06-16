package gdsc.core.data;

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
 * Exception to throw if data is invalid
 */
public class DataException extends RuntimeException
{
	private static final long serialVersionUID = 3748233784596729168L;

	public DataException()
	{
		super();
	}

	public DataException(String message)
	{
		super(message);
	}

	public DataException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public DataException(Throwable cause)
	{
		super(cause);
	}
}

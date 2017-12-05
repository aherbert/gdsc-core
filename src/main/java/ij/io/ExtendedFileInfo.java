package ij.io;

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
 * Extend the FileInfo to add extra fields for the FastTiffDecoder
 */
public class ExtendedFileInfo extends FileInfo
{
	/**
	 * Contains summary meta data (i.e. for non ImageJ applications such as MicroManager). This is expected to be used
	 * only for the first IFD in a TIFF file.
	 */
	public String summaryMetaData;
	/** Contains extended meta data for the image (i.e. for non ImageJ applications such as MicroManager) */
	public String extendedMetaData;
}

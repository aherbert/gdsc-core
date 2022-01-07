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
 * Copyright (C) 2011 - 2022 Alex Herbert
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

package uk.ac.sussex.gdsc.core.ij.io;

import ij.io.FileInfo;

/**
 * Extend the {@link ij.io.FileInfo} to add extra fields for the {@link FastTiffDecoder}.
 */
public class ExtendedFileInfo extends FileInfo {
  /**
   * Contains summary meta data (i.e. for non ImageJ applications such as MicroManager). This is
   * expected to be used only for the first IFD in a TIFF file.
   */
  private String summaryMetaData;
  /**
   * Contains extended meta data for the image (i.e. for non ImageJ applications such as
   * MicroManager)
   */
  private String extendedMetaData;

  /**
   * Gets the summary meta data.
   *
   * <p>Contains summary meta data (i.e. for non ImageJ applications such as MicroManager). This is
   * expected to be used only for the first IFD in a TIFF file.
   *
   * @return the summary meta data
   */
  public String getSummaryMetaData() {
    return summaryMetaData;
  }

  /**
   * Sets the summary meta data.
   *
   * @param summaryMetaData the new summary meta data
   */
  public void setSummaryMetaData(String summaryMetaData) {
    this.summaryMetaData = summaryMetaData;
  }

  /**
   * Gets the extended meta data.
   *
   * <p>Contains extended meta data for the image (i.e. for non ImageJ applications such as
   * MicroManager).
   *
   * @return the extended meta data
   */
  public String getExtendedMetaData() {
    return extendedMetaData;
  }

  /**
   * Sets the extended meta data.
   *
   * @param extendedMetaData the new extended meta data
   */
  public void setExtendedMetaData(String extendedMetaData) {
    this.extendedMetaData = extendedMetaData;
  }
}

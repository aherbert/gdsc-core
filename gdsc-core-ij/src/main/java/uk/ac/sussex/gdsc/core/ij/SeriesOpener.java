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

package uk.ac.sussex.gdsc.core.ij;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.io.Opener;
import ij.plugin.FolderOpener;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.TextEvent;
import java.io.File;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import uk.ac.sussex.gdsc.core.data.VisibleForTesting;
import uk.ac.sussex.gdsc.core.utils.AlphaNumericComparator;
import uk.ac.sussex.gdsc.core.utils.TextUtils;

/**
 * Opens a series of images in a folder. The series is sorted numerically.
 *
 * <p>Adapted from {@link ij.plugin.FolderOpener}.
 */
public class SeriesOpener {
  private static final String[] EMPTY_LIST = new String[0];

  private final String path;
  private String[] imageList = EMPTY_LIST;
  private int currentImage;
  private int width = -1;
  private int height = -1;
  private boolean variableSize;
  private String helpUrl;

  // Used to filter the image list
  private int maximumNumberOfImages;
  private int start;
  private int increment;
  private String filter;
  private boolean isRegex;

  /**
   * Create an opener with the given path.
   *
   * @param path the path
   */
  public SeriesOpener(String path) {
    this.path = path;
    buildImageList();
  }

  /**
   * Create an opener with the given path.
   *
   * @param path the path
   * @param showDialog Open a dialog and allow the user to filter the images
   * @return the series opener
   */
  public static SeriesOpener create(String path, boolean showDialog) {
    return create(path, showDialog, null);
  }

  /**
   * Create an opener with the given path.
   *
   * @param path the path
   * @param showDialog Open a dialog and allow the user to filter the images
   * @param helpUrl the help url (null to ignore)
   * @return the series opener
   */
  public static SeriesOpener create(String path, boolean showDialog, String helpUrl) {
    final SeriesOpener opener = new SeriesOpener(path);
    opener.helpUrl = helpUrl;
    if (showDialog) {
      opener.showFilterDialog();
    }
    return opener;
  }

  private void buildImageList() {
    final String directory = path;
    if (directory == null) {
      return;
    }

    // Get a list of files
    final File[] fileList = (new File(directory)).listFiles();
    if (fileList == null) {
      return;
    }

    // Exclude directories
    String[] list = new String[fileList.length];
    int count = 0;
    for (int i = 0; i < list.length; i++) {
      if (fileList[i].isFile()) {
        list[count++] = fileList[i].getName();
      }
    }
    if (count == 0) {
      return;
    }
    list = Arrays.copyOf(list, count);

    // Now exclude non-image files as per the ImageJ FolderOpener
    final FolderOpener fo = new FolderOpener();
    list = fo.trimFileList(list);
    if (list == null) {
      return;
    }

    Arrays.sort(list, AlphaNumericComparator.NULL_IS_MORE_INSTANCE);
    imageList = list;
  }

  /**
   * Returns the number of images in the series. Note that the number is based on a list of
   * filenames; each image is only opened with the nextImage() function.
   *
   * @return The number of images in the series
   */
  public int getNumberOfImages() {
    return imageList.length;
  }

  /**
   * Returns the path to the directory containing the images.
   *
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Returns the names of the images in the series.
   *
   * @return The names of the image files
   */
  public String[] getImageList() {
    return imageList.clone();
  }

  /**
   * Get the next image in the series (or null if no more images).
   *
   * <p>Only images that match the width and height of the first image are returned.
   *
   * @return The next image in the series
   */
  public ImagePlus nextImage() {
    ImagePlus imp = null;
    while (currentImage < imageList.length && imp == null) {
      ImageJUtils.showSlowProgress(currentImage, imageList.length);
      imp = openImage(imageList[currentImage++]);
      if (currentImage == imageList.length) {
        ImageJUtils.clearSlowProgress();
      }
    }
    return imp;
  }

  private ImagePlus openImage(String filename) {
    final Opener opener = new Opener();
    opener.setSilentMode(true);
    ImagePlus imp = opener.openImage(path, filename);
    if (imp != null) {
      // Initialise dimensions using first image
      if (width == -1) {
        width = imp.getWidth();
        height = imp.getHeight();
      }

      // Check dimensions
      if (!variableSize && (width != imp.getWidth() || height != imp.getHeight())) {
        imp = null;
      }
    }
    return imp;
  }

  /**
   * Open the first image and show a dialog allowing the series to be filtered.
   */
  private void showFilterDialog() {
    final String[] list = imageList;

    final ImagePlus imp = nextImage();

    // Reset image list
    currentImage = 0;
    imageList = EMPTY_LIST;

    if (showDialog(imp, list)) {
      filterImageList(list, maximumNumberOfImages, start, increment, filter, isRegex, IJ::error,
          (filteredList, size) -> imageList = Arrays.copyOf(filteredList, size));
    }
  }

  /**
   * Filter the image list.
   *
   * @param list the list
   * @param maximumNumberOfImages the maximum number of images
   * @param start the start
   * @param increment the increment
   * @param filter the filter
   * @param isRegex true if the filter is a regular expression
   * @param errorMessageAction the error message action
   * @param listAction the list action
   */
  @VisibleForTesting
  static void filterImageList(String[] list, int maximumNumberOfImages, int start, int increment,
      String filter, boolean isRegex, Consumer<String> errorMessageAction,
      ObjIntConsumer<String[]> listAction) {
    // Filter by name
    if (filter != null && (filter.equals("") || filter.equals("*"))) {
      filter = null;
    }
    if (filter != null) {
      int size = list.length;
      if (isRegex) {
        Pattern pattern;
        try {
          pattern = Pattern.compile(filter);
        } catch (PatternSyntaxException ex) {
          errorMessageAction.accept(ex.getMessage());
          return;
        }
        list = Arrays.stream(list).filter(s -> pattern.matcher(s).matches()).toArray(String[]::new);
      } else {
        final String text = filter;
        list = Arrays.stream(list).filter(s -> s.contains(text)).toArray(String[]::new);
      }
      if (list.length == 0) {
        if (isRegex) {
          errorMessageAction
              .accept("0/" + TextUtils.pleural(size, "file") + " match the regular expression.");
        } else {
          errorMessageAction.accept(
              "0/" + TextUtils.pleural(size, "file") + " contain '" + filter + "' in their name.");
        }
        return;
      }
    }

    // Process only the requested number of images
    if (maximumNumberOfImages < 1) {
      maximumNumberOfImages = list.length;
    }
    if (start < 1 || start > list.length) {
      start = 1;
    }
    increment = Math.max(1, increment);

    if (start == 1 && increment == 1 && maximumNumberOfImages == list.length) {
      listAction.accept(list, list.length);
      return;
    }

    final String[] imageList = new String[list.length];
    int count = 0;
    for (int i = start - 1; i < list.length && count < maximumNumberOfImages;
        i += increment, count++) {
      imageList[count] = list[i];
    }

    listAction.accept(imageList, count);
  }

  private boolean showDialog(ImagePlus imp, String[] list) {
    if (imp == null) {
      return false;
    }
    final int fileCount = list.length;
    final SeriesOpenerDialog gd = new SeriesOpenerDialog("Sequence Options", imp, list);
    gd.addMessage(
        "Folder: " + path + "\nFirst image: " + imp.getOriginalFileInfo().fileName + "\nWidth: "
            + imp.getWidth() + "\nHeight: " + imp.getHeight() + "\nFrames: " + imp.getStackSize());
    gd.addNumericField("Number of images:", fileCount, 0);
    gd.addNumericField("Starting image:", 1, 0);
    gd.addNumericField("Increment:", 1, 0);
    gd.addStringField("File name contains:", "", 10);
    gd.addStringField("or enter pattern:", "", 10);
    gd.addMessage("[info...]");
    gd.addHelp(helpUrl);
    gd.showDialog();
    if (gd.wasCanceled()) {
      return false;
    }
    maximumNumberOfImages = (int) gd.getNextNumber();
    start = (int) gd.getNextNumber();
    increment = (int) gd.getNextNumber();
    filter = gd.getNextString();
    final String regex = gd.getNextString();
    final boolean isRegex = !regex.isEmpty();
    if (isRegex) {
      filter = regex;
    }
    return true;
  }

  /**
   * Set to true to allow subsequent images after the first to have different XY dimensions.
   *
   * @param variableSize True for variable size images
   */
  public void setVariableSize(boolean variableSize) {
    this.variableSize = variableSize;
  }

  /**
   * A dialog to present the options for reading a series of images.
   */
  private static class SeriesOpenerDialog extends GenericDialog {
    private static final long serialVersionUID = 7944532917923080862L;
    transient ImagePlus imp;
    String[] list;

    SeriesOpenerDialog(String title, ImagePlus imp, String[] list) {
      super(title);
      this.imp = imp;
      this.list = list;
    }

    @Override
    protected void setup() {
      setStackInfo();
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
      // Do nothing
    }

    @Override
    public void textValueChanged(TextEvent event) {
      setStackInfo();
    }

    void setStackInfo() {
      final int maximumNumberOfImages = getNumber(numberField.elementAt(0));
      final int start = getNumber(numberField.elementAt(1));
      final int increment = getNumber(numberField.elementAt(2));
      String filter = ((TextField) stringField.elementAt(0)).getText();
      final String regex = ((TextField) stringField.elementAt(1)).getText();
      final boolean isRegex = !regex.isEmpty();
      if (isRegex) {
        filter = regex;
      }

      // Apply the same filtering
      filterImageList(list, maximumNumberOfImages, start, increment, filter, isRegex, IJ::log,
          (filteredList, size) -> {
            ((Label) theLabel).setText(String.format("%s (%s)", TextUtils.pleural(size, "image"),
                TextUtils.pleural(imp.getStackSize() * size, "frame")));
          });
    }

    /**
     * Gets the number.
     *
     * @param field the field
     * @return the number
     */
    public int getNumber(Object field) {
      final TextField tf = (TextField) field;
      try {
        return Integer.parseInt(tf.getText());
      } catch (final NumberFormatException ex) {
        // Not an integer
      }
      return 0;
    }
  }
}

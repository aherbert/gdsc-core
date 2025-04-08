/*-
 * #%L
 * Genome Damage and Stability Centre Core ImageJ Package
 *
 * Contains core utilities for image analysis in ImageJ and is used by:
 *
 * GDSC ImageJ Plugins - Microscopy image analysis
 *
 * GDSC SMLM ImageJ Plugins - Single molecule localisation microscopy (SMLM)
 * %%
 * Copyright (C) 2011 - 2025 Alex Herbert
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

import ij.Prefs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Logger;
import uk.ac.sussex.gdsc.core.ij.gui.ExtendedGenericDialog;
import uk.ac.sussex.gdsc.core.utils.TextUtils;

/**
 * Provide a global reference to a analytics client for tracking GDSC ImageJ plugins.
 */
public final class ImageJAnalyticsUtils {
  /**
   * Analytics application name.
   */
  static final String APPLICATION_NAME = "GDSC ImageJ Plugins";

  // Note:
  // The following constants are used to store user preferences for Google Analytics tracking.
  // Google Analytics is no longer used. Google's universal analytics (UA tacking IDs)
  // is due to be decommissioned in July-2023. These legacy settings are maintained to allow
  // retrieval in the event of any GDPR personal data request. Note that no personal data
  // was ever collected locally. Data sent to Google Analytics was a random client ID,
  // information on ImageJ and Java versions, and the plugin names executed by the user.
  // This is similar to anonymous browser data when a client views a web page (hence why
  // a web analytics provider can be used). The client ID can be used to link users to data
  // stored by Google which may have stored IP address information.

  // gdsc.ga.clientId
  // Store the user's client Id. This allows tracking repeat sessions.
  // This was randomly generated using java.util.UUID.randomUUID().toString().

  // If a new provider is added then the opt in/out status for the new provider will have to
  // be re-registered using new properties.

  /**
   * Store the user's opt in/out state. This prevents asking each time they start a new session.
   */
  private static final String PROPERTY_GA_STATE = "gdsc.ga.state";
  /**
   * Store the version of the code when the opt in/out state decision was made. This allows us to
   * re-ask the question if the code has had significant changes.
   */
  private static final String PROPERTY_GA_LAST_VERSION = "gdsc.ga.lastVersion";
  /**
   * Disabled state flag.
   */
  private static final int DISABLED = -1;
  /**
   * Flag to use when the state is unknown. The user should be asked to opt-in to analytics.
   */
  private static final int UNKNOWN = 0;
  /**
   * Enabled state flag.
   */
  private static final int ENABLED = 1;

  /** This page describes the usage tracking in more detail. */
  private static final String TRACKING_HELP_URL =
      "http://www.sussex.ac.uk/gdsc/intranet/microscopy/UserSupport/AnalysisProtocol/imagej/tracking/";
  /** The help label text. */
  private static final String HELP_LABEL = "More details...";
  /**
   * The comment character in the ImageJ plugins file.
   */
  private static final char COMMENT_CHAR = '#';
  /**
   * The number of entries in the ImageJ plugins file. Plugins have [Menu path, Name,
   * class(argument)].
   */
  private static final int PLUGIN_MAP_ENTRIES = 3;

  /**
   * Flag indicating that the user has opted in/out of analytics.
   *
   * <p>It is not vital that this is synchronised.
   */
  private static int state = (int) Prefs.get(PROPERTY_GA_STATE, UNKNOWN);

  /** No public construction. */
  private ImageJAnalyticsUtils() {}

  /**
   * Track a page view.
   *
   * @param documentPath The document path (must not be null)
   * @param documentTitle The document title
   */
  public static void pageview(String documentPath, String documentTitle) {
    // No analytics
  }

  /**
   * Sanitise the document path. Ensures a forward slash '/' is prepended to the path if missing.
   *
   * <p>A null or empty path returns "/".
   *
   * @param documentPath the document path
   * @return the sanitised document path
   */
  public static String sanitisePath(String documentPath) {
    if (TextUtils.isNullOrEmpty(documentPath)) {
      return "/";
    }
    return (documentPath.charAt(0) == '/') ? documentPath : "/" + documentPath;
  }

  /**
   * Provide a method to read an ImageJ properties file and create the map between the ImageJ plugin
   * class and argument and the ImageJ menu path and plugin title.
   *
   * @param map The map object to populate
   * @param pluginsStream The ImageJ properties file
   * @param charset the charset
   */
  public static void buildPluginMap(Map<String, String[]> map, InputStream pluginsStream,
      Charset charset) {
    try (BufferedReader input = new BufferedReader(new InputStreamReader(pluginsStream, charset))) {
      String line;
      while ((line = input.readLine()) != null) {
        if (line.isEmpty() || line.charAt(0) == COMMENT_CHAR) {
          continue;
        }
        final String[] tokens = line.split(",");
        if (tokens.length == PLUGIN_MAP_ENTRIES) {
          // Plugins have [Menu path, Name, class(argument)], e.g.
          // Plugins>GDSC>Colocalisation, "CDA (macro)", gdsc.colocalisation.cda.CDA_Plugin("macro")

          final String documentTitle = tokens[1].replaceAll("[\"']", "").trim();
          final String documentPath = getDocumentPath(tokens[0], documentTitle);
          final String key = getKey(tokens[2]);
          map.put(key, new String[] {documentPath, documentTitle});
        }
      }
    } catch (final IOException ex) {
      Logger.getLogger(ImageJAnalyticsUtils.class.getName())
          .warning(() -> "Failed to read the plugin map: " + ex.getMessage());
    }
  }

  /**
   * Split the menu path string and create a document path.
   *
   * @param menuPath The ImageJ menu path string
   * @param documentTitle the document title
   * @return The document path
   */
  private static String getDocumentPath(String menuPath, String documentTitle) {
    final StringBuilder sb = new StringBuilder();
    for (final String field : menuPath.split(">")) {
      sb.append('/').append(field.trim());
    }
    sb.append('/').append(documentTitle);
    return sb.toString();
  }

  /**
   * Get the raw class name and string argument from the ImageJ 'org.package.Class("argument")'
   * field
   *
   * @param string The field contents
   * @return The hash key
   */
  private static String getKey(String string) {
    String name = string.trim();
    String argument = null;
    final int index = name.indexOf('(');
    if (index != -1) {
      // Get the remaining text and remove the quotes " and brackets ()
      argument = name.substring(index).replaceAll("[\"'()]", "").trim();
      // Get the class name
      name = name.substring(0, index);
    }
    return getKey(name, argument);
  }

  /**
   * Get the key used for the given name and argument in the plugin map.
   *
   * @param name the name
   * @param argument the argument
   * @return The key
   */
  public static String getKey(String name, String argument) {
    return (argument != null && argument.length() > 0) ? name + '.' + argument : name;
  }

  /**
   * Checks if is disabled.
   *
   * <p>Currently no tracking code is used so this always returns false.
   *
   * @return True if analytics is disabled.
   */
  public static boolean isDisabled() {
    // Analytics is currently disabled.
    // Future versions may add a new analytics provider.
    return true;
  }

  /**
   * Set the state of the analytics tracker.
   *
   * @param disabled True to disable analytics
   */
  public static void setDisabled(boolean disabled) {
    final int oldState = ImageJAnalyticsUtils.state;
    ImageJAnalyticsUtils.state = (disabled) ? DISABLED : ENABLED;

    Prefs.set(PROPERTY_GA_LAST_VERSION, getVersion());

    if (oldState != state) {
      Prefs.set(PROPERTY_GA_STATE, state);

      // *** Requires an analytics provider ***

      // Record this opt in/out status change

      // If the state was previously unknown and they opt out then do nothing.
      // This is a user who never wants to be tracked.

      // Otherwise record the in/out status change, either:
      // - The user was previously opt in but now opts out; or
      // - The user was previously opt out but now opts in

      // Reset the session if tracking has been enabled.

      // Track the opt status change with an event
    }
  }

  /**
   * Gets the version of the code.
   *
   * @return The version of the code.
   */
  private static String getVersion() {
    // For legacy purposes use the most recent value of GDSC Analytics
    return "2.0";
  }

  /**
   * Check the opt-in/out status. If it is not known for this version of the analytics code then
   * return true.
   *
   * @return true, if the current opt-in/out status is unknown.
   */
  public static boolean unknownStatus() {
    final String lastVersion = Prefs.get(PROPERTY_GA_LAST_VERSION, "");
    final String thisVersion = getVersion();
    return (state == UNKNOWN || !lastVersion.equals(thisVersion));
  }

  /**
   * Show a dialog allowing users to opt in/out of analytics.
   *
   * <p>Currently no tracking code is used. This dialog serves to provide a link to a web page
   * with more details.
   *
   * @param title The dialog title
   * @param autoMessage Set to true to display the message about automatically showing when the
   *        status is unknown
   * @param helpUrl the help url (null to use the default)
   */
  public static void showDialog(String title, boolean autoMessage, String helpUrl) {
    final ExtendedGenericDialog gd = new ExtendedGenericDialog(title);
    // @formatter:off
    gd.addMessage(
        APPLICATION_NAME +
        "\n \n" +
        "No usage tracking is performed." +
        "\n \n" +
        "We previously used opt-in analytics code to record GDSC plugin names\n" +
        "similar to anonymous browser web page views.");
    // @formatter:on

    gd.hideCancelButton();

    gd.addAndGetButton(HELP_LABEL, event -> {
      final String url = TextUtils.isNotEmpty(helpUrl) ? helpUrl : TRACKING_HELP_URL;
      ImageJUtils.showUrl(url);
    });

    gd.showDialog();
  }
}

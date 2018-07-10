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
package gdsc.core.ij;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;

import gdsc.analytics.ClientParameters;
import gdsc.analytics.ClientParametersManager;
import gdsc.analytics.HitType;
import gdsc.analytics.JGoogleAnalyticsTracker;
import gdsc.analytics.JGoogleAnalyticsTracker.DispatchMode;
import gdsc.analytics.JGoogleAnalyticsTracker.MeasurementProtocolVersion;
import gdsc.analytics.RequestParameters;
import ij.IJ;
import ij.ImageJ;
import ij.Prefs;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.macro.MacroRunner;

/**
 * Provide a global reference to JGoogleAnalyticsTracker for tracking GDSC ImageJ plugins
 */
public class ImageJAnalyticsTracker
{
	/**
	 * Google Analytics (GA) tracking Id.
	 * <p>
	 * This is the GA property which is used to track all the user activity for
	 * the GDSC ImageJ plugins.
	 * <p>
	 * <b>If you copy this code then please use your own tracking Id!</b>
	 */
	private static final String GA_TRACKING_ID = "UA-74666243-1";
	/**
	 * Google Analytics (GA) application name.
	 * <p>
	 * This is the application name for GA. It doesn't really matter what the value is but it is a required field within
	 * GA.
	 */
	private static final String APPLICATION_NAME = "GDSC ImageJ Plugins";

	// The following constants are used to store user preferences for Google Analytics tracking

	/**
	 * Store the user's client Id. This allows tracking repeat sessions.
	 */
	private static final String PROPERTY_GA_CLIENT_ID = "gdsc.ga.clientId";
	/**
	 * Store the user's opt in/out state. This prevents asking each time they start a new session.
	 */
	private static final String PROPERTY_GA_STATE = "gdsc.ga.state";
	/**
	 * Store the version of the code when the opt in/out state decision was made. This allows us to re-ask the question
	 * if the code has had significant changes.
	 */
	private static final String PROPERTY_GA_LAST_VERSION = "gdsc.ga.lastVersion";
	/**
	 * Store the user's decision to use an anonymous IP address.
	 */
	private static final String PROPERTY_GA_ANONYMIZE = "gdsc.ga.anonymize";
	/**
	 * Disabled state flag
	 */
	private static final int DISABLED = -1;
	/**
	 * Flag to use when the state is unknown
	 */
	private static final int UNKNOWN = 0;
	/**
	 * Enabled state flag
	 */
	private static final int ENABLED = 1;

	private static ClientParameters clientParameters = null;
	private static JGoogleAnalyticsTracker tracker = null;
	private static boolean loggedPreferrences = false;

	/**
	 * Flag indicating that the user has opted in/out of analytics
	 */
	private static int state = (int) Prefs.get(PROPERTY_GA_STATE, UNKNOWN);
	/**
	 * A flag indicating if the anonymise option is available.
	 * If set to false then all personal data is anonymised by default.
	 * <p>
	 * Note: To comply with GDPR any personal data should be anonymised
	 */
	private static boolean anonymiseOption = false;
	/**
	 * Flag indicating that the IP address of the sender will be anonymized
	 * 
	 * @deprecated To comply with GDPR any personal data should be anonymized
	 */
	@Deprecated
	private static int anonymized = (int) Prefs.get(PROPERTY_GA_ANONYMIZE, UNKNOWN);

	/**
	 * Track a page view
	 * 
	 * @param documentPath
	 *            The document path (must not be null)
	 * @param documentTitle
	 *            The document title
	 */
	public static void pageview(String documentPath, String documentTitle)
	{
		initialiseTracker();
		if (isDisabled())
			return;
		// Get the timestamp. This allows asynchronous hits to be recorded at the correct time 
		final long timestamp = System.currentTimeMillis();
		RequestParameters data = new RequestParameters(HitType.PAGEVIEW);
		data.setDocumentPath(documentPath);
		data.setDocumentTitle(documentTitle);
		// Add custom dimensions for ImageJ state.
		data.addCustomMetric(1, (IJ.isMacro()) ? 1 : 0);
		data.addCustomDimension(8, Boolean.toString(IJ.isMacro()));
		tracker.makeCustomRequest(data, timestamp);
	}

	/**
	 * Create the client parameters for the tracker and populate with information about ImageJ and the system
	 */
	public static void initialise()
	{
		if (clientParameters == null)
		{
			synchronized (ImageJAnalyticsTracker.class)
			{
				// In case another thread was waiting to do this
				if (clientParameters != null)
					return;

				// Get the client parameters
				final String clientId = Prefs.get(PROPERTY_GA_CLIENT_ID, null);
				clientParameters = new ClientParameters(GA_TRACKING_ID, clientId, APPLICATION_NAME);

				ClientParametersManager.populate(clientParameters);

				// Record for next time
				Prefs.set(PROPERTY_GA_CLIENT_ID, clientParameters.getClientId());

				// Record the version of analytics we are using
				clientParameters.setApplicationVersion(gdsc.analytics.Version.VERSION_X_X_X);

				// Use custom dimensions to record client data. These should be registered
				// in the analytics account for the given tracking ID

				// Record the ImageJ information.
				clientParameters.addCustomDimension(1, getImageJInfo());

				// Java version
				clientParameters.addCustomDimension(2, System.getProperty("java.version"));

				// OS
				clientParameters.addCustomDimension(3, System.getProperty("os.name"));
				clientParameters.addCustomDimension(4, System.getProperty("os.version"));
				clientParameters.addCustomDimension(5, System.getProperty("os.arch"));

				// Versions
				clientParameters.addCustomDimension(9, gdsc.core.Version.getVersion());
			}
		}
	}

	/**
	 * @return The ImageJ information (Application name and version)
	 */
	private static String getImageJInfo()
	{
		// Avoid ImageJ throwing a HeadlessException
		if (java.awt.GraphicsEnvironment.isHeadless())
			return "Headless"; //+ ImageJ.VERSION+ImageJ.BUILD;

		ImageJ ij = IJ.getInstance();
		if (ij == null)
		{
			// Run embedded without showing
			ij = new ImageJ(ImageJ.NO_SHOW);
		}

		// ImageJ version
		// This call should return a string like:
		//   ImageJ 1.48a; Java 1.7.0_11 [64-bit]; Windows 7 6.1; 29MB of 5376MB (<1%)
		// (This should also be different if we are running within Fiji)
		String info = ij.getInfo();
		if (info.indexOf(';') != -1)
			info = info.substring(0, info.indexOf(';'));
		return info;
	}

	/**
	 * Add a custom dimension
	 * <p>
	 * Note that custom dimensions have to be created for your site before they can be used in analytics reports.
	 * 
	 * @see "https://support.google.com/analytics/answer/2709829"
	 * 
	 * @param index
	 *            The dimension index (1-20 or 1-200 for premium accounts)
	 * @param value
	 *            The dimension value (must not be null)
	 */
	public static void addCustomDimension(int index, String value)
	{
		initialise();
		clientParameters.addCustomDimension(index, value);
	}

	/**
	 * Create the tracker. Call this before sending any requests to Google Analytics.
	 */
	private static void initialiseTracker()
	{
		if (tracker == null)
		{
			synchronized (ImageJAnalyticsTracker.class)
			{
				// Check again since this may be a second thread that was waiting
				if (tracker != null)
					return;

				// Make sure we have created a client
				initialise();

				tracker = new JGoogleAnalyticsTracker(clientParameters, MeasurementProtocolVersion.V_1,
						DispatchMode.SINGLE_THREAD);

				// Following GDPR all personal data is removed so nothing
				// that is sent to Google can be used to identify a data
				// subject (an individual)
				setTrackerAnonymised(isAnonymized());

				// DEBUG: Enable logging
				if (Boolean.parseBoolean(System.getProperty("gdsc-analytics-logger", "false")))
					tracker.setLogger(new gdsc.analytics.ConsoleLogger());
			}
		}
	}

	/**
	 * Provide a method to read an ImageJ properties file and create the map between the ImageJ plugin class and
	 * argument and the ImageJ menu path and plugin title.
	 * 
	 * @param map
	 *            The map object to populate
	 * @param pluginsStream
	 *            The ImageJ properties file
	 */
	public static void buildPluginMap(HashMap<String, String[]> map, InputStream pluginsStream)
	{
		BufferedReader input = null;
		try
		{
			input = new BufferedReader(new InputStreamReader(pluginsStream));
			String line;
			while ((line = input.readLine()) != null)
			{
				if (line.startsWith("#"))
					continue;
				String[] tokens = line.split(",");
				if (tokens.length == 3)
				{
					// Plugins have [Menu path, Name, class(argument)], e.g.
					// Plugins>GDSC>Colocalisation, "CDA (macro)", gdsc.colocalisation.cda.CDA_Plugin("macro")

					String documentTitle = tokens[1].replaceAll("[\"']", "").trim();
					String documentPath = getDocumentPath(tokens[0], documentTitle);
					String key = getKey(tokens[2]);
					map.put(key, new String[] { documentPath, documentTitle });
				}
			}
		}
		catch (IOException e)
		{
			// Ignore
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					// Ignore
				}
			}
		}

	}

	/**
	 * Split the menu path string and create a document path
	 * 
	 * @param menuPath
	 *            The ImageJ menu path string
	 * @param documentTitle
	 * @return The document path
	 */
	private static String getDocumentPath(String menuPath, String documentTitle)
	{
		StringBuilder sb = new StringBuilder();
		for (String field : menuPath.split(">"))
		{
			sb.append('/').append(field.trim());
		}
		sb.append('/').append(documentTitle);
		return sb.toString();
	}

	/**
	 * Get the raw class name and string argument from the ImageJ 'org.package.Class("argument")' field
	 * 
	 * @param string
	 *            The field contents
	 * @return The hash key
	 */
	private static String getKey(String string)
	{
		String name = string.trim(), argument = null;
		final int index = name.indexOf('(');
		if (index != -1)
		{
			// Get the remaining text and remove the quotes " and brackets ()
			argument = name.substring(index).replaceAll("[\"'()]", "").trim();
			// Get the class name
			name = name.substring(0, index);
		}
		return getKey(name, argument);
	}

	/**
	 * Get the key used for the given name and argument in the plugin map
	 * 
	 * @param name
	 * @param argument
	 * @return The key
	 */
	public static String getKey(String name, String argument)
	{
		return (argument != null && argument.length() > 0) ? name + '.' + argument : name;
	}

	/**
	 * @return True if analytics is disabled
	 */
	public static boolean isDisabled()
	{
		return (state == DISABLED);
	}

	/**
	 * Set the state of the analytics tracker
	 * 
	 * @param disabled
	 *            True to disable analytics
	 */
	public static void setDisabled(boolean disabled)
	{
		final int oldState = ImageJAnalyticsTracker.state;
		ImageJAnalyticsTracker.state = (disabled) ? DISABLED : ENABLED;

		Prefs.set(PROPERTY_GA_LAST_VERSION, getVersion());

		if (oldState != state)
		{
			Prefs.set(PROPERTY_GA_STATE, state);

			// Record this opt in/out status change

			// If the state was previously unknown and they opt out then do nothing.
			// This is a user who never wants to be tracked.
			if (oldState == UNKNOWN && disabled)
				return;

			// Otherwise record the in/out status change, either:
			// - The user was previously opt in but now opts out; or
			// - The user was previously opt out but now opts in

			final boolean enabled = !disabled;

			initialiseTracker();
			// Reset the session if tracking has been enabled.
			if (enabled)
				tracker.resetSession();
			// Track the opt status change with an event
			tracker.event("Tracking", Boolean.toString(enabled), getVersion(), null);
		}
	}

	/**
	 * Check if the anonymise option is available.
	 * If set to false then all personal data is anonymised by default and
	 * any use of the anonymised flag will throw an IllegalStateException.
	 * <p>
	 * Note: To comply with GDPR any personal data should be anonymised
	 *
	 * @return true, if is anonymise option
	 */
	public static boolean isAnonymiseOption()
	{
		return anonymiseOption;
	}

	/**
	 * Sets the anonymise option.
	 * If set to false then all personal data is anonymised by default and
	 * any use of the anonymised flag will throw an IllegalStateException.
	 * <p>
	 * Note: To comply with GDPR any personal data should be anonymised
	 *
	 * @param anonymiseOption
	 *            the new anonymise option
	 */
	public static void setAnonymiseOption(boolean anonymiseOption)
	{
		ImageJAnalyticsTracker.anonymiseOption = anonymiseOption;
	}

	/**
	 * @return True if the IP address of the sender will be anonymized
	 * @deprecated To comply with GDPR any personal data should be anonymised
	 */
	@Deprecated()
	public static boolean isAnonymized()
	{
		return anonymiseOption ? (anonymized == ENABLED) : true;
	}

	/**
	 * Set the state of IP anonymization.
	 * <p>
	 * This can only be set if the anonymise option is available.
	 * <p>
	 * If data is not anonymised then a secure connection is used (HTTPS) to send
	 * the personal data.
	 *
	 * @param anonymize
	 *            True if the IP address of the sender will be anonymized
	 * @throws IllegalStateException
	 *             if the option to anonymise data is not configured
	 * @deprecated To comply with GDPR any personal data should be anonymised
	 * @see #isAnonymiseOption()
	 */
	@Deprecated
	public static void setAnonymized(boolean anonymize) throws IllegalStateException
	{
		if (!anonymiseOption)
			throw new IllegalStateException("Optional anonymisation is not available");

		final int oldAnonymized = anonymized;
		anonymized = (anonymize) ? ENABLED : DISABLED;

		Prefs.set(PROPERTY_GA_LAST_VERSION, getVersion());

		if (oldAnonymized != anonymized)
		{
			Prefs.set(PROPERTY_GA_ANONYMIZE, anonymized);

			// Make sure the tracker is informed
			if (tracker != null)
				setTrackerAnonymised(isAnonymized());
		}
	}

	private static void setTrackerAnonymised(boolean anonymize)
	{
		tracker.setAnonymised(anonymize);
		// If not anonymising the IP address then use a secure connection
		tracker.setSecure(!anonymize);
	}

	/**
	 * @return The version of the code
	 */
	private static String getVersion()
	{
		return gdsc.analytics.Version.VERSION_X_X;
	}

	/**
	 * Check the opt-in/out status. If it is not known for this version of the analytics code then return true.
	 *
	 * @return true, if the current opt-in/out status is unknown.
	 */
	public static boolean unknownStatus()
	{
		String lastVersion = Prefs.get(PROPERTY_GA_LAST_VERSION, "");
		String thisVersion = getVersion();
		if (state == UNKNOWN || !lastVersion.equals(thisVersion))
			return true;
		if (isAnonymiseOption() && anonymized == UNKNOWN)
			return true;
		return false;
	}

	/**
	 * Show a dialog allowing users to opt in/out of Google Analytics
	 * 
	 * @param title
	 *            The dialog title
	 * @param autoMessage
	 *            Set to true to display the message about automatically showing
	 *            when the status is unknown
	 */
	public static void showDialog(String title, boolean autoMessage)
	{
		GenericDialog gd = new GenericDialog(title);
		// @formatter:off
		gd.addMessage(
				APPLICATION_NAME + 
				"\n \n" +
				"We use tracking code to make our plugins better.");

		// With ImageJ 1.48a hiding the cancel button means the dialog is disposed when pressing 'Help'.
		// To work around this we add an empty string and our own listener to show the help. ImageJ 
		// should then notify listeners due to the empty help URL.
		final String helpLabel = "More details...";
		gd.setHelpLabel(helpLabel);
		gd.addHelp("");
		gd.addDialogListener(new DialogListener()
		{
			@Override
			public boolean dialogItemChanged(GenericDialog gd, AWTEvent e)
			{
				if (e != null && e.getSource() instanceof Button)
				{
					Button button = (Button) (e.getSource());
					if (button.getLabel().equals(helpLabel))
					{
						String macro = "run('URL...', 'url=" +
								// This page describe the usage tracking in more detail
								"http://www.sussex.ac.uk/gdsc/intranet/microscopy/imagej/tracking" +
								"');";
						new MacroRunner(macro);
					}
				}
				return true;
			}
		});

		boolean anonymiseOption = isAnonymiseOption();
		
		// Get the preferences
		boolean disabled = isDisabled();
		boolean anonymize = isAnonymized();

		// Use Opt-in to make the wording clear
		gd.addCheckbox("Opt in", !disabled);
		if (anonymiseOption)
		{
			gd.addCheckbox("Anonymise IP", anonymize);
		}
		if (autoMessage)
		{
			gd.addMessage(
					"Note: This message is shown when your\n" + 
					"preferences are unknown\n");
		}
		// @formatter:on
		gd.hideCancelButton();

		// Add event listener to disable anonymous checkbox when the user opts out
		if (anonymiseOption)
		{
			@SuppressWarnings("rawtypes")
			Vector checkboxes = gd.getCheckboxes();
			final Checkbox cb1 = (Checkbox) checkboxes.get(0);
			final Checkbox cb2 = (Checkbox) checkboxes.get(1);
			cb2.setEnabled(!disabled);
			cb1.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent e)
				{
					cb2.setEnabled(!cb1.getState());
				}
			});
		}

		gd.showDialog();

		if (!gd.wasCanceled())
		{
			// This will happen if the user clicks OK. 
			disabled = !gd.getNextBoolean();
			if (anonymiseOption)
				anonymize = gd.getNextBoolean();
		}
		else
		{
			// We have hidden the cancel button. 
			// This code will run if:
			// - The user closes the dialog by other means (clicks escape/close window).
			// In this case assume they are happy with the current settings and store 
			// them. This should prevent the dialog being shown again for any code 
			// using the unknownStatus() method.
		}

		if (anonymiseOption)
		{
			// Anonymize first to respect the user choice if they still have tracking on
			setAnonymized(anonymize);
		}
		setDisabled(disabled);
	}

	/**
	 * Write the current user preferences for analytics to the ImageJ log. This log message is written once only unless
	 * the force flag is used.
	 * 
	 * @param force
	 *            Force the preferences to be logged
	 */
	public static void logPreferences(boolean force)
	{
		if (loggedPreferrences && !force)
			return;
		loggedPreferrences = true;
		String onOrOff = (isDisabled()) ? "Off" : "On";
		// Get the preferences
		if (isAnonymiseOption())
			IJ.log(String.format("%s - Google Analytics: %s; Anonymise IP=%b", APPLICATION_NAME, onOrOff,
					isAnonymized()));
		else
			IJ.log(String.format("%s - Google Analytics: %s", APPLICATION_NAME, onOrOff));
		IJ.log("You can change these at any time by running the Usage Tracker plugin");
	}
}

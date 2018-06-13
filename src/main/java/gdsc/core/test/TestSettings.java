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
package gdsc.core.test;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.internal.ComparisonCriteria;

/**
 * Class used to control running and logging within tests.
 * This class is configured once using system properties.
 */
public class TestSettings
{
	/**
	 * The Log Level. Lower levels result is less verbose output.
	 */
	public enum LogLevel
	{
		/** Silent. Use this level to output information even if logging is disabled. */
		SILENT
		{
			@Override
			int getValue()
			{
				return -1;
			}
		},

		/**
		 * Warning logging.
		 * For example this can be used to log speed test results that fail but are not critical.
		 */
		WARN
		{
			@Override
			int getValue()
			{
				return 0;
			}
		},

		/** Information logging. */
		INFO
		{
			@Override
			int getValue()
			{
				return 1;
			}
		},

		/** Debug logging. */
		DEBUG
		{
			@Override
			int getValue()
			{
				return 2;
			}
		};

		/**
		 * Gets the value
		 *
		 * @return the value
		 */
		abstract int getValue();
	}

	/**
	 * The test complexity. Lower complexity are assumed to be faster.
	 */
	public enum TestComplexity
	{
		/** No complexity */
		NONE
		{
			@Override
			int getValue()
			{
				return -1;
			}
		},

		/** Low complexity */
		LOW
		{
			@Override
			int getValue()
			{
				return 0;
			}
		},

		/** Medium complexity */
		MEDIUM
		{
			@Override
			int getValue()
			{
				return 1;
			}
		},

		/** High complexity */
		HIGH
		{
			@Override
			int getValue()
			{
				return 2;
			}
		};

		/**
		 * Gets the value
		 *
		 * @return the value
		 */
		abstract int getValue();
	}

	/**
	 * Provide messages dynamically for logging.
	 */
	public interface MessageProvider
	{
		/**
		 * Gets the message.
		 *
		 * @return the message
		 */
		String getMessage();
	}

	/**
	 * Provide messages for logging. To be used when providing the arguments for the message is computationally intense.
	 */
	public static abstract class Message
	{
		/** The format. */
		final String format;

		/**
		 * Instantiates a new message.
		 *
		 * @param format
		 *            the format
		 */
		public Message(String format)
		{
			this.format = format;
		}

		/**
		 * Helper method to wrap the variable length arguments list for use in {@link #getArgs()}.
		 *
		 * @param args
		 *            the arguments for the message
		 * @return the arguments for the message as an array
		 */
		final public Object[] wrap(Object... args)
		{
			return args;
		}

		/**
		 * Gets the arguments for the message.
		 *
		 * @return the arguments
		 */
		public abstract Object[] getArgs();
	}

	/** The allowed log level. */
	private static int logLevel;

	/** The allowed test complexity */
	private static int testComplexity;

	/** The fixed seed for random generator */
	private static long seed;

	static
	{
		logLevel = LogLevel.SILENT.getValue();
		testComplexity = TestComplexity.NONE.getValue();
		seed = 30051977;

		try
		{
			logLevel = Integer.parseInt(System.getProperty("gdsc.test.logging"));
		}
		catch (Exception e)
		{
		}
		try
		{
			testComplexity = Integer.parseInt(System.getProperty("gdsc.test.level"));
		}
		catch (Exception e)
		{
		}
		try
		{
			seed = Long.parseLong(System.getProperty("gdsc.test.seed"));
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Sets the log level. Package scope for testing.
	 *
	 * @param level
	 *            the new log level
	 */
	static void setLogLevel(LogLevel level)
	{
		logLevel = level.getValue();
	}

	/**
	 * Sets the text complexity. Package scope for testing.
	 *
	 * @param complexity
	 *            the new text complexity
	 */
	static void setTextComplexity(TestComplexity complexity)
	{
		testComplexity = complexity.getValue();
	}

	/**
	 * Log at the warn level.
	 *
	 * @param format
	 *            the format
	 * @param args
	 *            the arguments
	 */
	public static void warn(String format, Object... args)
	{
		log(LogLevel.WARN, format, args);
	}

	/**
	 * Log at the warn level.
	 *
	 * @param msg
	 *            the message
	 */
	public static void warn(MessageProvider msg)
	{
		log(LogLevel.WARN, msg);
	}

	/**
	 * Log at the warn level.
	 *
	 * @param msg
	 *            the message
	 */
	public static void warn(Message msg)
	{
		log(LogLevel.WARN, msg);
	}

	/**
	 * Log at the info level.
	 *
	 * @param format
	 *            the format
	 * @param args
	 *            the arguments
	 */
	public static void info(String format, Object... args)
	{
		log(LogLevel.INFO, format, args);
	}

	/**
	 * Log at the info level.
	 *
	 * @param msg
	 *            the message
	 */
	public static void info(MessageProvider msg)
	{
		log(LogLevel.INFO, msg);
	}

	/**
	 * Log at the info level.
	 *
	 * @param msg
	 *            the message
	 */
	public static void info(Message msg)
	{
		log(LogLevel.INFO, msg);
	}

	/**
	 * Log at the debug level.
	 *
	 * @param format
	 *            the format
	 * @param args
	 *            the arguments
	 */
	public static void debug(String format, Object... args)
	{
		log(LogLevel.DEBUG, format, args);
	}

	/**
	 * Log at the debug level.
	 *
	 * @param msg
	 *            the message
	 */
	public static void debug(MessageProvider msg)
	{
		log(LogLevel.DEBUG, msg);
	}

	/**
	 * Log at the debug level.
	 *
	 * @param msg
	 *            the message
	 */
	public static void debug(Message msg)
	{
		log(LogLevel.DEBUG, msg);
	}

	/**
	 * Log.
	 *
	 * @param level
	 *            the level
	 * @param format
	 *            the format
	 * @param args
	 *            the arguments
	 */
	public static void log(LogLevel level, String format, Object... args)
	{
		if (allow(level))
			System.out.printf(format, args);
	}

	/**
	 * Log.
	 *
	 * @param level
	 *            the level
	 * @param msg
	 *            the message
	 */
	public static void log(LogLevel level, MessageProvider msg)
	{
		if (allow(level))
			System.out.print(msg.getMessage());
	}

	/**
	 * Log.
	 *
	 * @param level
	 *            the level
	 * @param msg
	 *            the message
	 */
	public static void log(LogLevel level, Message msg)
	{
		if (allow(level))
			System.out.printf(msg.format, msg.getArgs());
	}

	/**
	 * Check if logging is allowed at the given level.
	 *
	 * @param level
	 *            the level
	 * @return true, if successful
	 */
	public static boolean allow(LogLevel level)
	{
		return level.getValue() <= logLevel;
	}

	/**
	 * Check if testing is allowed at the given complexity.
	 *
	 * @param omplexity
	 *            the test complexity
	 * @return true, if successful
	 */
	public static boolean allow(TestComplexity complexity)
	{
		return complexity.getValue() <= testComplexity;
	}

	/**
	 * Check if logging and testing is allowed at the given level and complexity.
	 *
	 * @param level
	 *            the level
	 * @param complexity
	 *            the complexity
	 * @return true, if successful
	 */
	public static boolean allow(LogLevel level, TestComplexity complexity)
	{
		return allow(level) && allow(complexity);
	}

	// Helper methods for testing

	/**
	 * Assume logging is allowed at the given level.
	 *
	 * @param level
	 *            the level
	 */
	public static void assume(LogLevel level)
	{
		Assume.assumeTrue(allow(level));
	}

	/**
	 * Assume testing is allowed at the given complexity.
	 *
	 * @param complexity
	 *            the complexity
	 */
	public static void assume(TestComplexity complexity)
	{
		Assume.assumeTrue(allow(complexity));
	}

	/**
	 * Assume logging and testing is allowed at the given level and complexity.
	 *
	 * @param level
	 *            the level
	 * @param complexity
	 *            the complexity
	 */
	public static void assume(LogLevel level, TestComplexity complexity)
	{
		Assume.assumeTrue(allow(level, complexity));
	}

	/**
	 * Gets the random generator. If the seed is 0 then a random seed will be used.
	 *
	 * @param seed
	 *            the seed
	 * @return the random generator
	 */
	public static RandomGenerator getRandomGenerator(long seed)
	{
		return (seed == 0) ? new Well19937c() : new Well19937c(seed);
	}

	/**
	 * Gets a random generator with a fixed seed set using the system property gdsc.test.seed.
	 *
	 * @return the random generator
	 */
	public static RandomGenerator getRandomGenerator()
	{
		return getRandomGenerator(seed);
	}

	/**
	 * A class for comparing double arrays using a relative threshold.
	 * <p>
	 * Based on org.junit.internal.InexactComparisonCriteria
	 */
	public static class DoubleRelativeComparisonCriteria extends ComparisonCriteria
	{
		public double delta;

		public DoubleRelativeComparisonCriteria(double delta)
		{
			this.delta = delta;
		}

		@Override
		protected void assertElementsEqual(Object expected, Object actual)
		{
			final double e = (double) expected;
			final double a = (double) actual;

			// Check the two numbers are within an absolute distance.
			final double difference = max(Math.abs(e), Math.abs(a)) * delta;

			Assert.assertEquals(e, a, difference);
		}

		private double max(double a, double b)
		{
			return (a >= b) ? a : b;
		}
	}

	/**
	 * A class for comparing float arrays using a relative threshold.
	 * <p>
	 * Based on org.junit.internal.InexactComparisonCriteria
	 */
	public static class FloatRelativeComparisonCriteria extends ComparisonCriteria
	{
		public double delta;

		public FloatRelativeComparisonCriteria(double delta)
		{
			this.delta = delta;
		}

		@Override
		protected void assertElementsEqual(Object expected, Object actual)
		{
			final float e = (float) expected;
			final float a = (float) actual;

			// Check the two numbers are within an absolute distance.
			final float difference = (float) (max(Math.abs(e), Math.abs(a)) * delta);

			Assert.assertEquals(e, a, difference);
		}

		private double max(double a, double b)
		{
			return (a >= b) ? a : b;
		}
	}

	/**
	 * Asserts that two double arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message.
	 *
	 * @param expecteds
	 *            double array with expected values.
	 * @param actuals
	 *            double array with actual values
	 * @param relativeError
	 *            the relative error
	 */
	public static void assertArrayEquals(double[] expecteds, double[] actuals, double relativeError)
	{
		assertArrayEquals(null, expecteds, actuals, relativeError);
	}

	/**
	 * Asserts that two double arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message.
	 *
	 * @param message
	 *            the identifying message for the {@link AssertionError} (<code>null</code>
	 *            okay)
	 * @param expecteds
	 *            double array with expected values.
	 * @param actuals
	 *            double array with actual values
	 * @param delta
	 *            the maximum delta between <code>expecteds[i]</code> and
	 *            <code>actuals[i]</code> for which both numbers are still
	 *            considered equal.
	 */
	public static void assertArrayEquals(String message, double[] expecteds, double[] actuals, double relativeError)
	{
		new DoubleRelativeComparisonCriteria(relativeError).arrayEquals(message, expecteds, actuals);
	}

	/**
	 * Asserts that two float arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message.
	 *
	 * @param expecteds
	 *            float array with expected values.
	 * @param actuals
	 *            float array with actual values
	 * @param relativeError
	 *            the relative error
	 */
	public static void assertArrayEquals(float[] expecteds, float[] actuals, double relativeError)
	{
		assertArrayEquals(null, expecteds, actuals, relativeError);
	}

	/**
	 * Asserts that two float arrays are equal. If they are not, an
	 * {@link AssertionError} is thrown with the given message.
	 *
	 * @param message
	 *            the identifying message for the {@link AssertionError} (<code>null</code>
	 *            okay)
	 * @param expecteds
	 *            float array with expected values.
	 * @param actuals
	 *            float array with actual values
	 * @param delta
	 *            the maximum delta between <code>expecteds[i]</code> and
	 *            <code>actuals[i]</code> for which both numbers are still
	 *            considered equal.
	 */
	public static void assertArrayEquals(String message, float[] expecteds, float[] actuals, double relativeError)
	{
		new FloatRelativeComparisonCriteria(relativeError).arrayEquals(message, expecteds, actuals);
	}
}

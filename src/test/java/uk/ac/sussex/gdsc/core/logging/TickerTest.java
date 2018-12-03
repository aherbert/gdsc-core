package uk.ac.sussex.gdsc.core.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"javadoc"})
public class TickerTest {

  @Test
  public void canCreateDefaultTicker() {
    final long total = 10;
    final boolean threadSafe = false;

    Assertions.assertSame(Ticker.getDefaultInstance(), Ticker.create(null, total, threadSafe),
        "Failed to create default ticker using null track progress");

    Assertions.assertSame(Ticker.getDefaultInstance(),
        Ticker.create(NullTrackProgress.getInstance(), total, threadSafe),
        "Failed to create default ticker using default track progress");

    // Do nothing
    final TrackProgress progress = new TrackProgress() {
      @Override
      public void progress(double fraction) {}

      @Override
      public void progress(long position, long total) {}

      @Override
      public void incrementProgress(double fraction) {}

      @Override
      public void log(String format, Object... args) {}

      @Override
      public void status(String format, Object... args) {}

      @Override
      public boolean isEnded() {
        return false;
      }

      @Override
      public boolean isProgress() {
        return false;
      }

      @Override
      public boolean isLog() {
        return false;
      }

      @Override
      public boolean isStatus() {
        return false;
      }
    };
    Assertions.assertSame(Ticker.getDefaultInstance(), Ticker.create(progress, 0, threadSafe),
        "Failed to create default ticker using zero total progress");

    // Check the default ticker
    final Ticker ticker = Ticker.getDefaultInstance();
    Assertions.assertEquals(0, ticker.getCurrent(), "Current");
    Assertions.assertEquals(0, ticker.getTotal(), "Total");
    Assertions.assertEquals(true, ticker.isThreadSafe(), "ThreadSafe");
    ticker.start();
    ticker.tick();
    Assertions.assertEquals(0, ticker.getCurrent(), "Current");
    Assertions.assertEquals(0, ticker.getTotal(), "Total");
    ticker.stop();
  }

  @Test
  public void throwsWithBadInterval() {
    final long total = 10;
    final boolean threadSafe = false;
    // Do nothing
    final TrackProgress progress = new TrackProgress() {
      @Override
      public void progress(double fraction) {}

      @Override
      public void progress(long position, long total) {}

      @Override
      public void incrementProgress(double fraction) {}

      @Override
      public void log(String format, Object... args) {}

      @Override
      public void status(String format, Object... args) {}

      @Override
      public boolean isEnded() {
        return false;
      }

      @Override
      public boolean isProgress() {
        return false;
      }

      @Override
      public boolean isLog() {
        return false;
      }

      @Override
      public boolean isStatus() {
        return false;
      }
    };
    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      Ticker.create(progress, -1, total, threadSafe);
    });
  }

  @Test
  public void canCreateTickerForIntegerTotal() {
    testTicker(10, false);
  }

  @Test
  public void canCreateTickerForIntegerTotalThreadSafe() {
    testTicker(10, true);
  }

  @Test
  public void canCreateTickerForLongTotal() {
    testTicker(Integer.MAX_VALUE + 1L, false);
  }

  @Test
  public void canCreateTickerForLongTotalThreadSafe() {
    testTicker(Integer.MAX_VALUE + 1L, true);
  }

  private static void testTicker(long total, boolean threadSafe) {
    final double[] progressFraction = new double[] {Double.NaN};
    final TrackProgress progress = new TrackProgress() {
      @Override
      public void progress(double fraction) {
        progressFraction[0] = fraction;
      }

      @Override
      public void progress(long position, long total) {}

      @Override
      public void incrementProgress(double fraction) {}

      @Override
      public void log(String format, Object... args) {}

      @Override
      public void status(String format, Object... args) {}

      @Override
      public boolean isEnded() {
        return false;
      }

      @Override
      public boolean isProgress() {
        return false;
      }

      @Override
      public boolean isLog() {
        return false;
      }

      @Override
      public boolean isStatus() {
        return false;
      }
    };

    Ticker ticker = Ticker.create(progress, total, threadSafe);
    Assertions.assertEquals(total, ticker.getTotal(), "Total");
    Assertions.assertEquals(threadSafe, ticker.isThreadSafe(), "ThreadSafe");

    Assertions.assertEquals(0, ticker.getCurrent(), "Current when initialised");
    Assertions.assertEquals(0, ticker.getProgress(), "Ticker progress when initialised");
    Assertions.assertEquals(Double.NaN, progressFraction[0], "Progress when initialised");

    ticker.start();
    Assertions.assertEquals(0, ticker.getCurrent(), "Current when started");
    Assertions.assertEquals(0, ticker.getProgress(), "Ticker progress when started");
    Assertions.assertEquals(0, progressFraction[0], "Progress when started");

    // Interval is 2. Should report at current == 0,2,4,etc
    ticker = Ticker.createStarted(progress, 2, total, threadSafe);
    Assertions.assertEquals(total, ticker.getTotal(), "Total when started");
    Assertions.assertEquals(threadSafe, ticker.isThreadSafe(), "ThreadSafe when started");

    Assertions.assertEquals(0, ticker.getCurrent(), "Current when started");
    Assertions.assertEquals(0, progressFraction[0], "Progress when started");

    // Test - 1 tick should be enough to register progress
    ticker.tick();
    Assertions.assertEquals(1, ticker.getCurrent(), "Current after 1 tick");
    Assertions.assertEquals(1.0 / total, ticker.getProgress(), "Ticker progress 1 tick");
    Assertions.assertEquals(0, progressFraction[0], "Progress after 1 tick");

    ticker.tick();
    Assertions.assertEquals(2, ticker.getCurrent(), "Current after 2 ticks");
    Assertions.assertEquals(2.0 / total, ticker.getProgress(), "Ticker progress 2 ticks");
    // Updated progress as the interval is 2
    Assertions.assertEquals(2.0 / total, progressFraction[0], "Progress after 2 ticks");

    ticker.tick();
    Assertions.assertEquals(3, ticker.getCurrent(), "Current after 3 ticks");
    // This should not report progress
    Assertions.assertEquals(2.0 / total, progressFraction[0], "Progress after 3 ticks");

    ticker.tick();
    Assertions.assertEquals(4, ticker.getCurrent(), "Current after 4 ticks");
    // Updated progress as the interval is 2
    Assertions.assertEquals(4.0 / total, progressFraction[0], "Progress after 4 ticks");

    ticker.stop();
    Assertions.assertEquals(4, ticker.getCurrent(), "Current after 4 ticks and stopped");
    Assertions.assertEquals(1.0, progressFraction[0], "Progress after 4 ticks and stopped");

    ticker.tick();
    ticker.tick();
    Assertions.assertEquals(6, ticker.getCurrent(), "Current after 6 ticks after stop()");
    Assertions.assertEquals(6.0 / total, progressFraction[0],
        "Progress after 6 ticks after stop()");

    Assertions.assertEquals(6.0 / total, ticker.getProgress(),
        "Ticker Progress after 6 ticks after stop()");

    // This should reset the ticker
    ticker.start();
    Assertions.assertEquals(0, ticker.getCurrent(), "Current when re-started");
    Assertions.assertEquals(0, progressFraction[0], "Progress when re-started");
  }


  @Test
  public void testThreadSafeTickerForIntegerTotal() throws InterruptedException {
    testThreadSafeTicker(Integer.MAX_VALUE);
  }

  @Test
  public void testThreadSafeTickerForLongTotal() throws InterruptedException {
    testThreadSafeTicker(Integer.MAX_VALUE + 1L);
  }

  private static void testThreadSafeTicker(long total) throws InterruptedException {
    final int threadCount = Runtime.getRuntime().availableProcessors();
    Assumptions.assumeTrue(threadCount > 1, "Require multi-threading for this test");

    // A dummy ticker
    final TrackProgress progress = new TrackProgress() {
      @Override
      public void progress(double fraction) {}

      @Override
      public void progress(long position, long total) {}

      @Override
      public void incrementProgress(double fraction) {}

      @Override
      public void log(String format, Object... args) {}

      @Override
      public void status(String format, Object... args) {}

      @Override
      public boolean isEnded() {
        return false;
      }

      @Override
      public boolean isProgress() {
        return false;
      }

      @Override
      public boolean isLog() {
        return false;
      }

      @Override
      public boolean isStatus() {
        return false;
      }
    };

    final Ticker ticker = Ticker.createStarted(progress, total, true);
    Assertions.assertTrue(ticker.isThreadSafe(), "ThreadSafe");

    // Start a number of threads that should call the ticker a fixed number of times
    final ExecutorService es = Executors.newFixedThreadPool(threadCount);
    final int ticks = 10000;
    final AtomicInteger counter = new AtomicInteger(ticks);
    for (int i = 0; i < threadCount; i++) {
      es.submit(() -> {
        while (counter.decrementAndGet() >= 0) {
          ticker.tick();
        }
      });
    }
    es.shutdown();
    es.awaitTermination(10, TimeUnit.SECONDS);

    // Check the ticker was called a fixed number of times
    Assertions.assertEquals(ticks, ticker.getCurrent());
  }
}

package uk.ac.sussex.gdsc.core.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class NullTrackProgressTest {

  @Test
  public void canCreateIfNull() {
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

    Assertions.assertSame(progress, NullTrackProgress.createIfNull(progress),
        "Failed to return same track progress");

    final TrackProgress newProgress = NullTrackProgress.createIfNull(null);
    Assertions.assertNotNull(newProgress, "Failed to create if null");
    Assertions.assertSame(NullTrackProgress.getInstance(), newProgress,
        "Failed to return the default instance if null");

    Assertions.assertFalse(newProgress.isEnded());
    Assertions.assertFalse(newProgress.isProgress());
    Assertions.assertFalse(newProgress.isLog());
    Assertions.assertFalse(newProgress.isStatus());

    // Exercise the methods for coverage
    newProgress.progress(0.5);
    newProgress.progress(1, 2);
    newProgress.incrementProgress(0.1);
    newProgress.status("ignored");
  }
}

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
 * Copyright (C) 2011 - 2020 Alex Herbert
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

package uk.ac.sussex.gdsc.core.clustering.optics;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"javadoc"})
public class LoOpTest {
  @Test
  public void testExecutorService() {
    final float[] x = {0};
    final LoOp loop = new LoOp(x, x);
    Assertions.assertNull(loop.getExecutorService());
    // Can set null
    loop.setExecutorService(null);
    final ExecutorService es = Executors.newSingleThreadExecutor();
    loop.setExecutorService(es);
    Assertions.assertSame(es, loop.getExecutorService());
    es.shutdown();
    Assertions.assertNull(loop.getExecutorService());
  }

  public void testLoop2d() throws InterruptedException, ExecutionException {
    final float[] x = {0, 0, 0, 0, 5};
    final LoOp loop = new LoOp(x, x);
    final double[] scores = loop.run(4, 1.5);
    Assertions.assertTrue(scores[4] > scores[0]);
    for (int i = 1; i < 4; i++) {
      Assertions.assertEquals(scores[0], scores[i]);
    }
  }

  @Test
  public void testLoop3d() throws InterruptedException, ExecutionException {
    final float[] x = {0, 0, 0, 0, 5};
    final LoOp loop = new LoOp(x, x, x);
    final double[] scores = loop.run(4, 1.5);
    Assertions.assertTrue(scores[4] > scores[0]);
    for (int i = 1; i < 4; i++) {
      Assertions.assertEquals(scores[0], scores[i]);
    }
  }
}

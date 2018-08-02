package uk.ac.sussex.gdsc.core.math;

import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@SuppressWarnings({ "javadoc" })
public class RadialStatisticsTest
{
    @Test
    public void canComputeRadialSum()
    {
        assertEquals("3", new double[] { 1, 8, 0 }, RadialStatistics.radialSum(3, ones(3)));
        assertEquals("4", new double[] { 1, 8, 7 }, RadialStatistics.radialSum(4, ones(4)));
        assertEquals("5", new double[] { 1, 8, 16, 0, 0 }, RadialStatistics.radialSum(5, ones(5)));
        assertEquals("6", new double[] { 1, 8, 16, 10, 0 }, RadialStatistics.radialSum(6, ones(6)));
    }

    @Test
    public void canComputeRadialSum16()
    {
        assertEquals("16", new double[] { 1, 8, 16, 20, 24, 40, 36, 48, 38, 16, 8, 1 },
                RadialStatistics.radialSum(16, ones(16)));
    }

    @Test
    public void canComputeRadialSum512()
    {
        assertEquals("512", new double[] { 1, 8, 16, 20, 24, 40, 36, 48, 56, 56, 68, 64, 80, 92, 88, 96, 96, 116, 120,
                120, 124, 144, 136, 140, 152, 168, 176, 164, 168, 192, 188, 208, 200, 208, 228, 208, 232, 228, 256, 248,
                236, 272, 264, 288, 276, 272, 296, 292, 312, 304, 336, 324, 312, 344, 324, 376, 344, 360, 364, 368, 392,
                388, 392, 400, 388, 432, 400, 424, 452, 416, 448, 428, 480, 464, 464, 476, 480, 496, 476, 496, 520, 500,
                536, 504, 536, 548, 544, 552, 524, 600, 552, 576, 580, 584, 608, 580, 600, 632, 608, 644, 608, 664, 644,
                624, 672, 644, 696, 672, 664, 684, 688, 720, 700, 712, 744, 720, 716, 736, 752, 756, 752, 736, 772, 824,
                752, 808, 756, 816, 824, 788, 840, 808, 864, 844, 800, 880, 852, 872, 864, 852, 912, 872, 896, 900, 912,
                936, 908, 912, 952, 944, 948, 920, 976, 948, 1000, 960, 976, 1020, 960, 1000, 1004, 1056, 1024, 996,
                1040, 1032, 1080, 1028, 1024, 1096, 1076, 1080, 1064, 1112, 1108, 1080, 1112, 1084, 1152, 1136, 1108,
                1144, 1168, 1176, 1116, 1168, 1200, 1172, 1184, 1168, 1192, 1228, 1176, 1248, 1196, 1248, 1248, 1224,
                1260, 1232, 1296, 1236, 1240, 1328, 1268, 1320, 1264, 1304, 1324, 1304, 1312, 1348, 1336, 1344, 1344,
                1324, 1384, 1376, 1356, 1384, 1384, 1396, 1424, 1376, 1432, 1396, 1424, 1416, 1452, 1496, 1400, 1440,
                1452, 1472, 1512, 1444, 1480, 1520, 1452, 1536, 1464, 1568, 1548, 1512, 1552, 1492, 1616, 1544, 1528,
                1564, 1552, 1600, 1580, 1592, 1616, 1624, 1506, 1372, 1368, 1280, 1256, 1216, 1172, 1156, 1100, 1132,
                1068, 1044, 1040, 988, 992, 976, 960, 916, 896, 936, 872, 872, 820, 828, 860, 776, 804, 752, 768, 764,
                696, 756, 680, 700, 704, 628, 708, 616, 636, 628, 588, 632, 556, 588, 560, 540, 564, 500, 532, 508, 500,
                496, 444, 504, 432, 468, 428, 424, 424, 396, 416, 376, 380, 376, 336, 372, 328, 348, 312, 312, 308, 272,
                312, 260, 272, 256, 224, 276, 208, 232, 212, 176, 232, 168, 196, 164, 156, 160, 120, 168, 108, 128, 112,
                72, 132, 60, 108, 48, 72, 48, 32, 52, 20, 28, 8, 4, 1 }, RadialStatistics.radialSum(512, ones(512)));
    }

    @Test
    public void canComputeRadialCount()
    {
        assertEquals("3", new int[] { 1, 8, 0 }, RadialStatistics.radialCount(3));
        assertEquals("4", new int[] { 1, 8, 7 }, RadialStatistics.radialCount(4));
        assertEquals("5", new int[] { 1, 8, 16, 0, 0 }, RadialStatistics.radialCount(5));
        assertEquals("6", new int[] { 1, 8, 16, 10, 0 }, RadialStatistics.radialCount(6));
    }

    @Test
    public void canComputeRadialCount16()
    {
        assertEquals("16", new int[] { 1, 8, 16, 20, 24, 40, 36, 48, 38, 16, 8, 1 }, RadialStatistics.radialCount(16));
    }

    @Test
    public void canComputeRadialCount512()
    {
        assertEquals("512", new int[] { 1, 8, 16, 20, 24, 40, 36, 48, 56, 56, 68, 64, 80, 92, 88, 96, 96, 116, 120, 120,
                124, 144, 136, 140, 152, 168, 176, 164, 168, 192, 188, 208, 200, 208, 228, 208, 232, 228, 256, 248, 236,
                272, 264, 288, 276, 272, 296, 292, 312, 304, 336, 324, 312, 344, 324, 376, 344, 360, 364, 368, 392, 388,
                392, 400, 388, 432, 400, 424, 452, 416, 448, 428, 480, 464, 464, 476, 480, 496, 476, 496, 520, 500, 536,
                504, 536, 548, 544, 552, 524, 600, 552, 576, 580, 584, 608, 580, 600, 632, 608, 644, 608, 664, 644, 624,
                672, 644, 696, 672, 664, 684, 688, 720, 700, 712, 744, 720, 716, 736, 752, 756, 752, 736, 772, 824, 752,
                808, 756, 816, 824, 788, 840, 808, 864, 844, 800, 880, 852, 872, 864, 852, 912, 872, 896, 900, 912, 936,
                908, 912, 952, 944, 948, 920, 976, 948, 1000, 960, 976, 1020, 960, 1000, 1004, 1056, 1024, 996, 1040,
                1032, 1080, 1028, 1024, 1096, 1076, 1080, 1064, 1112, 1108, 1080, 1112, 1084, 1152, 1136, 1108, 1144,
                1168, 1176, 1116, 1168, 1200, 1172, 1184, 1168, 1192, 1228, 1176, 1248, 1196, 1248, 1248, 1224, 1260,
                1232, 1296, 1236, 1240, 1328, 1268, 1320, 1264, 1304, 1324, 1304, 1312, 1348, 1336, 1344, 1344, 1324,
                1384, 1376, 1356, 1384, 1384, 1396, 1424, 1376, 1432, 1396, 1424, 1416, 1452, 1496, 1400, 1440, 1452,
                1472, 1512, 1444, 1480, 1520, 1452, 1536, 1464, 1568, 1548, 1512, 1552, 1492, 1616, 1544, 1528, 1564,
                1552, 1600, 1580, 1592, 1616, 1624, 1506, 1372, 1368, 1280, 1256, 1216, 1172, 1156, 1100, 1132, 1068,
                1044, 1040, 988, 992, 976, 960, 916, 896, 936, 872, 872, 820, 828, 860, 776, 804, 752, 768, 764, 696,
                756, 680, 700, 704, 628, 708, 616, 636, 628, 588, 632, 556, 588, 560, 540, 564, 500, 532, 508, 500, 496,
                444, 504, 432, 468, 428, 424, 424, 396, 416, 376, 380, 376, 336, 372, 328, 348, 312, 312, 308, 272, 312,
                260, 272, 256, 224, 276, 208, 232, 212, 176, 232, 168, 196, 164, 156, 160, 120, 168, 108, 128, 112, 72,
                132, 60, 108, 48, 72, 48, 32, 52, 20, 28, 8, 4, 1 }, RadialStatistics.radialCount(512));
    }

    @Test
    public void canComputeRadialSum8x8()
    {
        assertEquals("8x8", new double[] { 37, 296, 592, 740, 338, 77 }, RadialStatistics.radialSum(8, sequence(8)));
    }

    @Test
    public void canComputeRadialSum32x32()
    {
        assertEquals("32x32",
                new double[] { 529, 4232, 8464, 10580, 12696, 21160, 19044, 25392, 29624, 29624, 35972, 33856, 42320,
                        48668, 46552, 50784, 33338, 28572, 16932, 15872, 6352, 4236, 1 },
                RadialStatistics.radialSum(32, sequence(32)));
    }

    @Test
    public void canComputeRadialSumAndCount32()
    {
        final double[][] sum = RadialStatistics.radialSumAndCount(32, sequence(32), sequence(32, 43));
        assertEquals("sum1", new double[] { 529, 4232, 8464, 10580, 12696, 21160, 19044, 25392, 29624, 29624, 35972,
                33856, 42320, 48668, 46552, 50784, 33338, 28572, 16932, 15872, 6352, 4236, 1 }, sum[0]);
        assertEquals("sum2", new double[] { 571, 4568, 9136, 11420, 13704, 22840, 20556, 27408, 31976, 31976, 38828,
                36544, 45680, 52532, 50248, 54816, 36446, 31092, 18444, 17216, 7024, 4740, 43 }, sum[1]);
        assertEquals("count", new double[] { 1, 8, 16, 20, 24, 40, 36, 48, 56, 56, 68, 64, 80, 92, 88, 96, 74, 60, 36,
                32, 16, 12, 1 }, sum[2]);
    }

    @Test
    public void canComputeRadialSumAndCountMulti32()
    {
        final double[][] sum = RadialStatistics.radialSum(32, sequence(32), sequence(32, 43));
        assertEquals("sum1", new double[] { 529, 4232, 8464, 10580, 12696, 21160, 19044, 25392, 29624, 29624, 35972,
                33856, 42320, 48668, 46552, 50784, 33338, 28572, 16932, 15872, 6352, 4236, 1 }, sum[0]);
        assertEquals("sum2", new double[] { 571, 4568, 9136, 11420, 13704, 22840, 20556, 27408, 31976, 31976, 38828,
                36544, 45680, 52532, 50248, 54816, 36446, 31092, 18444, 17216, 7024, 4740, 43 }, sum[1]);
    }

    private static float[] ones(int size)
    {
        final float[] data = new float[size * size];
        Arrays.fill(data, 1);
        return data;
    }

    private static float[] sequence(int size)
    {
        return sequence(size, 1);
    }

    private static float[] sequence(int size, int start)
    {
        final float[] data = new float[size * size];
        for (int i = 0; i < data.length; i++)
            data[i] = start++;
        return data;
    }

    private static void assertEquals(String msg, double[] e, double[] o)
    {
        //TestLog.debug(logger,"%s vs %s", Arrays.toString(e), Arrays.toString(o));

        // The radial sum is meant to match the computation of the Matlab DIPimage toolbox.
        // However RadialSum truncates the computation at the max in one dimension so only go up to the size of o.
        Assertions.assertArrayEquals(Arrays.copyOf(e, o.length), o, msg);
    }

    private static void assertEquals(String msg, int[] e, int[] o)
    {
        //TestLog.debug(logger,"%s vs %s", Arrays.toString(e), Arrays.toString(o));

        // The radial sum is meant to match the computation of the Matlab DIPimage toolbox.
        // However RadialSum truncates the computation at the max in one dimension so only go up to the size of o.
        Assertions.assertArrayEquals(Arrays.copyOf(e, o.length), o, msg);
    }
}

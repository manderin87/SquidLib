/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package squidpony.performance;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import squidpony.squidmath.*;

import java.util.concurrent.TimeUnit;

/** Generating 64 million points of noise, each run
 * Benchmark                            Mode  Cnt     Score     Error  Units
 * NoiseBenchmark.measureMerlin2D       avgt    4   734.085 ±  58.096  ms/op // different type of result, not smooth
 * NoiseBenchmark.measureMerlin3D       avgt    4  1082.559 ± 143.629  ms/op // different type of result, not smooth
 * NoiseBenchmark.measurePerlin2D       avgt    4  2100.514 ±  55.326  ms/op
 * NoiseBenchmark.measurePerlin3D       avgt    4  3385.798 ±  77.035  ms/op
 * NoiseBenchmark.measureWhirling2D     avgt    4  1609.510 ± 134.049  ms/op // best smooth 2D
 * NoiseBenchmark.measureWhirling3D     avgt    4  2808.307 ± 121.119  ms/op // best smooth 3D
 * NoiseBenchmark.measureWhirlingAlt2D  avgt    4  1758.283 ± 117.308  ms/op
 * NoiseBenchmark.measureWhirlingAlt3D  avgt    4  2895.686 ±  98.599  ms/op
 */
public class NoiseBenchmark {

    private static double seed = 9000;

    public double doPerlin2D()
    {
        for (double x = 0.0; x < 8000.0; x++) {
            for (double y = 0.0; y < 8000.0; y++) {
                seed += PerlinNoise.noise(x, y);
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePerlin2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doPerlin2D());
    }

    public double doPerlin3D()
    {
        for (double x = 0.0; x < 400.0; x++) {
            for (double y = 0.0; y < 400.0; y++) {
                for (double z = 0.0; z < 400.0; z++) {
                    seed += PerlinNoise.noise(x, y, z);
                }
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measurePerlin3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doPerlin3D());
    }

    public double doMerlin2D()
    {
        for (int x = 0; x < 8000; x++) {
            for (int y = 0; y < 8000; y++) {
                seed += MerlinNoise.noise2D(x, y);
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMerlin2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doMerlin2D());
    }

    public double doMerlin3D()
    {
        for (int x = 0; x < 400; x++) {
            for (int y = 0; y < 400; y++) {
                for (int z = 0; z < 400; z++) {
                    seed += MerlinNoise.noise3D(x, y, z);
                }
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureMerlin3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doMerlin3D());
    }

    public double doWhirling2D()
    {
        for (double x = 0.0; x < 8000.0; x++) {
            for (double y = 0.0; y < 8000.0; y++) {
                seed += WhirlingNoise.noise(x, y);
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirling2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirling2D());
    }

    public double doWhirling3D()
    {
        for (double x = 0.0; x < 400.0; x++) {
            for (double y = 0.0; y < 400.0; y++) {
                for (double z = 0.0; z < 400.0; z++) {
                    seed += WhirlingNoise.noise(x, y, z);
                }
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirling3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirling3D());
    }


    public double doWhirlingAlt2D()
    {
        for (double x = 0.0; x < 8000.0; x++) {
            for (double y = 0.0; y < 8000.0; y++) {
                seed += WhirlingNoise.noiseAlt(x, y);
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirlingAlt2D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirlingAlt2D());
    }

    public double doWhirlingAlt3D()
    {
        for (double x = 0.0; x < 400.0; x++) {
            for (double y = 0.0; y < 400.0; y++) {
                for (double z = 0.0; z < 400.0; z++) {
                    seed += WhirlingNoise.noiseAlt(x, y, z);
                }
            }
        }
        return seed;
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public void measureWhirlingAlt3D() throws InterruptedException {
        seed = 9000;
        System.out.println(doWhirlingAlt3D());
    }


    /*
     * ============================== HOW TO RUN THIS TEST: ====================================
     *
     * You are expected to see the different run modes for the same benchmark.
     * Note the units are different, scores are consistent with each other.
     *
     * You can run this test:
     *
     * a) Via the command line from the squidlib-performance module's root folder:
     *    $ mvn clean install
     *    $ java -jar target/benchmarks.jar NoiseBenchmark -wi 3 -i 3 -f 1
     *
     *    (we requested 5 warmup/measurement iterations, single fork)
     *
     * b) Via the Java API:
     *    (see the JMH homepage for possible caveats when running from IDE:
     *      http://openjdk.java.net/projects/code-tools/jmh/)
     */

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(NoiseBenchmark.class.getSimpleName())
                .timeout(TimeValue.seconds(30))
                .warmupIterations(4)
                .measurementIterations(4)
                .forks(1)
                .build();

        new Runner(opt).run();
    }


}

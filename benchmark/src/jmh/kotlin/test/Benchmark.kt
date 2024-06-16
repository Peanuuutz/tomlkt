package test

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Threads
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
object TomlObjects {
    val tomlkt = net.peanuuutz.tomlkt.Toml
    val toml4j = com.moandjiezana.toml.Toml()
    val ktoml = com.akuleshov7.ktoml.Toml
    val jackson = com.fasterxml.jackson.dataformat.toml.TomlMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
    }
    val night = com.electronwill.nightconfig.toml.TomlParser()
}

/*
    SmallSampleConfig:

    Benchmark          Mode  Cnt       Score      Error  Units
    Benchmark.jackson  avgt    5   5021.622 ±  140.504  ns/op
    Benchmark.night    avgt    5   7625.834 ±  593.511  ns/op
    Benchmark.tomlkt   avgt    5   9006.957 ±  158.111  ns/op
    Benchmark.toml4j   avgt    5   9628.198 ±  362.380  ns/op
    Benchmark.ktoml    avgt    5  30904.482 ± 2665.325  ns/op
    Benchmark.tomlj    avgt    5  82332.312 ± 1493.406  ns/op

    LargeSampleConfig:

    Benchmark          Mode  Cnt        Score       Error  Units
    Benchmark.jackson  avgt    5   48124.930 ±  2675.439  ns/op
    Benchmark.night    avgt    5   81193.034 ±  1100.800  ns/op
    Benchmark.tomlkt   avgt    5  134145.378 ± 29334.081  ns/op
    Benchmark.toml4j   avgt    5  227001.105 ±  1702.516  ns/op
    Benchmark.tomlj    avgt    5  521451.284 ± 13951.421  ns/op
    Benchmark.ktoml    avgt    5  640967.592 ±  9486.035  ns/op
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 4)
@Measurement(iterations = 5)
@Threads(4)
@Fork(1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
class Benchmark {
    @Benchmark
    fun tomlkt(hole: Blackhole) {
        hole.consume(TomlObjects.tomlkt.parseToTomlTable(SmallSampleConfig))
    }

    @Benchmark
    fun toml4j(hole: Blackhole) {
        hole.consume(TomlObjects.toml4j.read(SmallSampleConfig))
    }

    @Benchmark
    fun ktoml(hole: Blackhole) {
        hole.consume(TomlObjects.ktoml.tomlParser.parseString(SmallSampleConfig))
    }

    @Benchmark
    fun jackson(hole: Blackhole) {
        hole.consume(TomlObjects.jackson.readTree(SmallSampleConfig))
    }

    @Benchmark
    fun night(hole: Blackhole) {
        hole.consume(TomlObjects.night.parse(SmallSampleConfig))
    }

    @Benchmark
    fun tomlj(hole: Blackhole) {
        hole.consume(org.tomlj.Toml.parse(SmallSampleConfig))
    }
}

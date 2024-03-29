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
    Benchmark.jackson  avgt    5    9151.851 ± 1123.198  ns/op
    Benchmark.night    avgt    5   11480.495 ± 1424.068  ns/op
    Benchmark.tomlkt   avgt    5   11555.185 ±  347.118  ns/op
    Benchmark.toml4j   avgt    5   30508.511 ±  292.352  ns/op
    Benchmark.ktoml    avgt    5   53619.175 ± 1512.321  ns/op
    Benchmark.tomlj    avgt    5  146998.024 ± 2384.767  ns/op

    LargeSampleConfig:

    Benchmark          Mode  Cnt        Score       Error  Units
    Benchmark.jackson  avgt    5   102626.482 ±  1935.074  ns/op
    Benchmark.night    avgt    5   123354.484 ±  4292.667  ns/op
    Benchmark.tomlkt   avgt    5   157744.955 ±  5007.944  ns/op
    Benchmark.tomlj    avgt    5   953882.911 ± 13345.218  ns/op
    Benchmark.toml4j   avgt    5  1124525.511 ± 62901.746  ns/op
    Benchmark.ktoml    avgt    5  1193358.164 ± 12310.127  ns/op
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
    fun tomlkt() {
        TomlObjects.tomlkt.parseToTomlTable(SmallSampleConfig)
    }

    @Benchmark
    fun toml4j() {
        TomlObjects.toml4j.read(SmallSampleConfig)
    }

    @Benchmark
    fun ktoml() {
        TomlObjects.ktoml.tomlParser.parseString(SmallSampleConfig)
    }

    @Benchmark
    fun jackson() {
        TomlObjects.jackson.readTree(SmallSampleConfig)
    }

    @Benchmark
    fun night() {
        TomlObjects.night.parse(SmallSampleConfig)
    }

    @Benchmark
    fun tomlj() {
        org.tomlj.Toml.parse(SmallSampleConfig)
    }
}

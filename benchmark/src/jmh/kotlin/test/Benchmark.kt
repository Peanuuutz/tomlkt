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
    Benchmark                       Mode  Cnt      Score      Error  Units
    Benchmark.jackson               avgt    5   5175.377 ±  156.576  ns/op
    Benchmark.night                 avgt    5   7278.401 ±  257.462  ns/op
    Benchmark.tomlkt                avgt    5   9494.927 ±  271.427  ns/op
    Benchmark.toml4j                avgt    5  15858.802 ±  374.109  ns/op
    Benchmark.ktoml                 avgt    5  43655.068 ± 8584.812  ns/op
    Benchmark.tomlj                 avgt    5  92314.157 ± 4917.454  ns/op
 */
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 4)
@Measurement(iterations = 5)
@Threads(4)
@Fork(2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
class Benchmark {
    @Benchmark
    fun tomlkt() {
        TomlObjects.tomlkt.parseToTomlTable(SampleConfig)
    }

    @Benchmark
    fun toml4j() {
        TomlObjects.toml4j.read(SampleConfig)
    }

    @Benchmark
    fun ktoml() {
        TomlObjects.ktoml.tomlParser.parseString(SampleConfig)
    }

    @Benchmark
    fun jackson() {
        TomlObjects.jackson.readTree(SampleConfig)
    }

    @Benchmark
    fun night() {
        TomlObjects.night.parse(SampleConfig)
    }

    @Benchmark
    fun tomlj() {
        org.tomlj.Toml.parse(SampleConfig)
    }
}

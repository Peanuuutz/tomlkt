package test

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.serialization.decodeFromString
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
}

/*
    Benchmark                          Mode  Cnt      Score       Error  Units
    Benchmark.jacksonWithType          avgt   10   7753.556 ±  104.444  ns/op
    Benchmark.jacksonWithClass         avgt   10   7803.826 ±  312.212  ns/op
    Benchmark.tomlktWithSerializer     avgt   10  13638.573 ±  203.072  ns/op
    Benchmark.tomlktWithoutSerializer  avgt   10  13723.892 ±  320.052  ns/op
    Benchmark.toml4j                   avgt   10  21123.156 ±  209.969  ns/op
    Benchmark.ktomlWithSerializer      avgt   10  47823.952 ±  722.429  ns/op
    Benchmark.ktomlWithoutSerializer   avgt   10  48057.838 ± 1090.022  ns/op
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
    fun tomlktWithoutSerializer() {
        TomlObjects.tomlkt.decodeFromString<Config>(SampleConfig)
    }

    @Benchmark
    fun tomlktWithSerializer() {
        TomlObjects.tomlkt.decodeFromString(Config.serializer(), SampleConfig)
    }

    @Benchmark
    fun toml4j() {
        TomlObjects.toml4j.read(SampleConfig).to(Config::class.java)
    }

    @Benchmark
    fun ktomlWithoutSerializer() {
        TomlObjects.ktoml.decodeFromString<Config>(SampleConfig)
    }

    @Benchmark
    fun ktomlWithSerializer() {
        TomlObjects.ktoml.decodeFromString(Config.serializer(), SampleConfig)
    }

    @Benchmark
    fun jacksonWithClass() {
        TomlObjects.jackson.readValue(SampleConfig, Config::class.java)
    }

    @Benchmark
    fun jacksonWithType() {
        TomlObjects.jackson.readValue<Config>(SampleConfig)
    }
}

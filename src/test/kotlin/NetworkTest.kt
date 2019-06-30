import agent.brain.Network
import org.junit.jupiter.api.Test as test


@ExperimentalUnsignedTypes
class NetworkTest {

    @test
    fun compiles() {
        val net = Network.smallAutoencoder()
        val motorOut =
            net.propagate(
                arrayOf(
                    DoubleVector(1.0, 1.0), DoubleVector(1.0, 1.0)
                )
            )

        println("processed signal: ${motorOut.map { it.toString() }}")
    }

    @test
    fun `run code`() { //DEBUG
        println(UByte.MIN_VALUE)
    }
}
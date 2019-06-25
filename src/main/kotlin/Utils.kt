import javafx.geometry.Bounds
import tornadofx.*
import java.nio.ByteBuffer
import kotlin.math.*
import kotlin.random.Random

/**
 * This class is more for trying around, than bringing real value.
 */

tailrec fun fac(to: Int): Int {
    return (if (to == 1) 1 else fac(to - 1))
}

/**
 * Validation for boolean predicates.
 */
inline fun check(value: Boolean, lazyMessage: () -> Any): Unit {
    if (!value) {
        val message = lazyMessage()
        throw IllegalStateException(message.toString())
    }
}

// TODO FIX wrong angle calculation!
fun angleToXAxis(l1d1: Dot, l1d2: Dot = Dot(0.0, 0.0)): Double {
    val alpha = atan2((l1d2.y - l1d1.y), (l1d2.x - l1d1.x)) //radian = slope
    return (if (alpha > 0.0) alpha else alpha + 2 * PI)
}

data class Dot(val x: Double, val y: Double)

class DoubleVector(vararg elements: Double) {

    val length
        get() = sqrt(elements.map { it * it }.sum())

    var elements: DoubleArray = doubleArrayOf(*elements)
    var x = elements[0]
        get() = elements[0]
        set(value) {
            elements[0] = value
            field = value
        }

    var y = elements[1]
        get() = elements[1]
        set(value) {
            elements[1] = value
            field = value
        }

    fun rotate(theta: Dimension<Dimension.AngularUnits>) {
        check(elements.size == 2) { throw IllegalArgumentException("Can rotate only 2d arrays!") }
        val (x, y) = elements
        // angle transformation multiplication
        val rotAngle: Double = if (theta.units == Dimension.AngularUnits.deg) theta.value * PI / 180 else theta.value
        val xTick = x * cos(rotAngle) - y * sin(rotAngle)
        val yTick = y * cos(rotAngle) + x * sin(rotAngle)
        this.elements = DoubleArray(2) { arrayOf(xTick, yTick)[it] }
    }

    operator fun plus(vector: DoubleVector): DoubleVector {
        check(this.elements.size == vector.elements.size) { throw Exception("Can't add vectors of different lengths!") }
        var out = DoubleArray(this.elements.size)
        for (i in 0 until max(elements.size, vector.elements.size)) {
            when {
                i > vector.elements.size ->
                    out[i] = this.elements[i]
                i > this.elements.size ->
                    out[i] = vector.elements[i]
                else -> out[i] = vector.elements[i] + this.elements[i]
            }
        }
        return DoubleVector(*out)
    }


    operator fun unaryMinus() = run {
        var out = DoubleArray(this.elements.size)
        for (i in 0 until elements.size) {
            out[i] = -this.elements[i]
        }
        DoubleVector(*out)
    }

    operator fun times(vector: DoubleVector): DoubleVector {
        check(this.elements.size == vector.elements.size) { throw Exception("Can't add vectors of different lengths!") }
        var out = DoubleArray(this.elements.size)
        for (i in 0 until elements.size) {
            out[i] = this.elements[i] * vector.elements[i]
        }
        return DoubleVector(*out)
    }

    operator fun times(c: Double): DoubleVector {
        var out = DoubleArray(this.elements.size)
        for (i in 0 until elements.size) {
            out[i] = this.elements[i] * c
        }
        return DoubleVector(*out)
    }

    fun copy(): DoubleVector {
        return DoubleVector(*this.elements.copyOf())
    }

    override fun toString(): String {
        val sb: StringBuilder = StringBuilder()
        sb.append("( ")
        elements.forEach { sb.append("${it} ") }
        sb.append(")")
        return String(sb)
    }
}

fun Collection<DoubleVector>.sum(): DoubleVector {
    var out = DoubleVector(0.0, 0.0)
    this.forEach { out += it }
    return out
}

fun center(bounds: Bounds): Dot {
    return Dot((bounds.minX + bounds.maxX) / 2, (bounds.minY + bounds.maxY) / 2)
}

fun sum(vararg elements: Double): Double {
    var out = 0.0
    elements.forEach { out += it }
    return out
}

fun prod(vararg elements: Double): Double {
    var out = 0.0
    elements.forEach { out *= it }
    return out
}


/**
 * Radian to degrees conversion
 */
fun Double.degrees(): Double {
    return this * 180 / PI
}

fun Array<DoubleVector>.sum(): DoubleVector {
    var out = this[0]
    for (i in 1 until this.size) out += this[i]
    return out
}

/**
 * Matrix class.
 */
class Matrix<T>(val xSize: Int, val ySize: Int, val array: Array<Array<T>>) {

    companion object {

        inline operator fun <reified T> invoke() = Matrix(0, 0, Array(0, { emptyArray<T>() }))

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int) =
            Matrix(xWidth, yWidth, Array(xWidth, { arrayOfNulls<T>(yWidth) }))

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int, operator: (Int, Int) -> (T)): Matrix<T> {
            val array = Array(xWidth) {
                val x = it
                Array(yWidth) { el -> operator(x, el) }
            }
            return Matrix(xWidth, yWidth, array)
        }
    }

    operator fun get(x: Int, y: Int): T {
        return array[x][y]
    }

    operator fun set(x: Int, y: Int, t: T) {
        array[x][y] = t
    }

    inline fun forEach(operation: (T) -> Unit) {
        array.forEach { it.forEach { operation.invoke(it) } }
    }

    inline fun forEachIndexed(operation: (x: Int, y: Int, T) -> Unit) {
        array.forEachIndexed { x, p -> p.forEachIndexed { y, t -> operation.invoke(x, y, t) } }
    }

    fun isUpperHalfFree(): Boolean {
        for (i in 0..ySize) {
            for (j in i..xSize)
                if (this[i, j] != 0) return false
        }
        return true
    }
}

fun Double.bytes(): ByteArray =
    ByteBuffer.allocate(java.lang.Long.BYTES)
        .putLong(java.lang.Double.doubleToLongBits(this)).array()

fun generateNormalizedSequence(
    from: Double = 0.0,
    to: Double = 1.0,
    size: Int,
    normalize: Boolean = true
): Collection<Double> {
    var out = mutableListOf<Double>()
    for (i in 0 until size) {
        out.add(Random.nextDouble(from, to))
    }
    if (normalize) out = out.map { it / out.sum() }.toMutableList()
    return out
}

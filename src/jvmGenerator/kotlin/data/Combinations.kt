package data

import kotlin.math.pow

/**
 * Provides an iterator over all possible orders that a series of [values] can be arranged in.
 *
 * @param[values] The values whose combinations are returned.
 * @param[size] The number of values to include in each combination.
 */
internal class Combinations<T>(values: Iterable<T>, val size: Int) : Iterable<List<T>> {

    private val values = values.toList()

    init {
        require(size >= 0) { "size cannot be negative" }
        require(size <= this.values.size) { "size cannot exceed the number of values" }
    }

    override fun iterator(): Iterator<List<T>> = object : Iterator<List<T>> {
        private var counter = 0

        override fun next(): List<T> {
            if (!hasNext()) throw NoSuchElementException("All combos have been exhausted")

            val combo = buildList(size) {
                // Uses modular arithmetic to get all possible combinations + orders of the values.
                for (i in 0 until this@Combinations.size) {
                    val base = values.size.toDouble()
                    val indexOfValue = counter / base.pow(i) % base
                    add(values[indexOfValue.toInt()])
                }
            }
            counter++

            return combo
        }

        override fun hasNext(): Boolean {
            val maxCounter = values.size.toDouble().pow(size)
            return counter < maxCounter
        }
    }
}
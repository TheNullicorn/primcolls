package me.nullicorn.ooze.primcolls.list

import kotlin.math.ceil
import kotlin.math.min

/**
 * An ordered, dynamically-sized collection of values, all with the same primitive type.
 *
 * Duplicate elements are supported, meaning the same value can be added and retrieved from multiple
 * indices in the same list without replacing previous appearances of it.
 */
abstract class PrimitiveList {

    /**
     * The number of bytes currently in the list.
     *
     * When the list is empty, this is equal to `0`.
     */
    var size: Int = 0
        protected set

    /**
     * The index of the last element in the list.
     *
     * When the list is empty, this is equal to `-1`.
     */
    val lastIndex: Int
        get() = size - 1

    /**
     * Checks if the list has any elements in it.
     *
     * This is equivalent to:
     * ```kotlin
     * size == 0
     * ```
     *
     * @return `false` if the list has any elements. Otherwise `false`.
     */
    fun isEmpty(): Boolean = (size == 0)

    /**
     * The valid range of indices for the list.
     *
     * These values are guaranteed to not cause an [IndexOutOfBoundsException] when passed to a
     * method that accepts an index as a parameter.
     */
    val indices: IntRange
        get() = 0..lastIndex

    /**
     * Removes the element at a given [index] in the list.
     *
     * Any elements at greater indices will have their indices decreased by `1`, filling in the spot
     * where the removed element once was.
     *
     * @param[index] The index of the element to remove.
     *
     * @throws[IndexOutOfBoundsException] if the [index] is less than `0` or greater than the list's
     * [lastIndex].
     */
    fun remove(index: Int) {
        checkIndex(index)

        collapseElementAt(index)
        size--

        shrinkIfExcessive()
    }

    /**
     * Shorthand for [remove]-ing the value at a specific [index].
     */
    operator fun minusAssign(index: Int) = remove(index)

    /**
     * The number of elements that the list's current allocation can hold.
     *
     * The setter for this var should reallocate the list's contents into a new array with the
     * supplied size. If the value is less than the current capacity, any trailing elements should
     * be cut off. If it's larger, the trailing values should be filled with the default value for
     * the primitive.
     */
    protected abstract var capacity: Int

    /**
     * Shifts every element in the internal storage container back by `1` index starting at the
     * [index] specified, effectively removing that element from the list.
     */
    protected abstract fun collapseElementAt(index: Int)

    /**
     * Ensures that a supplied [index] is within the bounds of the list.
     *
     * @return the supplied [index].
     *
     * @throws[IndexOutOfBoundsException] if the [index] is a negative number.
     * @throws[IndexOutOfBoundsException] if the [index] is greater than the list's [lastIndex].
     */
    protected fun checkIndex(index: Int) =
        if (index in 0 until size) index
        else throw IndexOutOfBoundsException("index=$index, size=$size")

    /**
     * Increases the internal [capacity] of the list if there isn't already enough memory to store
     * `n` elements.
     *
     * @param[size] `n`, the number of elements that the list is expected to store.
     *
     * @throws[IllegalStateException] if the expected capacity is too big to allocate.
     */
    protected fun ensureCapacity(size: Int) {
        if (size <= capacity) return

        if (size > MAX_ARRAY_LENGTH) {
            throw IllegalStateException("Not enough memory for size=$size (currently ${this.size})")
        }

        val suggestedCapacity = ceil(size * GROWTH_FACTOR).toInt()
        capacity = min(suggestedCapacity, MAX_ARRAY_LENGTH)
    }

    /**
     * Decreases the internal [capacity] if there is an excessive amount of unused space allocated.
     *
     * This should be called after any operation that decreases the [size] of the list.
     */
    private fun shrinkIfExcessive() {
        if (capacity < MIN_SHRINKABLE_SIZE) return

        val suggestedCapacity = ceil(capacity / SHRINK_FACTOR).toInt()
        if (size <= suggestedCapacity) capacity = suggestedCapacity
    }

    protected companion object {
        const val DEFAULT_CAPACITY = 12

        /**
         * The maximum size of a list's internal storage array.
         */
        private const val MAX_ARRAY_LENGTH = Int.MAX_VALUE - 8

        /**
         * The amount to multiply the list's [capacity] by whenever it [grows][ensureCapacity].
         */
        private const val GROWTH_FACTOR = 1.5

        /**
         * The lowest capacity that will be shrunk.
         *
         * This is to avoid constant reallocation when the list only has a few elements in it.
         */
        private const val MIN_SHRINKABLE_SIZE = 32

        /**
         * The amount to divide the list's [capacity] by whenever it [shrinks][shrinkIfExcessive].
         */
        private const val SHRINK_FACTOR = 2.0

        fun checkCapacity(capacity: Int): Int =
            if (capacity >= 0) capacity
            else throw IllegalArgumentException("capacity cannot be negative: $capacity")
    }
}
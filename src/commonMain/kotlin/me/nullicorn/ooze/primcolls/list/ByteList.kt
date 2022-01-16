package me.nullicorn.ooze.primcolls.list

/**
 * An ordered, dynamically-sized collection of [Byte] values.
 *
 * @param[storage] The internal container for the list's elements.
 * This array's size is typically larger than the [list's][size].
 */
class ByteList private constructor(private var storage: ByteArray) : PrimitiveList() {

    /**
     * Creates an empty list, allocating enough memory to hold a specific number of elements.
     *
     * This is useful if the size (or maximum size) of the data is known ahead of time. This does
     * not restrict the list from growing if more than that many elements are added.
     *
     * If no value is supplied, `12` is used as an arbitrary default.
     *
     * @param[initialCapacity] The maximum number of elements that the list can hold without
     * reallocation.
     *
     * @throws[IllegalArgumentException] if [initialCapacity] is a negative number.
     */
    constructor(initialCapacity: Int = DEFAULT_CAPACITY) : this(
        ByteArray(checkCapacity(initialCapacity))
    )

    /**
     * Appends a [value] to the end of the list.
     *
     * This operation increases the list's [size] and [lastIndex] each by `1`.
     *
     * @param[value] The byte to insert into the list at the [lastIndex].
     */
    fun add(value: Byte) {
        ensureCapacity(size + 1)
        storage[size++] = value
    }

    /**
     * Appends an array of [values] to the end of the list.
     *
     * The [values] are appended in the order that they appear in the supplied array. This means
     * that the last element supplied will be the last element in the list immediately after this
     * operation.
     *
     * This operation increases the list's [size] by the number of [values] supplied.
     *
     * @param[values] Any bytes to append to the list.
     */
    fun addAll(vararg values: Byte) {
        ensureCapacity(size + values.size)

        values.copyInto(storage, destinationOffset = this.size)
        size += values.size
    }

    /**
     * Retrieves the value at a given [index] in the list.
     *
     * @param[index] The `0` based offset of the desired element.
     *
     * @throws[IndexOutOfBoundsException] if the [index] is a negative number.
     * @throws[IndexOutOfBoundsException] if the [index] is greater than the list's [lastIndex].
     */
    fun get(index: Int) = storage[checkIndex(index)]

    /**
     * Retrieves the values of each byte between two indices.
     *
     * @param[fromIndex] The (inclusive) index of the first element to retrieve.
     * @param[toIndex] The (exclusive) index of the last element to retrieve.
     *
     * @throws[IndexOutOfBoundsException] if [fromIndex] or [toIndex] are negative numbers.
     * @throws[IndexOutOfBoundsException] if [fromIndex] or [toIndex] are greater than the list's
     * [lastIndex].
     * @throws[IllegalArgumentException] if [toIndex] is less than [fromIndex].
     */
    fun getAll(fromIndex: Int = 0, toIndex: Int = size): ByteArray {
        checkIndex(fromIndex)
        checkIndex(toIndex)
        require(fromIndex < toIndex) { "toIndex cannot be less than fromIndex" }

        return storage.copyOfRange(fromIndex, toIndex)
    }

    /**
     * Checks if any element in the list is equal to a specific [value].
     *
     * @param[value] The value to compare each element to.
     * @return whether the list contains an equivalent element.
     */
    fun contains(value: Byte): Boolean {
        for (i in 0 until size)
            if (storage[i] == value)
                return true

        return false
    }

    /**
     * Iterates over each element in the list, in order, passing them to a supplied [consumer].
     *
     * @param[consumer] The function to pass each element to.
     *
     * @throws[Throwable] if the consumer throws anything during an iteration.
     */
    fun forEach(consumer: (value: Byte) -> Unit) {
        for (i in 0 until size)
            consumer(storage[i])
    }

    /**
     * Iterates over each element in the list, in order, passing them to a supplied [consumer] along
     * with the element's index in the list.
     *
     * @param[consumer] The function to pass each index/element pair to.
     *
     * @throws[Throwable] if the consumer throws anything during an iteration.
     */
    fun forEachIndexed(consumer: (index: Int, value: Byte) -> Unit) {
        for (i in 0 until size)
            consumer(i, storage[i])
    }

    /**
     * Copies the contents of the list, order preserved, into a [ByteArray] with the same [size].
     *
     * This the equivalent of:
     * ```kotlin
     * getAll(fromIndex = 0, toIndex = size)
     * ```
     *
     * Any element in the resulting array will be equal to the element at the same index in the
     * list.
     *
     * @return an array of all the list's elements, in the same order.
     */
    fun toArray() = storage.copyOf(size)

    override var capacity: Int
        get() = storage.size
        set(newCapacity) {
            require(newCapacity >= 0) { "capacity cannot be negative: $newCapacity" }

            if (newCapacity > storage.size)
                storage = storage.copyOf(newCapacity)
            else if (newCapacity < storage.size)
                storage = storage.copyOfRange(0, newCapacity)
        }

    override fun collapseElementAt(index: Int) {
        storage.copyInto(storage, destinationOffset = index, startIndex = index + 1)
    }

    override fun toString(): String {
        var contents = ""
        for (i in 0 until size) {
            contents += storage[i]
            if (i < lastIndex) contents += ", "
        }
        return "[$contents]"
    }

    companion object Factory {

        /**
         * Creates a new list containing each supplied byte, in the same order that they are
         * supplied.
         *
         * The supplied array is copied, so modifying it after the list is created will not affect
         * the list's contents.
         *
         * ```kotlin
         * ByteList(initialCapacity = contents.size).addAll(*contents)
         * ```
         *
         * @param[contents] the elements to copy into the list.
         */
        fun of(vararg contents: Byte) = ByteList(contents.copyOf())
    }
}
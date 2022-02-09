package me.nullicorn.primcolls.list

/**
 * An ordered, dynamically-sized collection of [Byte] values.
 *
 * Duplicate elements are supported, meaning the same byte value can be added and retrieved from
 * multiple indices in the same list without replacing previous appearances of it.
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
     * Inserts a [value] at a specific [index] in the list.
     *
     * Any values previously at or beyond the [index] will each be shifted to the next index. For
     * example...
     * ```text
     * addAt(index = 4, value = 99)
     *
     * Index  | 0  | 1  | 2  | 3  | 4  | 5  | 6  | 7  | 8  |
     * —————————————————————————————————————————————————————
     * Before | 1  | 1  | 2  | 3  | 5  | 8  | 13 | 21 |
     * After  | 1  | 1  | 2  | 3  | 99 | 5  | 8  | 13 | 21 |
     * ```
     * If [value] is equal to the list's [size], then the function behaves the same as [add]; the
     * value is simply appended to the end of the list, and no other elements are shifted.
     *
     * This operation increases the list's [size] and [lastIndex] each by `1`.
     *
     * @param[index] The index in the list that the value should be inserted at.
     * @param[value] The byte to insert into the list at the [index].
     *
     * @throws[IndexOutOfBoundsException] if the [index] is a negative number.
     * @throws[IndexOutOfBoundsException] if the [index] is greater than the list's [size].
     */
    fun addAt(index: Int, value: Byte) {
        // Use the normal add() behavior if the index == lastIndex + 1 (aka size).
        if (index == size)
            return add(value)

        if (index < 0 || index > size)
            throw IndexOutOfBoundsException("index=$index, size=$size")

        // Shift each element up by 1 index, starting at the index supplied.
        ensureCapacity(size + 1)
        storage.copyInto(
            destination = storage,
            destinationOffset = index + 1,
            startIndex = index,
            endIndex = size
        )

        // Insert the element at the supplied index.
        storage[index] = value
        size++
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
    fun addAll(values: ByteArray) {
        ensureCapacity(size + values.size)

        values.copyInto(storage, destinationOffset = this.size)
        size += values.size
    }

    /**
     * Inserts all [values] from an array into the list, starting at a specific [index].
     *
     * The [values] are inserted in the order that they appear in the supplied array. This means
     * that the first element in the array will be inserted at [index], the second element at
     * [index] `+ 1`, and so on.
     *
     * Any existing list elements at or beyond the [index] will move to the index equal to
     * `i + values.size`, where `i` is the element's current index, and `values.size` is the size of
     * the supplied [values] array.
     *
     * If [index] is equal to the list's [size], then the function behaves the same as [addAll]; the
     * values are simply appended to the end of the list, and no other elements are shifted.
     *
     * This operation increases the list's [size] by the number of [values] supplied.
     *
     * @param[index] The first index in the list that the [values] should start being inserted at.
     * @param[values] The bytes to insert into the list, in order.
     *
     * @throws[IndexOutOfBoundsException] if the [index] is a negative number.
     * @throws[IndexOutOfBoundsException] if the [index] is greater than the list's [size].
     */
    fun addAllAt(index: Int, values: ByteArray) {
        // Use the normal addAll() behavior if the index == lastIndex + 1 (aka size).
        if (index == size)
            return addAll(values)

        if (index < 0 || index > size)
            throw IndexOutOfBoundsException("index=$index, size=$size")

        // Shift each element's index up, starting at the index supplied.
        ensureCapacity(size + values.size)
        storage.copyInto(
            destination = storage,
            destinationOffset = index + values.size,
            startIndex = index,
            endIndex = size
        )

        // Insert the values into the array, starting at the index supplied.
        values.copyInto(storage, destinationOffset = index)
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
    operator fun get(index: Int) = storage[checkIndex(index)]

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

    /**
     * Shorthand for [add]-ing a [value].
     */
    operator fun plusAssign(value: Byte) = add(value)

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
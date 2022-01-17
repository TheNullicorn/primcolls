# Primitive Collections

A lightweight Kotlin/JVM library that offers simple data structures for primitives, which take
advantage of unboxed types' smaller sizes in-memory, especially when used in arrays.

## Help

If anyone interested in contributing has knowledge in code generation, feel free to open an issue or
pull request. I'd like to use a library like [kotlinpoet](https://github.com/square/kotlinpoet)
to make maintenance easier, but I'm not decided on what the best approach to that would be.

## Lists

###### me.nullicorn.ooze.primcolls.list

- Ordered
- Dynamically sized
- Can be converted to primitive arrays
    - And vice-versa
    - e.g. `byte[]` (Java) or `ByteArray` (Kotlin)
- Supports the following operations:
    - `add(value: <Primitive>)` - appends a value to the end of the list -`addAll(<Primitive>[])` -
      appends multiple values, in the same order provided
      <br><br>
    - `get(index: Int): <Primitive>` - retrieves the value at an index
        - `getAll(fromIndex: Int): <Primitive>[]` - retrieves all values between two indices, as one
          array
          <br><br>
    - `remove(index: Int): <Primitive>`- removes the value at an index, shifting any further
      elements back by 1
      <br><br>
    - `contains(value: <Primitive>): Boolean` - checks if the list contains a certain value
      <br><br>
    - `forEach(consumer: (value: <Primitive>) -> Unit)` - iterates over the values in the list in
      order, passing each one to a provided consumer.
        - `forEachIndexed(consumer: (index: Int, value: <Primitive>) -> Unit)` - iterates over the
          values in the list in order, passing each value and its index to a provided consumer.
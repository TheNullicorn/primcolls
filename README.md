# Primitive Collections

A lightweight Kotlin/JVM library that offers simple data structures for primitives, which take
advantage of unboxed types' smaller sizes in-memory, especially when used in arrays.

## Help

If anyone interested in contributing has knowledge in code generation, feel free to open an issue or
pull request. I'd like to use a library like [kotlinpoet](https://github.com/square/kotlinpoet)
to make maintenance easier, but I'm not decided on what the best approach to that would be.

## Lists

###### me.nullicorn.primcolls.list

- Ordered
- Dynamically sized
- Can be converted to primitive arrays
    - And vice-versa
    - e.g. `byte[]` (Java) or `ByteArray` (Kotlin)
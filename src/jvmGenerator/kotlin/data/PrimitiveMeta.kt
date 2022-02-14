package data

import template.FileTemplate
import kotlin.reflect.KClass

/**
 * Basic information about a primitive type in Kotlin, used to fill in a [FileTemplate].
 *
 * @param[name] A user-friendly name for the primitive type.
 * @param[type] A reference to the primitive's Kotlin class, such as [Byte].
 * @param[array] A reference to the Kotlin array class for the primitive, such as [ByteArray].
 */
data class PrimitiveMeta(
    private val name: String,
    private val type: KClass<*>,
    private val array: KClass<*>,
) {
    val typeName
        get() = type.simpleName!!

    val arrayTypeName
        get() = array.simpleName!!

    val friendlyName
        get() = name

    override fun toString() = "$typeName(name=\"$friendlyName\", arrayType=$arrayTypeName)"

    companion object {
        val ALL = setOf(
            PrimitiveMeta("byte", Byte::class, ByteArray::class),
            PrimitiveMeta("short", Short::class, ShortArray::class),
            PrimitiveMeta("int", Int::class, IntArray::class),
            PrimitiveMeta("long", Long::class, LongArray::class),
            PrimitiveMeta("float", Float::class, FloatArray::class),
            PrimitiveMeta("double", Double::class, DoubleArray::class),
            PrimitiveMeta("character", Char::class, CharArray::class),
        )
    }
}
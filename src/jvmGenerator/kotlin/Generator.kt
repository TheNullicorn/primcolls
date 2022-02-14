import data.Combinations
import data.PrimitiveMeta
import template.FileTemplate
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Paths

/**
 * Templates go in the generator's `resources` directory.
 *
 * ## File Names
 * Format:
 * ```text
 * <params>.<name>.template
 * ```
 * Where:
 * - `<params>` - an integer indicating the number of primitive types used by the collection.
 *     - e.g. for maps, this would be `2`, and for lists or sets it would be `1`
 * - `<name>` - the class's parameterized name, using the same template variables as the file's
 * contents. See below for details.
 *
 * It's recommended that the file name matches that of the main class in the file.
 *
 * ## File Contents
 * The generator looks for "keys", which are short, **case-sensitive** strings surrounded by hash
 * signs #. Known keys are:
 * - `type` - The name of the primitive's Kotlin class.
 *     - e.g. `Byte` for bytes or `Char` for characters
 * - `array_type` - The Kotlin name of the primitive's array class.
 *     - e.g. `ByteArray` for bytes or `CharArray` for characters
 * - `friendly_name` - A user-friendly name for the type, recommended for use in KDoc comments.
 *     - e.g. `byte` for bytes or `character` for characters
 *
 * If a template only needs one type, then the key can simply appear in the template surrounded by
 * hash signs #, such as:
 * ```text
 * #type#
 * #array_type#
 * #friendly_name#
 * ```
 * If a template needs multiple types (such as key/value pairs), then the key needs to end in a dot,
 * followed by the type's index, such as:
 * ```text
 * #type.0# - First type's name
 * #type.1# - Second type's name
 * ```
 *
 * For example, a file with the name
 * ```text
 * 2.#type.0##type.1#Map.template
 * ```
 * would produce the following files:
 * ```text
 * ByteByteMap.kt
 * ByteShortMap.kt
 * ByteIntMap.kt
 * ByteLongMap.kt
 * ByteFloatMap.kt
 * ByteDoubleMap.kt
 * ShortByteMap.kt
 * ShortShortMap.kt
 * ...etc
 * ```
 */
fun main(rawArgs: Array<String>) {
    val args = rawArgs.argumentPairs

    // Make sure both of the required paths were provided.
    val inputDir = args["input"]?.let { File(it) }
        ?: throw IllegalArgumentException("No input directory specified!")
    val outputDir = args["output"]?.let { File(it) }
        ?: throw IllegalArgumentException("No output directory specified!")

    // Make sure the input directory *does* exist.
    if (!inputDir.isDirectory)
        throw FileNotFoundException("Input directory doesn't exist, or it isn't a folder")

    // Make sure the output directory *doesn't* exist, but if it does, make sure it isn't a file.
    if (outputDir.isFile)
        throw FileAlreadyExistsException(outputDir, reason = "Directory expected, but got a file")

    outputDir.mkdirs()

    val templates = inputDir
        .walkTopDown()
        .maxDepth(16)
        .filter { it.isFile && it.extension == "template" }
        .map {
            FileTemplate<PrimitiveMeta>(it).apply {
                registerKey("type", PrimitiveMeta::typeName)
                registerKey("array_type", PrimitiveMeta::arrayTypeName)
                registerKey("friendly_name", PrimitiveMeta::friendlyName)
            }
        }

    for (template in templates) {
        val primCombos = Combinations(
            values = PrimitiveMeta.ALL,
            size = template.parametersNeeded
        )

        val relativeOutputPath = template.file.parentFile.canonicalPath.replace(inputDir.canonicalPath, "")

        for (combo in primCombos) {
            val fileName = template.getFilledName(*combo.toTypedArray())
            val fileContents = template.getFilledContents(*combo.toTypedArray())

            Paths.get(outputDir.canonicalPath, relativeOutputPath, "$fileName.kt")
                .toFile()
                .writeText(fileContents)
        }
    }
}


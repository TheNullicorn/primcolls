package template

import java.io.File

/**
 * @param[file] The file containing the template.
 * @param[T] The type of parameter that can be passed to the template to replace keys.
 */
class FileTemplate<T>(val file: File) {

    private val keyFillers = HashMap<String, (T) -> String>()

    val parametersNeeded: Int by lazy {
        // Get the integer before the first dot `.` in the file's name.
        file.nameWithoutExtension
            .substringBefore('.')
            .toUIntOrNull()?.toInt()
            ?: throw InvalidTemplateNameException.malformedParamCount(file.name)
    }

    fun registerKey(key: String, replacement: (T) -> String) {
        keyFillers[key] = replacement
    }

    fun getFilledName(vararg parameters: T): String {
        checkFillerParams(*parameters)
        return file.nameWithoutExtension
            .substringAfter('.')
            .replaceKeys(*parameters)
    }

    fun getFilledContents(vararg parameters: T): String {
        checkFillerParams(*parameters)
        return file.readText().replaceKeys(*parameters)
    }

    private fun checkFillerParams(vararg parameters: T) =
        require(parameters.size == parametersNeeded) {
            "Expected $parametersNeeded parameters, but got ${parameters.size}"
        }

    private fun formatKey(id: String, index: Int) =
        if (parametersNeeded == 1) "#$id#"
        else "#$id.$index#"

    private fun String.replaceKeys(vararg parameters: T): String {
        var result = this

        // For each parameter, fill in all the keys that refer to it with the filler()'s value.
        for ((index, param) in parameters.withIndex())
            for ((keyId, filler) in keyFillers)
                result = result.replace(formatKey(keyId, index), filler(param))

        return result
    }
}
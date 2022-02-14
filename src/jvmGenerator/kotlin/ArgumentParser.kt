private const val ARG_PREFIX = "--"

/**
 * Interprets the array as arguments to a Java program in key/value pairs.
 *
 * Each key is prefixed by the of [ARG_PREFIX]. The key's value consists of every value in the array
 * joined via spaces " " up until the next key starts or the array ends, whichever happens first.
 *
 * For example...
 * ```text
 * --key1 All of this is the value for key1 --key2 And this is key2's value!
 * ```
 * would map to...
 * ```json
 * {
 *     "key1": "All of this is the value for key1",
 *     "key2": "And this is key2's value!"
 * }
 * ```
 *
 * @return the parsed key/value pairs.
 * @throws[IllegalArgumentException] If any arguments are provided before the first key is declared.
 */
internal val Array<String>.argumentPairs: Map<String, String>
    get() {
        val mappedArgs = mutableMapOf<String, MutableList<String>>()
        var currentArg: String? = null

        for (token in this) {
            // Check if the token represents the name of the next argument.
            if (token.startsWith(ARG_PREFIX)) {
                currentArg = token.substring(ARG_PREFIX.length)
                mappedArgs[currentArg] = ArrayList()
                continue
            }

            // Make sure no tokens are received before an argument has been named.
            if (currentArg == null)
                throw IllegalArgumentException("Token \"$token\" must be preceded by argument name")

            // Add the token to the current argument's value.
            mappedArgs[currentArg]?.apply { add(token) }
        }

        // Join the tokens for each argument using spaces.
        return mappedArgs
            .map { it.key to it.value.joinToString(" ") }
            .toMap()
    }
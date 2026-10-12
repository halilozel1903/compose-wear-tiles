package io.github.halilozel1903.weartiles.core

/**
 * Versions for tile resources (images). Wear OS caches resources by version string, so the version
 * must change whenever the images change and stay the same otherwise.
 */
public object ResourceVersion {

    /** The version of a tile without resources. */
    public const val EMPTY: String = "r0"

    /**
     * A stable version for [resources], a map from the resource id used in the layout to what it
     * points at (a drawable resource id or name). The order of the map does not matter.
     */
    public fun of(resources: Map<String, Any>): String {
        if (resources.isEmpty()) return EMPTY
        val canonical = resources.entries
            .map { (id, value) -> "$id=$value" }
            .sorted()
            .joinToString("\n")
        return "r" + fnv1a64(canonical).toULong().toString(16).padStart(16, '0')
    }

    /** A stable version for a set of resource ids. */
    public fun of(ids: Collection<String>): String = of(ids.associateWith { it })

    /** True when a cached version [cached] can still be used for [current]. */
    public fun isCurrent(cached: String?, current: String): Boolean = cached == current

    private fun fnv1a64(text: String): Long {
        var hash = -0x340d631b7bdddcdbL // 0xcbf29ce484222325, the FNV offset basis
        for (byte in text.encodeToByteArray()) {
            hash = hash xor (byte.toLong() and 0xff)
            hash *= 0x100000001b3L
        }
        return hash
    }
}

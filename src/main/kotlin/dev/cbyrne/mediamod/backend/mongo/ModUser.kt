package dev.cbyrne.mediamod.backend.mongo

enum class Mod(val modid: String) {
    MEDIAMOD("mediamod")
}

enum class Version(val version: String) {
    V1_8_9("1.8.9"),
    V1_15_2("1.15.2")
}

data class ModUser(val _id: String, val username: String, var mods: Array<Mod>, var versions: Array<String>, var online: Boolean = true) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModUser

        if (_id != other._id) return false
        if (username != other.username) return false
        if (!mods.contentEquals(other.mods)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _id.hashCode()
        result = 31 * result + username.hashCode()
        result = 31 * result + mods.contentHashCode()
        return result
    }
}
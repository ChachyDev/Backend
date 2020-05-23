package dev.cbyrne.mediamod.backend.mongo

import dev.cbyrne.mediamod.backend.party.MediaModParty
import org.litote.kmongo.*
import org.slf4j.LoggerFactory

object Database {
    private val client = KMongo.createClient()
    private val usersCollection = client.getDatabase("statistics").getCollection<ModUser>("users")
    val partiesCollection = client.getDatabase("parties").getCollection<MediaModParty>("parties")

    private val logger = LoggerFactory.getLogger("Database")

    fun getUserCount(): Int = usersCollection.countDocuments().toInt()
    fun getOnlineUserCount(): Int = usersCollection.find(ModUser::online eq true).count()
    fun getUser(uuid: String): ModUser? = usersCollection.findOneById(uuid)
    fun getUserCountForMod(mod: Mod): Int = usersCollection.find(ModUser::mods `in` mod.modid.toUpperCase()).count()
    fun getOnlineUserCountForMod(mod: Mod): Int = usersCollection.find(ModUser::mods `in` mod.modid.toUpperCase()).filter(ModUser::online eq true).count()

    fun insertUser(user: ModUser) {
        usersCollection.insertOne(user)
    }

    fun updateUser(user: ModUser) {
        usersCollection.updateOneById(user._id, user)
    }

    init {
        logger.info("Initialising...")
        logger.info("All user count: " + getUserCount())
        logger.info("Online user count: " + getOnlineUserCount())

        logger.info("Ready!")
    }
}
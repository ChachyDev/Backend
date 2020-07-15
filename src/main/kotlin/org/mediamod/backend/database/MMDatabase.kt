package org.mediamod.backend.database

import com.mongodb.MongoClientSettings
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.mediamod.backend.database.schema.Party
import org.mediamod.backend.database.schema.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

private val logger: Logger = LoggerFactory.getLogger("mediamod.Database")

class MMDatabase {
    private val client: CoroutineClient
    private val database: CoroutineDatabase

    private val usersCollection: CoroutineCollection<User>
    private val partiesCollection: CoroutineCollection<Party>

    init {
        logger.info("Initialising...")
        val start = System.currentTimeMillis()

        client = KMongo.createClient(
            MongoClientSettings.builder().uuidRepresentation(UuidRepresentation.STANDARD).build()
        ).coroutine
        database = client.getDatabase("mediamod")
        usersCollection = database.getCollection("users")
        partiesCollection = database.getCollection("parties")

        val stop = System.currentTimeMillis()
        logger.info("Initialised in " + (stop - start) + "ms")
    }

    /**
     * Makes a user object and puts it into the users collection
     *
     * @param uuid: The user's UUID
     * @param username: The user's Minecraft name
     * @param currentMod: The mod that sent the request
     *
     * @return The request secret that the mod will need to make future requests
     */
    suspend fun createUser(uuid: String, username: String, currentMod: String): String {
        val user = User(uuid, username, UUID.randomUUID().toString(), arrayOf(currentMod), true)
        usersCollection.insertOne(user)

        return user.requestSecret
    }

    /**
     * Marks an existing user as online, called when the mod is initialising
     *
     * @param uuid: The user's UUID
     * @return The request secret that the mod will need to make future requests
     */
    suspend fun loginUser(uuid: UUID): String {
        return (usersCollection.findOne(User::_id eq uuid.toString()) ?: return "").requestSecret
    }

    /**
     * Checks if a user already exists in the database
     *
     * @param uuid: The user's UUID
     */
    suspend fun doesUserExist(uuid: UUID) = usersCollection.findOne(User::_id eq uuid.toString()) != null

    /**
     * Returns the user from the database with the corresponding UUID
     *
     * @param uuid: The user's uuid
     */
    suspend fun getUser(uuid: UUID) = usersCollection.findOne(User::_id eq uuid.toString())

}
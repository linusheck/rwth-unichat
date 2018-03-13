package me.glatteis.unichat.chat

import me.glatteis.unichat.crawler.RandomStringGenerator
import org.joda.time.DateTime
import sun.security.rsa.RSAPublicKeyImpl
import java.math.BigInteger
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import kotlin.concurrent.timer


object UserIdentification {

    /**
     * This uses a challengeString-and-response authentication method.
     * Step 1: The user creates an RSA private & public key.
     * Step 2: The user sends their public key to the server.
     * Step 3: The server generates a random string and encrypts it with the user's public key.
     * Step 4: To prove that the user owns the private key associated to their public key, the server sends that
     * string to the user to be decrypted using their private key.
     * Step 5: The user sends the decrypted string (which should be the original random string) back to the server.
     * Step 6: The server checks if the returned string matches with the original string. If yes, we are done.
     */

    data class Challenge(val key: BigInteger, val challengeString: String, val time: DateTime)

    private val openChallenges = ConcurrentHashMap<BigInteger, Challenge>()

    init {
        timer(daemon = true, period = 60_000L) {
            openChallenges.clear()
            openChallenges.putAll(openChallenges.filterNot { (_, c) ->
                DateTime.now().isAfter(c.time.plusHours(1))
            })
        }
    }

    private const val MODE = "RSA/ECB/PKCS1Padding"

    private val randomStringGenerator = RandomStringGenerator(SecureRandom())

    fun createChallenge(publicKeyBase64: String): String {
        val key = getPublicKey(publicKeyBase64)
        val cipher = Cipher.getInstance(MODE)
        if (key.modulus.bitLength() != 1024) {
            throw InvalidKeyException("Key modulus length not 1024")
        }

        cipher.init(Cipher.ENCRYPT_MODE, key)
        // The random challenge
        val byteArray = randomStringGenerator.randomString(32).toByteArray(charset("utf-8"))
        val challenge = Base64.getEncoder().encodeToString(byteArray)
        openChallenges[key.modulus] = Challenge(key.modulus, challenge, DateTime.now())
        val final = cipher.doFinal(byteArray)
        return Base64.getEncoder().encodeToString(final)
    }

    fun verifyChallenge(publicKeyBase64: String, result: String): Boolean {
        val publicKey = getPublicKey(publicKeyBase64).modulus
        val challenge = openChallenges[publicKey] ?: return false
        return challenge.challengeString == result
    }

    fun getPublicKey(pem: String): RSAPublicKeyImpl {
        val keyBytes = Base64.getDecoder().decode(pem)
        val spec = X509EncodedKeySpec(keyBytes)
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePublic(spec) as RSAPublicKeyImpl
    }

}
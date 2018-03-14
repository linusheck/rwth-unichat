package me.glatteis.unichat.chat;

import org.junit.jupiter.api.Test;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserIdentificationTest {

    private String publicKeyToPem(RSAPublicKeyImpl rsaPublicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory f = KeyFactory.getInstance("RSA");
        BigInteger modulus = rsaPublicKey.getModulus();
        BigInteger exp = new BigInteger("10001", 16);
        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exp);
        PublicKey pub = f.generatePublic(spec);
        byte[] data = pub.getEncoded();
        return Base64.getEncoder().encodeToString(data);
    }


    @Test
    void userIdTest() throws Exception {

        /*
        This method tests a valid key exchange.

        To achieve unique and unfakable ids, challenge-and-response authentication is used.

        Step 1: The user creates an RSA private & public key.
        Step 2: The user sends their public key to the server.
        Step 3: The server generates a random string and encrypts it with the user's public key.
        Step 4: To prove that the user owns the private key associated to their public key, the server sends that
                string to the user to be decrypted using their private key.
        Step 5: The user sends the decrypted string (which should be the original random string) back to the server.
        Step 6: The server checks if the returned string matches with the original string. If yes, we are done.
         */

        // Generate the RSA key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        RSAPublicKeyImpl rsaPublicKey = (RSAPublicKeyImpl) keyPair.getPublic();
        assertTrue(rsaPublicKey.getModulus().bitLength() == 1024);

        // Generate the base64 encoded public key
        String base64PublicKey = publicKeyToPem(rsaPublicKey);

        System.out.println(base64PublicKey);

        /*
        Once you have your RSA key, ask for a challenge using your public key

        Send this message:
        {
            "type": "challenge",
            "user-id": <your base64 public key>
        }

        You will receive this message:
        {
            "type": "challenge"
            "challenge": <challenge string> (base64)
        }
         */

        // This line gets the challenge string from internals (ignore)
        String challengeString = UserIdentification.INSTANCE.createChallenge(base64PublicKey);


        // Decode the challenge to bytes
        byte[] challenge = Base64.getDecoder().decode(challengeString);

        // Decrypt the challengeString using the Cipher class
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        byte[] decrypted = cipher.doFinal(challenge);
        String decryptedBase64 = Base64.getEncoder().encodeToString(decrypted);

        /*
        Send the login message with your user-id and decrypted string:
            {
                "type": "login",
                "room": "abc123",
                "username": "Supergrobi"
                "user-id": <your public string>
                "challenge-response": <your challenge response>
            }
         */


        assertTrue(UserIdentification.INSTANCE.verifyChallenge(base64PublicKey, decryptedBase64),
                "Decrypted string has to equal original string");
    }
}
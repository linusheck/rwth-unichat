package me.glatteis.unichat.chat;

import org.junit.jupiter.api.Test;
import sun.security.rsa.RSAPublicKeyImpl;

import javax.crypto.Cipher;
import java.lang.reflect.Array;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UserIdentificationTest {

    @Test
    void UserIdTest() throws Exception {
        // Generate the RSA key pair
        KeyPair keyPair = KeyPairGenerator.getInstance("RSA").genKeyPair();
        RSAPublicKeyImpl rsaPublicKey = (RSAPublicKeyImpl) keyPair.getPublic();
        assertTrue(rsaPublicKey.getModulus().bitLength() == 1024);

        String base64PublicKey = Base64.getEncoder().encodeToString(rsaPublicKey.getModulus().toByteArray());
        System.out.println(base64PublicKey.length());

        /*
        Once you have your RSA key, ask for a challenge using your public key modulus

        Send this message:
        {
            "type": "challenge",
            "user-id": <your public key modulus>
        }

        You will receive this message:
        {
            "type": "challenge"
            "challenge": <challenge string> (base64)
        }
         */

        String challengeString = UserIdentification.INSTANCE.createChallenge(base64PublicKey);

        System.out.println(challengeString);
        byte[] challenge = Base64.getDecoder().decode(challengeString);
        System.out.println(Arrays.toString(challenge));
        System.out.println(challenge.length);

        // Decrypt the challengeString using the Cipher class
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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

        assertTrue(UserIdentification.INSTANCE.verifyChallenge(rsaPublicKey.getModulus(), decryptedBase64),
                "Decrypted string has to equal original string");
    }

}
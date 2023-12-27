package org.example;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.example.crypt.RSAEncryptor;

import java.util.Random;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    public AppTest( String testName )
    {
        super( testName );
    }

    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    public void testApp() {
        int size = 128;
        Random random = new Random();
        RSAEncryptor rsaEncryptorAlice = new RSAEncryptor(size);
        rsaEncryptorAlice.generateKey();
        String str = String.valueOf(random.nextDouble());
        String sign = rsaEncryptorAlice.generateSignature(str);

        RSAEncryptor rsaEncryptorBob = new RSAEncryptor(rsaEncryptorAlice.n, rsaEncryptorAlice.e);
        String proto = rsaEncryptorBob.calculatePrototype(sign);

        assertEquals(str, proto);
    }
}

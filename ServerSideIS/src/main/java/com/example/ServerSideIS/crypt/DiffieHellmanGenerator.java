package com.example.ServerSideIS.crypt;


import com.example.ServerSideIS.util.tuple.Tuple3;
import com.example.ServerSideIS.util.prime.RandomPrime;

import java.math.BigInteger;

public class DiffieHellmanGenerator {
    public BigInteger a;
    public BigInteger b;
    public BigInteger g;
    public BigInteger p;

    public BigInteger A;
    public BigInteger B;
    public BigInteger K;

    private final int numSize;

    public static BigInteger findPrimitiveRoot(BigInteger p) {
        BigInteger phi = p.subtract(BigInteger.ONE);
        BigInteger[] factors = new BigInteger[] {BigInteger.ONE, BigInteger.TWO,
                phi.divide(BigInteger.TWO)};

        for (BigInteger g = BigInteger.TWO; g.compareTo(p) < 0; g = g.add(BigInteger.ONE)) {
            boolean isPrimitive = true;

            for (BigInteger factor : factors) {
                isPrimitive = !g.modPow(factor, p).equals(BigInteger.ONE);
                if (!isPrimitive) break;
            }

            if (isPrimitive && g.modPow(phi, p).equals(BigInteger.ONE)) {
                return g;
            }
        }

        return BigInteger.ZERO;
    }

    public DiffieHellmanGenerator(int numSize) {
        this.numSize = numSize;
    }

    public Tuple3<BigInteger, BigInteger, BigInteger> initAlice() {
        a = RandomPrime.randBigInteger(128);
        System.out.println(a);
        p = RandomPrime.randPrime(numSize);
        while (!p.subtract(BigInteger.ONE).divide(BigInteger.TWO)
                .isProbablePrime(10)) {
            p = RandomPrime.randPrime(numSize);
        }

        g = findPrimitiveRoot(p);

        A = g.modPow(a, p);

        return new Tuple3<>(g, p, A);
    }

    public BigInteger initBob(BigInteger g, BigInteger p) {
        this.p = p;
        b = RandomPrime.randBigInteger(128);
        B = g.modPow(b, p);

        return B;
    }

    public BigInteger getKeyAlice(BigInteger B) {
        this.B = B;
        K = B.modPow(a, p);
        return K;
    }

    public BigInteger getKeyBob(BigInteger A) {
        this.A = A;
        K = this.A.modPow(b, p);
        return K;
    }
}

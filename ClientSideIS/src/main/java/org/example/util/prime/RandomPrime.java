package org.example.util.prime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomPrime {

    static public BigInteger randBigInteger(int size) {
        if (size == 0) return BigInteger.ZERO;
        List<Character> binList = new ArrayList<>();
        StringBuilder binString = new StringBuilder();
        for (int i = 0; i < size; i++) {
            binList.add(i % 2 == 0 ? '1' : '0');
        }
        Collections.shuffle(binList);
        binList.set(0, '1');
        binList.forEach(binString::append);
        return new BigInteger(binString.toString(), 2);
    }

    static public BigInteger randBigInteger(BigInteger min, BigInteger max) {
        BigInteger bigInteger = max.subtract(min);
        Random randNum = new Random();
        int len = max.bitLength();
        BigInteger res = new BigInteger(len, randNum);
        if (res.compareTo(min) < 0)
            res = res.add(min);
        if (res.compareTo(bigInteger) >= 0)
            res = res.mod(bigInteger).add(min);
        return res;
    }

    static private BigInteger jacobi(BigInteger a, BigInteger b) {
        if (!a.gcd(b).equals(BigInteger.ONE))
            return BigInteger.ZERO;

        BigInteger r = BigInteger.ONE;

        if (a.compareTo(BigInteger.ZERO) < 0) {
            a = a.negate();
            if (b.mod(new BigInteger("4")).equals(new BigInteger("3")))
                r = r.negate();
        }

        BigInteger t;
        while (!a.equals(BigInteger.ZERO)) {
            t = BigInteger.ZERO;
            while (a.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                t = t.add(BigInteger.ONE);
                a = a.divide(BigInteger.TWO);
            }

            if (!t.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
                if (b.mod(new BigInteger("8")).equals(new BigInteger("3")) ||
                        b.mod(new BigInteger("8")).equals(new BigInteger("5"))) {
                    r = r.negate();
                }
            }

            if (a.mod(BigInteger.TWO).equals(new BigInteger("3")) &&
                    b.mod(new BigInteger("4")).equals(new BigInteger("3"))) {
                r = r.negate();
            }

            BigInteger c = a;
            a = b.mod(c);
            b = c;
        }
        return r;
    }

    static public boolean solovayStrassenTest(BigInteger n, int k) {
        BigInteger n1Half = n.subtract(BigInteger.ONE).divide(BigInteger.TWO);
        for (int i = 0; i < k; i++) {
            BigInteger a = randBigInteger(BigInteger.ZERO, n.subtract(BigInteger.ONE)).add(BigInteger.ONE);

            if (a.gcd(n).compareTo(BigInteger.ONE) > 0 || !a.modPow(n1Half, n).equals(jacobi(a, n).mod(n))) {
                return false;
            }
        }
        return true;
    }


    static public BigInteger randPrime(int size) {
        BigInteger prime;
        do {
            prime = randBigInteger(size);
        } while (!solovayStrassenTest(prime, 7));
        return prime;
    }
}

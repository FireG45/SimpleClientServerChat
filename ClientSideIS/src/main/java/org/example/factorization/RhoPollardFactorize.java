package org.example.factorization;

import java.math.BigInteger;

public class RhoPollardFactorize {

    public static BigInteger f(BigInteger val, BigInteger n) {
        return (val.multiply(val).add(BigInteger.ONE)).mod(n);
    }

    public static BigInteger factorize(BigInteger n) {
        if (n.equals(BigInteger.ONE))
            return n;
        if (n.mod(BigInteger.TWO).equals(BigInteger.ZERO))
            return n;

        BigInteger x = BigInteger.TWO, y = BigInteger.TWO, d = BigInteger.ONE;

        while (!d.equals(n)) {
            x = f(x, n);
            y = f(f(y, n), n);

            d = x.subtract(y).abs().gcd(n);
        }

        return d;
    }

}

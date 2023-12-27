package com.example.ServerSideIS.crypt;

import com.example.ServerSideIS.charEncoding.CharEncoder;
import com.example.ServerSideIS.charEncoding.UTFCharEncoder;
import com.example.ServerSideIS.util.tuple.Tuple;
import com.example.ServerSideIS.util.prime.RandomPrime;

import java.math.BigInteger;
import java.util.*;

public class RSAEncryptor {

    static final String alphabetRU = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя1234567890";
    static final String alphabetEN = "abcdefghijklmnopqrstuvwxyz1234567890";

    private String alphabet;
    private HashMap<Character, Integer> alphabetMap = new HashMap<>();
    private int alphabetPower = 0;
    public int keySize;

    private CharEncoder charEncoder = new UTFCharEncoder();

    public BigInteger p = new BigInteger("0");
    public BigInteger q = new BigInteger("0");
    public BigInteger n = new BigInteger("0");
    public BigInteger e = new BigInteger("0");
    public BigInteger d = new BigInteger("0");
    public BigInteger fi = new BigInteger("0");

    public RSAEncryptor(String alphabetCode, int size, BigInteger p, BigInteger q, BigInteger n, BigInteger e,
                        BigInteger d, BigInteger fi) {
        if (Objects.equals(alphabetCode, "RU")) {
            alphabet = alphabetRU;
        } else {
            alphabet = alphabetEN;
        }
        alphabetMap = new HashMap<>();
        alphabetPower = alphabet.length();
        for (int i = 0; i < alphabet.length(); i++) {
            alphabetMap.put(alphabet.charAt(i), i);
        }
        keySize = size;
    }

    public RSAEncryptor(int size, BigInteger p, BigInteger q, BigInteger n, BigInteger e, BigInteger d, BigInteger fi) {
        keySize = size;
    }

    public RSAEncryptor(int size) {
        keySize = size;
    }

    public RSAEncryptor(BigInteger n, BigInteger e) {
        this.e = e;
        this.n = n;
    }

    public void generateKey() {
        p = RandomPrime.randPrime(keySize);
        q = RandomPrime.randPrime(keySize);
        n = p.multiply(q);
        fi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
        e = calculateOpenExponent();
        Tuple<BigInteger, BigInteger> bezout = extendedGCD(e, fi);
        d = bezout.first;
        while (d.compareTo(BigInteger.ZERO) < 0) {
            e = calculateOpenExponent();
            bezout = extendedGCD(e, fi);
            d = bezout.first;
            System.out.println(d.toString());
        }
    }

    private Tuple<BigInteger, BigInteger> extendedGCD(BigInteger a, BigInteger b) {
        BigInteger s = BigInteger.ZERO;
        BigInteger old_s = BigInteger.ONE;
        BigInteger r = b;
        BigInteger old_r = a;

        while (!r.equals(BigInteger.ZERO)) {
            BigInteger quotient = old_r.divide(r);

            BigInteger t_old_r = old_r;
            old_r = r;
            r = t_old_r.subtract(quotient.multiply(r));

            BigInteger t_old_s = old_s;
            old_s = s;
            s = t_old_s.subtract(quotient.multiply(s));
        }

        BigInteger bezout_t;
        if (!b.equals(BigInteger.ZERO))
            bezout_t = (old_r.subtract(old_s.multiply(a))).divide(b);
        else
            bezout_t = BigInteger.ZERO;

        return new Tuple<>(old_s, bezout_t);
    }

    private BigInteger calculateOpenExponent() {
        BigInteger e = RandomPrime.randBigInteger(BigInteger.TWO, n);

        while (!e.gcd(fi).equals(BigInteger.ONE)) {
            e = RandomPrime.randBigInteger(BigInteger.TWO, n);
        }
        return e;
    }

    public String generateSignature(String string) {
        charEncoder = new UTFCharEncoder();
        StringBuilder encrypted = new StringBuilder();
        List<BigInteger> charIndexes = new LinkedList<>();
        for (int i = 0; i < string.length(); i++) {
            charIndexes.add(BigInteger.valueOf(charEncoder.getCharIndex(string.charAt(i))));
        }

        for (BigInteger index : charIndexes) {
            BigInteger c = index.modPow(d, n);
            encrypted.append(c).append(" ");
        }
        return encrypted.toString();
    }

    public String calculatePrototype(String signature) {
        charEncoder = new UTFCharEncoder();
        StringBuilder decrypted = new StringBuilder();
        List<BigInteger> charIndexes = new LinkedList<>();
        String[] split = signature.split(" ");

        for (int i = 0; i < split.length; i++) {
            String code = split[i];
            charIndexes.add(new BigInteger(code));
        }

        for (BigInteger index : charIndexes) {
            BigInteger c = index.modPow(e, n);
            decrypted.append(charEncoder.getCharByIndex(c.intValue()));
        }

        return decrypted.toString();
    }

    private boolean checkKey() {
        boolean check = !p.equals(BigInteger.ZERO) && !q.equals(BigInteger.ZERO) && !e.equals(BigInteger.ZERO) &&
                !d.equals(BigInteger.ZERO) && !fi.equals(BigInteger.ZERO);
        check &= !RandomPrime.solovayStrassenTest(p, 5);
        check &= !RandomPrime.solovayStrassenTest(q, 5);
        return check;
    }

    private boolean checkText(String string) {
        if (string.isEmpty()) return false;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (alphabet.indexOf(Character.toLowerCase(c)) == -1) {
                return false;
            }
        }
        return true;
    }

    private boolean checkCryptText(String string) {
        if (string.isEmpty()) return false;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (!Character.isSpaceChar(c) && !Character.isDigit(c) && c != '-') {
                return false;
            }
        }
        return true;
    }

    public void setLanguage(String alphabetCode) {
        if (Objects.equals(alphabetCode, "RU")) {
            alphabet = alphabetRU;
        } else {
            alphabet = alphabetEN;
        }
    }
}

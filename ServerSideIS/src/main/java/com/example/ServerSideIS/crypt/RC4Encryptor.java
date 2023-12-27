package org.example.crypt;


public class RC4Encryptor {
    private char[] S = new char[256];
    private int x = 0;
    private int y = 0;

    public RC4Encryptor(char[] key) {
        init(key);
    }

    private void init(char[] key) {
        int keyLength = key.length;

        for (int i = 0; i < 256; i++) {
            S[i] = (char) i;
        }

        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + key[i % keyLength]) % 256;
            swap(S, i, j);
        }
    }

    public char[] encode(char[] data, int size) {
        char[] cipher = new char[size];

        for (int m = 0; m < size; m++) {
            cipher[m] = (char) (data[m] ^ keyItem());
        }

        return cipher;
    }

    public char[] decode(char[] data, int size) {
        return encode(data, size);
    }

    private char keyItem() {
        x = (x + 1) % 256;
        y = (y + S[x]) % 256;
        swap(S, x, y);
        return S[(S[x] + S[y]) % 256];
    }

    private void swap(char[] array, int index1, int index2) {
        char temp = array[index1];
        if (index2 < 0) System.out.println(index1 + " " + index2);
        array[index1] = array[index2];
        array[index2] = temp;
    }
}
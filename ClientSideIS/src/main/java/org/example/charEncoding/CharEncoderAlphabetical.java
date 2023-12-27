package org.example.charEncoding;

import java.util.HashMap;
import java.util.Objects;

public class CharEncoderAlphabetical implements CharEncoder {
    private final String alphabet;
    private final int alphabetPower;

    public CharEncoderAlphabetical(String alphabet) {
        this.alphabet = alphabet;
        alphabetPower = alphabet.length();
    }

    @Override
    public int getCharIndex(char c) {
        return alphabet.indexOf(c);
    }

    @Override
    public char getCharByIndex(int index) {
        if (index < 0) index += alphabetPower;
        if (index < 0) {
            int a = 0;
        }
        return alphabet.charAt(index % alphabetPower);
    }
}

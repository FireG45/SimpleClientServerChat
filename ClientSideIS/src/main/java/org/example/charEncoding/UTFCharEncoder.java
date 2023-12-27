package org.example.charEncoding;

public class UTFCharEncoder implements CharEncoder {
    @Override
    public int getCharIndex(char c) {
        return c;
    }

    @Override
    public char getCharByIndex(int index) {
        return (char) index;
    }
}

package com.example.ServerSideIS.charEncoding;

public interface CharEncoder {
    int getCharIndex(char c);

    char getCharByIndex(int index);
}

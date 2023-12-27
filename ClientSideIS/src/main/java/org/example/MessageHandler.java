package org.example;

import org.example.crypt.RC4Encryptor;
import org.example.crypt.RSAEncryptor;
import org.example.util.LogType;
import org.example.util.LoggerService;
import org.example.util.MessageScreen;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageHandler extends Thread {

    private ServerSocket server;
    private final JTextArea messageDisplay;
    private final String key;
    private final LoggerService loggerService;

    public MessageHandler(JTextArea messageDisplay, String key, LoggerService loggerService) {
        this.messageDisplay = messageDisplay;
        this.key = key;
        this.loggerService = loggerService;
    }

    @Override
    public void run() {
        try {
            int socketPort = 4006;
            server = new ServerSocket(socketPort);
            while (true) {
                RC4Encryptor rc4Encryptor = new RC4Encryptor(key.toCharArray());
                Socket clientSocket = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = in.readLine();
                loggerService.log(LogType.INFO, "Получено сообщение:" + message);

                String signature = in.readLine();
                loggerService.log(LogType.INFO, "Получена подпись:" + signature);
                String nn = in.readLine();
                loggerService.log(LogType.INFO, "Получена n:" + nn);
                String ee = in.readLine();
                loggerService.log(LogType.INFO, "Получена e:" + ee);

                BigInteger n = new BigInteger(nn);
                BigInteger e = new BigInteger(ee);

                RSAEncryptor rsaEncryptor = new RSAEncryptor(n, e);
                String prototype = rsaEncryptor.calculatePrototype(signature);

                if (!prototype.equals(message)) {
                    loggerService.log(LogType.WARN, "Попытка передачи сообщения с неверной подписью!");
                    App.showErrorMessage("Попытка передачи сообщения с неверной подписью!");
                    continue;
                }
                message = MessageScreen.unscreen(message);
                String decoded = new String(rc4Encryptor.decode(message.toCharArray(), message.toCharArray().length));
                messageDisplay.setText(messageDisplay.getText() + decoded + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                App.showErrorMessage(e.getLocalizedMessage());
            }
        }
    }
}
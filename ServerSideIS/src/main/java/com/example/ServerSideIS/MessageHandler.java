package com.example.ServerSideIS;

import com.example.ServerSideIS.crypt.RSAEncryptor;
import com.example.ServerSideIS.services.logger.LogType;
import com.example.ServerSideIS.services.logger.LoggerService;
import com.example.ServerSideIS.util.MessageScreen;

import javax.swing.*;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

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
            int socketPort = 4005;
            server = new ServerSocket(socketPort);
            while (true) {
                org.example.crypt.RC4Encryptor rc4Encryptor = new org.example.crypt.RC4Encryptor(key.toCharArray());
                Socket clientSocket = server.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = in.readLine();
                String signature = in.readLine();
                BigInteger n = new BigInteger(in.readLine());
                BigInteger e = new BigInteger(in.readLine());

                RSAEncryptor rsaEncryptor = new RSAEncryptor(n, e);
                String prototype = rsaEncryptor.calculatePrototype(signature);

                if (!prototype.equals(message)) {
                    loggerService.log(LogType.WARN, "Попытка передачи сообщения с неверной подписью!");
                    ServerSideIsApplication.showErrorMessage("Попытка передачи сообщения с неверной подписью!");
                    continue;
                }
                message = MessageScreen.unscreen(message);

                String decoded = new String(rc4Encryptor.decode(message.toCharArray(), message.toCharArray().length));
                messageDisplay.setText(messageDisplay.getText() + decoded + "\n");
            }
        } catch (IOException e) {
            ServerSideIsApplication.showErrorMessage(e.getLocalizedMessage());
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                ServerSideIsApplication.showErrorMessage(e.getLocalizedMessage());
            }
        }
    }
}

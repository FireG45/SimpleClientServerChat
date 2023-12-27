package com.example.ServerSideIS.services.logger;

import javax.swing.*;
import java.util.Date;

public class LoggerService {

    private final JTextArea loggerTextField;

    public LoggerService(JTextArea loggerTextField) {
        this.loggerTextField = loggerTextField;
    }

    public void log(LogType logType, String text) {
        loggerTextField.setText(loggerTextField.getText() +
                new Date().toString() + " " + logType.name + " " + text + "\n\n");
    }

}

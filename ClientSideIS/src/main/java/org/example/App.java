package org.example;

import org.example.crypt.DiffieHellmanGenerator;
import org.example.crypt.RC4Encryptor;
import org.example.crypt.RSAEncryptor;
import org.example.util.LogType;
import org.example.util.LoggerService;
import org.example.util.MessageScreen;
import org.example.util.SHA1Hash;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class App {
    static final int MAX_DELAY = 10000;
    static final int MIN_DELAY = 0;

    static JLabel delayLabel = new JLabel("Задержка перед отправкой супер-хеша (мс):");
    static JSlider delaySlider = new JSlider(JSlider.HORIZONTAL, MIN_DELAY, MAX_DELAY, MIN_DELAY);
    static JLabel loginLabel = new JLabel("Логин");
    static JTextField loginField = new JTextField();
    static JLabel passwordLabel = new JLabel("Пароль");
    static JPasswordField passwordField = new JPasswordField();
    static JButton signInButton = new JButton("Войти");
    static JButton loggerButton = new JButton("Показать логи");
    static JButton chatButton = new JButton("Показать чат");
    static JFrame loggerFrame = new JFrame("Лог клиента");
    static JFrame messagesFrame = new JFrame("Чат клиента");
    static JButton clearLoggerButton = new JButton("Очистить логи");
    static JButton sendButton = new JButton("Отправить");
    static JTextField messageField = new JTextField();
    static JTextArea display = new JTextArea ( 25, 58 );
    static JTextArea messageDisplay = new JTextArea ( 16, 58 );

    static ActionListener buttonActionListener;

    static int keySize = 48;

    static String user;

    private static BufferedReader in;
    private static BufferedWriter out;

    private static LoggerService logger;

    private static BigInteger K;

    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(JFrame.getFrames()[0], message,
                "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    public static void showMessage(String message) {
        JOptionPane.showMessageDialog(JFrame.getFrames()[0], message,
                "Уведомление", JOptionPane.INFORMATION_MESSAGE);
    }

    public static class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch (actionEvent.getActionCommand()) {
                case "login" -> {
                    handleLogin();
                }
                case "logger" -> {
                    if (loggerFrame.isVisible()) {
                        loggerFrame.setVisible(false);
                        loggerButton.setText("Показать логи");
                    } else {
                        loggerFrame.setVisible(true);
                        loggerButton.setText("Скрыть логи");
                    }
                }
                case "chat" -> {
                    if (messagesFrame.isVisible()) {
                        messagesFrame.setVisible(false);
                        chatButton.setText("Показать чат");
                    } else {
                        messagesFrame.setVisible(true);
                        chatButton.setText("Скрыть чат");
                    }
                }
                case "send" -> {
                    sendMessage();
                }
            }
        }
    }

    private static void handleLogin() {
        if (loginField.getText().isEmpty() || passwordField.getPassword().length == 0) {
            showErrorMessage("Введите логин и пароль!");
            logger.log(LogType.WARN, "Попытка авторизации с пустыми полями ввода!");
            return;
        }
        try (Socket clientSocket = new Socket("localhost", 4004)) {
            in = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(
                    clientSocket.getOutputStream()));
            logger.log(LogType.INFO, "Установлено соединение с сервером!: " + clientSocket.getInetAddress()
                    + " порт: " + clientSocket.getPort());
            String username = loginField.getText();
            String password = SHA1Hash.hashCode(Arrays.toString(passwordField.getPassword()));
            String word = getWord(username);
            if (checkWord(word)) {
                logger.log(LogType.WARN, "Попытка авторизации несуществующего пользователя");
                showErrorMessage("Попытка авторизации несуществующего пользователя");
            } else {
                sendSuperHash(word, password);
                try {
                    keySize = Integer.parseInt(in.readLine());
                    logger.log(LogType.INFO, "Получен размер парамеров: " + keySize);
                    display.updateUI();
                } catch (NumberFormatException ex) {
                    showErrorMessage("Неверный пароль или слово-вызов истекло!");
                    logger.log(LogType.WARN, "Неудачная попытка авторизации! " +
                            "или слово-вызов истекло");
                    return;
                }
                DiffieHellmanGenerator diffieHellmanGenerator = new DiffieHellmanGenerator(keySize);
                BigInteger A = exchangeParams(diffieHellmanGenerator);
                MessageHandler messageHandler = getMessageHandler(diffieHellmanGenerator, A, username);
                messageHandler.start();
            }
        } catch (IOException e) {
            showErrorMessage(e.getLocalizedMessage());
            logger.log(LogType.ERROR, e.getLocalizedMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                showErrorMessage(e.getLocalizedMessage());
                logger.log(LogType.ERROR, e.getLocalizedMessage());
            }
        }
    }

    private static boolean checkWord(String word) {
        return word.charAt(0) != 'C';
    }

    private static String getWord(String username) throws IOException {
        out.write(username + "\n");
        out.flush();
        logger.log(LogType.INFO, "Отправлен запрос на авторизацию с логином: " + username);
        return in.readLine();
    }

    private static MessageHandler getMessageHandler(DiffieHellmanGenerator diffieHellmanGenerator, BigInteger A, String username) {
        K = diffieHellmanGenerator.getKeyBob(A);
        logger.log(LogType.INFO, "Сгенерированный распределенный ключ: " + K);
        user = username;
        chatButton.setEnabled(true);
        chatButton.doClick();
        return new MessageHandler(messageDisplay, K.toString(), logger);
    }

    private static void sendSuperHash(String word, String password) throws InterruptedException, IOException {
        logger.log(LogType.INFO, "Получено слово-вызов: " + word);
        if (delaySlider.getValue() != 0) Thread.sleep(delaySlider.getValue());
        String superHash = SHA1Hash.hashCode(SHA1Hash.hashCode(password) + word);
        out.write(superHash + "\n");
        out.flush();
        logger.log(LogType.INFO, "Отправлен супер-хеш: " + superHash);
    }

    private static BigInteger exchangeParams(DiffieHellmanGenerator diffieHellmanGenerator) throws IOException {
        BigInteger g = new BigInteger(in.readLine());
        logger.log(LogType.INFO, "Получена g: " + g);
        BigInteger p = new BigInteger(in.readLine());
        logger.log(LogType.INFO, "Получена p: " + p);
        BigInteger A = new BigInteger(in.readLine());
        logger.log(LogType.INFO, "Получена A: " + A);
        BigInteger B = diffieHellmanGenerator.initBob(g, p);
        out.write(B.toString() + "\n");
        out.flush();
        logger.log(LogType.INFO, "Отправлена B: " + B);
        return A;
    }

    private static void sendMessage() {
        BufferedWriter outStream = null;
        try (Socket clientSocket = new Socket("localhost", 4005)) {
            outStream = new BufferedWriter(new OutputStreamWriter(
                    clientSocket.getOutputStream()));
            String message = prepareMessage();

            outStream.write(message + "\n"); outStream.flush();

            RSAEncryptor rsaEncryptor = new RSAEncryptor(64);
            rsaEncryptor.generateKey();
            String signature = rsaEncryptor.generateSignature(message);
            BigInteger n = rsaEncryptor.n;
            BigInteger e = rsaEncryptor.e;

            outStream.write(signature + "\n"); outStream.flush();
            outStream.write(n.toString() + "\n"); outStream.flush();
            outStream.write(e.toString() + "\n"); outStream.flush();

            messageField.setText("");
            logger.log(LogType.INFO, "Установлено соединение с сервером!: " + clientSocket.getInetAddress()
                    + " порт: " + clientSocket.getPort());

        } catch (IOException e) {
            showErrorMessage(e.getLocalizedMessage());
            logger.log(LogType.ERROR, e.getLocalizedMessage());
        } finally {
            try {
                if (outStream != null) outStream.close();
            } catch (IOException e) {
                showErrorMessage(e.getLocalizedMessage());
                logger.log(LogType.ERROR, e.getLocalizedMessage());
            }
        }
    }

    private static String prepareMessage() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String message = formatter.format(date) + " " + user + ": " + messageField.getText();
        messageDisplay.setText(messageDisplay.getText() + message + "\n");

        RC4Encryptor rc4Encryptor = new RC4Encryptor(K.toString().toCharArray());
        message = new String(rc4Encryptor.encode(message.toCharArray(),
                message.toCharArray().length));

        message = MessageScreen.screen(message);
        return message;
    }

    public static void createComponents(JFrame frame) {

        Component[] components = new Component[] {delayLabel, delaySlider,loginLabel, loginField, passwordLabel,
                passwordField, signInButton, loggerButton, chatButton};

        buttonActionListener = new ButtonListener();
        signInButton.addActionListener(buttonActionListener);
        signInButton.setActionCommand("login");
        loggerButton.addActionListener(buttonActionListener);
        loggerButton.setActionCommand("logger");
        chatButton.addActionListener(buttonActionListener);
        chatButton.setActionCommand("chat");
        chatButton.setEnabled(false);
        int i = 0;
        for (Component c : components) {
            c.setBounds(50,25 * i + 10 * i,300,30);
            frame.add(c);
            i++;
        }

        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        for (int j = MIN_DELAY; j <= MAX_DELAY; j += MAX_DELAY / 4) {
            labelTable.put(j, new JLabel(String.valueOf(j)));
        }
        labelTable.put(MAX_DELAY, new JLabel(String.valueOf(MAX_DELAY)));
        delaySlider.setLabelTable(labelTable);
        delaySlider.setPaintLabels(true);
    }

    public static void createLogFrame() {
        JPanel middlePanel = new JPanel ();
        middlePanel.setBorder(new TitledBorder(new EtchedBorder(), "Лог"));

        display.setEditable(false);
        JScrollPane scroll = new JScrollPane ( display );
        scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

        middlePanel.add (scroll);

        loggerFrame.add(middlePanel);
        loggerFrame.setLocationRelativeTo(null);
        loggerFrame.setResizable(false);
        loggerFrame.setVisible(false);
        logger = new LoggerService(display);
        logger.log(LogType.INFO, "Клиент запущен!");
        middlePanel.add(clearLoggerButton);
        clearLoggerButton.addActionListener(actionEvent -> {
            display.setText("");
        });
        loggerFrame.pack();
    }

    public static void createMessengerFrame() {
        JPanel middlePanel = new JPanel();

        messageDisplay.setEditable(false);
        JScrollPane scroll = new JScrollPane(messageDisplay);
        scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        middlePanel.add(scroll);

        messagesFrame.setLocationRelativeTo(null);
        messagesFrame.setResizable(true);
        sendButton.addActionListener(buttonActionListener);
        sendButton.setActionCommand("send");
        messagesFrame.add(sendButton);
        messagesFrame.add(messageField);
        messagesFrame.setSize(800, 300);
        messagesFrame.add(middlePanel);
        sendButton.setBounds(650, 260, 135, 30);
        messageField.setBounds(10, 260, 635, 30);
    }

    public static void createGUI() {
        JFrame frame = new JFrame("Клиент");
        createComponents(frame);

        frame.setSize(400,360);
        frame.setResizable(false);
        frame.setLayout(null);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        createLogFrame();
        createMessengerFrame();
    }

    public static void main(String[] args) {
        createGUI();
    }
}

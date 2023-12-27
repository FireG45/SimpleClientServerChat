package com.example.ServerSideIS;

import com.example.ServerSideIS.crypt.RSAEncryptor;
import com.example.ServerSideIS.models.User;
import com.example.ServerSideIS.services.logger.LogType;
import com.example.ServerSideIS.services.logger.LoggerService;
import com.example.ServerSideIS.services.UsersService;
import com.example.ServerSideIS.util.ChallengeResponseGenerator;
import com.example.ServerSideIS.crypt.DiffieHellmanGenerator;
import com.example.ServerSideIS.util.MessageScreen;
import com.example.ServerSideIS.util.hash.SHA1Hash;
import com.example.ServerSideIS.util.tuple.Tuple3;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;

@SpringBootApplication
public class ServerSideIsApplication {

	static final int MAX_LIFETIME = 10000;
	static final int MIN_LIFETIME = 1000;

	static JLabel serverStatus = new JLabel("Сервер: онлайн");
	static JButton serverSwitchButton = new JButton("Выключить сервер");
	static JLabel lifetimeLabel = new JLabel("Срок действия слова-вызова (мс):");
	static JSlider lifetimeSlider = new JSlider(JSlider.HORIZONTAL, MIN_LIFETIME, MAX_LIFETIME, MIN_LIFETIME);
	static JLabel loginLabel = new JLabel("Логин");
	static JTextField loginField = new JTextField();
	static JLabel passwordLabel = new JLabel("Пароль");
	static JPasswordField passwordField = new JPasswordField();
	static JButton signInButton = new JButton("Зарегистрироваться");
	static JButton loggerButton = new JButton("Показать логи");
	static JFrame loggerFrame = new JFrame("Лог сервера");
	static JButton clearLoggerButton = new JButton("Очистить логи");
	static JLabel paramSizeLabel = new JLabel("Размер p:");
	static JTextField paramSizeField = new JTextField("32");
	static JFrame messagesFrame = new JFrame("Чат");
	static JButton sendButton = new JButton("Отправить");
	static JTextField messageField = new JTextField();
	static JTextArea messageDisplay = new JTextArea ( 16, 58 );
	static JButton chatButton = new JButton("Показать чат");

	static int keySize = 48;
	static BigInteger K;

	static UsersService usersService;
	static LoggerService logger;

	static boolean serverRunning = true;

	static Socket clientSocket = null;
	static ServerSocket server;
	static BufferedReader in = null;
	static BufferedWriter out = null;

	@Autowired
	public ServerSideIsApplication(UsersService usersService) {
		ServerSideIsApplication.usersService = usersService;
	}

	public static void showErrorMessage(String message) {
		JOptionPane.showMessageDialog(JFrame.getFrames()[0], message,
				"Ошибка", JOptionPane.ERROR_MESSAGE);
	}

	public static void createComponents(JFrame frame) {

		Component[] components = new Component[] { lifetimeLabel, lifetimeSlider, loginLabel, loginField,
				passwordLabel, passwordField, signInButton, loggerButton, paramSizeLabel, paramSizeField, chatButton };

		signInButton.addActionListener(actionEvent -> {
			if (loginField.getText().isEmpty() || passwordField.getPassword().length == 0) {
				showErrorMessage("Введите логин и пароль!");
				logger.log(LogType.WARN, "Попытка регистрации с пустыми полями ввода!");
				return;
			}
			String username = loginField.getText();
			String password = Arrays.toString(passwordField.getPassword());
            if (!usersService.registerUser(username, password)) {
				showErrorMessage("Пользователь с таким логином уже зарегистрирован!");
				logger.log(LogType.WARN, "Попытка регистрации с занятым логином: " + username);
			} else {
				logger.log(LogType.INFO, "Пользователь с логином " + username + " успешно зарегистрирован");
			}
        });

		serverSwitchButton.addActionListener(actionEvent -> {
			if (serverRunning) {
				try {
					if (clientSocket != null) clientSocket.close();
					if (in != null) in.close();
					if (out != null) out.close();
					serverRunning = false;
					serverStatus.setText("Сервер: оффлайн");
					server.close();
				} catch (IOException ex) {
					showErrorMessage(ex.getLocalizedMessage());
					serverRunning = false;
					serverStatus.setText("Сервер: оффлайн");
				}
			} else {
				serverRunning = true;
				serverStatus.setText("Сервер: онлайн");
				handleClientMessages();
			}
		});

		loggerButton.addActionListener(actionEvent -> {
			if (loggerFrame.isVisible()) {
                loggerFrame.setVisible(false);
                loggerButton.setText("Показать логи");
            } else {
                loggerFrame.setVisible(true);
                loggerButton.setText("Скрыть логи");
            }
        });

		chatButton.addActionListener(actionEvent -> {
			if (messagesFrame.isVisible()) {
				messagesFrame.setVisible(false);
				chatButton.setText("Показать чат");
			} else {
				messagesFrame.setVisible(true);
				chatButton.setText("Скрыть чат");
			}
		});

		int i = 0;
		for (Component c : components) {
			c.setBounds(50,25 * i + 10 * i,300, 30);
			frame.add(c);
			i++;
		}

		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		for (int j = MIN_LIFETIME; j <= MAX_LIFETIME; j += MAX_LIFETIME / 4) {
			labelTable.put(j, new JLabel(String.valueOf(j)));
		}
		labelTable.put(MAX_LIFETIME, new JLabel(String.valueOf(MAX_LIFETIME)));
		lifetimeSlider.setLabelTable(labelTable);
		lifetimeSlider.setPaintLabels(true);

	}

	public static void createGUI() {
		JFrame frame = new JFrame("\"Сервер\"");
		createComponents(frame);

		frame.setSize(400,400);
		frame.setResizable(false);
		frame.setLayout(null);
		frame.setVisible(true);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private static void handleClientMessages() {
		try {
			server = new ServerSocket(4004);
			try {
				while (serverRunning) {
					serverMainLoop();
				}
			} finally {
				if (clientSocket != null) clientSocket.close();
				if (in != null) in.close();
				if (out != null) out.close();
				logger.log(LogType.INFO, "Сервер отключен");
			}
		} catch (IOException exception) {
			showErrorMessage(exception.getLocalizedMessage());
			logger.log(LogType.ERROR, exception.getLocalizedMessage());
		}
	}

	private static void serverMainLoop() throws IOException {
		clientSocket = server.accept();
		logger.log(LogType.INFO, "Установлено соединение с клиентом: " + clientSocket.getInetAddress()
		+ " порт: " + clientSocket.getPort());
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

		String username = in.readLine();

		User user = usersService.findUserByUsername(username);
		String word = "C" + ChallengeResponseGenerator.generate();
		if (user != null) {
			long timestamp = System.currentTimeMillis();
			String refSuperHash = sendChallengeResponse(word, user);

			long lifetime = lifetimeSlider.getValue();

			String superHash = in.readLine();
			logger.log(LogType.INFO, "Получен супер-хеш: " + superHash);

			if (checkResponse(timestamp, lifetime, superHash, refSuperHash)) {
				initChat();
			} else {
				out.write("Неверные данные или слово-вызов истекло!" + "\n");
				out.flush();
				logger.log(LogType.WARN, "Попытка авторизации с логином: " + username +
						" и неверным паролем или истекшим словом-вызовом");
			}
		} else {
			out.write("Неверный логин или такого пользователя не существует!" + "\n");
			out.flush();
			logger.log(LogType.WARN, "Попытка авторизации с несуществующим логином: " + username);
		}
	}

	private static void initChat() throws IOException {
		out.write(keySize + "\n");
		out.flush();
		logger.log(LogType.INFO, "Отправлен размер парамеров: " + keySize);
		DiffieHellmanGenerator diffieHellmanGenerator = new DiffieHellmanGenerator(keySize);
		Tuple3<BigInteger, BigInteger, BigInteger> tuple =
				diffieHellmanGenerator.initAlice();
		BigInteger g = tuple.first;
		BigInteger p = tuple.second;
		BigInteger A = tuple.third;

		sendDiffieHellmanParams(g, p, A);

		BigInteger B = new BigInteger(in.readLine());
		logger.log(LogType.INFO, "Получена B: " + B);
		MessageHandler messageHandler = getMessageHandler(diffieHellmanGenerator, B);
		messageHandler.start();
	}

	private static MessageHandler getMessageHandler(DiffieHellmanGenerator diffieHellmanGenerator, BigInteger B) {
		K = diffieHellmanGenerator.getKeyAlice(B);
		logger.log(LogType.INFO, "Сгенерированный распределенный ключ: " + K);
		MessageHandler messageHandler = new MessageHandler(messageDisplay, K.toString(), logger);
		chatButton.setEnabled(true);
		chatButton.doClick();
		return messageHandler;
	}

	private static boolean checkResponse(long timestamp, long lifetime, String superHash, String refSuperHash) {
		long arrived = System.currentTimeMillis();
		keySize = Integer.parseInt(paramSizeField.getText());
        return arrived - timestamp <= lifetime && superHash.equals(refSuperHash);
	}

	private static String sendChallengeResponse(String word, User user) throws IOException {
		out.write(word + "\n");
		out.flush();
		logger.log(LogType.INFO, "Отправлено слово-вызов: " + word);
		String refSuperHash = SHA1Hash.hashCode(SHA1Hash.hashCode(user.getPassword()) + word);
		logger.log(LogType.INFO, "Сгенерирован супер-хеш: " + refSuperHash);
		return refSuperHash;
	}

	private static void sendDiffieHellmanParams(BigInteger g, BigInteger p, BigInteger A) throws IOException {
		out.write(g.toString() + "\n");
		logger.log(LogType.INFO, "Отправлена g: " + g);
		out.write(p.toString() + "\n");
		logger.log(LogType.INFO, "Отправлена p: " + p);
		out.write(A.toString() + "\n");
		out.flush();
		logger.log(LogType.INFO, "Отправлена A: " + A);
	}

	public static void createMessengerFrame() {
		JPanel middlePanel = new JPanel();

		messageDisplay.setEditable(false);
		JScrollPane scroll = new JScrollPane(messageDisplay);
		scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		middlePanel.add(scroll);

		messagesFrame.setLocationRelativeTo(null);
		messagesFrame.setResizable(true);
		messagesFrame.setVisible(false);
		chatButton.setEnabled(false);
		sendButton.addActionListener(actionEvent -> {
			sendMessage();
		});

		sendButton.setActionCommand("send");
		messagesFrame.add(sendButton);
		messagesFrame.add(messageField);
		messagesFrame.setSize(800, 300);
		messagesFrame.add(middlePanel);
		sendButton.setBounds(650, 260, 135, 30);
		messageField.setBounds(10, 260, 635, 30);
	}

	private static void sendMessage() {
		BufferedWriter outStream = null;
		try (Socket clientSocket = new Socket("localhost", 4006)) {
			outStream = new BufferedWriter(new OutputStreamWriter(
					clientSocket.getOutputStream()));
			String message = prepareMessage(messageField.getText());

			logger.log(LogType.INFO, "Отправлено сообщение:" + message);
			outStream.write(message + "\n"); outStream.flush();

			RSAEncryptor rsaEncryptor = new RSAEncryptor(64);
			rsaEncryptor.generateKey();

			String signature = rsaEncryptor.generateSignature(message);
			BigInteger n = rsaEncryptor.n;
			BigInteger e = rsaEncryptor.e;

			sendSignatureParams(signature, outStream, n, e);

			messageField.setText("");
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

	private static String prepareMessage(String messageText) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		String message = formatter.format(date) + " SERVER" + ": " + messageText;

		org.example.crypt.RC4Encryptor rc4Encryptor = new org.example.crypt.
				RC4Encryptor(K.toString().toCharArray());
		messageDisplay.setText(messageDisplay.getText() + message + "\n");
		message = new String(rc4Encryptor.encode(message.toCharArray(),
				message.toCharArray().length));

		message = MessageScreen.screen(message);
		return message;
	}

	private static void sendSignatureParams(String signature, BufferedWriter outStream, BigInteger n, BigInteger e) throws IOException {
		logger.log(LogType.INFO, "Отправлена подпись:" + signature);
		outStream.write(signature + "\n");
		outStream.flush();
		logger.log(LogType.INFO, "Отправлена n:" + n.toString());
		outStream.write(n.toString() + "\n");
		outStream.flush();
		logger.log(LogType.INFO, "Отправлена e:" + e.toString());
		outStream.write(e.toString() + "\n");
		outStream.flush();
	}

	public static void main(String[] args) {
		SpringApplication.run(ServerSideIsApplication.class, args);
		createGUI();

		JPanel middlePanel = new JPanel ();
		middlePanel.setBorder(new TitledBorder(new EtchedBorder(), "Лог"));
		JTextArea display = new JTextArea(16, 58);
		display.setEditable(false);
		JScrollPane scroll = new JScrollPane(display);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		middlePanel.add(scroll);

		loggerFrame.add(middlePanel);
		loggerFrame.setLocationRelativeTo(null);
		loggerFrame.setVisible(false);
		loggerFrame.setResizable(false);
		logger = new LoggerService(display);
		logger.log(LogType.INFO, "Сервер запущен!");
		middlePanel.add(clearLoggerButton);
		clearLoggerButton.addActionListener(actionEvent -> display.setText(""));
		loggerFrame.pack();
		createMessengerFrame();
		handleClientMessages();
	}

}

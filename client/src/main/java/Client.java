
import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;
import org.apache.log4j.Logger;


/**
 * Обеспечивает работу программы в режиме клиента
 */
public class Client extends Thread {
    private boolean stoped;
    public BufferedReader in;
    public PrintWriter out;
    private Socket socket;
    private static FileInputStream fis;
    private final static Properties property = new Properties();
    private final static Logger logger = Logger.getLogger(Client.class);


    /**
     * Запрашивает у пользователя ник и организовывает обмен сообщениями с
     * сервером
     */
    public Client() throws IOException {
        Scanner scan = new Scanner(System.in);
        String ip = property.getProperty("host");
        fis = new FileInputStream("config.properties");
        property.load(fis);
        try {
            // Подключаемся в серверу и получаем потоки(in и out) для передачи сообщений
            socket = new Socket(ip, Integer.parseInt(property.getProperty("port")));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Enter your nickname:");
            out.println(scan.nextLine());
            start();
            // Пока пользователь не введёт "/exit" отправляем на сервер всё, что
            // введено из консоли
            String str = "";
            while (!str.equals("/exit")) {
                logger.info(str = scan.nextLine());
                out.println(str);
            }
            setStop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close();
        }
    }

    /**
     * Закрывает входной и выходной потоки и сокет
     */
    private void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            logger.error("Error: Streams were not closed!", e);
        }
    }

    /**
     * Прекращает пересылку сообщений
     */
    public void setStop() {
        stoped = true;
    }

    /**
     * Считывает все сообщения от сервера и печатает их в консоль.
     * Останавливается вызовом метода setStop()
     */
    @Override
    public void run() {
        try {
            while (!stoped) {
                String str = in.readLine();
                System.out.println(str);
            }
        } catch (IOException e) {
            logger.error("Error receiving message.", e);
        }
    }
}

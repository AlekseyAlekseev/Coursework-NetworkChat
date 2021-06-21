
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * Обеспечивает работу программы в режиме сервера
 */
public class Server {
    private final static Logger logger = Logger.getLogger(Server.class);

    /**
     * Специальная "обёртка" для ArrayList, которая обеспечивает доступ к
     * массиву из разных нитей
     */
    private final List<Connection> connections =
            Collections.synchronizedList(new ArrayList<>());
    private ServerSocket server;
    private static FileInputStream fis;
    private final static Properties property = new Properties();


    /**
     * Конструктор создаёт сервер. Затем для каждого подключения создаётся
     * объект Connection и добавляет его в список подключений.
     */
    public Server() throws IOException {
        System.out.println("Server running...");
        logger.info("Server running...");

        fis = new FileInputStream("config.properties");
        property.load(fis);
        try {
            server = new ServerSocket(Integer.parseInt(property.getProperty("port")));

            while (true) {
                Socket socket = server.accept();
                // Создаём объект Connection и добавляем его в список
                Connection connection = new Connection(socket);
                connections.add(connection);

                connection.start();

            }
        } catch (IOException e) {
            logger.error("Error: ", e);
        } finally {
            closeAll();
        }
    }

    /**
     * Закрывает все потоки всех соединений а также серверный сокет
     */
    private void closeAll() {
        try {
            server.close();

            // Перебор всех Connection и вызов метода close() для каждого. Блок
            // synchronized {} необходим для правильного доступа к одним данным
            // их разных нитей
            synchronized (connections) {
                for (Connection connection : connections) {
                    connection.close();
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            //logger.log(Level.WARNING, "Exception: Streams were not closed!\n", e);
        }
    }

    /**
     * Класс содержит данные, относящиеся к конкретному подключению:
     * <ul>
     * <li>имя пользователя</li>
     * <li>сокет</li>
     * <li>входной поток BufferedReader</li>
     * <li>выходной поток PrintWriter</li>
     * </ul>
     * Расширяет Thread и в методе run() получает информацию от пользователя и
     * пересылает её другим
     */
    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        private String name = "";

        /**
         * Инициализирует поля объекта и получает имя пользователя
         *
         * @param socket сокет, полученный из server.accept()
         */
        public Connection(Socket socket) {
            this.socket = socket;

            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                logger.error("Error: ", e);
                close();
            }
        }

        /**
         * Запрашивает имя пользователя и ожидает от него сообщений. При
         * получении каждого сообщения, оно вместе с именем пользователя
         * пересылается всем остальным.
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try {
                name = in.readLine();
                // Отправляем всем клиентам сообщение о том, что зашёл новый пользователь
                synchronized (connections) {
                    for (Connection connection : connections) {
                        connection.out.println(name + " cames now");
                        logger.info(name + " cames now");
                    }
                }

                String str;
                while (true) {
                    logger.info(str = in.readLine());
                    if (str.equals("/exit")) break;

                    // Отправляем всем клиентам очередное сообщение
                    synchronized (connections) {
                        for (Connection connection : connections) {
                            connection.out.println(name + ": " + str);
                            logger.info(name + ": " + str);
                        }
                    }
                }

                synchronized (connections) {
                    for (Connection connection : connections) {
                        connection.out.println(name + " has left");
                        logger.info(name + " has left");
                    }
                }
            } catch (IOException e) {
                logger.error("Error: ", e);
            } finally {
                close();
            }
        }

        /**
         * Закрывает входной и выходной потоки и сокет
         */
        public void close() {
            try {
                in.close();
                out.close();
                socket.close();

                // Если больше не осталось соединений, закрываем всё, что есть и
                // завершаем работу сервера
                connections.remove(this);
                if (connections.size() == 0) {
                    Server.this.closeAll();
                    System.exit(0);
                }
            } catch (Exception e) {
                logger.error("Error: Streams were not closed!", e);
            }
        }
    }
}
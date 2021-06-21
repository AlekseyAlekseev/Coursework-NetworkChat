package ru.netology.client;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    /**
     * Спрашивает пользователя о режиме работы (сервер или клиент) и передаёт
     * управление соответствующему классу
     */
    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        System.out.println("Вы хотите запустить программу OnlineChat? (Y(yes) / N(no))");
        while (true) {
            char answer = Character.toLowerCase(in.nextLine().charAt(0));
            if (answer == 'y') {
                new Client();
                break;
            } else if (answer == 'n') {
                System.out.println("Вы отказались от запуска OnlineChat");
                break;
            } else {
                System.out.println("Некорректный ввод. Повторите.");
            }
        }
    }
}

package main;

import UI.Frame;

public class main {
    public static void main(String[] args){
        Frame jFrame = new Frame();
        jFrame.createFrame();
    }
}

/*
Удалось решить проблему нумерации строк!!! +++
Теперь необходимо
1. Найти способ увеличить размер открываемой таблицы (нашёл, однако имеет смысл найти способ,
который позволить изменять размер не вручную)
2. Найти метод, благодаря которому возможно сохранять вносимые в файл изменения
3. Реализовать метод: "Функция поиска должна позволять найти некоторую последовательность байт,
заданных точным значением либо значением по некоторой маске".
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package batiksvgviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Path2D;
import java.io.FileWriter;
import java.io.IOException;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.swing.JSVGCanvas;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

/**
 *
 *
 * Создаем CVG файл из графических компонентов 2D
 */
public class SVGGraphics2DDemo {

    private int iterCocha = 5; // сколько будем раз ломать линию. // итерации максимум рисует 10
    private static String nameFile = "booktitle.svg";
    private double x1 = 100;// точки отрезка
    private double y1 = 200; // если это больше то определяет все верно
    
    private double x2 = 1800; // конечная точка линии
    private double y2 = 200;
    
    public static void main(String args[]) {
        SVGGraphics2DDemo sv2Demo = new SVGGraphics2DDemo(); // собственный экземпляр для доступа
        sv2Demo.paint();
        sv2Demo.showFileSVG(); // это создание формы а надо обновление
    }

    public void paint() {
        DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
        Document doc = domImpl.createDocument(null, "svg", null);
        SVGGraphics2D gr2d = new SVGGraphics2D(doc);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // метод определения размеров экрана
        int widthM = screenSize.width;
        int heightM = screenSize.height;

        gr2d.drawString("Size monitor" + widthM + " x " + heightM, 12, 12);

        // Кривая Коха (основа линия)
        // начальная точка линии
        gr2d.setPaint(Color.MAGENTA);

        double[][] xyT = new double[2][2]; // коннечный массив координат который построится
        xyT[0][0] = x1;
        xyT[0][1] = y1;
        xyT[1][0] = x2;
        xyT[1][1] = y2;

        double[][] massPointSecondIter = new double[0][2];
        for (int i = 0; i < iterCocha; i++) {
            int sumPoint = (xyT.length - 1) * 3 + xyT.length; // такая длинна массива будет при следующей итерации
            massPointSecondIter = new double[sumPoint][2];
            int posE = 0;
            double x1_l = 0;
            double y1_l = 0;
            double x2_l = 0;
            double y2_l = 0;
            for (int j = 0; j < xyT.length; j++) {// тут косяк в неверной отрисовке
                if (j + 1 >= xyT.length) { // проверка на выход за диапазон
                    break;
                }
                x1_l = xyT[j][0];
                y1_l = xyT[j][1];
                x2_l = xyT[j + 1][0];
                y2_l = xyT[j + 1][1];

                // дробин отрезок на три равные части (находим векторами)                    
                // находим точки отрезка по отношению длин его частей
                double xC1 = (x1_l + (1.0 / 2.0) * x2_l) / (1.0 + 1.0 / 2.0); // это отношение 1 к 3 отрезков
                double yC1 = (y1_l + (1.0 / 2.0) * y2_l) / (1.0 + 1.0 / 2.0);
                // координаты точки 2/3 отрезка
                double xC2 = (x1_l + (2.0) * x2_l) / (1.0 + 2.0); // это 2 к 1
                double yC2 = (y1_l + (2.0) * y2_l) / (1.0 + 2.0);

                // вычисляем точки и заносим в конечный массив
                double[] toFunction = {xC1, yC1, xC2, yC2}; // точки на обработку
                double[] pointTriangle = getTopTriangle(toFunction); // получаем координаты от отрезка
                massPointSecondIter[posE][0] = x1_l;
                massPointSecondIter[posE][1] = y1_l;
                massPointSecondIter[++posE][0] = xC1;
                massPointSecondIter[posE][1] = yC1;
                massPointSecondIter[++posE][0] = pointTriangle[0];// полученная точка
                massPointSecondIter[posE][1] = pointTriangle[1];
                massPointSecondIter[++posE][0] = xC2;
                massPointSecondIter[posE][1] = yC2;
                massPointSecondIter[++posE][0] = x2_l;
                massPointSecondIter[posE][1] = y2_l;
            }
            xyT = massPointSecondIter; // присваиваем этот массив и заново проходим по итерациям
        }
        // строим сложную кривую
        Path2D polylineCocha = new Path2D.Double();
        polylineCocha.moveTo(massPointSecondIter[0][0], massPointSecondIter[0][1]);
        for (int i = 1; i < massPointSecondIter.length; i++) {
            double xT = massPointSecondIter[i][0];
            double yT = massPointSecondIter[i][1];
            polylineCocha.lineTo(xT, yT);
        }
        gr2d.draw(polylineCocha);
        massPointSecondIter = null;

        // --- вывод нарисованного в файл ---
        try {
            gr2d.stream(new FileWriter(nameFile), false);
        } catch (SVGGraphics2DIOException ex) {
            Logger.getLogger(SVGGraphics2DDemo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SVGGraphics2DDemo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // Показать форму с вновь сформированным фалом
    public void showFileSVG() {
        JFrame f = new JFrame("Batik");
        f.getContentPane().add(createComponentJpanel());
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        f.setExtendedState(javax.swing.JFrame.MAXIMIZED_BOTH); // В весь экран
        f.setVisible(true);
    }

    // формирование панели  Jpanel
    public JComponent createComponentJpanel() {
        final JPanel panel = new JPanel(new BorderLayout());
        JSVGCanvas svgCanvas = new JSVGCanvas();
        svgCanvas.setURI(nameFile);
        svgCanvas.setFocusable(true);
        panel.add("Center", svgCanvas);

        svgCanvas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println(e.getKeyCode());
                int ierSum = 10;
                switch (e.getKeyCode()) {
                    case (65): {  // лево A
                        x1 -= ierSum;
                        break;
                    }
                    case (68): {  // Право B
                        x1 += ierSum;
                        break;
                    }
                    case (87): { // W вверх
                        y1 -= ierSum;
                        break;
                    }
                    case (83): { // S вниз
                        y1 += ierSum;

                        break;
                    }
                    case (37): {  // лево
                        x2 -= ierSum;
                        break;
                    }
                    case (39): {  // Право 
                        x2 += ierSum;

                        break;
                    }
                    case (38): { // вверх
                        y2 -= ierSum;
                        break;
                    }
                    case (40): { // вниз
                        y2 += ierSum;
                        break;
                    }
                }
                paint(); // вызов всей перерисовки файла
                panel.removeAll();
                svgCanvas.removeAll();
                svgCanvas.setURI(nameFile);
                panel.add("Center", svgCanvas);
                panel.revalidate(); // переопределить элементы
                panel.repaint(); // перерисовать панель
                svgCanvas.setFocusable(true); // установить фокус на элементе
                svgCanvas.requestFocus(); // обновить фокус
            }

            @Override
            public void keyReleased(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
        return panel;
    }

    // --- Фукция получения координаты центральной точке  вершины треульника по отрезку---
    public double[] getTopTriangle(double[] XYCut) {
        double[] pointTopTrianle = new double[2];
        double x1 = XYCut[0];
        double y1 = XYCut[1];
        double x2 = XYCut[2];
        double y2 = XYCut[3];

        // находим сам вектор
        double[] vector = {x2 - x1, y2 - y1};
        // длина вектора по формуле(это применим для рисования точки под углом)
        double lenStartVector = sqrt(pow(vector[0], 2) + pow(vector[1], 2));
        //ищем направляющие косинуса А вектора(изначального отрезка)
        double cosAvector = (vector[0] / lenStartVector); // тут верно
        double cosBvector = (vector[1] / lenStartVector); // тут тоже

        // ищем sin угла A это и будет cos угла A для вектора перпендикуляра
        double sinAvector = sqrt(1 - pow(cosAvector, 2)); // если cos 90 то логично что sin будет 0 и нужно доп условие если это так
        double gradusA = Math.toDegrees(Math.acos(sinAvector));

        // от направления вектора будем или отнимать или прибавлять
        double cosDopvector = 0;
        if (cosAvector > 0 & cosBvector > 0) {
            gradusA = gradusA + 180; // разворачиваем на 180 градусов
            sinAvector = Math.cos(Math.toRadians(gradusA)); // и обратно преобразовываем
            cosDopvector = Math.cos(Math.toRadians(90 + gradusA));
        }
        cosDopvector = Math.cos(Math.toRadians(90 + gradusA));

        if (cosAvector > 0 & cosBvector < 0) { // при таком условви половина круга верно отображается
            cosDopvector = Math.cos(Math.toRadians(90 - gradusA));
        }
        if (cosAvector < 0 & cosBvector > 0) { // при таком условви половина круга верно отображается
            gradusA = gradusA + 180; // разворачиваем на 180 градусов
            sinAvector = Math.cos(Math.toRadians(gradusA)); // и обратно преобразовываем
            cosDopvector = Math.cos(Math.toRadians(90 - gradusA));
        }

        if (cosAvector < 0 & cosBvector < 0) {
            cosDopvector = Math.cos(Math.toRadians(90 + gradusA));
        }

        if (cosAvector == 0 | cosBvector == 0) {
            gradusA = gradusA + 180; // разворачиваем на 180 градусов
            sinAvector = Math.cos(Math.toRadians(gradusA)); // и обратно преобразовываем
            cosDopvector = Math.cos(Math.toRadians(90 + gradusA));
        }

        // определяем высоту треугольника
        double h = sqrt(3) / 2 * lenStartVector;  // высота треугольника тоже верно
        double startMediumX = (x1 + x2) / 2;
        double startMediumY = (y1 + y2) / 2; // а это должно всегда из 0 быть
        pointTopTrianle[0] = h * sinAvector + startMediumX;
        pointTopTrianle[1] = h * cosDopvector + startMediumY; // для наглядности

        return pointTopTrianle;

    }

}

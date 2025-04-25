package org.misha;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.function.DoubleUnaryOperator;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

public class App {

    public static void main(String[] args) {
        try {
            // === 1. Читаем параметры из input.txt ===
            double a, b, alpha1, alpha2, alpha3, beta1, beta2, beta3;
            int N;
            try (Scanner sc = new Scanner(new File("input.txt"))) {
                a      = sc.nextDouble();
                b      = sc.nextDouble();
                N      = sc.nextInt();
                alpha1 = sc.nextDouble();
                alpha2 = sc.nextDouble();
                alpha3 = sc.nextDouble();
                beta1  = sc.nextDouble();
                beta2  = sc.nextDouble();
                beta3  = sc.nextDouble();
            }

            // === 2. Определяем p(x) и f(x) ===
            DoubleUnaryOperator p = x -> 1.0;          // замените по задаче
            DoubleUnaryOperator f = x -> Math.sin(x);  // замените по задаче

            double h = (b - a) / N;
            double[] x = new double[N+1];
            for (int i = 0; i <= N; i++) {
                x[i] = a + i * h;
            }

            // === 3. Составляем трёхдиагональную систему (вариант б) ===
            double[] A = new double[N+1], B = new double[N+1],
                    C = new double[N+1], D = new double[N+1];

            // левая граничная точка i=0
            double p0 = p.applyAsDouble(x[0]), f0 = f.applyAsDouble(x[0]);
            B[0] = -2 + 2*h*alpha2/alpha1 - h*h*p0;
            C[0] =  2;
            D[0] =  2*h*alpha3/alpha1 + h*h*f0;

            // внутренние узлы i=1..N-1
            for (int i = 1; i < N; i++) {
                double xi = x[i];
                A[i] = 1;
                B[i] = -2 - h*h * p.applyAsDouble(xi);
                C[i] = 1;
                D[i] = h*h * f.applyAsDouble(xi);
            }

            // правая граничная точка i=N
            double pN = p.applyAsDouble(x[N]), fN = f.applyAsDouble(x[N]);
            A[N] =  2;
            B[N] = -2 - 2*h*beta2/beta1 - h*h*pN;
            D[N] =  h*h*fN - 2*h*beta3/beta1;

            // === 4. Решаем методом прогонки ===
            double[] cStar = new double[N+1], dStar = new double[N+1];
            cStar[0] = C[0] / B[0];
            dStar[0] = D[0] / B[0];
            for (int i = 1; i <= N; i++) {
                double denom = B[i] - A[i]*cStar[i-1];
                if (i < N) {
                    cStar[i] = C[i] / denom;
                }
                dStar[i] = (D[i] - A[i]*dStar[i-1]) / denom;
            }
            double[] y = new double[N+1];
            y[N] = dStar[N];
            for (int i = N-1; i >= 0; i--) {
                y[i] = dStar[i] - cStar[i]*y[i+1];
            }

            // === 5. Запись результатов в простое TXT solution.txt ===
            try (PrintWriter pw = new PrintWriter("solution.txt")) {
                for (int i = 0; i <= N; i++) {
                    pw.printf("%.6f %.6f%n", x[i], y[i]);
                }
            }
            System.out.println("Результаты сохранены в solution.txt");

            // === 6. Рисуем график через XChart ===
            XYChart chart = new XYChartBuilder()
                    .width(800).height(600)
                    .title("BVP решение")
                    .xAxisTitle("x").yAxisTitle("y")
                    .build();
            chart.addSeries("y_approx", x, y);

            new SwingWrapper<>(chart).displayChart();
            BitmapEncoder.saveBitmap(chart, "bvp_solution", BitmapEncoder.BitmapFormat.PNG);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}


/*
 * PURPOSE
 * This program multiplies two n by n matrices, A and B, producing a third
 * matrix, C. The matrices are multiplied using threads, each thread
 * producing a row-band of matrix C. To reduce, if not eliminate, read
 * contentions between the threads, matrix B will be partitioned. Once
 * each thread is finished using its partition, they exchange partitions with
 * each other in a ring fashion.
 */

import java.util.Scanner;

public class MatMul implements Runnable {
    public static final double MINRAND = 100;
    public static final double MAXRAND = 1;
    public static final int MAXTHRDS = 8;

    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
        int n = 0;
        int numthreads = 0;
        try {
            System.out.printf("Enter size of n: ");
            n = userInput.nextInt();
            System.out.printf("Enter number of threads: ");
            numthreads = userInput.nextInt();
            if(n < 2 || numthreads < 2)
                throw new Exception(); //exception thrown with wrong input
        } catch (Exception e) {
            System.out.printf("Input must be integer greater than 1\n");
            System.exit(1);
        }

        numthreads = numthreads < MAXTHRDS ? numthreads : MAXTHRDS; //caps # threads spawned
        n = n > numthreads ? n : numthreads; //ensures at least 1 row per thread

        int array_size = n * n;
        double matrixA[] = new double[array_size];
        double matrixB[] = new double[array_size];
        double matrixC[] = new double[array_size];
        int breakPoints[] = new int[numthreads + 1];
        Thread thread_handles[] = new Thread[numthreads];

        for(int i = 0; i < array_size; i++)
            //matrixA[i] = MINRAND + (MAXRAND - MINRAND) * Math.random();
            matrixA[i] = 1.0;
        for(int i = 0; i < array_size; i++)
            //matrixB[i] = MINRAND + (MAXRAND - MINRAND) * Math.random();
            matrixB[i] = 1.0;
        for(int i = 0; i < array_size; i++)
            matrixC[i] = 0.0;

        for(int i = 0; i < numthreads; i++)
            thread_handles[i] = new Thread();
        for(int i = 0; i < numthreads; i++)
            thread_handles[i].start();

        if(n < 7) {
            printMatrix(matrixA, n, 'A');
            printMatrix(matrixB, n, 'B');
            printMatrix(matrixC, n, 'C');
        }
    }

    public void run() {
    }

    public static void printMatrix(double matrix[], int n, char m) {
        int row, column, start, end;

        System.out.printf("matrix%c\n", m);
        for(row = 0; row < n; row++) {
            start = row * n;
            end = (row + 1) * n;
            for(column = start; column < end; column++)
                System.out.printf("%10.2f", matrix[column]);
            System.out.printf("\n");
        }
        System.out.printf("\n");
    }
}


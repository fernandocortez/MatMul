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

    private static int n = 0;
    private static int numthreads = 0;
    private static double[] matrixA;
    private static double[] matrixB;
    private static double[] matrixC;
    private static int[] breakPoints;

    public static void main(String[] args) {
        Scanner userInput = new Scanner(System.in);
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

        int matrix_size = n * n;
        matrixA = allocateMatrix(matrix_size, 1);
        matrixB = allocateMatrix(matrix_size, 1);
        matrixC = allocateMatrix(matrix_size, 0);
        breakPoints = calcBreakpoints(n, numthreads);
        Thread thread_handles[] = new Thread[numthreads];

        for(int i = 0; i < numthreads; i++)
            thread_handles[i] = new Thread();
        for(int i = 0; i < numthreads; i++)
            thread_handles[i].start();
        for(int i = 0; i < numthreads; i++)
            try {
                thread_handles[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        if(n < 7) {
            printMatrix(matrixA, n, 'A');
            printMatrix(matrixB, n, 'B');
            printMatrix(matrixC, n, 'C');
        }
    }

    public void run() {
        int my_rank = (int) Thread.currentThread().getId();
        int rowband_start = breakPoints[my_rank];
        int rowband_end = breakPoints[my_rank + 1];
        int startB, endB;

        for(int count = 0; count < numthreads; count++) {
            startB = breakPoints[(my_rank + count) % numthreads];
            endB = breakPoints[(my_rank + count) % numthreads + 1];

            for(int i = rowband_start; i < rowband_end; i++) {
                for(int j = startB; j < endB; j++) {
                    double temp = matrixA[i*n + j];
                    for(int k = 0; k < n; k++)
                        matrixC[i*n + k] += temp * matrixB[j*n + k];
                }
            }
        }
    }

    public static double[] allocateMatrix(int n, int random) {
        double[] matrix = new double[n];
        switch (random) {
            case 0:
                for(int i = 0; i < n; i++)
                    matrix[i] = 0.0;
                break;
            case 1:
                for(int i = 0; i < n; i++)
                    matrix[i] = MINRAND + (MAXRAND - MINRAND) * Math.random();
                break;
        }
        return matrix;
    }

    public static int[] calcBreakpoints(int n, int threadcount) {
        int rows = n / threadcount;
        int remainder = n % threadcount; //number of rows not evenly distributed
        int last_k = threadcount - remainder; //number of threads to get extra row

        int[] breakpoints = allocateArray(threadcount + 1);

        //row bands are attempted to be distributed evenly
        for(int i = 1; i <= threadcount; i++)
            breakpoints[i] += breakpoints[i-1] + rows;

        //the remaining rows are given one to each of the last_k threads
        for(int i = threadcount; i > last_k; i--)
            breakpoints[i] += remainder--;

        return breakpoints;
    }

    public static int[] allocateArray (int n) {
        int[] array = new int[n];
        for(int i = 0; i < n; i++)
            array[i] = 0;
        return array;
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


package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException {
        int threads = 4;
        int lines = 5;
        int cols = 5;
        int convolutionDimension = 3;
        boolean regenerateInput = false;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--regenerate")) {
                regenerateInput = true;
            }

            if (args[i].equals("--lines")) {
                if (i + 1 < args.length) {
                    int arg = Integer.parseInt(args[i + 1]);
                    if (arg > 0) {
                        lines = arg;
                    }
                }
            }

            if (args[i].equals("--columns")) {
                if (i + 1 < args.length) {
                    int arg = Integer.parseInt(args[i + 1]);
                    if (arg > 0) {
                        cols = arg;
                    }
                }
            }

            if (args[i].equals("--convolution")) {
                if (i + 1 < args.length) {
                    int arg = Integer.parseInt(args[i + 1]);
                    if (arg > 0) {
                        convolutionDimension = arg;
                    }
                }
            }

            if (args[i].equals("--threads")) {
                if (i + 1 < args.length) {
                    int arg = Integer.parseInt(args[i + 1]);
                    if (arg > 0) {
                        threads = arg;
                    }
                }
            }

            if (args[i].equals("--help")) {
                System.out.println("--lines: number of lines that the matrix will have, DEFAULT 5");
                System.out.println("--columns: number of columns that the matrix will have, DEFAULT 5");
                System.out.println("--convolution: the dimension of the convolution matrix, DEFAULT 3");
                System.out.println("--threads: the number of threads used, DEFAULT 4");
                System.out.println("--regenerate: value true will regenerate the matrix and the convolution matrix, DEFAULT false");
                System.exit(0);
            }
        }

        if (regenerateInput) {
            generateRandomMatrixToFile(lines, cols, convolutionDimension);
        }

        String filename = "/Users/eduarddavid/workspace/PPD/tema-lab-1-java/src/main/java/org/example/input.txt";
        File file = new File(filename);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;

        ArrayList<ArrayList<Integer>> fileMatrix = new ArrayList<>();
        ArrayList<ArrayList<Integer>> fileConvolutionMatrix = new ArrayList<>();
        boolean firstFinished = false;

        while ((line = bufferedReader.readLine()) != null) {
            if (line.isEmpty()) {
                firstFinished = true;
            }

            try {
                String[] parts = line.split(" ");
                ArrayList<Integer> row = new ArrayList<>();
                for (String part : parts) {
                    row.add(Integer.parseInt(part));
                }

                if (!firstFinished) {
                    fileMatrix.add(row);
                } else {
                    fileConvolutionMatrix.add(row);
                }
            } catch (Exception ignored) {

            }
        }

        int[][] matrix = convertArrayListTo2DMatrix(fileMatrix, lines, cols);
        int[][] convolutionMatrix = convertArrayListTo2DMatrix(fileConvolutionMatrix, convolutionDimension, convolutionDimension);

        long time1 = System.nanoTime();
        int[][] sequentialMatrix = convertMatrixSequential(matrix, convolutionMatrix, convolutionDimension, lines, cols);
        long time2 = System.nanoTime();
        System.out.println("secvential: " + (time2 - time1) + " ns");
        writeResultToFile(sequentialMatrix, lines, cols);

        long time3 = System.nanoTime();
        int[][] parallelMatrixByLine = convertMatrixParallelByLines(matrix, convolutionMatrix, convolutionDimension, lines, cols, threads);
        long time4 = System.nanoTime();
        System.out.println("parallel by line: " + (time4 - time3) + " ns");
        compareResultWithFile(parallelMatrixByLine, lines, cols);

        long time5 = System.nanoTime();
        int[][] parallelMatrixByColumn = convertMatrixParallelByColumn(matrix, convolutionMatrix, convolutionDimension, lines, cols, threads);
        long time6 = System.nanoTime();
        System.out.println("parallel by column: " + (time6 - time5) + " ns");
        compareResultWithFile(parallelMatrixByColumn, lines, cols);
    }

    private static int[][] convertMatrixSequential(int[][] matrix, int[][] convolutionMatrix, int convolutionDimension, int lines, int cols) {
        int[][] resultMatrix = new int[lines][cols];

        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < cols; j++) {
                resultMatrix[i][j] = convertElement(matrix, convolutionMatrix, convolutionDimension, i, j);
            }
        }

        return resultMatrix;
    }

    private static int[][] convertMatrixParallelByLines(int[][] matrix, int[][] convolutionMatrix, int convolutionDimension, int lines, int cols, int threads) {
        int[][] resultMatrix = new int[lines][cols];
        int rest = lines % threads;
        int threadLines = lines / threads;
        LineThread[] lineThreads = new LineThread[threads];

        int startSegment = 0;
        int endSegment;

        for (int i = 0; i < threads; i++) {
            endSegment = startSegment + threadLines;
            if (rest != 0) {
                endSegment++;
                rest--;
            }

            lineThreads[i] = new LineThread(matrix, convolutionMatrix, resultMatrix, convolutionDimension, startSegment, endSegment);
            lineThreads[i].start();
            startSegment = endSegment;
        }

        for (int i = 0; i < threads; i++) {
            try {
                lineThreads[i].join();
            } catch (Exception ex) {

            }
        }

        return resultMatrix;
    }

    private static int[][] convertMatrixParallelByColumn(int[][] matrix, int[][] convolutionMatrix, int convolutionDimension, int lines, int cols, int threads) {
        int[][] resultMatrix = new int[lines][cols];
        int rest = cols % threads;
        int threadCols = cols / threads;
        ColumnThread[] columnThreads = new ColumnThread[threads];

        int startSegment = 0;
        int endSegment;

        for (int i = 0; i < threads; i++) {
            endSegment = startSegment + threadCols;
            if (rest != 0) {
                endSegment++;
                rest--;
            }

            columnThreads[i] = new ColumnThread(matrix, convolutionMatrix, resultMatrix, convolutionDimension, startSegment, endSegment);
            columnThreads[i].start();
            startSegment = endSegment;
        }

        for (int i = 0; i < threads; i++) {
            try {
                columnThreads[i].join();
            } catch (Exception ex) {

            }
        }

        return resultMatrix;
    }

    private static int convertElement(int[][] matrix, int[][] convolutionMatrix, int convolutionDimension, int elementXIndex, int elementYIndex) {
        //we place the element that we want to change in the middle of a matrix;
        int startXIndex = elementXIndex - convolutionDimension / 2;
        int startYIndex = elementYIndex - convolutionDimension / 2;
        int stopXIndex = startXIndex + convolutionDimension;
        int stopYIndex = startYIndex + convolutionDimension;

        //get the matrix bounds
        int xMax = matrix.length;
        int yMax = matrix[0].length;

        //initialise convolution matrix indexes
        int convolutionXIndex = 0;
        int convolutionYIndex = 0;

        //initialise the final element
        int convertedElement = 0;

        for (int x = startXIndex; x < stopXIndex; x++) {
            for (int y = startYIndex; y < stopYIndex; y++) {
                int currentElement = 0;
                if (x < 0 && y < 0) {
                    currentElement = matrix[0][0];
                }
                if (x < 0 && y >= 0 && y < yMax) {
                    currentElement = matrix[0][y];
                }
                if (x < 0 && y >= yMax) {
                    currentElement = matrix[0][yMax - 1];
                }
                if (x >= 0 && x < xMax && y >= yMax) {
                    currentElement = matrix[x][yMax - 1];
                }
                if (x >= 0 && x < xMax && y < 0) {
                    currentElement = matrix[x][0];
                }
                if (x >= xMax && y < 0) {
                    currentElement = matrix[xMax - 1][0];
                }
                if (x >= xMax && y >= 0 && y < yMax) {
                    currentElement = matrix[xMax - 1][y];
                }
                if (x >= xMax && y >= yMax) {
                    currentElement = matrix[xMax - 1][yMax - 1];
                }
                if (x >= 0 && x < xMax && y >= 0 && y < yMax) {
                    currentElement = matrix[x][y];
                }

                //we update the converted element and update the y index
                convertedElement += currentElement * convolutionMatrix[convolutionXIndex][convolutionYIndex];
                convolutionYIndex += 1;
            }

            //we increase the convolution x index and reset the y index
            convolutionXIndex += 1;
            convolutionYIndex = 0;
        }

        return convertedElement;
    }

    private static int[][] convertArrayListTo2DMatrix(ArrayList<ArrayList<Integer>> matrix, int lines, int cols) {
        int[][] convertedMatrix = new int[lines][cols];
        for (int i = 0; i < lines; i++) {
            ArrayList<Integer> row = matrix.get(i);
            for (int j = 0; j < cols; j++) {
                convertedMatrix[i][j] = row.get(j);
            }
        }

        return convertedMatrix;
    }

    private static void generateRandomMatrixToFile(int lines, int cols, int convolutionDimension) {
        try {
            FileWriter writer = new FileWriter("/Users/eduarddavid/workspace/PPD/tema-lab-1-java/src/main/java/org/example/input.txt");
            Random random = new Random();
            int minValue = 0;
            int maxValue = 100;

            for (int i = 0; i < lines; i++) {
                for (int j = 0; j < cols; j++) {
                    int value = random.nextInt(maxValue - minValue + 1) + minValue;
                    writer.write(Integer.toString(value));

                    if (j < cols - 1) {
                        writer.write(" ");
                    }
                }
                writer.write(System.lineSeparator());
            }

            writer.write(System.lineSeparator());

            if (convolutionDimension > 0) {
                int randomRow = random.nextInt(convolutionDimension);
                int randomCol = random.nextInt(convolutionDimension);

                for (int i = 0; i < convolutionDimension; i++) {
                    for (int j = 0; j < convolutionDimension; j++) {
                        int value = (i == randomRow && j == randomCol) ? 1 : 0;
                        writer.write(Integer.toString(value));

                        if (j < convolutionDimension - 1) {
                            writer.write(" ");
                        }
                    }
                    writer.write(System.lineSeparator());
                }
            }

            writer.close();
            System.out.println("Random matrix has been generated and saved to 'input.txt'.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeResultToFile(int[][] resultMatrix, int lines, int cols) throws IOException {
        FileWriter writer = new FileWriter("/Users/eduarddavid/workspace/PPD/tema-lab-1-java/src/main/java/org/example/output.txt");
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < cols; j++) {
                writer.write(Integer.toString(resultMatrix[i][j]));
                writer.write(" ");
            }
            writer.write('\n');
        }
        writer.close();
    }

    private static void compareResultWithFile(int[][] matrix, int lines, int cols) throws IOException {
        String filename = "/Users/eduarddavid/workspace/PPD/tema-lab-1-java/src/main/java/org/example/output.txt";
        File file = new File(filename);
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String line;

        ArrayList<ArrayList<Integer>> fileMatrix = new ArrayList<>();
        while ((line = bufferedReader.readLine()) != null) {
            try {
                String[] parts = line.split(" ");
                ArrayList<Integer> row = new ArrayList<>();
                for (String part : parts) {
                    row.add(Integer.parseInt(part));
                }

                fileMatrix.add(row);

            } catch (Exception ignored) {

            }
        }

        int[][] resultMatrix = convertArrayListTo2DMatrix(fileMatrix, lines, cols);
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < cols; j++) {
                if (resultMatrix[i][j] != matrix[i][j]) {
                    System.out.println("Results are not the same!");
                    showResultDifferences(resultMatrix, matrix, lines, cols);
                    return;
                }
            }
        }
        System.out.println("Results match!");
    }

    private static void showResultDifferences(int[][] fileMatrix, int[][] resultMatrix, int lines, int cols) {
        System.out.println("Expected vs. Current result");
        for (int i = 0; i < lines; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(fileMatrix[i][j] + ", ");
            }
            System.out.print("         ");
            for (int j = 0; j < cols; j++) {
                System.out.print(resultMatrix[i][j] + ", ");
            }
            System.out.println();
        }
    }

    private static void showSquareMatrix(int[][] matrix, int dimension) {
        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                System.out.print(matrix[i][j] + ", ");
            }
            System.out.println();
        }
    }

    private static class LineThread extends Thread {
        private int[][] matrix;
        private int[][] convolutionMatrix;
        private int[][] resultMatrix;
        private int convolutionDimension;
        private int startLine;
        private int endLine;


        public LineThread(int[][] matrix, int[][] convolutionMatrix, int[][] resultMatrix, int convolutionDimension, int startLine, int endLine) {
            this.matrix = matrix;
            this.convolutionMatrix = convolutionMatrix;
            this.resultMatrix = resultMatrix;
            this.convolutionDimension = convolutionDimension;
            this.startLine = startLine;
            this.endLine = endLine;
        }

        @Override
        public void run() {
            int cols = matrix[0].length;
            for (int i = startLine; i < endLine; i++) {
                for (int j = 0; j < cols; j++) {
                    resultMatrix[i][j] = convertElement(matrix, convolutionMatrix, convolutionDimension, i, j);
                }
            }
        }
    }

    private static class ColumnThread extends Thread {
        private int[][] matrix;
        private int[][] convolutionMatrix;
        private int[][] resultMatrix;
        private int convolutionDimension;
        private int startColumn;
        private int endColumn;

        public ColumnThread(int[][] matrix, int[][] convolutionMatrix, int[][] resultMatrix, int convolutionDimension, int startColumn, int endColumn) {
            this.matrix = matrix;
            this.convolutionMatrix = convolutionMatrix;
            this.resultMatrix = resultMatrix;
            this.convolutionDimension = convolutionDimension;
            this.startColumn = startColumn;
            this.endColumn = endColumn;
        }

        @Override
        public void run() {
            int lines = matrix.length;
            for (int j = startColumn; j < endColumn; j++) {
                for (int i = 0; i < lines; i++) {
                    resultMatrix[i][j] = convertElement(matrix, convolutionMatrix, convolutionDimension, i, j);
                }
            }
        }
    }
}
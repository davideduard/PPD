#include <iostream>
#include <thread>
#include <fstream>

using namespace std;

#define MAX_LINES 10000
#define MAX_COLS 10000
#define MAX_CONVOLUTION_DIMENSION 5
#define DYNAMIC_ALLOCATION 0

int noThreads = 4;
int lines = 5;
int cols = 5;
int convolutionDimension = 3;
bool regenerate = false;
int fileMatrix[MAX_LINES][MAX_COLS];

#if !DYNAMIC_ALLOCATION
int matrix[MAX_LINES][MAX_COLS];
int convolutionMatrix[MAX_CONVOLUTION_DIMENSION][MAX_CONVOLUTION_DIMENSION];
int resultMatrix[MAX_LINES][MAX_COLS];
#endif

#if DYNAMIC_ALLOCATION
int** matrix;
int** convolutionMatrix;
int** resultMatrix;

void allocateDynamicMemory() {
    matrix = new int* [lines];
    convolutionMatrix = new int* [convolutionDimension];
    resultMatrix = new int* [lines];

    for (int i = 0; i < lines; ++i) {
        matrix[i] = new int[cols];
        resultMatrix[i] = new int[cols];
    }

    for (int i = 0; i < convolutionDimension; ++i) {
        convolutionMatrix[i] = new int[convolutionDimension];
    }
}
#endif

void generateRandomMatrix(int n, int m, int k, int p) {
    ofstream outputFile("/Users/eduarddavid/workspace/PPD/tema-lab-1-cpp/input.txt");
    if (!outputFile) {
        cout << "Error opening file." << endl;
        return;
    }

    outputFile << n << " " << m << "\n";


    srand((unsigned) time(NULL));

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < m; j++) {
            int value = rand() % 100;
            outputFile << value << " ";
        }
        outputFile << "\n";
    }

    outputFile << k << "\n";

    int randomRow = rand() % k;
    int randomColumn = rand() % k;

    for (int i = 0; i < k; i++) {
        for (int j = 0; j < k; j++) {
            if (i == randomRow && j == randomColumn) {
                outputFile << 1 << " ";
            } else {
                outputFile << 0 << " ";
            }
        }
        outputFile << "\n";
    }

    outputFile << p << "\n";

    outputFile.close();
}

void readFromFile() {
    ifstream inputFile("/Users/eduarddavid/workspace/PPD/tema-lab-1-cpp/input.txt");
    if (!inputFile) {
        cout << "No file existing" << endl;
        return;
    }

    inputFile >> lines >> cols;

    for (int i = 0; i < lines; i++) {
        for (int j = 0; j < cols; j++) {
            inputFile >> matrix[i][j];
        }
    }

    inputFile >> convolutionDimension;

    for (int i = 0; i < convolutionDimension; i++) {
        for (int j = 0; j < convolutionDimension; j++) {
            inputFile >> convolutionMatrix[i][j];
        }
    }

    inputFile >> noThreads;
    inputFile.close();
}

void writeResultToFile() {
    ofstream outputFile("/Users/eduarddavid/workspace/PPD/tema-lab-1-cpp/output.txt");
    for (int i = 0; i < lines; i++) {
        for (int j = 0; j < cols; j++) {
            outputFile << resultMatrix[i][j] << " ";
        }
        outputFile << '\n';
    }
}

void showResultDifferences() {
    cout << "Expected vs. Current result" << '\n';
    for (int i = 0; i < lines; i++) {
        for (int j = 0; j < cols; j++) {
            cout << fileMatrix[i][j] << ",";
        }
        cout << "         ";
        for (int j = 0; j < cols; j++) {
            cout << resultMatrix[i][j] << ",";
        }
        cout << '\n';
    }
}

void compareResultsWithFile() {
    ifstream inputFile("/Users/eduarddavid/workspace/PPD/tema-lab-1-cpp/output.txt");
    if (!inputFile) {
        cout << "No file existing" << '\n';
        return;
    }

    for (int i = 0; i < lines; i++) {
        for (int j = 0; j < cols; j++) {
            inputFile >> fileMatrix[i][j];
        }
    }

    for (int i = 0; i < lines; i++) {
        for (int j = 0; j < cols; j++) {
            if (fileMatrix[i][j] != resultMatrix[i][j]) {
                cout << "Results are not the same!" << "\n\n";
                showResultDifferences();
                return;
            }
        }
    }

    cout << "Results match!" << '\n';
    inputFile.close();
}

int convertElement(int elementXIndex, int elementYIndex) {
    //we place the element that we want to change in the middle of a matrix;
    int startXIndex = elementXIndex - convolutionDimension / 2;
    int startYIndex = elementYIndex - convolutionDimension / 2;
    int stopXIndex = startXIndex + convolutionDimension;
    int stopYIndex = startYIndex + convolutionDimension;

    //get the matrix bounds
    int xMax = lines;
    int yMax = cols;

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

void convertMatrixSequential() {
    for (int i = 0; i < lines; i++) {
        for (int j = 0; j < cols; j++) {
            resultMatrix[i][j] = convertElement(i, j);
        }
    }
}

class LineThread {
private:
    int start, stop;

public:
    LineThread(int start, int stop) : start(start), stop(stop) {}

    void operator()() const {
        for (int i = start; i < stop; i++) {
            for (int j = 0; j < cols; j++) {
                resultMatrix[i][j] = convertElement(i, j);
            }
        }
    }
};

void convertMatrixParallelByLines() {
    int rest = lines % noThreads;
    int threadLines = lines / noThreads;

    vector<thread> threads(noThreads);

    int startSegment = 0;
    int endSegment;

    for (int i = 0; i < noThreads; i++) {
        endSegment = startSegment + threadLines;
        if (rest != 0) {
            endSegment++;
            rest--;
        }

        threads[i] = thread(LineThread(startSegment, endSegment));
        startSegment = endSegment;
    }

    for (int i = 0; i < noThreads; i++) {
        threads[i].join();
    }
}

class ColumnThread {
private:
    int start, stop;

public:
    ColumnThread(int start, int stop) : start(start), stop(stop) {}

    void operator()() const {
        for (int j = start; j < stop; j++) {
            for (int i = 0; i < lines; i++) {
                resultMatrix[i][j] = convertElement(i, j);
            }
        }
    }
};

void convertMatrixParallelByColumns() {
    int rest = cols % noThreads;
    int threadColumns = cols / noThreads;

    vector<thread> threads(noThreads);

    int startSegment = 0;
    int endSegment;

    for (int i = 0; i < noThreads; i++) {
        endSegment = startSegment + threadColumns;
        if (rest != 0) {
            endSegment++;
            rest--;
        }

        threads[i] = thread(ColumnThread(startSegment, endSegment));
        startSegment = endSegment;
    }

    for (int i = 0; i < noThreads; i++) {
        threads[i].join();
    }
}

void resetResultMatrix() {
    for (int i = 0; i < lines; i++) {
        for (int j = 0; j < cols; j++) {
            resultMatrix[i][j] = 0;
        }
    }
}

int main(int argc, char **argv) {
    for (int i = 1; i < argc; i++) {
        if (strcmp(argv[i], "--regenerate") == 0) {
            if (i + 1 < argc) {
                string arg = argv[i + 1];
                if (arg == "true" || arg == "1") {
                    regenerate = true;
                }
                i++;
            }
        }

        if (strcmp(argv[i], "--lines") == 0) {
            if (i + 1 < argc) {
                int arg = stoi(argv[i + 1]);
                if (arg > 0) {
                    lines = arg;
                }
            }
        }

        if (strcmp(argv[i], "--columns") == 0) {
            if (i + 1 < argc) {
                int arg = stoi(argv[i + 1]);
                if (arg > 0) {
                    cols = arg;
                }
            }
        }

        if (strcmp(argv[i], "--convolution") == 0) {
            if (i + 1 < argc) {
                int arg = stoi(argv[i + 1]);
                if (arg > 0) {
                    convolutionDimension = arg;
                }
            }
        }

        if (strcmp(argv[i], "--threads") == 0) {
            if (i + 1 < argc) {
                int arg = stoi(argv[i + 1]);
                if (arg > 0) {
                    noThreads = arg;
                }
            }
        }

        if (strcmp(argv[i], "--help") == 0) {
            cout << "--lines: number of lines that the matrix will have, DEFAULT 5" << '\n';
            cout << "--columns: number of columns that the matrix will have, DEFAULT 5" << '\n';
            cout << "--convolution: the dimension of the convolution matrix, DEFAULT 3" << '\n';
            cout << "--threads: the number of threads used, DEFAULT 4" << '\n';
            cout << "--regenerate: value true will regenerate the matrix and the convolution matrix, DEFAULT false"
                 << '\n';
            return 0;
        }
    }

#if DYNAMIC_ALLOCATION
    allocateDynamicMemory();
#endif

    if (regenerate) {
        generateRandomMatrix(lines, cols, convolutionDimension, noThreads);
    }
    readFromFile();

    resetResultMatrix();
    auto t_start = chrono::steady_clock::now();
    convertMatrixSequential();
    auto t_final = chrono::steady_clock::now();
    auto diff = t_final - t_start;
    cout << "computation time of the main thread secv= " << chrono::duration<double, milli>(diff).count() << " ms"
         << endl;

    writeResultToFile();

    resetResultMatrix();
    auto t_start_parallel_line = chrono::steady_clock::now();
    convertMatrixParallelByLines();
    auto t_final_parallel_line = chrono::steady_clock::now();
    auto diff_parallel_line = t_final_parallel_line - t_start_parallel_line;
    cout << "computation time of the main thread parallel by line= "
         << chrono::duration<double, milli>(diff_parallel_line).count() << " ms" << endl;

    compareResultsWithFile();

    resetResultMatrix();
    auto t_start_parallel_column = chrono::steady_clock::now();
    convertMatrixParallelByColumns();
    auto t_final_parallel_column = chrono::steady_clock::now();
    auto diff_parallel_column = t_final_parallel_column - t_start_parallel_column;
    cout << "computation time of the main thread parallel by column= "
         << chrono::duration<double, milli>(diff_parallel_column).count() << " ms" << endl;

    compareResultsWithFile();

    return 0;
}
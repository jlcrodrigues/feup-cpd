import java.util.Scanner;

public class MatrixMultiplication {

public static void onMult(int m_ar, int m_br) {

    long startTime, endTime;

    double[] pha, phb, phc;

    pha = new double[m_ar * m_ar];
    phb = new double[m_ar * m_ar];
    phc = new double[m_ar * m_ar];

    for (int i = 0; i < m_ar; i++) {
        for (int j = 0; j < m_ar; j++) {
            pha[i * m_ar + j] = 1.0;
        }
    }

    for (int i = 0; i < m_br; i++) {
        for (int j = 0; j < m_br; j++) {
            phb[i * m_br + j] = i + 1;
        }
    }

    startTime = System.currentTimeMillis();

    for (int i = 0; i < m_ar; i++) {
        for (int j = 0; j < m_br; j++) {
            double temp = 0;
            for (int k = 0; k < m_ar; k++) {
                temp += pha[i * m_ar + k] * phb[k * m_br + j];
            }
            phc[i * m_ar + j] = temp;
        }
    }

    endTime = System.currentTimeMillis();

    System.out.println("Time: " + (endTime - startTime) / 1000.0 + " seconds");

    System.out.println("Result matrix: ");
    for (int i = 0; i < 1; i++) {
        for (int j = 0; j < Math.min(10, m_br); j++) {
            System.out.print(phc[j] + " ");
        }
    }
    System.out.println();
}

public static void onMultLine(int m_ar, int m_br) {

    long startTime, endTime;

    double[] pha, phb, phc;

    pha = new double[m_ar * m_ar];
    phb = new double[m_ar * m_br];
    phc = new double[m_ar * m_br];

    for (int i = 0; i < m_ar; i++) {
        for (int j = 0; j < m_ar; j++) {
            pha[i * m_ar + j] = 1.0 ;
        }
    }

    for (int i = 0; i < m_br; i++) {
        for (int j = 0; j < m_br; j++) {
            phb[i * m_br + j] = i + 1;
        }
    }

    startTime = System.currentTimeMillis();

    for (int i = 0; i < m_ar; i++) {
        for (int j = 0; j < m_br; j++) {
            double value = pha[i * m_ar + j];
            for (int k = 0; k < m_ar; k++) {
                phc[i * m_ar + k] += value * phb[j * m_br + k];
            }
        }
    }

    endTime = System.currentTimeMillis();

    System.out.println("Time: " + (endTime - startTime) / 1000.0 + " seconds");

    System.out.println("Result matrix: ");
    for (int i = 0; i < 1; i++) {
        for (int j = 0; j < Math.min(10, m_br); j++) {
            System.out.print(phc[j] + " ");
        }
    }
    System.out.println();
}


public static void onMultBlock(int m_ar, int m_br, int bkSize) {

    long startTime, endTime;

    double[] pha, phb, phc;

    pha = new double[m_ar * m_ar];
    phb = new double[m_ar * m_br];
    phc = new double[m_ar * m_br];

    for (int i = 0; i < m_ar; i++) {
        for (int j = 0; j < m_ar; j++) {
            pha[i * m_ar + j] = 1.0 ;
        }
    }

    for (int i = 0; i < m_br; i++) {
        for (int j = 0; j < m_br; j++) {
            phb[i * m_br + j] = i + 1;
        }
    }

    startTime = System.currentTimeMillis();

    for (int i = 0; i < m_ar; i += bkSize) {
        for (int j = 0; j < m_br; j += bkSize) {
            for (int k = 0; k < m_ar; k += bkSize) {

                for (int a = i; a < Math.min(i + bkSize, m_ar); a++) {
                    for (int b = k; b < Math.min(k + bkSize, m_br); b++) {
                        for (int c = j; c < Math.min(j + bkSize, m_ar); c++) {
                            phc[m_ar * a + c] += pha[m_ar * a + b] * phb[m_ar * b + c];
                        }
                    }
                }
            }
        }
    }

    endTime = System.currentTimeMillis();

    System.out.println("Time: " + (endTime - startTime) / 1000.0 + " seconds");

    System.out.println("Result matrix: ");
    for (int i = 0; i < 1; i++) {
        for (int j = 0; j < Math.min(10, m_br); j++) {
            System.out.print(phc[j] + " ");
        }
    }
    System.out.println();

}

public static void main(String[] args) {
    Scanner sc = new Scanner(System.in);
    int op;
    int size, blockSize;

    System.out.print("Size of the matrix: ");
    size = sc.nextInt();

    System.out.print("\n1 - simple multiplication\n2 - line multiplication\n3 - block multiplication\n\nOption: ");
    op = sc.nextInt();

    switch (op) {
    case 1:
        onMult(size, size);
        break;
    case 2:
        onMultLine(size, size);
        break;
    case 3:
        System.out.print("Enter the block size: ");
        blockSize = sc.nextInt();
        System.out.println();
        onMultBlock(size, size, blockSize);
        break;
    default:
        break;
}

}
}
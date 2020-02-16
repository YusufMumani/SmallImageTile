package alg;

import static java.util.Arrays.stream;

import java.util.Locale;

public class Matrix {

	public static void main(String... strings) {
		double x1 = 0, y1 = 0;
		double x2 = 1, y2 = 0;
		double x3 = 0, y3 = 1;
		double x4 = 1, y4 = 1;

		double x1p = 0, y1p = 0;
		double x2p = 1, y2p = 0;
		double x3p = 0, y3p = 1;
		double x4p = 1, y4p = 1;

		double angleX = 0;
		double angleY = 45;
		double angleZ = 0;
		double scaleX = 1;
		double scaleY = 1;
		double scaleZ = 1;
		double translateX = 0;
		double translateY = 0;
		double translateZ = 0;
		double[][] rotationX = { { 1, 0, 0, 0 },
				{ 0, Math.cos(Math.toRadians(angleX)), -Math.sin(Math.toRadians(angleX)), 0 },
				{ 0, Math.sin(Math.toRadians(angleX)), Math.cos(Math.toRadians(angleX)), 0 }, { 0, 0, 0, 1 } };
		double[][] rotationY = { { Math.cos(Math.toRadians(angleY)), 0, Math.sin(Math.toRadians(angleY)), 0 },
				{ 0, 1, 0, 0 }, { -Math.sin(Math.toRadians(angleY)), 0, Math.cos(Math.toRadians(angleY)), 0 },
				{ 0, 0, 0, 1 } };
		double[][] rotationZ = { { Math.cos(Math.toRadians(angleZ)), -Math.sin(Math.toRadians(angleZ)), 0, 0 },
				{ Math.sin(Math.toRadians(angleZ)), Math.cos(Math.toRadians(angleZ)), 0, 0 }, { 0, 0, 1, 0 },
				{ 0, 0, 0, 1 } };
		double[][] scale3D = { { scaleX, 0, 0, 0 }, { 0, scaleY, 0, 0 }, { 0, 0, scaleZ, 0 }, { 0, 0, 0, 1 } };
		double[][] translation3D = { { 1, 0, 0, translateX }, { 0, 1, 0, translateY }, { 0, 0, 1, translateZ },
				{ 0, 0, 0, 1 } };
		double[][] point = { { 5 }, { 5 }, { 1 }, { 1 } };
		double[][] transformation = matrixMul(
				matrixMul(matrixMul(matrixMul(translation3D, rotationX), rotationY), rotationZ), scale3D);
		double[][] pointPrime = matrixMul(transformation, point);
		double[][] pointPrime2 = matrixMul(inverse(transformation), pointPrime);
		print(point);
		print(pointPrime);
		print(pointPrime2);
	}

	public static double[][] inverse(double[][] mat) {
		double d = determinant(mat, mat.length);
		if (d == 0)
			throw new UnsupportedOperationException("Zero derminant!");
		else
			return cofactor(mat, mat.length);
	}

	public static double[][] cofactor(double[][] mat, int f/* Order of Matrix*/) {
		double[][] b = new double[mat.length][mat.length], fac = new double[mat.length][mat.length];
		int p, q, m, n, i, j;
		for (q = 0; q < f; q++) {
			for (p = 0; p < f; p++) {
				m = 0;
				n = 0;
				for (i = 0; i < f; i++) {
					for (j = 0; j < f; j++) {
						if (i != q && j != p) {
							b[m][n] = mat[i][j];
							if (n < (f - 2))
								n++;
							else {
								n = 0;
								m++;
							}
						}
					}
				}
				fac[q][p] = Math.pow(-1, q + p) * determinant(b, f - 1);
			}
		}

		return transpose(mat, fac, f);
	}

	/*Finding transpose of matrix*/
	public static double[][] transpose(double[][] mat, double[][] fac, double r/* Order of matrix */) {
		int i, j;
		double[][] b = new double[mat.length][mat.length], inverse = new double[mat.length][mat.length];
		double d;

		for (i = 0; i < r; i++) {
			for (j = 0; j < r; j++) {
				b[i][j] = fac[j][i];
			}
		}

		d = determinant(mat, r);

		for (i = 0; i < r; i++) {
			for (j = 0; j < r; j++) {
				inverse[i][j] = b[i][j] / d;
			}
		}

		return inverse;
	}

	/* For calculating Determinant of the Matrix */
	public static double determinant(double[][] a, double k) {
		double s = 1, det = 0;
		double[][] b = new double[a.length][a.length];
		int i, j, m, n, c;
		if (k == 1) {
			return (a[0][0]);
		} else {
			det = 0;
			for (c = 0; c < k; c++) {
				m = 0;
				n = 0;
				for (i = 0; i < k; i++) {
					for (j = 0; j < k; j++) {
						b[i][j] = 0;
						if (i != 0 && j != c) {
							b[m][n] = a[i][j];
							if (n < (k - 2))
								n++;
							else {
								n = 0;
								m++;
							}
						}
					}
				}

				det = det + s * (a[0][c] * determinant(b, k - 1));
				s = -1 * s;
			}
		}
		return det;
	}

	public static void print(double[][] m) {
		stream(m).forEach(a -> {
			stream(a).forEach(n -> System.out.printf(Locale.US, "%5.1f ", n));
			System.out.println();
		});
		System.out.println();
	}

	public static double[][] matrixMul(double[][] A, double[][] B) {
		double[][] result = new double[A.length][B[0].length];
		for (int m = 0; m < A.length; m++)
			for (int n = 0; n < B.length; n++)
				for (int k = 0; k < B[0].length; k++)
					result[m][k] += A[m][n] * B[n][k];
		return result;
	}

	public static double[][] scalar(double scalar, int order) {
		double[][] result = new double[order][order];
		for (int i = 0; i < order; i++) {
			for (int j = 0; j < order; j++) {
				if (i == j)
					result[i][j] = scalar;
			}
		}
		return result;
	}

	/**
	* Gaussian elimination
	* @param  array $A matrix
	* @param  array $x vector
	* @return array    solution vector
	*/
	public static double[][] gauss(double[][] A) {
		int n = A.length;

		for (int i = 0; i < n; i++) {
			// Search for maximum in this column
			double maxEl = Math.abs(A[i][i]);
			int maxRow = i;
			for (int k = i + 1; k < n; k++) {
				if (Math.abs(A[k][i]) > maxEl) {
					maxEl = Math.abs(A[k][i]);
					maxRow = k;
				}
			}

			// Swap maximum row with current row (column by column)
			for (int k = i; k < n + 1; k++) {
				double tmp = A[maxRow][k];
				A[maxRow][k] = A[i][k];
				A[i][k] = tmp;
			}

			// Make all rows below this one 0 in current column
			for (int k = i + 1; k < n; k++) {
				double c = -A[k][i] / A[i][i];
				for (int j = i; j < n + 1; j++) {
					if (i == j) {
						A[k][j] = 0;
					} else {
						A[k][j] += c * A[i][j];
					}
				}
			}
		}

		// Make all rows below this one 0 in current column
		double[][] X = new double[n][1];
		for (int i = n - 1; i > -1; i--) {
			X[i][0] = A[i][n] / A[i][i];
			for (int k = i - 1; k > -1; k--) {
				A[k][n] -= A[k][i] * X[i][0];
			}
		}

		return X;
	}
}

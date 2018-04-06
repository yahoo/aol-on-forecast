/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

/**
 * Class implementing commonly used computations.
 */
public class IFSComputation {

	/**
	 * Solve NxN linear equations (Ax = b).
	 * 
	 * @param matrix Augmented matrix. Includes A in NxN rows and columns
	 *    and b in column N+1. A triangular matrix will be left in its place.
	 *    
	 * @return Solution to equations (N values).
	 * 
	 * @throws IFSException if a solution could not be computed or the matrix
	 *    was improperly specified.
	 */
	public static double[] getLinearEqnSoln(
		double[][]	matrix
	) throws IFSException {
		if (matrix == null || matrix.length < 2)
			throw new IFSException(1);
		else
			for (int i = 0; i < matrix.length; i++)
				if (matrix[i].length != (matrix.length+1))
					throw new IFSException(2, i+1, matrix.length+1);

		int			n = matrix.length;
		double[]	x = new double[n];
		int			i;
		int			j;
		int			k;
		int			p;
		double		t;

		for (i = 0; i < n-1; i++) {
			for (p = i; p < n; p++)
				if (matrix[p][i] != 0.0)
					break;
			if (p == n)
				throw new IFSException(3, i+1);
			if (p != i)
				for (j = 0; j <= n; j++) {
					t = matrix[p][j];
					matrix[p][j] = matrix[i][j];
					matrix[i][j] = t;
				}
			for (j = i+1; j < n; j++) {
				t = matrix[j][i]/matrix[i][i];
				for (k = i; k < n+1; k++)
					matrix[j][k] -= t*matrix[i][k];
			}
		}
		if (matrix[n-1][n-1] == 0.0)
			throw new IFSException(4);
		x[n-1] = matrix[n-1][n]/matrix[n-1][n-1];
		for (i = n-2; i >= 0; i--) {
			t = 0.0;
			for (j = i+1; j < n; j++)
				t += matrix[i][j]*x[j];
			x[i] = (matrix[i][n]-t)/matrix[i][i];
		}
		return x;
	}
}

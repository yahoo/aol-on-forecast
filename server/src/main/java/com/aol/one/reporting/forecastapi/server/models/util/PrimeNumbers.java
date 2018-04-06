/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.util;

import java.util.ArrayList;
import java.util.List;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;

/**
 * Class that defines various prime number operations and provides a test
 * driver.
 */
public final class PrimeNumbers {
	
	/**
	 * Generate a vector containing the prime number factors of a given
	 * integer greater than or equal to 1.
	 * 
	 * @param value Integer greater than or equal to 1.
	 * 
	 * @return Vector containing prime factors.
	 * 
	 * @throws IFSException Thrown if integer is less than or equal to 1.
	 */
	public static int[] getPrimeFactors(
		int		value
	) throws IFSException {
		if (value < 1)
			throw new IFSException("Cannot compute prime factors for a number "
			+ "less than 1: "
			+ value
			+ ".");
		else if (value == 1)
			return new int[] {1};
		
		int[]			primes = getPrimes(value);
		List<Integer>	factors = new ArrayList<Integer>();
		
		for (int prime : primes) {
			if (prime*prime > value)
				break;
			while (value%prime == 0) {
				factors.add(prime);
				value /= prime;
			}
		}
		if (value > 1)
			factors.add(value);
		
		int[]		ifactors = new int[factors.size()];
		int			i = 0;
		
		for (int factor : factors)
			ifactors[i++] = factor;
		return ifactors;
	}
	
	/**
	 * Generate a vector containing the prime numbers up to and including a
	 * given integer greater than 1. Implements the Sieve of Eratosthenes.
	 * 
	 * @param value Integer greater than 1.
	 * 
	 * @return Vector containing prime numbers.
	 * 
	 * @throws IFSException Thrown if integer is less than or equal to 1.
	 */
	public static int[] getPrimes(
		int		value
	) throws IFSException {
		if (value <= 1)
			throw new IFSException("Cannot compute primes for a number less "
			+ "than or equal to 1: "
			+ value
			+ ".");
		
		List<Integer>	primes = new ArrayList<Integer>();
		boolean[]		is_comp = new boolean[value+1];
		int				value_s = (int)Math.sqrt(value);
		
		for (int i = 2; i <= value_s; i++)
			if (!is_comp[i]) {
				primes.add(i);
				for (int j = i*i; j <= value; j += i)
					is_comp[j] = true;
			}
		for (int i = value_s+1; i <= value; i++)
			if (!is_comp[i])
				primes.add(i);
		
		int[]		iprimes = new int[primes.size()];
		int			i = 0;
		
		for (int prime : primes)
			iprimes[i++] = prime;
		return iprimes;
	}

	/**
	 * Execute a prime number operation.
	 * 
	 * @param args Operation to execute and its arguments.
	 */
	public static void main(
		String[]	args
	) {
		if (args.length != 2) {
			usage();
			System.exit(0);
		}
		
		String		operation = args[0];
		
		if (!operation.equalsIgnoreCase("factor")
		&& !operation.equalsIgnoreCase("primes")) {
			usage();
			System.exit(1);
		}
		
		String		s_value = args[1];
		int			value = 0;
		
		try {
		value = Integer.parseInt(s_value);
		} catch (NumberFormatException ex) {
			System.err.printf("'%s' is not an integer.\n", s_value);
			System.exit(1);
		}
		if (value <= 1) {
			System.err.printf("'%s' is not greater than 1.\n", s_value);
			System.exit(1);
		}
		
		try {
		if (operation.equalsIgnoreCase("factor")) {
			int[]		factors = getPrimeFactors(value);
			boolean		is_first = true;
			
			for (int factor : factors) {
				if (!is_first)
					System.out.printf(" ");
				else
					is_first = false;
				System.out.printf("%d", factor);
			}
			System.out.printf("\n");
		} else if (operation.equalsIgnoreCase("primes")) {
			int[]		primes = getPrimes(value);
			boolean		is_first = true;
			
			for (int prime : primes) {
				if (!is_first)
					System.out.printf(" ");
				else
					is_first = false;
				System.out.printf("%d", prime);
			}
			System.out.printf("\n");
		}
		} catch (IFSException ex) {
			System.err.printf("'%s' operation encountered an error: %s\n",
				operation, ex.getMessage());
			System.exit(1);
		}
		
		System.exit(0);
	}

/*******************/
/* Private Methods */
/*******************/

    /**
     * Print main program usage information.
     */
    private static void usage() {
        System.err.print(
  "\n"
+ "PrimeNumbers -- Execute various prime number operations.\n"
+ "\n"
+ "Usage: com.aol.ifs.soa.utils.PrimeNumbers <operation> <integer greater than 1>\n"
+ "\n"
+ "<operation> -- Prime number operation to execute. Possible values are:\n"
+ "\n"
+ "   factor -- List the prime factors for a number.\n"
+ "   primes -- List the prime numbers up to and including the specified number.\n"
+ "\n"
        );
    }
}

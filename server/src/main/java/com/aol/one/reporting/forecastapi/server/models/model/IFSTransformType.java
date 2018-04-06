/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.model;

/**
 * Class that implements data transformations and can be used for history
 * and the reverse on forecasts. Transformations are used in the forecast
 * model interface for setting a series and obtaining forecasts. The
 * transformations supported are:
 * 
 * None    -- Do not transform historical values and forecasts.
 * Log     -- Take natural log of historical values and exponentiate forecasts.
 * Log10   -- Take log base 10 of historical values and use forecasts as base
 *            10 exponent.
 * Quartic -- Take quartic root of historical values and raise forecasts to
 *            4th power.
 * Sqrt    -- Take square root of historical values and square forecasts.
 */
public final class IFSTransformType {
	public static enum	Types {
		None, Log, Log10, Quartic, Sqrt
	};
	
	/**
	 * Get transformation type for a specification. Basically the specification
	 * is the string version of the transformation to perform.
	 * 
	 * @param spec Transformation name.
	 * 
	 * @return Transformation type corresponding to transformation spec.
	 * 
	 * @throws IFSException if an unknown transformation type is specified.
	 */
	public static Types getTransformType(
		String	spec
	) throws IFSException {
		if (spec == null || spec.equals(""))
			throw new IFSException(47);
		else if (spec.equals("none"))
			return(Types.None);
		else if (spec.equals("log"))
			return(Types.Log);
		else if (spec.equals("log10"))
			return(Types.Log10);
		else if (spec.equals("quartic"))
			return(Types.Quartic);
		else if (spec.equals("sqrt"))
			return(Types.Sqrt);
		else
			throw new IFSException(48, spec);
	}
	
	/**
	 * Transform the series according to type specification. A new series
	 * with the transformed series is returned unless no transformation was
	 * performed.
	 * 
	 * @param values Values to transform.
	 * @param type Transformation type.
	 * 
	 * @return New series if a transformation was performed. Otherwise the
	 *    input is returned.
	 */
	public static double[] getTransformedValues(
		double[] 	values,
		Types		type
	) {
		if (values == null || values.length == 0 || type == Types.None)
			return(values);
		
		double[]	new_values = new double[values.length];
		
		switch (type) {
		case Log:
			for (int i = 0; i < values.length; i++)
				if (values[i] <= 0)
					new_values[i] = 1.0;
				else
					new_values[i] = Math.log(values[i]);
			break;
		case Log10:
			for (int i = 0; i < values.length; i++)
				if (values[i] <= 0)
					new_values[i] = 1.0;
				else
					new_values[i] = Math.log10(values[i]);
			break;
		case Quartic:
			for (int i = 0; i < values.length; i++)
				if (values[i] < 0)
					new_values[i] = 0.0;
				else
					new_values[i] = Math.pow(values[i], 0.25);
			break;
		case Sqrt:
			for (int i = 0; i < values.length; i++)
				if (values[i] < 0)
					new_values[i] = 0.0;
				else
					new_values[i] = Math.sqrt(values[i]);
			break;
		default:
			break;
		}
		
		return(new_values);
	}
	
	/**
	 * Untransform the series according to type specification. A new series
	 * with the untransformed series is returned unless no transformation was
	 * performed.
	 * 
	 * @param values Values to transform.
	 * @param type Transformation type.
	 * 
	 * @return New series if an untransformation was performed. Otherwise the
	 *    input is returned.
	 */
	public static double[] getUntransformedValues(
		double[] 	values,
		Types		type
	) {
		if (values == null || values.length == 0 || type == Types.None)
			return(values);
		
		double[]	new_values = new double[values.length];
		
		switch (type) {
		case Log:
			for (int i = 0; i < values.length; i++)
				new_values[i] = Math.exp(values[i]);
			break;
		case Log10:
			for (int i = 0; i < values.length; i++)
				new_values[i] = Math.pow(10, values[i]);
			break;
		case Quartic:
			for (int i = 0; i < values.length; i++)
				new_values[i] = Math.pow(values[i], 4);
			break;
		case Sqrt:
			for (int i = 0; i < values.length; i++)
				new_values[i] = Math.pow(values[i], 2);
			break;
		default:
			break;
		}
		
		return(new_values);
	}

	
	/**
	 * Canned usage information for transform_type parameter.
	 * 
	 * @return Usage string.
	 */
	public static String usage() {
		return(
  "\n"
+ "transform_type=<transform type spec>\n"
+ "   none    -- Do not transform historical values and forecasts.\n"
+ "   log     -- Take natural log of historical values and exponentiate forecasts.\n"
+ "   log10   -- Take log base 10 of historical values and use forecasts as base\n"
+ "              10 exponent.\n"
+ "   quartic -- Take quartic root of historical values and raise forecasts to\n"
+ "              4th power.\n"
+ "   sqrt    -- Take square root of historical values and square forecasts.\n"
		);
	}
}

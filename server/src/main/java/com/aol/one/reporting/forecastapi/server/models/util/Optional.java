/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.util;

/**
 * Class implementing optional value for a specified type.
 */
public final class Optional<T> {
	private T		Value;
	
	/**
	 * Return empty optional.
	 * 
	 * @return Empty optional.
	 */
	public static <T> Optional<T> empty() {
		return new Optional<T>();
	}
	
	/**
	 * Fetch wrapped value.
	 * 
	 * @return Wrapped value.
	 */
	public T get() {
		return Value;
	}
	
	/**
	 * Is there a non-empty value wrapped.
	 * 
	 * @return True if not empty. False otherwise.
	 */
	public boolean isPresent() {
		return Value != null;
	}
	
	/**
	 * Return non-empty optional.
	 * 
	 * @param value Value to wrap.
	 * 
	 * @return Non-empty optional.
	 */
	public static <T> Optional<T> of(
		T	value
	) {
		return new Optional<T>(value);
	}
	
/*******************/
/* Private Methods */
/*******************/

	/**
	 * Default constructor makes empty optional.
	 */
	private Optional() {
		Value = null;
	}
	
	/**
	 * Fully specified constructor.
	 * 
	 * @param value Value to wrap.
	 */
	private Optional(
		T	value
	) {
		Value = value;
	}
}

/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.util;

/**
 * Class implementing something similar to C++ pair STL class.
 */
public class Pair<A, B> {
    private A	First;
    private B	Second;

    /**
     * Fully specified constructor.
     * 
     * @param first First pair entry.
     * @param second Second pair entry.
     */
    public Pair(
    	A	first,
    	B	second
    ) {
    	First = first;
    	Second = second;
    }

    /**
     * @return Object hash code.
     */
    public int hashCode() {
    	int	hash_first = First != null ? First.hashCode() : 0;
    	int	hash_second = Second != null ? Second.hashCode() : 0;

    	return (hash_first + hash_second) * hash_second + hash_first;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(
    	Object	other
    ) {
    	if (other instanceof Pair<?, ?>) {
    		@SuppressWarnings("unchecked")
			Pair<A, B>	other_pair = (Pair<A, B>)other;
    		
    		return 
    		((First == other_pair.First ||
    			(First != null && other_pair.First != null &&
    			  First.equals(other_pair.First))) &&
    		 (Second == other_pair.Second ||
    			(Second != null && other_pair.Second != null &&
    			  Second.equals(other_pair.Second))));
    	}

    	return false;
    }

    /**
     * @return First entry.
     */
    public A getFirst() {
    	return First;
    }

    /**
     * @return Second entry.
     */
    public B getSecond() {
    	return Second;
    }

    /**
     * @param first First entry value.
     */
    public void setFirst(
    	A	first
    ) {
    	First = first;
    }

    /**
     * @param second Second entry value.
     */
    public void setSecond(
    	B	second
    ) {
    	Second = second;
    }

    /**
     * @return String representation.
     */
    public String toString() { 
           return "(" + First + ", " + Second + ")"; 
    }
}

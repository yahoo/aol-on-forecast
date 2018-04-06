/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.cs;

import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterSpec;

/**
 * Class implementing named model parameter specifications
 * ({@link IFSParameterSpec}. A named parameter specification has ordering,
 * equality, and hashing attributes.
 */
public final class IFSCannedSet implements Comparable<IFSCannedSet> {
	private String				Name;
	private String				Description;
	private IFSParameterSpec	ParameterSpec;
	
	/**
	 * Default constructor
	 */
	public IFSCannedSet() {
		setName(null);
		setDescription(null);
		setParameterSpec(null);
	}
	
	/**
	 * Fully specified constructor.
	 * 
	 * @param name Canned set name.
	 * @param description Canned set description.
	 * @param parameter_spec Canned set model parameter specification.
	 */
	public IFSCannedSet(
		String				name,
		String				description,
		IFSParameterSpec	parameter_spec
	) {
		setName(name);
		setDescription(description);
		setParameterSpec(parameter_spec);
	}
	
	/**
	 * Clone this canned set.
	 * 
	 * @return Canned set clone.
	 */
	public IFSCannedSet clone() {
		return new IFSCannedSet(Name, Description, ParameterSpec.clone());
	}

	/**
	 * Order canned sets according to canned set name.
	 * 
	 * @param that Canned set to compare with.
	 * 
	 * @return 0 if canned set names are equal. -1 if this canned set name
	 *    lexically precedes that canned set name. 1 otherwise.
	 */
	public int compareTo(
		IFSCannedSet	that
	) {
		if (this.getName() == null && that.getName() == null)
			return 0;
		else if (this.getName() != null && that.getName() == null)
			return 1;
		else if (this.getName() == null && that.getName() != null)
			return -1;
		else
			return this.getName().compareTo(that.getName());
	}
	
	/**
	 * Are two canned sets equal?
	 * 
	 * @param obj Canned set to compare with.
	 * 
	 * @return True if canned sets have the same name. False if they do not.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return(true);
		if (obj == null)
			return(false);
		if (getClass() != obj.getClass())
			return(false);
		
		IFSCannedSet	that = (IFSCannedSet)obj;
		
		if (this.getName() == null || that.getName() == null)
			return(false);
		
		return(this.getName().equals(that.getName()));
	}
	
	/**
	 * Fetch canned set description.
	 * 
	 * @return Canned set description.
	 */
	public String getDescription() {
		return Description;
	}
	
	/**
	 * Fetch canned set name.
	 * 
	 * @return Canned set name.
	 */
	public String getName() {
		return Name;
	}
	
	/**
	 * Fetch parameter specification.
	 * 
	 * @return Model parameter specification.
	 */
	public IFSParameterSpec getParameterSpec() {
		return ParameterSpec;
	}

	/**
	 * Hash a canned set object based on canned set name.
	 * 
	 * @return Hash code of canned set name. 0 if name is null.
	 */
	@Override
	public int hashCode() {
		if (getName() == null)
			return 0;
		else
			return getName().hashCode();
	}
	
	/**
	 * Set canned set description.
	 * 
	 * @param description Canned set description.
	 */
	public void setDescription(
		String		description
	) {
		Description = description;
	}
	
	/**
	 * Set canned set name.
	 * 
	 * @param name Canned set name.
	 */
	public void setName(
		String		name
	) {
		Name = name;
	}
	
	/**
	 * Set model parameter specification.
	 * 
	 * @param parameter_spec Canned set model parameter specification.
	 */
	public void setParameterSpec(
		IFSParameterSpec	parameter_spec
	) {
		ParameterSpec = parameter_spec;
	}
}

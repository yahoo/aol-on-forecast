/********************************************************************************
 * Copyright 2018, Oath Inc.
 * Licensed under the terms of the Apache Version 2.0 license.
 * See LICENSE file in project root directory for terms.
 ********************************************************************************/

package com.aol.one.reporting.forecastapi.server.models.cs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterSpec;
import com.aol.one.reporting.forecastapi.server.models.model.IFSParameterValue;

/**
 * Class implementing methods for fetching canned set definitions from a file.
 */
public final class GetCannedSetDefinitions {
	
	/**
	 * Fetch the canned set definitions from a specified file. The file may
	 * contain no definitions, but, if it does, there must be one parameter
	 * definition per line in the following format:
	 * 
	 * <canned set id> <parameter> <value>
	 * 
	 * Note each canned set must have a special parameter 'model' that indicates
	 * the model to be used.
	 * 
	 * @param canned_set_definition_file File containing canned set definitions.
	 * 
	 * @return Canned set definition map which could be empty.
	 * 
	 * @throws IFSException Thrown if file has an unexpected format.
	 */
	public static Map<String, IFSCannedSet> getCannedSetDefinitions(
		String canned_set_definition_file
	) throws IFSException {
        String						line = null;
        Map<String, IFSCannedSet>	definitions = null;
        IFSCannedSet				canned_set = null;
        IFSParameterSpec			canned_set_spec = null;
        StringTokenizer				tokens = null;
        int							line_num = 0;
        String						id = null;
        String						parameter = null;
        String						value = null;
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(
        	new FileInputStream(canned_set_definition_file)))){
        definitions = new HashMap<String, IFSCannedSet>();
        while ((line = in.readLine()) != null) {
        	line_num++;
        	tokens = new StringTokenizer(line, " ");
        	if (tokens.countTokens() <= 0) {
        		continue;
        	} else if (tokens.countTokens() < 3) {
        		throw new IFSException(String.format("Canned set definition "
        		+ "file '%s' has the wrong format at line %d.",
        		canned_set_definition_file, line_num));
        	}
        	id = tokens.nextToken();
        	parameter = tokens.nextToken();
        	value = tokens.nextToken();
        	if (parameter.equals("desc")) {
        		while (tokens.hasMoreTokens()) {
        			value += " " + tokens.nextToken();
        		}
        	} else if (tokens.hasMoreTokens()) {
        		throw new IFSException(String.format("Canned set definition "
        		+ "file '%s' has the wrong format at line %d.",
        		canned_set_definition_file, line_num));
        	}
        	canned_set = definitions.get(id);
        	if (canned_set == null) {
        		canned_set = new IFSCannedSet();
        		canned_set.setName(id);
        		canned_set.setDescription("");
        		canned_set_spec = new IFSParameterSpec();
        		canned_set_spec.setParameterValues(new ArrayList<IFSParameterValue>());
        		canned_set.setParameterSpec(canned_set_spec);
        		definitions.put(id, canned_set);
        	}
        	canned_set_spec = canned_set.getParameterSpec();
        	if (parameter.equals("model")) {
        		canned_set_spec.setModel(value);
        	} else if (parameter.equals("desc")) {
            	canned_set.setDescription(value);
        	} else {
        		canned_set_spec.getParameterValues().add(
        			new IFSParameterValue(parameter, value));
        	}
        }
        } catch (FileNotFoundException ex) {
        	throw new IFSException(String.format("Could not read '%s': %s",
        		canned_set_definition_file, ex.getMessage()));
        } catch (SecurityException ex) {
        	throw new IFSException(String.format("Could not read '%s': %s",
            	canned_set_definition_file, ex.getMessage()));
        } catch (IOException ex) {
        	throw new IFSException(String.format("Could not read '%s': %s",
            	canned_set_definition_file, ex.getMessage()));
        }
        
        for (IFSCannedSet cs : definitions.values()) {
        	if (cs.getParameterSpec().getModel() == null) {
        		throw new IFSException(String.format("Canned set definition "
         		+ "'%s' has no model specified.", cs.getName()));
        	}
        }
        
		return definitions;
	}
}

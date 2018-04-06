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
import java.util.List;
import java.util.Map;

import com.aol.one.reporting.forecastapi.server.models.model.IFSException;

/**
 * Class implementing methods for fetching canned set candidates from a file.
 */
public final class GetCannedSetCandidates {
	
	/**
	 * Fetch the canned set candidates from the designated file. The file
	 * contains a list of canned set ids (one id per line). Each canned set
	 * id is compared against a collection of canned set definitions to arrive
	 * at a list of candidate canned sets. The list of canned set ids has the
	 * following format:
	 * 
	 * <canned set id>
	 * ...
	 * <canned set id>
	 * 
	 * @param canned_set_candidate_file File containing canned set ids.
	 * @param canned_set_definitions Collection of canned sets to reference.
	 * 
	 * @return List of candidate canned sets.
	 * 
	 * @throws IFSException Thrown if canned set ids have an unexpected format
	 *    or any of the canned set ids cannot be found in the canned set
	 *    collection.
	 */
	@SuppressWarnings("resource")
	public static List<IFSCannedSet> getCannedSetCandidates(
		String						canned_set_candidate_file,
		Map<String, IFSCannedSet>	canned_set_definitions
	) throws IFSException {
        BufferedReader  		in = null;
        String					line = null;
		List<IFSCannedSet>		candidates = new ArrayList<IFSCannedSet>();
		String					canned_set_id = null;
		IFSCannedSet			canned_set = null;
        int						line_num = 0;
		
        try {
        in = new BufferedReader(new InputStreamReader(
        	new FileInputStream(canned_set_candidate_file)));
        while ((line = in.readLine()) != null) {
        	line_num++;
			canned_set_id = line.trim();
			canned_set = canned_set_definitions.get(canned_set_id);
			if (canned_set == null)
				throw new IFSException(String.format("Candidate canned set id "
				+ "'%s' at line %d is not a canned set definition.",
				canned_set_id, line_num));
			candidates.add(canned_set);
		}
        in.close();
        } catch (FileNotFoundException ex) {
        	throw new IFSException(String.format("Could not read '%s': %s",
        		canned_set_candidate_file, ex.getMessage()));
        } catch (SecurityException ex) {
        	throw new IFSException(String.format("Could not read '%s': %s",
        		canned_set_candidate_file, ex.getMessage()));
        } catch (IOException ex) {
        	throw new IFSException(String.format("Could not read '%s': %s",
        		canned_set_candidate_file, ex.getMessage()));
        }
       
		return candidates;
	}
}

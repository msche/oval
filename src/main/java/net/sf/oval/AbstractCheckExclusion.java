/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2011 Sebastian
 * Thomschke.
 * 
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sebastian Thomschke - initial implementation.
 *******************************************************************************/
package net.sf.oval;

import net.sf.oval.expression.ExpressionLanguage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Partial implementation of exclusion classes.
 * 
 * @author Sebastian Thomschke
 */
public abstract class AbstractCheckExclusion implements CheckExclusion
{
	private static final long serialVersionUID = 1L;

	private String[] profiles;

	public Map<String, String> getMessageVariables()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getProfiles()
	{
		return profiles;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProfiles(final String... profiles)
	{
		this.profiles = profiles;
	}

}

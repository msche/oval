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
package net.sf.oval.ogn;

import net.sf.oval.exception.ObjectGraphNavigatorNotAvailableException;
import net.sf.oval.internal.util.Assert;
import net.sf.oval.internal.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 *
 */
public class ObjectGraphNavigatorRegistry
{
	private static final Logger LOG = LoggerFactory.getLogger(ObjectGraphNavigatorRegistry.class);

	private final Map<String, ObjectGraphNavigator> cache = new LinkedHashMap<>(2);

	private ObjectGraphNavigator _initializeDefaultOGN(final String id)
	{
		// JXPath support
		if ("jxpath".equals(id) && ReflectionUtils.isClassPresent("org.apache.commons.jxpath.JXPathContext"))
			return registerObjectGraphNavigator("jxpath", new ObjectGraphNavigatorJXPathImpl());

		if ("".equals(id)) return registerObjectGraphNavigator("", new ObjectGraphNavigatorDefaultImpl());
		return null;
	}

	public ObjectGraphNavigator getObjectGraphNavigator(final String id)
	{
		Assert.argumentNotNull("id", id);

		ObjectGraphNavigator ogn = cache.get(id);

		if (ogn == null) ogn = _initializeDefaultOGN(id);

		if (ogn == null) throw new ObjectGraphNavigatorNotAvailableException(id);

		return ogn;
	}

	public ObjectGraphNavigator registerObjectGraphNavigator(final String id, final ObjectGraphNavigator ogn)
			throws IllegalArgumentException
	{
		Assert.argumentNotNull("id", id);
		Assert.argumentNotNull("ogn", ogn);

		LOG.info("Object Graph Navigator '{}' registered: {}", id, ogn);

		cache.put(id, ogn);
		return ogn;
	}
}

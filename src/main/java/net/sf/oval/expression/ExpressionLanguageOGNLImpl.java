/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2012 Sebastian
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
package net.sf.oval.expression;

import net.sf.oval.exception.ExpressionEvaluationException;
import net.sf.oval.internal.util.ObjectCache;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Sebastian Thomschke
 *
 */
public class ExpressionLanguageOGNLImpl implements ExpressionLanguage
{
	private static final Logger LOG = LoggerFactory.getLogger(ExpressionLanguageOGNLImpl.class);

	private final ObjectCache<String, Object> expressionCache = new ObjectCache<String, Object>();

	/**
	 * {@inheritDoc}
	 */
	public Object evaluate(final String expression, final Map<String, ? > values) throws ExpressionEvaluationException
	{
		LOG.debug("Evaluating OGNL expression: {}", expression);
		try
		{
			final OgnlContext ctx = (OgnlContext) Ognl.createDefaultContext(null);

			for (final Entry<String, ? > entry : values.entrySet())
				ctx.put(entry.getKey(), entry.getValue());

			Object expr = expressionCache.get(expression);
			if (expr == null)
			{
				expr = Ognl.parseExpression(expression);
				expressionCache.put(expression, expr);
			}
			return Ognl.getValue(expr, ctx);
		}
		catch (final OgnlException ex)
		{
			throw new ExpressionEvaluationException("Evaluating MVEL expression failed: " + expression, ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluateAsBoolean(final String expression, final Map<String, ? > values)
			throws ExpressionEvaluationException
	{
		final Object result = evaluate(expression, values);
		if (!(result instanceof Boolean))
			throw new ExpressionEvaluationException("The script must return a boolean value.");
		return (Boolean) result;
	}
}
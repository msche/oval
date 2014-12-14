/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2013 Sebastian
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.oval.Validator;
import net.sf.oval.exception.ExpressionEvaluationException;
import net.sf.oval.internal.util.ObjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSR223 Support
 *
 * @author Sebastian Thomschke
 */
public class ExpressionLanguageScriptEngineImpl implements ExpressionLanguage
{
	private static final Logger LOG = LoggerFactory.getLogger(ExpressionLanguageScriptEngineImpl.class);

	private static final ScriptEngineManager FACTORY = new ScriptEngineManager();

	static
	{
		final List<Object> languages = new ArrayList<>();
		for (final ScriptEngineFactory ef : FACTORY.getEngineFactories())
			languages.add(ef.getNames());
		LOG.info("Available ScriptEngine language names: {}", languages);
	}

	public static ExpressionLanguageScriptEngineImpl get(final String languageId)
	{
		final ScriptEngine engine = FACTORY.getEngineByName(languageId);
		return engine == null ? null : new ExpressionLanguageScriptEngineImpl(engine);
	}

	private final Compilable compilable;
	private final ScriptEngine engine;
	private final ObjectCache<String, CompiledScript> compiledCache;

	private ExpressionLanguageScriptEngineImpl(final ScriptEngine engine)
	{
		this.engine = engine;
		if (engine instanceof Compilable)
		{
			compilable = (Compilable) engine;
			compiledCache = new ObjectCache<String, CompiledScript>();
		}
		else
		{
			compilable = null;
			compiledCache = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object evaluate(final String expression, final Map<String, ? > values) throws ExpressionEvaluationException
	{
		LOG.debug("Evaluating JavaScript expression: {}", expression);
		try
		{
			final Bindings scope = engine.createBindings();
			for (final Entry<String, ? > entry : values.entrySet())
				scope.put(entry.getKey(), entry.getValue());

			if (compilable != null)
			{
				CompiledScript compiled = compiledCache.get(expression);
				if (compiled == null)
				{
					compiled = compilable.compile(expression);
					compiledCache.put(expression, compiled);
				}
				return compiled.eval(scope);
			}
			return engine.eval(expression, scope);
		}
		catch (final ScriptException ex)
		{
			throw new ExpressionEvaluationException("Evaluating JavaScript expression failed: " + expression, ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean evaluateAsBoolean(final String expression, final Map<String, ? > values) throws ExpressionEvaluationException
	{
		final Object result = evaluate(expression, values);
		if (!(result instanceof Boolean)) throw new ExpressionEvaluationException("The script must return a boolean value.");
		return (Boolean) result;
	}
}
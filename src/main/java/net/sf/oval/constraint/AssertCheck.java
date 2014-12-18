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
package net.sf.oval.constraint;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.configuration.annotation.ConstraintAnnotationSettings;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.ExpressionEvaluationException;
import net.sf.oval.exception.ExpressionLanguageNotAvailableException;
import net.sf.oval.expression.ExpressionLanguage;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public class AssertCheck extends AbstractAnnotationCheck<Assert>
{
	private static final long serialVersionUID = 1L;

	private String expr;
	private String lang;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Assert constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setExpr(constraintAnnotation.expr());
		setLang(constraintAnnotation.lang());
	}

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint settings
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected final ConstraintAnnotationSettings getSettings(final  Assert constraintAnnotation) {

        ConstraintAnnotationSettings settings = new ConstraintAnnotationSettings.Builder()
                .message(constraintAnnotation.message())
                .appliesTo(constraintAnnotation.appliesTo())
                .errorCode(constraintAnnotation.errorCode())
                .severity(constraintAnnotation.severity())
                .profiles(constraintAnnotation.profiles())
                .target(constraintAnnotation.target())
                .when(constraintAnnotation.when())
                .build();
        return settings;
    }


    /**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> createMessageVariables()
	{
		final Map<String, String> messageVariables = new LinkedHashMap<>(2);
		messageVariables.put("expression", expr);
		messageVariables.put("language", lang);
		return messageVariables;
	}

	/**
	 * @return the expression
	 */
	public String getExpr()
	{
		return expr;
	}

	/**
	 * @return the expression language
	 */
	public String getLang()
	{
		return lang;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator) throws ExpressionEvaluationException, ExpressionLanguageNotAvailableException
	{
		final Map<String, Object> values = new LinkedHashMap<>();
		values.put("_value", valueToValidate);
		values.put("_this", validatedObject);

		final ExpressionLanguage el = validator.getExpressionLanguageRegistry().getExpressionLanguage(lang);
		return el.evaluateAsBoolean(expr, values);
	}

	/**
	 * @param expression the expression to set
	 */
	public void setExpr(final String expression)
	{
		expr = expression;
		requireMessageVariablesRecreation();
	}

	/**
	 * @param language the expression language to set
	 */
	public void setLang(final String language)
	{
		lang = language;
		requireMessageVariablesRecreation();
	}
}

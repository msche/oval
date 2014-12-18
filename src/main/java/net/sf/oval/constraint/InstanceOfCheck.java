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
import net.sf.oval.internal.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public class InstanceOfCheck extends AbstractAnnotationCheck<InstanceOf>
{
	private static final long serialVersionUID = 1L;

	private Class< ? >[] types;

	@Override
	public void configure(final InstanceOf constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setTypes(constraintAnnotation.value());
	}

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint settings
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected final ConstraintAnnotationSettings getSettings(final  InstanceOf constraintAnnotation) {

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

    @Override
	protected Map<String, String> createMessageVariables()
	{
		final Map<String, String> messageVariables = new LinkedHashMap<>(2);
		if (types.length == 1)
			messageVariables.put("types", types[0].getName());
		else
		{
			final String[] classNames = new String[types.length];
			for (int i = 0, l = classNames.length; i < l; i++)
				classNames[i] = types[i].getName();
			messageVariables.put("types", StringUtils.implode(classNames, ","));
		}
		return messageVariables;
	}

	/**
	 * @return the type
	 */
	public Class< ? >[] getTypes()
	{
		return types;
	}

	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator)
	{
		if (valueToValidate == null) return true;

		for (final Class< ? > type : types)
			if (!type.isInstance(valueToValidate)) return false;
		return true;
	}

	/**
	 * @param types the types to set
	 */
	public void setTypes(final Class< ? >... types)
	{
		this.types = types;
		requireMessageVariablesRecreation();
	}
}

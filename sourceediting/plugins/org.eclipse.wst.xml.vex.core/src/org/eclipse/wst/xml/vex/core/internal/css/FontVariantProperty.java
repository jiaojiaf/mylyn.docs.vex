/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.xml.vex.core.internal.css;

import org.eclipse.wst.xml.vex.core.internal.dom.Element;
import org.w3c.css.sac.LexicalUnit;

/**
 * The CSS font-variant property.
 */
public class FontVariantProperty extends AbstractProperty {

	/**
	 * Class constructor.
	 */
	public FontVariantProperty() {
		super(CSS.FONT_VARIANT);
	}

	public Object calculate(LexicalUnit lu, Styles parentStyles, Styles styles, Element element) {
		if (isFontVariant(lu)) {
			return lu.getStringValue();
		} else {
			// not specified, "inherit", or some other value
			if (parentStyles != null) {
				return parentStyles.getFontStyle();
			} else {
				return CSS.NORMAL;
			}
		}

	}

	/**
	 * Returns true if the given lexical unit represents a font variant.
	 * 
	 * @param lu
	 *            LexicalUnit to check.
	 */
	public static boolean isFontVariant(LexicalUnit lu) {
		if (lu == null) {
			return false;
		} else if (lu.getLexicalUnitType() == LexicalUnit.SAC_IDENT) {
			String s = lu.getStringValue();
			return s.equals(CSS.NORMAL) || s.equals(CSS.SMALL_CAPS);
		} else {
			return false;
		}
	}

}

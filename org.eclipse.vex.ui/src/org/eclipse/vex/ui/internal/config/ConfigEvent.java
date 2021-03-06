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
package org.eclipse.vex.ui.internal.config;

import java.util.EventObject;

/**
 * Event indicating a change of configuration items.
 */
public class ConfigEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	/**
	 * Class constructor.
	 * 
	 * @param source
	 *            Source of the event.
	 */
	public ConfigEvent(final Object source) {
		super(source);
	}

}

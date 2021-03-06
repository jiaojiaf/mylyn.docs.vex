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
package org.eclipse.vex.ui.internal.handlers;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.ui.internal.swt.ContentAssist;

/**
 * Shows the content assist to add a new element ({@link InsertAssistant}).
 */
public class AddElementHandler extends AbstractVexWidgetHandler {

	@Override
	public void execute(final VexWidget widget) throws ExecutionException {
		ContentAssist.openAddElementsContentAssist(widget);
	}

}

/*******************************************************************************
 * Copyright (c) 2010, Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Florian Thienel - initial implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.widget;

import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.layout.FakeGraphics;

public class MockHostComponent implements IHostComponent {

	public boolean selectionChanged;

	public Graphics createDefaultGraphics() {
		return new FakeGraphics();
	}

	public void fireSelectionChanged() {
		selectionChanged = true;
	}

	public Rectangle getViewport() {
		return new Rectangle(0, 0, 0, 0);
	}

	public void invokeLater(final Runnable runnable) {
	}

	public void repaint() {
	}

	public void repaint(final int x, final int y, final int width, final int height) {
	}

	public void scrollTo(final int left, final int top) {
	}

	public void setPreferredSize(final int width, final int height) {
	}

}

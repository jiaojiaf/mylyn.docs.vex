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
package org.eclipse.vex.core.internal.layout;

import org.eclipse.vex.core.internal.core.Caret;
import org.eclipse.vex.core.internal.core.Color;
import org.eclipse.vex.core.internal.core.ColorResource;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;

/**
 * A caret drawn as a vertical line between characters.
 */
public class TextCaret extends Caret {

	private static final int LINE_WIDTH = 2;

	private final int height;

	/**
	 * Class constructor
	 * 
	 * @param x
	 *            x-coordinate of the caret
	 * @param y
	 *            y-coordinate of the top of the caret
	 * @param height
	 *            height of the caret
	 */
	public TextCaret(final int x, final int y, final int height) {
		super(x, y);
		this.height = height;
	}

	@Override
	public void draw(final Graphics g, final Color color) {
		final ColorResource newColor = g.createColor(color);
		final ColorResource oldColor = g.setColor(newColor);
		g.fillRect(getX(), getY(), LINE_WIDTH, height);
		g.setColor(oldColor);
		newColor.dispose();
	}

	@Override
	public Rectangle getBounds() {
		return new Rectangle(getX(), getY(), LINE_WIDTH, height);
	}
}

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

import org.eclipse.vex.core.provisional.dom.INode;

/**
 * Represents a line of text and inline images.
 */
public class LineBox extends CompositeInlineBox {

	private final INode node;
	private final InlineBox[] children;
	private InlineBox firstContentChild = null;
	private InlineBox lastContentChild = null;
	private int baseline;

	/**
	 * Class constructor.
	 * 
	 * @param context
	 *            LayoutContext for this layout.
	 * @param children
	 *            InlineBoxes that make up this line.
	 */
	public LineBox(final LayoutContext context, final INode node, final InlineBox[] children) {

		this.node = node;
		this.children = children;

		int height = 0;
		int x = 0;
		baseline = 0;
		for (final InlineBox child : children) {
			child.setX(x);
			child.setY(0); // TODO: do proper vertical alignment
			baseline = Math.max(baseline, child.getBaseline());
			x += child.getWidth();
			height = Math.max(height, child.getHeight());
			if (child.hasContent()) {
				if (firstContentChild == null) {
					firstContentChild = child;
				}
				lastContentChild = child;
			}
		}

		setHeight(height);
		setWidth(x);
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.InlineBox#getBaseline()
	 */
	public int getBaseline() {
		return baseline;
	}

	@Override
	public Box[] getChildren() {
		return children;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getNode()
	 */
	@Override
	public INode getNode() {
		return node;
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getEndOffset()
	 */
	@Override
	public int getEndOffset() {
		return lastContentChild.getEndOffset();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#getStartOffset()
	 */
	@Override
	public int getStartOffset() {
		return firstContentChild.getStartOffset();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.Box#hasContent()
	 */
	@Override
	public boolean hasContent() {
		return firstContentChild != null && firstContentChild.hasContent();
	}

	/**
	 * @see org.eclipse.vex.core.internal.layout.CompositeInlineBox#split(org.eclipse.vex.core.internal.layout.LayoutContext,
	 *      org.eclipse.vex.core.internal.layout.InlineBox[], org.eclipse.vex.core.internal.layout.InlineBox[])
	 */
	@Override
	public Pair split(final LayoutContext context, final InlineBox[] lefts, final InlineBox[] rights) {

		LineBox left = null;
		LineBox right = null;

		if (lefts.length > 0) {
			left = new LineBox(context, getNode(), lefts);
		}

		if (rights.length > 0) {
			right = new LineBox(context, getNode(), rights);
		}

		return new Pair(left, right);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final Box[] children = getChildren();
		final StringBuffer sb = new StringBuffer();
		for (final Box element2 : children) {
			sb.append(element2);
		}
		return sb.toString();
	}

	// ========================================================== PRIVATE

}

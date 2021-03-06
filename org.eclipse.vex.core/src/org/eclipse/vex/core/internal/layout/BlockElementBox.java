/*******************************************************************************
 * Copyright (c) 2004, 2013 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *     Mohamadou Nassourou - Bug 298912 - rudimentary support for images
 *     Carsten Hiesserich - changed pseudo element handling
 *******************************************************************************/
package org.eclipse.vex.core.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.VEXCorePlugin;
import org.eclipse.vex.core.internal.core.Drawable;
import org.eclipse.vex.core.internal.core.Graphics;
import org.eclipse.vex.core.internal.core.Rectangle;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.css.Styles;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;

/**
 * A block box corresponding to a DOM Element. Block boxes lay their children out stacked top to bottom. Block boxes
 * correspond to the <code>display: block;</code> CSS property.
 */
public class BlockElementBox extends AbstractBlockBox {

	/** hspace btn. list-item bullet and block, as fraction of font-size */
	static final float BULLET_SPACE = 0.5f;

	/** vspace btn. list-item bullet and baseine, as fraction of font-size */
	// private static final float BULLET_LIFT = 0.1f;
	/** number of boxes created since VM startup, for profiling */
	private static int boxCount;

	BlockBox beforeMarker;

	/**
	 * Class constructor. This box's children are not created here but in the first call to layout. Instead, we estimate
	 * the box's height here based on the given width.
	 * 
	 * @param context
	 *            LayoutContext used for this layout.
	 * @param parent
	 *            This box's parent box.
	 * @param node
	 *            Node to which this box corresponds.
	 */
	public BlockElementBox(final LayoutContext context, final BlockBox parent, final INode node) {
		super(context, parent, node);
	}

	/**
	 * Returns the number of boxes created since VM startup. Used for profiling.
	 */
	public static int getBoxCount() {
		return boxCount;
	}

	@Override
	public int getEndOffset() {
		return getNode().getEndOffset();
	}

	@Override
	public int getStartOffset() {
		return getNode().getStartOffset() + 1;
	}

	@Override
	public void paint(final LayoutContext context, final int x, final int y) {

		super.paint(context, x, y);

		if (beforeMarker != null) {
			beforeMarker.paint(context, x + beforeMarker.getX(), y + beforeMarker.getY());
		}
	}

	@Override
	protected int positionChildren(final LayoutContext context) {

		final int repaintStart = super.positionChildren(context);

		final Styles styles = context.getStyleSheet().getStyles(getNode());
		if (beforeMarker != null) {
			final int x = -beforeMarker.getWidth() - Math.round(BULLET_SPACE * styles.getFontSize());
			int y = getFirstLineTop(context);
			final LineBox firstLine = getFirstLine();
			if (firstLine != null) {
				y += firstLine.getBaseline() - beforeMarker.getFirstLine().getBaseline();
			}

			beforeMarker.setX(x);
			beforeMarker.setY(y);
		}

		return repaintStart;
	}

	@Override
	public String toString() {
		return "BlockElementBox: <" + getNode() + ">" + "[x=" + getX() + ",y=" + getY() + ",width=" + getWidth() + ",height=" + getHeight() + "]";
	}

	// ===================================================== PRIVATE

	/**
	 * Lays out the children as vertically stacked blocks. Runs of text and inline elements are wrapped in
	 * DummyBlockBox's.
	 */
	@Override
	public List<Box> createChildren(final LayoutContext context) {
		long start = 0;
		if (VEXCorePlugin.getInstance().isDebugging()) {
			start = System.currentTimeMillis();
		}

		final INode node = getNode();
		final int width = getWidth();

		final List<Box> childList = new ArrayList<Box>();

		final StyleSheet styleSheet = context.getStyleSheet();

		// :before content
		List<InlineBox> beforeInlines = null;
		final IElement before = styleSheet.getPseudoElement(node, CSS.PSEUDO_BEFORE, true);
		if (before != null) {
			final Styles beforeStyles = styleSheet.getStyles(before);
			if (beforeStyles.getDisplay().equals(CSS.INLINE)) {
				beforeInlines = new ArrayList<InlineBox>();
				beforeInlines.addAll(LayoutUtils.createGeneratedInlines(context, before));
			} else {
				childList.add(new BlockPseudoElementBox(context, before, this, width));
			}
		}

		// background image
		final Styles styles = context.getStyleSheet().getStyles(node);
		if (styles.hasBackgroundImage() && !styles.getDisplay().equalsIgnoreCase(CSS.NONE)) {
			final InlineBox imageBox = ImageBox.create(node, context, getWidth());
			if (imageBox != null) {
				if (beforeInlines == null) {
					beforeInlines = new ArrayList<InlineBox>();
				}
				beforeInlines.add(imageBox);
			}
		}

		// :after content
		Box afterBlock = null;
		List<InlineBox> afterInlines = null;

		final IElement after = styleSheet.getPseudoElement(node, CSS.PSEUDO_AFTER, true);
		if (after != null) {
			final Styles afterStyles = styleSheet.getStyles(after);
			if (afterStyles.getDisplay().equals(CSS.INLINE)) {
				afterInlines = new ArrayList<InlineBox>();
				afterInlines.addAll(LayoutUtils.createGeneratedInlines(context, after));
			} else {
				afterBlock = new BlockPseudoElementBox(context, after, this, width);
			}
		}

		final int startOffset = node.getStartOffset() + 1;
		final int endOffset = node.getEndOffset();
		final List<Box> blockBoxes = createBlockBoxes(context, startOffset, endOffset, width, beforeInlines, afterInlines);
		childList.addAll(blockBoxes);

		if (afterBlock != null) {
			childList.add(afterBlock);
		}

		if (styles.getDisplay().equals(CSS.LIST_ITEM) && !styles.getListStyleType().equals(CSS.NONE)) {
			createListMarker(context);
		}

		if (VEXCorePlugin.getInstance().isDebugging()) {
			final long end = System.currentTimeMillis();
			if (end - start > 10) {
				System.out.println("BEB.layout for " + getNode() + " took " + (end - start) + "ms");
			}
		}

		return childList;
	}

	/**
	 * Creates a marker box for this primary box and puts it in the beforeMarker field.
	 */
	private void createListMarker(final LayoutContext context) {

		final Styles styles = context.getStyleSheet().getStyles(getNode());

		InlineBox markerInline;
		final String type = styles.getListStyleType();
		if (type.equals(CSS.NONE)) {
			return;
		} else if (type.equals(CSS.CIRCLE)) {
			markerInline = createCircleBullet(getNode(), styles);
		} else if (type.equals(CSS.SQUARE)) {
			markerInline = createSquareBullet(getNode(), styles);
		} else if (isEnumeratedListStyleType(type)) {
			final String item = getItemNumberString(type);
			markerInline = new StaticTextBox(context, getNode(), item + ".");
		} else {
			markerInline = createDiscBullet(getNode(), styles);
		}

		beforeMarker = ParagraphBox.create(context, getNode(), new InlineBox[] { markerInline }, Integer.MAX_VALUE);

	}

	/**
	 * Returns a Drawable that draws a circle-style list item bullet.
	 */
	private static InlineBox createCircleBullet(final INode node, final Styles styles) {
		final int size = Math.round(0.5f * styles.getFontSize());
		final int lift = Math.round(0.1f * styles.getFontSize());
		final Drawable drawable = new Drawable() {
			public void draw(final Graphics g, final int x, final int y) {
				g.setLineStyle(Graphics.LINE_SOLID);
				g.setLineWidth(1);
				g.drawOval(x, y - size - lift, size, size);
			}

			public Rectangle getBounds() {
				return new Rectangle(0, -size - lift, size, size);
			}
		};
		return new DrawableBox(drawable, node);
	}

	/**
	 * Returns a Drawable that draws a disc-style list item bullet.
	 */
	private static InlineBox createDiscBullet(final INode node, final Styles styles) {
		final int size = Math.round(0.5f * styles.getFontSize());
		final int lift = Math.round(0.1f * styles.getFontSize());
		final Drawable drawable = new Drawable() {
			public void draw(final Graphics g, final int x, final int y) {
				g.fillOval(x, y - size - lift, size, size);
			}

			public Rectangle getBounds() {
				return new Rectangle(0, -size - lift, size, size);
			}
		};
		return new DrawableBox(drawable, node);
	}

	/**
	 * Returns a Drawable that draws a square-style list item bullet.
	 */
	private static InlineBox createSquareBullet(final INode node, final Styles styles) {
		final int size = Math.round(0.5f * styles.getFontSize());
		final int lift = Math.round(0.1f * styles.getFontSize());
		final Drawable drawable = new Drawable() {
			public void draw(final Graphics g, final int x, final int y) {
				g.setLineStyle(Graphics.LINE_SOLID);
				g.setLineWidth(1);
				g.drawRect(x, y - size - lift, size, size);
			}

			public Rectangle getBounds() {
				return new Rectangle(0, -size - lift, size, size);
			}
		};
		return new DrawableBox(drawable, node);
	}

	/**
	 * Returns the vertical distance from the top of this box to the top of its first line.
	 */
	int getFirstLineTop(final LayoutContext context) {
		final Styles styles = context.getStyleSheet().getStyles(getNode());
		final int top = styles.getBorderTopWidth() + styles.getPaddingTop().get(0);
		final Box[] children = getChildren();
		if (children != null && children.length > 0 && children[0] instanceof BlockElementBox) {
			return top + ((BlockElementBox) children[0]).getFirstLineTop(context);
		} else {
			return top;
		}
	}

	/**
	 * Returns the item number of this box. The item number indicates the ordinal number of the corresponding element
	 * amongst its siblings starting with 1.
	 */
	private int getItemNumber() {
		final INode node = getNode();
		final IParent parent = node.getParent();

		if (parent == null) {
			return 1;
		}

		int item = 1;
		for (final INode child : parent.children()) {
			if (child == node) {
				return item;
			}
			if (child.isKindOf(node)) {
				item++;
			}
		}

		throw new IllegalStateException();
	}

	private String getItemNumberString(final String style) {
		final int item = getItemNumber();
		if (style.equals(CSS.DECIMAL_LEADING_ZERO)) {
			if (item < 10) {
				return "0" + Integer.toString(item);
			} else {
				return Integer.toString(item);
			}
		} else if (style.equals(CSS.LOWER_ALPHA) || style.equals(CSS.LOWER_LATIN)) {
			return getAlpha(item);
		} else if (style.equals(CSS.LOWER_ROMAN)) {
			return getRoman(item);
		} else if (style.equals(CSS.UPPER_ALPHA) || style.equals(CSS.UPPER_LATIN)) {
			return getAlpha(item).toUpperCase();
		} else if (style.equals(CSS.UPPER_ROMAN)) {
			return getRoman(item).toUpperCase();
		} else {
			return Integer.toString(item);
		}
	}

	private String getAlpha(final int n) {
		final String alpha = "abcdefghijklmnopqrstuvwxyz";
		return String.valueOf(alpha.charAt((n - 1) % 26));
	}

	private String getRoman(final int n) {
		final String[] ones = { "", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix" };
		final String[] tens = { "", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc" };
		final String[] hundreds = { "", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm" };
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n / 1000; i++) {
			sb.append("m");
		}
		sb.append(hundreds[n / 100 % 10]);
		sb.append(tens[n / 10 % 10]);
		sb.append(ones[n % 10]);
		return sb.toString();
	}

	private static boolean isEnumeratedListStyleType(final String s) {
		return s.equals(CSS.ARMENIAN) || s.equals(CSS.CJK_IDEOGRAPHIC) || s.equals(CSS.DECIMAL) || s.equals(CSS.DECIMAL_LEADING_ZERO) || s.equals(CSS.GEORGIAN) || s.equals(CSS.HEBREW)
				|| s.equals(CSS.HIRAGANA) || s.equals(CSS.HIRAGANA_IROHA) || s.equals(CSS.KATAKANA) || s.equals(CSS.KATAKANA_IROHA) || s.equals(CSS.LOWER_ALPHA) || s.equals(CSS.LOWER_GREEK)
				|| s.equals(CSS.LOWER_LATIN) || s.equals(CSS.LOWER_ROMAN) || s.equals(CSS.UPPER_ALPHA) || s.equals(CSS.UPPER_LATIN) || s.equals(CSS.UPPER_ROMAN);
	}

}

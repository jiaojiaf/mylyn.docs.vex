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
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IContent;
import org.eclipse.vex.core.provisional.dom.IPosition;
import org.junit.Test;

/**
 * Test the GapContent class
 */
public class GapContentTest extends ContentTest {

	@Override
	protected IContent createContent() {
		return new GapContent(100);
	}

	@Test
	public void useChar0AsElementMarker() throws Exception {
		final GapContent elementMarkerContent = new GapContent(4);
		elementMarkerContent.insertTagMarker(0);
		elementMarkerContent.insertTagMarker(0);

		final GapContent stringContent = new GapContent(4);
		stringContent.insertText(0, "\0\0");

		assertEquals(stringContent.length(), elementMarkerContent.length());
		assertEquals(stringContent.getText(stringContent.getRange()), elementMarkerContent.getText(elementMarkerContent.getRange()));
	}

	@Test
	public void givenAnOffset_whenInvokedMultipleTimes_shouldNotCreateMultiplePositionInstances() throws Exception {
		final GapContent gapContent = new GapContent(4);
		gapContent.insertTagMarker(0);
		final IPosition firstPosition = gapContent.createPosition(0);
		assertEquals(1, gapContent.getPositionCount());
		final IPosition secondPosition = gapContent.createPosition(0);
		assertEquals(1, gapContent.getPositionCount());
		assertSame(firstPosition, secondPosition);
	}

	@Test
	public void whenDeletingRange_shouldRemovePosistionsInRange() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertTagMarker(0);
		content.insertText(0, "Hello");
		content.insertTagMarker(0);
		content.createPosition(0);
		content.createPosition(1);
		content.createPosition(5);
		content.createPosition(6);

		content.remove(new ContentRange(1, 5));

		assertEquals(2, content.getPositionCount());
	}

	@Test
	public void testGapContent() throws Exception {
		//
		// a b (gap) c d
		// | | | | |
		// 0 1 2 3 4
		//

		final GapContent content = new GapContent(2);
		assertEquals(0, content.length());
		content.insertText(0, "a");
		assertEquals(1, content.length());
		content.insertText(1, "d");
		assertEquals(2, content.length());
		content.insertText(1, "c");
		assertEquals(3, content.length());
		content.insertText(1, "b");
		assertEquals(4, content.length());

		final IPosition pa = content.createPosition(0);
		final IPosition pb = content.createPosition(1);
		final IPosition pc = content.createPosition(2);
		final IPosition pd = content.createPosition(3);
		final IPosition pe = content.createPosition(4);

		try {
			content.getText(new ContentRange(-1, 0));
			fail("expected exception");
		} catch (final AssertionFailedException ex) {
		}

		try {
			content.getText(new ContentRange(4, 4));
			fail("expected exception");
		} catch (final AssertionFailedException ex) {
		}

		try {
			content.getText(new ContentRange(0, -1));
			fail("expected exception");
		} catch (final AssertionFailedException ex) {
		}

		try {
			content.getText(new ContentRange(0, 4));
			fail("expected exception");
		} catch (final AssertionFailedException ex) {
		}

		try {
			content.createPosition(-1);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		try {
			content.createPosition(5);
			fail("expected exception");
		} catch (final IllegalArgumentException ex) {
		}

		assertEquals("a", content.getText(new ContentRange(0, 0)));
		assertEquals("b", content.getText(new ContentRange(1, 1)));
		assertEquals("c", content.getText(new ContentRange(2, 2)));
		assertEquals("d", content.getText(new ContentRange(3, 3)));

		assertEquals("ab", content.getText(new ContentRange(0, 1)));
		assertEquals("bc", content.getText(new ContentRange(1, 2)));
		assertEquals("cd", content.getText(new ContentRange(2, 3)));

		assertEquals("abc", content.getText(new ContentRange(0, 2)));
		assertEquals("bcd", content.getText(new ContentRange(1, 3)));

		assertEquals("abcd", content.getText(new ContentRange(0, 3)));

		//
		// a b x (gap) y c d
		// | | | | | | |
		// 0 1 2 3 4 5 6
		//
		content.insertText(2, "y");
		assertEquals(5, content.length());
		content.insertText(2, "x");
		assertEquals(6, content.length());

		assertEquals(0, pa.getOffset());
		assertEquals(1, pb.getOffset());
		assertEquals(4, pc.getOffset());
		assertEquals(5, pd.getOffset());
		assertEquals(6, pe.getOffset());

		final IPosition px = content.createPosition(2);
		final IPosition py = content.createPosition(3);

		assertEquals("xy", content.getText(new ContentRange(2, 3)));

		content.remove(new ContentRange(2, 3));

		assertEquals(4, content.length());

		assertEquals(0, pa.getOffset());
		assertEquals(1, pb.getOffset());

		assertFalse(px.isValid());
		assertFalse(py.isValid());

		assertEquals(2, pc.getOffset());
		assertEquals(3, pd.getOffset());
		assertEquals(4, pe.getOffset());

		assertEquals("a", content.getText(new ContentRange(0, 0)));
		assertEquals("b", content.getText(new ContentRange(1, 1)));
		assertEquals("c", content.getText(new ContentRange(2, 2)));
		assertEquals("d", content.getText(new ContentRange(3, 3)));

		assertEquals("ab", content.getText(new ContentRange(0, 1)));
		assertEquals("bc", content.getText(new ContentRange(1, 2)));
		assertEquals("cd", content.getText(new ContentRange(2, 3)));

		assertEquals("abc", content.getText(new ContentRange(0, 2)));
		assertEquals("bcd", content.getText(new ContentRange(1, 3)));

		assertEquals("abcd", content.getText(new ContentRange(0, 3)));

	}

}

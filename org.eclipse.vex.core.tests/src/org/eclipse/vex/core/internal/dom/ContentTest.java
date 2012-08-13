/*******************************************************************************
 * Copyright (c) 2012 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public abstract class ContentTest {

	private Content content;

	@Before
	public void setUp() throws Exception {
		content = createContent();
	}

	protected abstract Content createContent();

	@Test
	public void insertText() throws Exception {
		final String text = "Hello World";

		assertEquals(0, content.getLength());
		content.insertText(0, text);
		assertEquals(text.length(), content.getLength());
	}

	@Test
	public void insertElementMarker() throws Exception {
		content.insertElementMarker(0);
		assertEquals(1, content.getLength());
		assertTrue(content.isElementMarker(0));
	}

	@Test
	public void mixTextWithElementMarkers() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		content.insertElementMarker(5);
		assertEquals("Each element marker should count 1 in the length calculation.", text.length() + 1, content.getLength());
	}

	@Test
	public void shouldReturnPlainTextWithoutElementMarkers() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		content.insertElementMarker(5);
		assertEquals(text, content.getText());
	}

	@Test
	public void shouldReturnAPartialCopy() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		final Content partialCopy = content.getContent(3, 5);
		assertEquals("lo Wo", partialCopy.getText());

		// make shure, it is a copy
		content.insertText(6, "New ");
		assertEquals("Hello New World", content.getText());
		assertEquals("lo Wo", partialCopy.getText());
	}

	@Test
	public void shouldReturnAFullCopy() throws Exception {
		final String text = "Hello World";
		content.insertText(0, text);
		final Content fullCopy = content.getContent();
		assertEquals(text, fullCopy.getText());

		// make shure, it is a copy
		content.insertText(6, "New ");
		assertEquals("Hello New World", content.getText());
		assertEquals(text, fullCopy.getText());
	}

	@Test
	public void insertContent() throws Exception {
		content.insertText(0, "Hello World");
		final Content other = createContent();
		other.insertElementMarker(0);
		other.insertText(1, "New");
		other.insertElementMarker(4);

		content.insertContent(6, other);
		assertEquals(16, content.getLength());
		assertEquals("Hello NewWorld", content.getText());
		assertTrue(content.isElementMarker(6));
		assertTrue(content.isElementMarker(10));
	}

	@Test
	public void removeAndInsertContent() throws Exception {
		content.insertText(0, "Hello Cut Out World");
		content.insertElementMarker(6);
		content.insertElementMarker(14);

		content.remove(7, 7);
		assertTrue(content.isElementMarker(6));
		assertTrue(content.isElementMarker(7));

		content.remove(6, 2);
		assertEquals("Hello  World", content.getText());

		content.insertText(6, "Cut Out");
		assertEquals("Hello Cut Out World", content.getText());
	}
}

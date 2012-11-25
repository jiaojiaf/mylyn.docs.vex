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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/**
 * @author Florian Thienel
 */
public class DeepCopyTest {

	@Test
	public void givenOneElement_shouldCopyElement() throws Exception {
		final Element element = new Element("element");

		final DeepCopy deepCopy = new DeepCopy(element);
		final List<Node> copiedNodes = deepCopy.getNodes();

		assertEquals(1, copiedNodes.size());
		assertTrue("copy should be of same type: " + copiedNodes.get(0), copiedNodes.get(0) instanceof Element);
		assertNotSame(element, copiedNodes.get(0));
	}

	@Test
	public void givenOneElementWithContent_shouldCopyAssociatedContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		final Element element = new Element("element");
		element.associate(content, new Range(0, 1));
		content.insertText(1, "Hello World");

		final DeepCopy deepCopy = new DeepCopy(element);
		final Content copiedContent = deepCopy.getContent();

		assertNotNull(copiedContent);
		assertNotSame(content, copiedContent);
		assertEquals(content.length(), copiedContent.length());
		assertEquals(content.getRawText(), copiedContent.getRawText());
	}

	@Test
	public void givenOneElementWithHugeContent_shouldOnlyCopyRelevantContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		final Element element = new Element("element");
		element.associate(content, new Range(0, 1));
		content.insertText(2, "World");
		content.insertText(1, " New ");
		content.insertText(0, "Hello");

		final DeepCopy deepCopy = new DeepCopy(element);
		final Content copiedContent = deepCopy.getContent();

		assertEquals(7, copiedContent.length());
		assertEquals(" New ", copiedContent.getText());
	}

	@Test
	public void givenOneElementWithHugeContent_shouldAssociateCopiedElementWithCopiedContent() throws Exception {
		final GapContent content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		final Element element = new Element("element");
		element.associate(content, new Range(0, 1));
		content.insertText(2, "World");
		content.insertText(1, " New ");
		content.insertText(0, "Hello");

		final DeepCopy deepCopy = new DeepCopy(element);
		final Element copiedElement = (Element) deepCopy.getNodes().get(0);

		assertTrue(copiedElement.isAssociated());
		assertEquals(element.getText(), copiedElement.getText());
	}

	@Test
	public void givenOneParentWithTwoChildren_shouldCopyParentAndChildren() throws Exception {
		final Element parent = new Element("parent");
		final Element child1 = new Element("child");
		child1.setAttribute("order", "1");
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.setAttribute("order", "2");
		parent.addChild(child2);

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final List<Node> copiedChildNodes = copiedParent.getChildNodes();

		assertEquals(2, copiedChildNodes.size());
		assertEquals("1", ((Element) copiedChildNodes.get(0)).getAttribute("order").getValue());
		assertEquals("2", ((Element) copiedChildNodes.get(1)).getAttribute("order").getValue());
	}

	@Test
	public void givenOneParentWithTwoChildrenAndContent_shouldCopyParentChildrenAndContent() throws Exception {
		final Content content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());
		final Element child1 = new Element("child");
		child1.associate(content, new Range(1, 2));
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.associate(content, new Range(3, 4));
		parent.addChild(child2);
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getStartOffset(), " New ");
		content.insertText(child2.getEndOffset(), "World");

		final DeepCopy deepCopy = new DeepCopy(parent);
		final Element copiedParent = (Element) deepCopy.getNodes().get(0);
		final List<Node> copiedChildNodes = copiedParent.getChildNodes();

		assertEquals(3, copiedChildNodes.size());
		assertTrue(copiedChildNodes.get(0).isAssociated());
		assertTrue(copiedChildNodes.get(0) instanceof Element);
		assertEquals("Hello", copiedChildNodes.get(0).getText());
		assertTrue(copiedChildNodes.get(1).isAssociated());
		assertTrue(copiedChildNodes.get(1) instanceof Text);
		assertEquals(" New ", copiedChildNodes.get(1).getText());
		assertTrue(copiedChildNodes.get(2).isAssociated());
		assertTrue(copiedChildNodes.get(2) instanceof Element);
		assertEquals("World", copiedChildNodes.get(2).getText());
	}

	@Test
	public void givenOneParentWithTwoChildrenInHugeContent_shouldCopyOnlyRelevantContent() throws Exception {
		final Content content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());
		final Element child1 = new Element("child");
		child1.associate(content, new Range(1, 2));
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.associate(content, new Range(3, 4));
		parent.addChild(child2);
		content.insertText(parent.getStartOffset(), "Prefix Content");
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getStartOffset(), " New ");
		content.insertText(child2.getEndOffset(), "World");
		content.insertText(parent.getEndOffset() + 1, "Suffix Content");

		final DeepCopy deepCopy = new DeepCopy(parent);

		assertEquals(21, deepCopy.getContent().length());
	}

	@Test
	public void givenOneParentWithTwoChildrenAndContent_whenGivenRange_shouldOnlyCopyChildrenAndContentWithinRange() throws Exception {
		final Content content = new GapContent(10);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		content.insertElementMarker(0);
		final Element parent = new Element("parent");
		parent.associate(content, content.getRange());
		final Element child1 = new Element("child");
		child1.associate(content, new Range(1, 2));
		parent.addChild(child1);
		final Element child2 = new Element("child");
		child2.associate(content, new Range(3, 4));
		parent.addChild(child2);
		content.insertText(child1.getStartOffset(), "Prefix Content");
		content.insertText(child1.getEndOffset(), "Hello");
		content.insertText(child2.getStartOffset(), " New ");
		content.insertText(child2.getEndOffset(), "World");
		content.insertText(parent.getEndOffset(), "Suffix Content");

		final DeepCopy deepCopy = new DeepCopy(parent, new Range(8, 39));

		assertEquals(32, deepCopy.getContent().length());
		assertEquals("Content\0Hello\0 New \0World\0Suffix", deepCopy.getContent().getRawText());
		assertEquals(2, deepCopy.getNodes().size());

	}
}
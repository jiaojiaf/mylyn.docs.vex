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
package org.eclipse.vex.core.internal.css;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.junit.Test;

/**
 * Test rule matching.
 */
public class RuleTest {

	@Test
	public void testRuleMatching() throws Exception {
		final URL url = RuleTest.class.getResource("testRules.css");
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet ss = reader.read(url);
		final List<Rule> rules = ss.getRules();

		final IDocument doc = new Document(new QualifiedName(null, "a"));
		final IElement a = doc.getRootElement();
		final IElement b = doc.insertElement(2, new QualifiedName(null, "b"));
		final IElement c = doc.insertElement(3, new QualifiedName(null, "c"));
		final IElement d = doc.insertElement(4, new QualifiedName(null, "d"));
		final IElement e = doc.insertElement(5, new QualifiedName(null, "e"));
		final IElement f = doc.insertElement(6, new QualifiedName(null, "f"));

		b.setAttribute("color", "blue");
		c.setAttribute("color", "blue red");
		d.setAttribute("color", "gree blue red");
		e.setAttribute("color", "red blue");
		f.setAttribute("color", "bluered");

		// /* 0 */ c { }
		Rule rule = rules.get(0);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertTrue(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 1 */ b c { }
		rule = rules.get(1);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertTrue(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 2 */ b d { }
		rule = rules.get(2);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertTrue(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 3 */ other b c { }
		rule = rules.get(3);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 4 */ other b d { }
		rule = rules.get(4);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 5 */ a c e { }
		rule = rules.get(5);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertTrue(rule.matches(e));

		// /* 6 */ c a e { }
		rule = rules.get(6);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 7 */ * { }
		rule = rules.get(7);
		assertTrue(rule.matches(a));
		assertTrue(rule.matches(b));
		assertTrue(rule.matches(c));
		assertTrue(rule.matches(d));
		assertTrue(rule.matches(e));

		// /* 8 */ *[color]
		rule = rules.get(8);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertTrue(rule.matches(c));
		assertTrue(rule.matches(d));
		assertTrue(rule.matches(e));

		// /* 9 */ a[color]
		rule = rules.get(9);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 10 */ b[color]
		rule = rules.get(10);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 11 */ c[color]
		rule = rules.get(11);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertTrue(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 12 */ d[color]
		rule = rules.get(12);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertTrue(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 13 */ *[color=blue]
		rule = rules.get(13);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 14 */ a[color=blue]
		rule = rules.get(14);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 15 */ b[color=blue]
		rule = rules.get(15);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 16 */ b[color='blue']
		rule = rules.get(16);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 17 */ b[color="blue"]
		rule = rules.get(17);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 18 */ c[color=blue]
		rule = rules.get(18);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 19 */ a * { }
		rule = rules.get(19);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertTrue(rule.matches(c));
		assertTrue(rule.matches(d));
		assertTrue(rule.matches(e));

		// /* 20 */ a > * { }
		rule = rules.get(20);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 21 */ a *[color] { }
		rule = rules.get(21);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertTrue(rule.matches(c));
		assertTrue(rule.matches(d));
		assertTrue(rule.matches(e));

		// /* 22 */ a > *[color] { }
		rule = rules.get(22);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertFalse(rule.matches(c));
		assertFalse(rule.matches(d));
		assertFalse(rule.matches(e));

		// /* 23 */ *[color~=blue] { }
		rule = rules.get(23);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertTrue(rule.matches(c));
		assertTrue(rule.matches(d));
		assertTrue(rule.matches(e));
		assertFalse(rule.matches(f));

		//
		// Rules that test class conditions
		//

		b.setAttribute("class", "foo");
		c.setAttribute("class", "foo bar");
		d.setAttribute("class", "bar");

		// /* 24 */ .foo { }
		rule = rules.get(24);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(b));
		assertTrue(rule.matches(c));
		assertFalse(rule.matches(d));

		// /* 25 */ .foo.bar { }
		rule = rules.get(25);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(b));
		assertTrue(rule.matches(c));
		assertFalse(rule.matches(d));
	}

	@Test
	public void testWithNamespace() throws Exception {
		final URL url = RuleTest.class.getResource("testRules.css");
		final StyleSheetReader reader = new StyleSheetReader();
		final StyleSheet ss = reader.read(url);
		final List<Rule> rules = ss.getRules();

		final IDocument doc = new Document(new QualifiedName(null, "a"));
		final IElement a = doc.getRootElement();
		final IElement ns = doc.insertElement(2, new QualifiedName("http://namespace/uri", "b"));

		ns.setAttribute("color", "blue");

		// /* 0 */ c { }
		Rule rule = rules.get(0);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 1 */ b c { }
		rule = rules.get(1);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 2 */ b d { }
		rule = rules.get(2);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 3 */ other b c { }
		rule = rules.get(3);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 4 */ other b d { }
		rule = rules.get(4);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 5 */ a c e { }
		rule = rules.get(5);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 6 */ c a e { }
		rule = rules.get(6);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 7 */ * { }
		rule = rules.get(7);
		assertTrue(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 8 */ *[color]
		rule = rules.get(8);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 9 */ a[color]
		rule = rules.get(9);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 10 */ b[color]
		rule = rules.get(10);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 11 */ c[color]
		rule = rules.get(11);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 12 */ d[color]
		rule = rules.get(12);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 13 */ *[color=blue]
		rule = rules.get(13);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 14 */ a[color=blue]
		rule = rules.get(14);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 15 */ b[color=blue]
		rule = rules.get(15);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 16 */ b[color='blue']
		rule = rules.get(16);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 17 */ b[color="blue"]
		rule = rules.get(17);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 18 */ c[color=blue]
		rule = rules.get(18);
		assertFalse(rule.matches(a));
		assertFalse(rule.matches(ns));

		// /* 19 */ a * { }
		rule = rules.get(19);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 20 */ a > * { }
		rule = rules.get(20);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 21 */ a *[color] { }
		rule = rules.get(21);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 22 */ a > *[color] { }
		rule = rules.get(22);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

		// /* 23 */ *[color~=blue] { }
		rule = rules.get(23);
		assertFalse(rule.matches(a));
		assertTrue(rule.matches(ns));

	}
}

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
 *     Florian Thienel - namespace handling (bug 253753), refactoring to full fledged DOM
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.vex.core.XML;
import org.eclipse.vex.core.internal.core.QualifiedNameComparator;
import org.eclipse.vex.core.provisional.dom.AttributeChangeEvent;
import org.eclipse.vex.core.provisional.dom.DocumentValidationException;
import org.eclipse.vex.core.provisional.dom.Filters;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IAxis;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitor;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.NamespaceDeclarationChangeEvent;

public class Element extends Parent implements IElement {

	private final QualifiedName name;

	private final Map<QualifiedName, Attribute> attributes = new HashMap<QualifiedName, Attribute>();
	private final Map<String, String> namespaceDeclarations = new HashMap<String, String>();

	public Element(final String localName) {
		this(new QualifiedName(null, localName));
	}

	public Element(final QualifiedName qualifiedName) {
		name = qualifiedName;
	}

	/*
	 * Node
	 */

	@Override
	public String getBaseURI() {
		final IAttribute baseAttribute = getAttribute(XML.BASE_ATTRIBUTE);
		if (baseAttribute != null) {
			return baseAttribute.getValue();
		}
		return super.getBaseURI();
	}

	public void setBaseURI(final String baseURI) {
		setAttribute(XML.BASE_ATTRIBUTE, baseURI);
	}

	public boolean isKindOf(final INode other) {
		if (!(other instanceof IElement)) {
			return false;
		}
		return getQualifiedName().equals(((IElement) other).getQualifiedName());
	}

	public void accept(final INodeVisitor visitor) {
		visitor.visit(this);
	}

	public <T> T accept(final INodeVisitorWithResult<T> visitor) {
		return visitor.visit(this);
	}

	/*
	 * Element Name
	 */

	public QualifiedName getQualifiedName() {
		return name;
	}

	public String getLocalName() {
		return name.getLocalName();
	}

	public String getPrefix() {
		return getNamespacePrefix(name.getQualifier());
	}

	public String getPrefixedName() {
		final String prefix = getPrefix();
		if (prefix == null) {
			return getLocalName();
		}
		return prefix + ":" + getLocalName();
	}

	public QualifiedName qualify(final String localName) {
		return new QualifiedName(name.getQualifier(), localName);
	}

	/*
	 * Attributes
	 */

	public IAttribute getAttribute(final String localName) {
		return getAttribute(new QualifiedName(null, localName));
	}

	public IAttribute getAttribute(final QualifiedName name) {
		return attributes.get(name);
	}

	public String getAttributeValue(final String localName) {
		return getAttributeValue(new QualifiedName(null, localName));
	}

	public String getAttributeValue(final QualifiedName name) {
		final IAttribute attribute = getAttribute(name);
		if (attribute == null || "".equals(attribute.getValue().trim())) {
			return null;
		}
		return attribute.getValue();
	}

	public boolean canRemoveAttribute(final String localName) {
		return canRemoveAttribute(qualify(localName));
	}

	public boolean canRemoveAttribute(final QualifiedName name) {
		return true; // TODO implement validation for attribute values
	}

	public void removeAttribute(final String localName) throws DocumentValidationException {
		removeAttribute(new QualifiedName(null, localName));
	}

	public void removeAttribute(final QualifiedName name) throws DocumentValidationException {
		final IAttribute attribute = this.getAttribute(name);
		if (attribute == null) {
			return;
		}
		final String oldValue = attribute.getValue();
		final String newValue = null;
		if (oldValue != null) {
			attributes.remove(name);
		}

		final Document document = getDocument();
		if (document == null) {
			return;
		}

		document.fireAttributeChanged(new AttributeChangeEvent(document, this, name, oldValue, newValue));
	}

	public boolean canSetAttribute(final String localName, final String value) {
		return canSetAttribute(qualify(localName), value);
	}

	public boolean canSetAttribute(final QualifiedName name, final String value) {
		return true; // TODO implement validation for attribute values
	}

	public void setAttribute(final String localName, final String value) throws DocumentValidationException {
		setAttribute(new QualifiedName(null, localName), value);
	}

	public void setAttribute(final QualifiedName name, final String value) throws DocumentValidationException {
		final IAttribute oldAttribute = attributes.get(name);
		final String oldValue = oldAttribute != null ? oldAttribute.getValue() : null;

		if (value == null && oldValue == null) {
			return;
		}

		if (value == null) {
			this.removeAttribute(name);
		} else {
			if (value.equals(oldValue)) {
				return;
			} else {
				final Attribute newAttribute = new Attribute(this, name, value);
				attributes.put(name, newAttribute);

				final Document document = getDocument();
				if (document == null) {
					return;
				}

				document.fireAttributeChanged(new AttributeChangeEvent(document, this, name, oldValue, value));
			}
		}
	}

	public Collection<IAttribute> getAttributes() {
		final ArrayList<IAttribute> result = new ArrayList<IAttribute>(attributes.values());
		Collections.sort(result);
		return Collections.unmodifiableCollection(result);
	}

	public Collection<QualifiedName> getAttributeNames() {
		final ArrayList<QualifiedName> result = new ArrayList<QualifiedName>();
		for (final IAttribute attribute : attributes.values()) {
			result.add(attribute.getQualifiedName());
		}
		Collections.sort(result, new QualifiedNameComparator());
		return result;
	}

	/*
	 * Element Structure
	 */

	public Element getParentElement() {
		for (final INode ancestor : ancestors()) {
			if (ancestor instanceof Element) {
				return (Element) ancestor;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public IAxis<Element> childElements() {
		return (IAxis<Element>) children().withoutText().matching(Filters.elements());
	}

	/*
	 * Namespaces
	 */

	public String getNamespaceURI(final String namespacePrefix) {
		if (namespaceDeclarations.containsKey(namespacePrefix)) {
			return namespaceDeclarations.get(namespacePrefix);
		}
		final IElement parent = getParentElement();
		if (parent != null) {
			return parent.getNamespaceURI(namespacePrefix);
		}
		return null;
	}

	public String getDefaultNamespaceURI() {
		return getNamespaceURI(null);
	}

	public String getDeclaredDefaultNamespaceURI() {
		return namespaceDeclarations.get(null);
	}

	public String getNamespacePrefix(final String namespaceURI) {
		if (namespaceURI == null) {
			return null;
		}
		if (Namespace.XML_NAMESPACE_URI.equals(namespaceURI)) {
			return Namespace.XML_NAMESPACE_PREFIX;
		}
		if (Namespace.XMLNS_NAMESPACE_URI.equals(namespaceURI)) {
			return Namespace.XMLNS_NAMESPACE_PREFIX;
		}
		for (final Entry<String, String> entry : namespaceDeclarations.entrySet()) {
			if (entry.getValue().equals(namespaceURI)) {
				return entry.getKey();
			}
		}
		final IElement parent = getParentElement();
		if (parent != null) {
			final String parentPrefix = parent.getNamespacePrefix(namespaceURI);
			if (!namespaceDeclarations.containsKey(parentPrefix)) {
				return parentPrefix;
			}
		}
		return null;
	}

	public Collection<String> getDeclaredNamespacePrefixes() {
		final ArrayList<String> result = new ArrayList<String>();
		for (final String prefix : namespaceDeclarations.keySet()) {
			if (prefix != null) {
				result.add(prefix);
			}
		}
		Collections.sort(result);
		return result;
	}

	public Collection<String> getNamespacePrefixes() {
		final HashSet<String> result = new HashSet<String>();
		result.addAll(getDeclaredNamespacePrefixes());
		final IElement parent = getParentElement();
		if (parent != null) {
			result.addAll(parent.getNamespacePrefixes());
		}
		return result;
	}

	public void declareNamespace(final String namespacePrefix, final String namespaceURI) {
		if (namespaceURI == null || "".equals(namespaceURI.trim())) {
			return;
		}
		final String oldNamespaceURI = namespaceDeclarations.put(namespacePrefix, namespaceURI);
		final Document document = getDocument();
		if (document == null) {
			return;
		}

		if (namespaceURI.equals(oldNamespaceURI)) {
			return;
		}

		document.fireNamespaceChanged(new NamespaceDeclarationChangeEvent(document, this));
	}

	public void removeNamespace(final String namespacePrefix) {
		final String oldNamespaceURI = namespaceDeclarations.remove(namespacePrefix);
		final Document document = getDocument();
		if (document == null) {
			return;
		}

		if (oldNamespaceURI == null) {
			return; // we have actually removed nothing, so we should not tell anybody about it
		}

		document.fireNamespaceChanged(new NamespaceDeclarationChangeEvent(document, this));
	}

	public void declareDefaultNamespace(final String namespaceURI) {
		declareNamespace(null, namespaceURI);
	}

	public void removeDefaultNamespace() {
		removeNamespace(null);
	}

	/*
	 * Miscellaneous
	 */

	@Override
	public String toString() {

		final StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(getPrefixedName().toString());

		for (final IAttribute attribute : getAttributes()) {
			sb.append(" ");
			sb.append(attribute.getPrefixedName());
			sb.append("=\"");
			sb.append(attribute.getValue());
			sb.append("\"");
		}

		sb.append("> (");
		if (isAssociated()) {
			sb.append(getStartOffset());
			sb.append(",");
			sb.append(getEndOffset());
		} else {
			sb.append("n/a");
		}
		sb.append(")");

		return sb.toString();
	}

}

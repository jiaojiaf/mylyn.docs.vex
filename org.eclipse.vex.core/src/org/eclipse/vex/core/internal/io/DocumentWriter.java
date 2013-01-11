/*******************************************************************************
 * Copyright (c) 2004, 2008 John Krasnay and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     John Krasnay - initial API and implementation
 *     Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
 *******************************************************************************/
package org.eclipse.vex.core.internal.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import org.eclipse.vex.core.internal.dom.Attribute;
import org.eclipse.vex.core.internal.dom.BaseNodeVisitor;
import org.eclipse.vex.core.internal.dom.Comment;
import org.eclipse.vex.core.internal.dom.Document;
import org.eclipse.vex.core.internal.dom.DocumentFragment;
import org.eclipse.vex.core.internal.dom.Element;
import org.eclipse.vex.core.internal.dom.IWhitespacePolicy;
import org.eclipse.vex.core.internal.dom.Node;
import org.eclipse.vex.core.internal.dom.Text;
import org.eclipse.vex.core.internal.dom.Validator;
import org.eclipse.vex.core.internal.validator.AttributeDefinition;

/**
 * Writes a document to an output stream, using a stylesheet to provide formatting hints.
 * 
 * <ul>
 * <li>Children of an element are indented by a configurable amount.</li>
 * <li>Text is wrapped to fit within a configurable width.
 * <li>
 * </ul>
 * 
 * <p>
 * Documents are currently saved UTF-8 encoding, with no encoding specified in the XML declaration.
 * </p>
 */
public class DocumentWriter {

	private IWhitespacePolicy whitespacePolicy;
	private String indent;
	private int wrapColumn;

	public DocumentWriter() {
		indent = "  ";
		wrapColumn = 72;
		whitespacePolicy = IWhitespacePolicy.NULL;
	}

/**
     * Escapes special XML characters. Changes '<', '>', and '&' to
     * '&lt;', '&gt;' and '&amp;', respectively.
     *
     * @param s the string to be escaped.
     * @return the escaped string
     */
	public static String escape(final String s) {
		final StringBuilder result = new StringBuilder(s.length());

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (c == '<') {
				result.append("&lt;");
			} else if (c == '>') {
				result.append("&gt;");
			} else if (c == '&') {
				result.append("&amp;");
			} else if (c == '"') {
				result.append("&quot;");
			} else if (c == '\'') {
				result.append("&apos;");
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * @return the indent string. By default this is two spaces.
	 */
	public String getIndent() {
		return indent;
	}

	/**
	 * Sets the value of the indent string.
	 * 
	 * @param indent
	 *            new value for the indent string.
	 */
	public void setIndent(final String indent) {
		this.indent = indent;
	}

	/**
	 * @return the whitespace policy used by this writer.
	 */
	public IWhitespacePolicy getWhitespacePolicy() {
		return whitespacePolicy;
	}

	/**
	 * Sets the whitespace policy for this writer. The whitespace policy tells the writer which elements are
	 * block-formatted and which are pre-formatted.
	 * 
	 * @param whitespacePolicy
	 *            The whitespacePolicy to set.
	 */
	public void setWhitespacePolicy(final IWhitespacePolicy whitespacePolicy) {
		if (whitespacePolicy == null) {
			this.whitespacePolicy = IWhitespacePolicy.NULL;
		} else {
			this.whitespacePolicy = whitespacePolicy;
		}
	}

	/**
	 * @return the column at which text should be wrapped. By default this is 72.
	 */
	public int getWrapColumn() {
		return wrapColumn;
	}

	/**
	 * Sets the value of the wrap column.
	 * 
	 * @param wrapColumn
	 *            new value for the wrap column.
	 */
	public void setWrapColumn(final int wrapColumn) {
		this.wrapColumn = wrapColumn;
	}

	public void write(final Document document, final OutputStream out) throws IOException {
		final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		printWriter.println("<?xml version='1.0' encoding='UTF-8'?>");

		writeNode(document, printWriter, "");
		printWriter.flush();
	}

	public void write(final DocumentFragment fragment, final OutputStream out) throws IOException {
		final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
		printWriter.println("<?xml version='1.0' encoding='UTF-8'?>");

		writeNode(fragment, printWriter, "");
		printWriter.flush();
	}

	// ====================================================== PRIVATE

	private void writeNode(final Node node, final PrintWriter out, final String indent) {
		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final Document document) {
				if (document.getSystemID() != null) {
					final StringBuilder buffer = new StringBuilder();
					buffer.append("<!DOCTYPE ");
					buffer.append(document.getRootElement().getPrefixedName());
					if (document.getPublicID() != null) {
						buffer.append(" PUBLIC");
						buffer.append(" \"");
						buffer.append(document.getPublicID());
						buffer.append("\"");
					} else {
						buffer.append(" SYSTEM");
					}
					buffer.append(" \"");
					buffer.append(document.getSystemID());
					buffer.append("\">");
					out.println(buffer.toString());
				}

				for (final Node child : document.getChildNodes()) {
					writeNode(child, out, indent);
				}
			}

			@Override
			public void visit(final DocumentFragment fragment) {
				out.print("<vex_fragment>");
				for (final Node child : fragment.getChildNodes()) {
					writeNodeNoWrap(child, out);
				}
				out.println("</vex_fragment>");
			}

			@Override
			public void visit(final Element element) {
				if (whitespacePolicy.isPre(element)) {
					out.print(indent);
					writeNodeNoWrap(node, out);
					out.println();
					return;
				}

				boolean hasBlockChild = false;
				final List<Element> children = element.getChildElements();
				for (int i = 0; i < children.size(); i++) {
					if (whitespacePolicy.isBlock(children.get(i))) {
						hasBlockChild = true;
						break;
					}
				}

				if (hasBlockChild) {
					out.print(indent);
					out.print("<");
					out.print(element.getPrefixedName());

					final TextWrapper wrapper = new TextWrapper();
					wrapper.addNoSplit(getNamespaceDeclarationsString(element));
					wrapper.addNoSplit(getAttributeString(element));
					final int outdent = indent.length() + 1 + element.getPrefixedName().length();
					final String[] lines = wrapper.wrap(wrapColumn - outdent);
					final char[] bigIndent = new char[outdent];
					Arrays.fill(bigIndent, ' ');
					for (int i = 0; i < lines.length; i++) {
						if (i > 0) {
							out.print(bigIndent);
						}
						out.print(lines[i]);
						if (i < lines.length - 1) {
							out.println();
						}
					}
					out.println(">");

					final String childIndent = indent + DocumentWriter.this.indent;
					for (final Node child : element.getChildNodes()) {
						writeNode(child, out, childIndent);
					}
					out.print(indent);
					out.print("</");
					out.print(element.getPrefixedName());
					out.println(">");
				} else {
					final TextWrapper wrapper = new TextWrapper();
					addNode(element, wrapper);
					final String[] lines = wrapper.wrap(wrapColumn - indent.length());
					for (final String line : lines) {
						out.print(indent);
						out.println(line);
					}
				}
			}

			@Override
			public void visit(final Comment comment) {
				out.print(indent);
				out.println("<!-- ");

				final String childIndent = indent + DocumentWriter.this.indent;
				final TextWrapper wrapper = new TextWrapper();
				wrapper.add(escape(node.getText()));
				final String[] lines = wrapper.wrap(wrapColumn - childIndent.length());

				for (final String line : lines) {
					out.print(childIndent);
					out.println(line);
				}

				out.print(indent);
				out.println(" -->");
			}

			@Override
			public void visit(final Text text) {
				final TextWrapper wrapper = new TextWrapper();
				wrapper.add(escape(node.getText()));

				final String[] lines = wrapper.wrap(wrapColumn - indent.length());

				for (final String line : lines) {
					out.print(indent);
					out.println(line);
				}
			}
		});
	}

	private void writeNodeNoWrap(final Node node, final PrintWriter out) {
		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final Element element) {
				out.print("<");
				out.print(element.getPrefixedName());
				out.print(getNamespaceDeclarationsString(element));
				out.print(getAttributeString(element));
				out.print(">");

				for (final Node child : element.getChildNodes()) {
					writeNodeNoWrap(child, out);
				}

				out.print("</");
				out.print(element.getPrefixedName());
				out.print(">");
			}

			@Override
			public void visit(final Comment comment) {
				out.print("<!-- ");
				out.print(escape(node.getText()));
				out.print(" -->");
			}

			@Override
			public void visit(final Text text) {
				out.print(escape(node.getText()));
			}
		});
	}

	private static String getNamespaceDeclarationsString(final Element element) {
		final StringBuilder result = new StringBuilder();
		final String declaredNamespaceURI = element.getDeclaredDefaultNamespaceURI();
		if (declaredNamespaceURI != null) {
			result.append(" xmlns=\"").append(declaredNamespaceURI).append("\"");
		}
		for (final String prefix : element.getDeclaredNamespacePrefixes()) {
			result.append(" xmlns:").append(prefix).append("=\"").append(element.getNamespaceURI(prefix)).append("\"");
		}
		return result.toString();
	}

	private static void addNode(final Node node, final TextWrapper wrapper) {
		node.accept(new BaseNodeVisitor() {
			@Override
			public void visit(final Element element) {
				final List<Node> content = element.getChildNodes();

				final StringBuilder buffer = new StringBuilder();
				buffer.append("<").append(element.getPrefixedName());
				buffer.append(getNamespaceDeclarationsString(element));
				buffer.append(getAttributeString(element));
				if (content.isEmpty()) {
					buffer.append("/>");
				} else {
					buffer.append(">");
				}
				wrapper.addNoSplit(buffer.toString());

				for (final Node child : content) {
					addNode(child, wrapper);
				}

				if (!content.isEmpty()) {
					wrapper.add("</" + element.getPrefixedName() + ">");
				}
			}

			@Override
			public void visit(final Comment comment) {
				wrapper.addNoSplit("<!-- ");
				wrapper.add(escape(node.getText()));
				wrapper.addNoSplit(" -->");
			}

			@Override
			public void visit(final Text text) {
				wrapper.add(escape(node.getText()));
			}
		});
	}

	private static String getAttributeString(final Element element) {
		final Document document = element.getDocument();
		final Validator validator = document != null ? document.getValidator() : null;

		final StringBuffer result = new StringBuffer();
		for (final Attribute attribute : element.getAttributes()) {
			if (!isAttributeDefaultValueSet(validator, attribute)) {
				result.append(" ");
				result.append(attribute.getPrefixedName());
				result.append("=\"");
				result.append(escape(attribute.getValue()));
				result.append("\"");
			}
		}
		return result.toString();
	}

	private static boolean isAttributeDefaultValueSet(final Validator validator, final Attribute attribute) {
		if (validator != null) {
			final AttributeDefinition attributeDefinition = validator.getAttributeDefinition(attribute);
			if (attributeDefinition != null) {
				final String currentValue = attribute.getValue();
				final String defaultValue = attributeDefinition.getDefaultValue();
				return currentValue.equals(defaultValue);
			}
		}
		return false;
	}
}
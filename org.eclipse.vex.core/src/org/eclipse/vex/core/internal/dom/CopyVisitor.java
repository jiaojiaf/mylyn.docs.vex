/*******************************************************************************
 * Copyright (c) 2012, 2013 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 * 		Carsten Hiesserich - added processing instructions
 *******************************************************************************/
package org.eclipse.vex.core.internal.dom;

import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IDocumentFragment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;

/**
 * This visitor creates a simple copy of the visited node. I.e. only the node itself, not its content neither its
 * children are copied. The copy is provided through the getCopy() method.
 * 
 * @author Florian Thienel
 */
public class CopyVisitor implements INodeVisitorWithResult<Node> {

	public Document visit(final IDocument document) {
		throw new UnsupportedOperationException("Document cannot be copied");
	}

	public DocumentFragment visit(final IDocumentFragment fragment) {
		throw new UnsupportedOperationException("DocumentFragment cannot be copied");
	}

	public Element visit(final IElement element) {
		final Element copyElement = new Element(element.getQualifiedName());
		copyElement.accept(new CopyOfElement(element));
		return copyElement;
	}

	public Text visit(final IText text) {
		// ignore Text nodes because they are created dynamically in Element.getChildNodes()
		return null;
	}

	public Comment visit(final IComment comment) {
		return new Comment();
	}

	public ProcessingInstruction visit(final IProcessingInstruction pi) {
		return new ProcessingInstruction(pi.getTarget());
	}

}

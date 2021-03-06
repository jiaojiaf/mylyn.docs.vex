/*******************************************************************************
 * Copyright (c) 2009 Holger Voormann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Holger Voormann - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.INodeVisitorWithResult;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.core.provisional.dom.IText;
import org.eclipse.vex.ui.internal.handlers.VexHandlerUtil;
import org.eclipse.vex.ui.internal.handlers.VexHandlerUtil.RowColumnInfo;

/**
 * If this class is declared in {@code org.eclipse.ui.services} extension then state information about {@link VexWidget}
 * could be exposed by variables. This variables could then be used to manage visibility or enablement of UI elements
 * declarative in plug-in extensions (e.g. see 'enabledWhen' in {@code org.eclipse.ui.handlers} extension or
 * 'visibleWhen' in {@code org.eclipse.ui.menus} extension).
 */
public class DocumentContextSourceProvider extends AbstractSourceProvider {

	/** Variable ID of the <em>is-column</em> flag. */
	public static final String IS_COLUMN = "org.eclipse.vex.ui.isColumn";

	/** Variable ID of the <em>is-first-column</em> flag. */
	public static final String IS_FIRST_COLUMN = "org.eclipse.vex.ui.isFirstColumn";

	/** Variable ID of the <em>is-last-column</em> flag. */
	public static final String IS_LAST_COLUMN = "org.eclipse.vex.ui.isLastColumn";

	/** Variable ID of the <em>is-row</em> flag. */
	public static final String IS_ROW = "org.eclipse.vex.ui.isRow";

	/** Variable ID of the <em>is-fist-row</em> flag. */
	public static final String IS_FIRST_ROW = "org.eclipse.vex.ui.isFirstRow";

	/** Variable ID of the <em>is-last-row</em> flag. */
	public static final String IS_LAST_ROW = "org.eclipse.vex.ui.isLastRow";

	/** Variable ID of the <em>is-element</em> flag. */
	public static final String IS_ELEMENT = "org.eclipse.vex.ui.isElement";

	/** Variable ID of the <em>is-comment</em> flag. */
	public static final String IS_COMMENT = "org.eclipse.vex.ui.isComment";

	/** Variable ID of the <em>is-processing-instruction</em> flag. */
	public static final String IS_PROCESSING_INSTRUCTION = "org.eclipse.vex.ui.isProcessingInstruction";

	private boolean isColumn;
	private boolean isFirstColumn;
	private boolean isLastColumn;
	private boolean isRow;
	private boolean isFirstRow;
	private boolean isLastRow;
	private boolean isElement;
	private boolean isComment;
	private boolean isProcessingInstruction;

	private INode currentNode;

	public void dispose() {
		// nothing to clean-up (all fields are primitives)
	}

	public String[] getProvidedSourceNames() {
		return new String[] { IS_COLUMN };
	}

	public Map<String, Boolean> getCurrentState() {
		final Map<String, Boolean> currentState = new HashMap<String, Boolean>(6);
		currentState.put(IS_COLUMN, Boolean.valueOf(isColumn));
		currentState.put(IS_FIRST_COLUMN, Boolean.valueOf(isFirstColumn));
		currentState.put(IS_LAST_COLUMN, Boolean.valueOf(isLastColumn));
		currentState.put(IS_ROW, Boolean.valueOf(isRow));
		currentState.put(IS_FIRST_ROW, Boolean.valueOf(isFirstRow));
		currentState.put(IS_LAST_ROW, Boolean.valueOf(isLastRow));
		currentState.put(IS_ELEMENT, Boolean.valueOf(isElement));
		currentState.put(IS_COMMENT, Boolean.valueOf(isComment));
		currentState.put(IS_PROCESSING_INSTRUCTION, Boolean.valueOf(isProcessingInstruction));
		return currentState;
	}

	/**
	 * Synchronizes the variable values which will be exposed by this service with the specified {@link VexWidget}.
	 * 
	 * @param widget
	 *            the Vex widget containing the actual states
	 */
	public void fireUpdate(final VexWidget widget) {
		final Map<String, Boolean> changes = new HashMap<String, Boolean>();
		final RowColumnInfo rowColumnInfo = VexHandlerUtil.getRowColumnInfo(widget);

		// column
		final int columnIndex = VexHandlerUtil.getCurrentColumnIndex(widget);
		final int columnCount = rowColumnInfo == null ? -1 : rowColumnInfo.maxColumnCount;
		isColumn = update(changes, isColumn, columnIndex != -1, IS_COLUMN);
		isFirstColumn = update(changes, isFirstColumn, columnIndex == 0, IS_FIRST_COLUMN);
		isLastColumn = update(changes, isLastColumn, columnIndex == columnCount - 1, IS_LAST_COLUMN);

		// row
		final int rowCount = rowColumnInfo == null ? -1 : rowColumnInfo.rowCount;
		final int rowIndex = rowColumnInfo == null ? -1 : rowColumnInfo.rowIndex;
		isRow = update(changes, isRow, rowIndex != -1, IS_ROW);
		isFirstRow = update(changes, isFirstRow, rowIndex == 0, IS_FIRST_ROW);
		isLastRow = update(changes, isLastRow, rowIndex == rowCount - 1, IS_LAST_ROW);

		// nodes
		final INode selectedNode = widget.getCurrentNode();
		if (!selectedNode.equals(currentNode)) {
			// No need to evaluate if the node has not changed
			currentNode = selectedNode;
			changes.putAll(currentNode.accept(nodeTypeVisitor));
		}

		if (!changes.isEmpty()) {
			fireSourceChanged(ISources.WORKBENCH, changes);
		}
	}

	private static boolean update(final Map<String, Boolean> changes, final boolean oldValue, final boolean newValue, final String valueName) {
		if (newValue == oldValue) {
			return oldValue;
		}

		changes.put(valueName, Boolean.valueOf(newValue));
		return newValue;
	}

	private final INodeVisitorWithResult<Map<String, Boolean>> nodeTypeVisitor = new BaseNodeVisitorWithResult<Map<String, Boolean>>(new HashMap<String, Boolean>()) {
		@Override
		public Map<String, Boolean> visit(final IElement element) {
			final Map<String, Boolean> result = new HashMap<String, Boolean>(3);
			result.put(IS_ELEMENT, true);
			result.put(IS_COMMENT, false);
			result.put(IS_PROCESSING_INSTRUCTION, false);
			return result;
		};

		@Override
		public Map<String, Boolean> visit(final IComment comment) {
			final Map<String, Boolean> result = new HashMap<String, Boolean>(3);
			result.put(IS_ELEMENT, false);
			result.put(IS_COMMENT, true);
			result.put(IS_PROCESSING_INSTRUCTION, false);
			return result;
		};

		@Override
		public Map<String, Boolean> visit(final IProcessingInstruction pi) {
			final Map<String, Boolean> result = new HashMap<String, Boolean>(3);
			result.put(IS_ELEMENT, false);
			result.put(IS_COMMENT, false);
			result.put(IS_PROCESSING_INSTRUCTION, true);
			return result;
		};

		@Override
		public Map<String, Boolean> visit(final IText text) {
			return text.getParent().accept(this);
		}
	};
}

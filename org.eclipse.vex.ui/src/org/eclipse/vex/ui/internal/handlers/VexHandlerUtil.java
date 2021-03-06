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
 *     Carsten Hiesserich - use a visitor to create a new table row
 *******************************************************************************/
package org.eclipse.vex.ui.internal.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.vex.core.internal.css.CSS;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.internal.layout.ElementOrRangeCallback;
import org.eclipse.vex.core.internal.layout.LayoutUtils;
import org.eclipse.vex.core.internal.widget.IVexWidget;
import org.eclipse.vex.core.internal.widget.swt.VexWidget;
import org.eclipse.vex.core.provisional.dom.BaseNodeVisitor;
import org.eclipse.vex.core.provisional.dom.ContentRange;
import org.eclipse.vex.core.provisional.dom.IAttribute;
import org.eclipse.vex.core.provisional.dom.IComment;
import org.eclipse.vex.core.provisional.dom.IDocument;
import org.eclipse.vex.core.provisional.dom.IElement;
import org.eclipse.vex.core.provisional.dom.INode;
import org.eclipse.vex.core.provisional.dom.IParent;
import org.eclipse.vex.core.provisional.dom.IProcessingInstruction;
import org.eclipse.vex.ui.internal.editor.VexEditor;

/**
 * Static helper methods used across handlers.
 */
public final class VexHandlerUtil {

	public static VexWidget computeWidget(final ExecutionEvent event) throws ExecutionException {

		final IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		assertNotNull(activeEditor);

		VexWidget widget = null;
		if (activeEditor instanceof VexEditor) {
			widget = ((VexEditor) activeEditor).getVexWidget();
		}
		assertNotNull(widget);
		return widget;
	}

	public static VexWidget computeWidget(final IWorkbenchWindow window) {
		final VexEditor editor = computeVexEditor(window);
		if (editor == null) {
			return null;
		}
		return editor.getVexWidget();
	}

	public static VexEditor computeVexEditor(final IWorkbenchWindow window) {
		final IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		if (activeEditor == null) {
			return null;
		}

		if (activeEditor instanceof VexEditor) {
			return (VexEditor) activeEditor;
		}
		return null;
	}

	private static void assertNotNull(final Object object) throws ExecutionException {
		if (object == null) {
			throw new ExecutionException("Can not compute VexWidget.");
		}
	}

	public static class RowColumnInfo {
		public Object row;
		public Object cell;
		public int rowIndex;
		public int cellIndex;
		public int rowCount;
		public int columnCount;
		public int maxColumnCount;
	}

	/**
	 * Duplicate the given table row, inserting a new empty one aove or below it. The new row contains empty children
	 * corresponding to the given row's children.
	 * 
	 * @param vexWidget
	 *            IVexWidget with which we're working
	 * @param tableRow
	 *            TableRowBox to be duplicated.
	 * @param addAbove
	 *            <code>true</code> to add the new row above the current one
	 */
	public static void duplicateTableRow(final IVexWidget vexWidget, final IElement tableRow, final boolean addAbove) {
		final StyleSheet styleSheet = vexWidget.getStyleSheet();
		if (!styleSheet.getStyles(tableRow).getDisplay().equals(CSS.TABLE_ROW)) {
			return;
		}

		if (addAbove) {
			vexWidget.moveTo(tableRow.getStartOffset());
		} else {
			vexWidget.moveTo(tableRow.getEndOffset() + 1);
		}

		// Create a new table row
		final IElement newRow = vexWidget.insertElement(tableRow.getQualifiedName());

		// Iterate all direct children and add them to the new row
		final Iterator<? extends INode> childIterator = tableRow.children().withoutText().iterator();
		while (childIterator.hasNext()) {
			childIterator.next().accept(new BaseNodeVisitor() {
				@Override
				public void visit(final IElement element) {
					final IElement newElement = vexWidget.insertElement(element.getQualifiedName());
					for (final IAttribute attr : element.getAttributes()) {
						newElement.setAttribute(attr.getQualifiedName(), attr.getValue());
					}
					vexWidget.moveBy(1);
				}

				@Override
				public void visit(final IComment comment) {
					// Comments are copied with content
					vexWidget.insertComment();
					vexWidget.insertText(comment.getText());
					vexWidget.moveBy(1);
				}

				@Override
				public void visit(final IProcessingInstruction pi) {
					// Processing instructions are copied with target and  content
					vexWidget.insertProcessingInstruction(pi.getTarget());
					vexWidget.insertText(pi.getText());
					vexWidget.moveBy(1);
				}
			});
		}
		try {
			final INode firstTextChild = newRow.childElements().first();
			vexWidget.moveTo(firstTextChild.getStartOffset());
		} catch (final NoSuchElementException ex) {
			vexWidget.moveTo(newRow.getStartOffset() + 1);
		}
	}

	/**
	 * Returns true if the given element or range is at least partially selected.
	 * 
	 * @param widget
	 *            IVexWidget being tested.
	 * @param elementOrRange
	 *            Element or IntRange being tested.
	 */
	public static boolean elementOrRangeIsPartiallySelected(final IVexWidget widget, final Object elementOrRange) {
		final ContentRange elementContentRange = getInnerRange(elementOrRange);
		final ContentRange selectedRange = widget.getSelectedRange();
		return elementContentRange.intersects(selectedRange);
	}

	/**
	 * Returns the zero-based index of the table column containing the current offset. Returns -1 if we are not inside a
	 * table.
	 */
	public static int getCurrentColumnIndex(final IVexWidget vexWidget) {

		final IElement row = getCurrentTableRow(vexWidget);

		if (row == null) {
			return -1;
		}

		final int offset = vexWidget.getCaretOffset();
		final int[] column = new int[] { -1 };
		LayoutUtils.iterateTableCells(vexWidget.getStyleSheet(), row, new ElementOrRangeCallback() {
			private int i = 0;

			public void onElement(final IElement child, final String displayStyle) {
				if (offset > child.getStartOffset() && offset <= child.getEndOffset()) {
					column[0] = i;
				}
				i++;
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				i++;
			}
		});

		return column[0];
	}

	/**
	 * Returns the innermost Element with style table-row containing the caret, or null if no such element exists.
	 * 
	 * @param vexWidget
	 *            IVexWidget to use.
	 */
	public static IElement getCurrentTableRow(final IVexWidget vexWidget) {
		final StyleSheet styleSheet = vexWidget.getStyleSheet();
		IElement element = vexWidget.getCurrentElement();

		while (element != null) {
			if (styleSheet.getStyles(element).getDisplay().equals(CSS.TABLE_ROW)) {
				return element;
			}
			element = element.getParentElement();
		}

		return null;
	}

	/**
	 * Returns the currently selected table rows, or the current row if ther is no selection. If no row can be found,
	 * returns an empty array.
	 * 
	 * @param vexWidget
	 *            IVexWidget to use.
	 */
	public static SelectedRows getSelectedTableRows(final IVexWidget vexWidget) {
		final SelectedRows selected = new SelectedRows();

		VexHandlerUtil.iterateTableCells(vexWidget, new TableCellCallbackAdapter() {
			@Override
			public void startRow(final Object row, final int rowIndex) {
				if (VexHandlerUtil.elementOrRangeIsPartiallySelected(vexWidget, row)) {
					if (selected.rows == null) {
						selected.rows = new ArrayList<Object>();
					}
					selected.rows.add(row);
				} else {
					if (selected.rows == null) {
						selected.rowBefore = row;
					} else {
						if (selected.rowAfter == null) {
							selected.rowAfter = row;
						}
					}
				}
			}
		});

		return selected;
	}

	public static void iterateTableCells(final IVexWidget vexWidget, final ITableCellCallback callback) {

		final StyleSheet ss = vexWidget.getStyleSheet();

		iterateTableRows(vexWidget, new ElementOrRangeCallback() {

			final private int[] rowIndex = { 0 };

			public void onElement(final IElement row, final String displayStyle) {

				callback.startRow(row, rowIndex[0]);

				LayoutUtils.iterateTableCells(ss, row, new ElementOrRangeCallback() {
					private int cellIndex = 0;

					public void onElement(final IElement cell, final String displayStyle) {
						callback.onCell(row, cell, rowIndex[0], cellIndex);
						cellIndex++;
					}

					public void onRange(final IParent parent, final int startOffset, final int endOffset) {
						callback.onCell(row, new ContentRange(startOffset, endOffset), rowIndex[0], cellIndex);
						cellIndex++;
					}
				});

				callback.endRow(row, rowIndex[0]);

				rowIndex[0]++;
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {

				final ContentRange row = new ContentRange(startOffset, endOffset);
				callback.startRow(row, rowIndex[0]);

				LayoutUtils.iterateTableCells(ss, parent, startOffset, endOffset, new ElementOrRangeCallback() {
					private int cellIndex = 0;

					public void onElement(final IElement cell, final String displayStyle) {
						callback.onCell(row, cell, rowIndex[0], cellIndex);
						cellIndex++;
					}

					public void onRange(final IParent parent, final int startOffset, final int endOffset) {
						callback.onCell(row, new ContentRange(startOffset, endOffset), rowIndex[0], cellIndex);
						cellIndex++;
					}
				});

				callback.endRow(row, rowIndex[0]);

				rowIndex[0]++;
			}
		});
	}

	/**
	 * Returns a RowColumnInfo structure containing information about the table containing the caret. Returns null if
	 * the caret is not currently inside a table.
	 * 
	 * @param vexWidget
	 *            IVexWidget to inspect.
	 */
	public static RowColumnInfo getRowColumnInfo(final IVexWidget vexWidget) {

		final boolean[] found = new boolean[1];
		final RowColumnInfo[] rcInfo = new RowColumnInfo[] { new RowColumnInfo() };
		final int offset = vexWidget.getCaretOffset();

		rcInfo[0].cellIndex = -1;
		rcInfo[0].rowIndex = -1;

		iterateTableCells(vexWidget, new ITableCellCallback() {

			private int rowColumnCount;

			public void startRow(final Object row, final int rowIndex) {
				rowColumnCount = 0;
			}

			public void onCell(final Object row, final Object cell, final int rowIndex, final int cellIndex) {
				found[0] = true;
				if (LayoutUtils.elementOrRangeContains(row, offset)) {
					rcInfo[0].row = row;
					rcInfo[0].rowIndex = rowIndex;
					rcInfo[0].columnCount++;

					if (LayoutUtils.elementOrRangeContains(cell, offset)) {
						rcInfo[0].cell = cell;
						rcInfo[0].cellIndex = cellIndex;
					}
				}

				rowColumnCount++;
			}

			public void endRow(final Object row, final int rowIndex) {
				rcInfo[0].rowCount++;
				rcInfo[0].maxColumnCount = Math.max(rcInfo[0].maxColumnCount, rowColumnCount);
			}
		});

		if (found[0]) {
			return rcInfo[0];
		} else {
			return null;
		}
	}

	/**
	 * Iterate over all rows in the table containing the caret.
	 * 
	 * @param vexWidget
	 *            IVexWidget to iterate over.
	 * @param callback
	 *            Caller-provided callback that this method calls for each row in the current table.
	 */
	public static void iterateTableRows(final IVexWidget vexWidget, final ElementOrRangeCallback callback) {

		final StyleSheet ss = vexWidget.getStyleSheet();
		final IDocument doc = vexWidget.getDocument();
		final int offset = vexWidget.getCaretOffset();

		// This may or may not be a table
		// In any case, it's the element that contains the top-level table
		// children
		IElement table = doc.getElementForInsertionAt(offset);

		while (table != null && !LayoutUtils.isTableChild(ss, table)) {
			table = table.getParentElement();
		}

		while (table != null && LayoutUtils.isTableChild(ss, table)) {
			table = table.getParentElement();
		}

		if (table == null || table.getParentElement() == null) {
			return;
		}

		final List<IElement> tableChildren = new ArrayList<IElement>();
		final boolean[] found = new boolean[] { false };
		LayoutUtils.iterateChildrenByDisplayStyle(ss, LayoutUtils.TABLE_CHILD_STYLES, table, new ElementOrRangeCallback() {
			public void onElement(final IElement child, final String displayStyle) {
				if (offset >= child.getStartOffset() && offset <= child.getEndOffset()) {
					found[0] = true;
				}
				tableChildren.add(child);
			}

			public void onRange(final IParent parent, final int startOffset, final int endOffset) {
				if (!found[0]) {
					tableChildren.clear();
				}
			}
		});

		if (!found[0]) {
			return;
		}

		final int startOffset = tableChildren.get(0).getStartOffset();
		final int endOffset = tableChildren.get(tableChildren.size() - 1).getEndOffset() + 1;
		LayoutUtils.iterateTableRows(ss, table, startOffset, endOffset, callback);
	}

	/**
	 * Returns an IntRange representing the offsets inside the given Element or IntRange. If an Element is passed,
	 * returns the offsets inside the sentinels. If an IntRange is passed it is returned directly.
	 * 
	 * @param elementOrRange
	 *            Element or IntRange to be inspected.
	 */
	public static ContentRange getInnerRange(final Object elementOrRange) {
		if (elementOrRange instanceof IElement) {
			final IElement element = (IElement) elementOrRange;
			return new ContentRange(element.getStartOffset() + 1, element.getEndOffset());
		} else {
			return (ContentRange) elementOrRange;
		}
	}

	/**
	 * Returns an IntRange representing the offsets outside the given Element or IntRange. If an Element is passed,
	 * returns the offsets outside the sentinels. If an IntRange is passed it is returned directly.
	 * 
	 * @param elementOrRange
	 *            Element or IntRange to be inspected.
	 */
	public static ContentRange getOuterRange(final Object elementOrRange) {
		if (elementOrRange instanceof IElement) {
			final IElement element = (IElement) elementOrRange;
			return new ContentRange(element.getStartOffset(), element.getEndOffset() + 1);
		} else {
			return (ContentRange) elementOrRange;
		}
	}

	public static class SelectedRows {

		private SelectedRows() {
		}

		public List<Object> getRows() {
			return rows;
		}

		public Object getRowBefore() {
			return rowBefore;
		}

		public Object getRowAfter() {
			return rowAfter;
		}

		private List<Object> rows;
		private Object rowBefore;
		private Object rowAfter;
	}

}

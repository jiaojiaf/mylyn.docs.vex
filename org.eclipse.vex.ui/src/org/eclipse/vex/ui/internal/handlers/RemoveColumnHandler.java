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
package org.eclipse.vex.ui.internal.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.vex.core.internal.widget.swt.VexWidget;

/**
 * Deletes current column.
 */
public class RemoveColumnHandler extends AbstractRemoveTableCellsHandler {

	@Override
	protected List<Object> collectCellsToDelete(final VexWidget widget, final VexHandlerUtil.RowColumnInfo rcInfo) {
		final List<Object> cellsToDelete = new ArrayList<Object>();
		VexHandlerUtil.iterateTableCells(widget, new TableCellCallbackAdapter() {
			@Override
			public void onCell(final Object row, final Object cell, final int rowIndex, final int cellIndex) {
				if (cellIndex == rcInfo.cellIndex) {
					cellsToDelete.add(cell);
				}
			}
		});
		return cellsToDelete;
	}

}

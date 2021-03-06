/*******************************************************************************
 * Copyright (c) 2011 Florian Thienel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Florian Thienel - initial API and implementation
 *******************************************************************************/
package org.eclipse.vex.ui.internal;

import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.vex.core.internal.css.IStyleSheetProvider;
import org.eclipse.vex.core.internal.css.StyleSheet;
import org.eclipse.vex.core.provisional.dom.DocumentContentModel;
import org.eclipse.vex.ui.internal.config.ConfigurationRegistry;
import org.eclipse.vex.ui.internal.config.DocumentType;
import org.eclipse.vex.ui.internal.config.Style;
import org.eclipse.vex.ui.internal.editor.VexDocumentContentModel;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author Florian Thienel
 */
public class VexPreferences implements IStyleSheetProvider {

	public static final String INDENTATION_CHAR_CHOICE = "indetationCharChoice";

	public static final String INDENTATION_CHAR_TAB = "\t";

	public static final String INDENTATION_CHAR_SPACE = " ";

	public static final String INDENTATION_SIZE = "indetationSize";

	public static final String LINE_WIDTH = "lineWidth";

	private static final String PREFERRED_STYLE_SUFFIX = ".style";

	private final IPreferenceStore preferenceStore;

	private final ConfigurationRegistry configurationRegistry;

	public VexPreferences(final IPreferenceStore preferenceStore, final ConfigurationRegistry configurationRegistry) {
		this.preferenceStore = preferenceStore;
		this.configurationRegistry = configurationRegistry;
	}

	public void setPreferredStyleId(final DocumentType doctype, final String styleId) {
		final Preferences preferences = InstanceScope.INSTANCE.getNode(VexPlugin.ID);
		final String key = getStylePreferenceKey(doctype.getUniqueId());
		preferences.put(key, styleId);
		try {
			preferences.flush();
		} catch (final BackingStoreException e) {
			VexPlugin.getDefault().log(IStatus.ERROR, Messages.getString("VexEditor.errorSavingStylePreference"), e); //$NON-NLS-1$
		}
	}

	public String getPreferredStyleId(final DocumentType doctype) {
		final Preferences preferences = InstanceScope.INSTANCE.getNode(VexPlugin.ID);
		final String preferredStyleId = preferences.get(getStylePreferenceKey(doctype.getUniqueId()), null);
		return preferredStyleId;
	}

	private static String getStylePreferenceKey(final String publicId) {
		return publicId + PREFERRED_STYLE_SUFFIX;
	}

	public Style getPreferredStyle(final DocumentType doctype) {
		return configurationRegistry.getStyle(doctype, getPreferredStyleId(doctype));
	}

	public String getIndentationPattern() {
		final String indentationChar = preferenceStore.getString(INDENTATION_CHAR_CHOICE);
		final int indentationSize = preferenceStore.getInt(INDENTATION_SIZE);
		final char[] pattern = new char[indentationSize];
		Arrays.fill(pattern, indentationChar.charAt(0));
		return new String(pattern);
	}

	public int getLineWidth() {
		return preferenceStore.getInt(LINE_WIDTH);
	}

	public StyleSheet getStyleSheet(final DocumentContentModel documentContentModel) {
		Style style = null;
		if (documentContentModel instanceof VexDocumentContentModel) {
			// We only support the VexDocumentContentModel
			// If there will ever be another DocumentContentModel implememntation, it will
			// have to define it's own method for resolving styles
			style = getPreferredStyle(((VexDocumentContentModel) documentContentModel).getDocumentType());
		}
		if (style == null) {
			return StyleSheet.NULL;
		}
		return style.getStyleSheet();
	}
}

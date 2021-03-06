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
package org.eclipse.vex.ui.internal.config;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.vex.ui.internal.VexPlugin;

/**
 * An installed Eclipse bundle that provides Vex configuration items.
 */
public class ConfigPlugin extends ConfigSource {

	private final String bundleSymbolicName;

	public ConfigPlugin(final String bundleSymbolicName) {
		super(bundleSymbolicName);
		this.bundleSymbolicName = bundleSymbolicName;
		load();
	}

	private void load() {
		removeAllItems();
		for (final IExtension extension : Platform.getExtensionRegistry().getExtensions(bundleSymbolicName)) {
			try {
				addItem(extension.getExtensionPointUniqueIdentifier(), extension.getSimpleIdentifier(), extension.getLabel(),
						ConfigurationElementWrapper.convertArray(extension.getConfigurationElements()));
			} catch (final IOException e) {
				final String message = MessageFormat.format(Messages.getString("ConfigPlugin.loadError"), //$NON-NLS-1$
						new Object[] { extension.getSimpleIdentifier(), bundleSymbolicName });
				VexPlugin.getDefault().log(IStatus.ERROR, message, e);
				return;
			}
		}
		parseResources(null);
	}

	@Override
	public URL getBaseUrl() {
		return Platform.getBundle(bundleSymbolicName).getEntry("plugin.xml"); //$NON-NLS-1$
	}

}

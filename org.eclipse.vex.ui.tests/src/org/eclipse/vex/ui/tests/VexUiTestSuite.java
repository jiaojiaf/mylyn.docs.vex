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
package org.eclipse.vex.ui.tests;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.vex.ui.internal.config.tests.ConfigLoaderJobTest;
import org.eclipse.vex.ui.internal.config.tests.ConfigurationRegistryTest;
import org.eclipse.vex.ui.internal.editor.tests.FindReplaceTargetTest;
import org.eclipse.vex.ui.internal.namespace.tests.EditNamespacesControllerTest;
import org.eclipse.vex.ui.internal.swt.tests.DocumentFragmentTransferTest;
import org.eclipse.vex.ui.internal.tests.ResourceTrackerTest;

public class VexUiTestSuite extends TestSuite {

	public static Test suite() {
		return new VexUiTestSuite();
	}

	public VexUiTestSuite() {
		super("Vex UI Tests"); //$NON-NLS-1$
		addTest(new JUnit4TestAdapter(ConfigLoaderJobTest.class));
		addTest(new JUnit4TestAdapter(ConfigurationRegistryTest.class));
		addTest(new JUnit4TestAdapter(EditNamespacesControllerTest.class));
		addTest(new JUnit4TestAdapter(DocumentFragmentTransferTest.class));
		//		addTestSuite(IconTest.class);
		addTestSuite(FindReplaceTargetTest.class);
		addTestSuite(ResourceTrackerTest.class);
	}

}

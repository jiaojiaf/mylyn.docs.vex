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
package org.eclipse.wst.xml.vex.docbook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.wst.xml.vex.core.internal.dom.*;
import org.eclipse.wst.xml.vex.core.internal.provisional.dom.IVEXDocument;
import org.eclipse.wst.xml.vex.core.internal.provisional.dom.IVEXElement;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.wst.xml.vex.ui.internal.editor.VexEditor;
import org.eclipse.wst.xml.vex.ui.internal.outline.IOutlineProvider;

/**
 * Provides an outline of the sections of a DocBook document.
 */
public class DocBookOutlineProvider implements IOutlineProvider {
    
    public void init(VexEditor editor) {
    }

    public ITreeContentProvider getContentProvider() {
        return this.contentProvider;
    }

    public IBaseLabelProvider getLabelProvider() {
        return this.labelProvider;
    }

    public IVEXElement getOutlineElement(final IVEXElement child) {
    	
    	IVEXElement element = child;
    	while (( element.getParent() != null) 
    			&& !isTitledElement(element) ) {
    		element = element.getParent();    		
    	}
    	
    	return element;
    }

    private final ITreeContentProvider contentProvider = new ITreeContentProvider() {

        public void dispose() {
        }

        public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
        }

        public Object[] getChildren(final Object parentElement) {
            return getOutlineChildren((IVEXElement) parentElement);
        }

        public Object getParent(final Object element) {
            final IVEXElement parent = ((IVEXElement) element).getParent();
            if (parent == null) {
                return element;
            } else {
                return getOutlineElement(parent);
            }
        }

        public boolean hasChildren(final Object element) {
            return getOutlineChildren((IVEXElement) element).length > 0;
        }

        public Object[] getElements(final Object inputElement) {
            final IVEXDocument document = (IVEXDocument) inputElement;
            return new Object[] { document.getRootElement() };
        }
        
    };
    
    /**
     * Returns an array of the children of the given element that represent
     * nodes in the outline. These are elements such as "section" that 
     * contain "title" elements that we can use as label content.
     * @param element
     * @return
     */
    private IVEXElement[] getOutlineChildren(final IVEXElement element) {
        final List children = new ArrayList();
        final IVEXElement[] childElements = element.getChildElements();
        for (int i = 0; i < childElements.length; i++) {
            if (titledElements.contains(childElements[i].getName())) {
                children.add(childElements[i]);
            }
        }
        return (IVEXElement[]) children.toArray(new IVEXElement[children.size()]);
    }

    
    private final ILabelProvider labelProvider = new LabelProvider() {
        public String getText(final Object o) {
            final IVEXElement e = (IVEXElement) o;
            IVEXElement titleChild = findChild(e, "title");
            if (titleChild != null) {
                return titleChild.getText();
            } else {
                IVEXElement infoChild = findChild(e, e.getName() + "info");
                if (infoChild != null) {
                    titleChild = findChild(infoChild, "title");
                    if (titleChild != null) {
                        return titleChild.getText();
                    }
                }
            }
            return e.getName();
        }
    };

    /**
     * Returns a value from a map of tag names that contain title elements 
     * The tags all have a similar characteristic. Their content model
     * includes a <title/> element as the first or second child.
     * 
     * For simplicity, we assume that the document is valid w.r.t. the DTD
     * and thus the 'title' element is optional and is one of the first two
     * elements inside the given element. 
     * 
     * @param element
     * @return
     */
    private boolean isTitledElement( final IVEXElement e ) {
    	
    	if( titledElements.contains(e.getName() )
     			|| e.getParent() == null ) {
    		final IVEXElement [] children = e.getChildElements();
    		if( (children.length > 0 && children[0].getName().equals("title"))
    		 || (children.length > 1 && children[1].getName().equals("title"))  		
    		  ) {
    		    	return true;
    		    }
    		}	
    	return false;
    }

    /**
     * Finds the first child of the given element with the given name. Returns
     * null if none found. We should move this to XPath when we gain that
     * facility.
     */
    private IVEXElement findChild(IVEXElement parent, String childName) {
        IVEXElement[] children = parent.getChildElements();
        for (int i = 0; i < children.length; i++) {
            IVEXElement child = children[i];
            if (child.getName().equals(childName)) {
                return child;
            }
        }
        return null;
    }
    
    private final static int MAPSIZE = 101; // prime greater than 65/.80
    
    private final static HashSet titledElements = new HashSet(MAPSIZE);
    
    static {
 // Comment on each line indicates tags content model if different than title
 // We are only interested in location of title tag within Content Model
 // so any extra following content is ignored.

// In general, it is impossible to keep this sort of table accurate and 
// up-to-date for more than the most basic of cases. It would be better 
// to extract this information from the DTD for the document.

// For the moment, it is observed that this imperfect method works for 
// vast majority of DocBook documents.

    	titledElements.add("abstract"); // title?
    	titledElements.add("article"); 
    //	titledElements.add("articleinfo"); 
    	titledElements.add("appendix"); 
    //	titledElements.add("authorblurb"); // title?
    //	titledElements.add("bibliodiv");
    //	titledElements.add("calloutlist");
    //	titledElements.add("caution"); // title?
    	titledElements.add("chapter"); // docinfo?, title
    //	titledElements.add("colophon");
    //	titledElements.add("constraintdef"); 
    //	titledElements.add("dedication");
    	
    //	titledElements.add("equation"); // blockinfo?, title
    //	titledElements.add("example"); // blockinfo?, title
    //	titledElements.add("figure"); // blockinfo?, title
    	titledElements.add("glossary"); // glossaryinfo?, title
    //	titledElements.add("glossdiv"); 
    //	titledElements.add("glosslist"); // blockinfo?, title
    //	titledElements.add("important"); // title?
    //	titledElements.add("index"); // indexinfo, title
    //	titledElements.add("indexdiv");
    //	titledElements.add("itemizedlist"); // blockinfo?, title
    	
    //	titledElements.add("legalnotice"); // blockinfo?, title
    //	titledElements.add("lot"); // beginpage?, title
    //	titledElements.add("msg"); // title?
    //	titledElements.add("msgexplan"); // title?
    //	titledElements.add("msgmain"); // title?
    //	titledElements.add("msgrel"); // title?
    //	titledElements.add("msgset");
    //	titledElements.add("msgsub"); // title?
    //	titledElements.add("note"); // title?
    //	titledElements.add("orderedlist"); // blockinfo?, title
    	
    	titledElements.add("part"); // beginpage?, partinfo?, title
    	titledElements.add("partintro"); 
    //	titledElements.add("personblurb"); // title?
    	titledElements.add("preface"); // beginpage?, prefaceinfo?, title
    //	titledElements.add("procedure"); // blockinfo?, title
    //	titledElements.add("productionset");
    //	titledElements.add("qandadiv"); // blockinfo?, title
    //	titledElements.add("qandaset"); // blockinfo?, title
    //	titledElements.add("reference"); // beginpage?, referenceinfo?, title
    //	titledElements.add("refsect1"); // refsect1info?, title
    	
    //	titledElements.add("refsect2"); // refsect2info, title
    //	titledElements.add("refsect3"); // refsect3info, title
    //	titledElements.add("refsection"); // refsectioninfo?, title
    //	titledElements.add("refsynopsisdiv"); // refsynopsisdivinfo?, title
    	titledElements.add("sect1"); // sect1info?, title
    	titledElements.add("sect2"); // sect2info?, title
       	titledElements.add("sect3"); // sect3info?, title
       	titledElements.add("sect4"); // sect4info?, title
       	titledElements.add("sect5"); // sect5info?, title
    	titledElements.add("section"); // sectioninfo?, title
    	
    //	titledElements.add("segmentedlist"); 
    //	titledElements.add("set");
    //	titledElements.add("setindex"); // setindexinfo?, title
    //	titledElements.add("sidebar"); // sidebarinfo?, title
    	titledElements.add("simplesect");
    //	titledElements.add("step"); // table?
    //	titledElements.add("table"); // blockinfo?, title
    //	titledElements.add("task"); // blockinfo?, title
    //	titledElements.add("taskprerequisites");// blockinfo?, title
    //	titledElements.add("taskrelated"); // blockinfo?, title
    	
    //	titledElements.add("tasksummary"); // blockinfo?, title
    //	titledElements.add("tip"); // title?
    	titledElements.add("toc"); // beginpage?, title
    //	titledElements.add("variablelist"); // blockinfo?, title
    //	titledElements.add("warning"); // title?
    }

}

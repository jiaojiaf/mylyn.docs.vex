package org.eclipse.jst.jsf.facelet.core.internal.tagmodel;


/**
 * A facelet tag with no information about it than its name
 * 
 * @author cbateman
 *
 */
public final class NoArchetypeFaceletTag extends FaceletTag {

    /**
     * 
     */
    private static final long serialVersionUID = 4810723162936027305L;

    public NoArchetypeFaceletTag(final String uri, final String name) {
        super(uri, name, TagType.HANDLER, null);
    }

}

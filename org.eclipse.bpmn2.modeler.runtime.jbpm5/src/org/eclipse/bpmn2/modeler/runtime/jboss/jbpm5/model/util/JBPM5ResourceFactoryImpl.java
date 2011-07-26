package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5.model.util;

import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerResourceFactoryImpl;
import org.eclipse.bpmn2.util.OnlyContainmentTypeInfo;
import org.eclipse.bpmn2.util.XmlExtendedMetadata;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.ElementHandlerImpl;

/**
 * The <b>Resource Factory</b> for the BPMN2 Modeler. Constructs a specialized BPMN2 Resource
 * which enhances the base Resource by adding ID lookup to sourceRef and targetRef object references. 
 * @see org.eclipse.bpmn2.util.Bpmn2ResourceImpl
 */
public class JBPM5ResourceFactoryImpl extends Bpmn2ModelerResourceFactoryImpl {
    /**
     * Creates an instance of the resource factory.
     */
    public JBPM5ResourceFactoryImpl() {
        super();
    }

    /**
     * Creates an instance of the resource.
     */
    @Override
    public Resource createResource(URI uri) {
        JBPM5ResourceImpl result = new JBPM5ResourceImpl(uri);
        ExtendedMetaData extendedMetadata = new XmlExtendedMetadata();
        result.getDefaultSaveOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetadata);
        result.getDefaultLoadOptions().put(XMLResource.OPTION_EXTENDED_META_DATA, extendedMetadata);

        result.getDefaultSaveOptions().put(XMLResource.OPTION_SAVE_TYPE_INFORMATION,
                new OnlyContainmentTypeInfo());

        result.getDefaultLoadOptions().put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE,
                Boolean.TRUE);
        result.getDefaultSaveOptions().put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE,
                Boolean.TRUE);

        result.getDefaultLoadOptions().put(XMLResource.OPTION_USE_LEXICAL_HANDLER, Boolean.TRUE);

        result.getDefaultSaveOptions().put(XMLResource.OPTION_ELEMENT_HANDLER,
                new ElementHandlerImpl(true));

        result.getDefaultSaveOptions().put(XMLResource.OPTION_ENCODING, "UTF-8");

        return result;
    }

}
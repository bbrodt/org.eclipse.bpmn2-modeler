/**
 * <copyright>
 * 
 * Copyright (c) 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Reiner Hille-Doering (SAP AG) - initial API and implementation and/or initial documentation
 * 
 * </copyright>
 */
package org.eclipse.bpmn2.modeler.runtime.jboss.jbpm5;

import java.util.Map;

import org.eclipse.bpmn2.modeler.core.utils.Bpmn2ModelerResourceImpl;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * <!-- begin-user-doc -->
 * The <b>Resource </b> associated with the package.
 * @implements Bpmn2Resource
 * <!-- end-user-doc -->
 * @see org.eclipse.bpmn2.util.Bpmn2ResourceFactoryImpl
 * @generated
 */
public class JBPM5ResourceImpl extends Bpmn2ModelerResourceImpl {

    /**
     * Creates an instance of the resource.
     * @param uri the URI of the new resource.
     */
    public JBPM5ResourceImpl(URI uri) {
        super(uri);
    }

    /**
     * We must override this method for having an own XMLHandler
     */
    @Override
    protected XMLLoad createXMLLoad() {
        return new XMLLoadImpl(createXMLHelper()) {
            @Override
            protected DefaultHandler makeDefaultHandler() {
                return new JBPM5ModelerXmlHandler(resource, helper, options);
            }
        };
    }

    /**
     * We need extend the standard SAXXMLHandler to hook into the handling of attribute references
     * which may be either simple ID Strings or QNames. We'll search through all of the objects'
     * IDs first to find the one we're looking for. If not, we'll try a QName search.
     */
    protected static class JBPM5ModelerXmlHandler extends BpmnXmlHandler {

        public JBPM5ModelerXmlHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
            super(xmiResource, helper, options);
        }

        /**
         * Overridden to be able to convert ID references in attributes to URIs during load.
         * If the reference can't be found by its ID, we'll try a QName search (done in the
         * super class)
         * @param ids
         *  In our case the parameter will contain exactly one ID that we resolve to URI.
         */
        @Override
        protected void setValueFromId(EObject object, EReference eReference, String ids) {

            for (EObject o : objects) {
                TreeIterator<EObject> iter = o.eAllContents();
                while (iter.hasNext()) {
                    EObject obj = iter.next();
                    EStructuralFeature feature = ((EObject) obj).eClass().getEIDAttribute();
                    if (feature != null && obj.eGet(feature) != null) {
                        Object id = obj.eGet(feature);
                        if (id!=null && id.equals(ids)) {
                        	System.out.println(obj.eClass().getName() + " id=" + id);
                        	object.eSet(eReference,obj);
                        	return;
                        }
                    }
                }
            }

            super.setValueFromId(object,eReference,ids);
        }
    }
}

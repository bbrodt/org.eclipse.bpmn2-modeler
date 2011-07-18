/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 *  All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.eclipse.bpmn2.modeler.core.features.data;

import java.io.IOException;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.modeler.core.Activator;
import org.eclipse.bpmn2.modeler.core.ModelHandler;
import org.eclipse.bpmn2.modeler.core.ModelHandlerLocator;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.impl.AbstractCreateFeature;

public abstract class AbstractCreateDataInputOutputFeature extends AbstractCreateFeature {

	public AbstractCreateDataInputOutputFeature(IFeatureProvider fp, String name, String description) {
		super(fp, name, description);
	}

	@Override
	public boolean canCreate(ICreateContext context) {
		return true;
	}

	@Override
	public Object[] create(ICreateContext context) {
		BaseElement element = null;
		try {
			ModelHandler handler = ModelHandlerLocator.getModelHandler(getDiagram().eResource());
			element = add(context.getTargetContainer(), handler);
		} catch (IOException e) {
			Activator.logError(e);
		}
		addGraphicalRepresentation(context, element);
		return new Object[] { element };
	}

	public abstract <T extends BaseElement> T add(Object target, ModelHandler handler);

	public abstract String getStencilImageId();

	@Override
	public String getCreateImageId() {
		return getStencilImageId();
	}

	@Override
	public String getCreateLargeImageId() {
		return getStencilImageId();
	}
}
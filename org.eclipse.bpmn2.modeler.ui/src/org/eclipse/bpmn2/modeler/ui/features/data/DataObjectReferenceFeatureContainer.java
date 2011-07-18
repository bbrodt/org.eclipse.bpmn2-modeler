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
package org.eclipse.bpmn2.modeler.ui.features.data;

import org.eclipse.bpmn2.DataObjectReference;
import org.eclipse.bpmn2.modeler.core.ModelHandler;
import org.eclipse.bpmn2.modeler.core.features.AbstractCreateFlowElementFeature;
import org.eclipse.bpmn2.modeler.core.features.UpdateBaseElementNameFeature;
import org.eclipse.bpmn2.modeler.core.features.data.AddDataFeature;
import org.eclipse.bpmn2.modeler.ui.ImageProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.ICreateContext;

public class DataObjectReferenceFeatureContainer extends AbstractDataFeatureContainer {

	@Override
	public boolean canApplyTo(Object o) {
		return super.canApplyTo(o) && o instanceof DataObjectReference;
	}

	@Override
	public ICreateFeature getCreateFeature(IFeatureProvider fp) {
		return new CreateDataObjectReferenceFeature(fp);
	}

	@Override
	public IAddFeature getAddFeature(IFeatureProvider fp) {
		return new AddDataFeature<DataObjectReference>(fp) {

			@Override
			public String getName(DataObjectReference t) {
				return t.getName();
			}
		};
	}

	@Override
	public IUpdateFeature getUpdateFeature(IFeatureProvider fp) {
		return new UpdateBaseElementNameFeature(fp);
	}

	public static class CreateDataObjectReferenceFeature extends AbstractCreateFlowElementFeature<DataObjectReference> {

		public CreateDataObjectReferenceFeature(IFeatureProvider fp) {
			super(fp, "Data Object Ref",
					"Provides ref information about what activities require to be performed or what they produce");
		}

		@Override
		protected DataObjectReference createFlowElement(ICreateContext context) {
			DataObjectReference ref = ModelHandler.FACTORY.createDataObjectReference();
			ref.setName("Data Object Ref");
			return ref;
		}

		@Override
		public String getCreateImageId() {
			return ImageProvider.IMG_16_DATA_OBJECT;
		}

		@Override
		public String getCreateLargeImageId() {
			return getCreateImageId();
		}
	}
}
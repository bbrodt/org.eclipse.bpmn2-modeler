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
package org.eclipse.bpmn2.modeler.ui.features.activity.task;

import org.eclipse.bpmn2.ManualTask;
import org.eclipse.bpmn2.Task;
import org.eclipse.bpmn2.modeler.core.ModelHandler;
import org.eclipse.bpmn2.modeler.core.features.activity.task.AbstractCreateTaskFeature;
import org.eclipse.bpmn2.modeler.core.features.activity.task.AddTaskFeature;
import org.eclipse.bpmn2.modeler.core.utils.GraphicsUtil;
import org.eclipse.bpmn2.modeler.ui.ImageProvider;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

public class ManualTaskFeatureContainer extends AbstractTaskFeatureContainer {

	@Override
	public boolean canApplyTo(Object o) {
		return super.canApplyTo(o) && o instanceof ManualTask;
	}

	@Override
	public ICreateFeature getCreateFeature(IFeatureProvider fp) {
		return new CreateManualTaskFeature(fp);
	}

	@Override
	public IAddFeature getAddFeature(IFeatureProvider fp) {
		return new AddTaskFeature(fp) {
			@Override
			protected void decorateActivityRectangle(RoundedRectangle rect) {
				IGaService service = Graphiti.getGaService();
				Image img = service.createImage(rect, ImageProvider.IMG_16_MANUAL_TASK);
				service.setLocationAndSize(img, 2, 2, GraphicsUtil.TASK_IMAGE_SIZE, GraphicsUtil.TASK_IMAGE_SIZE);
			}
		};
	}

	public static class CreateManualTaskFeature extends AbstractCreateTaskFeature {

		public CreateManualTaskFeature(IFeatureProvider fp) {
			super(fp, "Manual Task",
					"Task that is expected to perform without the aid of any business process execution engine or any application");
		}

		@Override
		protected Task createFlowElement(ICreateContext context) {
			ManualTask task = ModelHandler.FACTORY.createManualTask();
			task.setName("Manual Task");
			return task;
		}

		@Override
		protected String getStencilImageId() {
			return ImageProvider.IMG_16_MANUAL_TASK;
		}
	}
}
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
package org.eclipse.bpmn2.modeler.core.features;

import org.eclipse.bpmn2.di.BPMNShape;
import org.eclipse.bpmn2.modeler.core.di.DIUtils;
import org.eclipse.bpmn2.modeler.core.utils.AnchorUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IResizeShapeContext;
import org.eclipse.graphiti.features.impl.DefaultResizeShapeFeature;
import org.eclipse.graphiti.mm.pictograms.Shape;

public class DefaultBPMNResizeFeature extends DefaultResizeShapeFeature {

	public DefaultBPMNResizeFeature(IFeatureProvider fp) {
		super(fp);
	}

	@Override
	public void resizeShape(IResizeShapeContext context) {
		super.resizeShape(context);
		if (context.getPictogramElement() instanceof Shape) {
			Shape shape = (Shape) context.getPictogramElement();
			AnchorUtil.relocateFixPointAnchors(shape, context.getWidth(), context.getHeight());
			Object[] node = getAllBusinessObjectsForPictogramElement(context.getShape());
			for (Object object : node) {
				if (object instanceof BPMNShape) {
					BPMNShape s = (BPMNShape) object;
					AnchorUtil.reConnect(s, getDiagram());
				}
			}
		}
		DIUtils.updateDIShape(context.getPictogramElement());
	}
}
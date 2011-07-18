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
package org.eclipse.bpmn2.modeler.core.features.artifact;

import org.eclipse.bpmn2.TextAnnotation;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;

public class DirectEditTextAnnotationFeature extends AbstractDirectEditingFeature {

	public DirectEditTextAnnotationFeature(IFeatureProvider fp) {
	    super(fp);
    }

	@Override
    public int getEditingType() {
		return TYPE_TEXT;
    }

	@Override
    public String getInitialValue(IDirectEditingContext context) {
		PictogramElement pe = context.getPictogramElement();
		TextAnnotation annotation = (TextAnnotation) getBusinessObjectForPictogramElement(pe);
		return annotation.getText();
	}

	@Override
    public void setValue(String value, IDirectEditingContext context) {
		PictogramElement pe = context.getPictogramElement();
		TextAnnotation annotation = (TextAnnotation) getBusinessObjectForPictogramElement(pe);
		annotation.setText(value);
		updatePictogramElement(((Shape) pe).getContainer());
    }
	
	@Override
	public boolean canDirectEdit(IDirectEditingContext context) {
		PictogramElement pe = context.getPictogramElement();
		Object bo = getBusinessObjectForPictogramElement(pe);
		GraphicsAlgorithm ga = context.getGraphicsAlgorithm();
		return bo instanceof TextAnnotation && ga instanceof MultiText;
	}
}

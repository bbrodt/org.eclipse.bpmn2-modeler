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
package org.eclipse.bpmn2.modeler.ui.features.flow;

import java.util.Iterator;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.ComplexGateway;
import org.eclipse.bpmn2.ExclusiveGateway;
import org.eclipse.bpmn2.FlowNode;
import org.eclipse.bpmn2.InclusiveGateway;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.di.BPMNEdge;
import org.eclipse.bpmn2.modeler.core.Activator;
import org.eclipse.bpmn2.modeler.core.ModelHandler;
import org.eclipse.bpmn2.modeler.core.features.BaseElementConnectionFeatureContainer;
import org.eclipse.bpmn2.modeler.core.features.BaseElementReconnectionFeature;
import org.eclipse.bpmn2.modeler.core.features.BusinessObjectUtil;
import org.eclipse.bpmn2.modeler.core.features.MultiUpdateFeature;
import org.eclipse.bpmn2.modeler.core.features.UpdateBaseElementNameFeature;
import org.eclipse.bpmn2.modeler.core.features.flow.AbstractAddFlowFeature;
import org.eclipse.bpmn2.modeler.core.features.flow.AbstractCreateFlowFeature;
import org.eclipse.bpmn2.modeler.core.utils.StyleUtil;
import org.eclipse.bpmn2.modeler.core.utils.Tuple;
import org.eclipse.bpmn2.modeler.ui.ImageProvider;
import org.eclipse.dd.di.DiagramElement;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.IDeleteFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.IReconnectionFeature;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.IReconnectionContext;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.context.impl.ReconnectionContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.Color;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeService;
import org.eclipse.graphiti.util.IColorConstant;

public class SequenceFlowFeatureContainer extends BaseElementConnectionFeatureContainer {

	private static final String IS_DEFAULT_FLOW_PROPERTY = "is.default.flow";
	private static final String IS_CONDITIONAL_FLOW_PROPERTY = "is.conditional.flow";
	private static final String DEFAULT_MARKER_PROPERTY = "default.marker";
	private static final String CONDITIONAL_MARKER_PROPERTY = "conditional.marker";

	@Override
	public boolean canApplyTo(Object o) {
		return super.canApplyTo(o) && o instanceof SequenceFlow;
	}

	@Override
	public IAddFeature getAddFeature(IFeatureProvider fp) {
		return new AbstractAddFlowFeature(fp) {
			@Override
			protected Class<? extends BaseElement> getBoClass() {
				return SequenceFlow.class;
			}

			@Override
			protected void createConnectionDecorators(Connection connection) {
				int w = 3;
				int l = 8;
				
				ConnectionDecorator decorator = Graphiti.getPeService().createConnectionDecorator(connection, false,
						1.0, true);

				IGaService gaService = Graphiti.getGaService();
				Polyline arrow = gaService.createPolygon(decorator, new int[] { -l, w, 0, 0, -l, -w, -l, w });

				arrow.setForeground(manageColor(StyleUtil.CLASS_FOREGROUND));
				arrow.setLineWidth(2);
			}

			@Override
			protected void hook(IAddContext context, Connection connection, BaseElement element) {
				setDefaultSequenceFlow(connection);
				setConditionalSequenceFlow(connection);
			}
		};
	}

	@Override
	public ICreateConnectionFeature getCreateConnectionFeature(IFeatureProvider fp) {
		return new CreateSequenceFlowFeature(fp);
	}

	@Override
	public IUpdateFeature getUpdateFeature(IFeatureProvider fp) {
		MultiUpdateFeature multiUpdate = new MultiUpdateFeature(fp);
		multiUpdate.addUpdateFeature(new UpdateDefaultSequenceFlowFeature(fp));
		multiUpdate.addUpdateFeature(new UpdateConditionalSequenceFlowFeature(fp));
		multiUpdate.addUpdateFeature(new UpdateBaseElementNameFeature(fp));
		return multiUpdate;
	}

	@Override
	public IReconnectionFeature getReconnectionFeature(IFeatureProvider fp) {
		return new SequenceFlowReconnectionFeature(fp);
	}

	@Override
	public IDeleteFeature getDeleteFeature(IFeatureProvider fp) {
		return null;
	}

	public static class CreateSequenceFlowFeature extends AbstractCreateFlowFeature<FlowNode, FlowNode> {

		public CreateSequenceFlowFeature(IFeatureProvider fp) {
			super(fp, "Sequence Flow",
					"A Sequence Flow is used to show the order that Activities will be performed in a Process");
		}

		@Override
		protected String getStencilImageId() {
			return ImageProvider.IMG_16_SEQUENCE_FLOW;
		}

		@Override
		protected BaseElement createFlow(ModelHandler mh, FlowNode source, FlowNode target) {
			SequenceFlow flow = mh.createSequenceFlow(source, target);
			flow.setName("");
			return flow;
		}

		@Override
		protected Class<FlowNode> getSourceClass() {
			return FlowNode.class;
		}

		@Override
		protected Class<FlowNode> getTargetClass() {
			return FlowNode.class;
		}
	}

	private static Color manageColor(PictogramElement element, IColorConstant colorConstant) {
		IPeService peService = Graphiti.getPeService();
		Diagram diagram = peService.getDiagramForPictogramElement(element);
		return Graphiti.getGaService().manageColor(diagram, colorConstant);
	}
	
	private static void setDefaultSequenceFlow(Connection connection) {
		IPeService peService = Graphiti.getPeService();
		SequenceFlow flow = BusinessObjectUtil.getFirstElementOfType(connection, SequenceFlow.class);
		SequenceFlow defaultFlow = getDefaultFlow(flow.getSourceRef());
		boolean isDefault = defaultFlow == null ? false : defaultFlow.equals(flow);

		Tuple<ConnectionDecorator, ConnectionDecorator> decorators = getConnectionDecorators(connection);
		ConnectionDecorator def = decorators.getFirst();
		ConnectionDecorator cond = decorators.getSecond();

		if (isDefault) {
			if (cond != null) {
				peService.deletePictogramElement(cond);
			}
			def = createDefaultConnectionDecorator(connection);
			GraphicsAlgorithm ga = def.getGraphicsAlgorithm();
			ga.setForeground(manageColor(connection,StyleUtil.CLASS_FOREGROUND));
		} else {
			if (def != null) {
				peService.deletePictogramElement(def);
			}
			if (flow.getConditionExpression() != null && flow.getSourceRef() instanceof Activity) {
				cond = createConditionalConnectionDecorator(connection);
				GraphicsAlgorithm ga = cond.getGraphicsAlgorithm();
				ga.setFilled(true);
				ga.setForeground(manageColor(connection,StyleUtil.CLASS_FOREGROUND));
				ga.setBackground(manageColor(connection,IColorConstant.WHITE));
			}
		}

		peService.setPropertyValue(connection, IS_DEFAULT_FLOW_PROPERTY,
				Boolean.toString(isDefault));

	}
	
	public static class UpdateDefaultSequenceFlowFeature extends AbstractUpdateFeature {

		public UpdateDefaultSequenceFlowFeature(IFeatureProvider fp) {
			super(fp);
		}

		@Override
		public boolean canUpdate(IUpdateContext context) {
			SequenceFlow flow = BusinessObjectUtil.getFirstElementOfType(context.getPictogramElement(),
					SequenceFlow.class);
			boolean canUpdate = flow != null && isDefaultAttributeSupported(flow.getSourceRef());
			return canUpdate;
		}

		@Override
		public IReason updateNeeded(IUpdateContext context) {
			IPeService peService = Graphiti.getPeService();
			String property = peService.getPropertyValue(context.getPictogramElement(), IS_DEFAULT_FLOW_PROPERTY);
			if (property == null || !canUpdate(context)) {
				return Reason.createFalseReason();
			}
			SequenceFlow flow = BusinessObjectUtil.getFirstElementOfType(context.getPictogramElement(),
					SequenceFlow.class);
			SequenceFlow defaultFlow = getDefaultFlow(flow.getSourceRef());
			boolean isDefault = defaultFlow == null ? false : defaultFlow.equals(flow);
			boolean changed = isDefault != new Boolean(property);
			return changed ? Reason.createTrueReason() : Reason.createFalseReason();
		}

		@Override
		public boolean update(IUpdateContext context) {
			Connection connection = (Connection) context.getPictogramElement();
			setDefaultSequenceFlow(connection);
			return true;
		}
	}

	private static void setConditionalSequenceFlow(Connection connection) {
		IPeService peService = Graphiti.getPeService();
		SequenceFlow flow = BusinessObjectUtil.getFirstElementOfType(connection, SequenceFlow.class);

		Tuple<ConnectionDecorator, ConnectionDecorator> decorators = getConnectionDecorators(connection);
		ConnectionDecorator def = decorators.getFirst();
		ConnectionDecorator cond = decorators.getSecond();

		if (flow.getConditionExpression() != null && flow.getSourceRef() instanceof Activity && def == null) {
			ConnectionDecorator decorator = createConditionalConnectionDecorator(connection);
			GraphicsAlgorithm ga = decorator.getGraphicsAlgorithm();
			ga.setFilled(true);
			ga.setForeground(manageColor(connection, StyleUtil.CLASS_FOREGROUND));
			ga.setBackground(manageColor(connection, IColorConstant.WHITE));
		} else if (cond != null) {
			peService.deletePictogramElement(cond);
		}

		peService.setPropertyValue(connection, IS_CONDITIONAL_FLOW_PROPERTY,
				Boolean.toString(flow.getConditionExpression() != null));
	}
	
	public static class UpdateConditionalSequenceFlowFeature extends AbstractUpdateFeature {

		public UpdateConditionalSequenceFlowFeature(IFeatureProvider fp) {
			super(fp);
		}

		@Override
		public boolean canUpdate(IUpdateContext context) {
			SequenceFlow flow = BusinessObjectUtil.getFirstElementOfType(context.getPictogramElement(),
					SequenceFlow.class);
			boolean canUpdate = flow != null && flow.getSourceRef() instanceof Activity;
			return canUpdate;
		}

		@Override
		public IReason updateNeeded(IUpdateContext context) {
			// NOTE: if this method returns "true" the very first time it's called by the refresh
			// framework, the connection line will be drawn as a red dotted line, so make sure
			// all properties have been correctly set to their initial values in the AddFeature!
			// see https://issues.jboss.org/browse/JBPM-3102
			IPeService peService = Graphiti.getPeService();
			Connection connection = (Connection) context.getPictogramElement();
			SequenceFlow flow = BusinessObjectUtil.getFirstElementOfType(connection, SequenceFlow.class);
			String property = peService.getPropertyValue(connection, IS_CONDITIONAL_FLOW_PROPERTY);
			if (property == null || !canUpdate(context)) {
				return Reason.createFalseReason();
			}
			boolean changed = flow.getConditionExpression() != null != new Boolean(property);
			return changed ? Reason.createTrueReason() : Reason.createFalseReason();
		}

		@Override
		public boolean update(IUpdateContext context) {
			Connection connection = (Connection) context.getPictogramElement();
			setConditionalSequenceFlow(connection);
			return true;
		}
	}
	
	public static class SequenceFlowReconnectionFeature extends BaseElementReconnectionFeature {

		public SequenceFlowReconnectionFeature(IFeatureProvider fp) {
			super(fp);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean canReconnect(IReconnectionContext context) {
			if (super.canReconnect(context)) {
				BaseElement targetElement = BusinessObjectUtil.getFirstElementOfType(context.getTargetPictogramElement(), BaseElement.class);
				return targetElement instanceof FlowNode;
			}
			return false;
		}

//		@Override
//		public void postReconnect(IReconnectionContext context) {
//			super.postReconnect(context);
//			SequenceFlow flow = BusinessObjectUtil.getFirstElementOfType(context.getConnection(), SequenceFlow.class);
//			FlowNode targetElement = BusinessObjectUtil.getFirstElementOfType(context.getTargetPictogramElement(), FlowNode.class);
//			if (context.getReconnectType().equals(ReconnectionContext.RECONNECT_TARGET)) {
//				flow.setTargetRef(targetElement);
//			}
//			else {
//				flow.setSourceRef(targetElement);
//			}
//		}
	}
	
	private static boolean isDefaultAttributeSupported(FlowNode node) {
		if (node instanceof Activity) {
			return true;
		}
		if (node instanceof ExclusiveGateway || node instanceof InclusiveGateway || node instanceof ComplexGateway) {
			return true;
		}
		return false;
	}

	private static SequenceFlow getDefaultFlow(FlowNode node) {
		if (isDefaultAttributeSupported(node)) {
			try {
				return (SequenceFlow) node.getClass().getMethod("getDefault").invoke(node);
			} catch (Exception e) {
				Activator.logError(e);
			}
		}
		return null;
	}

	private static Tuple<ConnectionDecorator, ConnectionDecorator> getConnectionDecorators(Connection connection) {
		IPeService peService = Graphiti.getPeService();

		ConnectionDecorator defaultDecorator = null;
		ConnectionDecorator conditionalDecorator = null;

		Iterator<ConnectionDecorator> iterator = connection.getConnectionDecorators().iterator();
		while (iterator.hasNext()) {
			ConnectionDecorator connectionDecorator = iterator.next();
			String defProp = peService.getPropertyValue(connectionDecorator, DEFAULT_MARKER_PROPERTY);
			if (defProp != null && new Boolean(defProp)) {
				defaultDecorator = connectionDecorator;
				continue;
			}
			String condProp = peService.getPropertyValue(connectionDecorator, CONDITIONAL_MARKER_PROPERTY);
			if (condProp != null && new Boolean(condProp)) {
				conditionalDecorator = connectionDecorator;
			}
		}

		return new Tuple<ConnectionDecorator, ConnectionDecorator>(defaultDecorator, conditionalDecorator);
	}

	private static ConnectionDecorator createDefaultConnectionDecorator(Connection connection) {
		ConnectionDecorator marker = Graphiti.getPeService().createConnectionDecorator(connection, false, 0.0, true);
		Graphiti.getGaService().createPolyline(marker, new int[] { -5, 5, -10, -5 });
		Graphiti.getPeService().setPropertyValue(marker, DEFAULT_MARKER_PROPERTY, Boolean.toString(true));
		return marker;
	}

	private static ConnectionDecorator createConditionalConnectionDecorator(Connection connection) {
		ConnectionDecorator marker = Graphiti.getPeService().createConnectionDecorator(connection, false, 0.0, true);
		Graphiti.getGaService().createPolygon(marker, new int[] { -15, 0, -7, 5, 0, 0, -7, -5 });
		Graphiti.getPeService().setPropertyValue(marker, CONDITIONAL_MARKER_PROPERTY, Boolean.toString(true));
		return marker;
	}
}
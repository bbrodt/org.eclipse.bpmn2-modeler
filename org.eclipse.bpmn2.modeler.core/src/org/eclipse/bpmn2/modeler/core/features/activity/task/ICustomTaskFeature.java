package org.eclipse.bpmn2.modeler.core.features.activity.task;

import org.eclipse.bpmn2.modeler.core.runtime.CustomTaskDescriptor;
import org.eclipse.graphiti.features.IFeatureProvider;

public interface ICustomTaskFeature {

	public final static String CUSTOM_TASK_ID = "custom.task.id";

	public abstract void setId(String id);

	public abstract String getId();

	public abstract void setCustomTaskDescriptor(CustomTaskDescriptor customTaskDescriptor);

}
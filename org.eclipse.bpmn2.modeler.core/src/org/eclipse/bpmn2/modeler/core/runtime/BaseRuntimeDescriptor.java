package org.eclipse.bpmn2.modeler.core.runtime;

public class BaseRuntimeDescriptor {
	
	protected TargetRuntime targetRuntime;
	
	public TargetRuntime getRuntime() {
		return targetRuntime;
	}

	public void setRuntime(TargetRuntime targetRuntime) {
		this.targetRuntime = targetRuntime;
	}
}
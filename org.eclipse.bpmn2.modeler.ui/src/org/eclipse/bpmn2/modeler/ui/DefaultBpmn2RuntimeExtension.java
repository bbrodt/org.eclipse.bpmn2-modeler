package org.eclipse.bpmn2.modeler.ui;
import org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension;
import org.eclipse.core.resources.IFile;


public class DefaultBpmn2RuntimeExtension implements IBpmn2RuntimeExtension {

	public DefaultBpmn2RuntimeExtension() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isContentForRuntime(IFile file) {
		// TODO Auto-generated method stub
		return false;
	}

}

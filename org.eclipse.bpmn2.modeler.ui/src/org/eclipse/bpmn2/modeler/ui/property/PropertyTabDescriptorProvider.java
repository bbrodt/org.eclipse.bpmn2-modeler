package org.eclipse.bpmn2.modeler.ui.property;

import java.util.List;

import org.eclipse.bpmn2.modeler.core.TargetRuntime;
import org.eclipse.bpmn2.modeler.core.TargetRuntime.Bpmn2TabDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptorProvider;

public class PropertyTabDescriptorProvider implements ITabDescriptorProvider {

	public PropertyTabDescriptorProvider() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public ITabDescriptor[] getTabDescriptors(IWorkbenchPart part,
			ISelection selection) {
		TargetRuntime rt = TargetRuntime.getRuntime();
		List<Bpmn2TabDescriptor> desc = rt.getTabDescriptors();
		return desc.toArray(new ITabDescriptor[desc.size()]);
	}

}

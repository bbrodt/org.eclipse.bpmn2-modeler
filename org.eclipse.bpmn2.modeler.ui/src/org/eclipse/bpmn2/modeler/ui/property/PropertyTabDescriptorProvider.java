package org.eclipse.bpmn2.modeler.ui.property;

import java.util.List;

import org.eclipse.bpmn2.modeler.core.preferences.TargetRuntime;
import org.eclipse.bpmn2.modeler.core.preferences.TargetRuntime.Bpmn2TabDescriptor;
import org.eclipse.bpmn2.modeler.ui.editor.BPMN2Editor;
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
		
		TargetRuntime rt = TargetRuntime.getDefaultRuntime();
		if (part instanceof BPMN2Editor) {
			rt = ((BPMN2Editor)part).getTargetRuntime();
		}
		List<Bpmn2TabDescriptor> desc = rt.getTabDescriptors();
		return desc.toArray(new ITabDescriptor[desc.size()]);
	}

}

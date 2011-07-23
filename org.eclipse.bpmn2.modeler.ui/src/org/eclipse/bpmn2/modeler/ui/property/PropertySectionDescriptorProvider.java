package org.eclipse.bpmn2.modeler.ui.property;

import java.util.List;

import org.eclipse.bpmn2.modeler.core.TargetRuntime;
import org.eclipse.bpmn2.modeler.core.TargetRuntime.Bpmn2SectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptorProvider;

public class PropertySectionDescriptorProvider implements
		ISectionDescriptorProvider {

	public PropertySectionDescriptorProvider() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public ISectionDescriptor[] getSectionDescriptors() {
		TargetRuntime rt = TargetRuntime.getRuntime();
		List<Bpmn2SectionDescriptor> desc = rt.getSectionDescriptors();
		return desc.toArray(new ISectionDescriptor[desc.size()]);
	}

}

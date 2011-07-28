package org.eclipse.bpmn2.modeler.core.preferences;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

public interface IBpmn2PropertySection {
	
	public boolean appliesTo(IWorkbenchPart part, ISelection selection);

}

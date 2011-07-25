package org.eclipse.bpmn2.modeler.core;

import org.eclipse.core.resources.IFile;

public interface IBpmn2RuntimeExtension {

	/**
	 * Check if the given input file is specific to the runtime environment.
	 * The implementation should check for specific extensions and namespaces that identify
	 * the file for this runtime.
	 *  
	 * @param file
	 * @return true if the file is targeted for this runtime, false if the file is generic BPMN 2.0
	 */
	public boolean isContentForRuntime(IFile file);
}

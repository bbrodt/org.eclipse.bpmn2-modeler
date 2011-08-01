package org.eclipse.bpmn2.modeler.core.runtime;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;

public class ModelDescriptor extends BaseRuntimeDescriptor {
	
	protected EPackage ePackage;
	protected EFactory eFactory;
	protected ResourceFactoryImpl resourceFactory;
	
	public EFactory getEFactory() {
		return eFactory;
	}
	
	public ResourceFactoryImpl getResourceFactory() {
		return resourceFactory;
	}
	
	public EPackage getEPackage() {
		return ePackage;
	}

	public void setEPackage(EPackage ePackage) {
		this.ePackage = ePackage;
	}

	public void setEFactory(EFactory eFactory) {
		this.eFactory = eFactory;
	}

	public void setResourceFactory(ResourceFactoryImpl resourceFactory) {
		this.resourceFactory = resourceFactory;
	}
}
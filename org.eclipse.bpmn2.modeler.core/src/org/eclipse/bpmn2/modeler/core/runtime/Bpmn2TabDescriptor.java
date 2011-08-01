package org.eclipse.bpmn2.modeler.core.runtime;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.TabContents;

public class Bpmn2TabDescriptor extends AbstractTabDescriptor {

	protected String id;
	protected String category;
	protected String label;
	protected String afterTab = null;
	protected boolean indented = false;
	protected Image image = null;
	protected ArrayList<Bpmn2SectionDescriptor> unfilteredSectionDescriptors = null;
	
	public Bpmn2TabDescriptor(String id, String category, String label) {
		this.id = id;
		this.category = category;
		this.label = label;
	}
	
	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getAfterTab() {
		if (afterTab==null || afterTab.trim().length()==0)
			return super.getAfterTab();
		return afterTab;
	}

	@Override
	public Image getImage() {
		if (image==null)
			return super.getImage();
		return image;
	}

	@Override
	public List getSectionDescriptors() {
		if (unfilteredSectionDescriptors==null)
			return super.getSectionDescriptors();
		return unfilteredSectionDescriptors;
	}

	@Override
	public TabContents createTab() {
		// TODO Auto-generated method stub
		return super.createTab();
	}

	@Override
	public boolean isSelected() {
		// TODO Auto-generated method stub
		return super.isSelected();
	}

	@Override
	public void setSectionDescriptors(List sectionDescriptors) {
		// TODO Auto-generated method stub
		super.setSectionDescriptors(sectionDescriptors);
	}

	@Override
	public boolean isIndented() {
		return indented;
	}

	@Override
	public Object clone() {
		Bpmn2TabDescriptor clone = new Bpmn2TabDescriptor(id, category, label);
		clone.afterTab = this.afterTab;
		clone.image = this.image;
		clone.indented = this.indented;
		return clone;
	}
	
}
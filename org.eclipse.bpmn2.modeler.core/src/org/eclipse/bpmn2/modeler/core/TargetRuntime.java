package org.eclipse.bpmn2.modeler.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.modeler.core.utils.Bpmn2ModelerResourceImpl;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;


public class TargetRuntime extends AbstractPropertyChangeListenerProvider {

	// extension point ID for Target Runtimes
	public static final String RUNTIME_ID = "org.eclipse.bpmn2.modeler.runtime";
	public static final String DEFAULT_RUNTIME_ID = "org.eclipse.bpmn2.modeler.runtime.none";
	
	// our cached registry of target runtimes contributed by other plugins
	protected static TargetRuntime targetRuntimes[];
	protected static TargetRuntime currentRuntime;
	
	protected String name;
	protected String[] versions;
	protected String id;
	protected String description;
	protected IBpmn2RuntimeExtension runtimeExtension;
	protected ResourceFactoryImpl emfResourceFactory;
	protected ArrayList<Bpmn2TabDescriptor> tabDescriptors;
	protected ArrayList<Bpmn2SectionDescriptor> sectionDescriptors;
	
	public TargetRuntime(String id, String name, String versions, String description) {
		this.id = id;
		this.name = name;
		if (versions!=null)
			this.versions = versions.split("[, ]");
		this.description = description;
	}
	
	public static TargetRuntime getRuntime(String id) {
		getAllRuntimes();
		for (TargetRuntime rt : getAllRuntimes()) {
			if (rt.id.equals(id))
				return rt;
		}
		return null;
	}

	/**
	 * If the project has not been configured for a specific runtime through the "BPMN2"
	 * project properties page (i.e. the target is "None") then allow the runtime extension
	 * plug-ins an opportunity to identify the given process file contents as their own.
	 * 
	 * If none of the plug-ins respond with "yes, this file is targeted for my runtime",
	 * then use the "None" as the extension. This will configure the BPMN2 Modeler with
	 * generic property sheets and other default behavior.
	 * 
	 * @param file
	 * @return
	 */
	public static TargetRuntime getRuntime(IFile file) {
		getAllRuntimes();
		if (currentRuntime == getDefaultRuntime()) {
			for (TargetRuntime rt : targetRuntimes) {
				if (rt.runtimeExtension.isContentForRuntime(file)) {
					return rt;
				}
			}
		}
		else
			return currentRuntime;
		return getDefaultRuntime();
	}
	
	public static TargetRuntime getDefaultRuntime() {
		return getRuntime(DEFAULT_RUNTIME_ID);
	}
	
	public static void setRuntime(TargetRuntime rt) {
		currentRuntime = rt;
	}
	
	public static void setRuntime(String id) {
		currentRuntime = getRuntime(id);
		if (currentRuntime==null)
			currentRuntime = getRuntime(DEFAULT_RUNTIME_ID);
	}

	public void setResourceSet(ResourceSet resourceSet) {
		resourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap().put(
				Bpmn2ModelerResourceImpl.BPMN2_CONTENT_TYPE_ID, emfResourceFactory);
	}
	
	public static TargetRuntime[] getAllRuntimes() {
		if (targetRuntimes==null) {
			// load runtimes contributions from other plugins
			ArrayList<TargetRuntime> rtList = new ArrayList<TargetRuntime>();
			
			IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
					RUNTIME_ID);

			try {
				for (IConfigurationElement e : config) {
					if (e.getName().equals("runtime")) {
						String id = e.getAttribute("id");
						String name = e.getAttribute("name");
						String versions = e.getAttribute("versions");
						String description = e.getAttribute("description");
						TargetRuntime rt = new TargetRuntime(id,name,versions,description);
						
						rt.runtimeExtension = (IBpmn2RuntimeExtension) e.createExecutableExtension("class");
					
						rtList.add(rt);
					}
				}
				
				targetRuntimes = rtList.toArray(new TargetRuntime[rtList.size()]);
				
				for (IConfigurationElement e : config) {
					if (!e.getName().equals("runtime")) {
						TargetRuntime rt = getRuntime(e);
						
						if (e.getName().equals("emfResourceFactory")) {
							rt.emfResourceFactory = (ResourceFactoryImpl) e.createExecutableExtension("class");
						}
						else if (e.getName().equals("propertyTab")) {
							String id = e.getAttribute("id");
							String category = e.getAttribute("category");
							String label = e.getAttribute("label");
							
							Bpmn2TabDescriptor td = new Bpmn2TabDescriptor(id,category,label);
							td.afterTab = e.getAttribute("afterTab");
							String indented = e.getAttribute("indented");
							td.indented = indented!=null && indented.trim().equalsIgnoreCase("true");
							
							rt.getTabs().add(td);
						}
					}
				}
				
				for (IConfigurationElement e : config) {
					if (!e.getName().equals("runtime")) {
						TargetRuntime rt = getRuntime(e);

						if (e.getName().equals("propertySection")) {
							String id = e.getAttribute("id");
							String tab = e.getAttribute("tab");
							String label = e.getAttribute("label");
	
							Bpmn2SectionDescriptor sd = new Bpmn2SectionDescriptor(id,tab,label);
							sd.sectionClass = (AbstractPropertySection) e.createExecutableExtension("class");
							sd.name = e.getAttribute("name");
							sd.afterSection = e.getAttribute("afterSection");
							sd.filter = e.getAttribute("filter");
							sd.enablesFor = e.getAttribute("enablesFor");
							sd.type = e.getAttribute("type");
							
							rt.getSections().add(sd);
						}
					}
				}
				
				// associate property sections with their respective tabs
				for (TargetRuntime rt : targetRuntimes) {
					for (Bpmn2TabDescriptor td : rt.getTabs()) {
						for (Bpmn2SectionDescriptor sd : rt.getSections()) {
							if (sd.tab.equals(td.id)) {
								if (td.sectionDescriptors==null)
									td.sectionDescriptors = new ArrayList<Bpmn2SectionDescriptor>();
								td.sectionDescriptors.add(sd);
							}
						}
					}
				}
				
			} catch (Exception ex) {
				Activator.logError(ex);
			}
		}
		return targetRuntimes;
	}
	
	private static TargetRuntime getRuntime(IConfigurationElement e) throws InvalidRegistryObjectException, Exception {
		String runtimeId = e.getAttribute("runtimeId");
		TargetRuntime rt = getRuntime(runtimeId);
		if (rt==null) {
			throw new Exception("Plug-in " + Activator.PLUGIN_ID +
					" was unable to find the target runtime with id " +
					runtimeId +
					" referenced in the " + e.getName() + " section "
					);
		}
		return rt;
	}
	
	public ResourceFactoryImpl getEmfResourceFactory() {
		return emfResourceFactory;
	}
	
	public String getName() {
		return name;
	}
	
	public String getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}
	
	private static void addAfterTab(ArrayList<Bpmn2TabDescriptor> list, Bpmn2TabDescriptor tab) {
		
		getAllRuntimes();
		String afterTab = tab.getAfterTab();
		if (afterTab!=null && !afterTab.isEmpty() && !afterTab.equals("top")) {
			for (TargetRuntime rt : targetRuntimes) {
				for (Bpmn2TabDescriptor td : rt.getTabs()) {
					if (td.getId().equals(afterTab) && tab!=td) {
						addAfterTab(list,td);
						if (!list.contains(td))
							list.add(td);
						return;
					}
				}
			}
		}
	}

	private ArrayList<Bpmn2TabDescriptor> getTabs() {
		if (tabDescriptors==null)
			tabDescriptors = new ArrayList<Bpmn2TabDescriptor>();
		return tabDescriptors;
	}

	/**
	 * @return
	 */
	public ArrayList<Bpmn2TabDescriptor> getTabDescriptors() {
		ArrayList<Bpmn2TabDescriptor> list = new ArrayList<Bpmn2TabDescriptor>();
		for (Bpmn2TabDescriptor tab : getTabs()) {
			addAfterTab(list, tab);
			if (!list.contains(tab))
				list.add(tab);
		}
		if (list.isEmpty() && this!=getRuntime(DEFAULT_RUNTIME_ID)) {
			return getRuntime(DEFAULT_RUNTIME_ID).getTabDescriptors();
		}
		
		return list;
	}
	
	private static void addAfterSection(ArrayList<Bpmn2SectionDescriptor> list, Bpmn2SectionDescriptor section) {
		
		getAllRuntimes();
		String afterSection = section.getAfterSection();
		if (afterSection!=null) {
			for (TargetRuntime rt : targetRuntimes) {
				for (Bpmn2SectionDescriptor td : rt.getSections()) {
					if (td.getId().equals(afterSection)) {
						addAfterSection(list,td);
						list.add(td);
						return;
					}
				}
			}
		}
	}
	
	private ArrayList<Bpmn2SectionDescriptor> getSections() {
		if (sectionDescriptors==null)
			sectionDescriptors = new ArrayList<Bpmn2SectionDescriptor>();
		return sectionDescriptors;
	}
	
	/**
	 * @return
	 */
	public ArrayList<Bpmn2SectionDescriptor> getSectionDescriptors() {
		ArrayList<Bpmn2SectionDescriptor> list = new ArrayList<Bpmn2SectionDescriptor>();
		for (Bpmn2SectionDescriptor section : getSections()) {
			addAfterSection(list, section);
			if (!list.contains(section))
				list.add(section);
		}
		if (list.isEmpty() && this!=getRuntime(DEFAULT_RUNTIME_ID)) {
			return getRuntime(DEFAULT_RUNTIME_ID).getSectionDescriptors();
		}
		
		return list;
	}
	
	public static class Bpmn2TabDescriptor extends AbstractTabDescriptor {

		protected String id;
		protected String category;
		protected String label;
		protected String afterTab = null;
		protected boolean indented = false;
		protected Image image = null;
		protected ArrayList<Bpmn2SectionDescriptor> sectionDescriptors = null;
		
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
			if (sectionDescriptors==null)
				return super.getSectionDescriptors();
			return sectionDescriptors;
		}

		@Override
		public boolean isIndented() {
			return indented;
		}
		
	}
	
	public static class Bpmn2SectionDescriptor extends AbstractSectionDescriptor {

		protected String name;
		protected String id;
		protected String tab;
		protected String label;
		protected AbstractPropertySection sectionClass;
		protected String type;
		protected String enablesFor;
		protected String filter;
		protected String afterSection;
		
		public Bpmn2SectionDescriptor(String id, String tab, String label) {
			this.id = id;
			this.tab = tab;
			this.label = label;
		}
		
		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return id;
		}

		@Override
		public ISection getSectionClass() {
			return sectionClass;
		}

		@Override
		public String getTargetTab() {
			return tab;
		}

		@Override
		public boolean appliesTo(IWorkbenchPart part, ISelection selection) {
			// TODO Auto-generated method stub
			return super.appliesTo(part, selection);
		}

		@Override
		public String getAfterSection() {
			if (afterSection==null || afterSection.trim().length()==0)
				return super.getAfterSection();
			return afterSection;
		}

		@Override
		public int getEnablesFor() {
			try {
				return Integer.parseInt(enablesFor);
			}
			catch (Exception ex) {
				
			}
			return super.getEnablesFor();
		}

		@Override
		public IFilter getFilter() {
			// TODO Auto-generated method stub
			return super.getFilter();
		}

		@Override
		public List getInputTypes() {
			// TODO Auto-generated method stub
			return super.getInputTypes();
		}
		
	}
}

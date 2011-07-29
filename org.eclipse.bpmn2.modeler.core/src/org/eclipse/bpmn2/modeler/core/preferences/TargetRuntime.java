package org.eclipse.bpmn2.modeler.core.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.modeler.core.AbstractPropertyChangeListenerProvider;
import org.eclipse.bpmn2.modeler.core.Activator;
import org.eclipse.bpmn2.modeler.core.IBpmn2RuntimeExtension;
import org.eclipse.bpmn2.modeler.core.features.activity.task.ICustomTaskFeature;
import org.eclipse.bpmn2.modeler.core.model.Bpmn2ModelerResourceImpl;
import org.eclipse.bpmn2.modeler.core.preferences.TargetRuntime.CustomTaskDescriptor.Property;
import org.eclipse.bpmn2.modeler.core.preferences.TargetRuntime.CustomTaskDescriptor.Value;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EFactoryImpl;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;
import org.eclipse.ui.views.properties.tabbed.TabContents;


public class TargetRuntime extends AbstractPropertyChangeListenerProvider {

	// extension point ID for Target Runtimes
	public static final String RUNTIME_ID = "org.eclipse.bpmn2.modeler.runtime";
	public static final String DEFAULT_RUNTIME_ID = "org.eclipse.bpmn2.modeler.runtime.none";
	
	// our cached registry of target runtimes contributed by other plugins
	protected static TargetRuntime targetRuntimes[];
	
	protected String name;
	protected String[] versions;
	protected String id;
	protected String description;
	protected IBpmn2RuntimeExtension runtimeExtension;
	protected ModelDescriptor modelDescriptor;
	protected ArrayList<Bpmn2TabDescriptor> tabDescriptors;
	protected ArrayList<Bpmn2SectionDescriptor> sectionDescriptors;
	protected ArrayList<CustomTaskDescriptor> customTasks;
	
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
	
	public static TargetRuntime getDefaultRuntime() {
		return getRuntime(DEFAULT_RUNTIME_ID);
	}
	
	public void setResourceSet(ResourceSet resourceSet) {
		resourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap().put(
				Bpmn2ModelerResourceImpl.BPMN2_CONTENT_TYPE_ID, modelDescriptor.resourceFactory);
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
						
						if (e.getName().equals("model")) {
							ModelDescriptor md = new ModelDescriptor();
							if (e.getAttribute("uri")!=null) {
								String uri = e.getAttribute("uri");
								md.ePackage = EPackage.Registry.INSTANCE.getEPackage(uri);
								md.eFactory = md.ePackage.getEFactoryInstance();
							}
							if (e.getAttribute("resourceFactory")!=null)
								md.resourceFactory = (ResourceFactoryImpl) e.createExecutableExtension("resourceFactory");
							rt.setModelDescriptor(md);
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
						else if (e.getName().equals("customTask")) {
							String id = e.getAttribute("id");
							String name = e.getAttribute("name");
							CustomTaskDescriptor ct = new CustomTaskDescriptor(id,name);
							ct.type = e.getAttribute("type");
							ct.description = e.getAttribute("description");
							ct.createFeature = (ICustomTaskFeature) e.createExecutableExtension("createFeature");
							ct.createFeature.setCustomTaskDescriptor(ct);
							ct.createFeature.setId(id);
							getCustomTaskProperties(ct,e);
							rt.addCustomTask(ct);
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
							String type = e.getAttribute("type");
							if (type!=null && !type.isEmpty())
								sd.appliesToClass = Class.forName(type);

							rt.getSections().add(sd);
						}
					}
				}
				
				// associate property sections with their respective tabs
				for (TargetRuntime rt : targetRuntimes) {
					for (Bpmn2TabDescriptor td : rt.getTabs()) {
						for (Bpmn2SectionDescriptor sd : rt.getSections()) {
							if (sd.tab.equals(td.id)) {
								if (td.unfilteredSectionDescriptors==null)
									td.unfilteredSectionDescriptors = new ArrayList<Bpmn2SectionDescriptor>();
								td.unfilteredSectionDescriptors.add(sd);
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
	
	private static Object getCustomTaskProperties(CustomTaskDescriptor ct, IConfigurationElement e) {
		
		String elem = e.getName();
		if ("value".equals(elem)) {
			String id = e.getAttribute("id");
			Value val = new Value(id);
			for (IConfigurationElement cc : e.getChildren()) {
				Object propValue = getCustomTaskProperties(ct, cc);
				val.getValues().add(propValue);
			}
			return val;
		}
		else {
			if (e.getChildren().length==0) {
				if ("property".equals(elem)) {
					String name = e.getAttribute("name");
					String value = e.getAttribute("value");
					String description = e.getAttribute("description");
					Property prop = new Property(name,description);
					if (value!=null)
						prop.getValues().add(value);
					return prop;
				}
			}
			else {
				for (IConfigurationElement c : e.getChildren()) {
					elem = c.getName();
					String name = c.getAttribute("name");
					String value = c.getAttribute("value");
					String description = c.getAttribute("description");
					Property prop = new Property(name,description); 
					if (value!=null)
						prop.getValues().add(value);
					for (IConfigurationElement cc : c.getChildren()) {
						Object propValue = getCustomTaskProperties(ct, cc);
						prop.getValues().add(propValue);
					}
					ct.getProperties().add(prop);
				}
			}
		}
		return null;
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
	
	public ModelDescriptor getModelDescriptor() {
		return modelDescriptor;
	}
	
	public void setModelDescriptor(ModelDescriptor md) {
		md.targetRuntime = this;
		this.modelDescriptor = md;
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
	
	public ArrayList<CustomTaskDescriptor> getCustomTasks()
	{
		if (customTasks==null) {
			customTasks = new ArrayList<CustomTaskDescriptor>();
		}
		return customTasks;
	}
	
	public void addCustomTask(CustomTaskDescriptor ct) {
		getCustomTasks().add(ct);
		ct.targetRuntime = this;
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
	
	public static class BaseRuntimeDescriptor {
		
		protected TargetRuntime targetRuntime;
		
		public TargetRuntime getRuntime() {
			return targetRuntime;
		}
	}
	
	public static class ModelDescriptor extends BaseRuntimeDescriptor {
		
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
	}
	
	public static class Bpmn2TabDescriptor extends AbstractTabDescriptor {

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
	
	public static class Bpmn2SectionDescriptor extends AbstractSectionDescriptor {

		protected String name;
		protected String id;
		protected String tab;
		protected String label;
		protected AbstractPropertySection sectionClass;
		protected Class appliesToClass;
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
			
			// should we delegate to the section to determine whether it should be included in this tab?
			if (sectionClass instanceof IBpmn2PropertySection) {
				return ((IBpmn2PropertySection)sectionClass).appliesTo(part, selection);
			}
			
			// if an input description was specified, check if the selected business object is of this description. 
			if (appliesToClass!=null && selection instanceof IStructuredSelection &&
					((IStructuredSelection) selection).isEmpty()==false) {
			
				Object firstElement = ((IStructuredSelection) selection).getFirstElement();
				EditPart editPart = null;
				if (firstElement instanceof EditPart) {
					editPart = (EditPart) firstElement;
				} else if (firstElement instanceof IAdaptable) {
					editPart = (EditPart) ((IAdaptable) firstElement).getAdapter(EditPart.class);
				}
				if (editPart != null && editPart.getModel() instanceof PictogramElement) {
					PictogramElement pe = (PictogramElement) editPart.getModel();
					// this is a special hack to allow selection of connection decorator labels:
					// the connection decorator does not have a business object linked to it,
					// but its parent (the connection) does.
					if (pe.getLink()==null && pe.eContainer() instanceof PictogramElement)
						pe = (PictogramElement)pe.eContainer();
					if (pe.getLink()!=null) {
						for (EObject eObj : pe.getLink().getBusinessObjects()){
							if (appliesToClass.isInstance(eObj)) {
								return true;
							}
						}
					}
				}
				return false;
			}
			return true;
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
//			return super.getFilter();
			return new IFilter() {

				@Override
				public boolean select(Object toTest) {
					return false;
				}
				
			};
		}

		@Override
		public List getInputTypes() {
			// TODO Auto-generated method stub
			return super.getInputTypes();
		}
		
	}
	
	public static class CustomTaskDescriptor extends BaseRuntimeDescriptor {


		// Container class for property values
		public static class Value {
			
			static int ID = 0;
			String id;
			public List<Object>values;
			
			public Value() {
				setDefaultId();
			}
			
			public Value(String id) {
				if (id==null || id.isEmpty())
					setDefaultId();
				else
					this.id = id;
			}
			
			public List<Object> getValues() {
				if (values==null) {
					values = new ArrayList<Object>();
				}
				return values;
			}
			
			private void setDefaultId() {
				id = "V-" + ID++;
			}
		}
		
		public static class Property {
			public String name;
			public String description;
			public List<Object>values;
			
			public Property() {
				this.name = "unknown";
			}
			
			public Property(String name, String description) {
				this.name = name;
				this.description = description;
			}
			
			public List<Object> getValues() {
				if (values==null) {
					values = new ArrayList<Object>();
				}
				return values;
			}
		}
		
		protected String id;
		protected String name;
		protected String type;
		protected String description;
		protected ICustomTaskFeature createFeature;
		protected List<Property> properties = new ArrayList<Property>();
		
		public CustomTaskDescriptor(String id, String name) {
			this.id = id;
			this.name = name;
		}
		
		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}
		
		public String getType() {
			return type;
		}
		
		public String getDescription() {
			return description;
		}

		public ICustomTaskFeature getCreateFeature() {
			return createFeature;
		}
		
		public List<Property> getProperties() {
			return properties;
		}

		public static String getStringValue(Property prop) {

			if (!prop.getValues().isEmpty()) {
				// simple attribute - find a String value for it
				for (Object propValue : prop.getValues()) {
					if (propValue instanceof String) {
						return (String)propValue;
					}
					else if (propValue instanceof Property) {
						String s = getStringValue((Property)propValue);
						if (s!=null)
							return s;
					}
				}
			}
			return null;
		}
		
	}
}

package org.eclipse.bpmn2.modeler.core.utils;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.bpmn2.BaseElement;
import org.eclipse.bpmn2.provider.Bpmn2ItemProviderAdapterFactory;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ItemProviderAdapter;

public class ModelUtil {

	// TODO: need to determine whether IDs need to be unique within a Resource or ResourceSet - see getKey()
	
	// Map of EMF resource sets to ID mapping tables. The ID mapping tables map a BPMN2 element ID string to the EObject.
	// The EObject is not used anywhere (yet!) just a placeholder to allow use of a HashMap for fast lookups of the ID string.
	// The ID strings are composed from the BPMN2 element description name and a sequence number (starting at 1).
	// When a new ID is requested, generateID() simply increments the sequence number until an ID is found that isn't
	// already in the table.
	public static HashMap<Object, Hashtable<String, EObject>> ids = new  HashMap<Object, Hashtable<String, EObject>>();
	// Map of ID strings and sequential counters for each BPMN2 element description.
	public static HashMap<String, Integer> defaultIds = new HashMap<String, Integer>();

	/**
	 * Clear the IDs hashmap for the given EMF Resource. This should be called
	 * when the editor is disposed to avoid unnecessary growth of the IDs table.
	 * 
	 * @param res - the EMF Resource that was used to generate the ID strings.
	 */
	public static void clearIDs(Resource res, boolean all) {
		ids.remove( getKey(res) );
		if (all) {
			defaultIds.clear();
		}
	}

	/**
	 * Construct the first part of the ID string using the BPMN2 element description name.
	 * If the object is a DI element, concatenate the BPMN2 element description name.
	 * 
	 * @param obj - the BPMN2 object
	 * @return name string
	 */
	public static String getObjectName(EObject obj) {
		String name;
		EStructuralFeature feature = ((EObject)obj).eClass().getEStructuralFeature("bpmnElement");
		if (feature!=null && obj.eGet(feature)!=null) {
			EObject bpmnElement = (EObject) obj.eGet(feature);
			name = obj.eClass().getName() + "_" + bpmnElement.eClass().getName();
		}
		else {
			name = obj.eClass().getName();
		}
		return name;
	}
	
	private static Object getKey(EObject obj) {
		assert(obj!=null);
		return getKey(obj.eResource());
	}
	
	private static Object getKey(Resource res) {
		assert(res!=null);
		return res.getResourceSet();
	}
	
	/**
	 * If an EObject has not yet been added to a Resource (e.g. during construction)
	 * generate an ID string using a different strategy (basically same ID prefixed with an underscore).
	 * The "defaultIds" table is used to track the next sequential ID value for a given element description.
	 * 
	 * @param obj - the BPMN2 object
	 * @return the ID string
	 */
	private static String generateDefaultID(EObject obj) {
		String name = getObjectName(obj);
		Integer value = defaultIds.get(name);
		if (value==null)
			value = Integer.valueOf(1);
		value = Integer.valueOf( value.intValue() + 1 );
		defaultIds.put(name, Integer.valueOf(value));
		
		return "_" + name + "_" + value;
	}

	/**
	 * Generate an ID string for a given BPMN2 object.
	 * 
	 * @param obj - the BPMN2 object
	 * @return the ID string
	 */
	public static String generateID(EObject obj) {
		return generateID(obj,obj.eResource());
	}

	/**
	 * Generate an ID string for a given BPMN2 object that will (eventually!) be added to the given Resource.
	 * 
	 * CAUTION: IDs for objects that have already been deleted WILL be reused.
	 * 
	 * @param obj - the BPMN2 object
	 * @param res - the Resource to which the object will be added
	 * @return the ID string
	 */
	public static String generateID(EObject obj, Resource res) {
		Object key = (res==null ? getKey(obj) : getKey(res));
		if (key!=null) {
			Hashtable<String, EObject> tab = ids.get(key);
			if (tab==null) {
				tab = new Hashtable<String, EObject>();
				ids.put(key, tab);
			}
			
			String name = getObjectName(obj);
			for (int i=1;; ++i) {
				String id = name + "_" + i;
				if (tab.get(id)==null) {
					tab.put(id, obj);
					return id;
				}
			}
		}
		return generateDefaultID(obj);
	}

	/**
	 * Add an ID string to the ID mapping table(s). This must be used during model import
	 * to add existing BPMN2 element IDs to the table so we don't generate duplicates.
	 * 
	 * @param obj - the BPMN2 object
	 */
	public static void addID(EObject obj) {
		EStructuralFeature feature = ((EObject)obj).eClass().getEStructuralFeature("id");
		if (feature!=null) {
			Object value = obj.eGet(feature);
			if (value!=null) {
				addID(obj,(String)value);
			}
			else {
				// TODO: what to do here if the BPMN2 element has an "id" attribute which is not set?
				// should we generate one and set it?
				// yup
				setID(obj);
			}
		}
		
	}
	
	/**
	 * Add an ID string to the ID mapping table(s). This must be used during model import
	 * to add existing BPMN2 element IDs to the table so we don't generate duplicates.
	 * 
	 * @param obj - the BPMN2 object
	 * @param id - the object's ID string
	 */
	public static void addID(EObject obj, String id) {
		Object key = getKey(obj);
		String name = getObjectName(obj);
		if (key==null || id.startsWith("_" + name + "_")) {
			int newValue = 0;
			try {
				int i = id.lastIndexOf('_') + 1;
				if (i<id.length())
					newValue = Integer.parseInt(id.substring(i));
			} catch (Exception e) {
			}
			Integer oldValue = defaultIds.get(name);
			if (oldValue==null || newValue > oldValue.intValue())
				defaultIds.put(name, Integer.valueOf(newValue));
		}
		else {	
			Hashtable<String, EObject> tab = ids.get(key);
			if (tab==null) {
				tab = new Hashtable<String, EObject>();
				ids.put(key, tab);
			}
			tab.put(id, obj);
		}
	}

	/**
	 * Generate a unique ID for the given BPMN2 element and set it.
	 * This should only be used during object construction AFTER an object has
	 * already been added to a Resource.
	 * 
	 * @param obj - the BPMN2 object
	 */
	public static void setID(EObject obj) {
		setID(obj,obj.eResource());
	}

	/**
	 * Generate a unique ID for the given BPMN2 element and set it.
	 * This should be used during object construction if the object has NOT YET
	 * been added to a Resource.
	 * 
	 * @param obj - the BPMN2 object
	 * @param res - the Resource to which the object will be added
	 */
	public static void setID(EObject obj, Resource res) {
		EStructuralFeature feature = ((EObject)obj).eClass().getEStructuralFeature("id");
		if (feature!=null) {
			obj.eSet(feature, generateID(obj,res));
		}
	}


	public static String getName(BaseElement element) {
		EStructuralFeature feature = element.eClass().getEStructuralFeature("name");
		if (feature!=null && element.eGet(feature) instanceof String)
			return (String) element.eGet(feature);
		return null;
	}

	public static boolean hasName(BaseElement element) {
		EStructuralFeature feature = element.eClass().getEStructuralFeature("name");
		return feature!=null;
	}
	
	public static String getDisplayName(EObject obj, EAttribute attr) {
		if (attr!=null) {
			ItemProviderAdapter itemProviderAdapter = (ItemProviderAdapter) new Bpmn2ItemProviderAdapterFactory()
					.adapt(obj, ItemProviderAdapter.class);
			
			IItemPropertyDescriptor propertyDescriptor = itemProviderAdapter.getPropertyDescriptor(obj,attr);
			if (propertyDescriptor!=null)
				return propertyDescriptor.getDisplayName(attr);
			
			// There are no property descriptors available for this EObject -
			// this is probably because the "edit" plugin was not generated for
			// the EMF model, or is not available.
			// Use the class name to synthesize a display name
			obj = attr;
		}
		
		String className = obj.eClass().getName();
		className = className.replaceAll("Impl$", "");
		String displayName = "";
		for (char c : className.toCharArray()) {
			if ('A'<=c && c<'Z') {
				if (displayName.length()>0)
					displayName += " ";
			}
			displayName += c;
		}
		return displayName;
	}
}

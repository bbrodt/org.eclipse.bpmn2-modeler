package org.eclipse.bpmn2.modeler.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.bpmn2.modeler.core.TargetRuntime;
import org.eclipse.bpmn2.modeler.core.ToolEnablementPreferences;
import org.eclipse.bpmn2.modeler.core.ToolEnablement;
import org.eclipse.bpmn2.modeler.core.features.activity.task.extension.ICustomTaskEditor;
import org.eclipse.bpmn2.modeler.ui.Activator;
import org.eclipse.bpmn2.modeler.ui.features.activity.task.CustomTaskFeatureContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.palette.impl.ObjectCreationToolEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class Bpmn2PropertyPage extends PropertyPage {

	public final static String PREFERENCES_ID = "org.eclipse.bpmn2.modeler";
	public final static String TARGET_RUNTIME_ID = "target.runtime";
	
	private Preferences prefs;

	private Combo cboRuntimes;
	
	public Bpmn2PropertyPage() {
		super();
		setTitle("BPMN2");
	}

	@Override
	protected Control createContents(Composite parent) {
		
		initData();
		
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(3, false));

		Label lblRuntime = new Label(container, SWT.NONE);
		lblRuntime.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblRuntime.setText("Targeted runtime:");
		
		cboRuntimes = new Combo(container, SWT.NONE);
		cboRuntimes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		
		TargetRuntime cr = TargetRuntime.getRuntime();
		int i = 0;
		for (TargetRuntime r : TargetRuntime.getAllRuntimes()) {
			cboRuntimes.add(r.getName());
			if (r == cr)
				cboRuntimes.select(i);
			++i;
		}

		return container;
	}

	private void restoreDefaults() {
		TargetRuntime.setRuntime(TargetRuntime.DEFAULT_RUNTIME_ID);
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		restoreDefaults();
	}
	
	public static void loadPreferences(IProject project) {
		IEclipsePreferences rootNode = Platform.getPreferencesService()
				.getRootNode();
		Preferences prefs = rootNode.node(ProjectScope.SCOPE)
				.node(project.getName())
				.node(PREFERENCES_ID);
		reloadPreferences(prefs);
	}

	private void initData() {

		IProject project = (IProject) getElement().getAdapter(IProject.class);
		IEclipsePreferences rootNode = Platform.getPreferencesService()
				.getRootNode();
		prefs = rootNode.node(ProjectScope.SCOPE)
				.node(project.getName())
				.node(PREFERENCES_ID);

		reloadPreferences(prefs);
	}

	private static void reloadPreferences(Preferences prefs) {
		String id = prefs.get(TARGET_RUNTIME_ID,TargetRuntime.DEFAULT_RUNTIME_ID);
		TargetRuntime.setRuntime(id);
	}

	@Override
	public boolean performOk() {
		setErrorMessage(null);
		try {
			updateData();
			
			int i = cboRuntimes.getSelectionIndex();
			TargetRuntime rt = TargetRuntime.getAllRuntimes()[i];
			TargetRuntime.setRuntime(rt);
		} catch (BackingStoreException e) {
			Activator.showErrorWithLogging(e);
		}
		return true;
	}

	private void updateData() throws BackingStoreException {
		int i = cboRuntimes.getSelectionIndex();
		TargetRuntime rt = TargetRuntime.getAllRuntimes()[i];
		prefs.put(TARGET_RUNTIME_ID,rt.getId());
		prefs.flush();
	}
}

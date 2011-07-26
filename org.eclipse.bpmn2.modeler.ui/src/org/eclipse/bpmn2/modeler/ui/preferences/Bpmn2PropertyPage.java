package org.eclipse.bpmn2.modeler.ui.preferences;

import org.eclipse.bpmn2.modeler.core.preferences.Bpmn2Preferences;
import org.eclipse.bpmn2.modeler.core.preferences.TargetRuntime;
import org.eclipse.bpmn2.modeler.ui.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

public class Bpmn2PropertyPage extends PropertyPage {

	private Bpmn2Preferences prefs;
	
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
		
		TargetRuntime cr = prefs.getRuntime();
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
		prefs.restoreDefaults();
		prefs.getRuntime();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		restoreDefaults();
	}

	private void initData() {
		IProject project = (IProject) getElement().getAdapter(IProject.class);
		prefs = new Bpmn2Preferences(project);
		prefs.load();
	}

	@Override
	public boolean performOk() {
		setErrorMessage(null);
		try {
			updateData();
		} catch (BackingStoreException e) {
			Activator.showErrorWithLogging(e);
		}
		return true;
	}

	private void updateData() throws BackingStoreException {
		int i = cboRuntimes.getSelectionIndex();
		TargetRuntime rt = TargetRuntime.getAllRuntimes()[i];
		prefs.setRuntime(rt);
		prefs.save();
	}
}

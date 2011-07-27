/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 *  All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/
package org.eclipse.bpmn2.modeler.ui.property;

import org.eclipse.bpmn2.Bpmn2Factory;
import org.eclipse.bpmn2.Expression;
import org.eclipse.bpmn2.SequenceFlow;
import org.eclipse.bpmn2.modeler.core.utils.ModelUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ConditionPropertiesComposite extends MainPropertiesComposite {

	private Button button;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public ConditionPropertiesComposite(Composite parent, int style) {
		super(parent, style);
	}

	@SuppressWarnings("restriction")
	@Override
	public void createBindings(final EObject be) {
		
		if (be instanceof SequenceFlow) {
			
			final SequenceFlow sf = (SequenceFlow) be;
			//if (button==null)
			{
				
				button = new Button(this, SWT.PUSH);
				button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
				toolkit.adapt(button, true, true);
				widgets.add(button);
				button.addSelectionListener(new SelectionAdapter() {
					
					public void widgetSelected(SelectionEvent e) {
						TransactionalEditingDomain domain = bpmn2Editor.getEditingDomain();
						domain.getCommandStack().execute(new RecordingCommand(domain) {
							@Override
							protected void doExecute() {
								if (sf.getConditionExpression()!=null)
									sf.setConditionExpression(null);
								else {
									Expression exp = Bpmn2Factory.eINSTANCE.createFormalExpression();
									sf.setConditionExpression(exp);
									ModelUtil.setID(exp);
								}
								setEObject(be);
							}
						});
					}
				});
			}
			Expression exp = (Expression) sf.getConditionExpression();
			
			if (exp != null) {
				button.setText("Remove Condition");
				super.createBindings(exp);
			}
			else {
				button.setText("Add Condition");
			}
		}
		
	}
}

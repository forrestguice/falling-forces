/**
   Copyright (C) 2011 Forrest Guice
   This file is part of Falling Forces

   Falling Forces is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   Falling Forces is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with Falling Forces. If not, see <http://www.gnu.org/licenses/>.
*/

package com.forrestguice.fallforces.lib;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import com.forrestguice.android.EditTextUtility;
import com.forrestguice.fallforces.model.RopeModulus;
import com.forrestguice.fallforces.model.UnitsUtility;

import android.content.Context;
import android.content.SharedPreferences;
import android.app.Dialog;
import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Window;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextWatcher;
import android.text.Editable;

public class ImpactRatingDialog extends Dialog 
{
	private static final String FIELD_IMPACTRATING = "impactratingdialog_impactrating";
	private static final String FIELD_ROPEMODULUS = "impactratingdialog_ropemodulus"; 
	
	private Activity myParent;
	public DecimalFormat formatter = new DecimalFormat("0.0000");
	
	private double impactrating = 0;
	private double ropemodulus = 0;
	private boolean toggle = true;
	private boolean forward = true;
	
	public ImpactRatingDialog( Activity c )
	{
		super(c);
		myParent = c;
		setContentView(R.layout.dialog_impactrating);
		setTitle(myParent.getString(R.string.impactrating_dialog_title)); 
		setCancelable(true);
		
		EditText txtModulus = (EditText)findViewById(R.id.txt_impactratingdialog_modulus);
		EditTextUtility.addClearButtonToField(myParent, txtModulus);
		txtModulus.addTextChangedListener(new TextWatcher() 
		{
			public void afterTextChanged(Editable s)
			{
				forward = true;
				if (toggle) refreshDisplay();
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		
		EditText txtRating = (EditText)findViewById(R.id.txt_impactratingdialog_rating);
		EditTextUtility.addClearButtonToField(myParent, txtRating);
		txtRating.addTextChangedListener(new TextWatcher() 
		{
			public void afterTextChanged(Editable s)
			{
				forward = false;
				if (toggle) refreshDisplay();
			}	
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
					
		Button cancelButton = (Button)findViewById(R.id.btn_impactratingdialog_cancel);
		cancelButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				cancel();
			}
		});
		
		Button applyButton = (Button)findViewById(R.id.btn_impactratingdialog_apply);
		applyButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				applyResults();
				accept();
			}
		});
							
		restoreDialogFields();
		refreshDisplay();
	}
	
	public Bundle onSaveInstanceState ()
	{
		saveDialogFields();
		return super.onSaveInstanceState();
	}
	
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		restoreDialogFields();
		refreshDisplay();
	}
	
	public void onPrepareDialog()
	{
		restoreDialogFields();
		refreshDisplay();
	}
			
	private void saveDialogFields()
	{
		SharedPreferences settings = myParent.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor settings_editor = settings.edit();
		EditText txtModulus = (EditText)findViewById(R.id.txt_impactratingdialog_modulus);		
		EditText txtRating = (EditText)findViewById(R.id.txt_impactratingdialog_rating);
		
		String sModulus = txtModulus.getText().toString().trim();
		String sRating = txtRating.getText().toString().trim();
		settings_editor.putString(FIELD_ROPEMODULUS, sModulus);
        settings_editor.putString(FIELD_IMPACTRATING, sRating);
        settings_editor.commit();
	}
	
	private void restoreDialogFields()
	{
		SharedPreferences settings = myParent.getPreferences(Context.MODE_PRIVATE);
		
		EditText txtModulus = (EditText)findViewById(R.id.txt_impactratingdialog_modulus);
		String sTxtModulus = settings.getString(FIELD_ROPEMODULUS, "").trim();
		if (sTxtModulus.equals("0")) txtModulus.setText("");
		else txtModulus.setText(sTxtModulus);
		
		EditText txtImpact = (EditText)findViewById(R.id.txt_impactratingdialog_rating);
		String sTxtImpact = settings.getString(FIELD_IMPACTRATING, "").trim();
		if (sTxtImpact.equals("0")) txtImpact.setText("");
		else txtImpact.setText(sTxtImpact);	
	}
	
	private void refreshDisplay()
	{
		NumberFormat nf = NumberFormat.getInstance();
		
		EditText txtResult = (EditText)findViewById(R.id.txt_impactratingdialog_result);
				
		EditText txtModulus = (EditText)findViewById(R.id.txt_impactratingdialog_modulus);
		String inputModulus = txtModulus.getText().toString().trim();
		ropemodulus = 0;
		if (!inputModulus.equals(""))
			try {
				ropemodulus = nf.parse(inputModulus).doubleValue();
			} catch (ParseException e) {
				ropemodulus = 0;
				e.printStackTrace();
			}
				
		EditText txtImpact = (EditText)findViewById(R.id.txt_impactratingdialog_rating);
		String inputImpact = txtImpact.getText().toString();
		impactrating = 0;
		if (!inputImpact.equals(""))
			try {
				impactrating = nf.parse(inputImpact).doubleValue();
			} catch (ParseException e) {
				impactrating = 0;
				e.printStackTrace();
			}
		
		toggle = false;
		if (forward)
		{
			if (ropemodulus != 0)
			{
				// find rating from modulus
				RopeModulus modulus = new RopeModulus();
				modulus.setRopeModulus(ropemodulus);
				impactrating = modulus.getImpactRating().doubleValue();
				txtImpact.setText(formatter.format(impactrating) + "");
										
			} else {
				txtImpact.setText("");
				txtResult.setText("");
			}
			
		} else {	
			if (impactrating != 0)
			{
				// find modulus from rating
				RopeModulus modulus = new RopeModulus(impactrating);
				ropemodulus = modulus.getRopeModulus().doubleValue();
				txtModulus.setText(formatter.format(ropemodulus) + "");
				
			} else {
				txtModulus.setText("");
				txtResult.setText("");
			}
		}
		
		txtResult.setText("Rating: " + formatter.format(impactrating) + " kN");
		toggle = true;
	}
	
	private void applyResults()
	{
		SharedPreferences settings = myParent.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor settings_editor = settings.edit();
		settings_editor.putFloat(MainActivity.FIELD_IMPACTRATING, (float)impactrating);
        settings_editor.commit();
	}

	/**
	 * Applies changes, saves fields, closes the dialog.
	 */
	public void accept()
	{
		saveDialogFields();
		super.cancel();       // use cancel listener to signal changes (accept)
	}
	
	/**
	 * Saves fields and closes the dialog (does not apply; cancel action backs out)
	 */
	@Override
	public void cancel()
	{
		saveDialogFields();
		super.dismiss();           // use regular dismiss for cancel (do nothing)
	}
	
}
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

public class ForceDialog extends Dialog 
{
	private static final String FIELD_KN = "forcedialog_kn";
	private static final String FIELD_LB = "forcedialog_lb"; 
	
	private Activity myParent;
	public DecimalFormat formatter = new DecimalFormat("0.0000");
	
	private double kn = 0;
	private double lb = 0;
	private boolean toggle = true;
	private boolean forward = true;
	
	public ForceDialog( Activity c )
	{
		super(c);
		myParent = c;
		setContentView(R.layout.dialog_force);
		setTitle(myParent.getString(R.string.force_dialog_title)); 
		setCancelable(true);
		
		EditText txtKn = (EditText)findViewById(R.id.txt_forcedialog_kn);
		EditTextUtility.addClearButtonToField(myParent, txtKn);
		txtKn.addTextChangedListener(new TextWatcher() 
		{
			public void afterTextChanged(Editable s)
			{
				forward = true;
				if (toggle) refreshDisplay();
			}	
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
		
		EditText txtLb = (EditText)findViewById(R.id.txt_forcedialog_lb);
		EditTextUtility.addClearButtonToField(myParent, txtLb);
		txtLb.addTextChangedListener(new TextWatcher() 
		{
			public void afterTextChanged(Editable s)
			{
				forward = false;
				if (toggle) refreshDisplay();
			}	
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
					
		Button closeButton = (Button)findViewById(R.id.btn_forcedialog_close);
		closeButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				dismiss();
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
		EditText txtKn = (EditText)findViewById(R.id.txt_forcedialog_kn);		
		EditText txtLb = (EditText)findViewById(R.id.txt_forcedialog_lb);
		
		String sKn = txtKn.getText().toString().trim();
		String sLb = txtLb.getText().toString().trim();
		settings_editor.putString(FIELD_KN, sKn);
        settings_editor.putString(FIELD_LB, sLb);
        settings_editor.commit();
	}
	
	private void restoreDialogFields()
	{
		SharedPreferences settings = myParent.getPreferences(Context.MODE_PRIVATE);
		
		EditText txtKn = (EditText)findViewById(R.id.txt_forcedialog_kn);
		String sTxtKn = settings.getString(FIELD_KN, "").trim();
		if (sTxtKn.equals("0")) txtKn.setText("");
		else txtKn.setText(sTxtKn);
		
		EditText txtLb = (EditText)findViewById(R.id.txt_forcedialog_lb);
		String sTxtLb = settings.getString(FIELD_LB, "").trim();
		if (sTxtLb.equals("0")) txtLb.setText("");
		else txtLb.setText(sTxtLb);	
	}
	
	private void refreshDisplay()
	{
		NumberFormat nf = NumberFormat.getInstance();
		
		EditText txtKn = (EditText)findViewById(R.id.txt_forcedialog_kn);
		String inputKn = txtKn.getText().toString().trim();
		kn = 0;
		if (!inputKn.equals(""))
			try {
				kn = nf.parse(inputKn).doubleValue();
			} catch (ParseException e) {
				kn = 0;
				e.printStackTrace();
			}
				
		EditText txtLb = (EditText)findViewById(R.id.txt_forcedialog_lb);
		String inputLb = txtLb.getText().toString();
		lb = 0;
		if (!inputLb.equals(""))
			try {
				lb = nf.parse(inputLb).doubleValue();
			} catch (ParseException e) {
				lb = 0;
				e.printStackTrace();
			}
		
		toggle = false;
		if (forward)
		{
			if (kn != 0)
			{
				// convert kn to lb
				lb = kn * UnitsUtility.LB_IN_KN;
				txtLb.setText(formatter.format(lb) + "");
				
			} else {
				txtLb.setText("");
			}
			
		} else {	
			if (lb != 0)
			{
				// convert lb to kn
				kn = lb * UnitsUtility.KN_IN_LB;
				txtKn.setText(formatter.format(kn) + "");
				
			} else {
				txtKn.setText("");
			}
		}
		toggle = true;
	}
		
	@Override
	public void dismiss()
	{
		saveDialogFields();
		super.dismiss();
	}
	
	@Override
	public void cancel()
	{
		saveDialogFields();
		super.cancel();
	}
	
}
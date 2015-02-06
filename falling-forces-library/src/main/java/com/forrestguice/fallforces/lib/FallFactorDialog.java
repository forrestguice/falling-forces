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
import android.graphics.drawable.Drawable;
import android.app.Dialog;
import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.MenuItem;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextWatcher;
import android.text.Editable;

public class FallFactorDialog extends Dialog 
{
	private static final String FIELD_HEIGHT = "fallfactorcalc_height"; 
	private static final String FIELD_LENGTH = "fallfactorcalc_length"; 
	
	private Activity myParent;
	public DecimalFormat formatter = new DecimalFormat("0.0000");
	
	private double fallFactor = 0;
	private double height = 0;
	private double length = 0;
	
	public FallFactorDialog( Activity c )
	{
		super(c);
		myParent = c;
		setContentView(R.layout.dialog_fallfactor);
		setTitle(myParent.getString(R.string.fallfactor_dialog_title)); 
		setCancelable(true);
		
		final EditText txtHeight = (EditText)findViewById(R.id.txt_fallfactor_height);
		EditTextUtility.addClearButtonToField(myParent, txtHeight);
        txtHeight.addTextChangedListener(new TextWatcher() 
		{
			public void afterTextChanged(Editable s)
			{			
				calculateFallFactor();
				refreshDisplay();
			}	
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
			
		
		final EditText txtLength = (EditText)findViewById(R.id.txt_fallfactor_length);
		EditTextUtility.addClearButtonToField(myParent, txtLength);
		txtLength.addTextChangedListener(new TextWatcher() 
		{
			public void afterTextChanged(Editable s)
			{  				
				calculateFallFactor();
				refreshDisplay();
			}	
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
		});
			
		Button applyButton = (Button)findViewById(R.id.btn_fallfactor_apply);
		applyButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				applyResults();
				accept();
			}
		});
		
		Button cancelButton = (Button)findViewById(R.id.btn_fallfactor_cancel);
		cancelButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				cancel();
				//dismiss();
			}
		});
		
		Button unitsButton = (Button)findViewById(R.id.btn_fallfactor_units);
		registerForContextMenu(unitsButton);
		unitsButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				openContextMenu(v);
			}
		});
								
		restoreDialogFields();
		calculateFallFactor();
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
		calculateFallFactor();
		refreshDisplay();
	}
	
	public void onPrepareDialog()
	{
		restoreDialogFields();
		calculateFallFactor();
		refreshDisplay();
		
		EditText txtHeight = (EditText)findViewById(R.id.txt_fallfactor_height);	
		txtHeight.requestFocus();
	}
	
	private void calculateFallFactor()
	{
		NumberFormat nf = NumberFormat.getInstance();
	
		EditText txtHeight = (EditText)findViewById(R.id.txt_fallfactor_height);		
		EditText txtLength = (EditText)findViewById(R.id.txt_fallfactor_length);
		
		String sHeight = txtHeight.getText().toString();
		String sLength = txtLength.getText().toString();
		
		height = 0;
		length = 0;
		if (!sHeight.trim().equals(""))
			try {
				height = nf.parse(sHeight).doubleValue();
			} catch (ParseException e) {
				height = 0;
				e.printStackTrace();
			}
		if (!sLength.trim().equals(""))
			try {
				length = nf.parse(sLength).doubleValue();
			} catch (ParseException e) {
				length = 0;
				e.printStackTrace();
			}
		
		txtHeight.setError(null);
		if (height > (2*length+1) && length != 0)
		{
			txtHeight.setError(myParent.getString(R.string.error_fallfactor_height));
		}
		
		fallFactor = 0;				
		if (height > 0 && length > 0) fallFactor = height / length;	
		if (fallFactor > 2) fallFactor = 2;
	}
		
	private void saveDialogFields()
	{
		SharedPreferences settings = myParent.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor settings_editor = settings.edit();
		EditText txtHeight = (EditText)findViewById(R.id.txt_fallfactor_height);		
		EditText txtLength = (EditText)findViewById(R.id.txt_fallfactor_length);
		
		String sHeight = txtHeight.getText().toString().trim();
		String sLength = txtLength.getText().toString().trim();
		settings_editor.putString(FIELD_HEIGHT, sHeight);
        settings_editor.putString(FIELD_LENGTH, sLength);
        settings_editor.commit();
	}
	
	private void restoreDialogFields()
	{
		SharedPreferences settings = myParent.getPreferences(Context.MODE_PRIVATE);
		
		EditText txtHeight = (EditText)findViewById(R.id.txt_fallfactor_height);
		String sTxtHeight = settings.getString(FIELD_HEIGHT, "").trim();
		if (sTxtHeight.equals("0")) txtHeight.setText("");
		else txtHeight.setText(sTxtHeight);
		
		EditText txtLength = (EditText)findViewById(R.id.txt_fallfactor_length);
		String sTxtLength = settings.getString(FIELD_LENGTH, "").trim();
		if (sTxtLength.equals("0")) txtLength.setText("");
		else txtLength.setText(sTxtLength);	
		
		SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(myParent);
		Button btnUnits = (Button)findViewById(R.id.btn_fallfactor_units);
		String sUnits = settings2.getString("units_distance", "ft");
		btnUnits.setText(sUnits);
		
		TextView txtUnits = (TextView)findViewById(R.id.txt_fallfactor_units2);
		txtUnits.setText(sUnits + " of rope");
	}
	
	private void refreshDisplay()
	{
		EditText txtResult = (EditText)findViewById(R.id.txt_fallfactor_result);
		txtResult.setText("Fall Factor = " + formatter.format(fallFactor));
	}

	private void applyResults()
	{
		SharedPreferences settings = myParent.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor settings_editor = settings.edit();
		settings_editor.putFloat(MainActivity.FIELD_FALLFACTOR, (float)fallFactor);
        settings_editor.commit();
	}
		
	public void accept()
	{
		saveDialogFields();
		super.cancel();    // call cancel when we want to accept (swapped) - blame it on the crappy dialog api
	}
	
	@Override
	public void cancel()
	{
		saveDialogFields();
		super.dismiss();   // dismiss on cancel (dismiss is always called even when canceled)
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = myParent.getMenuInflater();
		inflater.inflate(R.menu.units_menu_distance, menu);

		SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(myParent);
		String distanceUnits = settings2.getString("units_distance", "ft");
		MenuItem item1 = menu.findItem(R.id.units_menuitem_distance_ft);
		MenuItem item2 = menu.findItem(R.id.units_menuitem_distance_m);
		if (distanceUnits.equals("ft")) item1.setChecked(true);
		else item2.setChecked(true);
	}
	
	@Override
	public boolean onContextItemSelected( android.view.MenuItem item ) 
	{
		boolean retValue = false;
		SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(myParent);
		SharedPreferences.Editor settings_editor = settings2.edit();
		
		Button btnUnits = (Button)findViewById(R.id.btn_fallfactor_units);
		TextView txtUnits = (TextView)findViewById(R.id.txt_fallfactor_units2);
		
		String oldUnits = settings2.getString("units_distance", "ft");
		String newUnits = "";
		
		if (item.getItemId() == R.id.units_menuitem_distance_ft) {
			if (item.isChecked()) item.setChecked(false);
			else item.setChecked(true);
			newUnits = "ft";
			settings_editor.putString("units_distance", "ft");
			btnUnits.setText("ft");
			txtUnits.setText("ft of rope");
			settings_editor.commit();
			retValue = true;
			
		} else if (item.getItemId() == R.id.units_menuitem_distance_m) {
			if (item.isChecked()) item.setChecked(false);
			else item.setChecked(true);
			newUnits = "m";
			settings_editor.putString("units_distance", "m");
			btnUnits.setText("m");
			txtUnits.setText("m of rope");
			settings_editor.commit();
			retValue = true;
		} else {
			retValue = super.onContextItemSelected(item);
		}
		
		boolean autoConvert = settings2.getBoolean("units_convert", true);	
		if (retValue && autoConvert)   // convert the current field values to newly set units
		{
			if (!oldUnits.equals(newUnits) && !newUnits.equals(""))
			{
		    	NumberFormat nf = NumberFormat.getInstance();
		    	
				EditText txtHeight = (EditText)findViewById(R.id.txt_fallfactor_height); 
				EditText txtLength = (EditText)findViewById(R.id.txt_fallfactor_length);
				
				double oldHeight = 0;  // get previous height
				String sOldHeight = txtHeight.getText().toString().trim();
				if (!sOldHeight.equals(""))
				{
					try {
						oldHeight = nf.parse(sOldHeight).doubleValue();
					} catch (ParseException e) {
						oldHeight = 0;
						e.printStackTrace();
					}
				}
				
				double oldLength = 0;  // get previous length
				String sOldLength = txtLength.getText().toString().trim();
				if (!sOldLength.equals(""))
				{
					try {
						oldLength = nf.parse(sOldLength).doubleValue();
					} catch (ParseException e) {
						oldLength = 0;
						e.printStackTrace();
					}
				}
				
				double newHeight = UnitsUtility.convertUnits(oldHeight, oldUnits, newUnits);
				double newLength = UnitsUtility.convertUnits(oldLength, oldUnits, newUnits);
				
				if (newHeight != 0) txtHeight.setText(formatter.format(newHeight) + "");  // set new height
				txtHeight.setText("");
				
				if (newLength != 0) txtLength.setText(formatter.format(newLength) + "");  // set new length
				txtLength.setText("");
			}
		}
		
		return retValue;
	}

	/**
		This override is required to get the dialog to trigger onContextItemSelected.
	*/
	@Override
	public boolean onMenuItemSelected(int aFeatureId, MenuItem aMenuItem) {
	    if (aFeatureId==Window.FEATURE_CONTEXT_MENU)
	        return onContextItemSelected(aMenuItem);
	    else
	        return super.onMenuItemSelected(aFeatureId, aMenuItem);
	}

}
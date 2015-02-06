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

package com.forrestguice.android;

import java.io.File;

import com.forrestguice.fallforces.lib.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * String Resources:
 * 
 * export_dialog_title 	: string
 * export_array  		: string-array
 * export_array_values	: string-array
 * export_extensions    : string-array
 *
 */
public class SaveToFileDialog extends Dialog
{
	public static String SETTING_METHOD = "save_method";
	
	public static File f = null;   // operation values; only one op can occur at
	
    private Activity myparent;
    private Button actionButton, cancelButton;
    
    public SaveToFileDialog(Activity c)
    {
    	super(c);
    	myparent = c;

    	setContentView(R.layout.dialog_save);
    	setTitle(myparent.getString(R.string.export_dialog_title));
    	setCancelable(true);

    	SharedPreferences settings = myparent.getPreferences(Context.MODE_PRIVATE);
    	
    	// Spinner: Save Type
        Spinner exportSpinner = (Spinner)findViewById(R.id.method);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(myparent, R.array.export_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exportSpinner.setAdapter(adapter);
        exportSpinner.setSelection(settings.getInt(SETTING_METHOD, 0));

        exportSpinner.setOnItemSelectedListener(new OnItemSelectedListener() 
        {
           public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) 
           { 	   
              SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(myparent);
              SharedPreferences.Editor editor = settings.edit();
              editor.putInt("export_dialog_option", pos);
              editor.commit();
           }

           public void onNothingSelected(AdapterView parent) 
           { }
        });

        // Spinner: File Type
        Spinner extSpinner = (Spinner)findViewById(R.id.extension);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(myparent, R.array.export_extensions, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        extSpinner.setAdapter(adapter2);

        // Button: Save
        actionButton = (Button)findViewById(R.id.action_button);
        actionButton.setOnClickListener( new View.OnClickListener()
        {
        	public void onClick(View v)
        	{
        		boolean ready = true;
        		EditText filenameText = (EditText)findViewById(R.id.filename);
        		Spinner exportSpinner = (Spinner)findViewById(R.id.method);
      
        		Spinner typeSpinner = (Spinner)findViewById(R.id.extension);
        		String ext = (String)typeSpinner.getSelectedItem();		
      
        		String filename = filenameText.getText().toString().trim();   // append file extension  		
        		if (!filename.endsWith(ext)) filename += ext;
        		if (filename.equals(ext))   // empty filename
        		{
        			ready = false;
        			filenameText.requestFocus();
        			filenameText.setError(myparent.getString(R.string.export_error_filename));
        		}

        		if (ready)
        		{
        			int pos = exportSpinner.getSelectedItemPosition();        			
        		    saveAction(filename, ext, pos);

        			SharedPreferences settings = myparent.getPreferences(Context.MODE_PRIVATE);
        			SharedPreferences.Editor editor = settings.edit();
        			editor.putInt(SETTING_METHOD, pos);
        			editor.commit();
        			dismiss();
        		}
        	}
        });

        // Button: Cancel
        cancelButton = (Button)findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener( new View.OnClickListener()
        {
        	public void onClick(View v)
        	{
        		cancel();
        	}
        });
    }
    
    public void saveAction( String filename, String type, int saveMethod )
    {
    	// override when creating instances of this class
    }

    public void onPrepareDialog()
    {
    	Spinner spinner = (Spinner)findViewById(R.id.method);
    	SharedPreferences settings = myparent.getPreferences(Context.MODE_PRIVATE);
    	int i = settings.getInt(SETTING_METHOD, 0);
    	spinner.setSelection(i >= 0 && i < spinner.getCount() ? i : 0);

    	EditText filenameText = (EditText)findViewById(R.id.filename);
    	filenameText.setText("");
    }


}

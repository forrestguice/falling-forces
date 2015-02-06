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

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import com.forrestguice.android.EditTextUtility;
import com.forrestguice.fallforces.lib.graph.ThreeDGraphActivity;
import com.forrestguice.fallforces.lib.graph.ThreeDGraphRenderer;
import com.forrestguice.fallforces.model.*;

import android.view.KeyEvent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.TextView.OnEditorActionListener;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.content.DialogInterface;

public class MainActivity extends Activity 
{	
	public static final int DIALOG_HELP = 0;		// dialog ids
	public static final int DIALOG_FALLFACTOR = 1;
	public static final int DIALOG_FORCEUNITS = 2;
	public static final int DIALOG_IMPACTRATING = 3;
	
	public static final String FIELD_FALLFACTOR = "input_fallfactor";  // field ids
	public static final String FIELD_MASS = "input_mass";
	public static final String FIELD_IMPACTRATING = "input_impactrating";
					
	private String help_text = "";  // string used to pass help text to the help dialog
	public DecimalFormat formatter = new DecimalFormat("0.00");   // output formatting
	
	private double fallFactor = 0;
	private double mass = 0;              // gathered input
	private double impactRating = 0;
	private String units_mass = "lb";
	private String units_force = "kN";
	
	private double force_on_climber = 0;
	private double force_on_belayer = 0;  // calculated results
	private double force_on_anchor = 0; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	ActivityUtil.initActivity(this);
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
               	   		                
        // help button: fall factor
        TextView fallfactorHelp = (TextView)findViewById(R.id.input_fallfactor_help);
        fallfactorHelp.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				help_text = getString(R.string.help_txt_fallfactor);
				showDialog(MainActivity.DIALOG_HELP);
			}
		});
        
        // help button: mass/weight
        TextView massHelp = (TextView)findViewById(R.id.input_mass_help); 
        massHelp.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				help_text = getString(R.string.help_txt_climbermass);
				showDialog(MainActivity.DIALOG_HELP);
			}
		});
        
        // help button: impact rating
        final TextView impactHelp = (TextView)findViewById(R.id.input_impactrating_help);
        impactHelp.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				help_text = getString(R.string.help_txt_impactrating);
				showDialog(MainActivity.DIALOG_HELP);
			}
		});
        
        // button: reset fields
        Button resetButton = (Button)findViewById(R.id.input_reset); 
        resetButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				resetFields();
			}
		});
                
        // button: calculate
        Button calcButton = (Button)findViewById(R.id.input_action); 
        calcButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				calculate();
			}
		});
        
        // button: mass units
        Button unitsButton = (Button)findViewById(R.id.input_mass_units);
        registerForContextMenu(unitsButton);
        unitsButton.setOnClickListener( new View.OnClickListener()
   		{
   			public void onClick(View v)
   			{
   				openContextMenu(v);
   			}
   		});
        
        // Button: Find fall factor
        ImageButton fallfactorCalcButton = (ImageButton)findViewById(R.id.input_fallfactor_calc);
        fallfactorCalcButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showDialog(MainActivity.DIALOG_FALLFACTOR);
			}
		
		});
        
        // Button: Find impact rating
        ImageButton impactratingCalcButton = (ImageButton)findViewById(R.id.input_impactrating_calc);
        impactratingCalcButton.setOnClickListener( new View.OnClickListener()
		{
			public void onClick(View v)
			{
				showDialog(MainActivity.DIALOG_IMPACTRATING);
			}
		
		});
        
        // Field: Fall Factor
        final EditText fieldFallFactor = (EditText)findViewById(R.id.input_fallfactor);
        EditTextUtility.addClearButtonToField(this, fieldFallFactor);
        
        final Drawable x0 = getResources().getDrawable(R.drawable.ic_input_delete);
        x0.setBounds(0, 0, x0.getIntrinsicWidth(), x0.getIntrinsicHeight());      
        fieldFallFactor.setOnFocusChangeListener( new EditTextUtility.ClearButtonFocusChangeListener(x0) {			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus)
				{
			    	NumberFormat nf = NumberFormat.getInstance();
					String sValue = fieldFallFactor.getText().toString().trim();
					double value = 0;
					if (!sValue.equals(""))
					{
						try {
							value = nf.parse(sValue).doubleValue();
						} catch (ParseException e) {
							value = 0;
							e.printStackTrace();
						}
					}
					if (value < 0)
					{
						fieldFallFactor.setText("");
						value = 0;
						//saveFields();
						
					} else if (value > 2) {
						fieldFallFactor.setText("2");
						value = 2;
						//saveFields();
					}
					
					//if (value != fallFactor) fieldFallFactor.setTextColor(Color.GRAY);
					//else fieldFallFactor.setTextColor(Color.BLACK);
					
				}
				super.onFocusChange(v, hasFocus);
			}
		});
    
        // Field: Mass
        final EditText fieldMass = (EditText)findViewById(R.id.input_mass);
        EditTextUtility.addClearButtonToField(this, fieldMass);
            
        // Field: Impact Rating
        final EditText fieldImpact = (EditText)findViewById(R.id.input_impactrating);
        EditTextUtility.addClearButtonToField(this, fieldImpact);
        
        restoreState();
    }
    
    public void calculate()
    {
		boolean ready = gatherInput();
		if (ready)
		{
			//final EditText fieldFallFactor = (EditText)findViewById(R.id.input_fallfactor);
			//fieldFallFactor.setTextColor(Color.BLACK);
			
			calculateResult();
			refreshDisplay();
			
		} else {
			resetOutput();
		}
    }
    
    private void calculateResult()
    {
    	Weight weight = (units_mass.equals("kg")) ? new Weight(mass) :
    												new Weight(mass, Weight.UNITS_LBS);
        FallFactor fall_factor = new FallFactor(fallFactor);
        RopeModulus modulus = new RopeModulus(impactRating);
        ModelWexler forces = new ModelWexler(weight, modulus, fall_factor);
          
        force_on_climber = forces.getForceOnClimber().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
       	force_on_belayer = forces.getForceOnBelayer().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    	force_on_anchor = forces.getForceOnAnchor().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
    	
        if (units_force.equals("lb"))
        {
        	force_on_climber *= UnitsUtility.LB_IN_KN;
        	force_on_belayer *= UnitsUtility.LB_IN_KN;
        	force_on_anchor *= UnitsUtility.LB_IN_KN;
        }        
    }
    
    private boolean gatherInput()
    {
    	boolean retValue = true;
    	
    	NumberFormat nf = NumberFormat.getInstance();
    	
    	EditText txt_impact = (EditText)findViewById(R.id.input_impactrating);
      	String sImpact = txt_impact.getText().toString().trim();
      	impactRating = 0;
      	if (!sImpact.equals(""))
			try {
				impactRating = nf.parse(sImpact).doubleValue();
			} catch (ParseException e1) {
				impactRating = 0;
				e1.printStackTrace();
			}
		else {
      		txt_impact.requestFocus();
      		retValue = false;
      	}
      	
     	EditText txt_mass = (EditText)findViewById(R.id.input_mass);
      	String sMass = txt_mass.getText().toString().trim();
      	mass = 0;
      	if (!sMass.equals(""))
			try {
				mass = nf.parse(sMass).doubleValue();
			} catch (ParseException e) {
				mass = 0;
				e.printStackTrace();
			}
		else {
      		txt_mass.requestFocus();
      		retValue = false;
      	}
      	     	
      	EditText txt_fallFactor = (EditText)findViewById(R.id.input_fallfactor);
      	String sFallfactor = txt_fallFactor.getText().toString().trim();
      	fallFactor = 0;
      	if (!sFallfactor.equals(""))
			try {
				fallFactor = nf.parse(sFallfactor).doubleValue();
			} catch (ParseException e) {
				fallFactor = 0;
				e.printStackTrace();
			}
		else { 
      		txt_fallFactor.requestFocus();
      		retValue = false;
      	}
       	
      	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		units_mass = settings.getString("units_mass", "lb");
		units_force = settings.getString("units_force", "kN");
		
		return retValue;
    }
    
    private void refreshDisplay()
    {   
    	String forceUnits = " " + units_force;
    	
        EditText output_anchor = (EditText)findViewById(R.id.output_force_anchor);
        output_anchor.setText(formatter.format(force_on_anchor) + forceUnits);
        
        EditText output_climber = (EditText)findViewById(R.id.output_force_climber);
        output_climber.setText(formatter.format(force_on_climber) + forceUnits);
        
        EditText output_belayer = (EditText)findViewById(R.id.output_force_belayer);
        output_belayer.setText(formatter.format(force_on_belayer) + forceUnits);
        
        if (force_on_climber != 0)
        {
        	output_climber.requestFocus();
        }
    }
    
    private void restoreState()
    {
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		
		//resetFields();
		restoreFallFactor(settings);
		restoreMass(settings);
		restoreImpactRating(settings);
		
		SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(this);
		restoreMassUnits(settings2);
	    		
		calculate();
    }
    private void restoreFallFactor(SharedPreferences settings)
    {
		float fallFactor = settings.getFloat(MainActivity.FIELD_FALLFACTOR, 0);
		EditText v = (EditText)findViewById(R.id.input_fallfactor);
		if (fallFactor == -1) v.setText("");
		else v.setText(formatter.format(fallFactor) + "");
    }
    private void restoreMass(SharedPreferences settings)
    {
		float mass = settings.getFloat(MainActivity.FIELD_MASS, 0);
		EditText v = (EditText)findViewById(R.id.input_mass);
		if (mass == 0) v.setText("");
		else v.setText(formatter.format(mass) + "");
    }
    private void restoreImpactRating(SharedPreferences settings)
    {
		float impact = settings.getFloat(MainActivity.FIELD_IMPACTRATING, 0);
		EditText v = (EditText)findViewById(R.id.input_impactrating);
		if (impact == 0) v.setText("");
		else v.setText(formatter.format(impact) + "");
    }
    private void restoreMassUnits(SharedPreferences settings)
    {
		String units = settings.getString("units_mass", "lb");
		Button v = (Button)findViewById(R.id.input_mass_units);
		v.setText(units);
    }
    
    private void saveFields()
    {
    	NumberFormat nf = NumberFormat.getInstance();
    	
    	EditText txt_fallFactor = (EditText)findViewById(R.id.input_fallfactor);
    	String sFallFactor = txt_fallFactor.getText().toString().trim();
    	double fallFactor = -1;
    	if (!sFallFactor.equals(""))
    	{
			try {
				fallFactor = nf.parse(sFallFactor).doubleValue();
			} catch (ParseException e1) {
				fallFactor = 0;
				e1.printStackTrace();
			}
    	}
   		
    	EditText txt_mass = (EditText)findViewById(R.id.input_mass);
    	String sMass = txt_mass.getText().toString().trim();
    	double mass = 0;
    	if (!sMass.equals(""))
			try {
				mass = nf.parse(sMass).doubleValue();
			} catch (ParseException e1) {
				mass = 0;
				e1.printStackTrace();
			}
    	
    	EditText txt_impact = (EditText)findViewById(R.id.input_impactrating);
    	String sImpact = txt_impact.getText().toString().trim();
    	double impactRating = 0;
    	if (!sImpact.equals(""))
    	{
			try {
				impactRating = nf.parse(sImpact).doubleValue();
			} catch (ParseException e) {
				impactRating = 0;
				e.printStackTrace();
			}
    	}
    	
    	SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
    	SharedPreferences.Editor settings_editor = settings.edit();
    	settings_editor.putFloat(MainActivity.FIELD_FALLFACTOR, (float)fallFactor);
    	settings_editor.putFloat(MainActivity.FIELD_MASS, (float)mass);
    	settings_editor.putFloat(MainActivity.FIELD_IMPACTRATING, (float)impactRating);
    	settings_editor.commit();
    }
    
    private void resetOutput()
    {
        EditText output_anchor = (EditText)findViewById(R.id.output_force_anchor);
        EditText output_climber = (EditText)findViewById(R.id.output_force_climber);
        EditText output_belayer = (EditText)findViewById(R.id.output_force_belayer);    	
    
    	String forceUnits = " " + units_force;
     	output_anchor.setText(formatter.format(0) + forceUnits);
     	output_climber.setText(formatter.format(0) + forceUnits);
     	output_belayer.setText(formatter.format(0) + forceUnits);
    }
    
    private void resetFields()
    {
        EditText txt_fallFactor = (EditText)findViewById(R.id.input_fallfactor);
     	EditText txt_mass = (EditText)findViewById(R.id.input_mass);
     	EditText txt_impact = (EditText)findViewById(R.id.input_impactrating);
     	
    	force_on_climber = 0;
    	force_on_belayer = 0;
    	force_on_anchor = 0;
     	
     	txt_fallFactor.setText("");
     	txt_mass.setText("");
     	txt_impact.setText("");
     	
     	txt_fallFactor.requestFocus();
     	saveFields();
     	resetOutput();
     	//calculate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	//////////////////////////////////////////////////
	// onOptionsItemSelected( item : MenuItem )  :  boolean
	//////////////////////////////////////////////////
	/**
      Options Menu
      Called when some options menu item is selected.
	 */
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) 
	{
		if (item.getItemId() == R.id.menu_settings) {
			Intent i = ActivityUtil.createIntent(this, SettingsActivity.class);
			startActivity(i);
			return true;
			
		} else if (item.getItemId() == R.id.menu_convert_force) {
			showDialog(DIALOG_FORCEUNITS);
			return true;
			
		} else if (item.getItemId() == R.id.menu_fallfactor) {
			showDialog(DIALOG_FALLFACTOR);
			return true;

		} else if (item.getItemId() == R.id.menu_impactrating) {
			showDialog(DIALOG_IMPACTRATING);
			return true;

		} else if (item.getItemId() == R.id.menu_graph) {
			Intent graphIntent = ActivityUtil.createIntent(this, ThreeDGraphActivity.class);
			boolean ready = gatherInput();
			if (ready)
            {
                calculateResult();
            }
			// TODO: calculate input before calling intent
			//graphIntent.putExtra("forcemode", ThreeDGraphRenderer.MODE_CLIMBER);
			//graphIntent.putExtra("plotmode", ThreeDGraphRenderer.PLOT_FORCES1);
			graphIntent.putExtra("fallfactor", fallFactor);
			graphIntent.putExtra("mass", mass);
			graphIntent.putExtra("impactrating", impactRating);
			graphIntent.putExtra("unitsmass", units_mass);
			graphIntent.putExtra("unitsforce", units_force);
			startActivity(graphIntent);
			return true;

		} else if (item.getItemId() == R.id.menu_help) {
			Intent helpIntent = ActivityUtil.createIntent(this, HelpActivity.class);
			startActivity(helpIntent);
			return true;
			
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	//////////////////////////////////////////////////
	// onCreateDialog( id : int, args : Bundle )  :  Dialog
	//////////////////////////////////////////////////
	@Override
	protected Dialog onCreateDialog( int id )
	{
		Dialog dialog = null;
		switch(id) 
		{
		case MainActivity.DIALOG_HELP:
			dialog = new HelpDialog(this);
			break;
			
		case MainActivity.DIALOG_FORCEUNITS:
			dialog = new ForceDialog(this);
			break;
			
		case MainActivity.DIALOG_FALLFACTOR:
			FallFactorDialog d = new FallFactorDialog(this);
			d.setOnCancelListener(new DialogInterface.OnCancelListener() 
			{				
				// use onCancel listener for action, and do nothing on actual cancel (swapped in dialog)
				@Override
				public void onCancel(DialogInterface dialog)
				{
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					restoreFallFactor(settings);	
				}
			});
			dialog = d;
			break;
			
		case MainActivity.DIALOG_IMPACTRATING:
			ImpactRatingDialog d2 = new ImpactRatingDialog(this);
			d2.setOnCancelListener(new DialogInterface.OnCancelListener() {
				// use onCancel listener for action, and do nothing on actual cancel (swapped in dialog)
				public void onCancel(DialogInterface dialog)
				{
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					restoreImpactRating(settings);
				}
			});
			dialog = d2;
			break;
						
		default:
			dialog = null;
			break;
		}
		return dialog;
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		//SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		switch (id)
		{
		case MainActivity.DIALOG_HELP:
			HelpDialog helpDialog = (HelpDialog)dialog;
			helpDialog.onPrepareDialog(help_text);
			break;
			
		case MainActivity.DIALOG_FALLFACTOR:
			FallFactorDialog fallfactorDialog = (FallFactorDialog)dialog;
			fallfactorDialog.onPrepareDialog();
			break;
			
		case MainActivity.DIALOG_IMPACTRATING:
			ImpactRatingDialog impactratingDialog = (ImpactRatingDialog)dialog;
			impactratingDialog.onPrepareDialog();
			break;
			
		case MainActivity.DIALOG_FORCEUNITS:
			ForceDialog forceDialog = (ForceDialog)dialog;
			forceDialog.onPrepareDialog();
			break;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle b)
	{
		saveFields();
		super.onSaveInstanceState(b);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);
		restoreState();
	}
	
	@Override
	protected void onDestroy()
	{
		saveFields();
		super.onDestroy();
	}

	@Override
	protected void onPause()
	{
		saveFields();
		super.onPause();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		restoreState();
	}
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) 
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.units_menu_mass, menu);

		SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(this);
		String distanceUnits = settings2.getString("units_mass", "lb");
		MenuItem item1 = menu.findItem(R.id.units_menuitem_mass_lb);
		MenuItem item2 = menu.findItem(R.id.units_menuitem_mass_kg);
		if (distanceUnits.equals("lb")) item1.setChecked(true);
		else item2.setChecked(true);
	}
	
	@Override
	public boolean onContextItemSelected( android.view.MenuItem item ) 
	{
		boolean retValue = false;
		SharedPreferences settings2 = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor settings_editor = settings2.edit();
		
		Button btnUnits = (Button)findViewById(R.id.input_mass_units);
		
		String oldUnits = settings2.getString("units_mass", "lb");
		String newUnits = "";
		
		if (item.getItemId() == R.id.units_menuitem_mass_lb) {
			if (item.isChecked()) item.setChecked(false);
			else item.setChecked(true);
			newUnits = "lb";
			settings_editor.putString("units_mass", "lb");
			btnUnits.setText("lb");
			settings_editor.commit();
			retValue = true;
			
		} else if (item.getItemId() == R.id.units_menuitem_mass_kg) {
			if (item.isChecked()) item.setChecked(false);
			else item.setChecked(true);
			newUnits = "kg";
			settings_editor.putString("units_mass", "kg");
			btnUnits.setText("kg");
			settings_editor.commit();
			retValue =  true;
			
		} else {
			retValue = super.onContextItemSelected(item);
		} 
		
		boolean autoConvert = settings2.getBoolean("units_convert", true);	
		if (retValue && autoConvert)   // convert the current field values to newly set units
		{
			if (!oldUnits.equals(newUnits) && !newUnits.equals(""))
			{
		    	NumberFormat nf = NumberFormat.getInstance();
				EditText txtMass = (EditText)findViewById(R.id.input_mass); 
								
				double oldMass = 0.0f;  // get previous mass
				String sOldMass = txtMass.getText().toString().trim();
				if (!sOldMass.equals(""))
				{
					try {
						oldMass = nf.parse(sOldMass).doubleValue();
					} catch (ParseException e) {
						oldMass = 0;
						e.printStackTrace();
					}
				}
								
				double newMass = UnitsUtility.convertUnits(oldMass, oldUnits, newUnits);
				if (newMass != 0) txtMass.setText(nf.format(newMass));  // set new mass
				else txtMass.setText("");
			}
		}
		
		return retValue;
	}
}

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

package com.forrestguice.fallforces.lib.graph;

import java.io.File;

import javax.microedition.khronos.opengles.GL;

import android.app.Activity;

import com.forrestguice.android.SaveToFileDialog;
import com.forrestguice.fallforces.lib.ActivityUtil;
import com.forrestguice.fallforces.lib.HelpActivity;
import com.forrestguice.fallforces.lib.R;
import com.forrestguice.glstuff.GLSurfaceView;
import com.forrestguice.glstuff.MatrixTrackingGL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

public class ThreeDGraphActivity extends Activity 
{
	public static final int DIALOG_SCREENSHOT = 10;
	
	private GLSurfaceView glSurfaceView;
	private ThreeDGraphRenderer renderer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		ActivityUtil.initActivity(this);
		super.onCreate(savedInstanceState);
		
		Bundle params = getIntent().getExtras();
		if (params == null)
		{
			Log.d("ThreeDGraphActivity", "null parameters!");
			finish();   // this activity requires extras!
		}
				
		glSurfaceView = new GLSurfaceView(this) 
		{
			@Override
			public boolean onTouchEvent(MotionEvent event) 
			{	
				if (!renderer.onTouchEvent(event))
				{
					return super.onTouchEvent(event);
				}
				return true;
			}
		};
		
		glSurfaceView.setGLWrapper(new GLSurfaceView.GLWrapper()  
	    {  
			@Override
	        public GL wrap(GL gl)  
	        {
	            return new MatrixTrackingGL(gl);
	        }
	    });  
		
		renderer = new ThreeDGraphRenderer(this, params);
		glSurfaceView.setRenderer(renderer);
		setContentView(glSurfaceView);
	}
	
	@Override
	protected void onResume() 
	{			
		super.onResume();
		glSurfaceView.onResume();
	}

	@Override
	protected void onPause() 
	{
		super.onPause();
		glSurfaceView.onPause();
	}
	
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		switch (keyCode) 
		{
		case KeyEvent.KEYCODE_DPAD_UP:
			renderer.move(0, 1);
			glSurfaceView.requestRender();
			return true;

		case KeyEvent.KEYCODE_DPAD_DOWN:
			renderer.move(0, -1);
			glSurfaceView.requestRender();
			return true;

		case KeyEvent.KEYCODE_DPAD_LEFT:
			renderer.move(-1, 0);
			glSurfaceView.requestRender();
			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			renderer.move(1, 0);
			glSurfaceView.requestRender();
			return true;

		default:
			return super.onKeyDown(keyCode, event);
		}
	}
	
	/**
	   Activity Dialogs
	*/
	
	@Override
	protected Dialog onCreateDialog(int id) 
	{
		Dialog d = null;
	
		switch (id)
		{
		case DIALOG_SCREENSHOT:
			d = new SaveToFileDialog(this) 
			{
				@Override
				public void saveAction( String filename, String type, int saveMethod )
				{
					takeScreenshot(filename, type, saveMethod);
				}
			};
			break;
			
		default:
			d = super.onCreateDialog(id);
		}
		
		return d;
	}
	
	@Override
	protected void onPrepareDialog( int id, Dialog dialog )
	{
		switch (id)
		{
		case DIALOG_SCREENSHOT:
			SaveToFileDialog d = (SaveToFileDialog)dialog;
			d.onPrepareDialog();
			break;
		}
	}

	/**
      Options Menu
	*/

    private Menu activityMenu = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        getMenuInflater().inflate(R.menu.activity_graph, menu);
        activityMenu = menu;
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) 
    {
    	// prepare plotmode menu selection
    	switch (renderer.getPlotMode())
    	{
    	//case ThreeDGraphRenderer.PLOT_FORCES2:
        //   	menu.findItem(R.id.graph_menuitem_plot2).setChecked(true);
    	//	break;

    	case ThreeDGraphRenderer.PLOT_FORCES1:
    	default:
           	menu.findItem(R.id.graph_menuitem_plot1).setChecked(true);
    		break;
    	}
    	
    	// prepare forcemode menu selection
    	switch (renderer.getForceMode())
    	{
    	case ThreeDGraph.MODE_ANCHOR:
           	menu.findItem(R.id.graph_menuitem_mode3).setChecked(true);
    		break;

    	case ThreeDGraph.MODE_BELAYER:
           	menu.findItem(R.id.graph_menuitem_mode2).setChecked(true);
    		break;
    		
    	case ThreeDGraph.MODE_CLIMBER:
    	default:
           	menu.findItem(R.id.graph_menuitem_mode1).setChecked(true);
    		break;
    	}
    	
    	// prepare gridmode menu selection
    	switch (renderer.getGridMode())
    	{
    	case ThreeDGraph.GRID_NOLINES:
           	menu.findItem(R.id.graph_menuitem_grid1).setChecked(true);
    		break;

    	case ThreeDGraph.GRID_MAJORLINES:
           	menu.findItem(R.id.graph_menuitem_grid2).setChecked(true);
    		break;
    		
    	case ThreeDGraph.GRID_ALLLINES:
    	default:
           	menu.findItem(R.id.graph_menuitem_grid3).setChecked(true);
    		break;
    	}
    	
    	return super.onPrepareOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected( MenuItem item ) 
	{
		if (item.getItemId() == R.id.menu_help) 
		{
			Intent helpIntent = ActivityUtil.createIntent(this, HelpActivity.class);
			startActivity(helpIntent);
			return true;

        //} else if (item.getItemId() == R.id.menu_calc) {
        //    Intent calcIntent = ActivityUtil.createIntent(this, HelpActivity.class);
        //    startActivity(helpIntent);
        //    return true;

        } else if ( item.getItemId() == R.id.menu_graph_mode ||
                    item.getItemId() == R.id.menu_graph_grid ||
                    item.getItemId() == R.id.menu_graph_current )
        {
            if (activityMenu != null)
            {
                onPrepareOptionsMenu(activityMenu);
            }
            return super.onOptionsItemSelected(item);

		} else if (item.getItemId() == R.id.graph_menuitem_mode1) {
			renderer.setForceMode(ThreeDGraph.MODE_CLIMBER, true);
			renderer.requestReset();
			return true;
			
		} else if (item.getItemId() == R.id.graph_menuitem_mode2) {
			renderer.setForceMode(ThreeDGraph.MODE_BELAYER, true);
			renderer.requestReset();
			return true;
			
		} else if (item.getItemId() == R.id.graph_menuitem_mode3) {
			renderer.setForceMode(ThreeDGraph.MODE_ANCHOR, true);
			renderer.requestReset();
			return true;
			
		} else if (item.getItemId() == R.id.graph_menuitem_plot1) {
			renderer.setPlotMode(ThreeDGraphRenderer.PLOT_FORCES1, true);
			renderer.requestReset();
			return true;
			
		//} else if (item.getItemId() == R.id.graph_menuitem_plot2) {
		//	renderer.setPlotMode(ThreeDGraphRenderer.PLOT_FORCES2, true);
		//	renderer.requestReset();
		//	return true;
			
		} else if (item.getItemId() == R.id.graph_menuitem_grid1) {
			renderer.setGridMode(ThreeDGraph.GRID_NOLINES, true);
			renderer.requestReset();
			return true;
			
		} else if (item.getItemId() == R.id.graph_menuitem_grid2) {
			renderer.setGridMode(ThreeDGraph.GRID_MAJORLINES, true);
			renderer.requestReset();
			return true;
			
		} else if (item.getItemId() == R.id.graph_menuitem_grid3) {
			renderer.setGridMode(ThreeDGraph.GRID_ALLLINES, true);
			renderer.requestReset();
			return true;			
			
		} else if (item.getItemId() == R.id.menu_graph_save) {
			showDialog(DIALOG_SCREENSHOT);			
			return true;
			
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void takeScreenshot(String file_name, final String type, int method)
	{
		String output_path = "fallingforces";
				
	    File sdpath = Environment.getExternalStorageDirectory();	    

	    if (!sdpath.exists())
	    {
	    	if (!sdpath.mkdirs())
	    	{
	    		Toast.makeText(this, "Failed to save screenshot.\n" + sdpath.getAbsolutePath() + " is unavailable.", Toast.LENGTH_LONG).show();
	    		return;
	    	}
	    }
	    
	    File path = new File(sdpath + "/" + output_path);
	    if (!path.exists())
	    {
	    	if (!path.mkdirs())
	    	{
	    		Toast.makeText(this, "Failed to save screenshot.\n" + path.getAbsolutePath() + " is unavailable.", Toast.LENGTH_LONG).show();
	    		return;
	    	}
	    }
	    
	    final File file = new File(path, file_name);
	    if (file.exists())
	    {
	    	// file exists: prompt to overwrite
	    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    	alertDialogBuilder.setTitle(getString(R.string.export_error_exists_title));
	    	alertDialogBuilder.setMessage(file.getAbsolutePath() + " " + getString(R.string.export_error_exists))
	    	.setCancelable(false)
	    	.setPositiveButton("Yes",new DialogInterface.OnClickListener() 
	    	{
	    		public void onClick(DialogInterface dialog,int id) 
	    		{
	    			renderer.takeScreenshot(file.getAbsolutePath(), type);  // notify the renderer
	    			Toast.makeText(ThreeDGraphActivity.this, getString(R.string.export_action_success) + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
	    			dialog.dismiss();
	    		}
	    	})
	    	.setNegativeButton("No",new DialogInterface.OnClickListener() 
	    	{
	    		public void onClick(DialogInterface dialog, int id) 
	    		{ 
	    			dialog.cancel();
	    		}
	    	});

	    	AlertDialog alertDialog = alertDialogBuilder.create();
	    	alertDialog.show();
	    	return;
	    }
	    
		renderer.takeScreenshot(file.getAbsolutePath(), type);  // notify the renderer
		Toast.makeText(this, getString(R.string.export_action_success) + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
	}
	
}

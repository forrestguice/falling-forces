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

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

/**import java.util.Date;
import java.text.DecimalFormat;
import android.content.res.Resources;
import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Handler;

import android.content.DialogInterface;
import android.database.Cursor;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Gravity;

import android.widget.TabHost;
import android.app.TabActivity;

import android.widget.TableRow;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.ToggleButton;
import android.widget.TextView;*/

public class HelpHowItWorksView extends Activity
{
   //////////////////////////////////////////////////
   // onCreate( savedInstanceState : Bundle )  :  void
   //////////////////////////////////////////////////
   /** 
      Application Life Cycle
      Called on Activity creation; register menus, attach listeners, setup
      display timers, restore state.
   */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.help_howto_view);
      restoreState();
   }

   //////////////////////////////////////////////////
   // onSaveInstanceState( outState : Bundle )  :  void
   //////////////////////////////////////////////////
   /**
      Application Life Cycle
      Called when an activity is being stopped and may be killed before
      it resumes; save state and free up resources.
   */
   @Override
   protected void onSaveInstanceState( Bundle outState )
   {
      super.onSaveInstanceState(outState);
      //saveState();
   }

   //////////////////////////////////////////////////
   // onPause()  :  void
   //////////////////////////////////////////////////
   /** 
      Application Life Cycle
      Called when Activity ends or is paused; save state and free up resources.
   */
   @Override
   protected void onPause()
   {
      super.onPause();
      //saveState();
   }

   //////////////////////////////////////////////////
   // onResume()  :  void
   //////////////////////////////////////////////////
   /**
      Application Life Cycle
      Called when Activity resumes; restore state. 
   */
   @Override
   protected void onResume()
   {
      super.onResume();
      restoreState();
   }

   //////////////////////////////////////////////////
   // restoreState()  :  void
   //////////////////////////////////////////////////
   /**
      Restore state of Activity (starting, resuming, ...)
   */
   private void restoreState()
   {
	   TextView t0 = (TextView)findViewById(R.id.label_content);
	   t0.setText(Html.fromHtml(getString(R.string.txtHow)));
	   
	   TextView t1 = (TextView)findViewById(R.id.label_link);
	   t1.setMovementMethod(LinkMovementMethod.getInstance());
   }
   
}

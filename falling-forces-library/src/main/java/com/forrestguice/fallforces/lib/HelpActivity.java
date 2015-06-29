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

import android.os.Bundle;
import android.view.Gravity;
import android.widget.TabHost;
import android.app.TabActivity;
import android.content.Intent;
import android.widget.TextView;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Color;
import android.graphics.Typeface;

public class HelpActivity extends TabActivity
{
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
	  ActivityUtil.initActivity(this);
      super.onCreate(savedInstanceState);
      setContentView(R.layout.help_view);
      getTabHost().setCurrentTab(0);
      restoreState();
   }

   @Override
   protected void onSaveInstanceState( Bundle outState )
   {
      super.onSaveInstanceState(outState);
      saveState();
   }

   @Override
   protected void onPause()
   {
      super.onPause();
      saveState();
   }

   @Override
   protected void onResume()
   {
      super.onResume();
      restoreState();
   }

   private void saveState()
   {	   
   }

   private void restoreState()
   {
      TabHost tabHost = getTabHost();
      TabHost.TabSpec spec;
      Intent intent;

      int selectedColor = Color.argb(255, 236, 236, 236);
      int defaultColor = Color.argb(255, 119, 119, 119);
      int[] selected   = {android.R.attr.state_selected};
      int[] unselected = {android.R.attr.state_selected * -1};

      // (re)populate tabs
      int currentTab = tabHost.getCurrentTab();
      tabHost.setCurrentTab(0);   
      tabHost.clearAllTabs();   

      //~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~
      //~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~=~
      
      StateListDrawable drawable;
      TextView indicator;

       // About View
       intent = new Intent().setClass(this, HelpAboutView.class);
       spec = tabHost.newTabSpec("about");
       drawable  = new StateListDrawable();
       drawable.addState(selected, new ColorDrawable(selectedColor));
       drawable.addState(unselected, new ColorDrawable(defaultColor));

       indicator = ActivityUtil.createTabIndicator(this, getString(R.string.tab_about));
       //indicator = new TextView(this);
       //indicator.setBackgroundDrawable(drawable);
       //indicator.setGravity(Gravity.CENTER);
       //indicator.setTextSize(18.0f);
       //indicator.setTypeface(null, Typeface.BOLD);
       //indicator.setText(R.string.tab_about);
       //indicator.setPadding(0, 10, 0, 10);
       spec.setIndicator(indicator);
       //spec.setIndicator(getString(R.string.tab_about));
       spec.setContent(intent);
       tabHost.addTab(spec);

      // Howto View
      intent = new Intent().setClass(this, HelpHowItWorksView.class);
      spec = tabHost.newTabSpec("howto");
      drawable  = new StateListDrawable();
      drawable.addState(selected, new ColorDrawable(selectedColor));
      drawable.addState(unselected, new ColorDrawable(defaultColor));

      indicator = ActivityUtil.createTabIndicator(this, getString(R.string.tab_how));
      //indicator = new TextView(this);
      //indicator.setBackgroundDrawable(drawable);
      //indicator.setGravity(Gravity.CENTER);
      //indicator.setTextSize(18.0f);
      //indicator.setTypeface(null, Typeface.BOLD);
      //indicator.setText(R.string.tab_how);
      //indicator.setPadding(0, 10, 0, 10);
      spec.setIndicator(indicator);
      spec.setContent(intent);
      tabHost.addTab(spec);

      tabHost.setCurrentTab(currentTab);
   }
   
}

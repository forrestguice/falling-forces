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

import android.app.Dialog;
import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;
import android.text.Html;
import android.view.View;

public class HelpDialog extends Dialog 
{
	private Activity myParent;
	
	public HelpDialog( Activity c )
	{
		super(c);
		myParent = c;
		setContentView(R.layout.dialog_help);	
		setTitle(myParent.getString(R.string.help_dialog_title)); 
		setCancelable(true);
	}

	public void onPrepareDialog(String content)
	{
		TextView txt = (TextView)findViewById(R.id.txt_help_content);
		txt.setText(Html.fromHtml(content));
	}


}

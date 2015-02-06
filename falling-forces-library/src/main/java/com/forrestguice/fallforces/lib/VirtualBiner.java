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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class VirtualBiner extends LinearLayout 
{
	public VirtualBiner(Context context)
	{
		super(context);
		this.setPadding(0, 0, 0, 0);
		this.setOrientation(VERTICAL);
		final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.composite_biner, this);
		this.setVisibility(GONE);
	}
	
	public VirtualBiner(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.setPadding(0, 0, 0, 0);
		this.setOrientation(VERTICAL);
		final LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.composite_biner, this);
		this.setVisibility(GONE);
	}
	
}

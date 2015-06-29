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

package com.forrestguice.glstuff;

import android.util.Log;

public class Texture 
{
	public int textureId = -1;
	public int textureIndex = -1;
	
	public float width = -1;
	public float height = -1;
	
	public Texture(int id, float w, float h)
	{
		textureId = id;
		width = w;
		height = h;
		
		//Log.d("Texture", "loaded " + textureId + ", width: " + width + ", height " + height);
	}
}

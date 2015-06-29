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

/** TextureRegion; contains texture mapping information for
 *  a portion of some texture.
 *        _____
 * u1,v1 |     |
 *       |     |
 *	     |_____| u2,v2
 */

public class TextureRegion
{	
	public float u1, v1;                               // Top/Left U,V Coordinates
	public float u2, v2;                               // Bottom/Right U,V Coordinates

	public TextureRegion()
	{
		u1 = 0;
		v1 = 0;

		u2 = 1;
		v2 = 1;
	}
	
	/**
	 * @param texWidth the entire texture width
	 * @param texHeight the entire texture height
	 * @param x the top left corner of the texture region
	 * @param y the top left corner of the texture region
	 * @param width the width of this texture region
	 * @param height the height of this texture region
	 */
	public TextureRegion(float texWidth, float texHeight, float x, float y, float width, float height)  
	{
		//System.out.println("textureSize = " + texWidth + ", " + texHeight + " :: x,y = " + x + ", " + y +
		//		         "\nregionSize = " + width + ", " + height);
		
		u1 = x / texWidth;                   // Calculate U1
		v1 = y / texHeight;                  // Calculate V1
		
		u2 = u1 + ( width / texWidth );      // Calculate U2
		v2 = v1 + ( height / texHeight );    // Calculate V2
	}
	
	public void setValues(TextureRegion other)
	{
		u1 = other.u1;
		v1 = other.v1;
		
		u2 = other.u2;
		v2 = other.v2;
	}
	
	public String toString()
	{
		return "[" + u1 + "," + v1 + " to " + u2 + "," + v2 + "]";
	}
}

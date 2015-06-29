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

/**
 * A sprite/texture sheet. The sheet is composed of a loaded texture (referenced
 * by textureId), and a number of rows and columns. Each row/column coordinate
 * into the sheet refers to an individual cell and each individual cell is composed
 * of a TextureRegion that contains the appropriate mapping for apply that portion
 * of the texture to some geometry.
 * 
 *             columns
 * 0,0 _______________________
 *    |__|__|__|__|__|__|__|__|
 *    |__|__|__|__|__|__|__|__|   
 *    |__|__|__|__|__|__|__|__| rows
 *    |__|__|__|__|__|__|__|__|
 *    |__|__|__|__|__|__|__|__|
 *    
 */
public class TextureSheet 
{
	public Texture texture;
			
	public int columns = -1;            // width (in cells)
	public int rows = -1;               // height (in cells)
	
	public float cellWidth = -1;        // cell width (in pixels)
	public float cellHeight = -1;       // cell height (in pixels)
	
	public TextureRegion[][] textures;  // A texture region for each cell on the sheet
	
	public TextureSheet( Texture _texture, int w, int h )
	{
		texture = _texture;
				
		rows = h;
		columns = w;
							
		cellWidth = texture.width / columns;
		cellHeight = texture.height / rows;
		
		textures = new TextureRegion[columns][rows];
		for (int i=0; i<columns; i++)
		{
			for (int j=0; j<rows; j++)
			{	
				textures[i][j] = new TextureRegion(texture.width, texture.height, i*cellWidth, j*cellHeight, cellWidth, cellHeight);
			}
		}
	}
	
	public float getWidth()
	{
		return texture.width;
	}
	
	public float getHeight()
	{
		return texture.height;
	}
	
}

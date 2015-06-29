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
 * An AnimatedTexture is composed of a single texture and a set of frames. The
 * single texture is assumed to be a sprite map. Each individual frame is composed
 * of a TextureRegion that maps to the correct portion of the texture.
 */
public class Animation 
{
	public TextureSheet sheet = null;   // underlying sprite sheet;
	public Vector3D[] frameIndices;     // sheet indices corresponding to frames
	public int numFrames = 0;           // total number of frames
	
	public long time = 500;            // the time (in ms) to spend on each frame

	/**
	 * @param _sheet
	 * @param args TODO document args string (format also used to specify animation in config files)
	 */
	public Animation(TextureSheet _sheet, String[] args)  
	{
		sheet = _sheet;
		
		for (int i=0; i<args.length; i++)
		{
			String[] parts = args[i].trim().split("=");
			String argName = parts[0].trim().toLowerCase();
			String argValue = parts[1].trim().toLowerCase();

			if (argName.equals("frames")) 
			{				
				String[] params = argValue.trim().split(",");
				frameIndices = new Vector3D[params.length/2];
				for (int j=0; j<params.length; j += 2)
				{
					int yC = Integer.parseInt(params[j].trim());
					int xC = Integer.parseInt(params[j+1].trim()); 
					addFrame(xC, yC);
				}
				
			} else if (argName.equals("time")) {
				time = Long.parseLong(argValue);
			}
		}
	}

	/**
	 * Add a frame to the animation using sprite map coordinates
	 * @param x coordinate into sprite map (0 is the first row)
	 * @param y coordinate into sprite map (0 is the first column)
	 */
	public void addFrame(int x, int y)
	{
		frameIndices[numFrames] = new Vector3D(x, y, 0);
		numFrames++;
	}
	
	public TextureRegion getFrame(int i)
	{
		if (i < 0 || i >= frameIndices.length)
		{
			return null;
		}
				
		Vector3D fI = frameIndices[i];
		return sheet.textures[(int)fI.x][(int)fI.y];
	}
	
}
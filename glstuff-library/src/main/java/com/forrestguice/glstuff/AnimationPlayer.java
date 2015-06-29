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

public class AnimationPlayer 
{
	public static final int ANIM_STOPPED = 0;
	public static final int ANIM_PLAYING = 1;
	public static final int ANIM_PAUSED = 2;
	
	public Animation animation = null;
	public int animation_state = ANIM_STOPPED;
	
	public int numFrames = 1;             // frame count
	public int currentFrame = 0;          // current frame
	
	public boolean playOnce = false;
	
	public long startTime = -1;
	public long currentTime = -1;
	
	public AnimationPlayer(Animation a)
	{
		animation = a;
		currentFrame = 0;
		numFrames = (animation == null) ? 1 : a.numFrames;
	}
	
	public void onUpdateFrame()
	{
		if (animation == null)
		{   // no animation; show frame 0
			currentFrame = 0;
			return;
		}
				
		switch (animation_state)
		{
		case ANIM_PAUSED:
			// animation paused; do nothing
			break;
			
		case ANIM_PLAYING:
			//System.out.println("play loop");
			currentTime = System.currentTimeMillis();
			if ((currentTime - startTime) > animation.time)
			{
				//System.out.println("advancing frame..");
				startTime = currentTime;
				// advance to the next frame
				if (currentFrame >= numFrames-1)
				{
					if (playOnce)
					{
						animation_state = ANIM_STOPPED;
						return;
					} else {
						currentFrame = 0;	
					}				
					
				} else {
					currentFrame++;
				}
			}
			break;
			
		case ANIM_STOPPED:
			// animation stopped; show frame 0
			animation_state = ANIM_STOPPED;
			break;
		}
	}
	
	public void setAnimation(Animation a)
	{
		animation = a;
	}
	
	public void stop()
	{
		animation_state = ANIM_STOPPED;
	}
	
	public void play()
	{
		if (animation_state != ANIM_STOPPED)
		{
			return;
		}
		startTime = System.currentTimeMillis();
		currentFrame = 0;
		animation_state = ANIM_PLAYING;
	}
	
	public void pause()
	{
		animation_state = ANIM_PAUSED;
	}
	
	public void unPause()
	{
		animation_state = ANIM_PLAYING;
	}
}


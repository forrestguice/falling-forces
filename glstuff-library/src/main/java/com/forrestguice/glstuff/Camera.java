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

import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLU;

public class Camera
{
   public float x = 0.0f, y = 0.0f, z = -2.0f;      // current camera settings
   public float rotationX = 0.0f, rotationY = 0.0f;
   
   public float sx = 0.0f, sy = 0.0f, sz = -2.0f;   // start camera settings
   public float sRotationX = 0.0f, sRotationY = 0.0f;
   
   public float upX = 0;
   public float upY = 1;
   public float upZ = 0;
   
   private float[] target = null;
   
   public Camera() 
   {
   }

   public Camera(float xVal, float yVal, float zVal, float rX, float rY)
   {
      x = xVal;
      y = yVal;
      z = zVal;
      rotationX = rX;
      rotationY = rY;
      
      sx = xVal;
      sy = yVal;
      sz = zVal;
      sRotationX = rX;
      sRotationY = rY;
   }
   
   public void lookAt(float[] p)
   {
	   target = p;
   }

   public void draw( GL10 gl )
   {
	   if (target != null)
	   {
		   GLU.gluLookAt(gl, x, y, z, target[0], target[1], target[2], upX, upY, upZ);
		   
	   } else {
		   gl.glRotatef(rotationX, 1.0f, 0.0f, 0.0f);   // camera position
		   gl.glRotatef(rotationY, 0.0f, 1.0f, 0.0f);
		   gl.glTranslatef(-1*x, -1*y, -1*z);		   
	   }
   }

   public void resetCamera()
   {
	   x = sx;
	   y = sy;
	   z = sz;
	   rotationX = sRotationX;
	   rotationY = sRotationY;
   }

   public void setPosition( Vector3D p )
   {
	   x = p.x;
	   y = p.y;
	   z = p.z;
   }
   
   public void setPosition(float xVal, float yVal, float zVal)
   {
	  x = xVal;
	  y = yVal;
	  z = zVal;
   }
   
   public void setRotation(float rX, float rY)
   {
	  rotationX = rX;
	  rotationY = rY;
   }
  
   public void setParameters(float xVal, float yVal, float zVal, float rX, float rY)
   {
	  x = xVal;
	  y = yVal;
	  z = zVal;
	  rotationX = rX;
	  rotationY = rY;
   }
      
}
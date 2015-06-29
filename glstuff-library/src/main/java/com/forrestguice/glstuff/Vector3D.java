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

public class Vector3D
{
	public float x = 0;
	public float y = 0;
	public float z = 0;

	public Vector3D() {}

	public Vector3D(float xVal, float yVal, float zVal)
	{
		x = xVal;
		y = yVal;
		z = zVal;
	}

	public Vector3D(double xVal, double yVal, double zVal)
	{
		x = (float)xVal;
		y = (float)yVal;
		z = (float)zVal;
	}

	public Vector3D( String[] v )
	{
		x = Float.parseFloat(v[0]);
		y = Float.parseFloat(v[1]);
		z = Float.parseFloat(v[2]);
	}

	public Vector3D( Vector3D other )
	{
		x = other.x;
		y = other.y;
		z = other.z;
	}

	public void copy( Vector3D other )
	{
		x = other.x;
		y = other.y;
		z = other.z;
	}

	public void setValue(String[] v)
	{
		x = Float.parseFloat(v[0]);
		y = Float.parseFloat(v[1]);
		z = Float.parseFloat(v[2]);
	}

	public void setValue( Vector3D other )
	{
		x = other.x;
		y = other.y;
		z = other.z;
	}

	public double magnitude()
	{
		return Math.sqrt((x * x) + (y * y) + (z * z));
	}

	public static double magnitude( float[] v )
	{
		return Math.sqrt((v[0] * v[0]) + (v[1] * v[1]) + (v[2] * v[2]));
	}

	public void normalize()
	{
		double magnitude = Math.sqrt((x * x) + (y * y) + (z * z));
		x /= magnitude;
		y /= magnitude;
		z /= magnitude;
	}

	public static void normalize( float[] v )
	{
		double magnitude = Math.sqrt((v[0] * v[0]) + (v[1] * v[1]) + (v[2] * v[2]));
		v[0] /= magnitude;
		v[1] /= magnitude;
		v[2] /= magnitude;
	}

	public static Vector3D cross( Vector3D a, Vector3D b)
	{
		Vector3D c = new Vector3D(0,0,0);
		c.x = (a.y * b.z) - (a.z * b.y);
		c.y = (a.z * b.x) - (a.x * b.z);
		c.z = (a.x * b.y) - (a.y * b.x);
		return c;
	}

	public static void cross( Vector3D a, Vector3D b, Vector3D c)
	{
		c.x = (a.y * b.z) - (a.z * b.y);
		c.y = (a.z * b.x) - (a.x * b.z);
		c.z = (a.x * b.y) - (a.y * b.x);
	}

	public static float dot( Vector3D v1, Vector3D v2 )
	{
		return (v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z);
	}

	/**
      Return this vector as an array of floats of size 4 (suitable for use
      with opengl matrix/vector functions).
	 */
	public float[] toArray()
	{
		float retValue[] = new float[4];
		retValue[0] = x;
		retValue[1] = y;
		retValue[2] = z;
		retValue[3] = 0;
		return retValue;
	}

}
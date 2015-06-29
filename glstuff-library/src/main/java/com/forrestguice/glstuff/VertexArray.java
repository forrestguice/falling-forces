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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.opengles.GL10;
import android.util.Log;

public class VertexArray extends AnimationPlayer
{	
	public int maxNodes = 0;
	public int numNodes = 0;              // node count
	
	public FloatBuffer node_v = null;     // node vertices
	public FloatBuffer[] node_c = null;   // node colors (one buffer for each frame)
	public FloatBuffer[] node_t = null;   // node texture mapping
	
	public int textureId = -1;            // textureId
	
	public VertexArray(int size)
	{
		this(size, null);
	}
	
	public VertexArray(int size, Animation a)
	{
		super(a);
		if (animation != null) textureId = a.sheet.texture.textureId;
		
		maxNodes = size;
		
		ByteBuffer byteBufferV = ByteBuffer.allocateDirect(4*3*maxNodes);
		byteBufferV.order(ByteOrder.nativeOrder());
		node_v = byteBufferV.asFloatBuffer();
		node_v.position(0);
		
		node_c = new FloatBuffer[numFrames];
		node_t = new FloatBuffer[numFrames];
		
		for (int i=0; i<numFrames; i++)
		{
			ByteBuffer byteBufferC = ByteBuffer.allocateDirect(4*4*maxNodes);
			byteBufferC.order(ByteOrder.nativeOrder());
			node_c[i] = byteBufferC.asFloatBuffer();
			node_c[i].position(0);
			
			ByteBuffer byteBufferT = ByteBuffer.allocateDirect(4*2*maxNodes);
			byteBufferT.order(ByteOrder.nativeOrder());
			node_t[i] = byteBufferT.asFloatBuffer();
			node_t[i].position(0);
		}
	}
	
	/**
	 * drawNodes( GL10, GL_LINES | GL_TRIANGLES | etc )  :  void
	 */
	public void drawNodes(GL10 gl, int drawMode)
	{	
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		node_v.position(0);
		node_c[currentFrame].position(0);
		
		if (textureId >= 0)
		{
	        node_t[currentFrame].position(0);
			gl.glEnable(GL10.GL_TEXTURE_2D);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
	        gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, node_t[currentFrame]);
	        
	        gl.glEnable(GL10.GL_BLEND);
	        gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		}
				
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, node_c[currentFrame]);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, node_v);	    
		gl.glDrawArrays(drawMode, 0, numNodes);
				
		if (textureId >= 0)
		{
	        gl.glDisable(GL10.GL_BLEND);
			gl.glDisable(GL10.GL_TEXTURE_2D);
	        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY); 
		}
		
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
	}
	//@Override
	//public void onUpdateFrame()
	//{
	//	super.onUpdateFrame();
	//}
	
	public void clearNodes()
	{
		numNodes = 0;
		
		node_v.clear();
		
		for (int i=0; i<numFrames; i++)
		{
			node_c[i].clear();
			node_t[i].clear();	
		}
	}	
	
	//
	// Add Nodes - Animated Texturing
	//	
	public void addNode( float x, float y, float z, float cr, float cg, float cb, float ca)
	{
		if (numNodes >= maxNodes)
		{
			Log.d("VertexArray", "Array is full; ignore add request.");
			return;
		}
		
		if (animation == null)
		{
			Log.d("VertexArray", "Missing animation when adding animated node; ignore add request.");
		}
		
		node_v.put(x);
		node_v.put(y);
		node_v.put(z);

		for (int i=0; i<numFrames; i++)
		{
			node_c[i].put(cr);
			node_c[i].put(cg);
			node_c[i].put(cb);
			node_c[i].put(ca);
	
			TextureRegion r = animation.getFrame(i);
			int j = (numNodes) % 6;
			switch (j)			
			{
			case 0:
				node_t[i].put(r.u1);
				node_t[i].put(r.v2);
				break;
				
			case 1:
				node_t[i].put(r.u2);
				node_t[i].put(r.v2);
				break;
				
			case 2:
				node_t[i].put(r.u1);
				node_t[i].put(r.v1);
				break;
				
			case 3:
				node_t[i].put(r.u1);
				node_t[i].put(r.v1);
				break;
				
			case 4:
				node_t[i].put(r.u2);
				node_t[i].put(r.v2);
				break;
				
			case 5:
				node_t[i].put(r.u2);
				node_t[i].put(r.v1);
				break;
			}
		}
		
		numNodes++;
	}
	
	//
	// Add Nodes - Static Texturing
	//
	public void addNode( float x, float y, float z, float cr, float cg, float cb, float ca, float tx, float ty )
	{
		if (numNodes >= maxNodes)
		{
			Log.d("VertexArray", "Array is full; ignore add request.");
			return;
		}
		
		node_v.put(x);
		node_v.put(y);
		node_v.put(z);
		
		node_c[0].put(cr);
		node_c[0].put(cg);
		node_c[0].put(cb);
		node_c[0].put(ca);
		node_t[0].put(tx);
		node_t[0].put(ty);
		
		numNodes++;
	}
	
	public void addNode(float[] node, float[] colors, float[] textureMapping)
	{
		addNode(node[0], node[1], node[2], colors[0], colors[1], colors[2], colors[3], textureMapping[0], textureMapping[1]);
	}
	
	public void addNode(float[] node, float[] colors)
	{
		addNode(node[0], node[1], node[2], colors[0], colors[1], colors[2], colors[3], 0, 0);
	}
	
	public void addNode(float x, float y, float z, float[] colors)
	{
		addNode(x, y, z, colors[0], colors[1], colors[2], colors[3], 0, 0);
	}

}

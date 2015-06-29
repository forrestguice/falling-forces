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

package com.forrestguice.fallforces.lib.graph;

import java.math.BigDecimal;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.forrestguice.fallforces.lib.R;
import com.forrestguice.fallforces.model.FallFactor;
import com.forrestguice.fallforces.model.ModelWexler;
import com.forrestguice.fallforces.model.RopeModulus;
import com.forrestguice.fallforces.model.UnitsUtility;
import com.forrestguice.fallforces.model.Weight;

import com.forrestguice.glstuff.VertexArray;
import com.forrestguice.glstuff.gltext.FontInfo;
import com.forrestguice.glstuff.gltext.GLText3D;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;

import android.os.Bundle;

public class ThreeDGraph
{	
	public static final int MODE_CLIMBER = 0;   // mode: show force on climber
	public static final int MODE_BELAYER = 1;   // mode: show force on anchor
	public static final int MODE_ANCHOR = 2;    // mode: show force on anchor

	public static final int GRID_NOLINES = 0;
	public static final int GRID_MAJORLINES = 1;
	public static final int GRID_ALLLINES = 2;
		
	public static final int MAX_NODES = 8192;
	
	private Context context;	    // parent context (usually Activity)
		
	private double fallFactor = 1.78;          // input: selected fallFactor
	private double mass = 80;                  // input: mass/weight
	private double impactRating = 8;           // input: impact rating
	private String units_mass = "kg";          // input: mass/weight units
	private String units_force = "kN";         // input: force units
	
	private int gridmode = GRID_ALLLINES;
	private int forcemode = MODE_CLIMBER;
		
	private boolean flag_showMajorGrid = true; // input: show major (axis) lines
	private boolean flag_showMinorGrid = true; // input: show minor (grid) lines

	private int xAxis = 5;				// x-major-axis (major axis every n minor)
	private int yAxis = 2;				// y-major-axis (major axis every n minor)
	private int zAxis = 5;				// z-major-axis (major axis every n minor)
	
	private float xScale = 0.05f;		// x-minor-axis (minor axis every n units)
	protected float yScale = 0.1f;		// y-minor-axis (minor axis every n units)
	private float zScale = 0.05f;		// z-minor-axis (minor axis every n units)
	
	protected float maxYValue = -1f;      // largest y value in the graph (determine in createGraph)
	
	private FontInfo fontBold;      // bold font sprite sheet
	private FontInfo fontNormal;    // normal font sprite sheet
		
	private String xAxisTitle = "";         // output: title of x-axis
	private String yAxisTitle = "";         // output: title of y-axis
	private String zAxisTitle = "";         // output: title of z-axis
	
	/**
	 * Colors
	 */
	
	protected float[] color_bg = {0, 0, 0, 1};                  // background color
	protected float[] color_grid = {0.5f, 0.5f, 0.5f, 1f};      // background grid color (minor axis)
	protected float[] color_axis = {1f, 1f, 1f, 1f};	         // background grid color (major axis)
	
	protected float[] color_axis_title = {0, 0, 0, 1};          // axis title color
	protected float[] color_axis_label = {0, 0, 0, 1};          // axis label color	
	
	protected float[] color_sgrid = {0.75f, 0.75f, 0.75f, 1f};  // surface grid color
	protected float[] color_saxis = {1f, 1f, 1f, 1f};           // surface grid color
		
	/**
	 * Geometry : these objects consume memory on the GPU
	 */
		
	private GLText3D axisXtitle = null;  // static text: x-axis title
	private GLText3D axisYtitle = null;  // static text: y-axis title
	private GLText3D axisZtitle = null;  // static text: z-axis title
	
	private ArrayList<GLText3D> axisXlabel = new ArrayList<GLText3D>();  // static text: x-axis ticks
	private ArrayList<GLText3D> axisYlabel = new ArrayList<GLText3D>();  // static text: y-axis ticks
	private ArrayList<GLText3D> axisZlabel = new ArrayList<GLText3D>();  // static text: z-axis ticks
	
	private VertexArray gridNodes;      // geometry: background grid
	private VertexArray graphNodes;     // geometry: graph surface
	private VertexArray graphNodes2;    // geometry: graph surface grid
	
	/**
	 * Constructor
	 * @param c
	 * @param params
	 */
	public ThreeDGraph(Context c, Bundle params)
	{
		context = c;

		// read graph input/settings
		units_mass = params.getString("unitsmass");
		units_force = params.getString("unitsforce");
		
		fallFactor = params.getDouble("fallfactor");
		if (fallFactor < 0) fallFactor = 0;
		else if (fallFactor > 2) fallFactor = 2;
		
		impactRating = params.getDouble("impactrating");
		if (impactRating < ThreeDGraphRenderer.MIN_IMPACTRATING) impactRating = 9;
		if (impactRating > ThreeDGraphRenderer.MAX_IMPACTRATING) impactRating = 12;
		
		mass = params.getDouble("mass");
		if (units_mass.equals("kg"))
		{
			if (mass <= 0) mass = 80;
			if (mass > 100) mass = 100;
		} else {
			if (mass <= 0) mass = 176.37;
			if (mass > 220.462) mass = 220.462;
		}	
		
		SharedPreferences settings = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
		
		xAxisTitle = "Fall Factor";
		yAxisTitle = "Force (" + units_force + ")";
		zAxisTitle = (units_mass.equals("lb")) ? "Weight (" + units_mass + ")" : 
												 "Mass (" + units_mass + ")";
		
		setForceMode(settings.getInt("forcemode", MODE_CLIMBER));
		setGridMode(settings.getInt("gridmode", GRID_ALLLINES));

		int[] attrs = new int[] { R.attr.graphBackgroundColor,     // 0
				R.attr.graphDomainTitleColor,    // 1
				R.attr.graphDomainLabelColor,    // 2
				R.attr.graphGridColor,           // 3
				R.attr.graphLineColor,           // 4
				R.attr.graphPointColor };        // 5 
		TypedArray a = context.obtainStyledAttributes(attrs);
				
		ThreeDGraphRenderer.setGLColor(a.getColor(1, Color.WHITE), color_axis_title);
		ThreeDGraphRenderer.setGLColor(a.getColor(2, Color.WHITE), color_axis_label);
		ThreeDGraphRenderer.setGLColor(a.getColor(3, Color.GRAY), color_grid);
		ThreeDGraphRenderer.setGLColor(a.getColor(5, Color.GRAY), color_sgrid);
		ThreeDGraphRenderer.setGLColor(a.getColor(4, Color.WHITE), color_axis);
		ThreeDGraphRenderer.setGLColor(a.getColor(4, Color.WHITE), color_saxis);
		
		ThreeDGraphRenderer.setGLColor(a.getColor(0, Color.BLACK), color_bg);

		a.recycle();
	}
		
	public void setFallFactor(double f)
	{
		fallFactor = f;
	}
	
	public void setMass( double m )
	{
		mass = m;
	}
	
	public void setImpactRating( double i )
	{
		impactRating = i;
	}
	
	public int getGridMode()
	{
		return gridmode;
	}
	
	public void setGridMode( int mode )
	{		
		gridmode = mode;
		switch (mode)            // no grid
		{
		case GRID_NOLINES:
			flag_showMajorGrid = false;
			flag_showMinorGrid = false;
			break;
			
		case GRID_MAJORLINES:
			flag_showMajorGrid = true;
			flag_showMinorGrid = false;
			break;
			
		case GRID_ALLLINES:
		default:
			flag_showMajorGrid = true;
			flag_showMinorGrid = true;
			break;	
		}
	}
	
	public int getForceMode()
	{
		return forcemode;
	}
	public void setForceMode( int mode )
	{
		forcemode = mode;
	}	
	
	public void reset()
	{		
		gridNodes.clearNodes();
		graphNodes.clearNodes();
		graphNodes2.clearNodes();
		populateBuffers();
	}
	
	/**
	 * Initialize buffers / allocate memory on the GPU.
	 */
	public void initBuffers()
	{	
		fontBold = new FontInfo();
		fontNormal = new FontInfo();
				
		gridNodes = new VertexArray(MAX_NODES);
		graphNodes = new VertexArray(MAX_NODES);
		graphNodes2 = new VertexArray(MAX_NODES);
		
		axisXtitle = new GLText3D(fontBold, xAxisTitle);
		axisYtitle = new GLText3D(fontBold, yAxisTitle);
		axisZtitle = new GLText3D(fontBold, zAxisTitle);
	}
	
	/**
	 * Populate buffers / fill memory previously reserved on the GPU with graph
	 * geometry, text, etc.
	 */
	private void populateBuffers()
	{		
		//long n1 = System.currentTimeMillis();		

		createGraph();
		initGrid();
		
		axisXtitle.setColor(color_axis_title);
		axisXtitle.setText(xAxisTitle);
		axisXtitle.setScale(0.01f);
		axisXtitle.setPosition(-0.5f, -0.10f, 0.20f);
		axisXtitle.setRotation(-45, 0, 0);
		if (axisXtitle.ready) axisXtitle.genTextC(axisXtitle.v.textureId, 0, 0);
				
		axisYtitle.setColor(color_axis_title);
		axisYtitle.setText(yAxisTitle);
		axisYtitle.setScale(0.01f);
		axisYtitle.setPosition(-1f, maxYValue / 2.0f, 0.20f);
		axisYtitle.setRotation(-45, 0, -90);
		if (axisYtitle.ready) axisYtitle.genTextC(axisYtitle.v.textureId, 0, 0);
				
		axisZtitle.setColor(color_axis_title);
		axisZtitle.setText(zAxisTitle);
		axisZtitle.setScale(0.01f);
		axisZtitle.setPosition(0.20f, -0.10f, -0.5f);
		axisZtitle.setRotation(-45, 90, 0);	
		if (axisZtitle.ready) axisZtitle.genTextC(axisZtitle.v.textureId, 0, 0);
						
		//long n2 = System.currentTimeMillis();
		//System.out.println("profile: " + (n2 - n1));
	}

	/**
	 * Draw Loop - the OpenGL draw loop where the GL context lives.
	 */
	//@Override
	public void onDrawFrame(GL10 gl) 
	{	
		gridNodes.drawNodes(gl, GL10.GL_LINES);
		graphNodes.drawNodes(gl, GL10.GL_TRIANGLES);
		graphNodes2.drawNodes(gl, GL10.GL_LINES);
		
		if (axisXtitle.ready) axisXtitle.draw(gl);	
		if (axisYtitle.ready) axisYtitle.draw(gl);
		if (axisZtitle.ready) axisZtitle.draw(gl);
		
		// This style of for loop uses an iterator, allocating heap memory
		// within the draw loop! Refactor these to avoid use of an iterator.
		//for ( GLText3D text3D : axisXlabel)
		//{
		//	if (text3D.ready) text3D.draw(gl);
		//}
		
		int numXlabels = axisXlabel.size();
		for (int i=0; i<numXlabels; i++)
		{
			GLText3D text3D = axisXlabel.get(i);
			if (text3D.ready) text3D.draw(gl);
		}
		
		int numZlabels = axisZlabel.size();
		for (int i=0; i<numZlabels; i++)
		{
			GLText3D text3D = axisZlabel.get(i);
			if (text3D.ready) text3D.draw(gl);
		}
		
		int numYlabels = axisYlabel.size();
		for (int i=0; i<numYlabels; i++)
		{
			GLText3D text3D = axisYlabel.get(i);
			if (text3D.ready) text3D.draw(gl);
		}
	}

	/**
	 * onSurfaceCreated
	 */
	//@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig arg1) 
	{		
		reset();
		gl.glClearColor(color_bg[0], color_bg[1], color_bg[2], color_bg[3]);
		gl.glPointSize(3);
		
		int textureIdB = fontBold.load(gl, null, Typeface.BOLD, 14, 2, 2);
		int textureIdN = fontNormal.load(gl, null, Typeface.BOLD, 7, 2, 2);
		
		axisXtitle.genTextC(textureIdB, 0, 0);
		axisXtitle.ready = true;
		
		axisYtitle.genTextC(textureIdB, 0, 0);
		axisYtitle.ready = true;
		
		axisZtitle.genTextC(textureIdB, 0, 0);
		axisZtitle.ready = true;
		
		for ( GLText3D text3D : axisXlabel)
		{
			text3D.genTextC(textureIdN, 0, 0);
			text3D.ready = true;
		}
		
		for ( GLText3D text3D : axisYlabel)
		{
			text3D.genTextC(textureIdN, 0, 0);
			text3D.ready = true;
		}
		
		for ( GLText3D text3D : axisZlabel)
		{
			text3D.genTextC(textureIdN, 0, 0);
			text3D.ready = true;
		}
	}
		
	private void initGrid()
	{
		float h = 0;   // determine grid height
		do {
			h += (yScale * yAxis);
		} while (h < maxYValue);	
		
		// bottom grid panel
		int c = 0;   // minor tick counter
		int lc = 0;  // major tick counter
		boolean emptyLabels = (axisXlabel.size() <= 0);
		for (float x=0; x>=-1-xScale; x=x-xScale)
		{
			float[] colors = color_grid;
			if (c % xAxis == 0)
			{				
				colors = color_axis;
				
				GLText3D text3D;
				if (emptyLabels)
				{
					text3D = new GLText3D(fontNormal);
					axisXlabel.add(text3D);
				} else {
					text3D = axisXlabel.get(lc); 
				}
						
				if (x == 0) text3D.setText("0");
				else text3D.setText(String.format("%.1f", Math.abs(2*x)));
								
				text3D.setPosition(x, -0.02f, 0.08f);
				text3D.setScale(0.01f);
				text3D.setRotation(45, 90, 90);
				text3D.setColor(color_axis_label);
				
				if (text3D.ready)
				{
					text3D.genTextC(text3D.fontInfo.textureId, 0, 0);
				}

				lc++;
			}
			gridNodes.addNode(x, 0, 0, colors);		
			gridNodes.addNode(x, 0, -1, colors);
			c++;
		}

		c = 0;
		lc = 0;
		emptyLabels = (axisZlabel.size() <= 0);
		boolean unitsKg = units_mass.equals("kg");
		for (float z=0; z>=-1-zScale; z=z-zScale)
		{		
			float[] colors = color_grid;
			if (c % zAxis == 0)
			{
				colors = color_axis;
				
				GLText3D text3D;
				if (emptyLabels)
				{
					text3D = new GLText3D(fontNormal);
					axisZlabel.add(text3D);
				} else {
					text3D = axisZlabel.get(lc); 
				}
				
				float wValue = Math.abs(z) * 100;
				if (!unitsKg) wValue *= 2.20462f;
				
				if (z == 0) text3D.setText("0");
				else text3D.setText(String.format("%.0f", wValue));
								
				text3D.setPosition(0.08f, -0.02f, z);
				text3D.setScale(0.01f);
				text3D.setRotation(-45, 0, 0);
				text3D.setColor(color_axis_label);
				
				if (text3D.ready)
				{
					text3D.genTextC(text3D.fontInfo.textureId, 0, 0);
				}
				
				lc++;
			}
			gridNodes.addNode(0, 0, z, colors);
			gridNodes.addNode(-1, 0, z, colors);
			c++;
		}
		
		// left grid panel
		c = 0;
		lc = 0;
		
		emptyLabels = (axisYlabel.size() <= 0);
		for (GLText3D text3d : axisYlabel)
		{
			text3d.setText("");
		}
		
		boolean unitsAreLbs = units_force.equals("lb");
		
		for (float y=0; y<=h+0.01; y=y+yScale)
		{
			float[] colors = color_grid;
			if (c % yAxis == 0)
			{
				colors = color_axis;
							
				if (y != 0)
				{					
					GLText3D text3D;
					if (emptyLabels)
					{
						text3D = new GLText3D(fontNormal);
						axisYlabel.add(text3D);
						
					} else {
						if (lc < axisYlabel.size())
						{
							text3D = axisYlabel.get(lc); 
						} else {
							text3D = new GLText3D(fontNormal);
							text3D.ready = true;
							axisYlabel.add(text3D);
						}
					}
										
					double labelValue = ((unitsAreLbs) ? y*10*UnitsUtility.LB_IN_KN : y*10);
					text3D.setText(String.format("%.0f", labelValue));
					text3D.setPosition(-1, y, ((unitsAreLbs) ? 0.08f : 0.05f) );
					text3D.setScale(0.01f);
					text3D.setRotation(0, 45, 0);
					text3D.setColor(colorizeValue(y*10));
					
					if (text3D.ready)
					{
						text3D.genTextC(text3D.fontInfo.textureId, 0, 0);
					}
					
					lc++;
				}
			}
			gridNodes.addNode(-1, y, 0, colors);
			gridNodes.addNode(-1, y, -1, colors);
			c++;
		}
		
		c = 0;
		for (float z=0; z>=-1-zScale; z=z-zScale)
		{
			float[] colors = color_grid;
			if (c % zAxis == 0) colors = color_axis;
			gridNodes.addNode(-1, h, z, colors);
			gridNodes.addNode(-1, 0, z, colors);
			c++;
		}

		// back grid panel
		c = 0;
		for (float x=0; x>=-1-xScale; x=x-xScale)
		{
			float[] colors = color_grid;
			if (c % xAxis == 0) colors = color_axis;
			gridNodes.addNode(x, 0, -1, colors);
			gridNodes.addNode(x, h, -1, colors);
			c++;
		}

		c = 0;
		for (float y=0; y<=h+0.01; y=y+yScale)
		{
			float[] colors = color_grid;
			if (c % yAxis == 0) colors = color_axis;
			gridNodes.addNode(0, y, -1, colors);	
			gridNodes.addNode(-1, y, -1, colors);
			c++;
		}
	}

	/**
	 * colorizeValue(float) : float[]
	 * @param value a float value to assign a color to
	 * @return an array of float containing [0,1] rgba color values
	 */
	private float[] colorizeValue(float value)
	{
		float[] colors = new float[4];
		colors[0] = 1;
		colors[1] = 1;
		colors[2] = 1;
		colors[3] = 1;

		float stop1 = 2;   // cyan
		float stop2 = 6;   // green
		float stop3 = 8;   // yellow
		float stop4 = 12;  // red
		
		if (value < stop1) 
		{
			// dark blue to cyan
			colors[0] = 0;
			colors[1] = value / stop1;
			colors[2] = 1;

		} else if (value >= stop1 && value < stop2) {
			// cyan to green
			colors[0] = 0;
			colors[1] = 1;
			colors[2] = 1 - (value - stop1) / (stop2 - stop1);
			
		} else if (value >= stop2 && value < stop3) {
			// green to yellow
			colors[0] = (value - stop2) / (stop3 - stop2);
			colors[1] = 1;
			colors[2] = 0;
			
		} else if (value >= stop3 && value < stop4) {
			// yellow to red
			colors[0] = 1;
			colors[1] = 1 - (value - stop3) / (stop4 - stop3);
			colors[2] = 0;
	
		} else {
			// red to dark red
			float rV = 1 - ((value - stop4) / stop4);
			colors[0] = rV < 0.5 ? 0.5f : rV;
			colors[1] = 0;
			colors[2] = 0;
		}
		
		return colors;
	}
	private float[] colorizeValue2(float value)
	{
		float[] colors = new float[4];
		colors[0] = 1;
		colors[1] = 1;
		colors[2] = 1;
		colors[3] = 1;

		float stop1 = 3;   // cyan location
		float stop2 = 4;   // green location
		float stop3 = 8;   // yellow location

		if (value < stop1)
		{
			// dark blue to cyan
			colors[0] = 0;
			colors[1] = value / stop1;
			colors[2] = 1;

		} else if (value >= stop1 && value < stop2) {
			// cyan to green
			colors[0] = 0;
			colors[1] = 1;
			colors[2] = 1 - (value - stop1) / (stop2 - stop1);
			
		} else if (value >= stop2 && value < stop3) {
			// green to yellow
			colors[0] = (value - stop2) / (stop3 - stop2);
			colors[1] = 1;
			colors[2] = 0;
			
		} else {
			// yellow to red
			colors[0] = 1;
			colors[1] = 1 - (value - stop3) / (stop3 - stop2);
			colors[2] = 0;	
		}
		
		return colors;
	}
	
	/**
	 * createGraph()  :  void
	 * 
	 *   x3,y4,z4     -     x3,y3,z3
	 *      | 		  /			|
	 *   x1,y1,z1     -     x2,y2,z2
	 */
	protected void createGraph()
	{	
		maxYValue = -1f;
		graphNodes.clearNodes();
		graphNodes2.clearNodes();
				
		RopeModulus modulus = new RopeModulus(impactRating);
		double k = modulus.getRopeModulus().doubleValue();
				
		int i = 0;
		for (float f = -1*xScale; f >= (-1 - xScale); f = f-xScale)
		{
			float l = f + xScale;
			float lF = Math.abs(2*l);
			float lN = Math.abs(2*f);
			
			int j = 1;
			for (float m = -1 * zScale; m >= -1; m = m - zScale)
			{				
				float wl = 9.8f * ((Math.abs(m) * 100f) / 1000);	
				float wn = 9.8f * ((Math.abs((m - zScale)) * 100) / 1000);

				float f1 = ModelWexler.computeForce(forcemode, wl, k, lF);
				float x1 = l;
				float z1 = m;
				float y1 = yScale * f1;
				float[] c1 = colorizeValue(f1);
											
				float f2 = ModelWexler.computeForce(forcemode, wn, k, lF);
				float x2 = l;
				float z2 = m-zScale;
				float y2 = yScale * f2;
				float[] c2 = colorizeValue(f2);
				
				float f3 = ModelWexler.computeForce(forcemode, wn, k, lN);
				float x3 = f;
				float z3 = m-zScale;
				float y3 = yScale * f3;
				float[] c3 = colorizeValue(f3);
								
				float f4 = ModelWexler.computeForce(forcemode, wl, k, lN);
				float x4 = f;
				float z4 = m;
				float y4 = yScale * f4;
				float[] c4 = colorizeValue(f4);
				
				// track the largest y-value (use node 3)
				if (y3 > maxYValue) maxYValue = y3;
												
				// triangle 1
				graphNodes.addNode(x1, y1, z1, c1);
				graphNodes.addNode(x2, y2, z2, c2);
				graphNodes.addNode(x3, y3, z3, c3);
				
				// color triangle 2
				graphNodes.addNode(x1, y1, z1, c1);
				graphNodes.addNode(x3, y3, z3, c3);
				graphNodes.addNode(x4, y4, z4, c4);
				
				// box wireframe surface grid - x axis
				if (i % xAxis == 0 && flag_showMajorGrid)
				{
					graphNodes2.addNode(x1, y1+0.02f, z1, color_saxis);
					graphNodes2.addNode(x2, y2+0.02f, z2, color_saxis);			

				} else if (flag_showMajorGrid && flag_showMinorGrid) {
					graphNodes2.addNode(x1, y1+0.02f, z1, color_sgrid);
					graphNodes2.addNode(x2, y2+0.02f, z2, color_sgrid);
				}			

				// box wireframe surface grid - z axis
				if (j % zAxis == 0 && flag_showMajorGrid)
				{
					graphNodes2.addNode(x4, y4+0.02f, z4, color_saxis);
					graphNodes2.addNode(x1, y1+0.02f, z1, color_saxis);

				} else if (flag_showMajorGrid && flag_showMinorGrid){
					graphNodes2.addNode(x4, y4+0.02f, z4, color_sgrid);
					graphNodes2.addNode(x1, y1+0.02f, z1, color_sgrid);
				}	

				j++;
			}
			
			i++;
		}
	}
		
	protected String[] generateQueryPoint(float[] qpoint)
	{
		Weight weight = (units_mass.equals("kg")) ? new Weight(mass) : new Weight(mass, Weight.UNITS_LBS);
    	FallFactor fall_factor = new FallFactor(fallFactor);
    	RopeModulus modulus = new RopeModulus(impactRating);
    	ModelWexler forces = new ModelWexler(weight, modulus, fall_factor);
		
    	double t = 0;		// calculate force T				
		switch (forcemode)
		{
		case MODE_ANCHOR:
			t = forces.getForceOnAnchor().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			break;
				
		case MODE_BELAYER:
			t = forces.getForceOnBelayer().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			break;
			
		case MODE_CLIMBER:
		default:
			t = forces.getForceOnClimber().setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue();
			break;
		}
		
		double _t = t;
		if (units_force.equals("lb"))
		{
			t *= UnitsUtility.LB_IN_KN;
			t = ThreeDGraphRenderer.roundToDecimals(t, 2);
		}
		
		float massKg = weight.getMass().floatValue() * 1000;
		
		qpoint[0] = -1f * ((float)fallFactor / 2f);	       // x - fallfactor, ..
		qpoint[1] = (yScale * (float)_t) + yScale/5; 	   // y - force
		qpoint[2] = -1f * (10f * massKg) / (9.8f * 100f);  // z - mass, ...
		
		// return formatted output
		String[] r = new String[2];
		r[0] = "r = " + ThreeDGraphRenderer.roundToDecimals(fallFactor,2) + ((units_mass.equals("kg")) ? ", m = " : ", w = ") + ThreeDGraphRenderer.roundToDecimals(mass,2) + " " + units_mass;
		r[1] = t + " " + units_force;
		return r;
	}
	

}

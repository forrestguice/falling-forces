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

/**
 * Stuff I don't hold copyright for that's in this file...
 * -> savePixels method by "eugenk" (http://www.anddev.org/how_to_get_opengl_screenshot__useful_programing_hint-t829.html)
 * -> gluUnProject method by "Streets of Boston" (https://groups.google.com/forum/?fromgroups=#!topic/android-developers/nSv1Pjp5jLY)
 */

package com.forrestguice.fallforces.lib.graph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.forrestguice.fallforces.lib.R;
import com.forrestguice.fallforces.model.ModelWexler;
import com.forrestguice.fallforces.model.RopeModulus;
import com.forrestguice.glstuff.Camera;
import com.forrestguice.glstuff.gle.GLSurfaceView;
import com.forrestguice.glstuff.gle.MatrixGrabber;
import com.forrestguice.glstuff.Vector3D;
import com.forrestguice.glstuff.VertexArray;
import com.forrestguice.glstuff.gltext.GLText;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.GLU;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.TypedValue;
import android.view.MotionEvent;

public class ThreeDGraphRenderer implements GLSurfaceView.Renderer
{
	public ThreeDGraph graph;
		
	public static final int MIN_IMPACTRATING = 2;
	public static final int AVG_IMPACTRATING = 10;
	public static final int MAX_IMPACTRATING = 20;

	public static final int PLOT_FORCES1 = 1;   // plot: force vs fallfactor and mass
	public static final int PLOT_FORCES2 = 2;   // plot: force vs fallfactor and impactRating
	
	private Context context;	    // parent context (usually Activity)
	
	public float fov = 60.0f;       // field of view (degrees)
	public float near = 1.0f;       // near viewing plane
	public float far = 100.0f;      // far viewing plane
	private PointF surfaceSize;     // 2D window/surface size
	private MatrixGrabber matrices; // local copy of the modelview/projection matrices
	private Camera camera;
	
	private PointF touchStart;
	
	/**
	 * Text Display
	 */

	private int text_size1 = 18;    // font: large pt size
	private int text_size2 = 16;    // font: normal pt size
	private int text_size3 = 14;    // font: small pt size
	
	/**
	 * Input Variables
	 */
		
	private double fallFactor = 1.78;          // input: selected fallFactor
	private double mass = 80;                  // input: mass/weight
	private double impactRating = 8;           // input: impact rating
	private String units_mass = "kg";          // input: mass/weight units
	private String units_force = "kN";         // input: force units
	private float graphRotation = 0;           // input: graph rotation
	
	private int gridmode = ThreeDGraph.GRID_ALLLINES;
	private int forcemode = ThreeDGraph.MODE_CLIMBER;
	private int plotmode = PLOT_FORCES1;
	
	/**
	 * Ouput Variables
	 */
	
	private String graphTitle1;             // output: main title
	private String graphTitle2;             // output: secondary title
		
	private String query_result = "";       // output: result of query
	private String query_input = "";        // output: query input vars
	private float[] query_point = {0,0,0};  // output: x,y,z to highlight

	private boolean buttonPressed = false;     // output: button is pressed
	private float[] buttonBox = new float[4];  // output: button bounding box
	
	/**
	 * Draw Loop : variables used to control the draw loop
	 */
	private boolean renderReady = false;         // renderReady: draw loop skips an iteration when false
	private boolean rendering = false;           // rendering: draw loop is busy with an iteration
	public boolean resetRequested = false;
	
	private String screenshotPath = null;        // generate a screenshot when non-null
	private String screenshotExt = "png";   
	
	/**
	 * Colors
	 */

	private float[] color_query_point = {1, 0, 1, 1};         // selected point color
	
	private float[] color_button = {0.5f,0.5f,0.5f,0.1f};     // normal button color
	private float[] color_button_pressed = {0,0.75f,0.25f,1f};// pressed button color	
		
	/**
	 * Geometry : these objects consume memory on the GPU
	 */
	
	private GLText textBold = null;     // dynamic text (bold)
	private GLText textNormal = null;   // dynamic text (normal)
	private GLText textSmall = null;    // dynamic text (small)
	
	private VertexArray queryPoint;     // geometry: selected graph point
	private VertexArray queryLine;      // geometry: graph point selector
	
	private VertexArray buttonNodes1;   // geometry: button (normal)
	private VertexArray buttonNodes2;   // geometry: button (pressed)
	
	/**
	 * Constructor
	 * @param c
	 * @param params
	 */
	public ThreeDGraphRenderer(Context c, Bundle params)
	{
		context = c;
		graph = new ThreeDGraph(c, params);

		// convert font dimensions from sp to px
		text_size1 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, text_size1, context.getResources().getDisplayMetrics());
		text_size2 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, text_size2, context.getResources().getDisplayMetrics());
		text_size3 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, text_size3, context.getResources().getDisplayMetrics());
				
		// gl / scene stuff
		camera = new Camera(1f, 1f, 1f, 45f, -45f);
		surfaceSize = new PointF();
		touchStart = new PointF();
		matrices = new MatrixGrabber();
		
		// read graph input/settings
		units_mass = params.getString("unitsmass");
		units_force = params.getString("unitsforce");
		
		fallFactor = params.getDouble("fallfactor");
		if (fallFactor < 0) fallFactor = 0;
		else if (fallFactor > 2) fallFactor = 2;
		
		impactRating = params.getDouble("impactrating");
		if (impactRating < MIN_IMPACTRATING) impactRating = 9;
		if (impactRating > MAX_IMPACTRATING) impactRating = 12;
		
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
		setPlotMode(settings.getInt("plotmode", PLOT_FORCES1));
		setForceMode(settings.getInt("forcemode", ThreeDGraph.MODE_CLIMBER));
		setGridMode(settings.getInt("gridmode", ThreeDGraph.GRID_ALLLINES));

		int[] attrs = new int[] { R.attr.graphBackgroundColor,     // 0
				R.attr.graphDomainTitleColor,    // 1
				R.attr.graphDomainLabelColor,    // 2
				R.attr.graphGridColor,           // 3
				R.attr.graphLineColor,           // 4
				R.attr.graphPointColor };        // 5 
		TypedArray a = context.obtainStyledAttributes(attrs);
		
		//setGLColor(a.getColor(0, Color.BLACK), color_bg);
		//setGLColor(a.getColor(1, Color.WHITE), color_axis_title);
		//setGLColor(a.getColor(2, Color.WHITE), color_axis_label);
		//setGLColor(a.getColor(3, Color.GRAY), color_grid);
		//setGLColor(a.getColor(5, Color.GRAY), color_sgrid);
		//setGLColor(a.getColor(4, Color.WHITE), color_axis);
		//setGLColor(a.getColor(4, Color.WHITE), color_saxis);
		setGLColor(a.getColor(5, Color.GRAY), color_button);
		setGLColor(a.getColor(4, Color.WHITE), color_query_point);
		
		a.recycle();
		
		// reserve GPU memory for graph geometry
		initBuffers();
	}
	
	/**
	 * Move down a ray (from start to end) searching for a point of collision
	 * with the graph. On collision, use the resulting x and y to set the
	 * fallFactor and mass variables, then call refreshQueryDisplay().
	 * @param ray two points (start, end) in an array (float[6])
	 */
	public void setQueryFromTouch(float[] ray)
	{
		RopeModulus modulus = new RopeModulus(impactRating);
		double k = modulus.getRopeModulus().doubleValue();
		
		// the start point of the ray
		float[] point_start = new float[3];
		point_start[0] = ray[0];
		point_start[1] = ray[1];
		point_start[2] = ray[2];
		
		// the end point of the ray
		float[] point_end = new float[3];
		point_end[0] = ray[3];
		point_end[1] = ray[4];
		point_end[2] = ray[5];
		
		float[] ray_direction = new float[3];
		ray_direction[0] = point_end[0] - point_start[0];
		ray_direction[1] = point_end[1] - point_start[1];
		ray_direction[2] = point_end[2] - point_start[2];
	
		double ray_length = Vector3D.magnitude(ray_direction);
		
		Vector3D.normalize(ray_direction);  // normalized direction vector
		
		// step through the ray looking for intersection with the graph
		float[] point_c = new float[3];
		for (float i=0; i<ray_length; i=i+0.01f)
		{
			point_c[0] = point_start[0] + i * ray_direction[0];
			point_c[1] = point_start[1] + i * ray_direction[1];
			point_c[2] = point_start[2] + i * ray_direction[2];
					
			if (point_c[0] < -1.01 || point_c[2] < -1.01)
			{
				// behind the graph; failed to find intersection
				return;
			}
			
			if (point_c[0] <= 0 && point_c[2] <= 0)
			{   // in graph space; check for collision.
				// find fall factor at ray point - x
				float x = Math.abs(point_c[0]) * 2;  
				if (x > 2) x = 2;
				else if (x < 0) x = 0;
				
				// find height value of ray point - y
				float y = Math.abs(point_c[1]);      
								
				// find mass at ray point - z
				float z = 9.8f * ((Math.abs(point_c[2]) * 100f) / 1000f);
				if (z > 100) z = 100;
				else if (z < 0) z = 0;
								
				// the force (y value) that would be graphed for x and y
				float f = graph.yScale * ModelWexler.computeForce(forcemode, z, k, x);
				
				// the difference between the calculated force value and the 
				// y value at this position along the ray is ...
				float d = Math.abs(y-f);
				
				//System.out.println("y: " + y + ", " + f + " :: " + d);
				
				if (d < 0.05)
				{	
					// close enough to call it a match - set query variables
					float massInKg = z * 100;
					
					setFallFactor(x);
					//fallFactor = x;
					setMass(units_mass.equals("kg") ? massInKg : massInKg * 2.20462);
					//mass = (units_mass.equals("kg")) ? massInKg : massInKg * 2.20462;	
					generateQueryDisplay();
					break;
				}
				
			}
		}
	}
	
	public void setFallFactor(double f)
	{
		fallFactor = f;
		graph.setFallFactor(f);
	}
	
	public void setMass( double m )
	{
		mass = m;
		graph.setMass(m);
	}
	
	/**
	 * Refresh the 'query' display - the text in the lower right hand corner
	 * that displays currently selected variables / values.
	 */
		
	private void generateQueryDisplay()
	{			
		String[] qInfo = graph.generateQueryPoint(query_point);
		query_input = qInfo[0];
		query_result = qInfo[1];	

		if (renderReady)
		{
			// regenerate button geometry
			generateButtonGeometry(surfaceSize.x, 0, textSmall.getLength(query_input) + 5 + 5, textSmall.getHeight()*2 + 5 + 2 + 2);
		}
		
		// query point geometry
		queryPoint.node_v.position(0);
		queryPoint.node_v.put(query_point);
				
		// query line geometry
		float[] query_line = new float[6];
		query_line[0] = query_point[0];
		query_line[1] = query_point[1];
		query_line[2] = query_point[2];
		
		query_line[3] = query_point[0];
		query_line[4] = 100;
		query_line[5] = query_point[2];		
						
		queryLine.node_v.position(0);
		queryLine.node_v.put(query_line);
	}		

	public void requestReset()
	{
		resetRequested = true;
	}	
	
	public int getGridMode()
	{
		return gridmode;
	}
	
	public void setGridMode( int mode )
	{
		setGridMode(mode, false);
	}
	
	public void setGridMode(int mode, boolean save)
	{
		if (save)
		{
			SharedPreferences settings = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("gridmode", mode);
			editor.commit();
		}
		
		gridmode = mode;
		graph.setGridMode(mode);
	}
		
	/** Convert android color (32bit int) to an opengl color (float[4]) */
	public static void setGLColor( int color, float[] result )
	{
		float r = Color.red(color);
		float g = Color.green(color);
		float b = Color.blue(color);
		float a = Color.alpha(color);		
		
		result[0] = (r <= 0) ? 0 : r / 255f;   // r (0-1)
		result[1] = (g <= 0) ? 0 : g / 255f;   // g (0-1)
		result[2] = (b <= 0) ? 0 : b / 255f;   // b (0-1)
		result[3] = (a <= 0) ? 0 : a / 255f;   // a (0-1)
	}
		
	public int getPlotMode()
	{
		return plotmode;
	}
	public void setPlotMode( int mode )
	{
		setPlotMode(mode, false);
	}
	public void setPlotMode( int mode, boolean save )
	{
		if (save)
		{
			SharedPreferences settings = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("plotmode", mode);
			editor.commit();
		}
		
		plotmode = mode;
		// TODO: change plots here
		
		switch (plotmode)
		{
		case PLOT_FORCES2:
			//xAxisTitle = "Fall Factor";
			//yAxisTitle = "Force (" + units_force + ")";
			//zAxisTitle = "UIAA Rating (kN)";
			
			graphTitle2 = "Mass: " + mass + " " + units_mass;
			break;
			
		case PLOT_FORCES1:
		default:
			//xAxisTitle = "Fall Factor";
			//yAxisTitle = "Force (" + units_force + ")";
			//zAxisTitle = (units_mass.equals("lb")) ? "Weight (" + units_mass + ")" :
			//										 "Mass (" + units_mass + ")";
			
			graphTitle2 = "UIAA Rating: " + impactRating + " kN";
			break;
		}
	}
	
	public int getForceMode()
	{
		return forcemode;
	}
	public void setForceMode( int mode )
	{
		setForceMode(mode, false);
	}
	public void setForceMode( int mode, boolean save )
	{
		if (save)
		{
			SharedPreferences settings = ((Activity)context).getPreferences(Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt("forcemode", mode);
			editor.commit();
		}
		
		forcemode = mode;
		graph.setForceMode(mode);
		switch (forcemode)
		{
		case ThreeDGraph.MODE_ANCHOR:
			graphTitle1 = "Force on Anchor";
			break;
			
		case ThreeDGraph.MODE_BELAYER:
			graphTitle1 = "Force on Belayer";
			break;
			
		case ThreeDGraph.MODE_CLIMBER:
		default:
			graphTitle1 = "Force on Climber";
			break;
		}
	}	
	
	public void reset()
	{		
		renderReady = false;
		resetRequested = false;
		do {
			// wait for current draw iteration to complete
		} while (rendering);	
		
		graph.reset();		
		
		camera.y = (graph.maxYValue < 1) ? 1 : graph.maxYValue;
		float[] lookAtTarget = {-1, 0, -1};
		camera.lookAt(lookAtTarget);	
		generateQueryDisplay();
		
		renderReady = true;
	}
	
	/**
	 * Initialize buffers / allocate memory on the GPU.
	 */
	private void initBuffers()
	{	
		graph.initBuffers();
		
		queryPoint = new VertexArray(1);   // space for a single point
		queryPoint.addNode(query_point, color_query_point);
		
		queryLine = new VertexArray(2);    // space for three lines(six points)
		queryLine.addNode(query_point, color_query_point);
		queryLine.addNode(query_point, color_query_point);
		
		buttonNodes1 = new VertexArray(4);   // room for line loop of 4 lines
		buttonNodes1.addNode(0, 0, 0, color_button);
		buttonNodes1.addNode(0, 0, 0, color_button);
		buttonNodes1.addNode(0, 0, 0, color_button);
		buttonNodes1.addNode(0, 0, 0, color_button);
		
		buttonNodes2 = new VertexArray(4);   // room for triangle strip of 2 triangles
		buttonNodes2.addNode(0, 0, 0, color_button_pressed);
		buttonNodes2.addNode(0, 0, 0, color_button_pressed);
		buttonNodes2.addNode(0, 0, 0, color_button_pressed);
		buttonNodes2.addNode(0, 0, 0, color_button_pressed);
	}
	
	/**
	 * Draw Loop - the OpenGL draw loop where the GL context lives.
	 */
	@Override
	public void onDrawFrame(GL10 gl) 
	{	
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		if (!renderReady) return;   // not ready yet - cancel draw iteration
		if (resetRequested) reset();
		rendering = true;		
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);       // 3D POV
		gl.glLoadIdentity();	
		gl.glPushMatrix();
		camera.draw(gl);
					
		// transform graph coordinates by arbitrary rotation
		float rotation = graphRotation - 10;
		if (rotation >= 45) gl.glRotatef(45, 0, 1, 0);
		else if (rotation <= -45) gl.glRotatef(-45, 0, 1, 0);
		else gl.glRotatef(rotation, 0, 1, 0);
		
		gl.glPushMatrix();
		matrices.getCurrentState(gl);  // store current matrices for onTouch events
		gl.glPopMatrix();
		
		graph.onDrawFrame(gl);
			
		queryPoint.drawNodes(gl, GL10.GL_POINTS);
		if (buttonPressed) queryLine.drawNodes(gl, GL10.GL_LINES);
					
		gl.glPopMatrix();
		
		gl.glMatrixMode( GL10.GL_PROJECTION );   // Orthogonal POV
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glOrthof(0, surfaceSize.x, 0, surfaceSize.y, 1.0f, -1.0f);	
		
		gl.glEnable( GL10.GL_BLEND );
		gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		if (buttonPressed) buttonNodes2.drawNodes(gl, GL10.GL_TRIANGLE_STRIP);
		else buttonNodes1.drawNodes(gl, GL10.GL_TRIANGLE_STRIP);
		gl.glDisable( GL10.GL_BLEND );
		
		drawHUD(gl);
						
		gl.glMatrixMode( GL10.GL_PROJECTION );
		gl.glPopMatrix();
		
		if (screenshotPath != null)
		{
			// take a screenshot
			generateScreenshot(screenshotPath, gl);
			screenshotPath = null;
		}
		
		rendering = false;
	}

	/**
	   Draw Loop - draw an orthogonal hud
	*/
	public void drawHUD( GL10 gl )
	{	
		gl.glEnable( GL10.GL_BLEND );
		gl.glDisable(GL10.GL_DEPTH_TEST);
		
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glPushMatrix();
		gl.glLoadIdentity();
			
		gl.glMatrixMode( GL10.GL_MODELVIEW );
		gl.glPushMatrix();
		gl.glLoadIdentity();

		gl.glEnable( GL10.GL_TEXTURE_2D );
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		
		// draw graph title
		textBold.begin(graph.color_axis_title[0], graph.color_axis_title[1], graph.color_axis_title[2], graph.color_axis_title[3]);
		textBold.draw( graphTitle1, 5, 5 + textNormal.getHeight() + 2);
		textBold.end();
		
		// draw graph subtitle
		textNormal.begin(graph.color_axis_title[0], graph.color_axis_title[1], graph.color_axis_title[2], graph.color_axis_title[3]);
		textNormal.draw( graphTitle2, 5, 5);
		textNormal.end();
				
		// draw query part1: dependent var
		textSmall.begin(graph.color_axis_title[0], graph.color_axis_title[1], graph.color_axis_title[2], graph.color_axis_title[3]);
		textSmall.drawR( query_result, surfaceSize.x - 5, 5 + textSmall.getHeight() + 2);
		textSmall.end();
			
		// draw query part2: independent vars
		textSmall.begin(graph.color_axis_title[0], graph.color_axis_title[1], graph.color_axis_title[2], graph.color_axis_title[3]);
		textSmall.drawR( query_input, surfaceSize.x - 5, 5);
		textSmall.end();
				
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable( GL10.GL_TEXTURE_2D );
		gl.glDisable( GL10.GL_BLEND );	
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glPopMatrix();
		
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glPopMatrix();
		gl.glMatrixMode( GL10.GL_MODELVIEW );
	}

	/**
	 * onSurfaceChanged
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) 
	{			
		surfaceSize.set(width, height);
		gl.glViewport(0, 0, (int)surfaceSize.x, (int)surfaceSize.y);
				
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();               
		GLU.gluPerspective(gl, fov, (float)width / (float)height, near, far);
	    
		gl.glMatrixMode(GL10.GL_MODELVIEW); 
		gl.glLoadIdentity();
		
		generateButtonGeometry(surfaceSize.x, 0, textSmall.getLength(query_input) + 5 + 5, textSmall.getHeight()*2 + 5 + 2 + 2);
	}
	
	private void generateButtonGeometry(float lowerRightX, float lowerRightY, float width, float height)
	{	
		float x1 = lowerRightX - width;
		float y1 = lowerRightY;
		float z1 = 0;
		
		float x2 = lowerRightX;
		float y2 = lowerRightY;
		float z2 = 0;
		
		float x3 = lowerRightX - width;
		float y3 = lowerRightY + height;
		float z3 = 0;
		
		float x4 = lowerRightX;
		float y4 = lowerRightY + height;
		float z4 = 0;
				
		// button bounding box
		buttonBox[0] = x1;
		buttonBox[1] = y1;
		buttonBox[2] = x4;
		buttonBox[3] = y4;
		
		// button 1
		buttonNodes1.node_v.position(0);		
		buttonNodes1.node_v.put(x1);
		buttonNodes1.node_v.put(y1);
		buttonNodes1.node_v.put(z1);
		
		buttonNodes1.node_v.put(x2);
		buttonNodes1.node_v.put(y2);
		buttonNodes1.node_v.put(z2);
		
		buttonNodes1.node_v.put(x3);
		buttonNodes1.node_v.put(y3);
		buttonNodes1.node_v.put(z3);
		
		buttonNodes1.node_v.put(x4);
		buttonNodes1.node_v.put(y4);
		buttonNodes1.node_v.put(z4);
				
		// button 2
		buttonNodes2.node_v.position(0);
		buttonNodes2.node_v.put(x1);
		buttonNodes2.node_v.put(y1);
		buttonNodes2.node_v.put(z1);
		
		buttonNodes2.node_v.put(x2);
		buttonNodes2.node_v.put(y2);
		buttonNodes2.node_v.put(z2);
		
		buttonNodes2.node_v.put(x3);
		buttonNodes2.node_v.put(y3);
		buttonNodes2.node_v.put(z3);
		
		buttonNodes2.node_v.put(x4);
		buttonNodes2.node_v.put(y4);
		buttonNodes2.node_v.put(z4);
	}

	/**
	 * onSurfaceCreated
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig arg1) 
	{		
		reset();
		graph.onSurfaceCreated(gl, arg1);
		
		textBold = new GLText( gl, context.getAssets() );
		textBold.load( null, Typeface.BOLD,  text_size1, 2, 2 );
		
		textNormal = new GLText( gl, context.getAssets() );
		textNormal.load( null, Typeface.NORMAL, text_size2, 2, 2 );
		
		textSmall = new GLText( gl, context.getAssets() );
		textSmall.load( null, Typeface.NORMAL, text_size3, 2, 2 );
	}
	
	public void takeScreenshot( String path, String ext )
	{
		screenshotPath = path;  // a non-null path triggers screenshot in draw loop
		screenshotExt = ext;
	}
	
	private void generateScreenshot(String path, GL10 gl)
	{
		Bitmap bitmap = savePixels(gl);
		try
		{
			BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(path), 1024);
			
			if (screenshotExt.equals("jpg"))
			{				
				bitmap.compress(CompressFormat.JPEG, 100, output);
			} else {				
				bitmap.compress(CompressFormat.PNG, 100, output);	
			}		
			
			try
			{
				output.flush();
				
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			try
			{
				output.close();
								
			} catch (IOException e)	{
				e.printStackTrace();
				return;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}		
		
		registerScreenshot(path);
	}
	
	private void registerScreenshot(String path)
	{
		// register the new image with the gallery app
		File imageFile = new File(path);		
		ContentValues image = new ContentValues();

		image.put(Images.Media.TITLE, graphTitle1 + ", " + graphTitle2);
		image.put(Images.Media.DISPLAY_NAME, graphTitle1 + ", " + graphTitle2);
		image.put(Images.Media.DESCRIPTION, graphTitle1 + ", " + graphTitle2);
		//image.put(Images.Media.DATE_ADDED, "date added");
		//image.put(Images.Media.DATE_TAKEN, "date taken");
		//image.put(Images.Media.DATE_MODIFIED, "date modified");
		image.put(Images.Media.MIME_TYPE, (screenshotExt.equals("jpg")) ? "image/jpeg" : "image/png");
		image.put(Images.Media.ORIENTATION, 0);

		File parent = imageFile.getParentFile();
		String pPath = parent.toString().toLowerCase();
		String name = parent.getName().toLowerCase();
		image.put(Images.ImageColumns.BUCKET_ID, pPath.hashCode());
		image.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
		image.put(Images.Media.SIZE, imageFile.length());
		image.put(Images.Media.DATA, imageFile.getAbsolutePath());

		Uri result = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
	}

	/**
	 * A touch event passed to the renderer from the parent context.
	 */
	public boolean onTouchEvent(MotionEvent event) 
	{	
		switch (event.getAction()) 
		{
		case MotionEvent.ACTION_DOWN:
			float x = event.getX();
			float y = surfaceSize.y - event.getY();
			touchStart.set(event.getX(), event.getY());
			
			if (buttonPressed)
			{
				if (x >= buttonBox[0] && x <= buttonBox[2])
				{
					if (y >= buttonBox[1] && y <= buttonBox[3])
					{
						buttonPressed = false;
						return true;
					}
				}
				
				float[] ray = touchToWorld(event.getX(), event.getY());
				setQueryFromTouch(ray);
				return true;
				
			} else {
				// button not pressed: first touch; check for button press
				if (x >= buttonBox[0] && x <= buttonBox[2])
				{
					if (y >= buttonBox[1] && y <= buttonBox[3])
					{
						buttonPressed = true;
						return true;
					}
				}
				
				// wasn't pressed, we are in move mode - set touchStart 
				//touchStart.set(event.getX(), event.getY());
				return true;
			}
						
		case MotionEvent.ACTION_MOVE:
			if (!buttonPressed)
			{
				// move mode - move the graph
				move(event.getX() - touchStart.x, touchStart.y - event.getY());
				touchStart.set(event.getX(), event.getY());
			}
			return true;
			
		case MotionEvent.ACTION_UP:
			return false;	
		}
		
		return false;
	}	
	
	/**
	 * Move Action (typically called by onTouch etc)
	 */
	public void move(float xDelta, float yDelta) 
	{
		graphRotation += xDelta / 10;
		if (graphRotation < -35) graphRotation = -35;
		if (graphRotation > 55) graphRotation = 55;
		
		if (camera.rotationY < camera.sRotationY - 45) camera.rotationY = camera.sRotationY - 45;
		if (camera.rotationY > camera.sRotationY + 45) camera.rotationY = camera.sRotationY + 45;
	  	    
	    camera.y += yDelta / 100;
	    if (camera.y < 0) camera.y = 0;
	}
	
	/**
	 * Convert touch coordinates to screen coordinates to world coordinates. 
	 * Touch coordinates have an origin in the upper left; screen coordinates
	 * use an origin in the lower left; world coordinates use the z axis. 
	 * @param touchX touch screen x coordinate
	 * @param touchY touch screen y coordinate
	 * @return a float[6] : a ray (start point, end point) through touched world coordinates
	 */
	private float[] touchToWorld( float touchX, float touchY )
	{		
		float sx = touchX;                   // x stays the same
		float sy = surfaceSize.y - touchY;   // invert y coordinate
		int[] viewport = {0, 0, (int)surfaceSize.x, (int)surfaceSize.y};
		
		// cast a ray from the near plane (screen) to the far plane
		float[] ray = new float[6];
		gluUnProject(sx, sy, 0f, matrices.mModelView, 0, matrices.mProjection, 0, viewport, 0, ray, 0);
		gluUnProject(sx, sy, 1f, matrices.mModelView, 0, matrices.mProjection, 0, viewport, 0, ray, 3);
		return ray;
	}
	
	//private void addImageGallery( File file ) 
	//{
	//    ContentValues values = new ContentValues();
	//    values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
	//    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
	//    context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	//}
		

	
	/**
	 * Utility method - quickly round a double to an arbitrary number of decimal places
	 */
	public static double roundToDecimals(double d, int c)  
	{   
	   int temp = (int)(d * Math.pow(10 , c));  
	   return ((double)temp)/Math.pow(10 , c);  
	}

	//////////////////////////////////////////////////
	//////////////////////////////////////////////////
	//////////////////////////////////////////////////

	/**
	 * savePixels(GL10) : Bitmap
	 * unknown author / copyright but a possible source is http://www.anddev.org/how_to_get_opengl_screenshot__useful_programing_hint-t829.html posted by user eugenk
	 */
	public Bitmap savePixels(GL10 gl)
	{
		int width = (int)surfaceSize.x;
		int height = (int)surfaceSize.y;

		int b[] = new int[width * height];
		IntBuffer ib = IntBuffer.wrap(b);
		ib.position(0);
		gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);

		Bitmap glbitmap = Bitmap.createBitmap(b, width, height, Bitmap.Config.ARGB_4444);
		final float[] cmVals = { 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0 };

		Paint paint = new Paint();
		paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(cmVals))); // our R<->B swapping paint

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_4444); // the bitmap we're going to draw onto
		Canvas canvas = new Canvas(bitmap); // we draw to the bitmap through a canvas
		canvas.drawBitmap(glbitmap, 0, 0, paint); // draw the opengl bitmap onto the canvas, using the color swapping paint
		glbitmap = null; // we're done with glbitmap, let go of its memory

		// the image is still upside-down, so vertically flip it
		Matrix matrix = new Matrix();
		matrix.preScale(1.0f, -1.0f); // scaling: x = x, y = -y, i.e. vertically flip
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true); // new bitmap, using the flipping matrix
	}

	private static final float[] _tempGluUnProjectData = new float[40];
	private static final int     _temp_m   = 0;
	private static final int     _temp_A   = 16;
	private static final int     _temp_in  = 32;
	private static final int     _temp_out = 36;

	/**
	 * Map window coordinates to object coordinates. 
	 * 
	 * gluUnProject maps the specified window coordinates into object 
	 * coordinates using model, proj, and view. The result is stored in xyz.
	 *
	 * @author by "Streets of Boston" (posted to support forum at https://groups.google.com/forum/?fromgroups=#!topic/android-developers/nSv1Pjp5jLY)
	 *
	 * @param winx window coordinates X
	 * @param winy window coordinates Y
	 * @param winz window coordinates Z
	 * @param model the current modelview matrix
	 * @param offsetM the offset into the model array where the modelview maxtrix data starts.
	 * @param proj the current projection matrix
	 * @param offsetP the offset into the project array where the project matrix data starts.
	 * @param viewport the current view, {x, y, width, height}
	 * @param offsetV the offset into the view array where the view vector data starts.
	 * @param xyz the output vector {objX, objY, objZ}, that returns the computed object coordinates.
	 * @param offset the offset into the obj array where the obj vector data starts.
	 * @return A return value of GL10.GL_TRUE indicates success, a return value of GL10.GL_FALSE indicates failure.
	 */
	public static int gluUnProject(float winx, float winy, float winz, float model[], 
								   int offsetM, float proj[], int offsetP, int viewport[],
								   int offsetV,  float[] xyz, int offset) 
	{
	   /* Transformation matrices */ 
	   // float[] m = new float[16], A = new float[16]; 
	   // float[] in = new float[4], out = new float[4]; 

	   /* Normalize between -1 and 1 */ 
	   _tempGluUnProjectData[_temp_in]   = (winx - viewport[offsetV]) * 2f / viewport[offsetV+2] - 1.0f; 
	   _tempGluUnProjectData[_temp_in+1] = (winy - viewport[offsetV+1]) * 2f / viewport[offsetV+3] - 1.0f; 
	   _tempGluUnProjectData[_temp_in+2] = 2f * winz - 1.0f; 
	   _tempGluUnProjectData[_temp_in+3] = 1.0f; 

	   /* Get the inverse */ 
	   android.opengl.Matrix.multiplyMM(_tempGluUnProjectData, _temp_A,	proj, offsetP, model, offsetM); 
	   android.opengl.Matrix.invertM(_tempGluUnProjectData, _temp_m, _tempGluUnProjectData, _temp_A); 

	   android.opengl.Matrix.multiplyMV(_tempGluUnProjectData, _temp_out, _tempGluUnProjectData, _temp_m, _tempGluUnProjectData, _temp_in); 
	   if (_tempGluUnProjectData[_temp_out+3] == 0.0) 
	      return GL10.GL_FALSE; 

	   xyz[offset]  =  _tempGluUnProjectData[_temp_out  ] / _tempGluUnProjectData[_temp_out+3]; 
	   xyz[offset+1] = _tempGluUnProjectData[_temp_out+1] / _tempGluUnProjectData[_temp_out+3]; 
	   xyz[offset+2] = _tempGluUnProjectData[_temp_out+2] / _tempGluUnProjectData[_temp_out+3]; 
	   return GL10.GL_TRUE; 
	} 

}

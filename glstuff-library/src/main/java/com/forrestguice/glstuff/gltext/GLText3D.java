/**
 * Copyright 2012 by fractious (http://fractiousg.blogspot.com/2012/04/rendering-text-in-opengl-on-android.html)
 * contributed to the Public Domain (using CC0 1.0 public domain license).
 *
 * See http://creativecommons.org/publicdomain/zero/1.0/legalcode for the full license text.
 *
 * Also contains minor modifications made by Forrest Guice, Copyright 2012.
 */

// files, generates a font map (texture) from them, and allows rendering of
// text strings.
//
// NOTE: the rendering portions of this class uses a sprite batcher in order
// provide decent speed rendering. Also, rendering assumes a BOTTOM-LEFT
// origin, and the (x,y) positions are relative to that, as well as the
// bottom-left of the string to render.

package com.forrestguice.glstuff.gltext;

import com.forrestguice.glstuff.TextureRegion;
import com.forrestguice.glstuff.VertexArray;

import javax.microedition.khronos.opengles.GL10;
import android.content.res.AssetManager;
import android.graphics.Typeface;

public class GLText3D
{
	public static final int ORIENTATION_HORIZONTAL = 0;
	public static final int ORIENTATION_VERTICAL = 1;
	
	public FontInfo fontInfo;
	public VertexArray v;
	public float[][] color = {{1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}, {1, 1, 1, 1}};
	public boolean ready = false;
	
	private String text = "";
	
	public float[] textPosition = {0, 0, 0};
	public float[] textRotation = {0, 0, 0};

	public GLText3D()
	{
		fontInfo = new FontInfo();
		v = new VertexArray(4 * 256);  // 256 characters
		v.textureId = -1;
	}
	
	public GLText3D(String txt)
	{
		text = txt;
		fontInfo = new FontInfo();
		v = new VertexArray(4 * 256);  // 256 characters
		v.textureId = -1;
	}
	
	public GLText3D(FontInfo font)
	{
		fontInfo = font;
		v = new VertexArray(4 * 256);  // 256 characters
		v.textureId = -1;
	}
	
	public GLText3D(FontInfo font, String txt)
	{
		text = txt;
		fontInfo = font;
		v = new VertexArray(4 * 256);  // 256 characters
		v.textureId = -1;
	}
	
	public int load(GL10 gl, String family, int style, int size, int padX, int padY)
	{
		Typeface tf = Typeface.create(family, style);
		return fontInfo.load(gl, tf, size, padX, padY);
	}
	public int load(AssetManager assets, GL10 gl, String file, int size, int padX, int padY)
	{
		Typeface tf = Typeface.createFromAsset( assets, file );  // Create the Typeface from Font File
		return fontInfo.load(gl, tf, size, padX, padY);
	}
	public int load(GL10 gl, Typeface tf, int size, int padX, int padY) 
	{
		return fontInfo.load(gl, tf, size, padX, padY);
	}
	
	public void setColor( float[][] colors )
	{
		setColor(colors[0][0], colors[0][1], colors[0][2], colors[0][3],
				 colors[1][0], colors[1][1], colors[1][2], colors[1][3],
				 colors[2][0], colors[2][1], colors[2][2], colors[2][3],
				 colors[3][0], colors[3][1], colors[3][2], colors[3][3]);
	}
	
	public void setColor( float[] colors )
	{
		setColor(colors[0], colors[1], colors[2], colors[3],
				 colors[0], colors[1], colors[2], colors[3],
				 colors[0], colors[1], colors[2], colors[3],
				 colors[0], colors[1], colors[2], colors[3]);
	}
	
	public void setColor( float r1, float g1, float b1, float a1,
						  float r2, float g2, float b2, float a2,
						  float r3, float g3, float b3, float a3,
						  float r4, float g4, float b4, float a4 )
	{
		color[0][0] = r1;
		color[0][1] = g1;
		color[0][2] = b1;
		color[0][3] = a1;
		
		color[1][0] = r2;
		color[1][1] = g2;
		color[1][2] = b2;
		color[1][3] = a2;
		
		color[2][0] = r3;
		color[2][1] = g3;
		color[2][2] = b3;
		color[2][3] = a3;
		
		color[3][0] = r4;
		color[3][1] = g4;
		color[3][2] = b4;
		color[3][3] = a4;
	}


	public void genText(int texId, float x, float y, float z, int orientation)
	{
		v.clearNodes();
		v.textureId = texId;

		float chrHeight = fontInfo.cellHeight * fontInfo.scaleY;          // Calculate Scaled Character Height
		float chrWidth = fontInfo.cellWidth * fontInfo.scaleX;            // Calculate Scaled Character Width
		int len = text.length();                        // Get String Length
		x += ( chrWidth / 2.0f ) - ( fontInfo.fontPadX * fontInfo.scaleX );  // Adjust Start X
		y += ( chrHeight / 2.0f ) - ( fontInfo.fontPadY * fontInfo.scaleY );  // Adjust Start Y

		for ( int i = 0; i < len; i++ )  
		{              // FOR Each Character in String
			int c = (int)text.charAt( i ) - FontInfo.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
			if ( c < 0 || c >= FontInfo.CHAR_CNT )                // IF Character Not In Font
				c = FontInfo.CHAR_UNKNOWN;                         // Set to Unknown Character Index

			generateSprite( x, y, 0, chrWidth, chrHeight, fontInfo.charRgn[c] );  // Draw the Character

			if (orientation == ORIENTATION_HORIZONTAL)
			{
				x += ( fontInfo.charWidths[c] + fontInfo.spaceX ) * fontInfo.scaleX;    // Advance X Position by Scaled Character Width
			} else {
				y -= ( fontInfo.charHeight + fontInfo.spaceX/4 ) * fontInfo.scaleY;    // Advance Y Position by Scaled Character Height
			}
		}
	}

	public float genTextC(int texId, float x, float y, int orientation) 
	{
		float len = getLength( text );                  // Get Text Length
		genText(texId, x - ( len / 2.0f ), y - ( getCharHeight() / 2.0f ), 0, orientation);  // Draw Text Centered
		return len;                                     // Return Length
	}
	public float genTextC(int texId, float x, float y) 
	{
		float len = getLength( text );                  // Get Text Length
		genText(texId, x - ( len / 2.0f ), y - ( getCharHeight() / 2.0f ), 0, ORIENTATION_HORIZONTAL);  // Draw Text Centered
		return len;                                     // Return Length
	}
	public float genTextCX(int texId, float x, float y)  
	{
		float len = getLength( text );                  // Get Text Length
		genText(texId, x - ( len / 2.0f ), y, 0, ORIENTATION_HORIZONTAL);            // Draw Text Centered (X-Axis Only)
		return len;                                     // Return Length
	}
	public void genTextCY(int texId, float x, float y)  
	{
		genText(texId, x, y - ( getCharHeight() / 2.0f ), 0, ORIENTATION_HORIZONTAL);  // Draw Text Centered (Y-Axis Only)
	}  

	private void generateSprite(float x, float y, float z, float width, float height, TextureRegion region)  
	{
		float halfWidth = width / 2.0f;                 // Calculate Half Width
		float halfHeight = height / 2.0f;               // Calculate Half Height
		float x1 = x - halfWidth;                       // Calculate Left X
		float y1 = y - halfHeight;                      // Calculate Bottom Y
		float x2 = x + halfWidth;                       // Calculate Right X
		float y2 = y + halfHeight;                      // Calculate Top Y

		v.addNode(x1, y1, z, color[0][0], color[0][1], color[0][2], color[0][3], region.u1, region.v2);  // 0
		v.addNode(x2, y1, z, color[1][0], color[1][1], color[1][2], color[1][3], region.u2, region.v2);  // 1
		v.addNode(x1, y2, z, color[2][0], color[2][1], color[2][2], color[2][3], region.u1, region.v1);  // 2

		v.addNode(x1, y2, z, color[2][0], color[2][1], color[2][2], color[2][3], region.u1, region.v1);  // 2
		v.addNode(x2, y1, z, color[1][0], color[1][1], color[1][2], color[1][3], region.u2, region.v2);  // 1
		v.addNode(x2, y2, z, color[3][0], color[3][1], color[3][2], color[3][3], region.u2, region.v1);  // 3  
	}
	
	public void setPosition( float x, float y, float z )
	{
		textPosition[0] = x;
		textPosition[1] = y;
		textPosition[2] = z;
	}
	
	public void setRotation(float x, float y, float z)
	{
		textRotation[0] = x;
		textRotation[1] = y;
		textRotation[2] = z;
	}

	public void draw( GL10 gl )
	{		
		gl.glPushMatrix();
		gl.glTranslatef(textPosition[0], textPosition[1], textPosition[2]);
		if (textRotation[2] != 0) gl.glRotatef(textRotation[2], 0, 0, 1);
		if (textRotation[1] != 0) gl.glRotatef(textRotation[1], 0, 1, 0);
		if (textRotation[0] != 0) gl.glRotatef(textRotation[0], 1, 0, 0);

		gl.glEnable( GL10.GL_BLEND );
		gl.glDisable(GL10.GL_DEPTH_TEST);
		v.drawNodes(gl, GL10.GL_TRIANGLES);
		gl.glDisable( GL10.GL_BLEND );
		gl.glEnable(GL10.GL_DEPTH_TEST);
		
		gl.glPopMatrix();
	}
	
	public void setText(String txt)
	{
		text = txt;
		v.clearNodes();
	}
	
	public String getText()
	{
		return text;
	}

	public void setScale(float scale) 
	{
		fontInfo.scaleX = fontInfo.scaleY = scale;                        // Set Uniform Scale
	}
	public void setScale(float sx, float sy)  
	{
		fontInfo.scaleX = sx;                                    // Set X Scale
		fontInfo.scaleY = sy;                                    // Set Y Scale
	}

	public float getScaleX()  
	{
		return fontInfo.scaleX;                                  // Return X Scale
	}
	public float getScaleY()  
	{
		return fontInfo.scaleY;                                  // Return Y Scale
	}

	public void setSpace(float space)  
	{
		fontInfo.spaceX = space;                                 // Set Space
	}

	public float getSpace()  
	{
		return fontInfo.spaceX;                                  // Return X Space
	}

	public float getLength(String text)
	{
		float len = 0.0f;                               // Working Length
		int strLen = text.length();                     // Get String Length (Characters)
		for ( int i = 0; i < strLen; i++ )  {           // For Each Character in String (Except Last
			int c = (int)text.charAt( i ) - FontInfo.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
			len += ( fontInfo.charWidths[c] * fontInfo.scaleX );           // Add Scaled Character Width to Total Length
		}
		len += ( strLen > 1 ? ( ( strLen - 1 ) * fontInfo.spaceX ) * fontInfo.scaleX : 0 );  // Add Space Length
		return len;                                     // Return Total Length
	}

	public float getCharWidth(char chr)  
	{
		int c = chr - FontInfo.CHAR_START;                       // Calculate Character Index (Offset by First Char in Font)
		return ( fontInfo.charWidths[c] * fontInfo.scaleX );              // Return Scaled Character Width
	}
	public float getCharWidthMax()  
	{
		return ( fontInfo.charWidthMax * fontInfo.scaleX );               // Return Scaled Max Character Width
	}
	public float getCharHeight() 
	{
		return ( fontInfo.charHeight * fontInfo.scaleY );                 // Return Scaled Character Height
	}

	public float getAscent()  
	{
		return ( fontInfo.fontAscent * fontInfo.scaleY );                 // Return Font Ascent
	}
	public float getDescent()  
	{
		return ( fontInfo.fontDescent * fontInfo.scaleY );                // Return Font Descent
	}
	public float getHeight()  
	{
		return ( fontInfo.fontHeight * fontInfo.scaleY );                 // Return Font Height (Actual)
	}
	public float getWidth()
	{
		return getLength(text);
	}

}
/**
 * Copyright 2012 by fractious (http://fractiousg.blogspot.com/2012/04/rendering-text-in-opengl-on-android.html)
 * contributed to the Public Domain (using CC0 1.0 public domain license).
 *
 * See http://creativecommons.org/publicdomain/zero/1.0/legalcode for the full license text.
 *
 * Also contains minor modifications made by Forrest Guice, Copyright 2012.
 */

package com.forrestguice.glstuff.gltext;

import javax.microedition.khronos.opengles.GL10;

import com.forrestguice.glstuff.TextureRegion;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLUtils;

public class FontInfo 
{
	public final static int CHAR_START = 32;           // First Character (ASCII Code)
	public final static int CHAR_END = 126;            // Last Character (ASCII Code)
	public final static int CHAR_CNT = ( ( ( CHAR_END - CHAR_START ) + 1 ) + 1 );  // Character Count (Including Character to use for Unknown)

	public final static int CHAR_NONE = 32;            // Character to Use for Unknown (ASCII Code)
	public final static int CHAR_UNKNOWN = ( CHAR_CNT - 1 );  // Index of the Unknown Character

	public final static int FONT_SIZE_MIN = 6;         // Minumum Font Size (Pixels)
	public final static int FONT_SIZE_MAX = 180;       // Maximum Font Size (Pixels)

	public int fontPadX, fontPadY;       // Font Padding (Pixels; On Each Side, ie. Doubled on Both X+Y Axis)
	public float fontHeight;             // Font Height (Actual; Pixels)
	public float fontAscent;             // Font Ascent (Above Baseline; Pixels)
	public float fontDescent;            // Font Descent (Below Baseline; Pixels)

	public float charWidthMax;           // Character Width (Maximum; Pixels)
	public float charHeight;             // Character Height (Maximum; Pixels)
	public final float[] charWidths;     // Width of Each Character (Actual; Pixels)
	public TextureRegion[] charRgn;      // Region of Each Character (Texture Coordinates)
	public int cellWidth, cellHeight;    // Character Cell Width/Height
	public int rowCnt, colCnt;           // Number of Rows/Columns

	public float scaleX, scaleY;         // Font Scale (X,Y Axis)
	public float spaceX;                 // Additional (X,Y Axis) Spacing (Unscaled)
	
	public int textureId;
	public int textureSize;              // Texture Size for Font (Square) [NOTE: Public for Testing Purposes Only!]
	public TextureRegion textureRgn;     // Full Texture Region

	public FontInfo()
	{
		textureSize = 0;
		
		charWidths = new float[CHAR_CNT];      // Create the Array of Character Widths
		charRgn = new TextureRegion[CHAR_CNT]; // Create the Array of Character Regions
		
		fontPadX = 0;
		fontPadY = 0;

		fontHeight = 0.0f;
		fontAscent = 0.0f;
		fontDescent = 0.0f;

		charWidthMax = 0;
		charHeight = 0;

		cellWidth = 0;
		cellHeight = 0;
		rowCnt = 0;
		colCnt = 0;

		scaleX = 1.0f;   // Default Scale = 1 (Unscaled)
		scaleY = 1.0f;   // Default Scale = 1 (Unscaled)

		spaceX = 0.0f;
	}
	
	public int load(GL10 gl, String family, int style, int size, int padX, int padY)
	{
		Typeface tf = Typeface.create(family, style);
		return load(gl, tf, size, padX, padY);
	}
	public int load(AssetManager assets, GL10 gl, String file, int size, int padX, int padY)
	{
		Typeface tf = Typeface.createFromAsset( assets, file );  // Create the Typeface from Font File
		return load(gl, tf, size, padX, padY);
	}
	
	public int load(GL10 gl, Typeface tf, int size, int padX, int padY) 
	{
		// setup requested values
		fontPadX = padX;                                // Set Requested X Axis Padding
		fontPadY = padY;                                // Set Requested Y Axis Padding

		// load the font and setup paint instance for drawing
		Paint paint = new Paint();                      // Create Android Paint Instance
		paint.setAntiAlias( true );                     // Enable Anti Alias
		paint.setTextSize( size );                      // Set Text Size
		paint.setColor( 0xffffffff );                   // Set ARGB (White, Opaque)
		paint.setTypeface( tf );                        // Set Typeface

		// get font metrics
		Paint.FontMetrics fm = paint.getFontMetrics();  // Get Font Metrics
		fontHeight = (float)Math.ceil( Math.abs( fm.bottom ) + Math.abs( fm.top ) );  // Calculate Font Height
		fontAscent = (float)Math.ceil( Math.abs( fm.ascent ) );  // Save Font Ascent
		fontDescent = (float)Math.ceil( Math.abs( fm.descent ) );  // Save Font Descent

		// determine the width of each character (including unknown character)
		// also determine the maximum character width
		char[] s = new char[2];                         // Create Character Array
		charWidthMax = charHeight = 0;                  // Reset Character Width/Height Maximums
		float[] w = new float[2];                       // Working Width Value
		int cnt = 0;                                    // Array Counter
		for ( char c = CHAR_START; c <= CHAR_END; c++ )  {  // FOR Each Character
			s[0] = c;                                    // Set Character
			paint.getTextWidths( s, 0, 1, w );           // Get Character Bounds
			charWidths[cnt] = w[0];                      // Get Width
			if ( charWidths[cnt] > charWidthMax )        // IF Width Larger Than Max Width
				charWidthMax = charWidths[cnt];           // Save New Max Width
			cnt++;                                       // Advance Array Counter
		}
		s[0] = CHAR_NONE;                               // Set Unknown Character
		paint.getTextWidths( s, 0, 1, w );              // Get Character Bounds
		charWidths[cnt] = w[0];                         // Get Width
		if ( charWidths[cnt] > charWidthMax )           // IF Width Larger Than Max Width
			charWidthMax = charWidths[cnt];              // Save New Max Width
		cnt++;                                          // Advance Array Counter

		// set character height to font height
		charHeight = fontHeight;                        // Set Character Height

		// find the maximum size, validate, and setup cell sizes
		cellWidth = (int)charWidthMax + ( 2 * fontPadX );  // Set Cell Width
		cellHeight = (int)charHeight + ( 2 * fontPadY );  // Set Cell Height
		int maxSize = cellWidth > cellHeight ? cellWidth : cellHeight;  // Save Max Size (Width/Height)
		if ( maxSize < FONT_SIZE_MIN || maxSize > FONT_SIZE_MAX )  // IF Maximum Size Outside Valid Bounds
			return -1;                                // Return Error

		// set texture size based on max font size (width or height)
		// NOTE: these values are fixed, based on the defined characters. when
		// changing start/end characters (CHAR_START/CHAR_END) this will need adjustment too!
		if ( maxSize <= 24 )                            // IF Max Size is 18 or Less
			textureSize = 256;                           // Set 256 Texture Size
		else if ( maxSize <= 40 )                       // ELSE IF Max Size is 40 or Less
			textureSize = 512;                           // Set 512 Texture Size
		else if ( maxSize <= 80 )                       // ELSE IF Max Size is 80 or Less
			textureSize = 1024;                          // Set 1024 Texture Size
		else                                            // ELSE IF Max Size is Larger Than 80 (and Less than FONT_SIZE_MAX)
			textureSize = 2048;                          // Set 2048 Texture Size

		// create an empty bitmap (alpha only)
		Bitmap bitmap = Bitmap.createBitmap( textureSize, textureSize, Bitmap.Config.ALPHA_8 );  // Create Bitmap
		Canvas canvas = new Canvas( bitmap );           // Create Canvas for Rendering to Bitmap
		bitmap.eraseColor( 0x00000000 );                // Set Transparent Background (ARGB)

		// calculate rows/columns
		// NOTE: while not required for anything, these may be useful to have :)
		colCnt = textureSize / cellWidth;               // Calculate Number of Columns
		rowCnt = (int)Math.ceil( (float)CHAR_CNT / (float)colCnt );  // Calculate Number of Rows

		// render each of the characters to the canvas (ie. build the font map)
		float x = fontPadX;                             // Set Start Position (X)
		float y = ( cellHeight - 1 ) - fontDescent - fontPadY;  // Set Start Position (Y)
		for ( char c = CHAR_START; c <= CHAR_END; c++ )  {  // FOR Each Character
			s[0] = c;                                    // Set Character to Draw
			canvas.drawText( s, 0, 1, x, y, paint );     // Draw Character
			x += cellWidth;                              // Move to Next Character
			if ( ( x + cellWidth - fontPadX ) > textureSize )  {  // IF End of Line Reached
				x = fontPadX;                             // Set X for New Row
				y += cellHeight;                          // Move Down a Row
			}
		}
		s[0] = CHAR_NONE;                               // Set Character to Use for NONE
		canvas.drawText( s, 0, 1, x, y, paint );        // Draw Character

		// generate a new texture
		int[] textureIds = new int[1];                  // Array to Get Texture Id
		gl.glGenTextures( 1, textureIds, 0 );           // Generate New Texture

		// setup filters for texture
		gl.glBindTexture( GL10.GL_TEXTURE_2D, textureIds[0] );  // Bind Texture
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST );  // Set Minification Filter
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR );  // Set Magnification Filter
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE );  // Set U Wrapping
		gl.glTexParameterf( GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE );  // Set V Wrapping

		// load the generated bitmap onto the texture
		GLUtils.texImage2D( GL10.GL_TEXTURE_2D, 0, bitmap, 0 );  // Load Bitmap to Texture
		gl.glBindTexture( GL10.GL_TEXTURE_2D, 0 );      // Unbind Texture

		// release the bitmap
		bitmap.recycle();                               // Release the Bitmap

		// setup the array of character texture regions
		x = 0;                                          // Initialize X
		y = 0;                                          // Initialize Y
		for ( int c = 0; c < CHAR_CNT; c++ )  
		{         // FOR Each Character (On Texture)
			charRgn[c] = new TextureRegion( textureSize, textureSize, x, y, cellWidth-1, cellHeight-1 );  // Create Region for Character
			x += cellWidth;                              // Move to Next Char (Cell)
			if ( x + cellWidth > textureSize )  
			{
				x = 0;                                    // Reset X Position to Start
				y += cellHeight;                          // Move to Next Row (Cell)
			}
		}

		// create full texture region
		textureRgn = new TextureRegion( textureSize, textureSize, 0, 0, textureSize, textureSize );  // Create Full Texture Region

		// return success
		textureId = textureIds[0];
		return textureIds[0];      // return font texture id
	}

}
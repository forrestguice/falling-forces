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

package com.forrestguice.fallforces.model;

import java.math.BigDecimal;

public class UnitsUtility 
{
	public static final String UNITS_M = "m";
	public static final String UNITS_FT = "ft";
	public static final String UNITS_KN = "kN";
	public static final String UNITS_LB = "lb";
	public static final String UNITS_KG = "kg";
	
	public static final double FT_IN_M = 3.28084;
	public static final double M_IN_FT = 0.3048;
	public static final double KG_IN_LB = 0.45359237;
	public static final double LB_IN_KG = 1.0 / KG_IN_LB;
	
	public static final double LB_IN_KN = 224.8089431;
	public static final double KN_IN_LB = 1.0 / LB_IN_KN;
		
	public static final double convertUnits(double value, String oldUnits, String newUnits)
	{
		BigDecimal v = BigDecimal.valueOf(value);   // default; unsupported returns unchanged
		BigDecimal retValue = v;
		if (oldUnits.equals(UNITS_M) && newUnits.equals(UNITS_FT))
		{
			// meters to ft
			retValue = v.multiply(BigDecimal.valueOf(FT_IN_M));
			
		} else if (oldUnits.equals(UNITS_FT) && newUnits.equals(UNITS_M)) {
			// ft to meters
			retValue = v.multiply(BigDecimal.valueOf(M_IN_FT));
			
		} else if (oldUnits.equals(UNITS_LB) && newUnits.equals(UNITS_KG)) {
			// lb to kg
			retValue = v.multiply(BigDecimal.valueOf(KG_IN_LB));
			
		} else if (oldUnits.equals(UNITS_KG) && newUnits.equals(UNITS_LB)) {
			// kg to lb
			retValue = v.multiply(BigDecimal.valueOf(LB_IN_KG));
		}
		return retValue.doubleValue(); 
	}

}

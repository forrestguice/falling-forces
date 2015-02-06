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
import java.math.RoundingMode;

public class RopeModulus
{
										// k = U(U-1.568) / 2.791
   private BigDecimal k;                // calculated rope modulus
   private BigDecimal U;                // UIAA impact force in kN

   public RopeModulus()
   {
   }
   
   public RopeModulus(double impactForce)
   {
	   U = BigDecimal.valueOf(impactForce);
	   findKFromU();
   }
   
   public RopeModulus(float impactForce)
   {
      U = BigDecimal.valueOf(impactForce);
      findKFromU();
   }
   
   private void findKFromU()
   {

	  k = (U.multiply(U.subtract(BigDecimal.valueOf(1.568F)))).divide(BigDecimal.valueOf(2.791F), 25, RoundingMode.HALF_UP);
   }
   
   private void findUFromK()
   {
	   // ax^2 + bx + c = 0
	   // x = (-b +- sqrt(b^2 - 4ac)) / 2a
	   //
	   // k = U(U-1.568) / 2.791
	   // k * 2.791 = U(U-1.568)
	   // k * 2.791 = U^2 - 1.568U
	   // U^2 - 1.568U - 2.791k = 0
	   //
	   // a = 1, b = -1.568, c = -2.791k
	   // U = (-b +- sqrt(b^2 - 4ac)) / 2a
	   // U = (1.568 +- sqrt((-1.568)^2 - 4*(-2.791k))) / 2
	   // U = (1.568 +- sqrt(2.458624 + 11.164k)) / 2
	   //
	   // U = (1.568 + sqrt(2.458624 + 11.164k)) / 2
	   // U = (1.568 - sqrt(2.458624 + 11.164k)) / 2
	   //
	   // t0 = 1.568, t1 = sqrt(2.458624 + 11.164k)), t2 = 2
	   // U = (t0 + t1) / t2

	   BigDecimal t0 = BigDecimal.valueOf(1.568);
	   BigDecimal t1 = BigDecimal.valueOf(Math.sqrt(BigDecimal.valueOf(2.458624).add(BigDecimal.valueOf(11.164).multiply(k)).doubleValue()));
	   BigDecimal t2 = BigDecimal.valueOf(2);   
	   U = t0.add(t1).divide(t2, RoundingMode.HALF_UP);
   }
   
   public void setRopeModulus(double modulus)
   {
	   k = BigDecimal.valueOf(modulus);
	   findUFromK();
   }
   
   public BigDecimal getRopeModulus()
   {
      return k;
   }
   
   public void setImpactRating(double impactForce)
   {
	   U = BigDecimal.valueOf(impactForce);
	   findKFromU();
   }
   
   public BigDecimal getImpactRating()
   {
	   return U;
   }
}

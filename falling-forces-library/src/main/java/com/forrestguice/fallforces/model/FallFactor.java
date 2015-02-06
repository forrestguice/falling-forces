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

public class FallFactor
{
   private BigDecimal H;                // length of fall
   private BigDecimal L;                // length of rope in system
   private BigDecimal r;                // fall factor

   public FallFactor(double f)
   {
	  L = BigDecimal.valueOf(-1);
	  H = BigDecimal.valueOf(-1);
      setFallFactor(f);
   }

   public FallFactor(double _length, float _falling )
   {
      L = BigDecimal.valueOf(_length);
      H = BigDecimal.valueOf(_falling);
      r = H.divide(L, 25, RoundingMode.HALF_UP);
   }

   public void setFallFactor(double f)
   {
	  double v = (f > 2) ? 2 : (f < 0) ? 0 : f;
      r = BigDecimal.valueOf(v);
   }

   public BigDecimal getFallFactor()
   {
      return r;
   }

   public BigDecimal getRopeLength()
   {
      return L;
   }

   public BigDecimal getFallLength()
   {
      return H;
   }
   
   public String toString()
   {
	   return "[Fall Factor: " + r.doubleValue() + "]";
   }
}

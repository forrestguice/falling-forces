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

public class Weight
{
   public static final int UNITS_LBS = 0;
   public static final int UNITS_KN = 1;

   public static final float EARTH_GRAVITY = 9.8F;

   private BigDecimal gravity;             // gravity (m/s^2)
   private BigDecimal mass;                // mass (grams)
   private BigDecimal weight;              // weight (kilonewtons)

   /**
      Initialize with an initial mass (in kg)
      @param _mass the mass in kg
   */   
   public Weight(double m)
   {
      gravity = BigDecimal.valueOf(EARTH_GRAVITY);
      mass = BigDecimal.valueOf(m).divide(BigDecimal.valueOf(1000f), 25, RoundingMode.HALF_UP);  
      recalculate();
   }

   /**
      @param _mass the mass in kg
      @param _g acceleration from gravity (m/s^2
   */
   public Weight(double m, double g)
   {
      gravity = BigDecimal.valueOf(g);
      mass = BigDecimal.valueOf(m).divide(BigDecimal.valueOf(1000f), 25, RoundingMode.HALF_UP);
      recalculate();
   }

   /**
      @param weight the weight in some units
      @param units the units the weight represents
   */
   public Weight(double w, int units)
   {
      gravity = BigDecimal.valueOf(EARTH_GRAVITY);
      setWeight(w, units);
   }

   public Weight( double w, double g, int units )
   {
      gravity = BigDecimal.valueOf(g);
      setWeight(w, units);
   }

   private void recalculate()
   {
      weight = mass.multiply(gravity);
   }

   /**
      @return the weight in KN
   */
   public BigDecimal getWeight()
   {
      return weight;
   }
   /**
      @return the weight in units
   */
   public BigDecimal getWeight(int units)
   {
      if (units == UNITS_LBS)
      {
         return (weight.multiply(BigDecimal.valueOf(1000F))).divide(BigDecimal.valueOf(4.448222F), 25, RoundingMode.HALF_UP);
      } else {
         return weight;
      }
   }

   public void setWeight(double w, int units)
   {
      if (units == UNITS_LBS)
      {
         // weight in lbs - convert to newtons, then kn
         //weight = (w * 4.44822216F) / 1000F;
         weight = (BigDecimal.valueOf(w).multiply(BigDecimal.valueOf(4.44822216F))).divide(BigDecimal.valueOf(1000F), 25, RoundingMode.HALF_UP);
         mass = weight.divide(gravity, 25, RoundingMode.HALF_UP);
         
      } else {
         // assume weight in kn
         weight = BigDecimal.valueOf(w);
         mass = weight.divide(gravity, 25, RoundingMode.HALF_UP);
      }
   }
   public void setWeight(double w, double g, int units)
   {
      gravity = BigDecimal.valueOf(g);
      setWeight(w, units);
   }

   /**
    * Returns the mass component in grams.
    * @return the mass in grams
    */
   public BigDecimal getMass()
   {
      return mass;
   }
   /**
      Set the mass component in grams.
      @param m mass in g
   */
   public void setMass( float m )
   {
      mass = BigDecimal.valueOf(m);
      recalculate();
   }

   public BigDecimal getGravity()
   {
      return gravity;
   }
   /**
      @param g acc due to gravity (m/s^2)
   */
   public void setGravity( float g )
   {
      gravity = BigDecimal.valueOf(g);
      recalculate();
   }
}

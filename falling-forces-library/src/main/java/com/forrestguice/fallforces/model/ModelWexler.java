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
import java.math.MathContext;
import java.math.RoundingMode;

public class ModelWexler implements FallModel
{
   /**public static void main( String[] args )
   {
      if (args.length < 3 || args.length > 3)
      {
         System.out.println("cmd <weight> <fall_factor> <impact_rating>");
         return;
      }

      float arg_weight = Float.parseFloat(args[0]);
      float arg_fallfactor = Float.parseFloat(args[1]);
      float arg_impact = Float.parseFloat(args[2]);

      Weight weight = new Weight(arg_weight, Weight.UNITS_LBS);
      FallFactor fallFactor = new FallFactor(arg_fallfactor);
      RopeModulus modulus = new RopeModulus(arg_impact);
      ModelWexler forces = new ModelWexler(weight, modulus, fallFactor);
      System.out.println(forces.toString());
   }*/

   private boolean ready = false;
   
   private FallFactor fallFactor;           // fall factor
   private RopeModulus ropeModulus;         // rope modulus
   private Weight weight;                   // weight

   private BigDecimal climberT;
   private BigDecimal anchorT;
   private BigDecimal belayerT;

   public ModelWexler()
   {
      ready = false;
   }

   public ModelWexler( Weight _w, RopeModulus _k, FallFactor _f )
   {
      weight = _w;
      fallFactor = _f;
      ropeModulus = _k;
      checkReady();
   }

   private void checkReady()
   {
      ready = (ropeModulus != null && fallFactor != null && weight != null);
      calculateForces();
   }

   public void calculateForces()
   {
      if (ready)
      {
         BigDecimal w = weight.getWeight().setScale(2, BigDecimal.ROUND_UP);
         BigDecimal r = fallFactor.getFallFactor();
         BigDecimal k = ropeModulus.getRopeModulus();

         //climberT = w + Math.sqrt(w.multiply(w) + 2*k*w*r);
         climberT = w.add( sqrt( w.multiply(w).add(BigDecimal.valueOf(2).multiply(k).multiply(w).multiply(r)) )  );
         anchorT = (climberT.multiply(BigDecimal.valueOf(5))).divide(BigDecimal.valueOf(3), 25, RoundingMode.HALF_UP);
         belayerT = anchorT.subtract(climberT);
      }
   }
   
   public static float computeForce(int mode, double _w, double _k, double _f)
   {
	   float tVal = (float)(_w + Math.sqrt(_w*_w + 2 * _k * _w * _f));
	   switch (mode)
	   {
	   case 2:
		   return (5f / 3f) * tVal;
		   
	   case 1:
		   return ((5f / 3f) * tVal) - tVal;
		   
	   case 0:
	   default:
		   return tVal;
	   }
   }

   public String toString()
   {
      String retString = "";
      retString += "Weight = " + weight.getWeight() + " kg, or " + weight.getWeight(Weight.UNITS_LBS) + " lbs";
      retString += "\nFallFactor = " + fallFactor.getFallFactor();
      retString += "\nRopeModulus = " + ropeModulus.getRopeModulus();
      retString += "\n-------------------------------------------------";
      retString += "\nForce on Climber: " + climberT;
      return retString;
   }

   public void setParams( Weight _w, RopeModulus _k, FallFactor _f )
   {
      weight = _w;
      fallFactor = _f;
      ropeModulus = _k;
      checkReady();
   }

   public void setWeight(Weight w)
   {
      weight = w;
      checkReady();
   }
   public Weight getWeight()
   {
      return weight;
   }

   public void setFallFactor(FallFactor f)
   {
      fallFactor = f;
      checkReady();
   }
   public FallFactor getFallFactor()
   {
      return fallFactor;
   }

   public void setRopeModulus(RopeModulus r)
   {
      ropeModulus = r;
      checkReady();
   }
   public RopeModulus getRopeModulus()
   {
      return ropeModulus;
   }

   public boolean isReady()
   {
      return ready;
   }

   public BigDecimal getForceOnClimber()
   {
	   return climberT;
   }

   public BigDecimal getForceOnAnchor()
   {
	   return anchorT;
   }
   
   public BigDecimal getForceOnBelayer()
   {
	   return belayerT;
   }
   
   public static BigDecimal sqrt(BigDecimal x) 
   {
      return BigDecimal.valueOf(StrictMath.sqrt(x.doubleValue()));
   }
}

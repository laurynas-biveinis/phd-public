/*
     Copyright (C) 2012 Laurynas Biveinis

     This file is part of RR-Tree.

     RR-Tree is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.

     RR-Tree is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.

     You should have received a copy of the GNU General Public License
     along with RR-Tree.  If not, see <http://www.gnu.org/licenses/>.
*/
package aau.bufferedIndexes;

/**
 * Methods for accessing sign, exponent, mantissa of an IEEE 754 double precision floating-point numbers
 */
final class IeeeDouble {

    final private static long IEEE754_NEGATIVE_BITMASK  = 0x8000000000000000L;
    final private static long IEEE754_EXPONENT_BITMASK  = 0x7FF0000000000000L;
    final private static long IEEE754_MANTISSA0_BITMASK = 0x000FFFFF00000000L;
    final private static long IEEE754_MANTISSA1_BITMASK = 0x00000000FFFFFFFFL;

    public static int getNegativeBit(final double d) {
        final long doubleBits = Double.doubleToRawLongBits(d);
        return ((doubleBits & IEEE754_NEGATIVE_BITMASK) != 0) ? 1 : 0;
    }

    public static int getExponent(final double d) {
        final long doubleBits = Double.doubleToRawLongBits(d);
        //noinspection NumericCastThatLosesPrecision
        return (int)((doubleBits & IEEE754_EXPONENT_BITMASK) >> 52);
    }

    public static int getMantissa0(final double d) {
        final long doubleBits = Double.doubleToRawLongBits(d);
        //noinspection NumericCastThatLosesPrecision
        return (int)((doubleBits & IEEE754_MANTISSA0_BITMASK) >> 32);
    }

    public static int getMantissa1(final double d) {
        final long doubleBits = Double.doubleToRawLongBits(d);
        //noinspection NumericCastThatLosesPrecision
        return (int)(doubleBits & IEEE754_MANTISSA1_BITMASK);
    }
}

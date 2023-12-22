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

import xxl.core.spatial.points.Point;

import java.util.Comparator;

/**
 * A Comparator for 2D points that uses Hilbert curve
 */
class HilbertPointComparator implements Comparator<Point> {

    private HilbertPointComparator() { }

    public static final Comparator<Point> INSTANCE = new HilbertPointComparator();

    static private class RotationBitsIndex {
        int rotation;
        long bits;
        long index;
    }

    public int compare(Point o1, Point o2) {
        int result;
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter("hilbert-trace.log", true));
//            writer.write(o1.getValue(0) + " " + Long.toHexString(Double.doubleToRawLongBits(o1.getValue(0))) + " "
//                    + o1.getValue(1) + " " + Long.toHexString(Double.doubleToRawLongBits(o1.getValue(1))) + " "
//                    + o2.getValue(0) + " " + Long.toHexString(Double.doubleToRawLongBits(o2.getValue(0))) + " "
//                    + o2.getValue(1) + " " + Long.toHexString(Double.doubleToRawLongBits(o2.getValue(1))) + " ");

        final int max = (getIEEESignBits(o1) != getIEEESignBits(o2))
                ? 2047 : Math.max(getIEEEexptMax(o1), getIEEEexptMax(o2));
        final RotationBitsIndex rotationBitsIndex = getIeeeInitValues(o1, max + 53);
        result = hilbert_cmp_work(64, max, max + 53, o1, o2, rotationBitsIndex);
//            writer.write(Integer.toString(result));
//            writer.newLine();
//            writer.close();
//        }
//        catch (IOException e) {
//            throw new IllegalStateException("I/O exception while logging Hilbert comparisson!");
//        }
        return result;
    }

    private static final int IEEEexpBits = 11;
    private static final int IEEEsigBits = 52;
    private static final int IEEErepBits = (1 << IEEEexpBits) + IEEEsigBits;

    private static int getIEEESignBits(final Point p)
    {
        return IeeeDouble.getNegativeBit(p.getValue(0)) | (IeeeDouble.getNegativeBit(p.getValue(1)) << 1);
    }

    private static int getIEEEexptMax(final Point p)
    {
        int max = Math.max(IeeeDouble.getExponent(p.getValue(0)), IeeeDouble.getExponent(p.getValue(1)));
        if (max != 0)
            --max;
        return max;
    }

    private static RotationBitsIndex getIeeeInitValues(final Point p, final int y)
    {
        final int signBits = getIEEESignBits(p);

        /* compute the odd/evenness of the number of sign bits */
        final int signPar = signBits ^ signBits >> 1;
        final int signParity = signPar & 1;

        /* find the position of the least-order 0 bit in among signBits and adjust it if necessary */
        final int leastZeroBit = ((signBits & 1) == 0) ? 0 : 1;
        final int strayBit = (leastZeroBit == 0) ? 1 : 0;

        final RotationBitsIndex result = new RotationBitsIndex();

        if ((y & 1) == 1)
        {
            result.rotation = (IEEErepBits - y + 1 + leastZeroBit) % 2;
            result.index = signParity;
            result.bits = (y < IEEErepBits - 1) ? signBits ^ (1L << ((result.rotation + strayBit) % 2)) : signBits ^ 2L;
        }
        else {
            if (y < IEEErepBits)
            {
                final int shift_amt = (IEEErepBits - y + leastZeroBit) % 2;
                result.rotation = (shift_amt + 2 + strayBit) % 2;
                result.bits = signBits ^ (1L << shift_amt);
                result.index = signParity ^ 1;
            }
            else /* y == IEEErepBits */
            {
                result.rotation = 0;
                result.bits = 1L << 1;
                result.index = 1;
            }
        }
        return result;
    }

    private static int hilbert_cmp_work(final int nBits, final int max, int y, final Point p1, final Point p2,
                                        final RotationBitsIndex rotationBitsIndex)
    {
        while (y-- > max)
        {
            long reflection = getIeeeBits(p1, y);
            long diff = reflection ^ getIeeeBits(p2, y);
            rotationBitsIndex.bits ^= reflection;
            rotationBitsIndex.bits = rotateRight(rotationBitsIndex.bits, rotationBitsIndex.rotation);
            if (diff != 0)
            {
                diff = rotateRight(diff, rotationBitsIndex.rotation);
                rotationBitsIndex.index ^= rotationBitsIndex.index >> 1;
                rotationBitsIndex.bits ^= rotationBitsIndex.bits >> 1;
                diff ^= diff >> 1;
                return (((rotationBitsIndex.index ^ y ^ nBits) & 1)
                        == ((rotationBitsIndex.bits < (rotationBitsIndex.bits ^ diff)) ? 1 : 0)) ? -1: 1;
            }
            rotationBitsIndex.index ^= rotationBitsIndex.bits;
            reflection ^= 1L << rotationBitsIndex.rotation;
            rotationBitsIndex.rotation = adjust_rotation(rotationBitsIndex.rotation, rotationBitsIndex.bits);
            rotationBitsIndex.bits = reflection;
        }
        return 0;
    }

    /* retrieve bits y of elements of double array c, where an expanded IEEE double has 2100 bits. */
    private static long getIeeeBits(final Point p, final int y)
    {
        return getIEEEBit(p.getValue(0), y) | (getIEEEBit(p.getValue(1), y) << 1);
    }

    private static long getIEEEBit(final double d, final int y) {
        long bit = IeeeDouble.getNegativeBit(d);
        final int exponent = IeeeDouble.getExponent(d);
        final int normalized = exponent != 0 ? 1 : 0;
        final int diff = y - (exponent - normalized);
        if (diff <= 52)
            bit ^= 1 & ((diff < 32) ? IeeeDouble.getMantissa1(d) >> diff:
                        (diff < 52) ? IeeeDouble.getMantissa0(d) >> (diff - 32):
                        /* else */    normalized);
        else
            bit ^= (y == IEEErepBits - 1) ? 1 : 0;
        return bit;
    }

    private static long rotateRight(long arg, int nRots) {
        return (((arg) >> (nRots)) | ((arg) << (2-(nRots)))) & 3L;
    }

    private static int adjust_rotation(int rotation, long bits) {
        /* rotation = (rotation + 1 + ffs(bits)) % nDims; */
        bits &= -bits & 1L;
        while (bits != 0) {
            bits >>= 1;
            ++rotation;
        }
        if ( ++rotation >= 2)
            rotation -= 2;
        return rotation;
    }
}

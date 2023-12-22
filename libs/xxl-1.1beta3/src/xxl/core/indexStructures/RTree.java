/* XXL: The eXtensible and fleXible Library for data processing

Copyright (C) 2000-2006 Prof. Dr. Bernhard Seeger
                        Head of the Database Research Group
                        Department of Mathematics and Computer Science
                        University of Marburg
                        Germany

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307,
USA

	http://www.xxl-library.de

bugs, requests for enhancements: request@xxl-library.de

If you want to be informed on new versions of XXL you can
subscribe to our mailing-list. Send an email to

	xxl-request@lists.uni-marburg.de

without subject and the word "subscribe" in the message body.
*/
package xxl.core.indexStructures;

import xxl.core.cursors.Cursors;
import xxl.core.cursors.mappers.Mapper;
import xxl.core.cursors.sources.Enumerator;
import xxl.core.functions.Function;
import xxl.core.io.converters.ConvertableConverter;
import xxl.core.io.converters.Converter;
import xxl.core.spatial.rectangles.DoublePointRectangle;
import xxl.core.spatial.rectangles.Rectangle;

import java.util.*;

/** An <tt>ORTree</tt> for objects with bounding rectangles as regions. 
 * This implementation of a member of the R-Tree family uses the 
 * split-strategy of the R*-Tree.  
 * 
 * For a detailed discussion see 
 * Norbert Beckmann, Hans-Peter Kriegel, Ralf Schneider, Bernhard Seeger:
 * "The R*-tree: An Efficient and Robust Access Method for Points and Rectangles",
 * ACM-SIGMOD (1990)322-331.
 * 
 * @see Tree
 * @see ORTree
 * @see LinearRTree
 * @see QuadraticRTree
 * @see GreenesRTree 
 */
public class RTree extends ORTree {

	/** Returns the bounding rectangle of <tt>entry</tt>.
	 * 
	 * @param entry an entry
	 * @return the bounding rectangle of <tt>entry</tt>
	 */
	public Rectangle rectangle (Object entry) {
		return (Rectangle)descriptor(entry);
	}

	/* (non-Javadoc)
	 * @see xxl.core.indexStructures.Tree#createNode(int)
	 */
	public Tree.Node createNode (int level) {
		return new Node().initialize(level, new ArrayList());
	}

    private static final Function<List<Distribution>, Double> MARGIN_ACCUMULATOR
            = new Function() {
                public Object invoke (final Object dists) {
                    final List<Distribution> distributions = (List<Distribution>)dists;
                    double marginValue = 0.0;
                    for (final Distribution distribution : distributions)
                        marginValue += distribution.marginValue();
                    return marginValue;
                }
            };

    private static final Function<Distribution, Double> OVERLAP_GETTER
            = new Function<Distribution, Double> () {
                public Double invoke (final Distribution distribution) {
                    return distribution.overlapValue();
                }
            };

    private static final Function<Distribution, Double> AREA_GETTER
            = new Function<Distribution, Double> () {
                public Double invoke (final Distribution distribution) {
                    return distribution.areaValue();
                }
            };

    public static int compareRectangles(final Rectangle r1, final Rectangle r2, final boolean rightCorner,
                                        final int dimension) {
        final double v1 = r1.getCorner(rightCorner).getValue(dimension);
        final double v2 = r2.getCorner(rightCorner).getValue(dimension);
        if (v1 < v2) return -1;
        if (v1 > v2) return 1;
        // Do not have to handle NaNs
        return 0;
    }

    private static final Comparator<Rectangle> leftXSorter = new Comparator<Rectangle>() {
        public int compare (final Rectangle r1, final Rectangle r2) {
            return compareRectangles(r1, r2, false, 0);
        }
    };

    private static final Comparator<Rectangle> leftYSorter = new Comparator<Rectangle>() {
        public int compare (final Rectangle r1, final Rectangle r2) {
            return compareRectangles(r1, r2, false, 1);
        }
    };

    private static final Comparator<Rectangle> rightXSorter = new Comparator<Rectangle>() {
        public int compare (final Rectangle r1, final Rectangle r2) {
            return compareRectangles(r1, r2, true, 0);
        }
    };

    private static final Comparator<Rectangle> rightYSorter = new Comparator<Rectangle>() {
        public int compare (final Rectangle r1, final Rectangle r2) {
            return compareRectangles(r1, r2, true, 1);
        }
    };

    private static final Comparator[] sorters = new Comparator[] {leftXSorter, leftYSorter, rightXSorter, rightYSorter};

    /**
     * Splits an R-tree node according to a standard R*-tree node split algorithm.
     *
     * @param node Node to split
     * @param entries Collection to put split-off entries to
     * @param minEntries Minimum allowed number of entries in a node
     * @param maxEntries Maximum allowed number of entries in a node
     *
     * @return a Distribution object containing information about chosen split
     */
    public Distribution splitNode(final ORTree.Node node, final Collection<Object> entries, final int minEntries,
                                  final int maxEntries) {
        final int dimensions = rectangle(node.entries.iterator().next()).dimensions();

        final Map<Rectangle, Object> mbrToEntry = new IdentityHashMap<Rectangle, Object>(node.entries.size());
        final Rectangle[] rects = new Rectangle[node.entries.size()];
        int i = 0;
        for (final Object e : node.entries) {
            final Rectangle r = rectangle(e);
            final Object prev = mbrToEntry.put(r, e);
            assert prev == null;
            rects[i] = r;
            i++;
        }

        // For each dimension generate a list of all possible distributions
        final Iterator<List<Distribution>> distributionLists = new Mapper<Integer, List<Distribution>>(
            new Function<Integer, List<Distribution>> () {
                int sorteri = 0;
                public List<Distribution> invoke (final Integer dim) {
                    // list of distributions for this dimension
                    final List<Distribution> distributionList
                            = new ArrayList<Distribution>(2*(maxEntries-minEntries+1));
                    final Rectangle [][] rectangles = new Rectangle[2][];

                    // Consider the entries sorted by left or right borders
                    for (int i=0; i<2; i++) {

                        final Rectangle[] rectArray = Arrays.copyOf(rects, rects.length);

                        // Sort the entries by left or right border in the actual dimension
                        //noinspection unchecked
                        Arrays.sort(rectArray, sorters[sorteri]);
                        sorteri++;

                        // Calculation of descriptors for all distributions (linear!)
                        for (int k = 0; k<2; k++) {
                            int e = k == 0 ? 0 : rectArray.length - 1;
                            final int direction = k == 0 ? +1 : -1;

                            Rectangle rectangle = new DoublePointRectangle(rectArray[e]);
                            e += direction;

                            for (int l = (k==0? minEntries: node.number()-maxEntries); --l>0;) {
                                rectangle.union(rectArray[e]);
                                e += direction;
                            }
                            (rectangles[k] = new Rectangle[maxEntries-minEntries+1])[0] = rectangle;
                            for (int j=1; j<=maxEntries-minEntries; rectangles[k][j++] = rectangle) {
                                rectangle = Descriptors.union(rectangle, rectArray[e]);
                                e += direction;
                            }
                        }
                        // Creation of the distributions for this dimension
                        for (int j = minEntries; j<=maxEntries; j++) {
                            distributionList.add(new Distribution(rectArray, j, rectangles[0][j-minEntries], rectangles[1][maxEntries-j], dim));
                        }
                    }
                    return distributionList;
                }
            }
        ,new Enumerator(dimensions));

        // Return the distributionList of the dimension for which the margin-sum of all
        // of its distributions is minimal (i.e. choose the dimension)
        List<Distribution> distributionList = Cursors.minima(distributionLists, MARGIN_ACCUMULATOR).getFirst();

        // Choose the distributions of the chosen dimension with minimal overlap
        distributionList = Cursors.minima(distributionList.iterator(), OVERLAP_GETTER);

        // If still more than one distribution has to be considered, choose one
        // with minimal area
        if ((distributionList.size() > 1)
                && (distributionList.get(0).overlapValue() == distributionList.get(1).overlapValue()))
            distributionList = Cursors.minima(distributionList.iterator(), AREA_GETTER);

        final Distribution distribution = distributionList.get(0);

        node.entries.clear(); // TODO: size properly
        for (final Rectangle r : distribution.entries(false)) {
            // TODO: generify
            //noinspection unchecked
            node.entries.add(mbrToEntry.get(r));
        }

        for (final Rectangle r : distribution.entries(true)) {
            entries.add(mbrToEntry.get(r)); // TODO: size properly
        }

        return distribution;
    }


	/** <tt>Node</tt> is the class used to represent leaf- and non-leaf nodes of <tt>RTree</tt>.
	 *	Nodes are stored in containers.
	 *	@see Tree.Node
	 *  @see ORTree.Node
	 */
	public class Node extends ORTree.Node {

		/** SplitInfo contains information about a split. The enclosing
		 * Object of this SplitInfo-Object (i.e. Node.this) is the new node
		 * that was created by the split.
		 */		
		public class SplitInfo extends ORTree.Node.SplitInfo {
						
			/** The distribution of rectangles for the split.
			 */
			protected Distribution distribution;

			/** Creates a new <tt>SplitInfo</tt> with a given path.
			 * @param path the path from the root to the splitted node
			 */
			public SplitInfo (Stack path) {
				super(path);
			}

			/** Initializes the SplitInfo by setting the distribution of 
			 * the split.
			 * 
			 * @param distribution the distribution for the split
			 * @return the initialized <tt>SplitInfo</tt>
			 */
			public ORTree.Node.SplitInfo initialize (Distribution distribution) {
				this.distribution = distribution;
				return initialize(distribution.descriptor(true));
			}

			/** Returns the distribution of the <tt>SplitInfo</tt>.
			 * 
			 * @return the distribution of the <tt>SplitInfo</tt>
			 */
			public Distribution distribution(){
				return distribution;
			}
		}

		protected ORTree.IndexEntry chooseSubtreeByDescriptor(Descriptor descriptor) {
            boolean containingFound = false;

            IndexEntry minAreaEntry = null;
            double minArea = Double.MAX_VALUE;
            IndexEntry minAreaEnlargementEntry = null;
            double minAreaEnlargement = Double.MAX_VALUE;

            for (final IndexEntry e : (Collection<IndexEntry>)entries) {
                final Rectangle rect = rectangle(e);
                final double area = rect.area();
                if (rect.contains(descriptor)) {
                    containingFound = true;
                    if (area < minArea) {
                        minArea = area;
                        minAreaEntry = e;
                    }
                }
                else if (!containingFound) {
                    final Rectangle newRect = new DoublePointRectangle(rect);
                    newRect.union(descriptor);
                    final double areaEnlargement = newRect.area() - area;
                    if (areaEnlargement < minAreaEnlargement) {
                        minAreaEnlargement = areaEnlargement;
                        minAreaEnlargementEntry = e;
                    }
                }
            }
            return containingFound ? minAreaEntry : minAreaEnlargementEntry;
		}


		/* (non-Javadoc)
		 * @see xxl.core.indexStructures.Tree.Node#split(java.util.Stack)
		 */
		protected Tree.Node.SplitInfo split (Stack path) {
			final ORTree.Node node = (ORTree.Node) node(path);
			final int minEntries = node.splitMinNumber();
			final int maxEntries = node.splitMaxNumber();

            // TODO: generify
            //noinspection unchecked
            final Distribution distribution = splitNode(node, entries, minEntries, maxEntries);

			// update the descriptor of the old index entry
			((IndexEntry)indexEntry(path)).descriptor = distribution.descriptor(false);
			return new SplitInfo(path).initialize(distribution);
		}
	}


    /** <tt>Distribution</tt> is the class used to represent the distribution of
     * entries of a node of the <tt>RTree</tt> into two partitions used for a split.
     */
    static public class Distribution {

        /** Entries stored in this distribution.
         */
//			protected Object [] entries;
        protected Rectangle [] entries;

        /** Start index of the second part of the distribution.
         */
        protected int secondStart;

        /** Bounding Rectangle of the first part of the distribution.
         */
        protected Rectangle firstDescriptor;

        /** Bounding Rectangle of the first part of the distribution.
         */
        protected Rectangle secondDescriptor;

        /** Number of dimensions.
         */
        protected int dim;

        /**
         * @param entries an array containing all entries to be distributed
         * @param secondStart the start index of the second partition
         * @param firstDescriptor the descriptor for the first partition
         * @param secondDescriptor the descriptor for the second partition
         * @param dim the number of dimensions
         */
//			protected Distribution (Object [] entries, int secondStart, Rectangle firstDescriptor, Rectangle secondDescriptor, int dim) {
        protected Distribution (Rectangle [] entries, int secondStart, Rectangle firstDescriptor, Rectangle secondDescriptor, int dim) {
            this.entries = entries;
            this.secondStart = secondStart;
            this.firstDescriptor = firstDescriptor;
            this.secondDescriptor = secondDescriptor;
            this.dim = dim;
        }

        /** Returns one of the partitions of this distribution.
         *
         * @param second a <tt>boolean</tt> value determining if the second partition
         * should be returned
         * @return the entries of the first (if <tt>second == false</tt>) or of the
         * second partition (if <tt>second == true</tt>)
         */
        protected List<Rectangle> entries (boolean second) {
            return Arrays.asList(entries).subList(second? secondStart: 0, second? entries.length: secondStart);
        }

        /** Returns a descriptor of one of the partitions of this distribution.
         *
         * @param second a <tt>boolean</tt> value determining if the descriptor of
         * second partition should be returned
         * @return the descriptor of the first (if <tt>second == false</tt>) or of the
         * second partition (if <tt>second == true</tt>)
         */
        protected Descriptor descriptor (boolean second) {
            return second? secondDescriptor: firstDescriptor;
        }

        /** Returns the number of dimenssions.
         *
         * @return the number of dimenssions
         */
        protected int getDim(){
            return dim;
        }

        /** Returns the sum of the margins of the two partitions.
         *
         * @return the sum of the margins of the two partitions
         */
        protected double marginValue () {
            return firstDescriptor.margin()+secondDescriptor.margin();
        }

        /** Returns the overlap of the two partitions.
         *
         * @return the overlap of the two partitions
         */
        protected double overlapValue () {
            return firstDescriptor.overlap(secondDescriptor);
        }

        /** Returns the sum of the areas of the two partitions.
         *
         * @return the sum of the areas of the two partitions
         */
        protected double areaValue () {
            return firstDescriptor.area()+secondDescriptor.area();
        }
    }

	/** Gets a suitable Converter to serialize the tree's nodes.
	 * 
	 * @param objectConverter a converter to convert the data objects stored in the tree
	 * @param dimensions the dimensions of the bounding rectangles 
	 * @return a NodeConverter
	 */
	public Converter nodeConverter (Converter objectConverter, final int dimensions) {
		return nodeConverter(objectConverter, indexEntryConverter(
			new ConvertableConverter(
				new Function () {
					public Object invoke () {
						return new DoublePointRectangle(dimensions);
					}
				}
			)
		));
	}
	
	/*********************************************************************/
	/*                       DEBUG FUNCTIONALITY                         */
	/*********************************************************************/
	
	/* (non-Javadoc)
	 * @see xxl.core.indexStructures.ORTree#checkDescriptors(xxl.core.indexStructures.ORTree.IndexEntry)
	 */
	public boolean checkDescriptors (IndexEntry indexEntry) {
		boolean returnValue = true;
		Node node = (Node)indexEntry.get(true);
		Descriptor descriptor = computeDescriptor(node.entries);

		if (!descriptor.equals(indexEntry.descriptor))
			System.out.println("Level "+node.level+": expected: "+descriptor+" actually:"+indexEntry.descriptor);
		if (node.level>0)
			for (Iterator entries = node.entries(); entries.hasNext();)
				if (!checkDescriptors((IndexEntry)entries.next()))
					returnValue = false;

		return returnValue;
	}
}

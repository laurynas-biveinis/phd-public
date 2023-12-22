/*  
   NEXMark Generator -- Niagara Extension to XMark Data Generator

   Acknowledgements:
   The NEXMark Generator was developed using the xmlgen generator 
   from the XMark Benchmark project as a basis. The NEXMark
   generator generates streams of auction elements (bids, items
   for auctions, persons) as opposed to the auction files
   generated by xmlgen.  xmlgen was developed by Florian Waas.
   See http://www.xml-benchmark.org for information.

   Copyright (c) Dept. of  Computer Science & Engineering,
   OGI School of Science & Engineering, OHSU. All Rights Reserved.

   Permission to use, copy, modify, and distribute this software and
   its documentation is hereby granted, provided that both the
   copyright notice and this permission notice appear in all copies
   of the software, derivative works or modified versions, and any
   portions thereof, and that both notices appear in supporting
   documentation.

   THE AUTHORS AND THE DEPT. OF COMPUTER SCIENCE & ENGINEERING 
   AT OHSU ALLOW USE OF THIS SOFTWARE IN ITS "AS IS" CONDITION, 
   AND THEY DISCLAIM ANY LIABILITY OF ANY KIND FOR ANY DAMAGES 
   WHATSOEVER RESULTING FROM THE USE OF THIS SOFTWARE.

   This software was developed with support from NSF ITR award
   IIS0086002 and from DARPA through NAVY/SPAWAR 
   Contract No. N66001-99-1-8098.

*/

// class to generate IDs for the auction generator
// is capable of generating new ids or generating existing IDs
// distributions supported are exponential and uniform

package xxl.applications.pipes.auctions.nexmark;
import java.util.Random;

class OpenAuctions {

    public static int ITEM_DISTR_SIZE = 100;

    // This class manages an array of open auction objects - each containing information
    // about an open auction.
    // To allow an arbitrary number of open auctions, we implement a scheme with two
    // arrays - one virtual, one physical. To code outside this class, the openAuctionId
    // is an index into the virtual array. The virtual array starts at 0 and grows indefinitely.
    // The physical array is a subarray of the virtual array - initial portions of the
    // physical array are deleted as the items in those initial portions close.
    // currPhysId indexes into the physical array, offset is the point in the virtual array
    // where the physical array starts so currPhysId+offset is the highest virtual open
    // auction id that has been issued.
    // closedPoint is relative to physical array, lowChunk and highChunk are maintained
    // relative to physical array

    // manage physical and virtual arrays
    private int currPhysId = 0; // physical
    private int offset = 0; // index of start of physical in virtual
    private int numSeqClosed = 0; // physical
    private int lowChunk = 0; // virtual
    private int highChunk = 1; // virtual
    private int allocSize; // allocated size of openAuctions array
    private OpenAuction[] openAuctions;
    private Random rnd = new Random(18394);
    private SimpleCalendar cal;

    public OpenAuctions(SimpleCalendar cal) {
        allocSize = 3000;
        openAuctions = new OpenAuction[allocSize];
        this.cal = cal;
    }

    // creates the open auction instance as well as returning the new id
    public int getNewId() {
        // don't bother to reuse the open auction objects - uugh, need to do this fast KT
        checkSpace(); // check to see if openAuctions needs to be expanded

        // as seen by outside world, id is always increasing and is id
        // of virtual array, real array may be shorter
        //assert !(currPhysId+offset == Integer.MAX_VALUE) :
        //    "KT: virtual Id is going to overflow - drats!!!";
        int virtualId = currPhysId+offset;
        OpenAuction newItem = new OpenAuction(cal, virtualId, rnd);
        openAuctions[currPhysId] = newItem;
        currPhysId++;
        if(virtualId == highChunk*ITEM_DISTR_SIZE) {
            highChunk++;
        }
        return virtualId;
    }

    public int getExistingId() {  // used by generateBid 
        int id;
        do {
            // generates an id between 0 and ITEM_DISTR_SIZE
            id = rnd.nextInt(ITEM_DISTR_SIZE); 

            // convert to offset somewhere in physical array 
            // which may be bigger than
            id += getRandomChunkOffset(); 

            // is closed checks to see if auction should be closed and closes
            // it if necessary
        } while (id >= currPhysId || openAuctions[id].isClosed(cal));
        openAuctions[id].recordBid();
        return id+offset; // return virtual id
    }

    public int increasePrice(int id) {
        //	return IntToString.strings[openAuctions[id-offset].increasePrice()];
        return openAuctions[id-offset].increasePrice();
    }   

    public long getEndTime(int id) {
        return openAuctions[id-offset].getEndTime();
    }

    public int getCurrPrice(int id) {
        return openAuctions[id-offset].getCurrPrice();
    }

    private void checkSpace() {
        // first try to get space by shrinking,
        // if there isn't enough to shrink, then we have to expand
        if(currPhysId == allocSize) {
            shrink();
            if(currPhysId == allocSize) {
                expand();
            }
        }
    }

    private void shrink() {
        // only shrink if I can make a lot of space
        if(numSeqClosed >= 500) {
            System.out.println("KT: Shrinking open auctions");
            for(int i = numSeqClosed; i<allocSize; i++)
                openAuctions[i-numSeqClosed] = openAuctions[i];
            offset += numSeqClosed;
            currPhysId -= numSeqClosed;
            numSeqClosed = 0;
            while(offset >= (lowChunk+1)*ITEM_DISTR_SIZE)
                lowChunk++; 
        }
    }

    private void expand() {
        int oldSize = allocSize;
        allocSize = allocSize*2;
        OpenAuction[] newArray = new OpenAuction[allocSize];
        // might as well shrink what we can as long as we are already copying
        for(int i = numSeqClosed; i<oldSize; i++)
            newArray[i-numSeqClosed] = openAuctions[i];
        offset += numSeqClosed;	// 
        currPhysId -= numSeqClosed;
        numSeqClosed = 0;
        openAuctions = newArray;
        while(offset >= (lowChunk+1)*ITEM_DISTR_SIZE)
            lowChunk++; 
    }

    private int getRandomChunkOffset() {
        int chunkId = rnd.nextInt(highChunk-lowChunk) + lowChunk;
        // chunks are maintained relative to virtual array, so need to subtract
        // offset to get a physical offset
        return (chunkId*ITEM_DISTR_SIZE)-offset;
    }
}

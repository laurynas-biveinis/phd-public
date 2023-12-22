/*
     Copyright (C) 2010, 2011, 2012 Laurynas Biveinis

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
package aau.bufferedIndexes.diskTrees.visitors;

import aau.bufferedIndexes.OperationType;
import aau.bufferedIndexes.TestUtils;
import aau.bufferedIndexes.UpdateTree;
import aau.bufferedIndexes.diskTrees.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xxl.core.indexStructures.Descriptor;
import xxl.core.spatial.KPE;
import xxl.core.spatial.rectangles.Rectangle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Unit tests for RRDiskTreeInvariantChecker class
 */
@RunWith(JMock.class)
public class RRTreeInvariantCheckerTest {

    private final Mockery context = new JUnit4Mockery();

    private RRDiskTreeInvariantChecker<KPE> checker;

    private IRRDiskTree<KPE> mockDiskTree;
    private IRRDiskUpdateTree<KPE> mockDiskUpdateTree;
    private IRRTreeIndexEntry<KPE> mockIndexEntry;
    private IRRTreeIndexEntry<KPE> mockIndexEntry2;
    private IRRTreeIndexEntry<KPE> mockIndexEntry3;
    private IRRTreeIndexEntry<KPE> mockIndexEntry4;
    private IRRTreeDiskDataNode<KPE> mockDataNode;
    private IRRTreeDiskDataNode<KPE> mockDataNode2;
    private IRRTreeDiskUpdateNode<KPE> mockUpdateNode;
    private IRRTreeDiskNode<KPE> mockNode;

    @Before
    public void setUp() {
        checker = new RRDiskTreeInvariantChecker<>();
        //noinspection unchecked
        mockDiskTree = context.mock(IRRDiskTree.class, "disk tree");
        //noinspection unchecked
        mockDiskUpdateTree = context.mock(IRRDiskUpdateTree.class, "disk update tree");
        //noinspection unchecked
        mockIndexEntry = context.mock(IRRTreeIndexEntry.class, "1st index entry");
        //noinspection unchecked
        mockDataNode = context.mock(IRRTreeDiskDataNode.class, "1st data node");
        //noinspection unchecked
        mockUpdateNode = context.mock(IRRTreeDiskUpdateNode.class, "update node");
        //noinspection unchecked
        mockNode = context.mock(IRRTreeDiskNode.class, "node");
        //noinspection unchecked
        mockIndexEntry2 = context.mock(IRRTreeIndexEntry.class, "2nd index entry");
        //noinspection unchecked
        mockIndexEntry3 = context.mock(IRRTreeIndexEntry.class, "3rd index entry");
        //noinspection unchecked
        mockIndexEntry4 = context.mock(IRRTreeIndexEntry.class, "4th index entry");
        //noinspection unchecked
        mockDataNode2 = context.mock(IRRTreeDiskDataNode.class, "2nd disk data node");
    }

    private void expectRootEntry(final IRRTreeIndexEntry<KPE> rootEntry) {
        context.checking(new Expectations() {{
            allowing(mockDiskTree).rootEntry(); will(returnValue(rootEntry));
        }});
    }

    private void assignNodeToIndexEntry(final IRRTreeIndexEntry<KPE> indexEntry,
                                        final IRRTreeDiskNode<KPE> nodeToReturn, final int id) {
        context.checking(new Expectations() {{
            oneOf(indexEntry).get(); will(returnValue(nodeToReturn));
            oneOf(indexEntry).id(); will(returnValue(id));
        }});
    }

    @Test(expected=IllegalStateException.class)
    public void emptyRoot() {
        assignNodeToIndexEntry(mockIndexEntry, mockDataNode, 1);
        expectRootEntry(mockIndexEntry);
        context.checking(new Expectations() {{
            oneOf(mockDataNode).overflows(); will(returnValue(false));
            oneOf(mockDataNode).number(); will(returnValue(0));
        }});
        checker.visitLeafNode(mockDiskTree, mockIndexEntry);
    }

    @Test(expected=IllegalStateException.class)
    public void underflowingLeaf() {
        assignNodeToIndexEntry(mockIndexEntry, mockDataNode, 1);
        expectRootEntry(null);
        context.checking(new Expectations() {{
            allowing(mockDataNode).overflows(); will(returnValue(false));
            oneOf(mockDataNode).underflows(); will(returnValue(true));
        }});
        checker.visitLeafNode(mockDiskTree, mockIndexEntry);
    }

    @Test(expected=IllegalStateException.class)
    public void overflowingLeaf() {
        assignNodeToIndexEntry(mockIndexEntry, mockDataNode, 1);
        expectRootEntry(null);
        context.checking(new Expectations() {{
            allowing(mockDataNode).underflows(); will(returnValue(false));
            oneOf(mockDataNode).overflows(); will(returnValue(true));
        }});
        checker.visitLeafNode(mockDiskTree, mockIndexEntry);
    }

    private void expectOKNodeSize(final IRRTreeDiskNode<KPE> node) {
        context.checking(new Expectations() {{
            allowing(node).underflows(); will(returnValue(false));
            allowing(node).overflows(); will(returnValue(false));
        }});
    }

    // TODO: drop KPE dependency
    private void expectEntryDescriptors(final KPE... entries) {
        for (final KPE entry : entries) {
            context.checking(new Expectations() {{
                allowing(mockDiskTree).descriptor(with(equal(entry)));
                    will(returnValue(entry.getData()));
            }});
        }
    }

    private void expectNodeContents(final IRRTreeDiskDataNode<KPE> dataNode, final KPE... entries) {
        expectOKNodeSize(dataNode);
        final Collection<KPE> nodeEntries = new ArrayList<>(Arrays.asList(entries));
        context.checking(new Expectations(){{
            allowing(dataNode).getLeafNodeEntries(); will(returnValue(nodeEntries));
        }});
        expectEntryDescriptors(entries);
    }

    private void expectIndexEntryDescriptor(final IRRTreeIndexEntry<KPE> indexEntry,
                                            final Descriptor indexEntryDescriptor) {
        context.checking(new Expectations(){{
            allowing(indexEntry).descriptor(); will(returnValue(indexEntryDescriptor));
            allowing(mockDiskTree).descriptor(with(equal(indexEntry))); will(returnValue(indexEntryDescriptor));
        }});
    }

    private void expectIndexEntryAndNode(final IRRTreeIndexEntry<KPE> indexEntry,
                                         final Descriptor indexEntryDescriptor,
                                         final int id, final IRRTreeDiskDataNode<KPE> dataNode,
                                         final KPE... entries) {
        assignNodeToIndexEntry(indexEntry, dataNode, id);
        expectIndexEntryDescriptor(indexEntry, indexEntryDescriptor);
        expectNodeContents(dataNode, entries);
    }

    @Test(expected=IllegalStateException.class)
    public void dataLeafEntryDescriptorOutsideNodeDescriptor() {
        expectRootEntry(null);
        expectIndexEntryAndNode(mockIndexEntry, TestUtils.makeDescriptor(0.0, 0.0, 3.0, 3.0),
                1, mockDataNode, TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0), TestUtils.makeKPE(10.0, 10.0, 20.0, 20.0));
        checker.visitLeafNode(mockDiskTree, mockIndexEntry);
    }

    @Test(expected=IllegalStateException.class)
    public void updateLeafEntryDescriptorOutsideNodeDescriptor() {
        final Rectangle dataDescriptor = TestUtils.makeRectangle(10.0, 10.0, 20.0, 20.0);
        final KPE dataKPE = new KPE(dataDescriptor);
        final UpdateTree.Entry<KPE> data = new UpdateTree.Entry<>(dataKPE, OperationType.INSERTION);
        final Collection<UpdateTree.Entry<KPE>> nodeContents = new ArrayList<>();
        nodeContents.add(data);

        assignNodeToIndexEntry(mockIndexEntry, mockUpdateNode, 1);
        expectRootEntry(null);
        expectOKNodeSize(mockUpdateNode);
        expectIndexEntryDescriptor(mockIndexEntry, TestUtils.makeDescriptor(0.0, 0.0, 1.0, 1.0));
        expectEntryDescriptors(dataKPE);
        context.checking(new Expectations() {{
            atLeast(1).of(mockUpdateNode).getLeafNodeEntries(); will(returnValue(nodeContents));
            oneOf(mockDiskUpdateTree).descriptor(with(same(data))); will(returnValue(dataDescriptor));
            // TODO: properly.
            //noinspection unchecked
            ignoring(mockDiskUpdateTree).doEntriesAnnihilate(with(any(UpdateTree.Entry.class)),
                    with(any(UpdateTree.Entry.class))); will(returnValue(false));
        }});
        checker.visitLeafNode (mockDiskUpdateTree, mockIndexEntry);
    }

    @Test(expected=IllegalStateException.class)
    public void unknownLeafNode() {
        assignNodeToIndexEntry(mockIndexEntry, mockNode, 1);
        expectRootEntry(null);
        expectOKNodeSize(mockNode);
        checker.visitLeafNode(mockDiskTree, mockIndexEntry);
    }

    @Test
    public void correctLeafNode() {
        expectRootEntry(null);
        expectIndexEntryAndNode(mockIndexEntry, TestUtils.makeDescriptor(0.0, 0.0, 3.0, 3.0),
                1, mockDataNode, TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0), TestUtils.makeKPE(2.0, 2.0, 3.0, 3.0));
        checker.visitLeafNode(mockDiskTree, mockIndexEntry);
    }

    @Test
    public void underflowingUpdateTreeLeafIsOK() {
        assignNodeToIndexEntry(mockIndexEntry, mockUpdateNode, 1);
        expectRootEntry(null);
        final Collection<UpdateTree.Entry<KPE>> leafEntries = new ArrayList<>();
        final KPE entryKPE = TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0);
        final UpdateTree.Entry<KPE> entry = new UpdateTree.Entry<>(entryKPE, OperationType.INSERTION);
        leafEntries.add(entry);
        context.checking(new Expectations() {{
            allowing(mockUpdateNode).underflows(); will(returnValue(true));
            allowing(mockUpdateNode).overflows(); will(returnValue(false));
            atLeast(1).of(mockUpdateNode).getLeafNodeEntries(); will(returnValue(leafEntries));
            allowing(mockDiskUpdateTree).descriptor(with(equal(entry)));
                will(returnValue(entryKPE.getData()));
            // TODO: properly.
            //noinspection unchecked
            ignoring(mockDiskUpdateTree).doEntriesAnnihilate(with(any(UpdateTree.Entry.class)),
                    with(any(UpdateTree.Entry.class))); will(returnValue(false));
        }});
        expectIndexEntryDescriptor(mockIndexEntry, TestUtils.makeDescriptor(0.0, 0.0, 3.0, 3.0));
        checker.visitLeafNode(mockDiskUpdateTree, mockIndexEntry);
    }

    @Test(expected=IllegalStateException.class)
    public void duplicateLeafContainerID() {
        expectRootEntry(null);
        expectIndexEntryAndNode(mockIndexEntry, TestUtils.makeDescriptor(0.0, 0.0, 3.0, 3.0),
                1, mockDataNode, TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        expectIndexEntryAndNode(mockIndexEntry2, TestUtils.makeDescriptor(5.0, 5.0, 8.0, 8.0),
                1, mockDataNode2, TestUtils.makeKPE(6.0, 6.0, 7.0, 7.0));

        checker.visitLeafNode (mockDiskTree, mockIndexEntry);
        checker.visitLeafNode (mockDiskTree, mockIndexEntry2);
    }

    @Test
    public void uniqueLeafContainerIDs() {
        expectRootEntry(null);
        expectIndexEntryAndNode(mockIndexEntry, TestUtils.makeDescriptor(0.0, 0.0, 3.0, 3.0),
                1, mockDataNode, TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        expectIndexEntryAndNode(mockIndexEntry2, TestUtils.makeDescriptor(5.0, 5.0, 8.0, 8.0),
                2, mockDataNode2, TestUtils.makeKPE(6.0, 6.0, 7.0, 7.0));

        checker.visitLeafNode (mockDiskTree, mockIndexEntry);
        checker.visitLeafNode (mockDiskTree, mockIndexEntry2);
    }

    @Test(expected=IllegalStateException.class)
    public void differentLevelEntryAndNode() {
        context.checking(new Expectations(){{
            oneOf(mockDataNode).level(); will(returnValue(2));
            oneOf(mockIndexEntry).level(); will(returnValue(1));
            oneOf(mockIndexEntry).id(); will(returnValue(null));
        }});
        checker.visitIndexNode(null, mockIndexEntry, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void singleEntryRoot() {
        expectRootEntry(mockIndexEntry);
        context.checking(new Expectations(){{
            allowing(mockDataNode).level(); will(returnValue(0));
            oneOf(mockDataNode).number(); will(returnValue(1));
            allowing(mockIndexEntry).id(); will(returnValue(null));
            allowing(mockIndexEntry).level(); will(returnValue(0));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void underflowingIndexNode() {
        expectRootEntry(null);
        context.checking(new Expectations(){{
            allowing(mockDataNode).level(); will(returnValue(0));
            atLeast(1).of(mockDataNode).underflows(); will(returnValue(true));
            allowing(mockIndexEntry).id(); will(returnValue(null));
            allowing(mockIndexEntry).level(); will(returnValue(0));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void overflowingIndexNode() {
        expectRootEntry(null);
        context.checking(new Expectations(){{
            allowing(mockDataNode).level(); will(returnValue(0));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            atLeast(1).of(mockDataNode).overflows(); will(returnValue(true));
            allowing(mockIndexEntry).id(); will(returnValue(null));
            allowing(mockIndexEntry).level(); will(returnValue(0));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void wrongLevelChildrenEntries() {
        final Descriptor mbr = TestUtils.makeRectangle(0.0, 0.0, 3.0, 3.0);
        expectRootEntry(null);
        expectIndexEntryDescriptor(mockIndexEntry, null);
        expectIndexEntryDescriptor(mockIndexEntry2, mbr);
        context.checking(new Expectations(){{
            allowing(mockIndexEntry).id(); will(returnValue(null));
            allowing(mockIndexEntry).level(); will(returnValue(2));
            allowing(mockIndexEntry2).level(); will(returnValue(0));
            allowing(mockDataNode).level(); will(returnValue(2));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            allowing(mockDataNode).overflows(); will(returnValue(false));
            atLeast(1).of(mockDataNode).getNonLeafNodeEntries();
            will(returnValue(Collections.unmodifiableCollection(Arrays.asList(mockIndexEntry2))));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void childEntryOutsideParentMBR() {
        final Descriptor mbr = TestUtils.makeRectangle(0.0, 0.0, 3.0, 3.0);
        final Descriptor mbr2 = TestUtils.makeRectangle(5.0, 5.0, 8.0, 8.0);
        expectRootEntry(null);
        expectIndexEntryDescriptor(mockIndexEntry, mbr);
        expectIndexEntryDescriptor(mockIndexEntry2, mbr2);
        context.checking(new Expectations(){{
            allowing(mockIndexEntry).id(); will(returnValue(null));
            allowing(mockIndexEntry).level(); will(returnValue(2));
            allowing(mockIndexEntry2).level(); will(returnValue(1));
            allowing(mockDataNode).level(); will(returnValue(2));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            allowing(mockDataNode).overflows(); will(returnValue(false));
            atLeast(1).of(mockDataNode).getNonLeafNodeEntries();
            will(returnValue(Collections.unmodifiableCollection(Arrays.asList(mockIndexEntry2))));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void parentMBRLargerThanChildMBRs() {
        final Descriptor mbr = TestUtils.makeRectangle(0.0, 0.0, 10.0, 10.0);
        final Descriptor mbr2 = TestUtils.makeRectangle(1.0, 1.0, 2.0, 2.0);
        final Descriptor mbr3 = TestUtils.makeRectangle(4.0, 4.0, 6.0, 6.0);
        expectRootEntry(null);
        expectIndexEntryDescriptor(mockIndexEntry, mbr);
        expectIndexEntryDescriptor(mockIndexEntry2, mbr2);
        expectIndexEntryDescriptor(mockIndexEntry3, mbr3);
        context.checking(new Expectations(){{
            allowing(mockIndexEntry).id(); will(returnValue(null));
            allowing(mockIndexEntry).level(); will(returnValue(2));
            allowing(mockIndexEntry2).level(); will(returnValue(1));
            allowing(mockIndexEntry3).level(); will(returnValue(1));
            allowing(mockDataNode).level(); will(returnValue(2));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            allowing(mockDataNode).overflows(); will(returnValue(false));
            atLeast(1).of(mockDataNode).getNonLeafNodeEntries();
            will(returnValue(Collections.unmodifiableCollection(Arrays.asList(mockIndexEntry2, mockIndexEntry3))));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);

    }

    @Test
    public void correctIndexNode() {
        final Descriptor mbr = TestUtils.makeRectangle(0.0, 0.0, 10.0, 10.0);
        final Descriptor mbr2 = TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0);
        final Descriptor mbr3 = TestUtils.makeRectangle(8.0, 8.0, 10.0, 10.0);
        expectRootEntry(null);
        expectIndexEntryDescriptor(mockIndexEntry, mbr);
        expectIndexEntryDescriptor(mockIndexEntry2, mbr2);
        expectIndexEntryDescriptor(mockIndexEntry3, mbr3);
        context.checking(new Expectations() {{
            allowing(mockIndexEntry).id(); will(returnValue(null));
            allowing(mockIndexEntry).level(); will(returnValue(2));
            allowing(mockIndexEntry2).level(); will(returnValue(1));
            allowing(mockIndexEntry3).level(); will(returnValue(1));
            allowing(mockDataNode).level(); will(returnValue(2));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            allowing(mockDataNode).overflows(); will(returnValue(false));
            atLeast(1).of(mockDataNode).getNonLeafNodeEntries();
            will(returnValue(Collections.unmodifiableCollection(Arrays.asList(mockIndexEntry2, mockIndexEntry3))));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void duplicateIndexContainerID() {
        final Descriptor mbr = TestUtils.makeRectangle(0.0, 0.0, 10.0, 10.0);
        final Descriptor mbr2 = TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0);
        final Descriptor mbr3 = TestUtils.makeRectangle(8.0, 8.0, 10.0, 10.0);
        expectRootEntry(null);
        expectIndexEntryDescriptor(mockIndexEntry, mbr);
        expectIndexEntryDescriptor(mockIndexEntry2, mbr2);
        expectIndexEntryDescriptor(mockIndexEntry3, mbr3);
        context.checking(new Expectations() {{
            atLeast(1).of(mockIndexEntry).id();
            will(returnValue(1));
            allowing(mockIndexEntry).level();
            will(returnValue(2));
            allowing(mockIndexEntry2).level();
            will(returnValue(1));
            allowing(mockIndexEntry3).level();
            will(returnValue(1));
            allowing(mockDataNode).level(); will(returnValue(2));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            allowing(mockDataNode).overflows(); will(returnValue(false));
            atLeast(2).of(mockDataNode).getNonLeafNodeEntries();
            will(returnValue(Collections.unmodifiableCollection(Arrays.asList(mockIndexEntry2, mockIndexEntry3))));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
    }

    @Test
    public void uniqueIndexContainerID() {
        final Descriptor mbr = TestUtils.makeRectangle(0.0, 0.0, 10.0, 10.0);
        final Descriptor mbr2 = TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0);
        final Descriptor mbr3 = TestUtils.makeRectangle(8.0, 8.0, 10.0, 10.0);
        expectRootEntry(null);
        expectIndexEntryDescriptor(mockIndexEntry, mbr);
        expectIndexEntryDescriptor(mockIndexEntry2, mbr2);
        expectIndexEntryDescriptor(mockIndexEntry3, mbr3);
        expectIndexEntryDescriptor(mockIndexEntry4, mbr);
        context.checking(new Expectations(){{
            atLeast(1).of(mockIndexEntry).id(); will(returnValue(1));
            atLeast(1).of(mockIndexEntry4).id(); will(returnValue(2));
            allowing(mockIndexEntry).level(); will(returnValue(2));
            allowing(mockIndexEntry4).level(); will(returnValue(2));
            allowing(mockIndexEntry2).level(); will(returnValue(1));
            allowing(mockIndexEntry3).level(); will(returnValue(1));
            allowing(mockDataNode).level(); will(returnValue(2));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            allowing(mockDataNode).overflows(); will(returnValue(false));
            atLeast(2).of(mockDataNode).getNonLeafNodeEntries();
            will(returnValue(Collections.unmodifiableCollection(Arrays.asList(mockIndexEntry2, mockIndexEntry3))));
        }});
        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
        checker.visitIndexNode(mockDiskTree, mockIndexEntry4, mockDataNode);
    }

    @Test(expected=IllegalStateException.class)
    public void duplicateIndexLeafContainerID() {
        expectRootEntry(null);
        final Rectangle indexEntryMBR = TestUtils.makeRectangle(0.0, 0.0, 10.0, 10.0);
        expectIndexEntryDescriptor(mockIndexEntry, indexEntryMBR);
        context.checking (new Expectations(){{
            oneOf(mockIndexEntry).id(); will(returnValue(1));
            oneOf(mockIndexEntry).level(); will(returnValue(2));
        }});
        //noinspection unchecked
        final IRRTreeIndexEntry<KPE> indexNodeEntry = context.mock(IRRTreeIndexEntry.class, "1st index node entry");
        //noinspection unchecked
        final IRRTreeIndexEntry<KPE> indexNodeEntry2 = context.mock(IRRTreeIndexEntry.class, "2nd index node entry");
        expectIndexEntryDescriptor(indexNodeEntry, TestUtils.makeRectangle(0.0, 0.0, 2.0, 2.0));
        expectIndexEntryDescriptor(indexNodeEntry2, TestUtils.makeRectangle(8.0, 8.0, 10.0, 10.0));
        context.checking (new Expectations(){{
            oneOf(indexNodeEntry).level(); will(returnValue(1));
            oneOf(indexNodeEntry2).level(); will(returnValue(1));
            allowing(mockDataNode).level(); will(returnValue(2));
            allowing(mockDataNode).underflows(); will(returnValue(false));
            allowing(mockDataNode).overflows(); will(returnValue(false));
            atLeast(1).of(mockDataNode).getNonLeafNodeEntries();
            will(returnValue(Collections.unmodifiableCollection(Arrays.asList(indexNodeEntry, indexNodeEntry2))));
        }});

        checker.visitIndexNode(mockDiskTree, mockIndexEntry, mockDataNode);
        expectIndexEntryAndNode(mockIndexEntry2, TestUtils.makeDescriptor(0.0, 0.0, 3.0, 3.0),
                1, mockDataNode, TestUtils.makeKPE(1.0, 1.0, 2.0, 2.0));
        checker.visitLeafNode(mockDiskTree, mockIndexEntry2);
    }

    @Test
    public void finishVisiting() {
        checker.finishVisiting();
    }
}

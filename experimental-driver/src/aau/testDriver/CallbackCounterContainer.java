/*
     Copyright (C) 2011 Laurynas Biveinis

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
package aau.testDriver;

import xxl.core.collections.containers.Container;
import xxl.core.collections.containers.CounterContainer;
import xxl.core.functions.Function;

import java.util.NoSuchElementException;

/**
 * A counter container that invokes specified callbacks at specified counter intervals
 */
public class CallbackCounterContainer extends CounterContainer {

    /**
     * The I/O interval between notifications
     */
    final int ioCallbackInterval;

    /**
     * The callback to be notified
     */
    final IOIntervalEvent ioIntervalEvent;

    boolean callbackEnabled;

    /**
     * Constructs a new CallbackCounterContainer that is a CounterContainer and additionally notifies a given 
     * IOIntervalEvent on every x I/Os.
     *
     * @param container the container to be decorated with the counter.
     * @param ioCallbackInterval the I/O interval between notifications.
     * @param ioIntervalEvent the callback to be notified
     */
    public CallbackCounterContainer(final Container container, final int ioCallbackInterval, 
                                    final IOIntervalEvent ioIntervalEvent) {
        super(container);
        this.ioCallbackInterval = ioCallbackInterval;
        this.ioIntervalEvent = ioIntervalEvent;
        this.callbackEnabled = true;
    }

    @Override
    public Object get(Object id, boolean unfix) throws NoSuchElementException {
        final Object result = super.get(id, unfix);
        maybeNotify();
        return result;
    }

    @Override
    public Object insert(Object object, boolean unfix) {
        final Object result = super.insert(object, unfix);
        maybeNotify();
        return result;
    }

    @Override
    public void remove(Object id) throws NoSuchElementException {
        super.remove(id);
        maybeNotify();
    }

    @Override
    public Object reserve(Function getObject) {
        throw new UnsupportedOperationException("Reserves are not I/O-accounted!");
    }

    @Override
    public void update(Object id, Object object, boolean unfix) throws NoSuchElementException {
        super.update(id, object, unfix);
        maybeNotify();
    }

    public void disableIoIntervalNotifications() {
        callbackEnabled = false;

    }

    public void enableIoIntervalNotifications() {
        callbackEnabled = true;
    }

    private void maybeNotify() {
        if (!callbackEnabled || ioCallbackInterval == 0)
            return;
        if ((inserts + gets + updates + removes) % ioCallbackInterval == 0)
            ioIntervalEvent.notifyIoInterval();        
    }
    
}

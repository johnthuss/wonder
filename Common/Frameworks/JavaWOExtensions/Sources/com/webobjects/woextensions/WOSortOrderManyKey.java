/*
 * WOSortOrderManyKey.java
 * � Copyright 2001 Apple Computer, Inc. All rights reserved.
 * This a modified version.
 * Original license: http://www.opensource.apple.com/apsl/
 */

package com.webobjects.woextensions;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;

import java.util.Enumeration;

public class WOSortOrderManyKey extends WOComponent {
    protected String _currentKey;
    protected String _selectedKey;
    // ** Internal Caching
    protected WODisplayGroup _displayGroup;

    public WOSortOrderManyKey(WOContext aContext)  {
        super(aContext);
    }

    public boolean isStateless() {
        return true;
    }

    public void reset()  {
        _currentKey=null;
        _selectedKey=null;
        _displayGroup=null;
    }

    /////////////
    // Bindings
    ////////////
    public WODisplayGroup displayGroup() {
        if (null==_displayGroup) {
            _displayGroup = (WODisplayGroup)_WOJExtensionsUtil.valueForBindingOrNull("displayGroup",this);
        }
        return _displayGroup;
    }

    ///////////
    // Utility
    ///////////
    protected EOSortOrdering _primarySortOrdering() {
        NSArray anArray = displayGroup().sortOrderings();
        if ((anArray!=null) && (anArray.count() > 0)) {
            EOSortOrdering anOrdering = (EOSortOrdering)anArray.objectAtIndex(0);
            return anOrdering;
        }
        return null;
    }

    public void setSelectedKey(String aNewValue) {
        _selectedKey = aNewValue;
        if (_isCurrentKeyPrimary()) {
            _removeSortOrderingWithKey(selectedKey());
        }
        _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareAscending);
    }

    public String selectedKey() {
        if (null==_selectedKey) {
            setSelectedKey(_primarySortOrdering().key());
        }
        return _selectedKey;
    }

    protected boolean _isCurrentKeyPrimary() {
        EOSortOrdering anOrdering = _primarySortOrdering();
        if ((anOrdering!=null) && anOrdering.key().equals(selectedKey())) {
            return true;
        }
        return false;
    }

    protected NSSelector _primaryKeySortOrderingSelector() {
        EOSortOrdering anOrdering = _primarySortOrdering();
        NSSelector anOrderingSelector = null;
        if (anOrdering!=null) anOrderingSelector = anOrdering.selector();
        return anOrderingSelector;
    }

    protected void _removeSortOrderingWithKey(String aKey) {
        int anIndex = 0;
        EOSortOrdering aSortOrdering = null;
        WODisplayGroup aDisplayGroup = displayGroup();
        NSArray sortOrderings = aDisplayGroup.sortOrderings();
        if (sortOrderings!=null) {
            NSMutableArray aSortOrderingArray = sortOrderings.mutableClone();
            Enumeration anEnumerator = aSortOrderingArray.objectEnumerator();
            while (anEnumerator.hasMoreElements()) {
                aSortOrdering = (EOSortOrdering) anEnumerator.nextElement();
                if (aKey.equals(aSortOrdering.key())) {
                    aSortOrderingArray.removeObjectAtIndex(anIndex);
                    break;
                }
                anIndex++;
            }
            aDisplayGroup.setSortOrderings(aSortOrderingArray);
        }
    }

    protected void _makePrimarySortOrderingWithSelector(NSSelector aSelector) {
        String aKey = selectedKey();
        WODisplayGroup aDisplayGroup = displayGroup();
        NSArray sortOrderings = aDisplayGroup.sortOrderings();
        NSMutableArray aSortOrderingArray;
        if (sortOrderings!=null) {
            aSortOrderingArray = new NSMutableArray(sortOrderings);
        } else {
            aSortOrderingArray = new NSMutableArray();
        }
        EOSortOrdering aNewSortOrdering = EOSortOrdering.sortOrderingWithKey(aKey, aSelector);
        aSortOrderingArray.insertObjectAtIndex(aNewSortOrdering, 0);
        if (aSortOrderingArray.count() > 3) {
            // ** limits sorting to 3 levels
            aSortOrderingArray.removeLastObject();
        }
        aDisplayGroup.setSortOrderings(aSortOrderingArray);
    }

    /////////////
    // Actions
    /////////////
    public WOComponent sortAscendingClicked() {
        if (_isCurrentKeyPrimary()) {
            _removeSortOrderingWithKey(selectedKey());
        }
        _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareAscending);
        displayGroup().updateDisplayedObjects();
        return null;
    }

    public WOComponent sortDescendingClicked() {
        if (_isCurrentKeyPrimary()) {
            _removeSortOrderingWithKey(selectedKey());
        }
        _makePrimarySortOrderingWithSelector(EOSortOrdering.CompareDescending);
        displayGroup().updateDisplayedObjects();
        return null;
    }

}
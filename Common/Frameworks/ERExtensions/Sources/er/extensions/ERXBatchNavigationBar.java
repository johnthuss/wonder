/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

// Corrects two bugs
// 1) User typing null for either number of objects per batch or the page number
// 2) When resetting the number of items per batch the page first page displayed would be the last page.

// dscheck: 5/10/2007
// I changed the component to use the ERXOptionalForm component allowig ERXBatchNavigationBar to also work within an existing form.
// This seems to be a better approach than always having to change the rule to use ERXBatchNavigationBarInForm when a form already exists.
// The component uses a hidden WOImageButton and javascript onChange to simulate clicking the button when the either textfield value is changed.
// This ensures that the default action for an existing form is not invoked when the user changes the value and hits enter.
 
import com.webobjects.foundation.*;
import com.webobjects.appserver.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;

/**
 * Better navigation bar<br />
 * 
 * @binding d2wContext
 * @binding displayGroup
 * @binding width
 * @binding objectName
 * @binding border
 * @binding bgcolor
 * @binding textColor
 * @binding sortKeyList
 * @binding showForm
*/

public class ERXBatchNavigationBar extends WOComponent {

    /** logging support */
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXBatchNavigationBar.class);

    /** Contains a string that names the notification posted when the batch size changes */
    public final static String BatchSizeChanged = "BatchSizeChanged";
    private String _buttonId=null;
    private Number numberOfObjectsPerBatch;
    private Number currentBatchIndex;

    /** Public constructor */
    public ERXBatchNavigationBar(WOContext aContext) {
        super(aContext);
    }

    /** component is stateless */
    public boolean isStateless() { return false; }
    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public void reset() {
        if (log.isDebugEnabled()) log.debug("reset: BEGIN "+hashCode());
        super.reset();
        if (log.isDebugEnabled()) log.debug("reset: END "+hashCode());
    }

    public void sleep() {
        if (log.isDebugEnabled()) log.debug("sleep: BEGIN "+hashCode());
        numberOfObjectsPerBatch = null;
        currentBatchIndex = null;
        _displayGroup = null;
        _buttonId = null;
        super.sleep();
        if (log.isDebugEnabled()) log.debug("sleep: END "+hashCode());
    }
    
    public void appendToResponse(WOResponse response, WOContext context) {
        if (log.isDebugEnabled()) log.debug("appendToResponse: BEGIN "+hashCode());

        if (displayGroup() != null  &&  ! displayGroup().hasMultipleBatches()) {
            if (currentBatchIndex() != 0) 
                _setCurrentBatchIndex(1);
        }
        super.appendToResponse(response, context);

        if (log.isDebugEnabled()) log.debug("appendToResponse: END "+hashCode());
    }
    
    private WODisplayGroup _displayGroup;
    public WODisplayGroup displayGroup() {
        if (_displayGroup == null) {
            _displayGroup = (WODisplayGroup)valueForBinding("displayGroup");
        }
        return _displayGroup;
    }
    
    public boolean hasObjectName() { return hasBinding("objectName"); }
    public boolean hasSortKeyList() { return hasBinding("sortKeyList"); }

    public int numberOfObjectsPerBatch() {
        return displayGroup()!=null ? displayGroup().numberOfObjectsPerBatch() : 0;
    }

    public int currentBatchIndex() {
        return displayGroup()!=null ? displayGroup().currentBatchIndex() : 0;        
    }

    // Save new value and only apply to displayGroup if the refresh action was invoked
    public void setCurrentBatchIndex(Number newValue) {
        if (log.isDebugEnabled()) log.debug("setCurrentBatchIndex: "+hashCode() +" value = "+newValue);
        currentBatchIndex = newValue;
    }

    // Now only called when refresh action is invoked
    private void _setCurrentBatchIndex(int newValue) {
        if (log.isDebugEnabled()) log.debug("_setCurrentBatchIndex: BEGIN "+hashCode() +" value = "+newValue);
        if (newValue > 0) {
            if (displayGroup()!=null){
                displayGroup().setCurrentBatchIndex(newValue);
                if (log.isDebugEnabled()) log.debug("The batch index is being set to :"+newValue);
            }
        }
        if (log.isDebugEnabled()) log.debug("_setCurrentBatchIndex: END "+hashCode());
    }

    // Save new value and only apply to displayGroup if the refresh action was invoked
    public void setNumberOfObjectsPerBatch(Number newValue) {
        if (log.isDebugEnabled()) log.debug("setNumberOfObjectsPerBatch: "+hashCode() +" value = "+newValue);
        numberOfObjectsPerBatch = newValue;
    }

    // Now only called when refresh action is invoked
    private void _setNumberOfObjectsPerBatch(int newValue) {
        if (log.isDebugEnabled()) log.debug("_setNumberOfObjectsPerBatch: BEGIN "+hashCode() +" value = "+newValue);
        if (newValue >0) {
            if (displayGroup()!=null) {
                if (log.isDebugEnabled()) log.debug("Setting db # of objects per batch to "+newValue);
                displayGroup().setNumberOfObjectsPerBatch(newValue);

                if(log.isDebugEnabled()) log.debug("The batch index is being set to : "+ 1);
                displayGroup().setCurrentBatchIndex(1);
            }
            Object context=valueForBinding("d2wContext");
            if (context!=null) {
                NSNotificationCenter.defaultCenter().postNotification("BatchSizeChanged",
                                                                      ERXConstant.integerForInt(newValue),
                                                                      new NSDictionary(context,"d2wContext"));
            }
        }
        if (log.isDebugEnabled()) log.debug("_setNumberOfObjectsPerBatch: END "+hashCode());
    }

    public int filteredObjectsCount() {
        WODisplayGroup dg=displayGroup();
        int result=0;
        EOQualifier q=dg.qualifier();
        if (q!=null) {
            result=EOQualifier.filteredArrayWithQualifier(dg.allObjects(),q).count();
        } else {
            result=dg.allObjects().count();
        }
        return result;
    }
    

    public int objectCount() {
        if (displayGroup() instanceof ERXBatchingDisplayGroup) {
            return ((ERXBatchingDisplayGroup)displayGroup()).rowCount();
        } else {
            return displayGroup().allObjects().count();
        }
    }

    // Reworked the actual changing of the displayGroup to when the hidden button is clicked
    // This fixes the problem when you have a top and bottom batch nav bar and the bottom values always win
    // even if they are not changed since they were forced back into the displayGroup on takeValuesFromRequest.    
    public WOComponent refresh() {
        if (log.isDebugEnabled()) log.debug("refresh BEGIN "+hashCode());
        if (numberOfObjectsPerBatch != null)
            _setNumberOfObjectsPerBatch(numberOfObjectsPerBatch.intValue());

        if (currentBatchIndex != null)
            _setCurrentBatchIndex(currentBatchIndex.intValue());

        if (log.isDebugEnabled()) log.debug("refresh END "+hashCode());
        return null;
    }

    public String hiddenImageButtonId() {
        if (_buttonId == null)
            _buttonId = context().elementID()+".hiddenBatchNavButton";

        return _buttonId;
    }

    public String simulateHiddenButtonClick() {
        return "return document.getElementById('"+hiddenImageButtonId()+"').click();";
    }

}
package er.cayenne;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.PersistenceState;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.map.Relationship;
import org.apache.cayenne.validation.ValidationResult;

import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSKeyValueCodingAdditions;

/**
 * Adds Key Value Coding (KVC) support to CayenneDataObject to make it suitable for use in WO applications.
 * 
 * @author john
 *
 */
public class CayenneObject extends CayenneDataObject implements NSKeyValueCodingAdditions, NSKeyValueCoding.ErrorHandling {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public CayenneObject() {
	}

	public void takeValueForKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.takeValueForKey(this, value, key);
	}

	public Object valueForKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.valueForKey(this, key);
	}

	public void takeValueForKeyPath(Object value, String keyPath) {
		NSKeyValueCodingAdditions.DefaultImplementation.takeValueForKeyPath(this, value, keyPath);
	}

	public Object valueForKeyPath(String keyPath) {
		return NSKeyValueCodingAdditions.DefaultImplementation.valueForKeyPath(this, keyPath);
	}

	public Object handleQueryWithUnboundKey(String key) {
		return NSKeyValueCoding.DefaultImplementation.handleQueryWithUnboundKey(this, key);
	}

	public void handleTakeValueForUnboundKey(Object value, String key) {
		NSKeyValueCoding.DefaultImplementation.handleTakeValueForUnboundKey(this, value, key);
	}

	public void unableToSetNullForKey(String key) {
		NSKeyValueCoding.DefaultImplementation.unableToSetNullForKey(this, key);
	}

	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public Object readProperty(String propertyName) {
		return super.readProperty(propertyName);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public Object readPropertyDirectly(String propertyName) {
		return super.readPropertyDirectly(propertyName);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public Object readNestedProperty(String path) {
		return super.readNestedProperty(path);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public void writeProperty(String propertyName, Object value) {
		super.writeProperty(propertyName, value);
	}
	
	/**
	 * Use KVC methods instead
	 */
	@Deprecated
	@Override
	public void writePropertyDirectly(String propertyName, Object value) {
		super.writePropertyDirectly(propertyName, value);
	}
	
	public ObjEntity entity() {
		return Cayenne.getObjEntity(this);
	}
	
    public String entityName() { 
    	return entity().getName(); 
    }
    
    public List<String> attributeKeys() { 
    	return new ArrayList<String>(entity().getAttributeMap().keySet()); 
    }

	public List<String> toManyRelationshipKeys() {
		List<String> result = new ArrayList<String>();
		for (ObjRelationship relationship : entity().getRelationships()) {
			if (relationship.isToMany()) {
				if (!relationship.getName().startsWith("runtimeRelationship")) { // skip runtime generated relationships and just return the info defined in the model
					result.add(relationship.getName());
				}
			}
		}
		return result;
	}

	public List<String> toOneRelationshipKeys() {
		List<String> result = new ArrayList<String>();
		for (ObjRelationship relationship : entity().getRelationships()) {
			if (!relationship.isToMany()) {
				if (!relationship.getName().startsWith("runtimeRelationship")) { // skip runtime generated relationships and just return the info defined in the model
					result.add(relationship.getName());
				}
			}
		}
		return result;
	}
	
	public List<String> allPropertyKeys() {
		List<String> result = new ArrayList<String>();
		result.addAll(attributeKeys());
		result.addAll(toOneRelationshipKeys());
		result.addAll(toManyRelationshipKeys());
		return result;
	}
	
	public boolean isReadOnly() {
		return entity().isReadOnly();
	}

	public boolean isToManyKey(String key) {
		Relationship relationship = entity().getRelationship(key);
		return relationship != null && relationship.isToMany();
	}

	public boolean isFault() {
		return getPersistenceState() == PersistenceState.HOLLOW;
	}

	public void willRead() {
		if (getObjectContext() != null && !attributeKeys().isEmpty()) {
			getObjectContext().prepareForAccess(this, attributeKeys().get(0), true);
		}
	}

	public <T extends CayenneObject> T localInstanceIn(ObjectContext context) {
		return (T) context.localObject(this);
	}
	
	public void delete() {
		if (getObjectContext() != null) {
			getObjectContext().deleteObjects(this);
		}
	}
	
	public boolean isNewObject() {
		if (getObjectContext() == null) return true;
		return getPersistenceState() == PersistenceState.NEW;
	}
	
}


package com.pagesociety.persistence;

/**
 *
 * <p>
 * Provides a structure for defining how reference fields link to each other.
 * Entity fields can be related as:
 * <ul>
 * <li>one-to-one</li>
 * <li>one-to-many</li>
 * <li>many-to-one</li>
 * <li>many-to-many</li>
 * </ul>
 * </p>
 *
 * Example:
 *
 * <code><pre>
 * new EntityRelationship(&quot;Author&quot;, &quot;books&quot;, EntityRelationShip.MANY_TO_MANY, &quot;Book&quot;, &quot;authors&quot;);
 * new EntityRelationship(&quot;Portfolio&quot;, &quot;resources&quot;, EntityRelationShip.ONE_TO_MANY, &quot;Resource&quot;, &quot;portfolio&quot;);
 * </pre></code>
 *
 * @author Topher LaFata
 * @author David Karam
 *
 * @see PersistentStore#addEntityRelationship(EntityRelationshipDefinition)
 *
 */
public class EntityRelationshipDefinition implements java.io.Serializable
{
	/**
	 * One to one cardinality.
	 */
	public static final int TYPE_ONE_TO_ONE = 0x01;
	/**
	 * One to many cardinality.
	 */
	public static final int TYPE_ONE_TO_MANY = 0x02;
	/**
	 * Many to one cardinality.
	 */
	public static final int TYPE_MANY_TO_ONE = 0x03;
	/**
	 * Many to many cardinality.
	 */
	public static final int TYPE_MANY_TO_MANY = 0x04;
	private int _type;
	private String _originating_entity = null;
	private String _originating_entity_field = null;
	private String _target_entity = null;
	private String _target_entity_field = null;

	/**
	 * Constructs a relationship definition.
	 *
	 * @param originating_entity
	 *            One side of the relationship.
	 * @param originating_entity_field
	 *            The field on this side.
	 * @param type
	 *            The cardinality of the relationship.
	 * @param target_entity
	 *            The other side of the relationship.
	 * @param target_entity_field
	 *            The field on the other side.
	 */
	public EntityRelationshipDefinition(String originating_entity,
			String originating_entity_field, int type, String target_entity,
			String target_entity_field)
	{
		_type = type;
		_originating_entity = originating_entity;
		_originating_entity_field = originating_entity_field;
		_target_entity = target_entity;
		_target_entity_field = target_entity_field;
	}

	/**
	 * Returns the cardinality of the entity relationship.
	 *
	 * @return An integer of one of the types specified statically in this
	 *         class.
	 */
	public int getType()
	{
		return _type;
	}

	/**
	 * Returns the entity that was specified as 'originating'.
	 *
	 * @return The originating entity name.
	 */
	public String getOriginatingEntity()
	{
		return _originating_entity;
	}

	/**
	 * Returns the field from the originating side.
	 *
	 * @return The name of the originating entity field.
	 */
	public String getOriginatingEntityField()
	{
		return _originating_entity_field;
	}

	/**
	 * Returns the entity that was specified as 'target'.
	 *
	 * @return The name of the target entity.
	 */
	public String getTargetEntity()
	{
		return _target_entity;
	}

	/**
	 * Returns the field from the target side.
	 *
	 * @return The name of the target field.
	 */
	public String getTargetEntityField()
	{
		return _target_entity_field;
	}

	/**
	 * A simple toString method for the 4 possible types/cardinalities.
	 *
	 * @param type
	 *            The type/cardinality.
	 * @return A text representation of the cardinality.
	 */
	public static String describeType(int type)
	{
		switch (type)
		{
		case TYPE_ONE_TO_ONE:
			return "1 to 1";
		case TYPE_ONE_TO_MANY:
			return "1 to Many";
		case TYPE_MANY_TO_MANY:
			return "Many To Many";
		default:
			return "Unknown Type";
		}
	}

	/**
	 * Returns a string representation of this object.
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer();
		b.append(describeType(_type) + ":\n");
		b.append("\toriginating_entity:\t" + _originating_entity + "\n");
		b.append("\toriginating_entity_field:\t" + _originating_entity_field + "\n");
		b.append("\ttarget_entity:\t" + _target_entity + "\n");
		b.append("\ttarget_entity_field:\t" + _target_entity_field + "\n");
		return b.toString();
	}

	/**
	 * This methods turns the originating side into the target and the target
	 * into the originator. If the type was one to many or many to one, the type
	 * is flipped as well.
	 *
	 * @return A new, flipped entity relationship.
	 */
	public EntityRelationshipDefinition flip()
	{
		int type = _type;
		switch (_type)
		{
		case EntityRelationshipDefinition.TYPE_ONE_TO_MANY:
			type = EntityRelationshipDefinition.TYPE_MANY_TO_ONE;
			break;
		case EntityRelationshipDefinition.TYPE_MANY_TO_ONE:
			type = EntityRelationshipDefinition.TYPE_ONE_TO_MANY;
			break;
		}
		return new EntityRelationshipDefinition(_target_entity, _target_entity_field, type, _originating_entity, _originating_entity_field);
	}
}

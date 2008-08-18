package com.pagesociety.sql.test;

import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.Types;

public class Bookmark
{
	public static String ENTITY_NAME = "Bookmark";
	public static String FIELD_IMG_URL = "url";
	public static String FIELD_PAGE_URL = "pageUrl";
	public static String FIELD_DESCRIPTION = "description";
	public static String FIELD_NOTES = "notes";
	public static String FIELD_TAGS = "tags";
	public static String FIELD_CATEGORY = "category";
	public static String FIELD_IS_PRIVATE = "isPrivate";
	public static String FIELD_CREATE_DATE = "createDate";
	public static String FIELD_USER = "user";
	public static EntityDefinition DEF;
	static
	{
		DEF = new EntityDefinition(ENTITY_NAME);
		DEF.addField(new FieldDefinition(FIELD_IMG_URL, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_PAGE_URL, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_DESCRIPTION, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_NOTES, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_TAGS, Types.TYPE_STRING | Types.TYPE_ARRAY));
		DEF.addField(new FieldDefinition(FIELD_IS_PRIVATE, Types.TYPE_BOOLEAN));
		DEF.addField(new FieldDefinition(FIELD_USER, Types.TYPE_REFERENCE, User.ENTITY_NAME));
		DEF.addField(new FieldDefinition(FIELD_CREATE_DATE, Types.TYPE_DATE));
	}
}

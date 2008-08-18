package com.pagesociety.sql.test;

import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.Types;

public class User
{
	public static String ENTITY_NAME = "User";
	public static String FIELD_USER_NAME = "userName";
	public static String FIELD_FIRST_NAME = "firstName";
	public static String FIELD_LAST_NAME = "lastName";
	public static String FIELD_EMAIL = "email";
	public static String FIELD_PASSWORD = "password";
	public static String FIELD_ROLES = "roles";
	public static String FIELD_BOOKMARKS = "bookmarks";
	public static String FIELD_BOOKMARK_CATEGORIES = "bookmarkCategories";
	public static String FIELD_PROFILE_IMAGES = "profileImages";
	public static String FIELD_SELECTED_PROFILE_IMAGE = "selectedProfileImage";
	// definition
	public static EntityDefinition DEF;
	static
	{
		DEF = new EntityDefinition(ENTITY_NAME);
		DEF.addField(new FieldDefinition(FIELD_USER_NAME, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_FIRST_NAME, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_LAST_NAME, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_LAST_NAME, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_EMAIL, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_PASSWORD, Types.TYPE_STRING));
		DEF.addField(new FieldDefinition(FIELD_PASSWORD, Types.TYPE_STRING | Types.TYPE_ARRAY));
		DEF.addField(new FieldDefinition(FIELD_BOOKMARKS, Types.TYPE_REFERENCE | Types.TYPE_ARRAY, Bookmark.ENTITY_NAME));
		DEF.addField(new FieldDefinition(FIELD_PROFILE_IMAGES, Types.TYPE_STRING | Types.TYPE_ARRAY));
		DEF.addField(new FieldDefinition(FIELD_SELECTED_PROFILE_IMAGE, Types.TYPE_INT));
	}
}

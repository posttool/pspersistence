package com.pagesociety.sql.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.pagesociety.persistence.Entity;
import com.pagesociety.sql.SqlStore;

public class Test
{
	public static void main(String[] args) throws Exception
	{
		HashMap<Object, Object> config = new HashMap<Object, Object>();
		config.put(SqlStore.JDBC_CONNECTION_URL_KEY, "jdbc:mysql://localhost/");
		config.put(SqlStore.JDBC_CLASS_KEY, "com.mysql.jdbc.Driver");
		config.put(SqlStore.SQL_USER_KEY, "root");
		config.put(SqlStore.SQL_PASSWORD_KEY, "hello");
		//
		SqlStore s = new SqlStore();
		s.init(config);
		//
		try
		{
			s.useDatabase("test02");
			s.addEntityDefinition(User.DEF);
			s.addEntityDefinition(Bookmark.DEF);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//
		Entity bkmk = Bookmark.DEF.createInstance();
		bkmk.setId(1);
		bkmk.setAttribute(Bookmark.FIELD_NOTES, "Notes about this");
		bkmk.setAttribute(Bookmark.FIELD_IMG_URL, "http://dadada.com/img.jpg");
		s.saveEntity(bkmk);
		List<Entity> bkmks = new ArrayList<Entity>();
		bkmks.add(bkmk);
		//
		Entity user = User.DEF.createInstance();
		user.setId(1);
		user.setAttribute(User.FIELD_FIRST_NAME, "Topher");
		user.setAttribute(User.FIELD_LAST_NAME, "LaFata");
		List<String> images = new ArrayList<String>();
		for (int i = 0; i < 10; i++)
			images.add("image" + i + ".jpg");
		user.setAttribute(User.FIELD_PROFILE_IMAGES, images);
		user.setAttribute(User.FIELD_BOOKMARKS, bkmks);
		s.saveEntity(user);
		//
		System.out.println(user.getId());
	}
}

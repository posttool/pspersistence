package com.pagesociety.bdb;

import java.util.List;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.PersistenceException;

public interface BDBEntityDefinitionProvider
{
	public EntityDefinition provideEntityDefinition(Entity e) ;
	public EntityDefinition provideEntityDefinition(String type) ;
	public List<EntityDefinition> provideEntityDefinitions() ;
}

package com.google.code.twig;

import com.google.code.twig.annotation.AnnotationConfiguration;
import com.google.code.twig.annotation.Entity;
import com.google.code.twig.configuration.Configuration;
import com.google.code.twig.configuration.DefaultConfiguration;
import com.google.code.twig.standard.StandardObjectDatastore;

public class ObjectDatastoreFactory
{
	private static ObjectDatastoreFactory instance = new ObjectDatastoreFactory();
	
	public static ObjectDatastore createObjectDatastore()
	{
		return createObjectDatastore(Settings.builder().build());
	}
	
	public static ObjectDatastore createObjectDatastore(Settings settings)
	{
		return getInstance().create(getSettings(), getConfiguration());
	}

	static Settings getSettings()
	{
		return Settings.defaults();
	}

	protected static Configuration getConfiguration()
	{
		return new AnnotationConfiguration();
	}

	public static void register(Class<?> model)
	{
		Entity annotation = model.getAnnotation(Entity.class);
		String kind;
		if (annotation == null)
		{
			kind = model.getSimpleName();
		}
		else
		{
			kind = annotation.kind();
		}
		DefaultConfiguration.registerTypeName(model, kind);
	}
	
	protected ObjectDatastore create(Settings settings, Configuration configuration)
	{
		return new StandardObjectDatastore(settings, configuration, 0, false);
	}
	
	public static final ObjectDatastoreFactory getInstance()
	{
		return instance;
	}

	public static void setInstance(ObjectDatastoreFactory objectDatastoreFactory)
	{
		instance = objectDatastoreFactory;
	}

	static void unregisterAll()
	{
		DefaultConfiguration.unregisterAll();
	}
}

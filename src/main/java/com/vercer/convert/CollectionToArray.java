package com.vercer.convert;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;

import com.google.code.twig.util.generic.Generics;

public class CollectionToArray extends BaseTypeConverter
{
	private final TypeConverter delegate;

	public CollectionToArray(TypeConverter delegate)
	{
		this.delegate = delegate;
	}
	
	@Override
	public <T> T convert(Object instance, Type source, Type target) throws CouldNotConvertException
	{
		assert Generics.erase(source).isAssignableFrom(instance.getClass());
		
		Type sourceElementType;
		Type targetElementType;
		
		Class<?> sourceClass = Generics.erase(source);
		Class<?> targetClass = Generics.erase(target);
		
		if (Collection.class.isAssignableFrom(sourceClass))
		{
			sourceElementType = Generics.getTypeParameter(source, Iterable.class.getTypeParameters()[0]);
		}
		else
		{
			return null;
		}
		
		if (targetClass.isArray())
		{
			targetElementType = Generics.getArrayComponentType(target);
		}
		else
		{
			return null;
		}
		
		Collection<?> collection = (Collection<?>) instance;
		
		Object result = Array.newInstance(Generics.erase(targetElementType), collection.size());
		
		Iterator<?> iterator = collection.iterator();
		for (int i = 0; i < collection.size(); i++)
		{
			Object next = iterator.next();
			next = delegate.convert(next, sourceElementType, targetElementType);
			Array.set(result, i, next);
		}
		
		@SuppressWarnings("unchecked")
		T cast = (T) result;
		return cast;
	}
}

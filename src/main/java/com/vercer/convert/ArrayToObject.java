package com.vercer.convert;

import java.lang.reflect.Type;

public class ArrayToObject extends BaseTypeConverter
{
	private TypeConverter converter;

	public ArrayToObject(TypeConverter converter)
	{
		this.converter = converter;
	}
	
	@Override
	public <T> T convert(Object instance, Type source, Type target) throws CouldNotConvertException
	{
		Object single = ((Object[]) instance)[0];
		return converter.convert(single, source, target);
	}
}

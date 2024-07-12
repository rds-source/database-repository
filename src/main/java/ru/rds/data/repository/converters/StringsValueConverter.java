package ru.rds.data.repository.converters;

import ru.rds.data.repository.ValueConverter;

/**
 * Реализация {@link ValueConverter} для преобразования данных типа {@link String} в {@link String} и наоборот
 *
 * @author RDS
 * @version 1
 * @see ValueConverter
 * @since 1.0.0
 */
public class StringsValueConverter implements ValueConverter<String, String> {

	@Override
	public String convertFrom(String source) {
		return source;
	}

	@Override
	public String convertTo(String object) {
		return object;
	}

}

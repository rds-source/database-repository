package ru.rds.data.repository.converters;

import ru.rds.data.repository.ValueConverter;

/**
 * Реализация {@link ValueConverter} для преобразования данных типа {@link Integer} в {@link Boolean} и наоборот
 *
 * @author RDS
 * @version 1
 * @see ValueConverter
 * @since 1.0.0
 */
public class IntegerBooleanValueConverter implements ValueConverter<Integer, Boolean> {

	@Override
	public Boolean convertFrom(Integer source) {
		return source == 1;
	}

	@Override
	public Integer convertTo(Boolean object) {
		return Boolean.TRUE.equals(object) ? 1 : 0;
	}

}

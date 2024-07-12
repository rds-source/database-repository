package ru.rds.data.repository.converters;

import ru.rds.data.repository.ValueConverter;

/**
 * Реализация {@link ValueConverter} для преобразования данных типа {@link Integer} в {@link Integer} и наоборот
 *
 * @author RDS
 * @version 1
 * @see ValueConverter
 * @since 1.0.0
 */
public class IntegersValueConverter implements ValueConverter<Integer, Integer> {

	@Override
	public Integer convertFrom(Integer source) {
		return source;
	}

	@Override
	public Integer convertTo(Integer object) {
		return object;
	}

}

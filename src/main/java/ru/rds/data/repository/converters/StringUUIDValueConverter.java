package ru.rds.data.repository.converters;

import java.util.UUID;
import ru.rds.data.repository.ValueConverter;

/**
 * Реализация {@link ValueConverter} для преобразования данных типа {@link String} в {@link UUID} и наоборот
 *
 * @author RDS
 * @version 1
 * @see ValueConverter
 * @since 1.0.0
 */
public class StringUUIDValueConverter implements ValueConverter<String, UUID> {

	@Override
	public UUID convertFrom(String source) {
		return source == null ? null : UUID.fromString(source);
	}

	@Override
	public String convertTo(UUID object) {
		return object == null ? null : object.toString();
	}

}

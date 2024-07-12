package ru.rds.data.repository.converters;

import java.util.Date;
import ru.rds.data.repository.ValueConverter;

/**
 * Реализация {@link ValueConverter} для преобразования данных типа {@link String} в {@link Date} и наоборот
 *
 * @author RDS
 * @version 1
 * @see ValueConverter
 * @since 1.0.0
 */
public class StringDateValueConverter implements ValueConverter<String, Date> {

	@Override
	public Date convertFrom(String source) {
		if (source != null) {
			try {
				return new Date(Long.parseLong(source));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String convertTo(Date object) {
		if (object != null) {
			return Long.toString(object.getTime());
		}
		return Long.toString(0);
	}

}

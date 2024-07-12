package ru.rds.data.repository;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Общие механизмы для реализаций {@link RowConverter}
 *
 * @param <OBJECT>
 * @author RDS
 * @version 1
 * @see RowConverter
 * @since 1.0.0
 */
public abstract class AbstractRowConverter<OBJECT> implements RowConverter<OBJECT> {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRowConverter.class);

	private ValueConvertersProvider provider;

	public AbstractRowConverter(ValueConvertersProvider provider) {
		this.provider = provider;
	}


	protected <S extends Object, T> Optional<T> convertRowValue(Optional<S> value, Class<T> targetType) {
		return convertRowValue(value.orElseGet(() -> null), targetType);
	}

	protected <S extends Object, T> Optional<T> convertRowValue(S value, Class<T> targetType) {
		if (value != null && targetType != null) {
			if (provider != null) {
				// Поиск необходимого конвертера
				Class sourceType = value.getClass();
				Optional<ValueConverter<S, T>> optional = provider.getValueConverter(sourceType, targetType);
				if (optional.isPresent()) {
					ValueConverter<S, T> valueConverter = optional.get();
					return Optional.ofNullable(valueConverter.convertFrom(value));
				} else {
					logger.error(String.format("Не удалось преобразовать тип <%s> в <%s>, т.к. не найден подходящий <RowValueConverter>", sourceType.getName(), targetType.getName()));
				}
			} else {
				logger.error(String.format("Не удалось преобразовать тип <%s> в <%s>, т.к. не задан <RowValueConvertersProvider>", value.getClass().getName(), targetType.getName()));
			}
		}
		return Optional.empty();
	}

	protected <S, T> Optional<T> convertRowValue(S value, Class<S> sourceType, Class<T> targetType) {
		if (value != null && targetType != null) {
			if (provider != null) {
				Optional<ValueConverter<S, T>> optional = provider.getValueConverter(sourceType, targetType);
				if (optional.isPresent()) {
					ValueConverter<S, T> valueConverter = optional.get();
					return Optional.ofNullable(valueConverter.convertFrom(value));
				} else {
					logger.error(String.format("Не удалось преобразовать тип <%s> в <%s>, т.к. не найден подходящий <RowValueConverter>", sourceType.getName(), targetType.getName()));
				}
			} else {
				logger.error(String.format("Не удалось преобразовать тип <%s> в <%s>, т.к. не задан <RowValueConvertersProvider>", sourceType.getName(), targetType.getName()));
			}
		}
		return Optional.empty();
	}

}

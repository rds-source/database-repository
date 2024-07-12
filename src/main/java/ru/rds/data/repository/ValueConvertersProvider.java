package ru.rds.data.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rds.data.repository.converters.*;

/**
 * Провайдер конвертеров данных {@link ValueConverter}.
 * <p>
 * По-умолчанию содержит в себе начальный набор конвертеров данный, который можно дополнить своими конвертерами
 *
 * @author RDS
 * @version 1
 * @see ValueConverter
 * @since 1.0.0
 */
public class ValueConvertersProvider {

	private static final Logger logger = LoggerFactory.getLogger(AbstractRowConverter.class);

	private final Map<RowValueConverterDetails, ValueConverter> valueConverters = new HashMap<>();

	public ValueConvertersProvider() {
		registerValueConverter(new StringsValueConverter());
		registerValueConverter(new IntegersValueConverter());

		registerValueConverter(new StringUUIDValueConverter());
		registerValueConverter(new StringDateValueConverter());
		registerValueConverter(new IntegerBooleanValueConverter());
	}

	/**
	 * Добавление своего конвертера данных
	 *
	 * @param valueConverter
	 */
	protected void registerValueConverter(ValueConverter valueConverter) {
		if (valueConverter != null) {
			Type[] genericInterfaces = valueConverter.getClass().getGenericInterfaces();
			for (Type genericInterface : genericInterfaces) {
				if (genericInterface instanceof ParameterizedType) {
					Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
					if (genericTypes.length == 2) {
						Type typeFrom = genericTypes[0];
						Type typeTo = genericTypes[1];
						RowValueConverterDetails valueConverterDetails = new RowValueConverterDetails(typeFrom.getTypeName(), typeTo.getTypeName());
						logger.debug(String.format("Добавлен конвертер типов <%s> <-> <%s>", typeFrom, typeTo));
						valueConverters.putIfAbsent(valueConverterDetails, valueConverter);
					} else {
						logger.warn(String.format("Неправильное количество типов в конвертере типов <%s> - должно быть 2", valueConverter.getClass().getName()));
					}
				}
			}
		}
	}

	/**
	 * Получение имеющегося конвертера данных в соответствии с типами, если такой зарегистрирован в провайдере
	 * <p>
	 * Обратите внимание, что важное значение имеет прядок типов
	 *
	 * @param sourceType
	 * @param targetType
	 * @param <S>
	 * @param <T>
	 * @return
	 */
	public <S, T> Optional<ValueConverter<S, T>> getValueConverter(Class<S> sourceType, Class<T> targetType) {
		if (sourceType != null && targetType != null) {
			RowValueConverterDetails details = new RowValueConverterDetails(sourceType.getTypeName(), targetType.getTypeName());
			return Optional.ofNullable(valueConverters.get(details));
		}
		return Optional.empty();
	}

	private class RowValueConverterDetails {

		private String fromTypeName;
		private String toTypeName;

		public RowValueConverterDetails(String fromTypeName, String toTypeName) {
			this.fromTypeName = fromTypeName;
			this.toTypeName = toTypeName;
		}

		public String getFromTypeName() {
			return fromTypeName;
		}

		public void setFromTypeName(String fromTypeName) {
			this.fromTypeName = fromTypeName;
		}

		public String getToTypeName() {
			return toTypeName;
		}

		public void setToTypeName(String toTypeName) {
			this.toTypeName = toTypeName;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			RowValueConverterDetails that = (RowValueConverterDetails) o;
			return Objects.equals(fromTypeName, that.fromTypeName) && Objects.equals(toTypeName, that.toTypeName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(fromTypeName, toTypeName);
		}

	}

}

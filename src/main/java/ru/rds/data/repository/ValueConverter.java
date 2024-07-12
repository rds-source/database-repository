package ru.rds.data.repository;

/**
 * Преобразовать типов данных при работе с данными в БД.
 * <p>
 * Необходим, чтобы преобразовывать данные, понятные для Java, в данные, понятные для СУБД.
 * <p>
 * Позволяет удобно хранить в БД данные любого типа.
 *
 * @param <SOURCE_TYPE> тип данных, из которого нужно преобразовать (обычно это тип данных в СУБД)
 * @param <TARGET_TYPE> тип данных, в который нужно преобразовать (обычно это тип данных Java)
 * @author RDS
 * @version 1
 * @see ValueConverter
 * @since 1.0.0
 */
public interface ValueConverter<SOURCE_TYPE, TARGET_TYPE> {

	/**
	 * Преобразование типа {@link SOURCE_TYPE} в тип {@link TARGET_TYPE}
	 *
	 * @param source экземпляр объекта типа {@link SOURCE_TYPE}
	 * @return
	 */
	TARGET_TYPE convertFrom(SOURCE_TYPE source);

	/**
	 * Преобразование типа {@link TARGET_TYPE} в тип {@link SOURCE_TYPE}
	 *
	 * @param object экземпляр объекта типа {@link TARGET_TYPE}
	 * @return
	 */
	SOURCE_TYPE convertTo(TARGET_TYPE object);

}

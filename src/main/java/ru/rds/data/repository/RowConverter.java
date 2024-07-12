package ru.rds.data.repository;

import java.util.List;
import ru.rds.data.database.Column;
import ru.rds.data.database.Row;

/**
 * Преобразователь данных типа {@link Row} в целевой тип объекта и наоборот
 *
 * @param <T> тип объекта
 * @author RDS
 * @version 1
 * @see Row
 * @since 1.0.0
 */
public interface RowConverter<T> {

	/**
	 * Преобразование данных {@link Row} в тип {@link T}
	 *
	 * @param row
	 * @return
	 */
	T fromRow(Row row);

	/**
	 * Преобразование данных типа {@link T} в {@link Row}
	 *
	 * @param columns перечень столбцов, которые будут содержаться в {@link Row}
	 * @param entity  экземпляр объекта, который будет преобразован в {@link Row с учетом переданных столбцов
	 * @return
	 */
	Row toRow(List<Column> columns, T entity);

}

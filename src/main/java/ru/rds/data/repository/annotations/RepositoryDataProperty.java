package ru.rds.data.repository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ru.rds.data.repository.ValueConverter;

/**
 * Сопоставляет название поля соответствующему названию колонки в БД
 *
 * @author RDS
 * @version 1
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RepositoryDataProperty {

	/**
	 * Название колонки таблицы в БД
	 *
	 * @return
	 */
	String columnName() default "";

	/**
	 * Класс для конвертации данных
	 *
	 * @return
	 */
	Class<? extends ValueConverter>[] valueConverter() default {};

}

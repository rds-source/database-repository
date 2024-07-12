package ru.rds.data.repository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация, которая позволяет понять, что класс, отмеченный ею является отображением данных соответствующей таблицы в БД.
 *
 * @author RDS
 * @version 1
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RepositoryData {

	/**
	 * Имя репозитория (таблицы в БД)
	 *
	 * @return
	 */
	String repositoryName();

}

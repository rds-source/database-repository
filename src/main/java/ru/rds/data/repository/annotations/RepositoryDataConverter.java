package ru.rds.data.repository.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import ru.rds.data.repository.RowConverter;

/**
 * Класс, аннотированный данной аннотацией будет рассматриваться как конвертер типов данных БД при их сохранении/чтении
 *
 * @author RDS
 * @version 1
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface RepositoryDataConverter {

	Class<? extends RowConverter> value();

}

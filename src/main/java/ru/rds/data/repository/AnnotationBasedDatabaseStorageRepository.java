package ru.rds.data.repository;

import java.lang.reflect.Field;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rds.data.database.Column;
import ru.rds.data.database.DatabaseQueryBuilder;
import ru.rds.data.database.DatabaseStorage;
import ru.rds.data.database.Row;

/**
 * Расширенная реализация {@link DatabaseStorageRepository}, которая позволяет автоматизировать некоторые действий на основе аннотаций:
 * <lo>
 * <li>позволяет узнать название таблицы, если объект аннотирован {@link ru.rds.data.repository.annotations.RepositoryData}</li>
 * <li>позволяет найти конвертер данных, если он зарегистрирован в провайдере {@link ValueConvertersProvider}, либо если указан в {@link ru.rds.data.repository.annotations.RepositoryDataConverter} или {@link ru.rds.data.repository.annotations.RepositoryDataProperty}</li>
 * </lo>
 *
 * @param <T>
 * @param <ID>
 * @author RDS
 * @version 1
 * @since 1.0.0
 */
public abstract class AnnotationBasedDatabaseStorageRepository<T, ID> extends DatabaseStorageRepository<T, ID> {

	private static final Logger logger = LoggerFactory.getLogger(AnnotationBasedDatabaseStorageRepository.class);

	private RepositoryDataDetails<T> repositoryDataDetails;
	private ValueConvertersProvider  valueConvertersProvider;

	public AnnotationBasedDatabaseStorageRepository(DataSource dataSource, DatabaseQueryBuilder databaseQueryBuilder) {
		super(dataSource, databaseQueryBuilder);
	}

	public AnnotationBasedDatabaseStorageRepository(DatabaseStorage databaseStorage) {
		super(databaseStorage);
	}

	public AnnotationBasedDatabaseStorageRepository(DatabaseStorage databaseStorage, ValueConvertersProvider valueConvertersProvider) {
		super(databaseStorage);
		this.valueConvertersProvider = valueConvertersProvider;
	}

	@Override
	protected void initializeBefore() {
		this.repositoryDataDetails = new RepositoryDataDetails<>(getClass());
		if (this.repositoryDataDetails.getDataConverterInstance() == null) {
			if (this.valueConvertersProvider == null) {
				this.valueConvertersProvider = new ValueConvertersProvider();
			}
		}
	}

	@Override
	protected String getRepositoryName() {
		return this.repositoryDataDetails.getRepositoryName();
	}

	@Override
	protected RowConverter<T> getRowConverter() {
		return this.repositoryDataDetails.getDataConverterInstance();
	}

	@Override
	protected T mapFromRow(Row row) {
		if (getRowConverter() != null) {
			return super.mapFromRow(row);
		} else {
			return mapFromRowByPresentAnnotations(row);
		}
	}

	private T mapFromRowByPresentAnnotations(Row row) {
		T repositoryDataInstance = this.repositoryDataDetails.createDataInstance();
		if (repositoryDataInstance != null) {
			row.getSpaceProperties().forEach(column -> mapColumn(column, row.getValue(column.getName()), repositoryDataInstance));
		}
		return repositoryDataInstance;
	}

	private T mapColumn(Column column, Optional<Object> value, T object) {
		if (value.isPresent()) {
			try {
				String fieldName = this.repositoryDataDetails.getPropertyFieldName(column.getName());
				if (fieldName != null) {
					Class fieldType = this.repositoryDataDetails.getPropertyFieldType(column.getName());

					Optional<ValueConverter> rowValueConverter = this.valueConvertersProvider.getValueConverter(value.get().getClass(), fieldType);
					if (!rowValueConverter.isPresent()) {
						rowValueConverter = Optional.ofNullable(this.repositoryDataDetails.getPropertyValueConverterInstance(column.getName()));
					}

					if (rowValueConverter.isPresent()) {
						setFieldValue(object, fieldName, rowValueConverter.get(), value.get());
					} else {
						logger.error(String.format("Не удалось найти подходящий <RowValueConverter> для преобразования типа <%s> в тип <%s>", value.get().getClass().getName(), fieldType.getName()));
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return object;
	}

	private void setFieldValue(T object, String fieldName, ValueConverter valueConverter, Object value) {
		Class<?> classForFieldsSearch = object.getClass();
		while (!Object.class.equals(classForFieldsSearch)) {
			try {
				Field field = classForFieldsSearch.getDeclaredField(fieldName);
				boolean isAccessible = field.isAccessible();
				if (!isAccessible) {
					field.setAccessible(true);
				}
				field.set(object, valueConverter.convertFrom(value));
				if (!isAccessible) {
					field.setAccessible(false);
				}
				break;
			} catch (Exception e) {
				classForFieldsSearch = classForFieldsSearch.getSuperclass();
			}
		}
	}

	@Override
	protected Row mapToRow(T entity) {
		if (getRowConverter() != null) {
			return super.mapToRow(entity);
		} else {
			return mapToRowByPresentAnnotations(entity);
		}
	}

	private Row mapToRowByPresentAnnotations(T entity) {
		Row row = new Row(getTable().getColumns());
		row.getSpaceProperties()
		   .forEach(column -> mapField(row, column, entity));
		return row;
	}

	private void mapField(Row row, Column column, T entity) {
		try {
			String fieldName = this.repositoryDataDetails.getPropertyFieldName(column.getName());
			if (fieldName != null) {
				Class fieldType = this.repositoryDataDetails.getPropertyFieldType(column.getName());
				Class columnType = null;
				try {
					columnType = Class.forName(column.getClassName());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}

				if (columnType != null) {
					Optional<ValueConverter> rowValueConverter = this.valueConvertersProvider.getValueConverter(columnType, fieldType);
					if (!rowValueConverter.isPresent()) {
						rowValueConverter = Optional.ofNullable(this.repositoryDataDetails.getPropertyValueConverterInstance(column.getName()));
					}

					if (rowValueConverter.isPresent()) {
						Object filedValue = getFieldValue(entity, fieldName, rowValueConverter.get());
						row.setValue(column.getName(), filedValue);
					} else {
						logger.error(String.format("Не удалось найти подходящий <RowValueConverter> для преобразования типа <%s> в тип <%s>", columnType.getName(), fieldType.getName()));
					}
				} else {
					logger.error(String.format("Не удалось определить Java-тип <%s> для сохранения данных в Столбце <%s>", column.getClassName(), column.getName()));
				}
				//row.setValue(column.getName(), getFieldValue(entity, fieldName));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Object getFieldValue(T object, String fieldName, ValueConverter valueConverter) {
		Class<?> classForFieldsSearch = object.getClass();
		Object fieldValue = null;
		while (!Object.class.equals(classForFieldsSearch)) {
			try {
				Field field = classForFieldsSearch.getDeclaredField(fieldName);
				boolean isAccessible = field.isAccessible();
				if (!isAccessible) {
					field.setAccessible(true);
				}
				if (valueConverter != null) {
					fieldValue = valueConverter.convertTo(field.get(object));
				} else {
					fieldValue = field.get(object);
				}
				if (!isAccessible) {
					field.setAccessible(false);
				}
				break;
			} catch (Exception e) {
				classForFieldsSearch = classForFieldsSearch.getSuperclass();
			}
		}
		return fieldValue;
	}

}

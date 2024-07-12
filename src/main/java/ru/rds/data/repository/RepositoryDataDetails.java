package ru.rds.data.repository;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rds.data.repository.annotations.RepositoryData;
import ru.rds.data.repository.annotations.RepositoryDataConverter;
import ru.rds.data.repository.annotations.RepositoryDataProperty;

/**
 * Класс-контейнер для хранения всё необходимой информации, которая нужна для класса {@link AnnotationBasedDatabaseStorageRepository}
 * <p>Выполняет действия по поиску всех необходимых данных самостоятельно при создании экземпляра и передачи в конструктор класса Репозитория
 *
 * @param <T>
 * @author RDS
 * @version 1
 * @since 1.0.0
 */
public class RepositoryDataDetails<T> {

	private static final Logger logger = LoggerFactory.getLogger(RepositoryDataDetails.class);

	// Класс объекта, с которым работает Репозиторий
	private Class<?> dataClass;

	// Название Репозитория (оно же название таблицы в БД)
	private String repositoryName;

	// Класс конвертера данных объекта
	private Class<? extends RowConverter> dataConverterClass;

	// Экземпляр класса конвертера данных объекта
	private RowConverter<T> dataConverterInstance;

	// Перечень имен полей и соответствущих имен столбцов в таблице БД
	private Map<String, String> dataPropertiesFieldsMap = new HashMap<>();

	// Перечень имен полей и соответствующих типов
	private Map<String, Class<?>> dataPropertiesTypesMap = new HashMap<>();

	// Перечень имен полей и соответствующи классов конвертеров типов
	private Map<String, Class<? extends ValueConverter>> dataPropertiesConvertersMap = new HashMap<>();

	// Перечень имен полей и соответствующих экземпляров конвертеров типов
	private Map<String, ValueConverter> dataPropertiesConvertersInstancesMap = new HashMap<>();

	public RepositoryDataDetails(Class repositoryClass) {
		Type superClass = repositoryClass.getGenericSuperclass();
		if (superClass instanceof ParameterizedType) {
			Type[] genericTypes = ((ParameterizedType) superClass).getActualTypeArguments();
			Type dataType = genericTypes[0];
			try {
				this.dataClass = Class.forName(dataType.getTypeName());
				logger.debug(String.format("Для Хранилища <%s> тип данных идентифицирован как <%s>", repositoryClass.getName(), this.dataClass.getName()));
			} catch (Exception e) {
				logger.error(String.format("Для Хранилища <%s> не удалось загрузить сведения о классе типа данных <%s>: %s", repositoryClass.getName(), dataType.getTypeName(), e.getMessage()), e);
			}
		} else {
			logger.warn(String.format("Не удалось идентифицировать тип данных для Хранилища <%s>", repositoryClass.getName()));
		}

		if (this.dataClass != null) {
			if (this.dataClass.isAnnotationPresent(RepositoryData.class)) {
				RepositoryData repositoryData = this.dataClass.getAnnotation(RepositoryData.class);
				this.repositoryName = repositoryData.repositoryName();
				logger.debug(String.format("Название Хранилища <%s> определено как <%s>", repositoryClass.getName(), this.repositoryName));
			} else {
				logger.error(String.format("Не удалось определить название Хранилища <%s>, т.к. тип данных <%s> не аннотирован аннотацией <RepositoryData>", repositoryClass.getName(), this.dataClass.getName()));
			}


			if (this.dataClass.isAnnotationPresent(RepositoryDataConverter.class)) {
				RepositoryDataConverter repositoryDataConverter = this.dataClass.getAnnotation(RepositoryDataConverter.class);
				dataConverterClass = repositoryDataConverter.value();
				logger.debug(String.format("Тип Конвертера данных для Хранилища <%s> определен как <%s>", repositoryClass.getName(), this.dataConverterClass.getName()));
				try {
					Constructor<? extends RowConverter> constructor = this.dataConverterClass.getDeclaredConstructor(ValueConvertersProvider.class);
					this.dataConverterInstance = constructor.newInstance(new ValueConvertersProvider());
				} catch (Exception e) {
					logger.error(String.format("Не удалось создать экземпляр Конвертера данных типа <%s> для Хранилища <%s>", this.dataConverterClass.getName(), repositoryClass.getName()), e);
				}
			} else {
				logger.warn(
						String.format("Не удалось определить тип Конвертера данных Хранилища <%s>, т.к. тип данных <%s> не аннотирован аннотацией <DataConverter>", repositoryClass.getName(), this.dataClass.getName()));
			}

			Class<?> classForFieldsSearch = this.dataClass;
			while (!Object.class.equals(classForFieldsSearch)) {
				for (Field field : classForFieldsSearch.getDeclaredFields()) {
					if (field.isAnnotationPresent(RepositoryDataProperty.class)) {
						RepositoryDataProperty repositoryDataProperty = field.getAnnotation(RepositoryDataProperty.class);
						String propertyName = repositoryDataProperty.columnName();
						if (propertyName != null && !propertyName.isEmpty()) {
							this.dataPropertiesFieldsMap.putIfAbsent(propertyName, field.getName());
							try {
								this.dataPropertiesTypesMap.putIfAbsent(propertyName, Class.forName(field.getGenericType().getTypeName()));
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
						Class<? extends ValueConverter>[] valueConverters = repositoryDataProperty.valueConverter();
						if (valueConverters.length > 0) {
							this.dataPropertiesConvertersMap.putIfAbsent(propertyName, valueConverters[0]);
							try {
								this.dataPropertiesConvertersInstancesMap.putIfAbsent(propertyName, valueConverters[0].getConstructor().newInstance());
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
					}
				}

				classForFieldsSearch = classForFieldsSearch.getSuperclass();
			}
		}
	}

	public Class<?> getDataClass() {
		return dataClass;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public Class<? extends RowConverter> getDataConverterClass() {
		return dataConverterClass;
	}

	public RowConverter<T> getDataConverterInstance() {
		return dataConverterInstance;
	}

	public T createDataInstance() {
		if (getDataClass() != null) {
			try {
				return (T) getDataClass().getConstructor().newInstance();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return null;
	}

	public List<String> getPropertyColumnNames() {
		return new ArrayList<>(this.dataPropertiesFieldsMap.keySet());
	}

	public String getPropertyFieldName(String propertyColumnName) {
		return this.dataPropertiesFieldsMap.get(propertyColumnName);
	}

	public Class<?> getPropertyFieldType(String propertyColumnName) {
		return this.dataPropertiesTypesMap.get(propertyColumnName);
	}

	public Class<? extends ValueConverter> getPropertyValueConverterClass(String propertyColumnName) {
		return this.dataPropertiesConvertersMap.get(propertyColumnName);
	}

	public ValueConverter getPropertyValueConverterInstance(String propertyColumnName) {
		return this.dataPropertiesConvertersInstancesMap.get(propertyColumnName);
	}

}

# Database Repository

Развитие проекта [Database Storage](https://github.com/rds-source/database-storage), реализующего концепцию [Storage API](https://github.com/rds-source/storage-api).

Создан для упрощения и более удобной работы с БД для тех, кто привык работать с аналогичными механизмами от проектов, реализующих концепцию _"Repository"_.

### Основы

* [DatabaseStorageRepository](src/main/java/ru/rds/data/repository/DatabaseStorageRepository.java) - расширенная
  реализация [DatabaseStorage](https://github.com/rds-source/database-storage/blob/master/src/main/java/ru/rds/data/database/DatabaseStorage.java) - реализует основные механизмы CRUD при работе с объектами;
* [AnnotationBasedDatabaseStorageRepository](src/main/java/ru/rds/data/repository/AnnotationBasedDatabaseStorageRepository.java) - расширенная
  реализация [DatabaseStorageRepository](src/main/java/ru/rds/data/repository/DatabaseStorageRepository.java) - позволяет автоматизировать некоторые механизмы за счет использования соответствующих аннотаций;

### Аннотации

Перечисленные ниже аннотации актуальны только при использовании [AnnotationBasedDatabaseStorageRepository](src/main/java/ru/rds/data/repository/AnnotationBasedDatabaseStorageRepository.java)

* [RepositoryData](src/main/java/ru/rds/data/repository/annotations/RepositoryData.java) - данную аннотацию следовать добавлять на уровне класса, чтобы указать соответствующую таблицу в БД, которая также будет использована в
  качестве имени Репозитория
```java
@RepositoryData(repositoryName = "TASKS")
public class TaskEntity {
	
}
```
* [RepositoryDataProperty](src/main/java/ru/rds/data/repository/annotations/RepositoryDataProperty.java) - данную аннотацию следовать добавлять к полям класса, чтобы указать, что данное поле соответствует определенному столбцу в таблице БД
```java
@RepositoryData(repositoryName = "TASKS")
public class TaskEntity {

	@RepositoryDataProperty(columnName = "ID")
	private UUID id;

	@RepositoryDataProperty(columnName = "NAME")
	private String name;

	@RepositoryDataProperty(columnName = "VERSION")
	private Integer version;

}
```
* [RepositoryDataConverter](src/main/java/ru/rds/data/repository/annotations/RepositoryDataConverter.java) - данную аннотацию следовать добавлять на уровне класса, чтобы указать конвертер данных, которые нужно будет использовать для преобразования строк таблицы в БД
```java
@RepositoryData(repositoryName = "TASKS")
@RepositoryDataConverter(TaskRowConverter.class)
public class TaskEntity {
	
}
```

### Пример использования
* [DatabaseStorageRepository](src/main/java/ru/rds/data/repository/DatabaseStorageRepository.java)

```java
public class TasksDatabaseStorageRepository extends DatabaseStorageRepository<TaskEntity, UUID> {

	private TaskRowConverter rowConverter;

	public TasksDatabaseStorageRepository(DataSource dataSource, DatabaseQueryBuilder databaseQueryBuilder, ValueConvertersProvider provider) {
		super(dataSource, databaseQueryBuilder);
		init(provider);
	}

	public TasksDatabaseStorageRepository(DatabaseStorage databaseStorage, ValueConvertersProvider provider) {
		super(databaseStorage);
		init(provider);
	}

	public TasksDatabaseStorageRepository(Table table, ValueConvertersProvider provider) {
		super(table);
		init(provider);
	}

	private void init(ValueConvertersProvider provider) {
		this.rowConverter = new TaskRowConverter(provider);
	}

	@Override
	protected String getRepositoryName() {
		return "TASKS";
	}

	@Override
	protected RowConverter<TaskEntity> getRowConverter() {
		return this.rowConverter;
	}

	@Override
	protected String getIdValue(UUID uuid, String idColumnName) {
		if (uuid != null) {
			return uuid.toString();
		}
		return null;
	}

}
```
```java
HikariDataSource dataSource = new HikariDataSource();
dataSource.setDriverClassName("org.h2.Driver");
dataSource.setJdbcUrl("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;INIT=RUNSCRIPT FROM 'classpath:h2init.sql'");
dataSource.setUsername("sa");
dataSource.setPassword("sa");

DatabaseQueryBuilder databaseQueryBuilder = new H2DatabaseQueryBuilder();

DatabaseStorage databaseStorage = new DatabaseStorage(dataSource, databaseQueryBuilder);

ValueConvertersProvider valueConvertersProvider = new ValueConvertersProvider();

TasksDatabaseStorageRepository databaseStorageRepository = new TasksDatabaseStorageRepository(databaseStorage, valueConvertersProvider);
```
```java
TaskEntity taskEntity = new TaskEntity();
taskEntity.setId(UUID.randomUUID());
taskEntity.setName("Task 1");
taskEntity.setVersion(1);

databaseStorageRepository.insert(taskEntity);
```

* [AnnotationBasedDatabaseStorageRepository](src/main/java/ru/rds/data/repository/AnnotationBasedDatabaseStorageRepository.java)
```java
@RepositoryData(repositoryName = "TASKS")
@RepositoryDataConverter(TaskRowConverter.class)
public class TaskEntity {

	@RepositoryDataProperty(columnName = "ID")
	private UUID id;

	@RepositoryDataProperty(columnName = "NAME")
	private String name;

	@RepositoryDataProperty(columnName = "VERSION")
	private Integer version;

	public TaskEntity() {
	}

	public TaskEntity(UUID id, String name, Integer version) {
		this.id = id;
		this.name = name;
		this.version = version;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
}

public class TaskRowConverter extends AbstractRowConverter<TaskEntity> {

	public TaskRowConverter(ValueConvertersProvider provider) {
		super(provider);
	}

	@Override
	public TaskEntity fromRow(Row row) {
		Optional<UUID> id = convertRowValue(row.getValue("ID"), UUID.class);
		Optional<String> name = convertRowValue(row.getValue("NAME"), String.class);
		Optional<Integer> version = convertRowValue(row.getValue("VERSION"), Integer.class);

		TaskEntity entity = new TaskEntity();
		entity.setId(id.orElseGet(() -> null));
		entity.setName(name.orElseGet(() -> null));
		entity.setVersion(version.orElseGet(() -> null));

		return entity;
	}

	@Override
	public Row toRow(List<Column> columns, TaskEntity entity) {
		Row row = new Row(columns);

		columns.forEach(column -> {
			row.setValue(column.getName(), mapEntityValue(entity, column.getName()));
		});

		return row;
	}

	private Object mapEntityValue(TaskEntity entity, String columnName) {
		switch (columnName) {
			case "ID":
				return entity.getId();
			case "NAME":
				return entity.getName();
			case "VERSION":
				return entity.getVersion();
			default:
				return null;
		}
	}

}

public class TasksRepository extends AnnotationBasedDatabaseStorageRepository<TaskEntity, UUID> {

	public TasksRepository(DatabaseStorage databaseStorage, ValueConvertersProvider valueConvertersProvider) {
		super(databaseStorage, valueConvertersProvider);
	}

	@Override
	protected String getIdValue(UUID uuid, String idColumnName) {
		if (uuid != null) {
			return uuid.toString();
		}
		return null;
	}

}
```
```java
HikariDataSource dataSource = new HikariDataSource();
dataSource.setDriverClassName("org.h2.Driver");
dataSource.setJdbcUrl("jdbc:h2:mem:test;DATABASE_TO_UPPER=false;INIT=RUNSCRIPT FROM 'classpath:h2init.sql'");
dataSource.setUsername("sa");
dataSource.setPassword("sa");

DatabaseQueryBuilder databaseQueryBuilder = new H2DatabaseQueryBuilder();

DatabaseStorage databaseStorage = new DatabaseStorage(dataSource, databaseQueryBuilder);

ValueConvertersProvider valueConvertersProvider = new ValueConvertersProvider();

TasksRepository repository = new TasksRepository(databaseStorage, valueConvertersProvider);
```


### Требования

- [JDK 8](https://openjdk.org/projects/jdk/8/)

### Зависимости

- [Storage API](https://github.com/rds-source/storage-api)
- [Database API](https://github.com/rds-source/database-storage)
- [SLF4J API](https://github.com/qos-ch/slf4j)
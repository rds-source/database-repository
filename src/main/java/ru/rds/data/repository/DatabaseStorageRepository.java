package ru.rds.data.repository;

import java.util.*;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.rds.data.database.*;
import ru.rds.data.database.common.AssertChecker;
import ru.rds.data.storage.*;

/**
 * Общие механизмы для реализаций {@link Repository}
 *
 * @param <T>  тип объекта
 * @param <ID> тип идентификатора объекта
 * @author RDS
 * @version 1
 * @see Repository
 * @since 1.0.0
 */
public abstract class DatabaseStorageRepository<T, ID> implements Repository<T, ID> {

	private static final Logger logger = LoggerFactory.getLogger(DatabaseStorageRepository.class);

	// Объект Таблицы, который будет соответствовать Репозиторий
	private Table table;

	public DatabaseStorageRepository(DataSource dataSource, DatabaseQueryBuilder databaseQueryBuilder) {
		initializeBefore();
		DatabaseStorage database = new DatabaseStorage(dataSource, databaseQueryBuilder);
		initialize(database.getSpace(getRepositoryName()).orElseGet(() -> null));
	}

	public DatabaseStorageRepository(DatabaseStorage databaseStorage) {
		initializeBefore();
		initialize(databaseStorage.getSpace(getRepositoryName()).orElseGet(() -> null));
	}

	public DatabaseStorageRepository(Table table) {
		initializeBefore();
		initialize(table);
	}

	private void initialize(Table table) {
		this.table = table;
		AssertChecker.notNull(table, String.format("Не удалось создать объект <Table> для Хранилища с именем <%s>", getRepositoryName()));
	}

	/**
	 * Вызывается ДО создания соответствующего объекта Таблицы
	 */
	protected void initializeBefore() {

	}

	/**
	 * Возвращает соответствующее название репозитория (названия таблицы в БД)
	 *
	 * @return
	 */
	protected abstract String getRepositoryName();

	/**
	 * Возвращает тип конвертера, который нужно будет использовать при конвертировании строк
	 *
	 * @return
	 */
	protected abstract RowConverter<T> getRowConverter();

	/**
	 * Возвращает значение идентификатора по названию соответствующего столбца в таблице БД
	 *
	 * @param id
	 * @param idColumnName
	 * @return
	 */
	protected abstract Object getIdValue(ID id, String idColumnName);

	/**
	 * Объект {@link Table}, соответствующий таблице Репозитория в БД.
	 *
	 * @return
	 */
	public Table getTable() {
		return table;
	}

	@Override
	public Optional<T> findById(ID id) {
		// Формирование критериев отбора для поиска данных по идентификатору
		List<ElementsSelectionCondition> selectionConditions = table.getIdColumns()
		                                                            .stream()
		                                                            .map(column -> {
			                                                            ElementsSelectionCondition selectionCondition = new ElementsSelectionCondition();
			                                                            selectionCondition.setSpacePropertyName(column.getName());
			                                                            selectionCondition.setSpacePropertyValue(getIdValue(id, column.getName()));
			                                                            selectionCondition.setSelectionConditionExpression(SelectionConditionExpression.EQUAL);
			                                                            return selectionCondition;
		                                                            }).collect(Collectors.toList());

		Optional<Row> row = table.getElements(selectionConditions, SelectionType.AND).stream().findFirst();
		return row.map(this::mapFromRow);
	}

	@Override
	public List<T> findAll() {
		return findAll(null, null, null);
	}

	@Override
	public List<T> findAll(List<ElementsSort> elementsSorts) {
		return findAll(elementsSorts, null, null);
	}

	@Override
	public List<T> findAll(List<ElementsSelectionCondition> selectionConditions, SelectionType selectionType) {
		return findAll(null, selectionConditions, selectionType);
	}

	@Override
	public List<T> findAll(List<ElementsSort> elementsSorts, List<ElementsSelectionCondition> selectionConditions, SelectionType selectionType) {
		List<Row> rows = this.table.getElements(elementsSorts, selectionConditions, selectionType);
		return mapFromRows(rows);
	}

	@Override
	public Section<T> findAll(Sectionable sectionable) {
		TableSection tableSection = this.table.getSection(sectionable);
		RepositorySection<T> repositorySection = RepositorySection.of(sectionable, tableSection.getTotalElementsCount(), mapFromRows(tableSection.getElements()));
		return repositorySection;
	}

	@Override
	public long count() {
		return count(null, null);
	}

	@Override
	public long count(List<ElementsSelectionCondition> selectionConditions, SelectionType selectionType) {
		return this.table.getElementsCount(selectionConditions, selectionType);
	}

	@Override
	public T insert(T entity) {
		AssertChecker.notNull(entity, "Переданная сущность не может быть сохранена в Репозитории <%>");
		Row row = mapToRow(entity);
		this.table.createElement(row);
		return entity;
	}

	@Override
	public T save(T entity) {
		Row row = mapToRow(entity);
		// Формирование условий, чтобы найти и сохранить данные для нужного объекта
		List<ElementsSelectionCondition> selectionConditions = table.getIdColumns()
		                                                            .stream()
		                                                            .map(column -> {
			                                                            Object idValue = row.getValue(column.getName()).orElseGet(() -> null);

			                                                            ElementsSelectionCondition selectionCondition = new ElementsSelectionCondition();
			                                                            selectionCondition.setSpacePropertyName(column.getName());
			                                                            selectionCondition.setSpacePropertyValue(idValue == null ? null : String.valueOf(idValue));
			                                                            selectionCondition.setSelectionConditionExpression(SelectionConditionExpression.EQUAL);
			                                                            return selectionCondition;
		                                                            }).collect(Collectors.toList());
		table.updateElements(row, selectionConditions);
		return entity;
	}

	@Override
	public void delete(T entity) {
		Row row = mapToRow(entity);
		// Формирование критериев, чтобы удалить только нужный объект
		List<ElementsSelectionCondition> selectionConditions = table.getIdColumns()
		                                                            .stream()
		                                                            .map(column -> {
			                                                            Object idValue = row.getValue(column.getName()).orElseGet(() -> null);

			                                                            ElementsSelectionCondition selectionCondition = new ElementsSelectionCondition();
			                                                            selectionCondition.setSpacePropertyName(column.getName());
			                                                            selectionCondition.setSpacePropertyValue(idValue == null ? null : String.valueOf(idValue));
			                                                            selectionCondition.setSelectionConditionExpression(SelectionConditionExpression.EQUAL);
			                                                            return selectionCondition;
		                                                            }).collect(Collectors.toList());
		table.deleteElements(selectionConditions);
	}

	@Override
	public void deleteById(ID id) {
		List<ElementsSelectionCondition> selectionConditions = table.getIdColumns()
		                                                            .stream()
		                                                            .map(column -> {
			                                                            ElementsSelectionCondition selectionCondition = new ElementsSelectionCondition();
			                                                            selectionCondition.setSpacePropertyName(column.getName());
			                                                            selectionCondition.setSpacePropertyValue(getIdValue(id, column.getName()));
			                                                            selectionCondition.setSelectionConditionExpression(SelectionConditionExpression.EQUAL);
			                                                            return selectionCondition;
		                                                            }).collect(Collectors.toList());
		table.deleteElements(selectionConditions);
	}

	@Override
	public void deleteAll() {
		table.deleteElements(Collections.emptyList());
	}

	protected List<T> mapFromRows(List<Row> rows) {
		List<T> entities = rows.stream()
		                       .map(this::mapFromRow)
		                       .filter(Objects::nonNull)
		                       .collect(Collectors.toList());
		return entities;
	}

	protected T mapFromRow(Row row) {
		RowConverter<T> rowConverter = getRowConverter();
		if (rowConverter != null) {
			return rowConverter.fromRow(row);
		} else {
			logger.warn(String.format("Для Хранилища <%s> не предоставлен <RowConverter>", getRepositoryName()));
		}
		return null;
	}

	protected Row mapToRow(T entity) {
		RowConverter<T> rowConverter = getRowConverter();
		if (rowConverter != null) {
			return rowConverter.toRow(this.table.getColumns(), entity);
		} else {
			logger.warn(String.format("Для Хранилища <%s> не предоставлен <RowConverter>", getRepositoryName()));
		}
		return null;
	}

}

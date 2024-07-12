package ru.rds.data.repository;

import java.util.List;
import java.util.Optional;
import ru.rds.data.storage.*;

/**
 * Репозиторий, описывающий механизмы работы с его данными.
 * <p>
 * Под Репозиторием понимается какая-либо соответствующая таблица в СУБД
 *
 * @param <T>  тип объекта, хранимого в Репозитории (данный тип должен соответствовать структуре данных в соответствующей таблице в СУБД)
 * @param <ID> тип идентификатора объекта (будет сопоставлен с колонками в таблице, являющихся ключами)
 * @author RDS
 * @version 1
 * @since 1.0.0
 */
public interface Repository<T, ID> {

	/**
	 * Поиск объекта по идентификатору
	 *
	 * @param id
	 * @return
	 */
	Optional<T> findById(ID id);

	/**
	 * Поиск всех объектов
	 *
	 * @return никогда не возвращает NULL
	 */
	default List<T> findAll() {
		return findAll(null, null, null);
	}

	/**
	 * Поиск всех объектов
	 *
	 * @param elementsSorts перечень параметров сортировки
	 * @return никогда не возвращает NULL
	 */
	default List<T> findAll(List<ElementsSort> elementsSorts) {
		return findAll(elementsSorts, null, null);
	}

	/**
	 * Поиск всех объектов, удовлетворяющих критериям
	 *
	 * @param selectionConditions перечень критериев отбора
	 * @param selectionType       способ комбинации критериев поиска
	 * @return никогда не возвращает NULL
	 */
	default List<T> findAll(List<ElementsSelectionCondition> selectionConditions, SelectionType selectionType) {
		return findAll(null, selectionConditions, selectionType);
	}

	/**
	 * Поиск всех объектов, удовлетворяющих критериям
	 *
	 * @param elementsSorts       перечень параметров сортировки
	 * @param selectionConditions перечень критериев отбора
	 * @param selectionType       способ комбинации критериев отбора
	 * @return никогда не возвращает NULL
	 */
	List<T> findAll(List<ElementsSort> elementsSorts, List<ElementsSelectionCondition> selectionConditions, SelectionType selectionType);

	/**
	 * Поиск всех объектов в соответствии с критериями {@link Sectionable}
	 *
	 * @param sectionable критерии получения {@link Section}
	 * @return никогда не возвращает NULL
	 */
	Section<T> findAll(Sectionable sectionable);

	/**
	 * Общее количество объектов
	 *
	 * @return
	 */
	default long count() {
		return count(null, null);
	}

	/**
	 * Количество элементов в соответствии с заданными критериями отбора
	 *
	 * @param selectionConditions перечень критериев отбора
	 * @param selectionType       способ комбинации критериев отбора
	 * @return количество элементов
	 */
	long count(List<ElementsSelectionCondition> selectionConditions, SelectionType selectionType);

	/**
	 * Добавление нового объекта
	 *
	 * @param entity
	 * @return возвращает добавленный объект
	 */
	T insert(T entity);

	/**
	 * Сохранение (обновление) данных существующего объекта
	 *
	 * @param entity
	 * @return возвращает сохраненный объект
	 */
	T save(T entity);

	/**
	 * Удаление объекта
	 *
	 * @param entity
	 */
	void delete(T entity);

	/**
	 * Удаление объекта по его идентификатору
	 *
	 * @param id
	 */
	void deleteById(ID id);

	/**
	 * Удаление всех объектов
	 */
	void deleteAll();

}

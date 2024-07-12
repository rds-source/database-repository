package ru.rds.data.repository;

import java.util.List;
import ru.rds.data.storage.Sectionable;
import ru.rds.data.storage.StorageSection;

/**
 * Релизация {@link StorageSection} для использования в Репозитории
 *
 * @param <T>
 * @author RDS
 * @version 1
 * @see StorageSection
 * @since 1.0.0
 */
public class RepositorySection<T> extends StorageSection<T> {

	/**
	 * Создание секции на основе переданных условий
	 *
	 * @param sectionable    перечень целевых критериев для создания секции
	 * @param totalRowsCount общее количество строк в секции
	 * @param elements       перечень объектов секции
	 * @param <T>
	 * @return
	 */
	public static <T> RepositorySection<T> of(Sectionable sectionable, long totalRowsCount, List<T> elements) {
		RepositorySection<T> section = empty(sectionable);
		section.setTotalElementsCount(totalRowsCount);
		section.setElements(elements);
		return section;
	}

	/**
	 * Создание пустой секции
	 *
	 * @param sectionable перечень целевых критериев для создания секции
	 * @param <T>
	 * @return
	 */
	public static <T> RepositorySection<T> empty(Sectionable sectionable) {
		RepositorySection<T> section = new RepositorySection<T>();
		section.setSort(sectionable.getSort());
		section.setElementsSelectionConditions(sectionable.getElementsSelectionConditions());
		section.setSelectionType(sectionable.getSelectionType());
		section.setSectionNumber(sectionable.getSectionNumber());
		section.setSectionSize(sectionable.getSectionSize());
		return section;
	}

	/**
	 * Создание пустой секции
	 *
	 * @param <T>
	 * @return
	 */
	public static <T> RepositorySection<T> empty() {
		return new RepositorySection<T>();
	}

}

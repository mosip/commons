package io.mosip.kernel.masterdata.utils;

import java.util.Comparator;
import java.util.List;

public class MultiFieldComparator<T> implements Comparator<T> {
	private List<FieldComparator<T>> listComparators;

	public MultiFieldComparator(List<FieldComparator<T>> listComparators) {
        this.listComparators = listComparators;
    }

	@Override
	public int compare(T o1, T o2) {
		for (FieldComparator<T> comparator : listComparators) {
            int result = comparator.compare(o1, o2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
	}

}
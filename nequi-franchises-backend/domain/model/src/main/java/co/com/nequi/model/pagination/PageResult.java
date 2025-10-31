package co.com.nequi.model.pagination;

import java.util.List;
import java.util.Objects;

public final class PageResult<T> {

    private final List<T> items;
    private final String lastEvaluatedKey;

    private PageResult(List<T> items, String lastEvaluatedKey) {
        this.items = List.copyOf(items);
        this.lastEvaluatedKey = lastEvaluatedKey;
    }

    public static <T> PageResult<T> of(List<T> items, String lastEvaluatedKey) {
        Objects.requireNonNull(items, "items");
        return new PageResult<>(items, lastEvaluatedKey);
    }

    public List<T> getItems() {
        return items;
    }

    public String getLastEvaluatedKey() {
        return lastEvaluatedKey;
    }
}

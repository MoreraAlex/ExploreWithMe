package ru.practicum.ewm.common;

import java.io.Serial;
import org.springframework.data.domain.AbstractPageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class OffsetPageRequest extends AbstractPageRequest {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int offset;
    private final Sort sort;

    public OffsetPageRequest(int offset, int limit) {
        this(offset, limit, Sort.unsorted());
    }

    public OffsetPageRequest(int offset, int limit, Sort sort) {
        super(offset / limit, limit);
        this.offset = offset;
        this.sort = sort == null ? Sort.unsorted() : sort;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return new OffsetPageRequest(offset + getPageSize(), getPageSize(), sort);
    }

    @Override
    public Pageable previous() {
        return hasPrevious() ? new OffsetPageRequest(offset - getPageSize(), getPageSize(), sort) : this;
    }

    @Override
    public Pageable first() {
        return new OffsetPageRequest(0, getPageSize(), sort);
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return new OffsetPageRequest(pageNumber * getPageSize(), getPageSize(), sort);
    }
}

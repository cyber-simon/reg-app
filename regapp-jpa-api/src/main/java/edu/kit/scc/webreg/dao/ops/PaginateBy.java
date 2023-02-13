package edu.kit.scc.webreg.dao.ops;

public class PaginateBy {

	public static final int DEFAULT_OFFSET = 0;
	public static final int DEFAULT_LIMIT = 100;

	private final int offset;
	private final int limit;

	protected PaginateBy(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}

	public static PaginateBy of(int offset, int limit) {
		return new PaginateBy(offset, limit);
	}

	public static PaginateBy defaultLimitOffset() {
		return new PaginateBy(DEFAULT_OFFSET, DEFAULT_LIMIT);
	}

	public static PaginateBy unlimited() {
		return new PaginateBy(DEFAULT_OFFSET, -1);
	}

	public static PaginateBy withLimit(int limit) {
		return new PaginateBy(DEFAULT_OFFSET, limit);
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}

}

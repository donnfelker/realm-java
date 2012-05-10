package com.tightdb.lib;

import com.tightdb.TableBase;
import com.tightdb.TableQuery;
import com.tightdb.TableViewBase;

public abstract class AbstractQuery<Query, Cursor, View extends AbstractView<Cursor, View, ?>> {

	private final TableQuery query;
	private final TableBase table;
	private final EntityTypes<?, View, Cursor, Query> types;

	public AbstractQuery(EntityTypes<?, View, Cursor, Query> types, TableBase table, TableQuery query) {
		this.types = types;
		this.table = table;
		this.query = query;
	}

	public View findAll() {
		TableViewBase viewBase = query.findAll(table, 0, table.getCount(), Integer.MAX_VALUE);
		return view(viewBase);
	}

	public Cursor findFirst() {
		TableViewBase viewBase = query.findAll(table, 0, table.getCount(), 1);
		if (viewBase.getCount() > 0) {
			return cursor(viewBase, 0);
		} else {
			return null;
		}
	}

	public Cursor findLast() {
		// TODO: find more efficient way to search
		TableViewBase viewBase = query.findAll(table, 0, table.getCount(), Integer.MAX_VALUE);
		int count = viewBase.getCount();
		if (count > 0) {
			return cursor(viewBase, count - 1);
		} else {
			return null;
		}
	}

	public Cursor findUnique() {
		TableViewBase viewBase = query.findAll(table, 0, table.getCount(), 2);
		switch (viewBase.getCount()) {
		case 0:
			throw new IllegalStateException("Expected exactly one result, but found none!");
		case 1:
			return cursor(viewBase, 0);
		default:
			throw new IllegalStateException("Expected exactly one result, but found more!");
		}
	}

	public Query or() {
		return newQuery(query.or());
	}

	public Query startGroup() {
		return newQuery(query.startGroup());
	}

	public Query endGroup() {
		return newQuery(query.endGroup());
	}

	private Query newQuery(TableQuery q) {
		return createQuery(types.getQueryClass(), table, q);
	}

	public long clear() {
		View results = findAll();
		int count = results.size();
		results.clear();
		return count;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	protected View view(TableViewBase viewBase) {
		try {
			return types.getViewClass().getConstructor(TableViewBase.class).newInstance(viewBase);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}

	private Cursor cursor(TableViewBase viewBase, long position) {
		return AbstractCursor.createCursor(types.getCursorClass(), viewBase, position);
	}

	protected static <Q> Q createQuery(Class<Q> queryClass, TableBase tableBase, TableQuery tableQuery) {
		try {
			return queryClass.getConstructor(TableBase.class, TableQuery.class).newInstance(tableBase, tableQuery);
		} catch (Exception e) {
			throw new RuntimeException("Cannot create a query!", e);
		}
	}

}
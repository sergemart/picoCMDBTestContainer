package com.github.sergemart.picocmdb.test.external.junitrule;

import java.util.Arrays;

/**
 * Represents parameters to be passed to JdbcTemplate.update()
 */
class JdbcTask {

	private final String sqlQuery;
	private final Object[] sqlQueryParameters;


	String getSqlQuery() {
		return this.sqlQuery;
	}


	Object[] getSqlQueryParameters() {
		return this.sqlQueryParameters;
	}


	JdbcTask(String sqlQuery, Object[] sqlQueryParameters) {
		this.sqlQuery = sqlQuery;
		this.sqlQueryParameters = sqlQueryParameters;
	}


	@Override
	public String toString() {
		return "JdbcTask{" +
				"sqlQuery='" + sqlQuery + '\'' +
				", sqlQueryParameters=" + Arrays.toString(sqlQueryParameters) +
				'}';
	}
}

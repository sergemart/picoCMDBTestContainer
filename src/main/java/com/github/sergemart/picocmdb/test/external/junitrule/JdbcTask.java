package com.github.sergemart.picocmdb.test.external.junitrule;

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

}

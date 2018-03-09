package com.datastax.oss.driver.api.querybuilder;

/** An element in the query builder DSL, that will generate part of a CQL query. */
public interface CqlSnippet {
  String asCql(boolean pretty);
}

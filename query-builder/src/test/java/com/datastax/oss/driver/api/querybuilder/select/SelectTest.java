/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.api.querybuilder.select;

import static com.datastax.oss.driver.api.querybuilder.Assertions.assertThat;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.bindMarker;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getAll;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getColumn;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getRaw;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.isColumn;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.isColumnComponent;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.isCustomIndex;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.isRaw;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.isToken;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.isTuple;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.raw;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.selectFrom;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.tuple;

import org.junit.Test;

public class SelectTest {

  @Test
  public void should_generate_selectors() {
    assertThat(selectFrom("foo").all()).hasUglyCql("SELECT * FROM \"foo\"");
    assertThat(selectFrom("foo").countAll()).hasUglyCql("SELECT count(*) FROM \"foo\"");
    assertThat(selectFrom("foo").column("bar")).hasUglyCql("SELECT \"bar\" FROM \"foo\"");
    assertThat(selectFrom("foo").raw("a,b,c")).hasUglyCql("SELECT a,b,c FROM \"foo\"");

    assertThat(selectFrom("foo").column("bar").column("baz"))
        .hasUglyCql("SELECT \"bar\", \"baz\" FROM \"foo\"");

    assertThat(selectFrom("foo").selectors(getColumn("bar"), getColumn("baz")))
        .hasUglyCql("SELECT \"bar\", \"baz\" FROM \"foo\"");
    assertThat(selectFrom("foo").selectors(getColumn("bar"), getRaw("baz")))
        .hasUglyCql("SELECT \"bar\", baz FROM \"foo\"");
  }

  @Test
  public void should_remove_star_selector_if_other_selector_added() {
    assertThat(selectFrom("foo").all().column("bar")).hasUglyCql("SELECT \"bar\" FROM \"foo\"");
  }

  @Test
  public void should_remove_other_selectors_if_star_selector_added() {
    assertThat(selectFrom("foo").column("bar").column("baz").all())
        .hasUglyCql("SELECT * FROM \"foo\"");
  }

  @Test(expected = IllegalArgumentException.class)
  public void should_fail_if_selector_list_contains_star_selector() {
    selectFrom("foo").selectors(getColumn("bar"), getAll(), getRaw("baz"));
  }

  @Test
  public void should_alias_selectors() {
    assertThat(selectFrom("foo").column("bar").as("baz"))
        .hasUglyCql("SELECT \"bar\" AS \"baz\" FROM \"foo\"");
    assertThat(selectFrom("foo").selectors(getColumn("bar").as("c1"), getColumn("baz").as("c2")))
        .hasUglyCql("SELECT \"bar\" AS \"c1\", \"baz\" AS \"c2\" FROM \"foo\"");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_to_alias_star_selector() {
    selectFrom("foo").all().as("allthethings");
  }

  @Test(expected = IllegalStateException.class)
  public void should_fail_to_alias_if_no_selector_yet() {
    selectFrom("foo").as("bar");
  }

  @Test
  public void should_keep_last_alias_if_aliased_twice() {
    assertThat(selectFrom("foo").countAll().as("allthethings").as("total"))
        .hasUglyCql("SELECT count(*) AS \"total\" FROM \"foo\"");
  }

  @Test
  public void should_generate_comparison_relation() {
    assertThat(selectFrom("foo").all().where(isColumn("k").eq(bindMarker())))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = ?");
    assertThat(selectFrom("foo").all().where(isColumn("k").eq(bindMarker("value"))))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = :\"value\"");
  }

  @Test
  public void should_generate_is_not_null_relation() {
    assertThat(selectFrom("foo").all().where(isColumn("k").notNull()))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" IS NOT NULL");
  }

  @Test
  public void should_generate_in_relation() {
    assertThat(selectFrom("foo").all().where(isColumn("k").in(bindMarker())))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" IN ?");
    assertThat(selectFrom("foo").all().where(isColumn("k").in(bindMarker(), bindMarker())))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" IN (?,?)");
  }

  @Test
  public void should_generate_token_relation() {
    assertThat(selectFrom("foo").all().where(isToken("k1", "k2").eq(bindMarker("t"))))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE token(\"k1\",\"k2\") = :\"t\"");
  }

  @Test
  public void should_generate_column_component_relation() {
    assertThat(
            selectFrom("foo")
                .all()
                .where(
                    isColumn("id").eq(bindMarker()),
                    isColumnComponent("user", raw("'name'")).eq(bindMarker())))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"id\" = ? AND \"user\"['name'] = ?");
  }

  @Test
  public void should_generate_tuple_relation() {
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(isTuple("c1", "c2", "c3").in(bindMarker())))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = ? AND (\"c1\",\"c2\",\"c3\") IN ?");
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(isTuple("c1", "c2", "c3").in(tuple(bindMarker(), bindMarker()))))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = ? AND (\"c1\",\"c2\",\"c3\") IN (?,?)");
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(isTuple("c1", "c2", "c3").in(tuple(bindMarker(), raw("(4,5,6)")))))
        .hasUglyCql(
            "SELECT * FROM \"foo\" WHERE \"k\" = ? AND (\"c1\",\"c2\",\"c3\") IN (?,(4,5,6))");
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(
                    isTuple("c1", "c2", "c3")
                        .in(
                            tuple(
                                tuple(bindMarker(), bindMarker(), bindMarker()),
                                tuple(bindMarker(), bindMarker(), bindMarker())))))
        .hasUglyCql(
            "SELECT * FROM \"foo\" WHERE \"k\" = ? AND (\"c1\",\"c2\",\"c3\") IN ((?,?,?),(?,?,?))");

    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(isTuple("c1", "c2", "c3").eq(bindMarker())))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = ? AND (\"c1\",\"c2\",\"c3\") = ?");
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(
                    isTuple("c1", "c2", "c3").lt(tuple(bindMarker(), bindMarker(), bindMarker()))))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = ? AND (\"c1\",\"c2\",\"c3\") < (?,?,?)");
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(isTuple("c1", "c2", "c3").gte(raw("(1,2,3)"))))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = ? AND (\"c1\",\"c2\",\"c3\") >= (1,2,3)");
  }

  @Test
  public void should_generate_custom_index_relation() {
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(isCustomIndex("my_index", raw("'custom expression'"))))
        .hasUglyCql(
            "SELECT * FROM \"foo\" WHERE \"k\" = ? AND expr(\"my_index\",'custom expression')");
  }

  @Test
  public void should_generate_raw_relation() {
    assertThat(
            selectFrom("foo")
                .all()
                .where(isColumn("k").eq(bindMarker()))
                .where(isRaw("c = 'test'")))
        .hasUglyCql("SELECT * FROM \"foo\" WHERE \"k\" = ? AND c = 'test'");
  }

  @Test
  public void should_generate_limit() {
    assertThat(selectFrom("foo").all().limit(1)).hasUglyCql("SELECT * FROM \"foo\" LIMIT 1");
    assertThat(selectFrom("foo").all().limit(bindMarker("l")))
        .hasUglyCql("SELECT * FROM \"foo\" LIMIT :\"l\"");
  }

  @Test
  public void should_use_last_limit_if_called_multiple_times() {
    assertThat(selectFrom("foo").all().limit(1).limit(2))
        .hasUglyCql("SELECT * FROM \"foo\" LIMIT 2");
  }

  @Test
  public void should_generate_allow_filtering() {
    assertThat(selectFrom("foo").all().allowFiltering())
        .hasUglyCql("SELECT * FROM \"foo\" ALLOW FILTERING");
  }

  @Test
  public void should_use_single_allow_filtering_if_called_multiple_times() {
    assertThat(selectFrom("foo").all().allowFiltering().allowFiltering())
        .hasUglyCql("SELECT * FROM \"foo\" ALLOW FILTERING");
  }
}

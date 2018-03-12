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
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getAll;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getColumn;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getField;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getOpposite;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getRaw;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.getSum;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilderDsl.selectFrom;

import org.junit.Test;

public class SelectSelectorTest {

  @Test
  public void should_generate_star_selector() {
    assertThat(selectFrom("foo").all()).hasUglyCql("SELECT * FROM \"foo\"");
    assertThat(selectFrom("ks", "foo").all()).hasUglyCql("SELECT * FROM \"ks\".\"foo\"");
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
  public void should_generate_count_all_selector() {
    assertThat(selectFrom("foo").countAll()).hasUglyCql("SELECT count(*) FROM \"foo\"");
  }

  @Test
  public void should_generate_column_selectors() {
    assertThat(selectFrom("foo").column("bar")).hasUglyCql("SELECT \"bar\" FROM \"foo\"");
    assertThat(selectFrom("foo").column("bar").column("baz"))
        .hasUglyCql("SELECT \"bar\", \"baz\" FROM \"foo\"");
    assertThat(selectFrom("foo").selectors(getColumn("bar"), getColumn("baz")))
        .hasUglyCql("SELECT \"bar\", \"baz\" FROM \"foo\"");
  }

  @Test
  public void should_generate_arithmetic_selectors() {
    assertThat(selectFrom("foo").sum(getColumn("bar"), getColumn("baz")))
        .hasUglyCql("SELECT \"bar\" + \"baz\" FROM \"foo\"");
    assertThat(selectFrom("foo").opposite(getSum(getColumn("bar"), getColumn("baz"))))
        .hasUglyCql("SELECT -(\"bar\" + \"baz\") FROM \"foo\"");
    assertThat(
            selectFrom("foo")
                .product(getOpposite(getColumn("bar")), getSum(getColumn("baz"), getRaw("1"))))
        .hasUglyCql("SELECT -\"bar\" * (\"baz\" + 1) FROM \"foo\"");
  }

  @Test
  public void should_generate_field_selectors() {
    assertThat(selectFrom("foo").field("user", "name"))
        .hasUglyCql("SELECT \"user\".\"name\" FROM \"foo\"");
    assertThat(selectFrom("foo").field(getField("user", "address"), "city"))
        .hasUglyCql("SELECT \"user\".\"address\".\"city\" FROM \"foo\"");
  }

  @Test
  public void should_generate_raw_selector() {
    assertThat(selectFrom("foo").raw("a,b,c")).hasUglyCql("SELECT a,b,c FROM \"foo\"");

    assertThat(selectFrom("foo").selectors(getColumn("bar"), getRaw("baz")))
        .hasUglyCql("SELECT \"bar\", baz FROM \"foo\"");
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
}

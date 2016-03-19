/*
 * Copyright 2016 Vladimir Konstantinov, Yuriy Gintsyak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.oxydev.korro.util.config

import com.typesafe.config._

import java.net.{URI, URL}
import java.util

import scala.collection.JavaConversions._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try

/**
 * Collection of methods for <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html">
 * `Config`</a> interface allowing to get optional values from it instead of exceptions if value is missing of has
 * wrong type.
 *
 * @constructor Creates new instance of `ConfigExt` wrapping provided config object.
 * @param config Original config object.
 */
class ConfigExt(config: Config) {

  /**
   * Searches for `scala.Boolean` at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getBoolean-java.lang.String-
   */
  def findBoolean(path: String):Option[Boolean] = lookupValue(path, config.getBoolean)

  /**
   * Searches for `java.lang.Number` at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getNumber-java.lang.String-
   */
  def findNumber(path: String): Option[Number] = lookupValue(path, config.getNumber)

  /**
   * Searches for `scala.Int` at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getInt-java.lang.String-
   */
  def findInt(path: String): Option[Int] = lookupValue(path, config.getInt)

  /**
   * Searches for `scala.Long` at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getLong-java.lang.String-
   */
  def findLong(path: String): Option[Long] = lookupValue(path, config.getLong)

  /**
   * Searches for `scala.Double` at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getDouble-java.lang.String-
   */
  def findDouble(path: String): Option[Double] = lookupValue(path, config.getDouble)

  /**
   * Searches for `java.lang.String` at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getString-java.lang.String-
   */
  def findString(path: String): Option[String] = lookupValue(path, config.getString)

  /**
   * Searches for <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/ConfigObject.html">
   * `ConfigObject`</a> at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getObject-java.lang.String-
   */
  def findObject(path: String): Option[ConfigObject] = lookupValue(path, config.getObject)

  /**
   * Searches for <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html">`Config`</a>
   * at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getConfig-java.lang.String-
   */
  def findConfig(path: String): Option[Config] = lookupValue(path, config.getConfig)

  /**
   * Searches for <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/ConfigValue.html">
   * `ConfigValue`</a> at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getValue-java.lang.String-
   */
  def findValue(path: String): Option[ConfigValue] = lookupValue(path, config.getValue)

  /**
   * Searches for `size in bytes` as `scala.Long` at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getBytes-java.lang.String-
   */
  def findBytes(path: String): Option[Long] = lookupValue(path, config.getBytes)

  /**
   * Searches for `scala.concurrent.duration.Duration` at path.
   */
  def findDuration(path: String): Option[Duration] = lookupStringTry(path).map(Duration.apply).toOption

  /**
   * Searches for `scala.concurrent.duration.FiniteDuration` at path.
   */
  def findFiniteDuration(path: String): Option[FiniteDuration] = {
    lookupStringTry(path).map(Duration.apply).map(d => FiniteDuration(d.length, d.unit)).toOption
  }

  /**
   * Searches for `java.net.URL` at path.
   */
  def findURL(path: String): Option[URL] = lookupStringTry(path).map(new URL(_)).toOption

  /**
   * Searches for `java.net.URI` at path.
   */
  def findURI(path: String): Option[URI] = lookupStringTry(path).map(new URI(_)).toOption

  /**
   * Searches for <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/ConfigList.html">
   * `ConfigList`</a> at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getList-java.lang.String-
   */
  def findList(path: String): Option[ConfigList] = lookupValue(path, config.getList)

  /**
   * Searches for `scala.Boolean` list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getBooleanList-java.lang.String-
   */
  def findBooleanList(path: String): List[Boolean] = lookupList(path, config.getBooleanList).map(_.booleanValue)

  /**
   * Searches for `java.lang.Number` list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getNumberList-java.lang.String-
   */
  def findNumberList(path: String): List[Number] = lookupList(path, config.getNumberList)

  /**
   * Searches for `scala.Int` list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getIntList-java.lang.String-
   */
  def findIntList(path: String): List[Int] = lookupList(path, config.getIntList).map(_.intValue)

  /**
   * Searches for `scala.Long` list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getLongList-java.lang.String-
   */
  def findLongList(path: String): List[Long] = lookupList(path, config.getLongList).map(_.longValue)

  /**
   * Searches for `scala.Double` list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getDoubleList-java.lang.String-
   */
  def findDoubleList(path: String): List[Double] = lookupList(path, config.getDoubleList).map(_.doubleValue)

  /**
   * Searches for `java.lang.String` list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getStringList-java.lang.String-
   */
  def findStringList(path: String): List[String] = lookupList(path, config.getStringList)

  /**
   * Searches for <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/ConfigObject.html">
   * `ConfigObject`</a> list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getObjectList-java.lang.String-
   */
  def findObjectList(path: String): List[_ <: ConfigObject] = {
    lookupValue(path, config.getObjectList).map(_.toList).getOrElse(Nil)
  }

  /**
   * Searches for <a href="http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html">`Config`</a>
   * list at path.
   * @see http://typesafehub.github.io/config/latest/api/com/typesafe/config/Config.html#getConfigList-java.lang.String-
   */
  def findConfigList(path: String): List[_ <: Config] = {
    lookupValue(path, config.getConfigList).map(_.toList).getOrElse(Nil)
  }


  @inline private def lookupValue[V](path: String, f: (String) => V): Option[V] = Try(f(path)).toOption

  @inline private def lookupList[V](path: String, f: (String) => util.List[V]): List[V] = {
    lookupValue(path, f).map(_.toList).getOrElse(Nil)
  }

  @inline private def lookupStringTry(path: String): Try[String] = Try(config.getString(path))
}

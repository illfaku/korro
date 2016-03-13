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
 * TODO: Add description.
 *
 * @author Vladimir Konstantinov
 * @author Yuriy Gintsyak
 */
class WrappedConfig(config: Config) {

  // Value retrieval
  def findBoolean(path: String):Option[Boolean] = lookupValue(path, config.getBoolean)
  def findNumber(path: String): Option[Number] = lookupValue(path, config.getNumber)
  def findInt(path: String): Option[Int] = lookupValue(path, config.getInt)
  def findLong(path: String): Option[Long] = lookupValue(path, config.getLong)
  def findDouble(path: String): Option[Double] = lookupValue(path, config.getDouble)
  def findString(path: String): Option[String] = lookupValue(path, config.getString)

  def findObject(path: String): Option[ConfigObject] = lookupValue(path, config.getObject)
  def findConfig(path: String): Option[Config] = lookupValue(path, config.getConfig)
  def findValue(path: String): Option[ConfigValue] = lookupValue(path, config.getValue)

  def findBytes(path: String): Option[Long] = lookupValue(path, config.getBytes)

  def findDuration(path: String): Option[Duration] = findString(path).map(Duration.apply)
  def findFiniteDuration(path: String): Option[FiniteDuration] = {
    findDuration(path).filter(_.isFinite()).map(duration => FiniteDuration(duration.length, duration.unit))
  }

  def findURL(path: String): Option[URL] = findString(path) flatMap { url => Try(new URL(url)).toOption }
  def findURI(path: String): Option[URI] = findString(path) flatMap { uri => Try(new URI(uri)).toOption }

  // Collection retrieval
  def findList(path: String): Option[ConfigList] = lookupValue(path, config.getList)
  def findBooleanList(path: String): Iterable[Boolean] = lookupIterable(path, config.getBooleanList).map(_.booleanValue())
  def findNumberList(path: String): Iterable[Number] = lookupIterable(path, config.getNumberList)
  def findIntList(path: String): Iterable[Int] = lookupIterable(path, config.getIntList).map(_.intValue())
  def findLongList(path: String): Iterable[Long] = lookupIterable(path, config.getLongList).map(_.longValue)
  def findDoubleList(path: String): Iterable[Double] = lookupIterable(path, config.getDoubleList).map(_.doubleValue)
  def findStringList(path: String): Iterable[String] = lookupIterable(path, config.getStringList)

  def findObjectList(path: String): Iterable[_ <: ConfigObject] = lookupValue(path, config.getObjectList) match {
    case Some(x) => x.toIterable
    case None => Iterable.empty
  }

  def findConfigList(path: String): Iterable[_ <: Config] = lookupValue(path, config.getConfigList) match {
    case Some(x) => x.toIterable
    case None => Iterable.empty
  }

  def findBytesList(path: String): Iterable[Long] = lookupIterable(path, config.getBytesList).map(_.longValue)

  def findDurationList(path: String): Iterable[Duration] = findStringList(path).map(Duration.apply)
  def findFiniteDurationList(path: String): Iterable[FiniteDuration] = {
    findDurationList(path).filter(_.isFinite()).map(duration => FiniteDuration(duration.length, duration.unit))
  }

  // Internal
  @inline private def lookupValue[V](path: String, f: (String) => V): Option[V] = Try(f(path)).toOption

  @inline private def lookupIterable[V](path: String, f: (String) => util.List[V]): Iterable[V] = {
    lookupValue(path, f) match {
      case Some(x) => x.toIterable
      case None => Iterable.empty
    }
  }
}

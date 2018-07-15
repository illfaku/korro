/*
 * Copyright (C) 2015  Vladimir Konstantinov, Yuriy Gintsyak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.cafebabe.korro.util.config

import com.typesafe.config._

import java.net.{URI, URL}
import java.util

import scala.collection.JavaConverters._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Try

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
    case Some(x) => x.asScala
    case None => Iterable.empty
  }

  def findConfigList(path: String): Iterable[_ <: Config] = lookupValue(path, config.getConfigList) match {
    case Some(x) => x.asScala
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
      case Some(x) => x.asScala
      case None => Iterable.empty
    }
  }
}

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
package org.oxydev.korro.util.concurrent

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ThreadFactory.html">`ThreadFactory`</a> that
 * creates new threads with names as `prefix-N`, where `prefix` is provided via constructor and `N` is a sequence
 * number starting with 0.
 */
class SequenceThreadFactory(prefix: String) extends ThreadFactory {

  private val counter = new AtomicInteger()

  override def newThread(r: Runnable): Thread = new Thread(r, s"$prefix-${counter.getAndIncrement}")
}

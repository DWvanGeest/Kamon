/* =========================================================================================
 * Copyright © 2013-2017 the kamon project <http://kamon.io/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 * =========================================================================================
 */

package kamon.metric

import java.util.concurrent.atomic.AtomicLong

import kamon.util.MeasurementUnit

trait Gauge {
  def measurementUnit: MeasurementUnit

  def increment(): Unit
  def increment(times: Long): Unit
  def decrement(): Unit
  def decrement(times: Long): Unit
  def set(value: Long): Unit
}


class AtomicLongGauge(name: String, tags: Map[String, String], val measurementUnit: MeasurementUnit)
  extends SnapshotableGauge {

  private val currentValue = new AtomicLong(0L)

  def increment(): Unit =
    currentValue.incrementAndGet()

  def increment(times: Long): Unit =
    currentValue.addAndGet(times)

  def decrement(): Unit =
    currentValue.decrementAndGet()

  def decrement(times: Long): Unit =
    currentValue.addAndGet(-times)

  def set(value: Long): Unit =
    currentValue.set(value)

  def snapshot(): MetricValue =
    MetricValue(name, tags, measurementUnit, currentValue.get())
}

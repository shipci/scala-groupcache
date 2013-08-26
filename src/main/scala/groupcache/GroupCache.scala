/*
Copyright 2013 Josh Conrad

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package groupcache

import collection.mutable.Map
import group.{GroupRegister, Group}
import sinks.Sink
import peers.PeerPicker
import java.util.concurrent.locks.ReentrantReadWriteLock

class GroupCacheException(msg: String) extends Exception(msg)

object GroupCache extends GroupRegister {
  type Getter = (Any, String, Sink) => Unit

  private val rwLock = new ReentrantReadWriteLock
  private val groups = Map[String, Group]()

  override def getGroup(name: String): Option[Group] = {
    rwLock.readLock.lock()
    try {
      groups.get(name)
    }
    finally {
      rwLock.readLock.unlock()
    }
  }

  def addGroup(name: String, maxCacheBytes: Long, getter: Getter, peerPicker: PeerPicker): Group = {
    rwLock.writeLock.lock()
    try {
      if (groups.contains(name)) {
        throw new GroupCacheException(s"A group with name $name already exists")
      }

      val group = new Group(name, getter, peerPicker, maxCacheBytes)
      groups += name -> group
      group
    }
    finally {
      rwLock.writeLock.unlock()
    }
  }

  // TODO: Add methods for registering hooks...
}


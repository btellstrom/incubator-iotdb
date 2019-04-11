/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.cluster.concurrent.pool;

import org.apache.iotdb.cluster.concurrent.ThreadName;
import org.apache.iotdb.cluster.config.ClusterConfig;
import org.apache.iotdb.cluster.config.ClusterDescriptor;
import org.apache.iotdb.db.concurrent.IoTDBThreadPoolFactory;

/**
 * Manage all qp tasks in thread.
 */
public class QPTaskManager extends ThreadPoolManager {

  private static final String managerName = "qp task manager";

  private QPTaskManager() {
    init();
  }

  public static QPTaskManager getInstance() {
    return QPTaskManager.InstanceHolder.instance;
  }

  @Override
  public void init() {

    ClusterConfig config = ClusterDescriptor.getInstance().getConfig();
    this.threadCnt = config.getConcurrentQPSubTaskThread();
    pool = IoTDBThreadPoolFactory.newFixedThreadPool(threadCnt, ThreadName.QP_TASK.getName());
  }

  /**
   * Name of Pool Manager
   */
  @Override
  public String getManagerName() {
    return managerName;
  }

  private static class InstanceHolder {

    private InstanceHolder() {
    }

    private static QPTaskManager instance = new QPTaskManager();
  }
}

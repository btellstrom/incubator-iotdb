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

package org.apache.iotdb.db.tools;

import static org.apache.iotdb.db.writelog.node.ExclusiveWriteLogNode.WAL_FILE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.iotdb.db.exception.SysCheckException;
import org.apache.iotdb.db.qp.physical.crud.InsertPlan;
import org.apache.iotdb.db.writelog.io.LogWriter;
import org.apache.iotdb.db.writelog.transfer.PhysicalPlanLogTransfer;
import org.junit.Test;

public class WalCheckerTest {

  @Test
  public void testNoDir() {
    WalChecker checker = new WalChecker("no such dir");
    boolean caught = false;
    try {
      checker.doCheck();
    } catch (SysCheckException e) {
      caught = true;
    }
    assertTrue(caught);
  }

  @Test
  public void testEmpty() throws IOException, SysCheckException {
    File tempRoot = new File("root");
    tempRoot.mkdir();

    try {
      WalChecker checker = new WalChecker(tempRoot.getAbsolutePath());
      assertTrue(checker.doCheck().isEmpty());
    } finally {
      FileUtils.deleteDirectory(tempRoot);
    }
  }

  @Test
  public void testNormalCheck() throws IOException, SysCheckException {
    File tempRoot = new File("root");
    tempRoot.mkdir();

    try {
      for (int i = 0; i < 5; i++) {
        File subDir = new File(tempRoot, "storage_group" + i);
        subDir.mkdir();
        LogWriter logWriter = new LogWriter(subDir.getPath() + File.separator
            + WAL_FILE_NAME);

        List<byte[]> binaryPlans = new ArrayList<>();
        String deviceId = "device1";
        List<String> measurements = Arrays.asList("s1", "s2", "s3");
        List<String> values = Arrays.asList("5", "6", "7");
        for (int j = 0; j < 10; j++) {
          binaryPlans.add(PhysicalPlanLogTransfer
              .operatorToLog(new InsertPlan(deviceId, j, measurements, values)));
        }
        logWriter.write(binaryPlans);
        logWriter.force();

        logWriter.close();
      }

      WalChecker checker = new WalChecker(tempRoot.getAbsolutePath());
      assertTrue(checker.doCheck().isEmpty());
    } finally {
      FileUtils.deleteDirectory(tempRoot);
    }
  }

  @Test
  public void testAbnormalCheck() throws IOException, SysCheckException {
    File tempRoot = new File("root");
    tempRoot.mkdir();

    try {
      for (int i = 0; i < 5; i++) {
        File subDir = new File(tempRoot, "storage_group" + i);
        subDir.mkdir();
        LogWriter logWriter = new LogWriter(subDir.getPath() + File.separator
            + WAL_FILE_NAME);

        List<byte[]> binaryPlans = new ArrayList<>();
        String deviceId = "device1";
        List<String> measurements = Arrays.asList("s1", "s2", "s3");
        List<String> values = Arrays.asList("5", "6", "7");
        for (int j = 0; j < 10; j++) {
          binaryPlans.add(PhysicalPlanLogTransfer
              .operatorToLog(new InsertPlan(deviceId, j, measurements, values)));
        }
        if (i > 2) {
          binaryPlans.add("not a wal".getBytes());
        }
        logWriter.write(binaryPlans);
        logWriter.force();

        logWriter.close();
      }

      WalChecker checker = new WalChecker(tempRoot.getAbsolutePath());
      assertEquals(2, checker.doCheck().size());
    } finally {
      FileUtils.deleteDirectory(tempRoot);
    }
  }
}

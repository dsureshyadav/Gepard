package com.epam.gepard;

/*==========================================================================
 Copyright 2004-2015 EPAM Systems

 This file is part of Gepard.

 Gepard is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Gepard is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Gepard.  If not, see <http://www.gnu.org/licenses/>.
===========================================================================*/

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.reflection.Whitebox;

import com.epam.gepard.common.Environment;
import com.epam.gepard.common.helper.ConsoleWriter;
import com.epam.gepard.common.helper.ReportFinalizer;
import com.epam.gepard.common.helper.ResultCollector;
import com.epam.gepard.common.helper.TestFailureReporter;
import com.epam.gepard.common.threads.ExecutorThreadManager;
import com.epam.gepard.exception.ShutDownException;
import com.epam.gepard.logger.LogFileWriter;
import com.epam.gepard.logger.LogFinalizer;
import com.epam.gepard.logger.LogFolderCreator;
import com.epam.gepard.logger.helper.LogFileWriterFactory;

/**
 * Unit tests for {@link AllTestRunner}.
 * @author Tibor_Kovacs
 */
public class AllTestRunnerTest {

    @Mock
    private ConsoleWriter consoleWriter;
    @Mock
    private LogFolderCreator logFolderCreator;
    @Mock
    private ExecutorThreadManager executorThreadManager;
    @Mock
    private LogFileWriterFactory logFileWriterFactory;
    @Mock
    private LogFileWriter htmlLog;
    @Mock
    private LogFileWriter csvLog;
    @Mock
    private LogFileWriter quickLog;
    @Mock
    private ResultCollector resultCollector;
    @Mock
    private ReportFinalizer reportFinalizer;
    @Mock
    private LogFinalizer logFinalizer;
    @Mock
    private TestFailureReporter failureReporter;

    @InjectMocks
    private AllTestRunner underTest;

    @Before
    public void setup() {
        underTest = new AllTestRunner();
        MockitoAnnotations.initMocks(this);
        given(logFileWriterFactory.createSpecificLogWriter(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).willReturn(htmlLog)
                .willReturn(csvLog).willReturn(quickLog);
        Environment.setProperty(Environment.GEPARD_INSPECTOR_TEST_FACTORY, "com.epam.gepard.inspector.dummy.DummyFactory");
        Environment.setProperty(Environment.GEPARD_FILTER_CLASS, "com.epam.gepard.filter.DefaultTestFilter");
        Environment.setProperty(Environment.GEPARD_FILTER_EXPRESSION, "?");
    }

    @Test
    public void testRunAll() throws Exception {
        //GIVEN
        String testListFile = "./build/resources/test/testlist.txt";
        Whitebox.setInternalState(underTest, "executorThreadManager", executorThreadManager);
        Environment.resetProperty(Environment.GEPARD_LOAD_AND_EXIT, "false");
        //WHEN
        underTest.runAll(testListFile, consoleWriter);
        //THEN
        verify(logFolderCreator).prepareOutputFolders();
        verify(executorThreadManager).initiateAndStartExecutorThreads();
        verify(htmlLog).insertBlock(eq("Header"), Mockito.any(Properties.class));
        verify(csvLog).insertBlock(eq("Header"), Mockito.any(Properties.class));
        verify(quickLog).insertBlock(eq("Header"), Mockito.any(Properties.class));
        verify(executorThreadManager).closeRunningThreads();
        verify(consoleWriter).printStatusAfterTestRunCheck();
        verify(failureReporter).generateTestlistFailure();
    }

    @Test(expected = ShutDownException.class)
    public void testRunAllWhenGepardLoadAndExitFalse() throws Exception {
        //GIVEN
        String testListFile = "./build/resources/test/testlist.txt";
        Environment.resetProperty(Environment.GEPARD_LOAD_AND_EXIT, "true");
        //WHEN
        underTest.runAll(testListFile, consoleWriter);
        //THEN expected Exception
    }

    @Test(expected = ShutDownException.class)
    public void testRunAllWhenTestListNull() throws Exception {
        //GIVEN in setup
        //WHEN
        underTest.runAll(null, consoleWriter);
        //THEN expected Exception
    }
}
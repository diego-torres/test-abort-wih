package com.myspace.test_abort_wih;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManualOperationProcessTest extends JbpmJUnitBaseTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ManualOperationProcessTest.class);

    public ManualOperationProcessTest() {
        super(true, true);
    }

    @Test
    public void testProcess() {
        addWorkItemHandler("ManualOperation", new ManualOperation());
        createRuntimeManager("com/myspace/test_abort_wih/abort-wih-process.bpmn");
        RuntimeEngine runtimeEngine = getRuntimeEngine();
        KieSession ksession = runtimeEngine.getKieSession();

        ProcessInstance processInstance = ksession.startProcess("test-abort-wih.abort-wih-process");
        Long processInstanceId = processInstance.getId();

        assertProcessInstanceActive(processInstanceId);
        assertNodeActive(processInstanceId, ksession, "Manual Operation");

        ksession.signalEvent("Cancel", null);

        // FIXME: Only one node should be active, but 2 nodes are active
        List<String> activeNodes = getActiveNodesInProcessInstance(ksession, processInstanceId);
        logger.debug("NODES FOUND: {}", activeNodes);
        int activeNodesCount = activeNodes.size();
        assertEquals(1, activeNodesCount);

        assertNodeActive(processInstanceId, ksession, "Cancel Report Task");

        ksession.abortProcessInstance(processInstanceId);
        disposeRuntimeManager();
    }
}
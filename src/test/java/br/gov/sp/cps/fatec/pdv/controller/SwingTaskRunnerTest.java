package br.gov.sp.cps.fatec.pdv.controller;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SwingTaskRunnerTest {

    @Test
    void executaTarefaForaDaEdtECallbacksNaEdt() throws Exception {
        CountDownLatch concluido = new CountDownLatch(1);
        AtomicBoolean tarefaNaEdt = new AtomicBoolean(true);
        AtomicBoolean callbackNaEdt = new AtomicBoolean(false);

        SwingUtilities.invokeAndWait(() -> SwingTaskRunner.run(
                SwingUtilities::isEventDispatchThread,
                executadaNaEdt -> {
                    tarefaNaEdt.set(executadaNaEdt);
                    callbackNaEdt.set(SwingUtilities.isEventDispatchThread());
                    concluido.countDown();
                },
                error -> {
                    callbackNaEdt.set(SwingUtilities.isEventDispatchThread());
                    concluido.countDown();
                }));

        assertTrue(concluido.await(5, TimeUnit.SECONDS));
        assertFalse(tarefaNaEdt.get());
        assertTrue(callbackNaEdt.get());
    }
}

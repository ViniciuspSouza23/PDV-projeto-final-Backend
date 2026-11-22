package br.gov.sp.cps.fatec.pdv.controller;

import javax.swing.SwingWorker;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

final class SwingTaskRunner {

    private SwingTaskRunner() {
    }

    static <T> void run(Callable<T> task, Consumer<T> onSuccess, Consumer<Throwable> onFailure) {
        Objects.requireNonNull(task, "A tarefa não pode ser nula.");
        Objects.requireNonNull(onSuccess, "O callback de sucesso não pode ser nulo.");
        Objects.requireNonNull(onFailure, "O callback de erro não pode ser nulo.");

        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    onFailure.accept(ex);
                } catch (ExecutionException ex) {
                    onFailure.accept(ex.getCause() != null ? ex.getCause() : ex);
                }
            }
        }.execute();
    }
}

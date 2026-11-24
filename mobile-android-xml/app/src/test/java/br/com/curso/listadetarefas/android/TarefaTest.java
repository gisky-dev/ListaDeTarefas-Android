package br.com.curso.listadetarefas.android;

import br.com.curso.listadetarefas.android.model.Tarefa;
import org.junit.Test;
import static org.junit.Assert.*;

public class TarefaTest {

    @Test
    public void criarTarefaComDescricao() {
        Tarefa t = new Tarefa("Estudar Android", false);
        assertEquals("Estudar Android", t.getDescricao());
        assertFalse(t.isConcluida());
        assertNull(t.getId());
    }

    @Test
    public void marcarComoConcluida() {
        Tarefa t = new Tarefa(1L, "Teste", false);
        t.setConcluida(true);
        assertTrue(t.isConcluida());
    }

    @Test
    public void equalsBaseadoEmId() {
        Tarefa t1 = new Tarefa(10L, "A", false);
        Tarefa t2 = new Tarefa(10L, "B", true);
        assertEquals(t1, t2);
    }
}

package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FinalizarLeilaoServiceTest {

    private FinalizarLeilaoService service;

    @Mock
    private LeilaoDao leilaoDao;

    @Mock
    private EnviadorDeEmails enviadorDeEmails;

    @BeforeEach
    public void beforeEach() {
        MockitoAnnotations.initMocks(this);
        this.service = new FinalizarLeilaoService(this.leilaoDao, enviadorDeEmails);
    }

    @Test
    void deveriaFinalizarUmLeilao() {
        List<Leilao> leiloes = this.criaListaLeiloes();

        Mockito.when(leilaoDao.buscarLeiloesExpirados())
                .thenReturn(leiloes);

        this.service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);

        Assertions.assertEquals(new BigDecimal("900"), leilao.getLanceVencedor().getValor());
        Assertions.assertTrue(leilao.isFechado());
        Mockito.verify(leilaoDao).salvar(leilao);
    }

    @Test
    void deveriaEnviarEmailParaLanceVencedor() {
        List<Leilao> leiloes = this.criaListaLeiloes();

        Mockito.when(leilaoDao.buscarLeiloesExpirados())
                .thenReturn(leiloes);

        this.service.finalizarLeiloesExpirados();

        Leilao leilao = leiloes.get(0);
        Lance lanceVencedor = leilao.getLanceVencedor();

        Mockito.verify(enviadorDeEmails).enviarEmailVencedorLeilao(lanceVencedor);
    }

    @Test
    void naoDeveriaEnviarEmailParaLanceVencedorPorCausaDeErroAoEncerrarOLeilao() {
        List<Leilao> leiloes = this.criaListaLeiloes();

        Mockito.when(leilaoDao.buscarLeiloesExpirados())
                .thenReturn(leiloes);

        Mockito.when(leilaoDao.salvar(Mockito.any()))
                .thenThrow(RuntimeException.class);

        try {
            this.service.finalizarLeiloesExpirados();
        } catch (RuntimeException e) {

        }

        Mockito.verifyNoInteractions(enviadorDeEmails);
    }

    private List<Leilao> criaListaLeiloes() {
        List<Leilao> leiloes = new ArrayList<>();

        Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("Fulano"));

        Lance primeiro = new Lance(new Usuario("Beltrano"), new BigDecimal("600"));
        Lance segundo = new Lance(new Usuario("Ciclano"), new BigDecimal("900"));

        leilao.propoe(primeiro);
        leilao.propoe(segundo);

        leiloes.add(leilao);

        return leiloes;
    }
}

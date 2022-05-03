package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.*;

public class GeradorDePagamentoTest {

    private GeradorDePagamento geradorDePagamento;

    @Mock
    private PagamentoDao pagamentoDao;

    @Mock
    private Clock clock;

    @Captor
    private ArgumentCaptor<Pagamento> captor;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
        this.geradorDePagamento = new GeradorDePagamento(pagamentoDao, clock);
    }

    @Test
    void deveriaGerarPagamentoParaVencedorDoLeilao() {
        Leilao leilao = this.criaLeilao();
        Lance lanceVencedor = leilao.getLanceVencedor();

        LocalDate data = LocalDate.of(2022, Month.MAY, 3);

        Instant instant = data.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();

        Mockito.when(clock.instant()).thenReturn(instant);
        Mockito.when(clock.getZone()).thenReturn(ZoneId.systemDefault());

        this.geradorDePagamento.gerarPagamento(lanceVencedor);

        Mockito.verify(this.pagamentoDao).salvar(captor.capture());

        Pagamento pagamento = captor.getValue();

        Assertions.assertEquals(lanceVencedor.getLeilao(), pagamento.getLeilao());
        Assertions.assertEquals(lanceVencedor.getUsuario(), pagamento.getUsuario());
        Assertions.assertEquals(lanceVencedor.getValor(), pagamento.getValor());
        Assertions.assertEquals(LocalDate.now().plusDays(1), pagamento.getVencimento());
        Assertions.assertFalse(pagamento.getPago());
    }

    private Leilao criaLeilao() {
        Leilao leilao = new Leilao("Celular", new BigDecimal("500"), new Usuario("Fulano"));

        Usuario usuario = new Usuario("Ciclano");

        Lance lance = new Lance(usuario, new BigDecimal("900"));

        leilao.propoe(lance);

        leilao.setLanceVencedor(lance);
        lance.setLeilao(leilao);

        return leilao;
    }

}

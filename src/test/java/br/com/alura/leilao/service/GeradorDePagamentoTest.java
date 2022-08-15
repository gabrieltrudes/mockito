package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.PagamentoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Pagamento;
import br.com.alura.leilao.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeradorDePagamentoTest {

	private GeradorDePagamento gerador;
	
	@Mock
	private PagamentoDao pagamentoDao;
	
	@Mock
	private Clock clock;
	
	@Captor
	private ArgumentCaptor<Pagamento> captor;
	
	@BeforeEach
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		this.gerador = new GeradorDePagamento(pagamentoDao, clock);
	}
	
	@Test
	void deveriaCriarPagamentoParaVencedorDoLeilao() {
		Leilao leilao = leilao();
		Lance vencedor = leilao.getLanceVencedor();
		LocalDate data = LocalDate.of(2022, 8, 15);
		
		Instant instant = data.atStartOfDay(ZoneId.systemDefault()).toInstant();
		
		when(clock.instant()).thenReturn(instant);
		when(clock.getZone()).thenReturn(ZoneId.systemDefault());
		
		gerador.gerarPagamento(vencedor);
		
		verify(pagamentoDao).salvar(captor.capture());
		
		Pagamento pagamento = captor.getValue();
		
		assertEquals(LocalDate.now().plusDays(1),
				pagamento.getVencimento());
		assertEquals(vencedor.getValor(), pagamento.getValor());
		assertFalse(pagamento.getPago());
		assertEquals(vencedor.getUsuario(), pagamento.getUsuario());
		assertEquals(leilao, pagamento.getLeilao());
	}
	
	 private Leilao leilao() {
	        Leilao leilao = new Leilao("Celular",
	                        new BigDecimal("500"),
	                        new Usuario("Fulano"));

	        Lance lance = new Lance(new Usuario("Ciclano"),
	                        new BigDecimal("900"));

	        leilao.propoe(lance);
	        leilao.setLanceVencedor(lance);

	        return leilao;
	    }
}

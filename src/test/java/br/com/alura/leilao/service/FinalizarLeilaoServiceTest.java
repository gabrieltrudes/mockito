package br.com.alura.leilao.service;

import br.com.alura.leilao.dao.LeilaoDao;
import br.com.alura.leilao.model.Lance;
import br.com.alura.leilao.model.Leilao;
import br.com.alura.leilao.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class FinalizarLeilaoServiceTest {

	private FinalizarLeilaoService service;
	
	@Mock
	private LeilaoDao leilaoDao;
	
	@Mock
	private EnviadorDeEmails enviadorDeEmails;
	
	@BeforeEach
	public void beforeEach() {
		initMocks(this);
		this.service = new FinalizarLeilaoService(leilaoDao, enviadorDeEmails);
	}
	
	@Test
	void deveriaFinalizarUmLeilao() {
		List<Leilao> leiloes = leiloes();
		
		when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
		
		service.finalizarLeiloesExpirados();

		Leilao leilao = leiloes.get(0);
		assertTrue(leilao.isFechado());
		assertEquals(new BigDecimal("900"), leilao.getLanceVencedor().getValor());
		
		verify(leilaoDao).salvar(leilao);
	}
	
	@Test
	void deveriaEnviarEmailParaVencedorDoLeilao() {
		List<Leilao> leiloes = leiloes();
		
		when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
		
		service.finalizarLeiloesExpirados();
		
		Leilao leilao = leiloes.get(0);
		Lance lanceVencedor = leilao.getLanceVencedor();
		
		verify(enviadorDeEmails).enviarEmailVencedorLeilao(lanceVencedor);
	}
	
	@Test
	void naoDeveriaEnviarEmailParaVencedorDoLeilaoEmCasoDeErroAoEncerrarLeilao() {
		List<Leilao> leiloes = leiloes();
		
		when(leilaoDao.buscarLeiloesExpirados()).thenReturn(leiloes);
		
		when(leilaoDao.salvar(any())).thenThrow(RuntimeException.class);
		
		try {
			service.finalizarLeiloesExpirados();
			verifyNoInteractions(enviadorDeEmails);
		} catch (Exception e) {}
	}


    private List<Leilao> leiloes() {
        List<Leilao> lista = new ArrayList();

        Leilao leilao = new Leilao("Celular",
                        new BigDecimal("500"),
                        new Usuario("Fulano"));

        Lance primeiro = new Lance(new Usuario("Beltrano"),
                        new BigDecimal("600"));
        Lance segundo = new Lance(new Usuario("Ciclano"),
                        new BigDecimal("900"));

        leilao.propoe(primeiro);
        leilao.propoe(segundo);

        lista.add(leilao);

        return lista;

    }
}

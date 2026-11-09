package com.Nexturing.ODS7;

import com.Nexturing.ODS7.entity.ConfiguracaoEnergia;
import com.Nexturing.ODS7.repository.AparelhoRepository;
import com.Nexturing.ODS7.repository.ConfiguracaoEnergiaRepository;
import com.Nexturing.ODS7.service.DemoUsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendEndpointsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AparelhoRepository aparelhoRepository;

	@Autowired
	private ConfiguracaoEnergiaRepository configuracaoRepository;

	@Autowired
	private DemoUsuarioService demoUsuarioService;

	@BeforeEach
	void limparDadosDeTeste() {
		aparelhoRepository.deleteAll();
		ConfiguracaoEnergia configuracao = configuracaoRepository
				.findByUsuario_Id(demoUsuarioService.obterUsuarioDemo().getId())
				.orElse(null);
		if (configuracao != null && configuracao.getTarifaKwh() != null) {
			configuracao.atualizarTarifa(null);
			configuracaoRepository.save(configuracao);
		}
	}

	@Test
	void disponibilizaInterfaceERecursosEstaticosPeloSpring() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/index.html"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
				.andExpect(content().string(containsString("Energia Consciente")))
				.andExpect(content().string(containsString("href=\"#solar\"")))
				.andExpect(content().string(containsString("href=\"#quiz\"")))
				.andExpect(content().string(containsString("js/app.js")));

		mockMvc.perform(get("/js/app.js"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("renderDashboard")))
				.andExpect(content().string(containsString("renderSolarSimulator")))
				.andExpect(content().string(containsString("renderQuiz")))
				.andExpect(content().string(containsString("renderSimulator")));

		mockMvc.perform(get("/css/app.css"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("prefers-reduced-motion")));
	}

	@Test
	void endpointsCRUDAtualizamDashboardESimulacaoSemAlterarCadastro() throws Exception {
		mockMvc.perform(get("/api/dashboard"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantidadeAparelhos").value(0))
				.andExpect(jsonPath("$.maiorConsumidor").doesNotExist())
				.andExpect(jsonPath("$.tarifaConfigurada").value(false));

		mockMvc.perform(get("/api/configuracoes"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("\"tarifaKwh\":null")));

		mockMvc.perform(post("/api/consumo")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"potenciaWatts":100,"horasPorDia":5,"diasPorMes":30}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consumoMensalKwh").value(15))
				.andExpect(jsonPath("$.custoMensalEstimado").doesNotExist())
				.andExpect(jsonPath("$.tarifaConfigurada").value(false));

		mockMvc.perform(put("/api/configuracoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"tarifaKwh\":0.90}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tarifaKwh").value(0.90));

		MvcResult criacao = mockMvc.perform(post("/api/aparelhos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome":"Televisão","potenciaWatts":100,"horasPorDia":5,"diasPorMes":30}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.consumoMensalKwh").value(15))
				.andExpect(jsonPath("$.custoMensalEstimado").value(13.50))
				.andReturn();
		long id = jsonLong(criacao, "id");

		mockMvc.perform(get("/api/aparelhos/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.nome").value("Televisão"));
		mockMvc.perform(post("/api/consumo")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"potenciaWatts":100,"horasPorDia":5,"diasPorMes":30}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consumoMensalKwh").value(15))
				.andExpect(jsonPath("$.custoMensalEstimado").value(13.50))
				.andExpect(jsonPath("$.tarifaConfigurada").value(true));
		mockMvc.perform(get("/api/aparelhos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id));

		mockMvc.perform(get("/api/dashboard"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantidadeAparelhos").value(1))
				.andExpect(jsonPath("$.consumoMensalTotalKwh").value(15))
				.andExpect(jsonPath("$.custoMensalEstimado").value(13.50))
				.andExpect(jsonPath("$.maiorConsumidor.id").value(id));

		mockMvc.perform(post("/api/simulacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"aparelhoId\":" + id + ",\"horasPorDiaSimuladas\":3}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consumoOriginalKwh").value(15))
				.andExpect(jsonPath("$.consumoSimuladoKwh").value(9))
				.andExpect(jsonPath("$.reducaoConsumoKwh").value(6))
				.andExpect(jsonPath("$.economiaEstimada").value(5.40))
				.andExpect(jsonPath("$.percentualReducao").value(40));

		mockMvc.perform(get("/api/aparelhos/{id}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.horasPorDia").value(5));

		mockMvc.perform(put("/api/aparelhos/{id}", id)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome":"Televisão atualizada","potenciaWatts":100,"horasPorDia":4,"diasPorMes":30}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consumoMensalKwh").value(12));

		mockMvc.perform(delete("/api/aparelhos/{id}", id))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/dashboard"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.quantidadeAparelhos").value(0))
				.andExpect(jsonPath("$.consumoMensalTotalKwh").value(0));
	}

	@Test
	void rejeitaDadosInvalidosRegrasDeSimulacaoEIdsInexistentes() throws Exception {
		mockMvc.perform(post("/api/aparelhos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome":" ","potenciaWatts":-1,"horasPorDia":25,"diasPorMes":32}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detalhes").exists());

		MvcResult criacao = mockMvc.perform(post("/api/aparelhos")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"nome":"Ventilador","potenciaWatts":80,"horasPorDia":4,"diasPorMes":30}
								"""))
				.andExpect(status().isCreated())
				.andReturn();
		long id = jsonLong(criacao, "id");

		mockMvc.perform(post("/api/simulacoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"aparelhoId\":" + id + ",\"horasPorDiaSimuladas\":5}"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("não podem ultrapassar")));

		mockMvc.perform(get("/api/aparelhos/{id}", 999999))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.mensagem").value("Aparelho não encontrado."));
	}

	@Test
	void calculaCustoDoDashboardSobreOSomatorioAntesDoArredondamento() throws Exception {
		mockMvc.perform(put("/api/configuracoes")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"tarifaKwh\":0.50}"))
				.andExpect(status().isOk());

		for (String nome : new String[] { "Lâmpada 1", "Lâmpada 2" }) {
			mockMvc.perform(post("/api/aparelhos")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"nome\":\"" + nome
									+ "\",\"potenciaWatts\":1,\"horasPorDia\":10,\"diasPorMes\":1}"))
					.andExpect(status().isCreated());
		}

		mockMvc.perform(get("/api/dashboard"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consumoMensalTotalKwh").value(0.02))
				.andExpect(jsonPath("$.custoMensalEstimado").value(0.01));
	}

	@Test
	void endpointSolarValidaEntradaEDevolvePremissasSemDadosInventados() throws Exception {
		mockMvc.perform(post("/api/simulacoes/solar")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "valorContaMensal":400,
								  "consumoMensalKwh":500,
								  "cidade":"Campinas",
								  "estado":"SP",
								  "tarifaKwh":0.80,
								  "investimentoInicial":18000,
								  "geracaoMensalKwh":300,
								  "custoMensalNaoCompensavel":60,
								  "percentualTarifaCompensavel":80
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.consumoMensalAtualKwh").value(500))
				.andExpect(jsonPath("$.consumoEstimadoPeloValorDaConta").value(false))
				.andExpect(jsonPath("$.energiaCompensavelKwh").value(300))
				.andExpect(jsonPath("$.custoMensalComSolar").value(208))
				.andExpect(jsonPath("$.economiaMensal").value(192))
				.andExpect(jsonPath("$.economiaAnual").value(2304))
				.andExpect(jsonPath("$.retornoEstimadoMeses").value(93.75));

		mockMvc.perform(post("/api/simulacoes/solar")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "valorContaMensal":400,
								  "consumoMensalKwh":500,
								  "cidade":"Campinas",
								  "estado":"SP",
								  "tarifaKwh":0.80,
								  "custoMensalNaoCompensavel":60,
								  "percentualTarifaCompensavel":80
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.geracaoInformada").value(false))
				.andExpect(jsonPath("$.geracaoSolarMensalKwh").doesNotExist())
				.andExpect(jsonPath("$.economiaMensal").doesNotExist())
				.andExpect(jsonPath("$.retornoEstimadoMeses").doesNotExist());

		mockMvc.perform(post("/api/simulacoes/solar")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "valorContaMensal":100,
								  "consumoMensalKwh":200,
								  "cidade":"Campinas",
								  "estado":"SP",
								  "tarifaKwh":0.80,
								  "custoMensalNaoCompensavel":101,
								  "percentualTarifaCompensavel":100
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.mensagem").value(
						"O custo mensal não compensável não pode ser maior que o valor da conta."));

		mockMvc.perform(post("/api/simulacoes/solar")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "valorContaMensal":100,
								  "cidade":"Campinas",
								  "estado":"SP",
								  "tarifaKwh":0.80,
								  "custoMensalNaoCompensavel":20
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.detalhes.percentualTarifaCompensavel").exists());
	}

	private long jsonLong(MvcResult result, String field) throws Exception {
		String body = result.getResponse().getContentAsString();
		String value = body.replaceAll(".*\"" + field + "\":([0-9]+).*", "$1");
		return Long.parseLong(value);
	}
}

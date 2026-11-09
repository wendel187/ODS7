package com.Nexturing.ODS7.service;

import com.Nexturing.ODS7.entity.QuizCategoria;
import com.Nexturing.ODS7.entity.QuizPergunta;
import com.Nexturing.ODS7.repository.QuizPerguntaRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuizPerguntasInitializer implements ApplicationRunner {

	private final QuizPerguntaRepository perguntaRepository;

	public QuizPerguntasInitializer(QuizPerguntaRepository perguntaRepository) {
		this.perguntaRepository = perguntaRepository;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (perguntaRepository.count() == 0) {
			perguntaRepository.saveAll(perguntasIniciais());
		}
	}

	private List<QuizPergunta> perguntasIniciais() {
		return List.of(
				pergunta(QuizCategoria.ECONOMIA_DOMESTICA, "Qual atitude ajuda a reduzir o gasto de energia com iluminação durante o dia?",
						"Quando houver luz natural suficiente, aproveitá-la pode evitar o uso desnecessário de lâmpadas. A necessidade de iluminação depende do ambiente e da atividade.",
						"Acender todas as lâmpadas", "Aproveitar a luz natural", "Deixar as luzes acesas ao sair", "Usar cortinas sempre fechadas", 1),
				pergunta(QuizCategoria.ECONOMIA_DOMESTICA, "O que costuma acontecer com aparelhos que permanecem em modo de espera (stand-by)?",
						"Alguns aparelhos continuam consumindo energia em stand-by. O consumo varia por modelo; consulte o manual ou a etiqueta do produto.",
						"Todos passam a gerar energia", "Podem continuar consumindo energia", "Consomem sempre mais que ligados", "O consumo é sempre zero", 1),
				pergunta(QuizCategoria.ECONOMIA_DOMESTICA, "Qual hábito pode diminuir o tempo de uso de um chuveiro elétrico?",
						"Reduzir o tempo de banho diminui o período em que o chuveiro elétrico permanece ligado. O resultado depende da potência e do uso.",
						"Deixar o chuveiro ligado durante a escolha da roupa", "Tomar banhos mais curtos", "Abrir mais o registro para aquecer a água", "Aumentar a temperatura em dias quentes", 1),
				pergunta(QuizCategoria.ECONOMIA_DOMESTICA, "Ao sair de um cômodo vazio, qual é uma escolha consciente?",
						"Apagar a luz de um cômodo vazio evita seu uso sem necessidade, desde que isso não comprometa a segurança ou uma necessidade específica.",
						"Apagar a luz, quando não for necessária", "Trocar a lâmpada por uma mais potente", "Deixar a luz acesa para economizar", "Ligar outros aparelhos no cômodo", 0),
				pergunta(QuizCategoria.ECONOMIA_DOMESTICA, "Por que vale a pena observar o tempo de uso dos aparelhos?",
						"O consumo depende da potência e do tempo de funcionamento; observar os dois ajuda a compreender o consumo estimado.",
						"Porque o tempo substitui a potência no cálculo", "Porque o consumo depende também do tempo de uso", "Porque aparelhos desligados sempre consomem mais", "Porque todo aparelho consome igual por hora", 1),
				pergunta(QuizCategoria.ECONOMIA_DOMESTICA, "Qual prática ajuda a evitar consumo desnecessário de carregadores?",
						"Desconectar carregadores sem uso pode evitar consumo em alguns modelos e também reduz equipamentos ligados sem necessidade; o efeito varia entre aparelhos.",
						"Manter todos conectados o dia inteiro", "Desconectar os que não estão sendo usados", "Conectar vários em uma extensão sem limite", "Deixar o celular carregando sem necessidade", 1),

				pergunta(QuizCategoria.ELETRODOMESTICOS, "O que comparar ao escolher uma lâmpada para substituir outra?",
						"A quantidade de luz (lúmens) ajuda a comparar a iluminação entregue; potência em watts indica consumo elétrico, mas não é a única medida de brilho.",
						"Apenas a cor da embalagem", "Lúmens e potência em watts", "Somente o tamanho do interruptor", "A quantidade de fios da casa", 1),
				pergunta(QuizCategoria.ELETRODOMESTICOS, "Qual cuidado recomendado para uma geladeira?",
						"Manter espaço de ventilação conforme o manual ajuda o aparelho a dissipar calor. As orientações específicas variam por modelo.",
						"Encostar a parte traseira na parede em qualquer modelo", "Seguir as folgas de instalação do fabricante", "Deixar a porta aberta para circular o ar", "Instalar perto de uma fonte de calor", 1),
				pergunta(QuizCategoria.ELETRODOMESTICOS, "O que pode ajudar a evitar perda de ar frio da geladeira?",
						"Uma borracha de vedação em boas condições ajuda a porta a fechar corretamente e a reduzir a troca de ar com o ambiente.",
						"Verificar se a borracha da porta veda corretamente", "Abrir a porta por longos períodos", "Guardar alimentos ainda muito quentes sem cuidado", "Bloquear a circulação interna de ar", 0),
				pergunta(QuizCategoria.ELETRODOMESTICOS, "Como a etiqueta de eficiência energética pode ajudar na compra?",
						"A etiqueta do Inmetro apresenta informações para comparar eficiência de modelos dentro das regras aplicáveis ao produto; confira a etiqueta vigente.",
						"Ela garante que o aparelho nunca terá consumo", "Ela permite comparar informações de eficiência", "Ela informa o valor final da conta de luz", "Ela substitui a leitura do manual", 1),
				pergunta(QuizCategoria.ELETRODOMESTICOS, "Qual informação é necessária para estimar o consumo de um aparelho?",
						"Uma estimativa simples usa potência em quilowatts multiplicada pelo tempo de funcionamento. O consumo real pode variar conforme o uso e o aparelho.",
						"Somente a cor do aparelho", "Potência e tempo de uso", "A idade de quem usa o aparelho", "A distância até o medidor", 1),
				pergunta(QuizCategoria.ELETRODOMESTICOS, "Qual cuidado com o filtro do ar-condicionado é adequado?",
						"Limpar o filtro conforme as instruções do fabricante ajuda a manter o fluxo de ar e o funcionamento previsto do aparelho.",
						"Retirar o filtro permanentemente", "Limpar conforme o manual do fabricante", "Lavar o aparelho ligado", "Cobrir a entrada de ar", 1),

				pergunta(QuizCategoria.ENERGIA_SOLAR, "Como um painel fotovoltaico produz eletricidade?",
						"Células fotovoltaicas convertem parte da luz incidente em energia elétrica. Não é necessário que o painel aqueça água para gerar eletricidade.",
						"Converte luz em energia elétrica", "Armazena vento dentro das células", "Queima combustível no telhado", "Transforma chuva diretamente em eletricidade", 0),
				pergunta(QuizCategoria.ENERGIA_SOLAR, "Um sistema fotovoltaico pode gerar energia em dias nublados?",
						"Painéis podem gerar eletricidade com luz difusa, mas a produção costuma variar com a quantidade de luz e as condições do local.",
						"Não, nunca há geração com nuvens", "Sim, pode haver geração, geralmente diferente de um dia ensolarado", "Sim, sempre gera a potência máxima", "Somente se chover", 1),
				pergunta(QuizCategoria.ENERGIA_SOLAR, "Qual componente converte a corrente contínua dos módulos para corrente alternada usada na instalação?",
						"O inversor converte a corrente contínua produzida pelos módulos em corrente alternada, conforme as especificações do sistema.",
						"O medidor de água", "O inversor", "O disjuntor do chuveiro", "A estrutura do telhado", 1),
				pergunta(QuizCategoria.ENERGIA_SOLAR, "O que é importante avaliar antes de instalar painéis solares?",
						"Uma avaliação técnica considera condições do local, sombreamento, estrutura e perfil de consumo; não existe dimensionamento único para todos.",
						"Somente a cor do telhado", "Condições do telhado, sombreamento e consumo", "A quantidade de lâmpadas da rua", "A marca do medidor de água", 1),
				pergunta(QuizCategoria.ENERGIA_SOLAR, "Painéis solares fotovoltaicos precisam de sol direto para produzir qualquer energia?",
						"A geração fotovoltaica também pode ocorrer com luz difusa, embora a produção varie e geralmente seja menor do que sob condições favoráveis de irradiância.",
						"Sim, sem sol direto a geração é sempre zero", "Não; luz difusa também pode permitir geração", "Não; funcionam apenas com calor noturno", "Sim, mas apenas no verão", 1),
				pergunta(QuizCategoria.ENERGIA_SOLAR, "Uma simulação de energia solar representa uma proposta definitiva de instalação?",
						"Simuladores são estimativas baseadas em premissas. Projeto, orçamento, regras de compensação e condições locais precisam de análise profissional.",
						"Sim, substitui vistoria e projeto", "Não, é uma estimativa que depende das premissas informadas", "Sim, garante economia igual para todos", "Não, porque painéis não geram eletricidade", 1),

				pergunta(QuizCategoria.ENERGIA_RENOVAVEL, "Qual destas fontes é renovável e depende da força dos ventos?",
						"A energia eólica utiliza o vento para movimentar turbinas e gerar eletricidade.",
						"Carvão mineral", "Energia eólica", "Óleo diesel", "Gás natural", 1),
				pergunta(QuizCategoria.ENERGIA_RENOVAVEL, "Qual característica descreve uma fonte renovável de energia?",
						"Fontes renováveis são repostas por processos naturais em escalas compatíveis com seu uso, mas isso não significa ausência de impactos ambientais.",
						"É reposta naturalmente em escala compatível com seu uso", "Não causa nenhum impacto ambiental", "Está disponível em quantidade ilimitada em qualquer lugar", "Não precisa de infraestrutura", 0),
				pergunta(QuizCategoria.ENERGIA_RENOVAVEL, "A energia hidrelétrica é considerada renovável?",
						"A geração hidrelétrica utiliza o ciclo da água e é classificada como renovável, embora barragens e reservatórios possam produzir impactos socioambientais.",
						"Sim, e não tem impacto ambiental", "Sim, mas pode haver impactos socioambientais", "Não, porque usa água", "Não, porque depende de turbinas", 1),
				pergunta(QuizCategoria.ENERGIA_RENOVAVEL, "De onde pode vir a energia da biomassa?",
						"A biomassa pode incluir matéria orgânica, como resíduos vegetais. Seu uso precisa considerar origem, emissões e impactos do ciclo de vida.",
						"Da matéria orgânica", "Da luz refletida pela Lua", "Do petróleo refinado", "Do calor do núcleo da Terra", 0),
				pergunta(QuizCategoria.ENERGIA_RENOVAVEL, "Por que a geração solar e eólica pode variar ao longo do tempo?",
						"A disponibilidade de sol e vento muda conforme o clima e o horário. Sistemas elétricos equilibram fontes e recursos para atender à demanda.",
						"Porque dependem de condições naturais variáveis", "Porque as turbinas usam combustível fóssil", "Porque não podem ser conectadas à rede", "Porque deixam de ser renováveis à noite", 0),
				pergunta(QuizCategoria.ENERGIA_RENOVAVEL, "Qual opção é um combustível fóssil?",
						"Carvão mineral, petróleo e gás natural são combustíveis fósseis formados ao longo de períodos geológicos e não se renovam na escala de consumo humano.",
						"Vento", "Luz solar", "Carvão mineral", "Marés", 2),

				pergunta(QuizCategoria.SUSTENTABILIDADE, "Qual é o objetivo central da ODS 7 da Agenda 2030?",
						"A ODS 7 busca assegurar acesso confiável, sustentável, moderno e a preço acessível à energia para todas as pessoas.",
						"Universalizar o acesso à energia confiável, sustentável, moderna e acessível", "Substituir toda a água por energia", "Garantir que toda energia venha de uma única fonte", "Eliminar o uso de eletricidade nas cidades", 0),
				pergunta(QuizCategoria.SUSTENTABILIDADE, "Eficiência energética significa necessariamente deixar de usar um serviço?",
						"Eficiência busca oferecer o mesmo serviço ou resultado usando menos energia, por exemplo com um equipamento mais eficiente.",
						"Sim, significa parar de usar aparelhos", "Não; pode oferecer o mesmo serviço com menos energia", "Sim, significa usar sempre menos luz", "Não; significa aumentar a potência dos aparelhos", 1),
				pergunta(QuizCategoria.SUSTENTABILIDADE, "Economizar energia em casa garante uma redução fixa na conta de todas as pessoas?",
						"A conta depende de consumo, tarifas, encargos e outros componentes. Uma mudança de hábito pode alterar o consumo, mas não garante um valor fixo de economia.",
						"Sim, a redução é sempre igual", "Não; o resultado depende do consumo e das condições da tarifa", "Sim, basta desligar uma lâmpada", "Não; hábitos nunca alteram consumo", 1),
				pergunta(QuizCategoria.SUSTENTABILIDADE, "Qual alternativa descreve uma escolha de consumo mais consciente?",
						"Escolher produtos adequados à necessidade e comparar informações de eficiência e durabilidade pode ajudar a considerar impactos e uso de recursos.",
						"Comprar sempre o aparelho de maior potência", "Comparar eficiência e necessidades antes da compra", "Trocar aparelhos que ainda funcionam sem avaliar", "Ignorar as instruções de uso", 1),
				pergunta(QuizCategoria.SUSTENTABILIDADE, "Por que a transição para energia limpa também exige planejamento?",
						"Fontes, redes, armazenamento, território e impactos precisam ser considerados para que a transição seja confiável, sustentável e justa.",
						"Porque nenhuma fonte renovável produz eletricidade", "Para considerar rede, confiabilidade, impactos e acesso", "Para impedir o acesso de novas comunidades", "Porque energia limpa não pode ser distribuída", 1),
				pergunta(QuizCategoria.SUSTENTABILIDADE, "O que fazer com um equipamento eletrônico que não funciona mais?",
						"Equipamentos eletrônicos podem conter materiais que exigem manejo adequado. Procure pontos de coleta ou logística reversa disponíveis na região.",
						"Descartá-lo em qualquer terreno", "Queimá-lo em casa", "Procurar coleta ou logística reversa apropriada", "Jogá-lo em rios", 2)
		);
	}

	private QuizPergunta pergunta(QuizCategoria categoria, String enunciado, String explicacao, String alternativa1,
			String alternativa2, String alternativa3, String alternativa4, int indiceCorreto) {
		return new QuizPergunta(categoria, enunciado, explicacao,
				List.of(alternativa1, alternativa2, alternativa3, alternativa4), indiceCorreto);
	}
}

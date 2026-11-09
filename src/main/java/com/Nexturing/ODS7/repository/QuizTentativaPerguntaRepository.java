package com.Nexturing.ODS7.repository;

import com.Nexturing.ODS7.entity.QuizTentativaPergunta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizTentativaPerguntaRepository extends JpaRepository<QuizTentativaPergunta, Long> {

	@EntityGraph(attributePaths = { "pergunta", "pergunta.alternativas" })
	List<QuizTentativaPergunta> findAllByTentativa_IdOrderByOrdemAsc(Long tentativaId);

	@EntityGraph(attributePaths = { "pergunta", "pergunta.alternativas" })
	Optional<QuizTentativaPergunta> findByTentativa_IdAndPergunta_Id(Long tentativaId, Long perguntaId);
}

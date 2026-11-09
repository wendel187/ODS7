package com.Nexturing.ODS7.repository;

import com.Nexturing.ODS7.entity.QuizPergunta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizPerguntaRepository extends JpaRepository<QuizPergunta, Long> {

	@EntityGraph(attributePaths = "alternativas")
	List<QuizPergunta> findAllByOrderByIdAsc();
}

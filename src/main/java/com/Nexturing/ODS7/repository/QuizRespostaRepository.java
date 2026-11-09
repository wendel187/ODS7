package com.Nexturing.ODS7.repository;

import com.Nexturing.ODS7.entity.QuizResposta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRespostaRepository extends JpaRepository<QuizResposta, Long> {

	boolean existsByTentativaPergunta_Id(Long tentativaPerguntaId);

	long countByTentativaPergunta_Tentativa_Id(Long tentativaId);
}

package com.Nexturing.ODS7.repository;

import com.Nexturing.ODS7.entity.QuizTentativa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizTentativaRepository extends JpaRepository<QuizTentativa, Long> {

	Optional<QuizTentativa> findByIdAndUsuario_Id(Long id, Long usuarioId);

	List<QuizTentativa> findAllByUsuario_IdOrderByIniciadaEmDesc(Long usuarioId);
}

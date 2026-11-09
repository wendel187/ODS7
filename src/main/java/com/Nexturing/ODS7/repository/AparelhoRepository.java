package com.Nexturing.ODS7.repository;

import com.Nexturing.ODS7.entity.Aparelho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AparelhoRepository extends JpaRepository<Aparelho, Long> {

	List<Aparelho> findAllByUsuario_IdOrderByNomeAsc(Long usuarioId);

	Optional<Aparelho> findByIdAndUsuario_Id(Long id, Long usuarioId);
}

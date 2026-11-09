package com.Nexturing.ODS7.repository;

import com.Nexturing.ODS7.entity.ConfiguracaoEnergia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracaoEnergiaRepository extends JpaRepository<ConfiguracaoEnergia, Long> {

	Optional<ConfiguracaoEnergia> findByUsuario_Id(Long usuarioId);
}

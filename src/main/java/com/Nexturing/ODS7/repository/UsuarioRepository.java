package com.Nexturing.ODS7.repository;

import com.Nexturing.ODS7.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

	Optional<Usuario> findByCodigo(String codigo);
}

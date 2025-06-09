package com.seguranca.redes.repository;

import com.seguranca.redes.model.Arquivos;
import com.seguranca.redes.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArquivosRepository extends JpaRepository<Arquivos, Long> {

    // Buscar arquivos por usuário
    List<Arquivos> findByUsuario(Usuario usuario);

    // Buscar por nome original
    List<Arquivos> findByNomeOriginalContainingIgnoreCase(String nomeOriginal);

    // Buscar por finalidade
    List<Arquivos> findByFinalidadeContainingIgnoreCase(String finalidade);

}

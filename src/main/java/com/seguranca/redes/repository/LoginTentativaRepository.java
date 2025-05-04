package com.seguranca.redes.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.seguranca.redes.model.LoginTentativa;

import jakarta.transaction.Transactional;

@Repository
public interface LoginTentativaRepository extends JpaRepository<LoginTentativa, Long> {

    // Buscar a última tentativa bem-sucedida
    Optional<LoginTentativa> findTopBySessionIdAndSucessoTrueOrderByTentativaEmDesc(String sessionId);
    Optional<LoginTentativa> findTopByUsuarioIdAndSucessoTrueOrderByTentativaEmDesc(Long usuarioId);


    // Contar falhas de login nos últimos 15 minutos para um usuário
    @Query("SELECT COUNT(t) FROM LoginTentativa t WHERE t.usuario.id = :usuarioId AND t.sucesso = false AND t.tentativaEm > :limite")
    long countRecentesFalhas(@Param("usuarioId") Long usuarioId, @Param("limite") LocalDateTime limite);

    // Obter a última tentativa de login falha para um usuário
    @Query("SELECT MAX(t.tentativaEm) FROM LoginTentativa t WHERE t.usuario.id = :usuarioId AND t.sucesso = false")
    LocalDateTime ultimaTentativa(@Param("usuarioId") Long usuarioId);

    // Método para excluir tentativas de login falhas quando o login for bem-sucedido
    @Modifying
    @Transactional
    @Query("DELETE FROM LoginTentativa t WHERE t.usuario.id = :usuarioId AND t.sucesso = false")
    void deleteFalhasByUsuarioId(@Param("usuarioId") Long usuarioId);
}
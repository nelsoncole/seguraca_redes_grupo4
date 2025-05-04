package com.seguranca.redes.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.seguranca.redes.model.CodigoAutenticacao;

@Repository
public interface CodigoAutenticacaoRepository extends JpaRepository<CodigoAutenticacao, Long> {
    Optional<CodigoAutenticacao> findFirstByDestinoOrderByDataGeracaoDesc(String destino);
    void deleteByDataGeracaoBefore(LocalDateTime data);
}

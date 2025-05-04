package com.seguranca.redes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.seguranca.redes.repository.LoginTentativaRepository;

import jakarta.transaction.Transactional;

@Service
public class LoginService {

    @Autowired
    private LoginTentativaRepository falhaLoginRepository;

    @Transactional
    public void deletarFalhasPorUsuario(Long usuarioId) {
        falhaLoginRepository.deleteFalhasByUsuarioId(usuarioId);
    }
}


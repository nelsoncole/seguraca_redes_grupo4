package com.seguranca.redes.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.seguranca.redes.model.LoginTentativa;
import com.seguranca.redes.repository.LoginTentativaRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;


@Controller
public class IndexController {
    
    @Autowired
    private LoginTentativaRepository loginTentativaRepository;

    @GetMapping("/")
    public String index(@CookieValue(value = "usuarioId", required = false) String usuarioId) {

        // Verificação de login
        if (usuarioId == null) {
            return "redirect:/login";
        }
        // Verifique de sessao unica
        String sessionId = usuarioId;
        // Busca a última tentativa com sucesso para a sessão
        Optional<LoginTentativa> tentativaSucessoSession = loginTentativaRepository
        .findTopBySessionIdAndSucessoTrueOrderByTentativaEmDesc(sessionId);

        if (tentativaSucessoSession.isPresent()) {
            // Sessão válida
            LoginTentativa tentativaSession = tentativaSucessoSession.get();
            Long id = tentativaSession.getUsuario().getId();

            Optional<LoginTentativa> tentativaSucesso = loginTentativaRepository
            .findTopByUsuarioIdAndSucessoTrueOrderByTentativaEmDesc(id);

            if (tentativaSucesso.isPresent()) {
                LoginTentativa tentativa = tentativaSucesso.get();
                if(!sessionId.equals(tentativa.getSessionId())){
                    return "redirect:/logout-manual";
                }
            }else{
                // Nenhuma tentativa de login com sucesso encontrada para essa sessão
                return "redirect:/login";
            }
        } else {
            // Nenhuma tentativa de login com sucesso encontrada para essa sessão
            return "redirect:/login";
        }

        // Verificação de inatividade

        // Retorna o arquivo HTML
        return "index";
    }

    @GetMapping("/ping")
    @ResponseBody
    public ResponseEntity<Void> renovarSessao(HttpSession session, HttpServletResponse response) {
        String sessionId = session.getId();

        // Recria o cookie com nova validade
        Cookie cookie = new Cookie("usuarioId", sessionId);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // true se usar HTTPS
        cookie.setMaxAge(600); // 10 minutos
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }


}

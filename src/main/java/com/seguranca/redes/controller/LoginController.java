package com.seguranca.redes.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.seguranca.redes.model.LoginTentativa;
import com.seguranca.redes.model.Usuario;
import com.seguranca.redes.repository.LoginTentativaRepository;
import com.seguranca.redes.repository.UsuarioRepository;
import com.seguranca.redes.service.LoginService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class LoginController {
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginTentativaRepository loginTentativaRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private LoginService loginService;

    // Exibe a página de login.html
    @GetMapping("/login")
    public String loginFrom() {
        return "auth/login";
    }

    public void removerCookie(String nome, HttpServletResponse response) {
        Cookie cookie = new Cookie(nome, null);
        cookie.setMaxAge(0);
        cookie.setPath("/"); // O path precisa ser o mesmo
        cookie.setHttpOnly(true); // Igual ao da criação
        cookie.setSecure(false);  // Igual ao da criação
        response.addCookie(cookie);
    }

    /*
        O Spring Security tem uma implementação interna de logout
        que é ativada automaticamente quando você usa o método @GetMapping("/logout")
        @GetMapping("/logout")
    */
    @GetMapping("/logout-manual")
    public String logout(HttpServletResponse response) {
        
        removerCookie("usuarioId", response);
        return "redirect:/login";
    }
    
    // Exibe a página de criar_conta.html
    @GetMapping("/criar-conta")
    public String criarContaFrom() {
        return "auth/criar_conta";
    }
    
    // Exibe a página de recuperar_senha.html
    @GetMapping("/recuperar-senha")
    public String recuperarSenhaFrom() {
        return "auth/recuperar_senha";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute Usuario usuario, Model model, HttpSession session, HttpServletResponse response) {
        Usuario usuarioBanco = usuarioRepository.findByEmail(usuario.getEmail()).orElse(null);

        if (usuarioBanco == null) {
            model.addAttribute("erro", "Usuário não encontrado.");
            return "auth/login";
        }

    
        boolean senhaCorreta = passwordEncoder.matches(usuario.getSenha(), usuarioBanco.getSenha());
        String sessionId = session.getId();

        Long userId = usuarioBanco.getId();
        LocalDateTime limite = LocalDateTime.now().minusMinutes(15);
        long tentativas = loginTentativaRepository.countRecentesFalhas(userId, limite);

        if (tentativas >= 4) {
            LocalDateTime ultima = loginTentativaRepository.ultimaTentativa(userId);
            long restante = 15 * 60 - Duration.between(ultima, LocalDateTime.now()).getSeconds();
            long min = restante / 60;
            long seg = restante % 60;

            // Adiciona o tempo restante no modelo para usar no Thymeleaf
            model.addAttribute("tempoRestante", restante);  // Aqui é o valor do tempo restante
            model.addAttribute("erro", "Excedeu as tentativas. Tente novamente em " + min + " min e " + seg + " s.");
            return "auth/login";
        }

        // Registra tentativa de login
        LoginTentativa tentativa = new LoginTentativa();
        tentativa.setUsuario(usuarioBanco);
        tentativa.setSessionId(sessionId);
        tentativa.setTentativaEm(LocalDateTime.now());
        tentativa.setSucesso(senhaCorreta);

        
        if (senhaCorreta) {
            // Antes de continuar
            // MFA

            // Salva dados da tentativa na sessão
            session.setAttribute("classTentativa", tentativa);

            // Se login for bem-sucedido, apaga tentativas falhas
            loginService.deletarFalhasPorUsuario(usuarioBanco.getId());

            return "redirect:/enviar-login?destino=" + URLEncoder.encode(usuario.getEmail(), StandardCharsets.UTF_8);
        }
        
        loginTentativaRepository.save(tentativa);

        model.addAttribute("erro", "Senha incorreta.");
        return "auth/login";
    }

    
    @PostMapping("/criar-conta")
    public String criarConta(@Valid Usuario usuario, Model model, HttpSession session) {
       
        try {
            if (usuarioRepository.findByEmail(usuario.getEmail()).isPresent()) {
                model.addAttribute("erro", "Este e-mail já está cadastrado.");
                return "auth/criar_conta"; // Renderiza novamente a página de cadastro, sem redirect
            }
            
            // Criptografa a senha antes de salvar no banco
            String senhaCriptografada = passwordEncoder.encode(usuario.getSenha());
            usuario.setSenha(senhaCriptografada); // Define a senha criptografada
            
            // Salva no banco
            // Salvar depois da autenticacao do Email
            //usuarioRepository.save(usuario);

            // Salva dados do usuário na sessão
            session.setAttribute("classUsuario", usuario);

            model.addAttribute("sucesso", "Conta criada com sucesso!");
            return "redirect:/enviar?destino=" + URLEncoder.encode(usuario.getEmail(), StandardCharsets.UTF_8);

            //return "redirect:/login"; // Redireciona para a página de login após sucesso
        } catch (Exception e) {
            model.addAttribute("erro", "Ocorreu um erro inesperado ao criar a conta.");
            return "auth/criar_conta"; // Renderiza novamente a página de cadastro
        }
    }
}

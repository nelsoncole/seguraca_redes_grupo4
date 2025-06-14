package com.seguranca.redes.controller;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.seguranca.redes.model.CodigoAutenticacao;
import com.seguranca.redes.model.LoginTentativa;
import com.seguranca.redes.model.Usuario;
import com.seguranca.redes.repository.CodigoAutenticacaoRepository;
import com.seguranca.redes.repository.LoginTentativaRepository;
import com.seguranca.redes.repository.UsuarioRepository;
import com.seguranca.redes.service.EmailService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private CodigoAutenticacaoRepository codigoRepository;

    @Autowired
    private LoginTentativaRepository loginTentativaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoginController loginController;

    @Autowired
    private final PasswordEncoder hashEncoder = new BCryptPasswordEncoder();


    private String gerarCodigoDeAutenticacao() {
        Random random = new Random();
        int codigo = 100000 + random.nextInt(900000); // Gera um código de 6 dígitos
        return String.valueOf(codigo);
    }

    @GetMapping("/enviar")
    public String enviar(HttpSession session, Model model) {

        // Recupera o objeto Usuario da sessão
        Usuario usuario = (Usuario) session.getAttribute("classUsuario");
        
        if (usuario == null) {
            model.addAttribute("erro", "Ocorreu um erro inesperado.");
            return "auth/criar_conta";
        }

        String destino = usuario.getEmail();
        String codigo = gerarCodigoDeAutenticacao();
        String codigoCriptografado = hashEncoder.encode(codigo);

        // Salva o código no banco de dados
        CodigoAutenticacao codigoAutenticacao = new CodigoAutenticacao();
        codigoAutenticacao.setDestino(destino);
        codigoAutenticacao.setCodigo(codigoCriptografado);
        codigoAutenticacao.setDataGeracao(LocalDateTime.now());
        codigoRepository.save(codigoAutenticacao);

        emailService.enviarEmail(
            destino,
            "Código de Autenticação",
            "<h1>Autenticação de Conta</h1>\n" +
            "<p>Olá!</p>\n" +
            "<p>Para completar o processo de autenticação, utilize o seguinte código de verificação:</p>\n" +
            "<h2 style='text-align: center;'>" + codigo + "</h2>\n" +
            "<p>Este código é válido por 5 minutos. Não compartilhe com ninguém.</p>\n" +
            "<p>Se você não solicitou essa autenticação, por favor, ignore este e-mail.</p>\n"
        );

        model.addAttribute("destino", destino);
        model.addAttribute("sucesso", "Código enviado para o e-mail informado!");
        return "auth/auth_email"; // ou outra página de retorno
    }

    @GetMapping("/enviar-login")
    public String enviarLogin(HttpSession session, Model model) {
        // Recupera o objeto Usuario da sessão
        LoginTentativa tentativa = (LoginTentativa) session.getAttribute("classTentativa");
        
        if (tentativa == null) {
            model.addAttribute("erro", "Ocorreu um erro inesperado. Por favor, tente novamente.");
            return "auth/login";
        }
        String destino = tentativa.getUsuario().getEmail();
        String codigo = gerarCodigoDeAutenticacao();
        String codigoCriptografado = hashEncoder.encode(codigo);

        // Salva o código no banco de dados
        CodigoAutenticacao codigoAutenticacao = new CodigoAutenticacao();
        codigoAutenticacao.setDestino(destino);
        codigoAutenticacao.setCodigo(codigoCriptografado);
        codigoAutenticacao.setDataGeracao(LocalDateTime.now());
        codigoRepository.save(codigoAutenticacao);

        emailService.enviarEmail(
            destino,
            "Código de Autenticação",
            "<h1>Autenticação de Conta</h1>\n" +
            "<p>Olá!</p>\n" +
            "<p>Para completar o processo de autenticação, utilize o seguinte código de verificação:</p>\n" +
            "<h2 style='text-align: center;'>" + codigo + "</h2>\n" +
            "<p>Este código é válido por 5 minutos. Não compartilhe com ninguém.</p>\n" +
            "<p>Se você não solicitou essa autenticação, por favor, ignore este e-mail.</p>\n"
        );

        model.addAttribute("destino", destino);
        model.addAttribute("sucesso", "Código enviado para o e-mail informado!");
        return "auth/auth_login";
    }

    // Verificar email da nova conta
    @PostMapping("/verificar")
    public String verificar(@RequestParam String destino, @RequestParam String codigo,
        HttpSession session, Model model, HttpServletResponse response) {
        Optional<CodigoAutenticacao> codigoSalvoOptional = codigoRepository.findFirstByDestinoOrderByDataGeracaoDesc(destino);

        if (codigoSalvoOptional.isPresent()) {
            CodigoAutenticacao codigoSalvo = codigoSalvoOptional.get();
            
            if (codigoSalvo != null) {

                boolean valido = hashEncoder.matches(codigo, codigoSalvo.getCodigo());
                // use codigoSalvo normalmente
                if (codigoSalvo.isValido() && valido) {
                    // Código correto e dentro do prazo
                    // Remove o código após a validação
                    codigoRepository.delete(codigoSalvo);

                    try {
                        // Recupera o objeto Usuario da sessão
                        Usuario usuario = (Usuario) session.getAttribute("classUsuario");
            
                        if (usuario == null) {
                            model.addAttribute("erro", "Sessão expirada ou inválida. Por favor, refaça o cadastro.");
                            return "auth/criar_conta";
                        }
            
                        String email = usuario.getEmail();
                        // Salva o usuário no banco
                        usuarioRepository.save(usuario);
                        // Limpa a sessão após salvar, se desejar
                        session.removeAttribute("classUsuario");

                        Usuario usuarioBanco = usuarioRepository.findByEmail(email).orElse(null);

                        if (usuarioBanco == null) {
                            model.addAttribute("erro", "Usuário não encontrado.");
                            return "auth/login";
                        }

                        if(loginController.loginAuto(usuarioBanco, session, response)){
                            return "redirect:/";
                        }

                        model.addAttribute("erro", "Usuário não encontrado.");
                        return "auth/login";
            
                
                    } catch (Exception e) {
                        model.addAttribute("erro", "Erro ao salvar usuário.");
                        return "auth/criar_conta";
                    }
                }
            }
        }
        
        model.addAttribute("destino", destino);
        model.addAttribute("erro", "Código inválido ou expirado!");
        return "auth/auth_email";
    }

    @PostMapping("/verificar-login")
    public String verificarLogin(@RequestParam String codigo, HttpSession session, Model model,
        HttpServletResponse response) {

        // Recupera o objeto Usuario da sessão
        LoginTentativa tentativa = (LoginTentativa) session.getAttribute("classTentativa");
        
        if (tentativa == null) {
            model.addAttribute("erro", "Sessão expirada ou inválida. Por favor, tente novamente");
            return "auth/login";
        }

        String destino = tentativa.getUsuario().getEmail();
        Optional<CodigoAutenticacao> codigoSalvoOptional = codigoRepository.findFirstByDestinoOrderByDataGeracaoDesc(destino);

        if (codigoSalvoOptional.isPresent()) {
            CodigoAutenticacao codigoSalvo = codigoSalvoOptional.get();
            if (codigoSalvo != null) {

                boolean valido = hashEncoder.matches(codigo, codigoSalvo.getCodigo());
                // use codigoSalvo normalmente
                if (codigoSalvo.isValido() && valido) {
                    // Código correto e dentro do prazo
                    // Remove o código após a validação
                    codigoRepository.delete(codigoSalvo);

                    try {
                        
                        // Salva o login no banco
                        loginTentativaRepository.save(tentativa);

                        String sessionId = tentativa.getSessionId();
                        Long userId = tentativa.getUsuario().getId();
                        String nome = tentativa.getUsuario().getNome();
                        String email = tentativa.getUsuario().getEmail();
                        int nivel = tentativa.getUsuario().getNivelAcesso().getNivel();

                        session.setAttribute("usuarioId", userId);
                        Cookie cookie = new Cookie("usuarioId", sessionId);
                        cookie.setHttpOnly(true);
                        cookie.setSecure(false); // true se usar HTTPS, false para HTTP em desenvolvimento
                        cookie.setMaxAge(600); // Expiração de cookie em 10 minitos
                        cookie.setPath("/");
                        response.addCookie(cookie);
            
                        // Limpa a sessão após salvar, para maior seguraça
                        session.removeAttribute("classTentativa");


                        // Salvar dados de uso gerais na sessão
                        session.setAttribute("usuarioNome", nome);
                        session.setAttribute("usuarioEmail", email);
                        session.setAttribute("usuarioNivel", nivel);

                        return "redirect:/";

                    } catch (Exception e) {
                        model.addAttribute("erro", "Erro ao salvar login.");
                        return "auth/login";
                    }
                }
            }
        }
        
        model.addAttribute("destino", destino);
        model.addAttribute("erro", "Código inválido ou expirado!");
        return "auth/auth_login";
    }
}

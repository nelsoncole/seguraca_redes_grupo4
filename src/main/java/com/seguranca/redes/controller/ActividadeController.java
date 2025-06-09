package com.seguranca.redes.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.seguranca.redes.model.Arquivos;
import com.seguranca.redes.model.LoginTentativa;
import com.seguranca.redes.model.Usuario;
import com.seguranca.redes.repository.ArquivosRepository;
import com.seguranca.redes.repository.LoginTentativaRepository;
import com.seguranca.redes.repository.UsuarioRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ActividadeController {

    @Autowired
    private LoginTentativaRepository loginTentativaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ArquivosRepository arquivosRepository;

    public int verificarLogin(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        if (usuarioId == null) {
            return 1; // Redirecionar para login
        }
    
        Optional<LoginTentativa> tentativaSession = loginTentativaRepository
            .findTopBySessionIdAndSucessoTrueOrderByTentativaEmDesc(usuarioId);
    
        if (tentativaSession.isEmpty()) {
            return 1; // Sessão não tem login válido
        }
    
        Long usuarioIdDb = tentativaSession.get().getUsuario().getId();
    
        Optional<LoginTentativa> tentativaUsuario = loginTentativaRepository
            .findTopByUsuarioIdAndSucessoTrueOrderByTentativaEmDesc(usuarioIdDb);
    
        if (tentativaUsuario.isEmpty() || !usuarioId.equals(tentativaUsuario.get().getSessionId())) {
            return 2; // Sessão não é mais a ativa
        }
    
        return 0; // Sessão válida
    }
    
    @GetMapping("/notificacoes")
    public String notificacoesForm(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> "activity/notificacoes";
        };
    }

    @GetMapping("/ajuda")
    public String ajudaFrom(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> "activity/ajuda";
        };
    }

    @GetMapping("/perfil")
    public String perfilFrom(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> "activity/perfil";
        };
    }

    @GetMapping("/usuarios")
    public String usuariosFrom(
            @CookieValue(value = "usuarioId", required = false) String usuarioId,
            HttpSession session,
            Model model) {

        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> {
                String email = (String) session.getAttribute("usuarioEmail");

                if (email == null || email.isBlank()) {
                    model.addAttribute("erro", "Sessão inválida. Faça login novamente.");
                    yield "auth/login";
                }

                Usuario usuarioBanco = usuarioRepository.findByEmail(email).orElse(null);

                if (usuarioBanco == null) {
                    model.addAttribute("erro", "Usuário não encontrado.");
                    yield "auth/login";
                }

                if (usuarioBanco.getNivelAcesso() != Usuario.NivelAcesso.ADMIN && !email.equals("nelsoncole72@gmail.com")) {
                    yield "activity/violacao";
                }

                model.addAttribute("usuarios", usuarioRepository.findAll());
                yield "activity/usuarios";
            }
        };
    }


    @GetMapping("/documentos-enviar")
    public String documentosEnviarFrom(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> "activity/documentos_enviar";
        };
    }

    @GetMapping("/tarefas-pendentes")
    public String tarefasPendentesFrom(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> "activity/tarefas_pendentes";
        };
    }

    @GetMapping("/tarefas-em-andamento")
    public String tarefasEmAndamentoFrom(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> "activity/tarefas_em_andamento";
        };
    }

    @GetMapping("/tarefas-concluidas")
    public String tarefasConcluidasFrom(@CookieValue(value = "usuarioId", required = false) String usuarioId) {
        int resultado = verificarLogin(usuarioId);
        return switch (resultado) {
            case 1 -> "redirect:/login";
            case 2 -> "redirect:/logout-manual";
            default -> "activity/tarefas_concluidas";
        };
    }

    @PostMapping("/documentos/upload")
    public String uploadDocumento(@CookieValue(value = "usuarioId", required = false) String usuarioId,
            @RequestParam("documento") MultipartFile documento, RedirectAttributes redirectAttributes,
            HttpSession session) {

        Integer resultado = verificarLogin(usuarioId);
        if (resultado == 1) return "redirect:/login";
        if (resultado == 2) return "redirect:/logout-manual";

        String email = (String) session.getAttribute("usuarioEmail");

        if (email == null || email.isBlank()) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao enviar o arquivo.");
            return "redirect:/documentos-enviar";
        }


        // Aqui salva o arquivo
        try {
            // Diretório onde os arquivos serão armazenados
            String uploadDir = "uploads/";

            // Cria o diretório se não existir
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Gera um nome único para evitar sobrescritas
            String originalFilename = documento.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_SR";

            // Caminho completo do arquivo
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Url do arquivo armazenado no bunker AWS (endpoint futuro)
            // Nelson, no futuro aqui vamos armazenar o endpoint do arquivo armazenado no bunker AWS
            String url = filePath.toString();

            // Salva o arquivo no sistema de arquivos
            Files.copy(documento.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Salvar no banco: nome do arquivo, caminho, data, ID do usuário...
            Arquivos arquivo = new Arquivos();
            arquivo.setFinalidade("Geral");
            arquivo.setNomeArmazenado(uniqueFilename);
            arquivo.setNomeOriginal(originalFilename);
            arquivo.setUrl(url);
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
            arquivo.setUsuario(usuario);
            arquivosRepository.save(arquivo);

            redirectAttributes.addFlashAttribute("mensagemSucesso", "Arquivo enviado com sucesso.");

        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao enviar o arquivo.");
            return "redirect:/documentos-enviar";
        }
        


        redirectAttributes.addFlashAttribute("mensagemSucesso", "Arquivo enviado com sucesso.");
        return "redirect:/documentos-enviar";
    }

}

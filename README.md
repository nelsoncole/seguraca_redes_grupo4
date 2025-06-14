# Sistema de Segurança de Redes

Este projeto é um sistema completo de **gerenciamento de segurança de redes**, desenvolvido em **Java com Spring Boot**, com foco em:

- Controle de login com bloqueio de tentativas
- Autenticação por credenciais
- Autorização por níveis de acesso (Administrador, Cliente, etc.)
- Registro de acessos e tentativas inválidas
- Validação de URLs para impedir bypass de permissões

---

## Tecnologias Utilizadas

- Java 21+
- Spring Boot
- Spring Security
- Hibernate / JPA
- MySQL
- Maven

---

## Funcionalidades

- ✅ Registro e autenticação de usuários com diferentes níveis de acesso
- ✅ Sistema de bloqueio temporário após várias tentativas de login incorretas
- ✅ Logs de acesso e tentativas inválidas salvos no banco de dados
- ✅ Proteção contra acesso direto por URL
- ✅ Interface de administração para gerenciamento de usuários e permissões

---

## Níveis de Acesso

O sistema suporta múltiplos perfis de usuários:

- **Administrador**: Acesso total
- **Cliente**: Acesso aos seus próprios dados

---
## Como Executar o Projeto

1. Clone o repositório:

git clone https://github.com/nelsoncole/seguraca_redes_grupo4.git
cd seguraca_redes_grupo4
$mvn spring-boot:run


2. URL de Teste:

http://ec2-3-94-31-109.compute-1.amazonaws.com/

---

## Estrutura do Projeto

```text
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── seguranca/
│   │           └── redes/
│   │               ├── controller/
│   │               ├── model/
│   │               ├── repository/
│   │               └── service/
│   └── resources/
│       ├── application.properties
│       └── static/

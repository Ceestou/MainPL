# 🌟 MainPL 🌟
**Plugin gerenciador definitivo para servidores Minecraft!**  

---

## ✨ Funcionalidades Principais  

### 💬 Mensagens e Chat
- 🟢 **Mensagens personalizadas** de *join*, *quit* e *AFK* por grupo do LuckPerms.  
- 🟢 **Mensagens exclusivas** para *staffers* e *apoiadores*.  
- 🟢 Mensagens automáticas adaptadas ao **grupo principal** do jogador.

---

### 🛠️ Comandos Utilitários
- `/level [jogador]` → Gerencia níveis de jogadores (**adicionar**, **remover** ou **ver**).  
- `/fly [jogador]` → Ativa/desativa o voo para si ou para outro jogador.  
- `/spawn` → Leva jogadores diretamente ao spawn configurado.
- `/craft` → Abre a **crafting table** diretamente na mão do jogador. 🛠️  
- `/lixo` → Abre um inventário temporário para descartar itens. 🗑️  
- `/fornalha` → Abre uma **fornalha** para o jogador. 🔥  
- `/hat` → Coloca o item na mão como **chapéu**. 👒  
- `/invsee [jogador]` → Permite **visualizar o inventário** de outro jogador. 👀  
- `/luz` → Liga/desliga **visão noturna**. 🌙  
- `/anvil` → Abre uma **bigorna** para reparos e encantamentos. ⚒️   
---

#### 🔹 Comandos de Jogabilidade e Inventário
- `/speed [jogador]` → Ajusta velocidade de movimento.  
- `/feed [jogador]` → Restaura fome do jogador.  
- `/heal [jogador]` → Cura o jogador.  
- `/hat` → Coloca o item da mão como capacete.  
- `/lixo` → Abre inventário de descarte de itens.  
- `/craft` → Abre a bancada de crafting.  
- `/fornalha` → Abre uma fornalha.  
- `/luz` → Ativa/desativa visão noturna.  
- `/rename [item]` → Renomeia item na mão.
---

### 👑 Comandos Administrativos
- `/gm [modo]` → Muda o modo de jogo do jogador (`/gm2` para Criativo).  
- `/invsee [jogador]` → Abre o inventário de outro jogador. 
- `/god` → Ativa modo Deus.  
- `/ban [jogador]` → Bane jogador permanentemente.  
- `/unban [jogador]` → Remove banimento.  
- `/tempban [jogador]` → Bane temporariamente.  
- `/tempbanip [IP]` → Bane temporariamente pelo IP.  
- `/list` → Lista jogadores online.  
- `/clearchat` → Limpa o chat.  
- `/repair [item]` → Repara item na mão.  
- `/repairall` → Repara todos os itens do inventário.  
- `/lightning [jogador]` → Lança raio sobre jogador.  
- `/lightningall` → Lança raio em todos os jogadores.
- `/kick [jogador]` → Expulsa jogador (console ou in-game).  
- `/kickall` → Expulsa todos os jogadores online.
- `/receitas` → Abre menu de receitas de crafting.
- `/receitas create` → Permite criar receitas facilmente.   
- `/give [jogador] [item] [Encantamento]` → Dá item a um jogador já encantado se quiser.  
- `/giveall [item] [Encantamento]` → Dá item a todos os jogadores.  
- `/enchant [item] [encantamento] [nível]` → Aplica encantamento a item até o máximo 1000 ou quanto desejar.
- `/mortes` → Mostra mortes do jogador.  
- `/back [quantia]` → Permite voltar a locais de morte antigos.  
---

#### ✨ Comandos de Mensagens e Interação
- `/anunciar [mensagem]` → Envia mensagem global para todos.  
- `/setjoinmsg [mensagem]` → Define mensagem de entrada personalizada.  
- `/confiar [jogador]` → Dá permissão para acessar baú ou área.  
- `/acessar [jogador]` → Acessa baú de outro jogador autorizado.  
- `/rep [jogador]` → Dá reputação a um jogador.  
- `/repinfo [jogador]` → Mostra informações de reputação.
---

#### 🧶 Comandos de Personalização
- `/nick [novoNome]` → Altera o apelido do jogador.  
- `/restaurarnick` → Remove apelido e retorna ao nome original.  
- `/especial` → Acessa menu especial de comandos.  
- `/sbau` → Acessa baú especial.  
- `/escolher` → Seleciona opções de baús.
---

#### 🛑 Comandos de Configuração e Suporte
- `/mreload` → Recarrega configuração do plugin.  
- `/ajuda` → Mostra todos os comandos disponíveis totalmente editável.  
- `/auth` → Sistema de autenticação interno.  
- `/migrar` → Migra dados das homes do Essentials para o plugin.  
- `/migrarwarp` → Migra warps do Essentials para o plugin.
---

#### ⌛ Comandos de Teleporte e Localização
- `/spawn` → Teleporta ao spawn principal.  
- `/setspawn` → Define o spawn principal.  
- `/sethome [nome]` → Define uma home.  
- `/home [nome]` → Teleporta para a home.  
- `/homes` → Lista todas as homes do jogador.  
- `/delhome [nome]` → Remove uma home.  
- `/warp [nome]` → Teleporta para warp.  
- `/setwarp [nome]` → Cria warp.  
- `/delwarp [nome]` → Remove warp.  
- `/warps` → Lista todos os warps disponíveis em GUI totalmente configurável.  
- `/tpa [jogador]` → Solicita teleporte para outro jogador.  
- `/tpahere [jogador]` → Solicita teleporte de outro jogador para você.   
- `/tphere [jogador]` → Teleporta o jogador até você.  
---

### 🎁 Sistema de Apoio
- 💎 **Baús Virtuais** (até 10 por jogador).  
- Acesso via `/bau` com **GUI moderna**.  
- Efeitos sonoros e visuais premium ao abrir.  
- Proteção contra exploits e salvamento automático.  
- Sistema de liberação por grupo ou comando `/darbau`.  

---

### 🔒 Segurança
- 🔑 Sistema de **autenticação por código** exclusivo para OPs.  
- Código gerado no console a cada reinício.  
- Bloqueio de comandos até validação.

---

### ⏳ Automação & Controle
- ⏱️ **ScheduleCommands**: execução de comandos periódicos por grupo LuckPerms.  
- O tempo **continua mesmo sem jogadores online**.  
- Comandos executados automaticamente quando o tempo configurado acabar.

---

### 🖥️ Atualizações
- 🔔 Checagem automática de novas versões no GitHub.  
- Mensagem de atualização destacada no console e para OPs online.  
- Link direto para download e Discord de suporte:
  - [Releases no GitHub](https://github.com/Ceestou/MainPL/releases)  
  - [Discord Oficial](https://discord.gg/HhkTKberpQ)

---

### 📦 Instalação
1. Baixe a última versão em [Releases](https://github.com/Ceestou/MainPL/releases).  
2. Coloque o `.jar` dentro da pasta `/plugins/` do seu servidor.  
3. Reinicie ou carregue o plugin com `/reload`.  

---

### 🖌️ Observações
- Compatível com **Minecraft Java Edition**.  
- Suporta **LuckPerms** para grupos e permissões.  
- Pensado para **ser fácil de configurar** e **visualmente elegante**.  

---

> 🚀 Com o **MainPL**, seu servidor ganha controle, estilo e automação de forma **profissional**!

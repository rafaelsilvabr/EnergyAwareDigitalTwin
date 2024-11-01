# Casos de Uso

## 1. Funcionalidade: Otimizar Consumo de Energia do Dispositivo IoT

**Como** usuário\
**Eu quero** que o gêmeo digital otimize o consumo de energia do meu dispositivo IoT\
**Para que** eu possa estender a vida útil da bateria sem comprometer a coleta de dados críticos.

**Cenário: Ajuste Dinâmico da Frequência de Coleta de Dados**

**Dado que** o usuário está utilizando um dispositivo IoT para monitoramento de saúde\
**E** o gêmeo digital está analisando os padrões de uso e estado da bateria\
**Quando** o gêmeo digital detecta que os sinais vitais do usuário estão estáveis\
**Então** o sistema reduz automaticamente a frequência de coleta e transmissão de dados\
**E** notifica o usuário sobre a mudança, explicando como isso ajudará a economizar energia.

## 2. Funcionalidade: Priorizar Coleta de Dados Críticos

**Como** usuário\
**Eu quero** que o gêmeo digital priorize a coleta de dados críticos de saúde\
**Para que** eu possa manter um monitoramento eficaz mesmo com baixa energia.

**Cenário: Modo de Economia de Energia Inteligente**

**Dado que** o dispositivo IoT está com bateria baixa\
**Quando** o gêmeo digital detecta essa condição\
**Então** o sistema entra em um modo de economia de energia\
**E** prioriza a coleta apenas de dados vitais críticos, como frequência cardíaca\
**E** desativa temporariamente a coleta de dados menos críticos, como contagem de passos.

## 3. Funcionalidade: Visualizar Previsão de Duração da Bateria

**Como** usuário\
**Eu quero** visualizar uma previsão da duração da bateria do meu dispositivo IoT\
**Para que** eu possa planejar quando recarregá-lo sem interromper o monitoramento.

**Cenário: Exibir Previsão de Bateria no Dashboard**

**Dado que** o usuário está visualizando o dashboard do gêmeo digital\
**Quando** o usuário seleciona a opção de previsão de bateria\
**Então** o sistema exibe um gráfico mostrando a estimativa de duração da bateria\
**E** fornece recomendações sobre quando recarregar com base nos padrões de uso e dados críticos.

## 4. Funcionalidade: Alternar entre Dispositivos IoT

**Como** usuário\
**Eu quero** que o gêmeo digital alterne automaticamente entre dispositivos IoT disponíveis\
**Para que** eu possa manter o monitoramento contínuo enquanto economizo energia.

**Cenário: Transição Automática para Smartphone**

**Dado que** o usuário está utilizando um smartwatch para monitoramento\
**E** o gêmeo digital detecta que a bateria do smartwatch está criticamente baixa\
**Quando** o smartphone do usuário está próximo e disponível\
**Então** o sistema transfere automaticamente o monitoramento para o smartphone\
**E** notifica o usuário sobre a mudança, solicitando que recarregue o smartwatch.

## 5. Funcionalidade: Simular Cenários de Consumo de Energia

**Como** usuário\
**Eu** quero simular diferentes cenários de uso do meu dispositivo IoT\
**Para que** eu possa entender como minhas atividades afetam o consumo de energia.

**Cenário: Simular Impacto de Atividade Física Intensa**

**Dado** que o usuário planeja uma sessão intensa de exercícios\
**Quando** o usuário inicia uma simulação no gêmeo digital\
**Então** o sistema calcula e exibe o impacto estimado no consumo de energia do dispositivo\
**E** fornece recomendações sobre configurações ideais para maximizar a duração da bateria durante a atividade.

## 6. Funcionalidade: Receber Alertas de Consumo Anormal de Energia

**Como** usuário\
**Eu** quero receber alertas quando houver um consumo anormal de energia no meu dispositivo IoT\
**Para que** eu possa identificar e resolver problemas rapidamente.

**Cenário: Detectar e Notificar sobre Aplicativo com Alto Consumo**

**Dado que** o gêmeo digital está monitorando o consumo de energia do dispositivo\
**Quando** o sistema detecta um aplicativo consumindo energia de forma anormal\
**Então** o usuário recebe uma notificação identificando o aplicativo problemático\
**E** o sistema fornece opções para otimizar ou encerrar o aplicativo para economizar energia.

## 7. Diagnóstico Preditivo de Problemas Cardiovasculares

**Como** paciente monitorado por um sistema de gêmeo digital de saúde\
**Eu quero** que o sistema analise continuamente meus dados fisiológicos\
**Para que** possa identificar precocemente potenciais problemas cardiovasculares

**Cenário: Detecção de Risco Elevado de Arritmia Cardíaca**

**Dado que** o paciente está sendo monitorado pelo sistema de gêmeo digital\
**E** possui um histórico de dados fisiológicos coletados ao longo do tempo\
**Quando** o gêmeo digital detecta padrões anormais na frequência cardíaca e variabilidade da frequência cardíaca\
**Então** o sistema realiza uma análise aprofundada utilizando modelos preditivos treinados com dados populacionais\
**E** identifica um risco elevado de desenvolvimento de arritmia cardíaca\
**E** gera um alerta para o médico responsável com um relatório detalhado da análise\
**E** recomenda ao paciente, via aplicativo, que agende uma consulta de acompanhamento
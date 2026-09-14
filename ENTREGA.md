# Entrega

## Tarefa 1 — disponibilidade no `listar`

O `listar` mostrava quantos exemplares a biblioteca tem no total. O pedido era mostrar
quantos estão livres agora, sem que emprestar um exemplar de Duna faça os outros dois
sumirem da listagem.

## Como pensei

Criei um método que, para um livro, conta quantos empréstimos ativos existem para aquele
livro e subtrai do total de cópias. O que sobra é o que está na prateleira.

### Pseudocódigo

```text
availableCopies(bookId):
  livro ← achar livro no acervo
  ativos ← contar empréstimos com esse bookId
  livres ← max(0, cópias do livro − ativos)
  devolver livres

listar:
  para cada livro do acervo
    mostrar id, título, autor, gênero, livres, total
```

Preferi calcular na hora em vez de guardar um campo "disponíveis" no `Book`: o campo seria
uma segunda fonte de verdade, e qualquer empréstimo que esquecesse de atualizá-lo deixaria
o acervo mentindo. Assim `Book.copies` é o total e a lista de empréstimos é o que está fora.

O método fica no service e só devolve número, quem monta a tabela é o `Main.kt` — é a
convenção que o README pede.

## O que mudei no que já existia

- `LibraryService.availableCopies`: implementei o `TODO`.
- `Main.kt` / `showCatalog`: a coluna "exemplares" mostrava `book.copies`. Troquei por duas
  colunas, `livres` e `total`. Mostrar os dois deixa explícito o ponto do enunciado: `Duna`
  aparece como `livres = 1` e `total = 3`, e o título não desaparece da listagem.

## Tarefa 2 — `buscar <termo>`

O comando precisava procurar por título, autor ou gênero, no mesmo formato do `listar`,
sem depender de maiúsculas/minúsculas nem de acentos (`solidao` acha `Solidão`). Se não
houvesse resultado, avisar em vez de mostrar tabela vazia.

## Como pensei

A busca ficou no `LibraryService`, que devolve a lista de livros. O `Main.kt` só monta a
tela. Assim a regra não imprime nada — segue a arquitetura do projeto.

Para o termo bater com ou sem acento, normalizo o texto antes de comparar: decompõe os
caracteres (NFD), remove as marcas de acento e passa para minúsculas. Aí `contains`
funciona igual para `SARAMAGO` e `saramago`, e para `solidao` e `Solidão`.

### Pseudocódigo

```text
normalize(texto):
  decompor acentos (NFD)
  remover marcas de acento
  passar para minúsculas

search(termo):
  needle ← normalize(termo)
  se needle vazio → lista vazia
  filtrar livros onde
    normalize(título) contém needle
    OU normalize(autor) contém needle
    OU normalize(gênero) contém needle

buscar:
  se termo vazio → erro de uso
  resultados ← search(termo)
  se vazio → avisar
  senão → mesma tabela do listar
```

## O que mudei no que já existia

- `LibraryService.search` e `normalize`: implementei o TODO da busca.

- `Main.kt`: liguei o comando buscar a showSearch, no mesmo estilo do showCatalog.

## Tarefa 3 — `emprestar` e `devolver`

## Como pensei

O enunciado pedia regras (14 dias, no máximo 3, bloqueio por atraso) e motivo em
toda recusa. A dúvida era onde isso mora: se o `Main` validasse, a regra se
misturava com a tela. Mantive o padrão das tarefas anteriores — o service decide,
o `Main` só exibe.

Precisei de um jeito do service falar “deu certo” ou “não deu” sem `println`.
Criei o `ActionResult` (`Ok` / `Err`) com a mensagem pronta. Assim o `Main` fica
fino: lê os ids, chama `borrow`/`returnBook` e despacha para `Console.info` ou
`Console.error`.

Para o atraso, partimos do que o `Loan` já tinha (`borrowedAt`). Não precisei
mudar o modelo: a data limite é `borrowedAt + 14`, e atrasado é quando essa data
já passou. Os helpers `dueDate`, `isOverdue` e `activeLoansOf` concentraram isso
num lugar só — e já servem para a tarefa 4.

Na hora de saber se dava para emprestar, vi que a tarefa 1 já resolvia a
prateleira: `availableCopies`. Reaproveitei em vez de duplicar a conta
“cópias − empréstimos”. Emprestar vira `add` na lista; devolver vira `remove`.
A disponibilidade acompanha sozinha.

Ordem das validações: primeiro “existe livro/membro?”, depois “tem cópia?”,
depois “está atrasado?” e “já tem 3?”. Assim a mensagem de erro aponta o motivo
certo, sem checagens à toa.

Além do enunciado, tratei devolução sem empréstimo daquele livro+membro. Também
decidi, nas mensagens de sucesso do emprestar e do devolver, mostrar quantos
exemplares daquele título ainda restam livres — em outra linha, depois da
confirmação. Calculo isso com `availableCopies` *depois* do `add`/`remove`, para
o número já refletir a operação. A ideia foi amarrar a tarefa 3 com a 1: o usuario
vê na hora o efeito na prateleira, sem precisar rodar `listar`.

### Pseudocódigo

```text
dueDate(loan) = borrowedAt + 14 dias
atrasado(loan) = dueDate < hoje

borrow(livro, membro):
  se livro/membro não existe → Err(motivo)
  se livres ≤ 0 → Err(sem exemplar)
  se membro tem atraso → Err(bloqueado)
  se membro já tem 3 → Err(limite)
  adicionar Loan(hoje)
  Ok(prazo + livres restantes)

returnBook(livro, membro):
  se livro/membro não existe → Err(motivo)
  se não há empréstimo desse par → Err(não tem)
  remover Loan
  Ok(livres restantes)

Main: lê ids → chama service → info/erro
```

## O que mudei no que já existia

`LibraryService`: empréstimo, devolução, helpers de prazo/atraso e `ActionResult`.

`Main.kt`: emprestar / devolver ligados a `showBorrow` / `showReturn`.

`Loan` e `Library` ficaram iguais — só passamos a usar a lista mutável de empréstimos de verdade.

## Tarefa 4 — `membro <id>`

## Como pensei

O enunciado pedia os empréstimos ativos do membro, a data de devolução de cada um
e quais estavam atrasados. A maior parte disso já existia na tarefa 3: a lista do
membro (`activeLoansOf`), o prazo (`dueDate`) e o atraso (`isOverdue`). A tarefa 4
foi juntar esses pedaços numa resposta que o `Main` consegue mostrar.

Em vez de o service devolver só `List<Loan>` (aí o `Main` teria que calcular prazo
e atraso), montei um `MemberLoanView` com título, datas e flag `overdue`. Assim a
regra continua no service e o `Main` só monta a tabela — mesmo padrão das outras
tarefas.

Para membro inexistente usei um `MemberLoansResult` (`Ok` / `Err`), no mesmo
espírito do `ActionResult`. Lista vazia não é erro: o membro existe, só não tem
nada em mãos — aí o `Main` avisa com mensagem, como no `buscar` sem resultado.

A coluna `situação` (`em dia` / `atrasado`) deixa o atraso visível sem o usuario
fazer conta de cabeça. No seed, `membro 1` (Ana) já mostra o Hobbit atrasado,
o que serve de prova rápida das regras da tarefa 3.

### Pseudocódigo

```text
loansOfMember(membroId):
  se membro não existe → Err
  loans ← empréstimos ativos do membro
  para cada loan:
    montar view(título, borrowedAt, dueDate, atrasado?)
  Ok(nome do membro, lista)
  // lista vazia = Ok, não Err

membro:
  se Ok e lista vazia → "nenhum empréstimo"
  senão → tabela com coluna situação
```

## O que mudei no que já existia

`LibraryService`: `MemberLoanView`, `MemberLoansResult` e `loansOfMember`.

`Main.kt`: comando membro ligado a `showMember`.

Não mexi de novo em Loan nem nos helpers da tarefa 3 — só reutilizei.

## Bônus — `atrasados`

## Como pensei

O enunciado pedia um relatório com tudo que está atrasado, de todos os membros.
Já tinha `isOverdue`, `dueDate` e `MemberLoanView` da tarefa 4. Em vez de
recalcular prazo/atraso no `Main`, filtrei `library.loans` com `isOverdue` e
reaproveitei a montagem da view.

Para não duplicar o `map` da tarefa 4, extraí `toMemberLoanView`. O
`loansOfMember` passou a usá-lo também — só refatoração, o comportamento da
tarefa 4 ficou igual.

`MemberLoanView` não carrega o membro (no `membro <id>` o nome já vem no `Ok`).
No relatório global precisei do nome: montei um `OverdueLoanRow` com
`memberId` / `memberName` + o `MemberLoanView`. Lista vazia não é erro — o
`Main` avisa, como no `buscar` sem resultado. No seed, `atrasados` mostra o
Hobbit da Ana, o mesmo caso que `membro 1` marca como atrasado.

### Pseudocódigo

```text
overdueLoans():
  para cada loan do acervo
    se isOverdue(loan)
      incluir linha(membro + view do empréstimo)

atrasados:
  se lista vazia → avisar
  senão → tabela global de atrasados
```

## O que mudei no que já existia

`LibraryService`: `OverdueLoanRow`, `overdueLoans`, `toMemberLoanView` (e `loansOfMember` passou a chamar o helper).

`Main.kt`: comando atrasados ligado a `showOverdue`, entrada na ajuda.

## Bônus — `limite`

## Como pensei

A tarefa 3 já bloqueia quem tem 3 empréstimos, mas isso só aparecia na hora do
`emprestar`. Quis um relatório no mesmo espírito do `atrasados`: ver de uma vez
quem já está no teto e não pode pegar mais.

A diferença é o foco. `atrasados` lista **empréstimos**; aqui o objeto é o
**membro**. Reaproveitei `activeLoansOf` e a constante `MAX_LOANS_PER_MEMBER`
em vez de espalhar o número `3` de novo. Montei um `MemberAtLimitRow` com id,
nome e quantidade — o suficiente para a tabela, sem recalcular regra no `Main`.

Lista vazia não é erro: o `Main` avisa, como no `atrasados` e no `buscar`. No
seed ninguém começa com 3, então o comando só mostra alguém depois de emprestar
até o limite (e quem está atrasado não chega lá enquanto não devolver).

### Pseudocódigo

```text
membersAtLoanLimit():
  para cada membro do acervo
    count ← tamanho de activeLoansOf(membro)
    se count ≥ MAX_LOANS_PER_MEMBER
      incluir linha(id, nome, count)

limite:
  se lista vazia → avisar
  senão → tabela de membros no teto
```

## O que mudei no que já existia

`LibraryService`: `MemberAtLimitRow` e `membersAtLoanLimit`.

`Main.kt`: comando limite ligado a `showAtLimit`, entrada na ajuda.

Não mexi nas regras do `borrow` — só tornei o limite visível num relatório.

## Bônus — testes do `LibraryService`

## Como pensei

O README aponta testes de regra de negócio como primeiro bônus e já deixa um
`CommandTest` de modelo em `src/test`. Quis o mesmo estilo: poucos testes curtos,
só `kotlin.test`, sem lib externa, cobrindo o que o service decide — não a CLI.

Cada `@Test` cria uma `Library()` nova e um `LibraryService` em cima dela. Assim
um `borrow`/`returnBook` não contamina o próximo caso. Usei o seed que já vem
no acervo em vez de montar dados à mão: o custo cai e os cenários batem com o
que o app mostra no terminal (`membro 1` atrasado, Duna com cópias emprestadas).

Cinco testes, no ponto em que a regra mora:

1. `availableCopies(1)` — Duna tem 3 cópias e 2 empréstimos no seed → 1 livre.
2. `search` — `solidao` (sem acento), `SARAMAGO` (caixa) e termo inexistente.
3. `borrow(7, 1)` — Ana (membro 1) está atrasada → `Err` com “atrasada”.
4. Diego (membro 4) empresta 3 vezes e falha no 4º → limite de 3.
5. `returnBook(1, 3)` — Carla devolve Duna → `Ok` e livres sobe 1.

Não testei o `Main` nem o parse de comando de novo: o `CommandTest` já cobre o
CLI de entrada, e o bônus pedia as regras. Também não fiz helper/`@BeforeTest`
nem assert da mensagem inteira — para este tamanho de projeto isso só aumentaria
ruído.

### Pseudocódigo

```text
para cada teste:
  library ← Library()          // seed fresco
  service ← LibraryService(library)
  chamar availableCopies / search / borrow / returnBook
  assertEquals / assertTrue no resultado
```

## O que mudei no que já existia

`src/test/kotlin/biblioteca/LibraryServiceTest.kt`: arquivo novo com os cinco
testes acima.

Não alterei `LibraryService` nem o seed para “facilitar” o teste — os asserts
seguem o comportamento que já estava na entrega.

Rodar: `./gradlew test`.

## O que ficou de fora / com mais tempo
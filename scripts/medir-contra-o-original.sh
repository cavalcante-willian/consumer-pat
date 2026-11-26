#!/usr/bin/env bash
#
# Mede o legado e o sistema novo lado a lado, em HTTP de verdade, em dois processos.
#
# O legado é o original: o último commit antes da padronização, extraído do histórico para uma
# pasta à parte, compilado e iniciado com a configuração dele. O sistema novo é o desta árvore.
# Os dois sobem com a mesma memória, e o driver (ComparativoComOLegadoTeste) mede um por vez.
# Ao final os dois são parados, e o relatório fica em build/medicao/comparativo.txt.
#
# Uso:   scripts/medir-contra-o-original.sh
# Exige: JAVA_HOME apontando para o JDK 17, git, curl e tar.
#
# Variáveis opcionais:
#   QUANTIDADE      quantos consumidores cadastrar em cada sistema (padrão 50000)
#   COMMIT_ORIGINAL o commit do original (padrão 3b2388a, o último antes dos nossos PRs)
#   PORTA_LEGADO    porta do legado (padrão 18080)
#   PORTA_NOVO      porta do novo (padrão 18081)
#   MEMORIA         o -Xmx dos dois sistemas e do driver (padrão 2g)

set -euo pipefail

: "${JAVA_HOME:?Defina JAVA_HOME com o JDK 17}"

raiz="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
commit="${COMMIT_ORIGINAL:-3b2388a}"
porta_legado="${PORTA_LEGADO:-18080}"
porta_novo="${PORTA_NOVO:-18081}"
memoria="${MEMORIA:-2g}"
quantidade="${QUANTIDADE:-50000}"
destino="$raiz/build/medicao"
original="$destino/original"

pid_legado=""
pid_novo=""

parar() {
    kill ${pid_legado:+"$pid_legado"} ${pid_novo:+"$pid_novo"} 2>/dev/null || true
}
trap parar EXIT

esperar() {
    local nome="$1" url="$2"
    for _ in $(seq 1 90); do
        curl -s -o /dev/null --max-time 2 "$url" && return 0
        sleep 1
    done
    echo "O sistema $nome não respondeu em $url" >&2
    return 1
}

memoria_residual() {
    ps -o rss= -p "$1" | awk '{printf "%.0f MB", $1 / 1024}'
}

echo "== Extraindo o original ($commit) para $original"
rm -rf "$original"
mkdir -p "$original"
git -C "$raiz" archive "$commit" | tar -x -C "$original"

echo "== Compilando o original e o sistema novo"
(cd "$original" && ./gradlew bootJar -x test --console=plain -q)
(cd "$raiz" && ./gradlew bootJar -x test --console=plain -q)
jar_legado="$(ls "$original"/build/libs/*.jar | head -n 1)"
jar_novo="$(ls "$raiz"/build/libs/*.jar | grep -v -- '-plain' | head -n 1)"

echo "== Subindo o legado na porta $porta_legado e o novo na porta $porta_novo"
"$JAVA_HOME/bin/java" "-Xmx$memoria" -jar "$jar_legado" "--server.port=$porta_legado" \
>"$destino/legado.log" 2>&1 &
pid_legado=$!
"$JAVA_HOME/bin/java" "-Xmx$memoria" -jar "$jar_novo" "--server.port=$porta_novo" \
>"$destino/novo.log" 2>&1 &
pid_novo=$!
esperar legado "http://localhost:$porta_legado/consumer/consumerList"
esperar novo "http://localhost:$porta_novo/api/v1/saude"

echo "== Medindo com $quantidade consumidores em cada sistema"
(
    cd "$raiz"
    MEDICAO=1 QUANTIDADE="$quantidade" MEMORIA_DOS_SISTEMAS="-Xmx$memoria" \
    URL_LEGADO="http://localhost:$porta_legado" URL_NOVO="http://localhost:$porta_novo" \
    _JAVA_OPTIONS="-Xmx$memoria" \
    ./gradlew cleanTest test --tests '*ComparativoComOLegadoTeste' -i --console=plain
)

echo "== Memória residual dos dois processos, ao fim da medição"
{
    echo "Memória residual ao fim (com ${quantidade} consumidores e -Xmx${memoria}):"
    echo "  legado: $(memoria_residual "$pid_legado")"
    echo "  novo  : $(memoria_residual "$pid_novo")"
} | tee -a "$destino/comparativo.txt"

echo "== Relatório em $destino/comparativo.txt"

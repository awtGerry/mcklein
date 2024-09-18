#!/bin/sh

echo "Compilando proyectos..."
javac -d bin src/*.java
echo "Completado."

case "$1" in
    1|productor)
        java -cp bin ProductorConsumidor
        ;;
    2|cena)
        java -cp bin CenaFilosofos
        ;;
    *)
        echo "Uso: $0 {1,2,3}"
        exit 1
    ;;
esac

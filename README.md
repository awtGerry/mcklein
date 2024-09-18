# Proyecto de computación paralela

## Instalación
```sh
# Clona el proyecto
git clone https://github.com/awtgerry/mcklein.git
# Entra al directorio
cd mcklein
# Haz el script ejecutable
chmod +x compile.sh

# Ejecuta el script
# Para el script necesitas darle que numero quieres
# 1: ProductorConsumidor
# 2: CenaFilosofos
# Ejemplo con ProductorConsumidor:
./compile.sh 1
```

## Descripción del proyecto
Este proyecto es una implementación de los problemas clásicos para
computacion paralela. Los problemas son:
### ProductorConsumidor
El problema surge cuando el productor desea colocar un nuevo
elemento en el almacén, pero éste está totalmente ocupado. La
solución para el productor es irse a dormir, para ser despertado
cuando el consumidor ha eliminado uno o más elementos.
### CenaFilosofos

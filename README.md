# Mini IoT Architecture

This project aims to use the various skills you have acquired during the practical sessions to allow you to set up an Internet of Things architecture, such as:

  1. Developing a network protocol to establish communication between a device and a gateway.     
  2. Programming a microcontroller so that it can collect data and send it in a specific format, as well as receive data from the gateway to display on an OLED screen.     
  3. Programming a gateway so that it can receive data from a client, manipulate it, store it, and send it to the next client in a specific format.      
  4. Developing an Android application capable of connecting to a server to request data and display it, as well as sending configurations to a connected device.


## src_microbit

Ce dossier contient le code source à flasher sur deux micro:bits distincts pour permettre la communication et le traitement des données de capteurs :
1. Sensor Microbit: Ce micro:bit est équipé d'un écran OLED et de capteurs (comme un capteur de température et un accéléromètre). Il lit les données des capteurs et les envoie à un autre micro:bit (server micro:bit) via une communication radio.
2.  Server Microbit: Ce micro:bit reçoit les données envoyées par le micro:bit sensor et les transfère à un serveur Python via une connexion série (USB).

## server_python 
 
Ce serveur (server.py), exécuté sur un ordinateur , reçoit les données du micro:bit server via une connexion série. Il stocke ces données dans un fichier values.txt pour garder un historique.


## AndroidIot 
 
Une application Android sert à contrôler l'ordre d'affichage des données sur l'écran OLED connecté au micro:bit sensor. Elle pourrait également afficher les données recue du serveur Python.

## Chiffrement 
1.   Génération d'une clé de session : Un nombre aléatoire est généré et partagé entre les deux micro:bits. Ce nombre sert de clé pour le chiffrement et le déchiffrement des messages.

2. Chiffrement des données : Pour chiffrer les données, le micro:bit effectue une opération XOR entre les données à chiffrer (par exemple, les valeurs des capteurs) et la clé de session. En cryptographie, l'opération XOR est définie comme suit pour deux bits a et b :
        XOR(a, b) = 0 si a = b
        XOR(a, b) = 1 si a ≠ b
Mathématiquement, cela peut s'écrire : C = A XOR B, où A est la donnée originale, B est la clé, et C est la donnée chiffrée.

3. Déchiffrement des données : Pour déchiffrer les données, la même opération XOR est appliquée entre les données chiffrées et la clé de session. L'opération XOR a une propriété intéressante : appliquer XOR deux fois avec la même clé annule l'opération et restitue la valeur originale. Cela signifie que A = C XOR B.

4. Vérification de l'authenticité : Après déchiffrement, le micro:bit vérifie que la clé de session reçue est correcte pour s'assurer de l'authenticité du message.
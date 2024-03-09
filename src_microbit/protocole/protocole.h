#include "MicroBit.h"
#include <string>
#include <map>

/**
 * Génère un nombre aléatoire entre 1 et 1000 qui servira de terme pour l'addition afin de convenir d'une clé de session commune
 * @param microBit Micro:bit
 *
 * @return int Nombre généré aléatoirement
 */
int keyGen(MicroBit* microBit);

/**
 * Envoie le nombre généré aléatoirement par RF
 * @param microBit Micro:bit
 * @param key Nombre généré aléatoirement
 *
 * @return void
 */
void sendKey(MicroBit* microBit, int key);

/**
* Utilise les termes générés aléatoirement pour créer une clé de session commune
* @param microBit Micro:bit
* @param key1 Clé générée aléatoirement par le micro:bit
* @param key2 Clé générée aléatoirement par le micro:bit
*
* @return std::string Clé de session commune
*/
std::string computeKey(MicroBit* microBit, std::string key1, std::string key2);

/**
* Chiffre/déchiffre le texte en utilisant une clé alphanumérique
* @param texte Texte à chiffrer/déchiffrer
*
* @return std::string Texte chiffré/déchiffré
*/
std::string encrypt(const std::string &texte);

/**
 * Envoie les données à partir d'une std::string de données non chiffrées
 * @param microBit Micro:bit
 * @param sessionKey Clé de session commune
 * @param code Code de mesure (T, H, P, L, w) => w étant le code pour la fin de la transmission d'une série de mesures
 *
 * @return void
 */
void sendData(MicroBit* microBit, std::string sessionKey, char code, std::string data);

/**
 * Protocole complet d'envoi de données
 * @param microBit Micro:bit
 * @param sessionKey Clé de session commune
 * @param data Données à envoyer
 *
 * @return void
 */
void sendRf(MicroBit* microBit,std::string sessionKey, map<char, std::string> data);

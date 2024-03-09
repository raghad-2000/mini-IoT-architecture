#include "MicroBit.h"
#include "protocole/protocole.h"
#include <string>
#include <map>

MicroBit uBit;

bool session = false;

using namespace std;

string sessionKey;

// Gestion des données reçues en série
void serialDataReceived(){

    // Si la session est OK et qu'on reçoit des données, on les transmert à l'autre carte
    if (session == true) {
        ManagedString s = uBit.serial.read(uBit.serial.getRxBufferSize(), ASYNC);
        
        if (s.length() > 0)
        {
            string plain = sessionKey + " " + s.toCharArray();
            string toSend = encrypt(plain);
            uBit.radio.datagram.send(toSend.c_str());
        }
    }
}

// Gestion des données reçues en RF
void onDataReceive(MicroBitEvent)
{
    // Si la session est OK on déchiffre les données reçues sinon on met en place la session
    if (session == true){
        ManagedString s = uBit.radio.datagram.recv();

        // Dechiffrement des données
        string encryptedData = s.toCharArray();
        string decryptedData = encrypt(encryptedData);

        string rcvKey = decryptedData.substr(0, 11);

        // Test si sessionKey OK => on envoie les données en série
        if(strcmp(rcvKey.c_str(), sessionKey.c_str()) ==0) {
            string code = decryptedData.substr(12, 1);
            string data = decryptedData.substr(14);
            if (code == "w") {
                data = "\n\r";
                uBit.serial.printf(data.c_str());
            } else {
                uBit.serial.printf(code.c_str());
                uBit.serial.printf(data.c_str());
            }
        }
        
    } else {
        //key 1 received from rf
        ManagedString stringKey1 = uBit.radio.datagram.recv();

        int key2 = keyGen(&uBit);
        string key2Str = to_string(key2);

        string key2Str(s.toCharArray());
        sessionKey = computeKey(&uBit, stringKey1.toCharArray(), key2Str);
        session = true;

        uBit.sleep(1000);
        uBit.radio.datagram.send(key2Str.c_str());
    }

}

int main()
{
    // Initialise the micro:bit runtime.
    uBit.init();
    uBit.radio.enable();
    uBit.radio.setGroup(8);

    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onDataReceive);
    uBit.serial.printf("Debut\n\r");

    // Check des données reçues en série toutes les secondes
    while (1)
    {
        serialDataReceived();
        uBit.sleep(1000);
    }
}
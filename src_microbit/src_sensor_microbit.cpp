#include "MicroBit.h"
#include "ssd1306.h"
#include "protocole/protocole.h"
#include <string>
#include <map>

using namespace std;

MicroBit uBit;

// I2C
MicroBitI2C i2c(I2C_SDA0,I2C_SCL0);
MicroBitPin P0(MICROBIT_ID_IO_P0, MICROBIT_PIN_P0, PIN_CAPABILITY_DIGITAL_OUT);
ssd1306 screen(&uBit, &i2c, &P0);
// Gestion de la session
bool isSessionOk = false;
ManagedString key2;
string session;

// Gestion de l'ordre d'affichage
string order = "TXY";

// Réception de la clé 2 et gestion de l'ordre d'affichage sur l'écran
void onDataReceive(MicroBitEvent) {
    // Si on a pas de session c'est qu'on a reçu la seconde clé
    if(!isSessionOk) {
        key2 = uBit.radio.datagram.recv();
        uBit.serial.printf("Sensor key 2 received: %s\r\n", key2.toCharArray());
        isSessionOk = true;
    } else { // Sinon c'est une maj de l'ordre => déchiffrement des données
        ManagedString s = uBit.radio.datagram.recv();
        string decryptedData = encrypt(s.toCharArray());
        string rcvKey = decryptedData.substr(0, 11);

        uBit.serial.printf("Sensor data received: %s\r\n", decryptedData.c_str());

        // Test si sessionKey OK => on maj l'ordre
        if(strcmp(rcvKey.c_str(), session.c_str()) == 0) {
            uBit.serial.printf("Sensor order received: %s\r\n", decryptedData.c_str());
            order = decryptedData.substr(12);
        } else {
            uBit.serial.printf("Sensor data received but wrong key: %s\r\n", s.toCharArray());
        }
    }

}
//
// Scales the given value that is in the -1024 to 1024 range
// int a value between 0 and 4.
//
int pixel_from_g(int value)
{
    int x = 0;

    if (value > -750)
        x++;
    if (value > -250)
        x++;
    if (value > 250)
        x++;
    if (value > 750)
        x++;

    return x;
}
// Affiche les données sur l'écran et les envoie par RF
void display_rf_loop(string order) {
    // screen.clear();
    // récuperation des données capturées
    int tempInt = uBit.thermometer.getTemperature();
    int xInt = pixel_from_g(uBit.accelerometer.getX());
    int yInt = pixel_from_g(uBit.accelerometer.getY());

    // Récupération de l'ordre
    int tempOrder = order.find('T') + 1;
    int xOrder = order.find('X') + 1;
    int yOrder = order.find('Y') + 1;

    // Affichage Tmp
    ManagedString line = "Tmp:" + ManagedString((int)tempInt) + "\r\n";
    screen.display_line(tempOrder,0,line.toCharArray());
    // Affichage x
    line = "x:" + ManagedString((int)xInt) + "\r\n";
    screen.display_line(xOrder,0,line.toCharArray());
    // Affichage y
    line = "y:" + ManagedString((int)yInt) + "\r\n";
    screen.display_line(yOrder,0,line.toCharArray());
    // Update screen
    screen.update_screen();

    // Send data
    char tempChar = 'T';
    char xChar = 'X';
    char yChar = 'Y';

    map<char, string> data;

    data[tempChar] = to_string(tempInt);
    data[xChar] = to_string(xInt);
    data[yChar] = to_string(yInt);
    uBit.serial.printf("Send data\r\n");

    sendRf(&uBit, session, data);
}

int main() {

    // Init Micro:bit
    uBit.init();

    uBit.radio.enable();
    uBit.radio.setGroup(8);

    // Met l'écran à 0
    // screen.clear();
    screen.update_screen();

    // Génère la première clé
    int key1 = keyGen(&uBit);
    string key1Str = to_string(key1);

    uBit.serial.printf("Sensor key 1 generated: %d\r\n", key1);

    uBit.radio.datagram.send(key1Str.c_str());
    uBit.serial.printf("Sensor key 1 sent\r\nWaiting for key 2...\r\n");


    // Attend la clé pour initier la connection et l'ordre d'affichage si la session existe déjà
    uBit.messageBus.listen(MICROBIT_ID_RADIO, MICROBIT_RADIO_EVT_DATAGRAM, onDataReceive);
    while(!isSessionOk) {
        // uBit.display.scroll("!isSessionOk");
        
        char Text[] = "Sensor still waiting...";
        screen.display_line(0,0, Text);
        screen.update_screen();

        uBit.serial.printf("Sensor still waiting...\r\n");
        uBit.sleep(1000);
    }

    // Connection ok
    uBit.serial.printf("Sensor connection ok\r\n");
    string key2Str(key2.toCharArray());
    session = computeKey(&uBit, key1Str, key2Str);
    uBit.serial.printf("Sensor session key: %s\r\n", session.c_str());

    // Boucle de traitement
    while(1) {
        // Affichage
        uBit.serial.printf("Sensor refresh screen\r\n");
        display_rf_loop(order);
        uBit.sleep(1000);
    }
    release_fiber();
}
import threading
import socketserver
import serial
import sys
import socket
import time
import socket


# Server setup
HOST = "0.0.0.0"  # Host address (0.0.0.0 listens on all available network interfaces)  
UDP_PORT = 10000  # Port number for the UDP server
FILENAME = "values.txt"  # Name of the file for storing data (not used in this script)
clients = {}  # Dictionary to keep track of connected clients

# Class to handle multi-threaded UDP server
class ThreadedUDPServer(socketserver.ThreadingMixIn, socketserver.UDPServer):
    pass

# Class to handle incoming UDP requests
class ThreadedUDPRequestHandler(socketserver.BaseRequestHandler):
    def handle(self): #Appelée automatiquement chaque fois qu'une requête UDP arrive au serveur.
        global clients
        data = self.request[0].strip()  # Data received from the client
        client_socket = self.request[1]  # Socket object for the client
        client_address = self.client_address  # Address of the client
        # Registering a new client if not already registered
        if client_address not in clients:
            clients[client_address] = True
            print("Registered client: ", client_address)

        # Handling specific commands (TL or LT) and forwarding them to the Microbit
        # byte string est une séquence d'octets (qui peuvent ou non correspondre à des caractères ASCII) et est préfixée par b
        #if data in [b"TXY", b"YXT"]:
        decoded_data = data.decode().upper()
        if all(letter in decoded_data for letter in ["T", "X", "Y"]) and len(decoded_data) == 3:
            print(f"Order {data.decode()} received from {client_address}")
            sendCommandToMicrobit(data.decode())
            response = f"Order {data.decode()} sent to Microbit"
            sendDataToClient(response, client_address)



# Function to send data to a client via UDP
def sendDataToClient(message, client_address):
    try:
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        server_socket.sendto(message.encode(), client_address)
        server_socket.close()
    except Exception as e:
        print("Error sending data: ", e)

# Function to send a command to the Microbit via serial
def sendCommandToMicrobit(command):
    try:
        if ser.is_open:
            ser.write(command.encode())
            print(f"Order {command} sent to Microbit")
    except Exception as e:
        print("Error sending command to Microbit: ", e)

def send_data_to_android(data, android_ip, android_port):
    try:
        server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        server_socket.sendto(data.encode(), (android_ip, android_port))
        server_socket.close()
        #with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as sock:
        #    sock.sendto(data.encode(), (android_ip, android_port))
    except Exception as e:
        print("Error sending data to android: ", e)


# Serial port setup for communication with the microcontroller
SERIALPORT = "COM3"  # Serial port to which the microcontroller is connected
BAUDRATE = 115200  # Baud rate for serial communication
try:
    ser = serial.Serial(SERIALPORT, BAUDRATE, timeout=1)  # Initializing the serial port
except serial.SerialException as e:
    print(f"Error opening serial port {SERIALPORT}: {e}")
    sys.exit(1)

# Main function to start the server
if __name__ == '__main__':
    # Setting up and starting the UDP server
    server = ThreadedUDPServer((HOST, UDP_PORT), ThreadedUDPRequestHandler)
    server_thread = threading.Thread(target=server.serve_forever)
    server_thread.daemon = True  # Setting the server thread as a daemon thread
    server_thread.start()
    print("Server started at {} port {}".format(HOST, UDP_PORT))

    try:
        print('Press Ctrl-C to quit.')
        while True:
            if ser.in_waiting > 0:
                data = ser.readline().decode('utf-8').rstrip()  # Lire une ligne de données depuis le port série et la décoder
                if data != "":
                    print("Data received from micro:bit:", data)
                    send_data_to_android(data,"192.168.1.45",10000)
                    # Open the file in append mode ('a') and write the data
                    with open(FILENAME, 'a') as file:
                        file.write(data+"\n")

            
            #time.sleep(0.1)  # Petite pause pour ne pas surcharger le CPU

            # The server runs in this loop and waits for incoming data
            # Additional logic can be added here for reading from serial port and sending to clients
            pass
    except serial.SerialException as e:
        print("Erreur lors de l'ouverture du port série:", e)
    except KeyboardInterrupt:
        # Handling keyboard interrupt to shut down the server gracefully
        print("Shutting down the server...")
        server.shutdown()
        server.server_close()
        ser.close()
        sys.exit()
    finally:
        if 'ser' in locals() and ser.is_open:
            ser.close()  # S'assurer de fermer le port série à la fin
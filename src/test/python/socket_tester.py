from socket import socket
import random
import time
import string


HOST = 'localhost'
PORT = 5060


if __name__ == '__main__':
    sock = socket()
    sock.connect((HOST, PORT))
    while True:
        data = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(random.randint(32, 256)))
        print data
        sock.sendall(data+'\n')
        time.sleep(0.005)

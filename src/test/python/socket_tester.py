from socket import socket
import random
import time
import string
import argparse


HOST = 'localhost'
PORT = 5060


if __name__ == '__main__':

    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--size')

    args = parser.parse_args()

    size = random.randint(32, 256)
    if args.size:
        size = int(args.size)

    sock = socket()
    sock.connect((HOST, PORT))
    while True:
        data = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(size))
        print data
        sock.sendall(data+'\n')
        time.sleep(0.005)

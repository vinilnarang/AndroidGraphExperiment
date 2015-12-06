import random
import signal
import time
import threading
import SocketServer
import sys

class ThreadedEchoStockPriceHandler(SocketServer.BaseRequestHandler):

    def handle(self):
        # Echo the back to the client
        data = self.request.recv(1024)
        stock_base_price = 45.0
        max_dev = 0.3
        print "Starting new Thread"
	self.request.send('HTTP/1.0 200 OK\r\n')
	self.request.send('Content-Type: text/html\r\n\r\n')
	while 1:
            dev_plus = max_dev * random.random()            
            dev_minus = max_dev * random.random()
            best_buy_price = stock_base_price - dev_minus
            best_buy_quantity = random.randint(10,50)
            best_sell_price = stock_base_price + dev_plus
            best_sell_quantity = random.randint(10,50)
            response = '%d , %f,  %f , %d , %f , %d \n' % (time.time(), stock_base_price,  best_buy_price, best_buy_quantity, best_sell_price, best_sell_quantity)
	    try :
                self.request.send(response)
            except :
                print "Closing connection"
                return 
            stock_base_price = (best_buy_price * best_buy_quantity + best_sell_price * best_sell_quantity)/(best_sell_quantity + best_buy_quantity)
            time.sleep(1)
            # return

class ThreadedEchoStockPriceServer(SocketServer.ThreadingMixIn, SocketServer.TCPServer):
    pass

def signal_handler(signal, frame):
        print('You pressed Ctrl+C!')        
        sys.exit(0)        

if __name__ == '__main__':
    import socket
    import threading

    address = ("0.0.0.0", 48129) # let the kernel give us a port
    server = ThreadedEchoStockPriceServer(address, ThreadedEchoStockPriceHandler)
    ip, port = server.server_address # find out what port we were given
    print "Server address is " + str(ip) + ", " + str(port)
    t = threading.Thread(target=server.serve_forever)
    t.setDaemon(True) # don't hang on exit
    t.start()
    print 'Server loop running in thread:', t.getName()
    signal.signal(signal.SIGINT, signal_handler)
    while 1:
        pass    
    s.close()
    server.socket.close()

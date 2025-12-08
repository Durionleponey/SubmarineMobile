# main.py - Pico W - LCD1602 4-bit + serveur HTTP pour Submarine + bouton reset
from machine import Pin
from time import sleep
import network
import socket

# ----------------- CONFIG WIFI (⚠️ à adapter) -----------------
WIFI_SSID = "Proximus-Home-EDC0"
WIFI_PASSWORD = "w9x5k2b2f2jnj"

# ----------------- CONFIG PINs -----------------
# LCD 4-bit pins (RS, E, D4, D5, D6, D7)
LCD_RS = 10
LCD_E  = 11
LCD_D4 = 12
LCD_D5 = 13
LCD_D6 = 14
LCD_D7 = 15

LED_PIN = 2        # LED d'alerte
BTN_PIN = 16       # Bouton "reset / probleme resolu"

# ----------------- LCD 1602 (4-bit) utils -----------------
class LCD1602:
    def __init__(self, rs, e, d4, d5, d6, d7):
        self.rs = Pin(rs, Pin.OUT)
        self.e  = Pin(e, Pin.OUT)
        self.data = [
            Pin(d4, Pin.OUT),
            Pin(d5, Pin.OUT),
            Pin(d6, Pin.OUT),
            Pin(d7, Pin.OUT)
        ]
        self.init_lcd()

    def _pulse(self):
        self.e.value(1)
        sleep(0.001)
        self.e.value(0)
        sleep(0.001)

    def _write_nibble(self, nibble):
        for i in range(4):
            self.data[i].value((nibble >> i) & 1)
        self._pulse()

    def _write_byte(self, byte, rs):
        self.rs.value(rs)
        self._write_nibble(byte >> 4)
        self._write_nibble(byte & 0x0F)
        sleep(0.002)

    def command(self, cmd):
        self._write_byte(cmd, 0)

    def write_char(self, ch):
        self._write_byte(ord(ch), 1)

    def write(self, text):
        for c in text:
            self.write_char(c)

    def clear(self):
        self.command(0x01)
        sleep(0.002)

    def set_cursor(self, line, pos):
        # line: 0 or 1, pos: 0..15
        addr = (0x80 + (0x40 * line) + pos)
        self.command(addr)

    def init_lcd(self):
        sleep(0.05)
        self.rs.value(0)
        self._write_nibble(0x3)
        sleep(0.005)
        self._write_nibble(0x3)
        sleep(0.001)
        self._write_nibble(0x3)
        sleep(0.001)
        self._write_nibble(0x2)  # 4-bit
        sleep(0.002)
        self.command(0x28)  # 4-bit, 2 lignes
        self.command(0x0C)  # display ON, cursor OFF
        self.command(0x06)  # auto-increment
        self.clear()

# ----------------- FONCTIONS D'AFFICHAGE -----------------

lcd = LCD1602(LCD_RS, LCD_E, LCD_D4, LCD_D5, LCD_D6, LCD_D7)
led = Pin(LED_PIN, Pin.OUT)
btn = Pin(BTN_PIN, Pin.IN, Pin.PULL_UP)

def show_boot():
    """État au démarrage / après reset manuel."""
    lcd.clear()
    lcd.set_cursor(0, 0)
    lcd.write("Submarine pret ")
    lcd.set_cursor(1, 0)
    lcd.write("En attente...  ")
    led.value(0)

def lcd_show_status():
    """État 'aucune alerte / total 0' comme dans ton ancienne idée."""
    lcd.clear()
    lcd.set_cursor(0, 0)
    lcd.write("Aucune alerte   ")
    lcd.set_cursor(1, 0)
    lcd.write("Total: 0        ")
    led.value(0)

def show_resolved_then_status():
    """Quand tu appuies sur le bouton GP16 :
       - affiche 'Probleme resolu'
       - puis 'Aucune alerte / Total: 0'
    """
    print("[+] Clear: probleme resolu via bouton")
    lcd.clear()
    lcd.set_cursor(0, 0)
    lcd.write("Probleme resolu ")
    lcd.set_cursor(1, 0)
    lcd.write("Merci admin    ")
    led.value(0)
    sleep(1)
    lcd_show_status()

def show_thanks():
    lcd.clear()
    lcd.set_cursor(0, 0)
    lcd.write("Merci admin !  ")
    lcd.set_cursor(1, 0)
    lcd.write("Submarine OK   ")
    led.value(0)

def show_alert():
    lcd.clear()
    lcd.set_cursor(0, 0)
    lcd.write("!!! ALERTE !!! ")
    lcd.set_cursor(1, 0)
    lcd.write("Voir mail     ")
    led.value(1)

# ----------------- WIFI + SERVEUR HTTP -----------------

def connect_wifi():
    wlan = network.WLAN(network.STA_IF)
    wlan.active(True)
    if not wlan.isconnected():
        print("Connexion au WiFi...", WIFI_SSID)
        wlan.connect(WIFI_SSID, WIFI_PASSWORD)
        while not wlan.isconnected():
            sleep(0.5)
            print(".", end="")
    print("\nConnecte !")
    print("Config reseau:", wlan.ifconfig())
    return wlan

def start_http_server():
    addr = socket.getaddrinfo("0.0.0.0", 8080)[0][-1]
    s = socket.socket()
    s.bind(addr)
    s.listen(1)
    s.settimeout(0.1)  # ⬅️ non bloquant pour pouvoir lire le bouton
    print("Serveur HTTP sur", addr)
    print("Endpoints :")
    print("  POST /lcd/thank-admin")
    print("  POST /lcd/alert")
    return s

def handle_request(conn):
    try:
        # Timeout de 5 secondes max pour lire la requête
        conn.settimeout(5)
        req = conn.recv(1024)
        if not req:
            return

        # première ligne : "POST /xxx HTTP/1.1"
        first_line = req.split(b"\r\n", 1)[0]
        parts = first_line.split(b" ")
        if len(parts) < 2:
            path = b"/"
        else:
            path = parts[1]

        print("Requete path:", path)

        if path == b"/lcd/thank-admin":
            show_thanks()
            body = b'{"success": true, "message": "thanks displayed"}'
        elif path == b"/lcd/alert":
            show_alert()
            body = b'{"success": true, "message": "alert displayed"}'
        else:
            body = b'{"success": false, "error": "unknown path"}'

        conn.send(b"HTTP/1.1 200 OK\r\n")
        conn.send(b"Content-Type: application/json\r\n")
        conn.send(b"Content-Length: " + str(len(body)).encode() + b"\r\n")
        conn.send(b"Connection: close\r\n")
        conn.send(b"\r\n")
        conn.send(body)

    except OSError as e:
        # <-- C'est ça qui évite que tout plante
        print("Erreur socket dans handle_request:", e)

    finally:
        try:
            conn.close()
        except:
            pass

# ----------------- MAIN -----------------

show_boot()
wlan = connect_wifi()
server = start_http_server()

while True:
    # 1) Essayer d'accepter une requête HTTP (non bloquant)
    try:
        conn, addr = server.accept()
    except OSError:
        conn = None

    if conn:
        print("Client connecte :", addr)
        handle_request(conn)

    # 2) Lire le bouton GP16
    if btn.value() == 0:  # appui (pull-up)
        show_resolved_then_status()
        # attendre que le bouton soit relâché pour éviter les multi-triggers
        while btn.value() == 0:
            sleep(0.05)

    # petite pause
    sleep(0.02)

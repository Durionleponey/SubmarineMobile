# main.py - Pico W - LCD1602 4-bit + bouton CLEAR + LED d'alerte
from machine import Pin
from time import sleep, ticks_ms

# ----------------- CONFIG PINs (adapte si nécessaire) -----------------
# LCD 4-bit pins (RS, E, D4, D5, D6, D7)
LCD_RS = 10
LCD_E  = 11
LCD_D4 = 12
LCD_D5 = 13
LCD_D6 = 14
LCD_D7 = 15

LED_PIN = 2        # LED d'alerte
BTN_PIN = 16       # Bouton reset (utiliser Pin.PULL_UP)

# Intervalle d'alerte simulée (ms) - remplace par réception réseau en prod
ALERT_INTERVAL_MS = 10000  # 10s

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
        # Enable pulse
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
        # high nibble then low nibble
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
        # Init sequence for 4-bit mode
        sleep(0.05)
        # send function set (special sequence to init 4-bit)
        self.rs.value(0)
        # send 0x33 then 0x32 as nibble sequence
        self._write_nibble(0x3)
        sleep(0.005)
        self._write_nibble(0x3)
        sleep(0.001)
        self._write_nibble(0x3)
        sleep(0.001)
        self._write_nibble(0x2)  # set to 4-bit
        sleep(0.002)
        # function set: 4-bit, 2 lines, 5x8 dots
        self.command(0x28)
        # display on, cursor off, blink off
        self.command(0x0C)
        # entry mode set: increment, no shift
        self.command(0x06)
        self.clear()

# ----------------- INITIALISATIONS -----------------
lcd = LCD1602(LCD_RS, LCD_E, LCD_D4, LCD_D5, LCD_D6, LCD_D7)
led = Pin(LED_PIN, Pin.OUT)
btn = Pin(BTN_PIN, Pin.IN, Pin.PULL_UP)

# Etat
alert_count = 0
last_alert_time = ticks_ms()
last_btn_state = btn.value()
btn_debounce_time = 0

def lcd_show_status():
    """Affiche sur le LCD : ligne1 = état, ligne2 = compteur"""
    lcd.clear()
    if alert_count == 0:
        lcd.set_cursor(0, 0)
        lcd.write("Aucune alerte   ")  # 16 chars
        lcd.set_cursor(1, 0)
        lcd.write("Total: 0        ")
    else:
        lcd.set_cursor(0, 0)
        lcd.write("!! ALERTE !!    ")
        lcd.set_cursor(1, 0)
        # afficher "Total: XX" (jusqu'à 3 chiffres)
        txt = "Total: {:d}".format(alert_count)
        lcd.write(txt + " " * (16 - len(txt)))

    # LED selon état
    led.value(1 if alert_count > 0 else 0)

def clear_alerts():
    global alert_count
    alert_count = 0
    print("[+] Clear: problèmes résolus")
    lcd.clear()
    lcd.set_cursor(0, 0)
    lcd.write("Probleme resolu ")
    lcd.set_cursor(1, 0)
    lcd.write("Total: 0        ")
    led.value(0)
    # garder l'affichage résolu 1s puis revenir au statut normal
    sleep(1)
    lcd_show_status()

# Affichage initial
lcd.set_cursor(0,0)
lcd.write("Systeme pret     ")
lcd.set_cursor(1,0)
lcd.write("Total: 0         ")
led.value(0)
print("Systeme pret. Bouton sur GP{}, LED sur GP{}".format(BTN_PIN, LED_PIN))

# ----------------- BOUCLE PRINCIPALE (non bloquante) -----------------
while True:
    now = ticks_ms()

    # --- génération d'alerte simulée (remplacer par réseau / interrupt) ---
    if now - last_alert_time >= ALERT_INTERVAL_MS:
        last_alert_time = now
        alert_count += 1
        print("[!] Nouvelle alerte (count {})".format(alert_count))
        lcd_show_status()

    # --- gestion bouton avec debounce (falling edge detection) ---
    current = btn.value()
    if current != last_btn_state:
        # changement d'état détecté -> démarrer debounce
        btn_debounce_time = now
        last_btn_state = current

    else:
        # si stable depuis >50ms et bouton appuyé (valeur 0)
        if (now - btn_debounce_time) > 50 and current == 0:
            # on a un appui stable -> actionner clear
            clear_alerts()
            # attendre que l'utilisateur relâche le bouton (évite multi-trigger)
            while btn.value() == 0:
                sleep(0.05)

    # petite pause pour éviter boucle trop serrée
    sleep(0.02)


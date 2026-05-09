import numpy as np
import soundfile as sf
import os
from PIL import Image, ImageDraw

# ============================================
# ЗВУКИ
# ============================================
os.makedirs("src/main/resources/assets/dronemod/sounds", exist_ok=True)

sample_rate = 44100

def normalize(audio, peak=0.8):
    max_val = np.max(np.abs(audio))
    if max_val > 0:
        audio = audio / max_val * peak
    return audio

# 1. drone_idle.ogg — тихое жужжание
duration = 3.0
t = np.linspace(0, duration, int(sample_rate * duration), endpoint=False)
idle = 0.3 * np.sin(2 * np.pi * 180 * t)
idle += 0.05 * np.sin(2 * np.pi * 220 * t)
# Небольшой шум
noise = 0.02 * np.random.randn(len(t))
idle += noise
# Плавные fade in/out
fade_len = int(0.05 * sample_rate)
idle[:fade_len] *= np.linspace(0, 1, fade_len)
idle[-fade_len:] *= np.linspace(1, 0, fade_len)
# Модуляция амплитуды для реалистичности
modulation = 1.0 + 0.1 * np.sin(2 * np.pi * 3.5 * t)
idle *= modulation
idle = normalize(idle, 0.4)
sf.write("src/main/resources/assets/dronemod/sounds/drone_idle.ogg", idle, sample_rate, format='OGG', subtype='VORBIS')
print("Generated drone_idle.ogg")

# 2. drone_fly.ogg — активный полёт
duration = 3.0
t = np.linspace(0, duration, int(sample_rate * duration), endpoint=False)
fly = 0.5 * np.sin(2 * np.pi * 180 * t)
fly += 0.35 * np.sin(2 * np.pi * 360 * t)
fly += 0.15 * np.sin(2 * np.pi * 540 * t)
fly += 0.1 * np.sin(2 * np.pi * 720 * t)
noise = 0.06 * np.random.randn(len(t))
fly += noise
modulation = 1.0 + 0.15 * np.sin(2 * np.pi * 7 * t) + 0.05 * np.sin(2 * np.pi * 13 * t)
fly *= modulation
fade_len = int(0.05 * sample_rate)
fly[:fade_len] *= np.linspace(0, 1, fade_len)
fly[-fade_len:] *= np.linspace(1, 0, fade_len)
fly = normalize(fly, 0.7)
sf.write("src/main/resources/assets/dronemod/sounds/drone_fly.ogg", fly, sample_rate, format='OGG', subtype='VORBIS')
print("Generated drone_fly.ogg")

# 3. drone_crash.ogg — крэш
duration = 1.5
t = np.linspace(0, duration, int(sample_rate * duration), endpoint=False)
# Нисходящий тон
freq_start = 400
freq_end = 60
freqs = np.linspace(freq_start, freq_end, len(t))
phase = np.cumsum(2 * np.pi * freqs / sample_rate)
crash = 0.6 * np.sin(phase)
# Белый шум с затуханием
noise = 0.8 * np.random.randn(len(t))
envelope = np.exp(-3.0 * t / duration)
noise *= envelope
crash += noise
# Низкочастотный удар в начале
impact_dur = int(0.1 * sample_rate)
impact_t = np.linspace(0, 0.1, impact_dur, endpoint=False)
impact = np.zeros(len(t))
impact[:impact_dur] = 0.9 * np.sin(2 * np.pi * 50 * impact_t) * np.exp(-30 * impact_t)
crash += impact
# Общий envelope
crash *= np.exp(-1.5 * t / duration)
fade_len = int(0.01 * sample_rate)
crash[:fade_len] *= np.linspace(0, 1, fade_len)
crash[-fade_len:] *= np.linspace(1, 0, fade_len)
crash = normalize(crash, 0.85)
sf.write("src/main/resources/assets/dronemod/sounds/drone_crash.ogg", crash, sample_rate, format='OGG', subtype='VORBIS')
print("Generated drone_crash.ogg")

# ============================================
# ТЕКСТУРЫ
# ============================================
os.makedirs("src/main/resources/assets/dronemod/textures/item", exist_ok=True)
os.makedirs("src/main/resources/assets/dronemod/textures/entity", exist_ok=True)

# --- Текстура предмета дрона (16x16) ---
img = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# Корпус дрона (вид сверху)
d.rectangle([5, 6, 10, 9], fill=(80, 80, 90, 255))  # Тёмный корпус
d.rectangle([6, 7, 9, 8], fill=(50, 50, 60, 255))   # Центр

# Лучи (arms)
d.line([3, 4, 5, 6], fill=(100, 100, 110, 255), width=1)
d.line([10, 6, 12, 4], fill=(100, 100, 110, 255), width=1)
d.line([3, 11, 5, 9], fill=(100, 100, 110, 255), width=1)
d.line([10, 9, 12, 11], fill=(100, 100, 110, 255), width=1)

# Пропеллеры (точки на концах)
for px, py in [(2, 3), (13, 3), (2, 12), (13, 12)]:
    d.ellipse([px-1, py-1, px+1, py+1], fill=(180, 180, 200, 200))

# LED индикатор
d.point((7, 7), fill=(0, 255, 0, 255))
d.point((8, 8), fill=(255, 0, 0, 255))

img.save("src/main/resources/assets/dronemod/textures/item/drone_item.png")
print("Generated drone_item.png")

# --- Текстура планшета (16x16) ---
img2 = Image.new('RGBA', (16, 16), (0, 0, 0, 0))
d2 = ImageDraw.Draw(img2)

# Корпус планшета
d2.rectangle([3, 2, 12, 13], fill=(40, 40, 45, 255))         # Рамка
d2.rectangle([4, 3, 11, 12], fill=(30, 30, 35, 255))          # Внутренняя рамка
d2.rectangle([5, 4, 10, 11], fill=(20, 60, 30, 255))          # Экран (зелёный тон)

# Полоски на экране (интерфейс)
d2.line([5, 6, 10, 6], fill=(40, 120, 50, 255))
d2.line([5, 8, 10, 8], fill=(40, 120, 50, 255))

# Точка в центре экрана (курсор)
d2.point((7, 7), fill=(100, 255, 100, 255))
d2.point((8, 7), fill=(100, 255, 100, 255))

# Кнопка внизу
d2.rectangle([7, 12, 8, 12], fill=(70, 70, 80, 255))

img2.save("src/main/resources/assets/dronemod/textures/item/tablet_item.png")
print("Generated tablet_item.png")

# --- Текстура entity дрона (64x32) ---
img3 = Image.new('RGBA', (64, 32), (0, 0, 0, 0))
d3 = ImageDraw.Draw(img3)

# Корпус (основная часть текстуры)
# Верх корпуса
d3.rectangle([0, 0, 15, 7], fill=(70, 70, 80, 255))
# Бока
d3.rectangle([0, 8, 15, 15], fill=(55, 55, 65, 255))
# Низ
d3.rectangle([16, 0, 31, 7], fill=(50, 50, 60, 255))

# Лопасти
d3.rectangle([32, 0, 47, 3], fill=(160, 160, 180, 200))
d3.rectangle([48, 0, 63, 3], fill=(160, 160, 180, 200))
d3.rectangle([32, 4, 47, 7], fill=(160, 160, 180, 200))
d3.rectangle([48, 4, 63, 7], fill=(160, 160, 180, 200))

# Моторы
for bx in [34, 50, 34, 50]:
    d3.rectangle([bx, 8, bx+3, 11], fill=(90, 90, 100, 255))

# LED
d3.rectangle([6, 2, 8, 4], fill=(0, 200, 0, 255))
d3.rectangle([10, 2, 12, 4], fill=(200, 0, 0, 255))

# Детали
for x in range(0, 16, 4):
    d3.line([x, 15, x, 8], fill=(45, 45, 55, 255))

# Сетка на лопастях
for bx_start in [32, 48]:
    for x in range(bx_start, bx_start + 16, 2):
        d3.line([x, 0, x, 7], fill=(140, 140, 160, 180))

img3.save("src/main/resources/assets/dronemod/textures/entity/drone_entity.png")
print("Generated drone_entity.png")

# --- GUI текстура планшета (256x256) ---
os.makedirs("src/main/resources/assets/dronemod/textures/gui", exist_ok=True)
img4 = Image.new('RGBA', (256, 256), (0, 0, 0, 0))
d4 = ImageDraw.Draw(img4)

# Фон планшета
d4.rectangle([10, 10, 245, 245], fill=(30, 30, 35, 255), outline=(60, 60, 70, 255), width=2)

# Экран
d4.rectangle([20, 20, 235, 160], fill=(15, 40, 20, 255), outline=(40, 100, 50, 255))

# Сетка на экране
for x in range(20, 236, 20):
    d4.line([x, 20, x, 160], fill=(25, 60, 30, 200))
for y in range(20, 161, 20):
    d4.line([20, y, 235, y], fill=(25, 60, 30, 200))

# Перекрестие в центре экрана
cx, cy = 127, 90
d4.line([cx-8, cy, cx+8, cy], fill=(0, 255, 0, 255), width=1)
d4.line([cx, cy-8, cx, cy+8], fill=(0, 255, 0, 255), width=1)

# Кнопки WASD область
# W
d4.rectangle([110, 170, 145, 190], fill=(50, 50, 60, 255), outline=(80, 80, 90, 255))
# A
d4.rectangle([70, 195, 105, 215], fill=(50, 50, 60, 255), outline=(80, 80, 90, 255))
# S
d4.rectangle([110, 195, 145, 215], fill=(50, 50, 60, 255), outline=(80, 80, 90, 255))
# D
d4.rectangle([150, 195, 185, 215], fill=(50, 50, 60, 255), outline=(80, 80, 90, 255))

# UP / DOWN
d4.rectangle([200, 170, 235, 190], fill=(50, 80, 50, 255), outline=(80, 120, 80, 255))
d4.rectangle([200, 195, 235, 215], fill=(80, 50, 50, 255), outline=(120, 80, 80, 255))

# Индикаторы
d4.rectangle([20, 220, 120, 240], fill=(20, 20, 25, 255), outline=(60, 60, 70, 255))
d4.rectangle([130, 220, 235, 240], fill=(20, 20, 25, 255), outline=(60, 60, 70, 255))

img4.save("src/main/resources/assets/dronemod/textures/gui/tablet_gui.png")
print("Generated tablet_gui.png")

print("\nAll assets generated successfully!")

import json
import matplotlib.pyplot as plt
with open("1573348918599.json") as json_file:
    datas = json.load(json_file)

accelerometer = {}
gyroscope = {}
light = {}
pressure = {}
magnetic = {}
ans = []

def switch(type_,key,value):
    if type_ == 'light':
        light[key] = value
    elif type_ == 'gyroscope':
        gyroscope[key] = value
    elif type_ == 'magnetic':
        magnetic[key] = value
    elif type_ == 'pressure':
        pressure[key] = value
    elif type_ == 'accelerometer':
        accelerometer[key] = value
    else:
        print("type not found")

def create_dict():
    for data in datas:
        for key,val in data.items():
            if key == 'time':
                time = val
            elif key != 'time':
                for type_,val_ in val.items():
                    print(time)
                    switch(type_,time,val_)
create_dict()
# print(accelerometer)

import numpy as np

fig, ax = plt.subplots()
# for time,value in accelerometer.items():
for time,value in gyroscope.items():
    a=ax.scatter(time/1000,value*5,label="gyro",c="blue",s=10)
for time,value in magnetic.items():
    b=ax.scatter(time/1000,value,label="mag",c="green",s=10)
for time,value in light.items():
    c = ax.scatter(time/1000,value,label="light",c="orange",s=10)
for time,value in pressure.items():
    d= ax.scatter(time/1000,value-1000,label="pressure",c="red",s=10)
for time,value in accelerometer.items():
    e = ax.scatter(time/1000,value*5,label="acc",c="purple",s=10)

labels = ['gyro','mag','light','pressure','acc']
ax.set_xlabel('Time')
ax.set_ylabel('Value')
ax.set_title('Sensor Data')
ax.grid(True)
# x = np.arange(0,15,1)
# y = np.sin(x)
# c = np.random.randint(1, 15, size=15)
# s = np.random.randint(1, 15, size=15)
# scatter = ax.scatter()
# ax.legend(labels)
ax.legend(handles=[a,b,c,d,e])
plt.show()
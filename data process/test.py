import json
import sys
import matplotlib.pyplot as plt
# with open(sys.argv[1]) as json_file:
with open("./test/TEST_20191120_160740.json") as json_file:
    datas = json.load(json_file)



accelerometer = []
gyroscope = []
light = []
pressure = []
magnetic = []
ans = []





def switch(type_,value):
    if type_ == 'light':
        light.append(value)
    elif type_ == 'gyroscope':
        gyroscope.append(value)
    elif type_ == 'magnetic':
        magnetic.append(value)
    elif type_ == 'pressure':
        pressure.append(value)
    elif type_ == 'accelerometer':
        accelerometer.append(value)
    else:
        print("type not found")

def create_dict():
    for data in datas[-20:]:
        for key,val in data.items():
                switch(key,val)

create_dict()
# print(accelerometer)

import numpy as np

fig, ax = plt.subplots()
# for time,value in accelerometer.items():
a=ax.plot(light,label="light",c="blue",linewidth=3)
# b=ax.plot(pressure,label="pressure",c="red",linewidth=3)
# c=ax.plot(gyroscope,label="gyro",c="green",linewidth=5)
# d=ax.plot(accelerometer,label="accelerometer",c="pink",linewidth=3)
# e=ax.plot(magnetic,label="magnetic",c="purple",linewidth=3)



labels = ['gyro','mag','light','pressure','acc']
ax.set_xlabel('Points')
ax.set_ylabel('Timestamp')
# ax.set_title(sys.argv[2])
ax.grid(True)
# x = np.arange(0,15,1)
# y = np.sin(x)
# c = np.random.randint(1, 15, size=15)
# s = np.random.randint(1, 15, size=15)
# scatter = ax.scatter()
# ax.legend(labels)
# ax.legend(handles=[a,b,c,d,e])
plt.show()
'''
Created on Apr 8, 2014

@author: czar
'''
from os import listdir, makedirs
from os.path import isfile, isdir, exists
import h5py
import hdf5_getters
import matplotlib.pyplot as plt
import numpy as np
import sys

def traverse_dir(root):
    count=0
    if not isfile(root):
        for f in listdir(root):
            traverse_dir(root+"/"+f)
            ++count
            if count == 2:
                break
    else:
        h5 = hdf5_getters.open_h5_file_read(root)
        tags = hdf5_getters.get_artist_mbtags(h5)
        print tags.keys()
        for genre in genreDict.keys():
            if genre in tags:
                genreDict[genre].append(root)
        h5.close()
        
dataPath = sys.argv[1]
minGenreSize = int(sys.argv[2])
maxGenreSize = int(sys.argv[3])
genreKeys = [sys.argv[i+4] for i in range(len(sys.argv)-4)]
genreDict = {x:[] for x in genreKeys}

traverse_dir(dataPath)
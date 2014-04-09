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
        for genre in genreDict.keys():
             if genre in tags:
                 genreDict[genre].append(root)
        h5.close()
genreDict = {'rock':[],'pop':[],'country':[], 'hip-hop':[], 'punk':[]}
print [len(genreDict[x]) for x in genreDict.keys()]
traverse_dir(sys.argv[1])
print [len(genreDict[x]) for x in genreDict.keys()]

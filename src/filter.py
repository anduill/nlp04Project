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
import json

def traverse_dir(root):
    if not isfile(root):
        for f in listdir(root):
            print root + "/" + f
            traverse_dir(root + "/" + f)
    else:
        h5 = hdf5_getters.open_h5_file_read(root)
        numOfSongs = hdf5_getters.get_num_songs(h5)
        for index in range(numOfSongs):
            tags = hdf5_getters.get_artist_mbtags(h5,index)
            artist = hdf5_getters.get_artist_name(h5,index)
            songName = hdf5_getters.get_title(h5,index)
            segmentTimbre = hdf5_getters.get_segments_timbre(h5,index)
            segmentPitches = hdf5_getters.get_segments_pitches(h5,index)
            if tags.size ==0 or not artist or not songName or segmentTimbre.size == 0 or segmentPitches.size ==0:
                h5.close()
                continue
            for genre in genreDict.keys():
                if len(genreDict[genre]) == maxGenreSize:
                    continue
                if genre in tags:
                    genreDict[genre].append((genre,artist, songName, segmentPitches.tolist(), segmentTimbre.tolist()))
        h5.close()
        
def write():
    makedirs(outputFileName)
    for genre in genreDict.keys():
        data = [ { 'genre':x[0], 'artist_name':x[1], 'song_title':x[2], 'segments_pitches':x[3],'segments_timbre':x[4] } for x in genreDict[genre]]
        if len(data) < minGenreSize:
            continue
        with open(outputFileName+"/"+genre+".json", 'w') as outfile:
            json.dump(data, outfile)
            
if len(sys.argv) < 6:
    print "ERROR - Parameters Missing"
    print "  <dataPath> <outputPath> <minGenreSize> <maxGenreSize> <genre>+"
    print "  MSC/DATA/PATH/ OUTPUT/PATH 1 10 pop country bachata"
    sys.exit(0)
dataPath = sys.argv[1]
outputFileName = sys.argv[2]
minGenreSize = int(sys.argv[3])
maxGenreSize = int(sys.argv[4])
genreKeys = [sys.argv[i + 5] for i in range(len(sys.argv) - 5)]
genreDict = {x:[] for x in genreKeys}

traverse_dir(dataPath)
write()

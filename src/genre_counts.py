'''
Created on Apr 8, 2014

@author: czar
'''
import os
from os import listdir
from os.path import isfile
import hdf5_getters
import string
import sys
import json


def notValidSong(tags, artist, songName, segmentTimbre, segmentPitches):
    return tags.size ==0 or not artist or not songName or segmentTimbre.size == 0 or segmentPitches.size ==0

def genreInTags(genre,tags):
    for tag in tags:
        if genre in tag:
            return True

def traverseAndWrite(root, genreKeys, counts):
    if not isfile(root):
        for f in listdir(root):
            traverseAndWrite(root + "/" + f,genreKeys,counts)
    else:
        h5 = hdf5_getters.open_h5_file_read(root)
        numOfSongs = hdf5_getters.get_num_songs(h5)
        for index in range(numOfSongs):
            tags = hdf5_getters.get_artist_mbtags(h5,index)
            # print tags
            for genre in genreKeys:
                if genreInTags(genre,tags):
                    counts[genre] +=1
        print counts 
        h5.close()


def writeToDescriptor(fd, obj):
    json.dump(obj,fd)

def main(sys_args):
    dataPath = sys_args[1]
    genreKeys = [sys.argv[i + 2] for i in range(len(sys.argv) - 2)]
    # genreFileDescriptors = {gk:open(genreDir+"/"+gk+".json",'w') for gk, genreDir in zip(genreKeys,genreDirs.keys())}
    count={genre:0 for genre in genreKeys}
    traverseAndWrite(dataPath, genreKeys,count)
    # for key in genreFileDescriptors.keys():
    #     genreFileDescriptors[key].close()

if __name__ == "__main__":
    main(sys.argv)

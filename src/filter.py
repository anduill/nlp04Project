'''
Created on Apr 8, 2014

@author: czar
'''
import os
from os import listdir
from os.path import isfile
import hdf5_getters
import sys


def notValidSong(tags, artist, songName, segmentTimbre, segmentPitches):
    return tags.size ==0 or not artist or not songName or segmentTimbre.size == 0 or segmentPitches.size ==0

def genreInTags(genre,tags):
    for tag in tags:
        if genre in tag:
            return True

def traverseAndWrite(root, genreFileDescriptors):
    if not isfile(root):
        for f in listdir(root):
            traverseAndWrite(root + "/" + f,genreFileDescriptors)
    else:
        h5 = hdf5_getters.open_h5_file_read(root)
        numOfSongs = hdf5_getters.get_num_songs(h5)
        for index in range(numOfSongs):
            tags = hdf5_getters.get_artist_mbtags(h5,index)
            # print tags
            artist = hdf5_getters.get_artist_name(h5,index)
            songName = hdf5_getters.get_title(h5,index)
            segmentTimbre = hdf5_getters.get_segments_timbre(h5,index)
            segmentPitches = hdf5_getters.get_segments_pitches(h5,index)
            if notValidSong(tags, artist, songName, segmentTimbre, segmentPitches):
                h5.close()
                continue
            for genre in genreFileDescriptors.keys():
                if genreInTags(genre,tags):
                    song = {}
                    song['genre'] = genre
                    song['artist_name'] = artist
                    song['song_title'] = songName
                    song['segments_pitches'] = segmentPitches.tolist()
                    song['segments_timbre'] = segmentTimbre.tolist()
                    line = str(song)
                    writeToDescriptor(genre,genreFileDescriptors,line)
        h5.close()


def writeToDescriptor(genreKey, genreDict, line):
    fd = genreDict[genreKey]
    fd.write(line)
    fd.write("\n")

def main(sys_args):
    if len(sys_args) < 6:
        print "ERROR - Parameters Missing"
        print "  <dataPath> <outputPath> <minGenreSize> <maxGenreSize> <genre>+"
        print "  MSC/DATA/PATH/ OUTPUT/PATH 1 10 pop country bachata"
        sys.exit(0)
    dataPath = sys_args[1]
    output_dir = sys_args[2]
    minGenreSize = int(sys_args[3])
    maxGenreSize = int(sys_args[4])
    genreKeys = [sys.argv[i + 5] for i in range(len(sys.argv) - 5)]
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    genreFileDescriptors = {gk:open(output_dir+"/"+gk+".json",'w') for gk in genreKeys}

    traverseAndWrite(dataPath, genreFileDescriptors)
    for key in genreFileDescriptors.keys():
        genreFileDescriptors[key].close()

if __name__ == "__main__":
    main(sys.argv)

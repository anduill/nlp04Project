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
    return False

def traverseAndWrite(root, genreDirs, genreKeys):
    if not isfile(root):
        for f in listdir(root):
            traverseAndWrite(root + "/" + f,genreDirs, genreKeys)
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
            for genre in genreKeys:
                if genreInTags(genre,tags):
                    song = {}
                    song['genre'] = genre
                    song['artist_name'] = artist
                    song['song_title'] = songName
                    song['segments_pitches'] = segmentPitches.tolist()
                    song['segments_timbre'] = segmentTimbre.tolist()

                    valid_chars = "-_.() %s%s" % (string.ascii_letters, string.digits)
                    songName = ''.join(c for c in songName if c in valid_chars)
                    artist = ''.join(c for c in artist if c in valid_chars)
                    fd = open(genreDirs[genre]+"/"+artist+"--"+songName+".json",'a')
                    writeToDescriptor(fd,song)
                    fd.close()
        h5.close()


def writeToDescriptor(fd, obj):
    json.dump(obj,fd)

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
    genreDirs = {genre:output_dir+"/"+genre for genre in genreKeys}
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)
    for genreDir in genreDirs.values():
        if not os.path.exists(genreDir):
            os.makedirs(genreDir)
    # genreFileDescriptors = {gk:open(genreDir+"/"+gk+".json",'w') for gk, genreDir in zip(genreKeys,genreDirs.keys())}

    traverseAndWrite(dataPath, genreDirs, genreKeys)
    # for key in genreFileDescriptors.keys():
    #     genreFileDescriptors[key].close()

if __name__ == "__main__":
    main(sys.argv)

'''
Created on Apr 23, 2014

@author: czar
'''
from os import listdir, makedirs, remove
from os.path import isfile, splitext, basename
from sklearn import svm
import json
import numpy as np
import sys

def traverseAndWrite(root, outputFile, training_size, testing_size, training_set, testing_set, genre_map, genre_count):
    if not isfile(root):
        parent = basename(root)
        for f in listdir(root):
            if genre_count.get(parent,0) == training_size+testing_size:
                return
            traverseAndWrite(root +"/"+ f,outputFile, training_size, testing_size, training_set, testing_set, genre_map, genre_count)
    else: 
        fileName, fileExtension = splitext(root)
        #if fileExtension == '.txt' or testing_size == len(testing_set) or training_size:
        #    return
        jsonFile = ''.join([fileName, ".json"])
        lyricFile = ''.join([fileName, ".txt"])
        #if isfile(jsonFile) and isfile(lyricFile):
            
        try:
            json_data=open(jsonFile)
            data = json.load(json_data)
            json_data.close()
        except (ValueError, IOError):
            f = open(outputFile,'a+')
            f.write(jsonFile)
            f.write('\n')
            f.close()
            try:
                remove(jsonFile)
                remove(lyricFile)
            except OSError:
                print 
            return
        
        genre = data['genre']
        timbre = data['segments_timbre']
        array = np.array(timbre)
        mean = np.mean(array, axis=0).tolist()
        std = np.std(array, axis=0).tolist()
        audioFeatures = mean + std
        
        pitches = data['segments_pitches']
        array = np.array(pitches)
        mean = np.mean(array, axis=0).tolist()
        std = np.std(array, axis=0).tolist()
        
        audioFeatures = audioFeatures + mean + std
        if genre_count[genre] < training_size:
            training_set.append((genre_map[data['genre']], audioFeatures))
            genre_count[genre] += 1
        elif genre_count[genre] < testing_size + training_size:
            testing_set.append((genre_map[data['genre']], audioFeatures))
            genre_count[genre] += 1
            
        
def main (argv):
    dataPath, output_dir, training_size, testing_size = argv[1:] 
    training_size = int(training_size)
    testing_size = int(testing_size)
    genres_count = {f:0 for f in listdir(dataPath)}
    genres = [genre for genre in genres_count.keys()]
    genre_map = {genres[i]:i for i in range(len(genres))}
    makedirs(output_dir)
    training_set = []
    testing_set = []
    traverseAndWrite(dataPath, output_dir+"errorFiles", training_size, testing_size, training_set, testing_set, genre_map, genres_count)
    clf = svm.SVC()
    X = [x[1] for x in training_set]
    Y = [x[0] for x in training_set]
    print clf.fit(X, Y)
    X = [x[1] for x in testing_set]
    Y = [x[0] for x in testing_set]
    results = []
    for i in range (len(X)):
        if clf.predict(X[i]) == Y[i]:
            results.append(Y[i]) 
    print genre_map
    print 0,results.count(0)
    print 1,results.count(1)
    print 2,results.count(2)
    print 3,results.count(3)
    print 4,results.count(4)
    
    clf = svm.LinearSVC()
    X = [x[1] for x in training_set]
    Y = [x[0] for x in training_set]
    print clf.fit(X, Y)
    X = [x[1] for x in testing_set]
    Y = [x[0] for x in testing_set]
    results = []
    for i in range (len(X)):
        if clf.predict(X[i]) == Y[i]:
            results.append(Y[i]) 
    print genre_map
    print 0,results.count(0)
    print 1,results.count(1)
    print 2,results.count(2)
    print 3,results.count(3)
    print 4,results.count(4)
#print mean
#print std

if __name__ == "__main__":
    main(sys.argv)
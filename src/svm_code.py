'''
Created on Apr 23, 2014

@author: czar
'''
from os import listdir, makedirs, remove
from os.path import isfile, splitext, basename, isdir
from sklearn import svm
import json
import numpy as np
import sys

def traverseAndWrite(root, output_dir, data_size, data_set):
    if not isfile(root):
        parent = basename(root)
        for f in listdir(root):
            if len(data_set.get(parent,[])) == data_size:
                return
            traverseAndWrite(root +"/"+ f,output_dir, data_size, data_set)
    else: 
        fileName, fileExtension = splitext(root)
        jsonFile = ''.join([fileName, ".json"])
        lyricFile = ''.join([fileName, ".txt"])
        #if not isfile(jsonFile) or not isfile(lyricFile):
            
        try:
            json_data=open(jsonFile)
            data = json.load(json_data)
            json_data.close()
        except (ValueError, IOError):
            f = open(output_dir,'a+')
            f.write(jsonFile)
            f.write('\n')
            f.close()
            try:
                remove(jsonFile)
                remove(lyricFile)
            except OSError:
                print 
            return
        
        genre = str(data['genre'])
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
        if not data_set.get(genre,[]):
            data_set[genre]=[]
        if len(data_set[genre]) < data_size:
            data_set[genre].append(audioFeatures)
            
        
def main (argv):
    dataPath, output_dir, data_size, cross_validation = argv[1:] 
    data_size = int(data_size)
    cross_validation = int(cross_validation)
    if not isdir(output_dir):
        makedirs(output_dir)
    data_set = {}
    traverseAndWrite(dataPath, output_dir+"errorFiles", data_size, data_set)
    
    #convert genres to numbers
    genre_map={}
    mapping = 0
    for key in sorted(data_set.keys()):
        genre_map[key] = mapping
        mapping +=1
    tmp = {}
    for key in sorted(data_set.keys()):
        tmp[genre_map[key]] = data_set[key]
    data_set = tmp
    print genre_map
    #cross validation
    clf_scores = []
    clf2_scores = []
    for i in range(cross_validation):
        start_index = i*len(data_set[0])/cross_validation
        end_index = (i+1)*len(data_set[0])/cross_validation
        # partition data for testing and training
        training_set = []
        testing_set = []
        for key in data_set.keys():
            for i in range(start_index):
                training_set.append((key,data_set[key][i]))
            for i in range(start_index, end_index):
                testing_set.append((key,data_set[key][i]))
            for i in range(end_index, len(data_set[0])):
                training_set.append((key,data_set[key][i]))
        #normalize training data        
        Y = [x[0] for x in training_set]
        X = [x[1] for x in training_set]
        array = np.array(X)
        std = np.std(array, axis=0)
        normal_array = array/std    
        X = normal_array.tolist()
        
        # create SVM
        clf = svm.SVC()
        clf.fit(X, Y) #feed training data_set
        
        clf2 = svm.LinearSVC()
        clf2.fit(X, Y) #feed training data_set
        
        Y = [x[0] for x in testing_set]
        X = [x[1] for x in testing_set]
        array = np.array(X)
        normal_array = array/std    
        X = normal_array.tolist()
        results = []
        for i in range (len(X)):
            if clf.predict(X[i]) == Y[i]:
                results.append(Y[i])
        clf_scores.append([results.count(i) for i in range(5)])
        
        results = []
        for i in range (len(X)):
            if clf2.predict(X[i]) == Y[i]:
                results.append(Y[i])
        clf2_scores.append([results.count(i) for i in range(5)])
    array = np.array(clf_scores)
    mean = np.mean(array, axis=0).tolist()
    std = np.std(array, axis=0).tolist()
    print mean
    print std
    
    array = np.array(clf2_scores)
    mean = np.mean(array, axis=0).tolist()
    std = np.std(array, axis=0).tolist()
    print mean
    print std
#print mean
#print std

if __name__ == "__main__":
    main(sys.argv)
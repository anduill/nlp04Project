In order to extract the language features (from a set of lyric files), simply run the following java program:

java <classpath> nlp.lm.GenreModels -e electronic -r rock -p pop -pu  punk  -o  out50 -s 8  -sz 50 -t  200

where electronic is the directory with the electronic lyrics, rock is the directory with the rock lyrics...etc
It's worth noting that each of these directory contains both .json and .txt files.  These files come in pairs.
For example, portisHead.json and portisHead.txt are a pair; the .json file contains the audio features and the
.txt file contains the lryics.  This java program scores the lyrics file and appends the results in the .json
The -o option in the command specifies where to dump the new files.
The -s option specifies a seed (must be an integer)
The -sz option specifies the training-file size for each of the Bigrams.
the -t option specifies the size of the testing set.

The java code is a Maven project. Simply run "mvn clean compile"  It is probably a good idea to skip tests.

=========================================
The filter.py script is used to preprocess the MSD into a nicely structured directory where each genre is a 
directory and the content of that directory are the songs with that genre in its tags.

To run:
python filter.py <dataPath> <outputDir> <minGenreSize> <maxGenreSize> <genre>+

<dataPath> is the root directory containing the data. For example, the directory containing the genre directories
<outputDir> is the root directory where to write the output of the program
<minGenreSize> the minimum number of songs desired to be in a genre directory
<maxGenreSize> the maximum number of songs desired to be in a genre directory
<genre> a genre label string
==================================
The classification.py script is the implementation of our experimental set up. It trains the classifiers 
and evaluates them.

Note: that scikit needs to be installed in order to use this script

To run:
python classification.py <dataPath> <outputDir> <data_size> <cross_validation> <use_audio_features> <use_lyric_features>

<dataPath> is the root directory containing the data. For example, the directory containing the genre directories
<outputDir> is the root directory where to write the output of the program
<data_size> how much data to read for each genre from the <dataPath> dir 
<cross_validation> how many folds to use during evaluation
<use_audio_features> append the audio features from the MSD into the feature vector used for classification
<use_lyric_features> append the lyric features made from the Bigram into the feature vector used for classification

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
